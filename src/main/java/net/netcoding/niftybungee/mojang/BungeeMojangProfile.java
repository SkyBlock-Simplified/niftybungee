package net.netcoding.niftybungee.mojang;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.netcoding.niftybungee.NiftyBungee;
import net.netcoding.niftycore.minecraft.MinecraftServer;
import net.netcoding.niftycore.mojang.MojangProfile;

public class BungeeMojangProfile extends MojangProfile {

	private final ProxiedPlayer player;

	BungeeMojangProfile(ProxiedPlayer player) {
		this.player = player;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public final ProxiedPlayer getPlayer() {
		return this.player;
	}

	@Override
	public MinecraftServer getServer() {
		return NiftyBungee.getBukkitHelper().getServer(this.getPlayer().getServer().getInfo());
	}

	@Override
	public boolean isOnlineAnywhere() {
		return ProxyServer.getInstance().getPlayer(this.getUniqueId()) != null;
	}

}