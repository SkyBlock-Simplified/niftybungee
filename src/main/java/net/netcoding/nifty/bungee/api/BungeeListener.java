package net.netcoding.nifty.bungee.api;

import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

public abstract class BungeeListener implements Listener {

	public BungeeListener(Plugin plugin) {
		plugin.getProxy().getPluginManager().registerListener(plugin, this);
	}

}