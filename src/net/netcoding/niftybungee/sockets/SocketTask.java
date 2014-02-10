package net.netcoding.niftybungee.sockets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.netcoding.niftybungee.NiftyBungee;

public class SocketTask extends Thread {

	private final Socket socket;

	public SocketTask(Socket socket) throws SocketException {
		this.socket = socket;
		this.socket.setSoTimeout(2000);
	}

	@Override
	public void run() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
			String sCommand = reader.readLine();
			boolean successful = false;

			switch (sCommand) {
			case "ServerIP":
				String servername1 = reader.readLine();

				for (ServerInfo si : NiftyBungee.getInstance().getProxy().getServers().values()) {
					if (si.getName().equalsIgnoreCase(servername1)) {
						writer.write(si.getAddress().getAddress().getHostAddress());
						writer.newLine();
						writer.write(String.valueOf(si.getAddress().getPort()));
						successful = true;
						break;
					}
				}

				break;
			case "GetServerInfo":
				String servername2 = reader.readLine();

				for (ServerInfo si : NiftyBungee.getInstance().getProxy().getServers().values()) {
					if (si.getName().equalsIgnoreCase(servername2)) {
						writer.write(si.getAddress().getAddress().getHostAddress());
						writer.newLine();
						writer.write(String.valueOf(si.getAddress().getPort()));
						writer.newLine();
						writer.write(si.getMotd());
						successful = true;
						break;
					}
				}

				break;
			case "IPOther":
				String playername2 = reader.readLine();

				for (ProxiedPlayer pp : NiftyBungee.getInstance().getProxy().getPlayers()) {
					if (pp.getName().toLowerCase().startsWith(playername2.toLowerCase())) {
						writer.write(pp.getAddress().getAddress().getHostAddress());
						successful = true;
						break;
					}
				}

				break;
			}

			if (!successful) writer.write("");
			writer.close();
			reader.close();
		} catch (Exception ignored) { } finally {
			try {
				this.socket.close();
			} catch (Exception ex) { }
		}
	}

}