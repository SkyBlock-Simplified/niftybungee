package net.netcoding.niftybungee.minecraft.ping;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import net.md_5.bungee.api.config.ServerInfo;
import net.netcoding.niftybungee.NiftyBungee;
import net.netcoding.niftycore.minecraft.ping.BukkitServer;
import net.netcoding.niftycore.minecraft.ping.MinecraftPingListener;
import net.netcoding.niftycore.mojang.MojangProfile;

public class BukkitInfoServer extends BukkitServer {

	protected final ServerInfo serverInfo;

	public BukkitInfoServer(ServerInfo serverInfo, MinecraftPingListener listener) {
		super(serverInfo.getAddress(), listener);
		this.serverInfo = serverInfo;
	}

	@Override
	public int getMaxPlayers() {
		return this.maxPlayers;
	}

	@Override
	public String getMotd() {
		return this.serverInfo.getMotd();
	}

	@Override
	public String getName() {
		return this.serverInfo.getName();
	}

	@Override
	public int getPlayerCount() {
		return this.serverInfo.getPlayers().size();
	}

	@Override
	public Collection<MojangProfile> getPlayerList() {
		MojangProfile[] profiles = NiftyBungee.getMojangRepository().searchByPlayer(this.serverInfo.getPlayers());
		return Collections.unmodifiableCollection(new HashSet<>(Arrays.asList(profiles)));
	}

}