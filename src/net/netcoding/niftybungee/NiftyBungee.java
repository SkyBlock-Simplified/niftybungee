package net.netcoding.niftybungee;

import net.md_5.bungee.api.plugin.Plugin;
import net.netcoding.niftybungee.pluginmessages.PluginMessageListener;
import net.netcoding.niftybungee.sockets.SocketListener;

public class NiftyBungee extends Plugin {

	private static transient NiftyBungee instance;

	@Override
	public void onEnable() {
		instance = this;

		try {
			new SocketListener().start();
		} catch (Exception ignored) { }

		this.getProxy().registerChannel("NiftyBungee");
		this.getProxy().getPluginManager().registerListener(this, new PluginMessageListener());
	}

	public static NiftyBungee getInstance() {
		return instance;
	}

}