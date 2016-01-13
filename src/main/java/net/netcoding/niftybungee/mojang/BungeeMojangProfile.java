package net.netcoding.niftybungee.mojang;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.netcoding.niftybungee.NiftyBungee;
import net.netcoding.niftybungee.minecraft.ping.BungeeInfoServer;
import net.netcoding.niftycore.mojang.MojangProfile;

public class BungeeMojangProfile extends MojangProfile {

	@Override
	public String getName() {
		return this.name;
	}

	public final ProxiedPlayer getPlayer() {
		return ProxyServer.getInstance().getPlayer(this.getUniqueId());
	}

	@Override
	public BungeeInfoServer getServer() {
		return NiftyBungee.getBukkitHelper().getServer(this.getPlayer().getServer().getInfo());
	}

	@Override
	public boolean isOnlineAnywhere() {
		return ProxyServer.getInstance().getPlayer(this.getUniqueId()) != null;
	}

}