package net.netcoding.niftybungee.minecraft.ping;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.netcoding.niftybungee.mojang.BungeeMojangProfile;
import net.netcoding.niftycore.minecraft.ping.MinecraftPingListener;
import net.netcoding.niftycore.util.StringUtil;

public class BungeeInfoServer extends BukkitInfoServer {

	private static final String PING_VERSION = "NiftyPing";

	public BungeeInfoServer(ServerInfo serverInfo, MinecraftPingListener<BungeeMojangProfile> listener) {
		super(serverInfo, listener);
	}

	@Override
	public void ping() {
		if (this.getAddress() == null) return;

		this.serverInfo.ping(new Callback<ServerPing>() {
			@Override
			public void done(ServerPing result, Throwable error) {
				if (error != null) {
					reset();
					System.out.println(StringUtil.format("Unable to ping {0}: {1}", getName(), error.getMessage()));
				} else {
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
					onPing();
				}
			}
		});
	}

}