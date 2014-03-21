package net.netcoding.niftybungee;

import net.md_5.bungee.api.plugin.Plugin;
import net.netcoding.niftybungee.minecraft.BungeeHelper;

public class NiftyBungee extends Plugin {

	private static transient NiftyBungee plugin;

	@Override
	public void onEnable() {
		plugin = this;
		this.getProxy().registerChannel("NiftyBungee");
		this.getProxy().getPluginManager().registerListener(this, new BungeeHelper());
	}

	public static NiftyBungee getPlugin() {
		return plugin;
	}

}