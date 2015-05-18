package net.netcoding.niftybungee.minecraft;

import java.net.InetSocketAddress;
import java.util.Collection;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

abstract class MinecraftServer {

	protected final ServerInfo serverInfo;
	private int maxPlayers = 0;
	private boolean online = false;
	private String motd = "";
	private Version version = Version.DEFAULT;
	final transient ServerPingListener listener;

	MinecraftServer(ServerInfo serverInfo, ServerPingListener listener) {
		this.serverInfo = serverInfo;
		this.listener = listener;
		this.motd = serverInfo.getMotd();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof MinecraftServer)) return false;
		MinecraftServer server = (MinecraftServer)obj;
		if (!server.getAddress().getAddress().getHostAddress().equals(this.getAddress().getAddress().getHostAddress())) return false;
		if (server.getAddress().getPort() != this.getAddress().getPort()) return false;
		return true;
	}

	public InetSocketAddress getAddress() {
		return this.serverInfo.getAddress();
	}

	public int getMaxPlayers() {
		return this.maxPlayers;
	}

	public String getMotd() {
		return this.motd;
	}

	public String getName() {
		return this.serverInfo.getName();
	}

	public int getPlayerCount() {
		return this.getPlayerList().size();
	}

	public Collection<ProxiedPlayer> getPlayerList() {
		return this.serverInfo.getPlayers();
	}

	public Version getVersion() {
		return this.version;
	}

	@Override
	public int hashCode() {
		return 31 * (this.getAddress() == null ? 0 : this.getAddress().hashCode());
	}

	public boolean isOnline() {
		return this.online;
	}

	public abstract void ping(final Runnable runnable);

	void reset() {
		this.motd = this.serverInfo.getMotd();
		this.maxPlayers = 0;
		this.version = Version.DEFAULT;
	}

	void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
	}

	void setMotd(String motd) {
		this.motd = motd;
	}

	void setOnline(boolean online) {
		this.online = online;
	}

	void setVersion(String name, int protocol) {
		this.version = new Version(name, protocol);
	}

	public static class Version {

		static final Version DEFAULT = new Version("", 0);
		private final String name;
		private final int protocol;

		Version(String name, int protocol) {
			this.name = name;
			this.protocol = protocol;
		}

		public String getName() {
			return this.name;
		}

		public int getProtocol() {
			return this.protocol;
		}

	}

}