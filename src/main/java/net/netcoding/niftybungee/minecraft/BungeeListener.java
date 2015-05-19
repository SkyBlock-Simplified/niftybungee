package net.netcoding.niftybungee.minecraft;

import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeListener implements Listener {

	public BungeeListener(Plugin plugin) {
		plugin.getProxy().getPluginManager().registerListener(plugin, this);
	}

}