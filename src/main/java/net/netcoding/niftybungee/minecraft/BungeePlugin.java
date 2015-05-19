package net.netcoding.niftybungee.minecraft;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.netcoding.niftybungee.mojang.BungeeMojangProfile;
import net.netcoding.niftycore.mojang.MojangProfile;
import net.netcoding.niftycore.util.StringUtil;

public abstract class BungeePlugin extends Plugin {

	private final transient BungeeLogger log;

	public BungeePlugin() {
		this.log = new BungeeLogger(this);
	}

	public BungeeLogger getLog() {
		return this.log;
	}

	public boolean hasPermissions(MojangProfile profile, String... permissions) {
		return this.hasPermissions(profile, false, permissions);
	}

	public boolean hasPermissions(MojangProfile profile, boolean defaultError, String... permissions) {
		BungeeMojangProfile bungeeProfile = (BungeeMojangProfile)profile;
		return bungeeProfile.isOnlineAnywhere() ? this.hasPermissions(bungeeProfile.getPlayer(), defaultError, permissions) : false;
	}

	public boolean hasPermissions(CommandSender sender, String... permissions) {
		return this.hasPermissions(sender, false, permissions);
	}

	public boolean hasPermissions(CommandSender sender, boolean defaultError, String... permissions) {
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