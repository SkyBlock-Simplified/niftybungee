package net.netcoding.niftybungee;

import net.md_5.bungee.api.plugin.Plugin;
import net.netcoding.niftybungee.pluginmessages.BungeeHelper;

public class NiftyBungee extends Plugin {

	private static transient NiftyBungee instance;

	@Override
	public void onEnable() {
		instance = this;

		this.getProxy().registerChannel("NiftyBungee");
		this.getProxy().getPluginManager().registerListener(this, new BungeeHelper());
	}

	public static NiftyBungee getInstance() {
		return instance;
	}

}