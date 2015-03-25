package net.netcoding.niftybungee.minecraft;

import java.net.InetSocketAddress;
import java.util.Collection;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

abstract class MinecraftServer {

	protected final ServerInfo serverInfo;
	private String gameVersion = "";
	private int maxPlayers = 0;
	private boolean online = false;
	private int protocolVersion = -1;
	private String motd = "";
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
		if (!this.getAddress().equals(server.getAddress())) return false;
		return true;
	}

	public InetSocketAddress getAddress() {
		return this.serverInfo.getAddress();
	}

	public String getGameVersion() {
		return this.gameVersion;
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

	public int getProtocolVersion() {
		return this.protocolVersion;
	}

	@Override
	public int hashCode() {
		int hashCode = 31 * (this.getAddress() == null ? 0 : this.getAddress().hashCode());
		hashCode *= this.getName().hashCode();
		return hashCode;
	}

	public boolean isOnline() {
		return this.online;
	}

	public abstract void ping(final Runnable runnable);

	void reset() {
		this.motd = this.serverInfo.getMotd();
		this.protocolVersion = -1;
		this.gameVersion = "";
		this.maxPlayers = 0;
	}

	void setGameVersion(String gameVersion) {
		this.gameVersion = gameVersion;
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

	void setProtocolVersion(int protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

}