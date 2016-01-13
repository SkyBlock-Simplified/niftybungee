package net.netcoding.niftybungee.minecraft.ping;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.netcoding.niftybungee.NiftyBungee;
import net.netcoding.niftybungee.minecraft.messages.BukkitHelper;
import net.netcoding.niftybungee.mojang.BungeeMojangProfile;
import net.netcoding.niftycore.minecraft.ping.MinecraftPingListener;
import net.netcoding.niftycore.util.StringUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.RejectedExecutionException;

public class BungeeInfoServer extends BukkitInfoServer {

	private static final String NIFTY_PING = "NiftyPing";
	private String socketIp = null;
	private int socketPort = -1;

	public BungeeInfoServer(ServerInfo serverInfo, MinecraftPingListener<BungeeMojangProfile> listener) {
		super(serverInfo, listener);
	}

	public boolean isBukkitSocketListening() {
		return StringUtil.notEmpty(this.socketIp) && this.socketPort > 0;
	}

	private void onNiftyPing(int port) {
		try (Socket socket = new Socket(this.getAddress().getAddress().getHostAddress(), port)) {
			socket.setSoTimeout(2000);

			try (DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream())) {
				dataOutputStream.writeUTF(NIFTY_PING);
				dataOutputStream.writeUTF(BukkitHelper.getSocketWrapper().getSocketAddress());
				dataOutputStream.writeInt(BukkitHelper.getSocketWrapper().getLocalPort());

				try (DataInputStream dataInputStream = new DataInputStream(socket.getInputStream())) {
					this.socketIp = dataInputStream.readUTF();
					this.socketPort = dataInputStream.readInt();
				}
			}
		} catch (Exception ex) {
			NiftyBungee.getPlugin().getLog().console(ex);
			this.socketIp = null;
			this.socketPort = -1;
		}
	}

	@Override
	public void ping() {
		if (this.getAddress() == null) return;

		try {
			this.serverInfo.ping(new Callback<ServerPing>() {
				@Override
				public void done(ServerPing result, Throwable error) {
					reset();

					if (error == null) {
						// Process Socket Registration
						if (result.getVersion().getName().startsWith(NIFTY_PING) && BukkitHelper.getSocketWrapper().isSocketListening()) {
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
		} catch (RejectedExecutionException ignore) { }
	}

	@Override
	public void sendData(String channel, byte[] data) {
		if (BukkitHelper.getSocketWrapper().isSocketListening() && this.isBukkitSocketListening()) {
			try (Socket socket = new Socket(this.socketIp, this.socketPort)) {
				try (DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream())) {
					dataOutputStream.writeUTF(channel);
					dataOutputStream.writeInt(data.length);
					dataOutputStream.write(data);
				}
			} catch (IOException ioex) {
				NiftyBungee.getPlugin().getLog().console(ioex);
				// TODO: Possibly Disable Socket
				super.sendData(channel, data, false);
			}
		} else
			super.sendData(channel, data, false);
	}

}