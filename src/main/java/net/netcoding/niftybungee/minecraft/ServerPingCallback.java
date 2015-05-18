package net.netcoding.niftybungee.minecraft;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.netcoding.niftycore.util.ByteUtil;

public class ServerPingCallback implements ServerPingListener {

	private final Collection<ServerInfo> targets;
	private final boolean updatePlayers;

	public ServerPingCallback(Collection<ServerInfo> targets, boolean updatePlayers) {
		this.targets = targets;
		this.updatePlayers = updatePlayers;
	}

	/**
	 * When a ping on a server is completed, send the resulting
	 * information to the requesting server.
	 * 
	 * @param server The server which was pinged.
	 */
	@Override
	public void onServerPing(MinecraftServer server) {
		List<Object> objs = new ArrayList<>();
		objs.add("ServerInfo");
		objs.add(server.getName());
		objs.add(server.isOnline());

		if (server.isOnline()) {
			objs.add(server.getMotd());
			objs.add(server.getVersion().getName());
			objs.add(server.getVersion().getProtocol());
			objs.add(server.getMaxPlayers());
			objs.add(this.updatePlayers);

			if (this.updatePlayers) {
				objs.add(server.getPlayerList().size());

				for (ProxiedPlayer player : server.getPlayerList())
					objs.add(BungeeHelper.parsePlayerInfo(player, true));
			}
		}

		byte[] data = ByteUtil.toByteArray(objs);

		for (ServerInfo target : this.targets)
			target.sendData(BungeeHelper.NIFTY_CHANNEL, data);
	}

}