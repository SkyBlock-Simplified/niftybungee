package net.netcoding.niftybungee.minecraft;

import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.netcoding.niftybungee.NiftyBungee;
import net.netcoding.niftybungee.util.ByteUtil;

public class BungeeHelper implements Listener {

	public static final String BUNGEE_CHANNEL = "BungeeCord";
	public static final String NIFTY_CHANNEL = "NiftyBungee";

	private void sendServerInfo(ServerInfo sendThis, ServerInfo toHere) {
		sendThis.ping(new ServerPingCallback(sendThis, toHere));
	}

	@EventHandler
	public void onServerConnected(ServerConnectedEvent event) {
		final ServerInfo currentServer = event.getServer().getInfo();

		if (currentServer.getPlayers().size() == 0) {
			List<Object> servers = new ArrayList<>();
			servers.add("GetServers");
			servers.add(NiftyBungee.getPlugin().getProxy().getServers().size());

			for (ServerInfo serverInfo : NiftyBungee.getPlugin().getProxy().getServers().values()) {
				servers.add(serverInfo.getName());
				servers.add(serverInfo.getAddress().getHostString());
				servers.add(serverInfo.getAddress().getPort());
			}

			currentServer.sendData(NIFTY_CHANNEL, ByteUtil.toByteArray(servers));

			for (ServerInfo serverInfo : NiftyBungee.getPlugin().getProxy().getServers().values())
				this.sendServerInfo(serverInfo, currentServer);
		}

		for (ServerInfo serverInfo : NiftyBungee.getPlugin().getProxy().getServers().values()) {
			if (serverInfo.getPlayers().size() == 0) continue;
			serverInfo.sendData(NIFTY_CHANNEL, ByteUtil.toByteArray("PlayerJoin", currentServer.getName(), event.getPlayer().getName(), event.getPlayer().getUniqueId().toString()));
		}
	}

	@EventHandler
	public void onServerDisconnect(ServerDisconnectEvent event) {
		final ServerInfo leftServer = event.getTarget();

		for (ServerInfo serverInfo : NiftyBungee.getPlugin().getProxy().getServers().values()) {
			if (serverInfo.getPlayers().size() == 0) continue;
			serverInfo.sendData(NIFTY_CHANNEL, ByteUtil.toByteArray("PlayerLeave", leftServer.getName(), event.getPlayer().getUniqueId().toString()));
		}

		if (leftServer.getPlayers().size() <= 1) {
			leftServer.ping(new Callback<ServerPing>() {
				@Override
				public void done(ServerPing result, Throwable error) {
					if (result == null) {
						for (ServerInfo serverInfo : NiftyBungee.getPlugin().getProxy().getServers().values()) {
							if (leftServer.equals(serverInfo)) continue;
							serverInfo.sendData(NIFTY_CHANNEL, ByteUtil.toByteArray("ServerOffline", leftServer.getName()));
						}
					}
				}
			});
		}
	}

}