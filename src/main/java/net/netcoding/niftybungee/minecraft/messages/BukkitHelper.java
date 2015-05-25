package net.netcoding.niftybungee.minecraft.messages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ProxyReloadEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.netcoding.niftybungee.NiftyBungee;
import net.netcoding.niftybungee.minecraft.BungeeHelper;
import net.netcoding.niftybungee.minecraft.BungeeListener;
import net.netcoding.niftybungee.minecraft.ping.BungeeInfoServer;
import net.netcoding.niftycore.minecraft.scheduler.MinecraftScheduler;
import net.netcoding.niftycore.mojang.MojangProfile;
import net.netcoding.niftycore.util.ByteUtil;
import net.netcoding.niftycore.util.concurrent.ConcurrentSet;

import com.google.gson.JsonObject;

public class BukkitHelper extends BungeeHelper {

	public static final String BUNGEE_CHANNEL = "BungeeCord";
	public static final String NIFTY_CHANNEL = "NiftyBungee";
	private static final ConcurrentHashMap<String, BungeeInfoServer> SERVERS = new ConcurrentHashMap<>();
	private PlayerListener playerListener = new PlayerListener();
	private ConcurrentSet<String> processed = new ConcurrentSet<>();
	private volatile boolean stopped = false;
	private boolean hardStopped = false;
	//private volatile int taskId = -1;
	private long startTime = 0;
	private final Runnable threadUpdate = new Runnable() {
		@Override
		public void run() {
			startThread();
		}
	};

	static {
		for (ServerInfo server : ProxyServer.getInstance().getServers().values())
			SERVERS.put(server.getName(), new BungeeInfoServer(server, null));
	}

	public BukkitHelper(Plugin plugin) {
		super(plugin);
		this.register();
	}

	public BungeeInfoServer getServer(ServerInfo info) {
		for (BungeeInfoServer server : this.getServers()) {
			if (server.getName().equals(info.getName()))
				return server;
		}

		return null;
	}

	public BungeeInfoServer getServer(String serverName) {
		return SERVERS.get(serverName);
	}

	public Set<BungeeInfoServer> getServers() {
		return Collections.unmodifiableSet(new HashSet<>(SERVERS.values()));
	}

	public static String parseServerInfo(BungeeInfoServer server) {
		JsonObject json = new JsonObject();
		json.addProperty("name", server.getName());
		json.addProperty("ip", server.getAddress().getAddress().getHostAddress());
		json.addProperty("port", server.getAddress().getPort());
		return json.toString();
	}

	public static String parsePlayerInfo(MojangProfile profile, boolean address) {
		JsonObject json = new JsonObject();
		json.addProperty("id", profile.getUniqueId().toString());
		json.addProperty("name", profile.getName());

		if (address) {
			json.addProperty("ip", profile.getAddress().getAddress().getHostAddress());
			json.addProperty("port", profile.getAddress().getPort());
		}

		return json.toString();
	}

	private void sendServerInfo(BungeeInfoServer server) {
		server.setRunnable(this.threadUpdate);
		server.ping();
	}

	private static void sendServerList(BungeeInfoServer server) {
		List<Object> servers = new ArrayList<>();
		servers.add("GetServers");
		servers.add(NiftyBungee.getPlugin().getProxy().getServers().size());

		for (BungeeInfoServer bungeeServer : NiftyBungee.getBukkitHelper().getServers())
			servers.add(parseServerInfo(bungeeServer));

		server.sendData(BukkitHelper.NIFTY_CHANNEL, ByteUtil.toByteArray(servers));
	}

