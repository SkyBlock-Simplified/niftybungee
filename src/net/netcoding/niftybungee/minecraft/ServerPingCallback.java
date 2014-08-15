package net.netcoding.niftybungee.minecraft;

import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.ServerPing.PlayerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.netcoding.niftybungee.util.ByteUtil;

public class ServerPingCallback implements Callback<ServerPing> {

	private final ServerInfo source;
	private final ServerInfo target;

	public ServerPingCallback(ServerInfo source, ServerInfo target) {
		this.source = source;
		this.target = target;
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
			boolean hasPlayers = false;

			if (result.getPlayers() != null) {
				if (result.getPlayers().getSample() != null)
					hasPlayers = true;
			}

			objs.add(hasPlayers ? result.getPlayers().getSample().length : 0);

			if (hasPlayers) {
				for (PlayerInfo player : result.getPlayers().getSample()) {
					objs.add(player.getName());
					objs.add(player.getId());
				}
			}
		}

		this.target.sendData(BungeeHelper.NIFTY_CHANNEL, ByteUtil.toByteArray(objs));
	}

}