package net.netcoding.nifty.bungee.mojang;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.chat.ComponentSerializer;
import net.netcoding.nifty.bungee.api.ping.BungeeInfoPingServer;
import net.netcoding.nifty.bungee.NiftyBungee;
import net.netcoding.nifty.core.mojang.MojangProfile;
import net.netcoding.nifty.core.util.json.JsonMessage;

public class BungeeMojangProfile extends MojangProfile {

	protected BungeeMojangProfile() { }

	public final ProxiedPlayer getPlayer() {
		return ProxyServer.getInstance().getPlayer(this.getUniqueId());
	}

	@Override
	public BungeeInfoPingServer getServer() {
		return NiftyBungee.getBukkitHelper().getServer(this.getPlayer().getServer().getInfo());
	}

	@Override
	public boolean isOnline() {
		return ProxyServer.getInstance().getPlayer(this.getUniqueId()) != null;
	}

	@Override
	public void sendMessage(JsonMessage message) {
		this.getPlayer().sendMessage(ComponentSerializer.parse(message.toJSONString()));
	}

	@Override
	public void sendMessage(String message) {
		this.getPlayer().sendMessage(TextComponent.fromLegacyText(message));
	}

}