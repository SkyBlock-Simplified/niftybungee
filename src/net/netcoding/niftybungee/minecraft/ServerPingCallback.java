package net.netcoding.niftybungee.minecraft;

import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.netcoding.niftybungee.util.ByteUtil;

public class ServerPingCallback implements Callback<ServerPing> {

	private final ServerInfo source;
	private final ServerInfo target;
	private final boolean updatePlayers;

	public ServerPingCallback(ServerInfo source, ServerInfo target, boolean updatePlayers) {
		this.source = source;
		this.target = target;
		this.updatePlayers = updatePlayers;
	}

	/**
	 * When a ping on a server is completed, send the resulting
	 * information to the requesting server.
	 */
	@Override
	public void done(ServerPing result, Throwable error) {
		List<Object> objs = new ArrayList<>();
		objs.add("ServerInfo");
		objs.add(this.source.getName());
		objs.add(result != null);

		if (result != null) {
			objs.add(result.getDescription());
			objs.add(result.getVersion().getName());
			objs.add(result.getVersion().getProtocol());
			objs.add(result.getPlayers().getMax());
			objs.add(this.updatePlayers);

			if (this.updatePlayers) {
				objs.add(this.source.getPlayers().size());

				for (ProxiedPlayer player : this.source.getPlayers())
					objs.add(BungeeHelper.parsePlayerInfo(player, true));
			}
		}

		this.target.sendData(BungeeHelper.NIFTY_CHANNEL, ByteUtil.toByteArray(objs));
	}

}