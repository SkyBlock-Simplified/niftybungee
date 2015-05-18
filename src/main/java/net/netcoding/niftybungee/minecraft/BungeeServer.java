package net.netcoding.niftybungee.minecraft;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;

public class BungeeServer extends MinecraftServer {

	private static final String PING_VERSION = "NiftyPing";

	public BungeeServer(ServerInfo serverInfo, ServerPingListener listener) {
		super(serverInfo, listener);
	}

	public void ping(final Runnable runnable) {
		if (this.getAddress() == null) return;
		this.serverInfo.ping(new Callback<ServerPing>() {
			@Override
			public void done(ServerPing result, Throwable error) {
				if (error != null)
					reset();
				else {
					if (result.getVersion().getName().startsWith(PING_VERSION)) {
						//String niftyPing = StringUtil.split(",", result.getVersion().getName())[0];
						//int port = Integer.valueOf(StringUtil.split(" ", niftyPing)[1]);
						// TODO: Send socket info to this server's ip and the above protocol (ping port)
						//return;
					}

					setVersion(result.getVersion().getName(), result.getVersion().getProtocol());
					setMotd(result.getDescription());
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