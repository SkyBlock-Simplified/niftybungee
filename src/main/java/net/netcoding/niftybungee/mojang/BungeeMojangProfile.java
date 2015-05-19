package net.netcoding.niftybungee.mojang;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.netcoding.niftycore.minecraft.MinecraftServer;
import net.netcoding.niftycore.mojang.MojangProfile;

public class BungeeMojangProfile extends MojangProfile {

	private final ProxiedPlayer player;

	BungeeMojangProfile(ProxiedPlayer player) {
		this.player = player;
	}

	@Override
	public String getName() {
		return null;
	}

	public final ProxiedPlayer getPlayer() {
		return this.player;
	}

	@Override
	public MinecraftServer getServer() {
		return null;
	}

	@Override
	public boolean isOnlineAnywhere() {
		return this.getPlayer() != null;
	}

}