package net.netcoding.niftybungee;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.netcoding.niftybungee.minecraft.BungeeHelper;

public class NiftyBungee extends Plugin {

	private static transient NiftyBungee plugin;
	private transient BungeeHelper listener;

	@Override
	public void onEnable() {
		plugin = this;
		ProxyServer.getInstance().getConfig().getListeners().iterator().next().getHost();
		this.getProxy().registerChannel(BungeeHelper.NIFTY_CHANNEL);
		this.getProxy().getPluginManager().registerListener(this, this.listener = new BungeeHelper());
	}

	@Override
	public void onDisable() {
		this.getProxy().unregisterChannel(BungeeHelper.NIFTY_CHANNEL);
		this.getProxy().getPluginManager().unregisterListener(this.listener);
		this.listener.stopThread();
		this.listener = null;
	}

	public static NiftyBungee getPlugin() {
		return plugin;
	}

}