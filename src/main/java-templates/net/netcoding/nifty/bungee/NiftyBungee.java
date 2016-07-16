package net.netcoding.nifty.bungee;

import net.netcoding.nifty.bungee.api.plugin.BungeePlugin;
import net.netcoding.nifty.bungee.api.plugin.messaging.BukkitHelper;
import net.netcoding.nifty.bungee.mojang.BungeeMojangRepository;
import net.netcoding.nifty.core.api.plugin.annotations.Plugin;

@Plugin(name = "Nifty${name}", version = "${version}")
public class NiftyBungee extends BungeePlugin {

	private static transient NiftyBungee plugin;
	private static transient BungeeMojangRepository repository;
	private static transient BukkitHelper bukkitHelper;

	@Override
	public void onEnable() {
		this.getLog().console("Registering Helpers");
		plugin = this;
		bukkitHelper = new BukkitHelper(this);
		repository = new BungeeMojangRepository();
	}

	@Override
	public void onDisable() {
		bukkitHelper.unregister();
	}

	public static BukkitHelper getBukkitHelper() {
		return bukkitHelper;
	}

	public static BungeeMojangRepository getMojangRepository() {
		return repository;
	}

	public static NiftyBungee getPlugin() {
		return plugin;
	}

}