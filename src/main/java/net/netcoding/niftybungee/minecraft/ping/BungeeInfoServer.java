package net.netcoding.niftybungee.minecraft.ping;

import java.io.DataOutputStream;
import java.net.Socket;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.netcoding.niftybungee.NiftyBungee;
import net.netcoding.niftybungee.minecraft.messages.BukkitHelper;
import net.netcoding.niftybungee.mojang.BungeeMojangProfile;
import net.netcoding.niftycore.minecraft.ping.MinecraftPingListener;
import net.netcoding.niftycore.util.StringUtil;

public class BungeeInfoServer extends BukkitInfoServer {

	private static final String PING_VERSION = "NiftyPing";
	private boolean usesSocket = false;

	public BungeeInfoServer(ServerInfo serverInfo, MinecraftPingListener<BungeeMojangProfile> listener) {
		super(serverInfo, listener);
	}

	private void onNiftyPing(int port) {
		try {
			try (Socket socket = new Socket(this.getAddress().getAddress().getHostAddress(), port)) {
				try (DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {
					System.out.println(StringUtil.format("{0}: Sending NiftyPing...", System.currentTimeMillis()));
					outputStream.writeUTF("NiftyPing");
					outputStream.writeUTF(this.getAddress().getAddress().getHostAddress());
					outputStream.writeInt(BukkitHelper.getSocket().getLocalPort());
				}
			}
		} catch (Exception ex) {
			NiftyBungee.getPlugin().getLog().console(ex);
			// TODO: Possibly Disable Socket
			// this.usesSocket = false;
		}
	}

	@Override
	public void ping() {
		if (this.getAddress() == null) return;

		this.serverInfo.ping(new Callback<ServerPing>() {
			@Override
			public void done(ServerPing result, Throwable error) {
				reset();

				if (error != null) {
					if (getName().equalsIgnoreCase("pixelmon") && getAddress().getAddress().getHostAddress().equals("192.99.45.103"))
						System.out.println(StringUtil.format("Unable to ping {0}: {1}", getName(), error.getMessage()));
				} else {
					if (result.getVersion().getName().startsWith(PING_VERSION) && BukkitHelper.isSocketListening()) {
						String niftyPing = StringUtil.split(",", result.getVersion().getName())[0];
						int port = Integer.valueOf(StringUtil.split(" ", niftyPing)[1]);
						onNiftyPing(port);
						ping();
						return;
					}

					setVersion(result.getVersion().getName(), result.getVersion().getProtocol());
					setMotd(result.getDescription());
					setMaxPlayers(result.getPlayers().getMax());
					setOnline(true);
				}

				onPing();
			}
		});
	}

	@Override
	public void sendData(String channel, byte[] data) {
		if (BukkitHelper.isSocketListening() && this.usesSocket) {
			try {
				try (Socket socket = BukkitHelper.getSocket()) {
					try (DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {
						outputStream.writeUTF(channel);
						outputStream.writeInt(data.length);
						outputStream.write(data);
					}
				}
			} catch (Exception ex) {
				NiftyBungee.getPlugin().getLog().console(ex);
				// TODO: Possibly Disable Socket
				// this.usesSocket = false;
				super.sendData(channel, data);
			}
		} else
			super.sendData(channel, data);
	}

}