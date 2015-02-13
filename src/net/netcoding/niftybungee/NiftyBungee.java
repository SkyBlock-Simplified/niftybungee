package net.netcoding.niftybungee;

import net.md_5.bungee.api.plugin.Plugin;
import net.netcoding.niftybungee.minecraft.BungeeHelper;

public class NiftyBungee extends Plugin {

	private static transient NiftyBungee plugin;
	private static transient BungeeHelper listener;

	@Override
	public void onEnable() {
		plugin = this;
		this.getProxy().registerChannel("NiftyBungee");
		this.getProxy().getPluginManager().registerListener(this, listener = new BungeeHelper());
	}

	@Override
	public void onDisable() {
		this.getProxy().unregisterChannel("NiftyBungee");
		BungeeHelper.stopThread();
		this.getProxy().getPluginManager().unregisterListener(listener);
		listener = null;
		
	}

	public static NiftyBungee getPlugin() {
		return plugin;
	}

}