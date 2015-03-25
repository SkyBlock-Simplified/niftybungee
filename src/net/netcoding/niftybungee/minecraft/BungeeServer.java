package net.netcoding.niftybungee.minecraft;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;

public class BungeeServer extends MinecraftServer {

	public BungeeServer(ServerInfo serverInfo, ServerPingListener listener) {
		super(serverInfo, listener);
	}

	public void ping(final Runnable runnable) {
		if (this.getAddress() == null) return;

		serverInfo.ping(new Callback<ServerPing>() {
			@Override
			public void done(ServerPing result, Throwable error) {
				if (error != null) // result == null
					reset();
				else {
					setMotd(result.getDescription());
					setGameVersion(result.getVersion().getName());
					setProtocolVersion(result.getVersion().getProtocol());
					setMaxPlayers(result.getPlayers().getMax());
					setOnline(true);
				}

				if (listener != null)
					listener.onServerPing(BungeeServer.this);

				if (runnable != null)
					runnable.run();
			}
		});
	}

}