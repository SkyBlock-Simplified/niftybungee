package net.netcoding.nifty.bungee.api.plugin;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.netcoding.nifty.bungee.api.BungeeLogger;
import net.netcoding.nifty.bungee.mojang.BungeeMojangProfile;
import net.netcoding.nifty.core.api.plugin.PluginDescription;
import net.netcoding.nifty.core.mojang.MojangProfile;
import net.netcoding.nifty.core.util.StringUtil;

public abstract class BungeePlugin extends Plugin implements net.netcoding.nifty.core.api.plugin.Plugin<BungeeLogger> {

	private final transient BungeeLogger log;

	public BungeePlugin() {
		this.log = new BungeeLogger(this);
	}

	@Override
	public final BungeeLogger getLog() {
		return this.log;
	}

	@Override
	public final PluginDescription getPluginDescription() {
		return new PluginDescription(this.getDescription().getName(), this.getFile(), this.getDataFolder());
	}

	public final boolean hasPermissions(MojangProfile profile, String... permissions) {
		return this.hasPermissions(profile, false, permissions);
	}

	public final boolean hasPermissions(MojangProfile profile, boolean defaultError, String... permissions) {
		BungeeMojangProfile bungeeProfile = (BungeeMojangProfile)profile;
		return bungeeProfile.isOnline() && this.hasPermissions(bungeeProfile.getPlayer(), defaultError, permissions);
	}

	public final boolean hasPermissions(CommandSender sender, String... permissions) {
		return this.hasPermissions(sender, false, permissions);
	}

	public final boolean hasPermissions(CommandSender sender, boolean defaultError, String... permissions) {
		if (isConsole(sender)) return true;
		String permission = StringUtil.format("{0}.{1}", this.getDescription().getName().toLowerCase(), StringUtil.implode(".", permissions));
		boolean hasPerms = sender.hasPermission(permission);
		if (!hasPerms && defaultError) this.noPerms(sender, permission);
		return hasPerms;
	}

	public static boolean isConsole(CommandSender sender) {
		return isConsole(sender.getName());
	}

	public static boolean isConsole(String senderName) {
		return ProxyServer.getInstance().getConsole().getName().equals(senderName) || "@".equals(senderName);
	}

	public static boolean isPlayer(CommandSender sender) {
		return isPlayer(sender.getName());
	}

	public static boolean isPlayer(String senderName) {
		return !isConsole(senderName);
	}

	void noPerms(CommandSender sender, String permission) {
		this.getLog().error(sender, "You do not have the permission {{0}}!", permission);
	}

}