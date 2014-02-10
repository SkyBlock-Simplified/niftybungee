package net.netcoding.niftybungee.sockets;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class SocketListener extends Thread {

	private final ServerSocket server;

	public SocketListener() throws IOException {
		this.server = new ServerSocket(7777, 100, InetAddress.getLoopbackAddress());
	}

	@Override
	public void run() {
		while (true) {
			try {
				new SocketTask(server.accept()).start();
			} catch (Exception ignored) { }
		}
	}

}