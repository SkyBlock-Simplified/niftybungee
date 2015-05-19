package net.netcoding.niftybungee;

import net.netcoding.niftybungee.minecraft.BungeePingHelper;
import net.netcoding.niftybungee.minecraft.BungeePlugin;
import net.netcoding.niftybungee.minecraft.messages.BukkitHelper;
import net.netcoding.niftybungee.mojang.BungeeMojangRepository;

public class NiftyBungee extends BungeePlugin {

	private static transient NiftyBungee plugin;
	private static transient BungeeMojangRepository repository;
	private static transient BukkitHelper bukkitHelper;
	private transient BungeePingHelper listener;

	@Override
	public void onEnable() {
		this.getLog().console("Registering Helpers");
		plugin = this;
		repository = new BungeeMojangRepository();
		bukkitHelper = new BukkitHelper();

		this.getProxy().registerChannel(BungeePingHelper.NIFTY_CHANNEL);
		this.getProxy().getPluginManager().registerListener(this, this.listener = new BungeePingHelper());
	}

	@Override
	public void onDisable() {
		this.getProxy().unregisterChannel(BungeePingHelper.NIFTY_CHANNEL);
		this.getProxy().getPluginManager().unregisterListener(this.listener);
		this.listener.stopThread();
		this.listener = null;
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