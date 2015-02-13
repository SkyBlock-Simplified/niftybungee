package net.netcoding.niftybungee.minecraft;

import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.netcoding.niftybungee.NiftyBungee;
import net.netcoding.niftybungee.util.ByteUtil;

public class BungeeHelper implements Listener {

	public static final String NIFTY_CHANNEL = "NiftyBungee";

	private void sendServerInfo(ServerInfo sendThis, ServerInfo toHere) {
		sendThis.ping(new ServerPingCallback(sendThis, toHere));
	}

	private void sendOfflineServer(final ServerInfo offline) {
		offline.ping(new Callback<ServerPing>() {
			@Override
			public void done(ServerPing result, Throwable error) {
				if (result == null || result.getPlayers().getOnline() == 0) {
					for (ServerInfo serverInfo : NiftyBungee.getPlugin().getProxy().getServers().values()) {
						if (serverInfo.equals(offline) || serverInfo.getPlayers().size() == 0) continue;
						sendServerInfo(offline, serverInfo);
					}
				}
			}
		});
	}

	/**
	 * When a user joins or switches servers, if the joining server had 0 players
	 * then notify said server of all bungee servers to cache with, and send out
	 * a PlayerJoin event to all servers.
	 */
	@EventHandler
	public void onServerSwitch(ServerSwitchEvent event) {
		final ServerInfo connected = event.getPlayer().getServer().getInfo();

		if (connected.getPlayers().size() == 1) {
			List<Object> servers = new ArrayList<>();
			servers.add("GetServers");
			servers.add(NiftyBungee.getPlugin().getProxy().getServers().size());

			for (ServerInfo serverInfo : NiftyBungee.getPlugin().getProxy().getServers().values()) {
				servers.add(serverInfo.getName());
				servers.add(serverInfo.getAddress().getHostString());
				servers.add(serverInfo.getAddress().getPort());
			}

			connected.sendData(NIFTY_CHANNEL, ByteUtil.toByteArray(servers));

			for (ServerInfo serverInfo : NiftyBungee.getPlugin().getProxy().getServers().values())
				this.sendServerInfo(serverInfo, connected);
		}

		for (ServerInfo serverInfo : NiftyBungee.getPlugin().getProxy().getServers().values()) {
			if (serverInfo.getPlayers().size() == 0) continue;
			serverInfo.sendData(NIFTY_CHANNEL, ByteUtil.toByteArray("PlayerJoin", connected.getName(), event.getPlayer().getUniqueId(), event.getPlayer().getName()));
		}
	}

	/**
	 * When a user leaves the network, send all servers with more than 0 players
	 * a PlayerLeave event, and if the left server now has 0 players, notify all
	 * other servers with more than 0 players to reset the cached server info.
	 */
	@EventHandler
	public void onServerDisconnect(ServerDisconnectEvent event) {
		final ServerInfo disconnect = event.getTarget();

		for (ServerInfo serverInfo : NiftyBungee.getPlugin().getProxy().getServers().values()) {
			if (serverInfo.getPlayers().size() == 0)
				this.sendOfflineServer(serverInfo);
			else
				serverInfo.sendData(NIFTY_CHANNEL, ByteUtil.toByteArray("PlayerLeave", disconnect.getName(), event.getPlayer().getUniqueId()));
		}
	}

}