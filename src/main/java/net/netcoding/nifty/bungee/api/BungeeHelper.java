package net.netcoding.nifty.bungee.api;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginDescription;
import net.netcoding.nifty.bungee.api.plugin.BungeePlugin;
import net.netcoding.nifty.bungee.mojang.BungeeMojangProfile;
import net.netcoding.nifty.core.mojang.MojangProfile;
import net.netcoding.nifty.core.util.StringUtil;
import net.netcoding.nifty.core.util.concurrent.Concurrent;
import net.netcoding.nifty.core.util.concurrent.ConcurrentList;

public abstract class BungeeHelper {

	private static final transient ConcurrentList<String> PLUGINS = Concurrent.newList();
	private final transient BungeePlugin plugin;

	public BungeeHelper(BungeePlugin plugin) {
		this.plugin = plugin;
		PLUGINS.add(this.getPluginDescription().getName());
	}

	public final BungeeLogger getLog() {
		return this.getPlugin().getLog();
	}

	public final BungeePlugin getPlugin() {
		return this.plugin;
	}

	public final <T extends Plugin> T getPlugin(Class<T> plugin) {
		return plugin.cast(this.getPlugin());
	}

	public static ConcurrentList<String> getPluginCache() {
		return PLUGINS;
	}

	public final PluginDescription getPluginDescription() {
		return this.getPlugin().getDescription();
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
		String permission = StringUtil.format("{0}.{1}", this.getPlugin().getDescription().getName().toLowerCase(), StringUtil.implode(".", permissions));
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