package net.netcoding.niftybungee.minecraft;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginDescription;
import net.netcoding.niftybungee.mojang.BungeeMojangProfile;
import net.netcoding.niftycore.mojang.MojangProfile;
import net.netcoding.niftycore.util.StringUtil;
import net.netcoding.niftycore.util.concurrent.ConcurrentList;

public abstract class BungeeHelper {

	private final static transient ConcurrentList<String> PLUGINS = new ConcurrentList<>();
	private final transient Plugin plugin;
	private final transient BungeeLogger logger;

	public BungeeHelper(Plugin plugin) {
		this.plugin = plugin;
		this.logger = new BungeeLogger(plugin);
		PLUGINS.add(this.getPluginDescription().getName());
	}

	public final BungeeLogger getLog() {
		return this.logger;
	}

	public final Plugin getPlugin() {
		return this.plugin;
	}

	public final <T extends Plugin> T getPlugin(Class<T> plugin) {
		return plugin.cast(this.getPlugin());
	}

	public final static ConcurrentList<String> getPluginCache() {
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
		return bungeeProfile.isOnlineAnywhere() && this.hasPermissions(bungeeProfile.getPlayer(), defaultError, permissions);
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