	private void startThread() {
		if (!this.hardStopped) {
			final Collection<BungeeInfoServer> servers = NiftyBungee.getBukkitHelper().getServers();
			long delay = 20;

			if (processed.size() == servers.size()) {
				delay = System.currentTimeMillis() - startTime;
				long compare = delay + (servers.size() * 5);

				if (compare < 500)
					delay = 500 - compare;
			}

			MinecraftScheduler.runAsync(new Runnable() {
				@Override
				public void run() {
					if (servers.size() > 0) {
						Iterator<BungeeInfoServer> iterator = servers.iterator();
						BungeeInfoServer server = null;

						while (iterator.hasNext()) {
							if (!processed.contains((server = iterator.next()).getName()))
								break;
						}

						if (processed.size() == servers.size()) {
							processed.clear();
							server = servers.iterator().next();
						}

						if (processed.size() == 0)
							startTime = System.currentTimeMillis();

						processed.add(server.getName());

						if (stopped)
							return;

						sendServerInfo(server);
					}
				}
			}, delay);
		}
	}

	public void stopThread() {
		stopThread(true);
	}

	private void stopThread(boolean hard) {
		if (this.hardStopped)
			return;

		if (!this.stopped) {
			this.stopped = true;
			this.hardStopped = hard;
		}
	}

	public void register() {
		if (!ProxyServer.getInstance().getChannels().contains(NIFTY_CHANNEL)) {
			ProxyServer.getInstance().registerChannel(NIFTY_CHANNEL);
			this.playerListener = new PlayerListener();
			MinecraftScheduler.schedule(threadUpdate, 1);
		}
	}

	public void unregister() {
		if (ProxyServer.getInstance().getChannels().contains(NIFTY_CHANNEL)) {
			ProxyServer.getInstance().unregisterChannel(NIFTY_CHANNEL);
			ProxyServer.getInstance().getPluginManager().unregisterListener(this.playerListener);
		}
	}

	protected class PlayerListener extends BungeeListener {

		public PlayerListener() {
			super(NiftyBungee.getPlugin());
		}

		/**
		 * When the proxy gets reloaded, resend the list of servers and information.
		 */
		@EventHandler
		public void onProxyReload(ProxyReloadEvent event) {
			stopThread(false);

			for (BungeeInfoServer server : getServers()) {
				if (server.getPlayerList().size() == 0) continue;
				server.sendData(BukkitHelper.NIFTY_CHANNEL, ByteUtil.toByteArray("BungeeInfo", ProxyServer.getInstance().getConfig().isOnlineMode()));
				sendServerList(server);
			}

			startThread();
		}

		/**
		 * When a user joins or switches servers, if the joining server had 0 players
		 * then notify said server of all bungee servers to cache with, and send out
		 * a PlayerJoin event to all servers.
		 */
		@EventHandler
		public void onServerSwitch(ServerSwitchEvent event) {
			final ServerInfo connected = event.getPlayer().getServer().getInfo();
			MojangProfile profile = NiftyBungee.getMojangRepository().searchByPlayer(event.getPlayer());
			BungeeInfoServer bungeeServer = NiftyBungee.getBukkitHelper().getServer(connected);

			if (bungeeServer.getPlayerList().size() == 1) {
				bungeeServer.sendData(BukkitHelper.NIFTY_CHANNEL, ByteUtil.toByteArray("BungeeInfo", ProxyServer.getInstance().getConfig().isOnlineMode()));
				sendServerList(bungeeServer);
			}

			for (BungeeInfoServer server : getServers()) {
				if (server.getPlayerList().size() == 0) continue;
				server.sendData(BukkitHelper.NIFTY_CHANNEL, ByteUtil.toByteArray("PlayerJoin", bungeeServer.getName(), parsePlayerInfo(profile, true)));
			}
		}

		/**
		 * When a user leaves a server, send all servers with more than 0 players
		 * a PlayerLeave event, and if the left server now has 0 players, notify all
		 * other servers with more than 0 players to reset the cached server info.
		 */
		@EventHandler
		public void onServerDisconnect(ServerDisconnectEvent event) {
			final ServerInfo disconnect = event.getTarget();
			MojangProfile profile = NiftyBungee.getMojangRepository().searchByPlayer(event.getPlayer());

			for (BungeeInfoServer server : getServers()) {
				if (server.getPlayerList().size() == 0) continue;
				server.sendData(BukkitHelper.NIFTY_CHANNEL, ByteUtil.toByteArray("PlayerLeave", disconnect.getName(), parsePlayerInfo(profile, false)));
			}
		}

	}

}