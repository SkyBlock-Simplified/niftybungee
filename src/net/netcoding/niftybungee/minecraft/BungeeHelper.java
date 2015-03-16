package net.netcoding.niftybungee.minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ProxyReloadEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.netcoding.niftybungee.NiftyBungee;
import net.netcoding.niftybungee.util.ByteUtil;
import net.netcoding.niftybungee.util.StringUtil;
import net.netcoding.niftybungee.util.concurrent.ConcurrentList;

public class BungeeHelper implements Listener {

	public static final String NIFTY_CHANNEL = "NiftyBungee";
	private static final ConcurrentHashMap<String, Boolean> CACHE = new ConcurrentHashMap<>();
	private List<String> sentBungeeInfo = new ArrayList<>();

	static {
		for (ServerInfo serverInfo : NiftyBungee.getPlugin().getProxy().getServers().values())
			CACHE.put(serverInfo.getName(), true);
	}

	public BungeeHelper() {
		runThread();
	}

	/**
	 * When the proxy gets reloaded, resend the list of servers and information
	 */
	@EventHandler
	public void onProxyReload(ProxyReloadEvent event) {
		CACHE.clear();
		sentBungeeInfo.clear();

		for (ServerInfo serverInfo : NiftyBungee.getPlugin().getProxy().getServers().values())
			sendServerList(serverInfo);
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
			if (!sentBungeeInfo.contains(connected.getName())) {
				sentBungeeInfo.add(connected.getName());
				connected.sendData(NIFTY_CHANNEL, ByteUtil.toByteArray("BungeeInfo", ProxyServer.getInstance().getConfig().isOnlineMode()));
			}

			sendServerList(connected);
		}

		for (ServerInfo serverInfo : NiftyBungee.getPlugin().getProxy().getServers().values())
			serverInfo.sendData(NIFTY_CHANNEL, ByteUtil.toByteArray("PlayerJoin", connected.getName(), parsePlayerInfo(event.getPlayer())));
	}

	/**
	 * When a user leaves the network, send all servers with more than 0 players
	 * a PlayerLeave event, and if the left server now has 0 players, notify all
	 * other servers with more than 0 players to reset the cached server info.
	 */
	@EventHandler
	public void onServerDisconnect(ServerDisconnectEvent event) {
		final ServerInfo disconnect = event.getTarget();

		for (ServerInfo serverInfo : NiftyBungee.getPlugin().getProxy().getServers().values())
			serverInfo.sendData(NIFTY_CHANNEL, ByteUtil.toByteArray("PlayerLeave", disconnect.getName(), event.getPlayer().getUniqueId()));
	}

	public static String parsePlayerInfo(ProxiedPlayer player) {
		return StringUtil.format("{0},{1},{2},{3}", player.getUniqueId().toString(), player.getName(), player.getAddress().getHostString(), String.valueOf(player.getAddress().getPort()));
	}

	private static void runThread() {
		NiftyBungee.getPlugin().getProxy().getScheduler().runAsync(NiftyBungee.getPlugin(), new NotifyServers());
	}

	private static void sendServerInfo(ServerInfo sendThis, ServerInfo toHere, boolean updatePlayers) {
		sendThis.ping(new ServerPingCallback(sendThis, toHere, updatePlayers));
	}

	private static void sendServerList(ServerInfo toHere) {
		List<Object> servers = new ArrayList<>();
		servers.add("GetServers");
		servers.add(NiftyBungee.getPlugin().getProxy().getServers().size());

		for (ServerInfo serverInfo : NiftyBungee.getPlugin().getProxy().getServers().values()) {
			servers.add(serverInfo.getName());
			servers.add(serverInfo.getAddress().getHostString());
			servers.add(serverInfo.getAddress().getPort());
		}

		toHere.sendData(NIFTY_CHANNEL, ByteUtil.toByteArray(servers));

		for (ServerInfo serverInfo : NiftyBungee.getPlugin().getProxy().getServers().values())
			sendServerInfo(serverInfo, toHere, true);
	}

	private static class NotifyServers implements Runnable {

		@Override
		public void run() {
			ConcurrentList<ServerInfo> servers = new ConcurrentList<ServerInfo>(NiftyBungee.getPlugin().getProxy().getServers().values());

			for (final ServerInfo testThis : servers) {
				testThis.ping(new Callback<ServerPing>() {
					@Override
					public void done(ServerPing result, Throwable error) {
						boolean changeDetected = false;
						boolean playerUpdate = false;

						if (result == null || result.getPlayers().getOnline() == 0) {
							if (!CACHE.containsKey(testThis.getName()) || CACHE.get(testThis.getName())) {
								CACHE.put(testThis.getName(), false);
								changeDetected = true;
								playerUpdate = false;
							}
						} else {
							if (!CACHE.containsKey(testThis.getName()) || !CACHE.get(testThis.getName())) {
								CACHE.put(testThis.getName(), true);
								changeDetected = true;
								playerUpdate = true;
							}
						}

						if (changeDetected) {
							for (ServerInfo serverInfo : NiftyBungee.getPlugin().getProxy().getServers().values()) {
								if (serverInfo.equals(testThis)) continue;
								sendServerInfo(testThis, serverInfo, playerUpdate);
							}
						}
					}
				});
			}

			runThread();
		}

	}

}