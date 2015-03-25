package net.netcoding.niftybungee.minecraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ProxyReloadEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.netcoding.niftybungee.NiftyBungee;
import net.netcoding.niftybungee.util.ByteUtil;

import com.google.gson.JsonObject;

public class BungeeHelper implements Listener {

	public static final String BUNGEE_CHANNEL = "BungeeCord";
	public static final String NIFTY_CHANNEL = "NiftyBungee";
	private volatile boolean running = false;
	private volatile int totalServers = 0;
	private volatile int processedServers = 0;
	private int taskId = -1;

	public BungeeHelper() {
		this.startThread();
	}

	/**
	 * When the proxy gets reloaded, resend the list of servers and information.
	 */
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onProxyReload(ProxyReloadEvent event) {
		this.stopThread(false);

		for (ServerInfo serverInfo : NiftyBungee.getPlugin().getProxy().getServers().values()) {
			if (serverInfo.getPlayers().size() == 0) continue;
			serverInfo.sendData(NIFTY_CHANNEL, ByteUtil.toByteArray("BungeeInfo", ProxyServer.getInstance().getConfig().isOnlineMode()));
			sendServerList(serverInfo);
		}

		this.startThread();
	}

	/**
	 * When a user joins or switches servers, if the joining server had 0 players
	 * then notify said server of all bungee servers to cache with, and send out
	 * a PlayerJoin event to all servers.
	 */
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onServerSwitch(ServerSwitchEvent event) {
		final ServerInfo connected = event.getPlayer().getServer().getInfo();

		if (connected.getPlayers().size() == 1) {
			connected.sendData(NIFTY_CHANNEL, ByteUtil.toByteArray("BungeeInfo", ProxyServer.getInstance().getConfig().isOnlineMode()));
			sendServerList(connected);
		}

		for (ServerInfo serverInfo : NiftyBungee.getPlugin().getProxy().getServers().values()) {
			if (serverInfo.getPlayers().size() == 0) continue;
			serverInfo.sendData(NIFTY_CHANNEL, ByteUtil.toByteArray("PlayerJoin", connected.getName(), parsePlayerInfo(event.getPlayer(), true)));
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

		for (ServerInfo serverInfo : NiftyBungee.getPlugin().getProxy().getServers().values()) {
			if (serverInfo.getPlayers().size() > 0)
				serverInfo.sendData(NIFTY_CHANNEL, ByteUtil.toByteArray("PlayerLeave", disconnect.getName(), parsePlayerInfo(event.getPlayer(), false)));
		}
	}

	public static String parseServerInfo(ServerInfo info) {
		JsonObject json = new JsonObject();
		json.addProperty("name", info.getName());
		json.addProperty("ip", info.getAddress().getHostString());
		json.addProperty("port", info.getAddress().getPort());
		return json.toString();
	}

	public static String parsePlayerInfo(ProxiedPlayer player, boolean address) {
		JsonObject json = new JsonObject();
		json.addProperty("id", player.getUniqueId().toString());
		json.addProperty("name", player.getName());

		if (address) {
			json.addProperty("ip", player.getAddress().getHostString());
			json.addProperty("port", player.getAddress().getPort());
		}

		return json.toString();
	}

	private void sendServerInfo(ServerInfo sendThis, Collection<ServerInfo> toHere, boolean updatePlayers, final boolean thread) {
		new BungeeServer(sendThis, new ServerPingCallback(toHere, updatePlayers)).ping(new Runnable() {
			@Override
			public void run() {
				if (thread) {
					if (++processedServers == totalServers) {
						processedServers = 0;
						running = false;
						startThread();
					}
				}
			}
		});
	}

	private void sendServerList(ServerInfo toHere) {
		List<Object> servers = new ArrayList<>();
		servers.add("GetServers");
		servers.add(NiftyBungee.getPlugin().getProxy().getServers().size());

		for (ServerInfo serverInfo : NiftyBungee.getPlugin().getProxy().getServers().values())
			servers.add(parseServerInfo(serverInfo));

		toHere.sendData(NIFTY_CHANNEL, ByteUtil.toByteArray(servers));

		for (ServerInfo serverInfo : NiftyBungee.getPlugin().getProxy().getServers().values())
			sendServerInfo(serverInfo, Arrays.asList(toHere), true, false);
	}

	private void startThread() {
		if (!this.running && this.taskId > -2) {
			this.taskId = NiftyBungee.getPlugin().getProxy().getScheduler().runAsync(NiftyBungee.getPlugin(), new Runnable() {
				@Override
				public void run() {
					Collection<ServerInfo> servers = NiftyBungee.getPlugin().getProxy().getServers().values();
					int count = servers.size();
					totalServers = count;
					processedServers = 0;

					for (ServerInfo sendThis : servers)
						sendServerInfo(sendThis, servers, true, true);
				}
			}).getId();
		}
	}

	public void stopThread() {
		this.stopThread(true);
	}

	private void stopThread(boolean hard) {
		if (this.taskId > 0) {
			this.running = false;
			NiftyBungee.getPlugin().getProxy().getScheduler().cancel(this.taskId);
			this.taskId = (hard ? -2 : -1);
		}
	}

}