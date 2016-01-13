package net.netcoding.niftybungee;

import net.netcoding.niftybungee.minecraft.BungeePlugin;
import net.netcoding.niftybungee.minecraft.messages.BukkitHelper;
import net.netcoding.niftybungee.mojang.BungeeMojangRepository;

public class NiftyBungee extends BungeePlugin {

	private static transient NiftyBungee plugin;
	private static transient BungeeMojangRepository repository;
	private static transient BukkitHelper bukkitHelper;

	@Override
	public void onEnable() {
		//test
		this.getLog().console("Registering Helpers");
		plugin = this;
		repository = new BungeeMojangRepository();
		bukkitHelper = new BukkitHelper(this);
	}

	@Override
	public void onDisable() {
		bukkitHelper.unregister();
		bukkitHelper.stopThread();
	}

	public final static BukkitHelper getBukkitHelper() {
		return bukkitHelper;
	}

	public final static BungeeMojangRepository getMojangRepository() {
		return repository;
	}

	public final static NiftyBungee getPlugin() {
		return plugin;
	}

}