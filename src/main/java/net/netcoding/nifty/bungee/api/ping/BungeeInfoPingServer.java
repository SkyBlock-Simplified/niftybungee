package net.netcoding.nifty.bungee.api.ping;

import net.md_5.bungee.api.config.ServerInfo;
import net.netcoding.nifty.bungee.NiftyBungee;
import net.netcoding.nifty.bungee.api.plugin.messaging.BukkitHelper;
import net.netcoding.nifty.bungee.mojang.BungeeMojangProfile;
import net.netcoding.nifty.core.api.ping.MinecraftPingListener;
import net.netcoding.nifty.core.util.StringUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BungeeInfoPingServer extends MinecraftInfoPingServer {

	private static final String NIFTY_PING = "NiftyPing";
	private String socketIp = null;
	private int socketPort = -1;

	public BungeeInfoPingServer(ServerInfo serverInfo, MinecraftPingListener<BungeeMojangProfile> listener) {
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

		this.serverInfo.ping((result, error) -> {
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
		});
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