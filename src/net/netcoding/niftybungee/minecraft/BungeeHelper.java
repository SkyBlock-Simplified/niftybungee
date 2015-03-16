package net.netcoding.niftybungee.minecraft;

import java.util.ArrayList;
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
import net.netcoding.niftybungee.util.StringUtil;

public class BungeeHelper implements Listener {

	public static final String BUNGEE_CHANNEL = "BungeeCord";
	public static final String NIFTY_CHANNEL = "NiftyBungee";
	private List<String> sentBungeeInfo = new ArrayList<>();

	/**
	 * When the proxy gets reloaded, resend the list of servers and information.
	 */
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onProxyReload(ProxyReloadEvent event) {
		for (ServerInfo serverInfo : NiftyBungee.getPlugin().getProxy().getServers().values()) {
			serverInfo.sendData(NIFTY_CHANNEL, ByteUtil.toByteArray("BungeeInfo", ProxyServer.getInstance().getConfig().isOnlineMode()));
			sendServerList(serverInfo);
		}
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
	 * When a user leaves a server, send all servers with more than 0 players
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

}