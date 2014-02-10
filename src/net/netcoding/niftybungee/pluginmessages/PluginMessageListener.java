package net.netcoding.niftybungee.pluginmessages;

import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PluginMessageListener implements Listener {

	@EventHandler
	public void onPluginMessage(PluginMessageEvent e) {
		try {
			new PluginMessageTask(e).start();
		} catch (Exception ignored) { }
	}

}