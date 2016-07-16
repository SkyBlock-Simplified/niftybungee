package net.netcoding.nifty.bungee.api.plugin.messaging;

import com.google.gson.JsonObject;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ProxyReloadEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.event.EventHandler;
import net.netcoding.nifty.bungee.NiftyBungee;
import net.netcoding.nifty.bungee.api.BungeeHelper;
import net.netcoding.nifty.bungee.api.BungeeListener;
import net.netcoding.nifty.bungee.api.ping.BungeeInfoPingServer;
import net.netcoding.nifty.bungee.api.plugin.BungeePlugin;
import net.netcoding.nifty.bungee.mojang.BungeeMojangProfile;
import net.netcoding.nifty.core.api.scheduler.MinecraftScheduler;
import net.netcoding.nifty.core.util.ByteUtil;
import net.netcoding.nifty.core.util.concurrent.Concurrent;
import net.netcoding.nifty.core.util.concurrent.ConcurrentMap;
import net.netcoding.nifty.core.util.concurrent.ConcurrentSet;
import net.netcoding.nifty.core.util.misc.ServerSocketWrapper;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BukkitHelper extends BungeeHelper {

	public static final String BUNGEE_CHANNEL = "BungeeCord";
	public static final String NIFTY_CHANNEL = "NiftyBungee";
	private static final ConcurrentMap<String, BungeeInfoPingServer> SERVERS = Concurrent.newMap();
	private static final ServerSocketWrapper SOCKET_WRAPPER;
	private final ConcurrentSet<String> processed = Concurrent.newSet();
	private PlayerListener playerListener;
	private volatile boolean stopped = false;
	private volatile long lastPing = System.currentTimeMillis();
	private volatile int pingTaskId = -1;
	private int rebootTaskId = -1;
	private long startTime = 0;
	private final long defaultDelay = 100L;
	private long delay = defaultDelay;
	private final Runnable threadUpdate = () -> {
		this.lastPing = System.currentTimeMillis();

		try {
			Thread.sleep(this.delay);
		} catch (Exception ignore) { }

		this.pingServer();
	};

	static {
		for (ServerInfo server : ProxyServer.getInstance().getServers().values())
			SERVERS.put(server.getName(), new BungeeInfoPingServer(server, null));

		ServerSocket socket = null;

		/*try {
			socket = new ServerSocket(0);
			socket.setSoTimeout(2000);
		} catch (Exception ex) {
			ex.printStackTrace();
		}*/

		SOCKET_WRAPPER = new ServerSocketWrapper(socket);
	}

	public BukkitHelper(BungeePlugin plugin) {
		super(plugin);
		this.register();
	}

	public BungeeInfoPingServer getServer(ServerInfo info) {
		for (BungeeInfoPingServer server : this.getServers()) {
			if (server.getName().equals(info.getName()))
				return server;
		}

		return null;
	}

	public BungeeInfoPingServer getServer(String serverName) {
		return SERVERS.get(serverName);
	}

	public Set<BungeeInfoPingServer> getServers() {
		return Collections.unmodifiableSet(new HashSet<>(SERVERS.values()));
	}

	public static ServerSocketWrapper getSocketWrapper() {
		return SOCKET_WRAPPER;
	}

	public static String parseServerInfo(BungeeInfoPingServer server) {
		JsonObject json = new JsonObject();
		json.addProperty("name", server.getName());
		json.addProperty("ip", server.getAddress().getAddress().getHostAddress());
		json.addProperty("port", server.getAddress().getPort());
		return json.toString();
	}

	public static String parsePlayerInfo(BungeeMojangProfile profile, boolean address) {
		JsonObject json = new JsonObject();
		json.addProperty("id", profile.getUniqueId().toString());
		json.addProperty("name", profile.getName());

		if (address) {
			String ip;
			int port;

			try {
				ip = profile.getAddress().getAddress().getHostAddress();
				port = profile.getAddress().getPort();
			} catch (Exception ex) {
				ip = "";
				port = 0;
			}

			json.addProperty("ip", ip);
			json.addProperty("port", port);
		}

		return json.toString();
	}

	public void register() {
		if (!ProxyServer.getInstance().getChannels().contains(NIFTY_CHANNEL)) {
			ProxyServer.getInstance().registerChannel(NIFTY_CHANNEL);
			this.playerListener = new PlayerListener();
			this.pingTaskId = MinecraftScheduler.getInstance().runAsync(this::pingServer).getId();
			this.rebootTaskId = MinecraftScheduler.getInstance().runAsync(() -> {
				long current = System.currentTimeMillis();

				if ((current - this.lastPing) > 500L) {
					this.lastPing = current;
					this.processed.clear();
					this.stopThread();
					this.stopped = false;
					this.pingServer();
				}
			}, 1000L, this.defaultDelay).getId();
		}
	}

	private static void sendServerList(BungeeInfoPingServer server) {
		List<Object> servers = new ArrayList<>();
		servers.add("GetServers");
		servers.add(NiftyBungee.getPlugin().getProxy().getServers().size());
		servers.addAll(NiftyBungee.getBukkitHelper().getServers().stream().map(BukkitHelper::parseServerInfo).collect(Collectors.toList()));
		server.sendData(BukkitHelper.NIFTY_CHANNEL, ByteUtil.toByteArray(servers));
	}

	private void pingServer() {
		if (!this.stopped) {
			final Collection<BungeeInfoPingServer> servers = this.getServers();

			if (servers.isEmpty()) {
				this.stopThread();
				return;
			}

			if (processed.size() == servers.size()) {
				long delay = ((System.currentTimeMillis() - this.startTime) + (this.defaultDelay * servers.size())) / servers.size();
				this.delay = Math.max(50L, Math.min(250L, delay));
				this.processed.clear();
			}

			if (processed.size() == 0)
				this.startTime = System.currentTimeMillis();

			if (servers.size() > 0) {
				Iterator<BungeeInfoPingServer> iterator = servers.iterator();
				BungeeInfoPingServer server = null;

				while (iterator.hasNext()) {
					if (!this.processed.contains((server = iterator.next()).getName()))
						break;
				}

				this.processed.add(server.getName());
				server.setRunnable(this.threadUpdate);
				server.ping();
			}
		}
	}

	private void stopThread() {
		if (!this.stopped) {
			this.stopped = true;

			if (this.pingTaskId > 0) {
				try {
					MinecraftScheduler.getInstance().cancel(this.pingTaskId);
				} catch (Exception ignore) { }
			}
		}
	}

	public void unregister() {
		if (ProxyServer.getInstance().getChannels().contains(NIFTY_CHANNEL)) {
			ProxyServer.getInstance().unregisterChannel(NIFTY_CHANNEL);
			ProxyServer.getInstance().getPluginManager().unregisterListener(this.playerListener);

			if (this.pingTaskId > 0) {
				try {
					MinecraftScheduler.getInstance().cancel(this.rebootTaskId);
				} catch (Exception ignore) { }
			}

			this.stopThread();

			/*if (getSocketWrapper().isSocketListening()) {
				try {
					System.out.println("Closing socket?");
					SOCKET.close();
					System.out.println("Socket closed?");
				} catch (Exception ignored) { }
			}*/
		}
	}

	protected class PlayerListener extends BungeeListener {

		private PlayerListener() {
			super(NiftyBungee.getPlugin());
		}

		/**
		 * When the proxy gets reloaded, resend the list of servers and information.
		 */
		@EventHandler
		public void onProxyReload(ProxyReloadEvent event) {
			if (!BukkitHelper.this.stopped) {
				BukkitHelper.this.stopThread();

				getServers().stream().filter(server -> !server.getPlayerList().isEmpty()).forEach(server -> {
					server.sendData(BukkitHelper.NIFTY_CHANNEL, ByteUtil.toByteArray("BungeeInfo", ProxyServer.getInstance().getConfig().isOnlineMode()));
					sendServerList(server);
				});

				BukkitHelper.this.stopped = false;
				BukkitHelper.this.pingServer();
			}
		}

		/**
		 * When a user joins or switches servers, if the joining server had 0 players
		 * then notify said server of all bungee servers to cache with, and send out
		 * a PlayerJoin event to all servers.
		 */
		@EventHandler
		public void onServerSwitch(ServerSwitchEvent event) {
			if (!BukkitHelper.this.stopped) {
				BungeeMojangProfile profile = NiftyBungee.getMojangRepository().searchByPlayer(event.getPlayer());
				BungeeInfoPingServer bungeeServer = profile.getServer();

				if (bungeeServer.getPlayerList().size() == 1) {
					bungeeServer.sendData(BukkitHelper.NIFTY_CHANNEL, ByteUtil.toByteArray("BungeeInfo", ProxyServer.getInstance().getConfig().isOnlineMode()));
					BukkitHelper.sendServerList(bungeeServer);
				}

				BukkitHelper.this.getServers().stream().filter(server -> !server.getPlayerList().isEmpty())
						.forEach(server -> server.sendData(BukkitHelper.NIFTY_CHANNEL, ByteUtil.toByteArray("PlayerJoin", bungeeServer.getName(), parsePlayerInfo(profile, true))));
			}
		}

		/**
		 * When a user leaves a server, send all servers with more than 0 players
		 * a PlayerLeave event, and if the left server now has 0 players, notify all
		 * other servers with more than 0 players to reset the cached server info.
		 */
		@EventHandler
		public void onServerDisconnect(ServerDisconnectEvent event) {
			if (!BukkitHelper.this.stopped) {
				BungeeMojangProfile profile = NiftyBungee.getMojangRepository().searchByPlayer(event.getPlayer());
				BungeeInfoPingServer bungeeServer = getServer(event.getTarget());
				BukkitHelper.this.getServers().stream().filter(server -> !server.getPlayerList().isEmpty())
						.forEach(server -> server.sendData(BukkitHelper.NIFTY_CHANNEL, ByteUtil.toByteArray("PlayerLeave", bungeeServer.getName(), parsePlayerInfo(profile, false))));
			}
		}

	}

}