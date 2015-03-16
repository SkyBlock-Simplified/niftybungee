package net.netcoding.niftybungee;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.netcoding.niftybungee.minecraft.BungeeHelper;

public class NiftyBungee extends Plugin {

	private static transient NiftyBungee plugin;
	private static transient BungeeHelper listener;

	@Override
	public void onEnable() {
		plugin = this;
		ProxyServer.getInstance().getLogger().addHandler(new LogHandler());
		this.getProxy().registerChannel(BungeeHelper.NIFTY_CHANNEL);
		this.getProxy().getPluginManager().registerListener(this, listener = new BungeeHelper());
	}

	@Override
	public void onDisable() {
		this.getProxy().unregisterChannel(BungeeHelper.NIFTY_CHANNEL);
		this.getProxy().getPluginManager().unregisterListener(listener);
		listener.stopThread();
		listener = null;
	}

	public static NiftyBungee getPlugin() {
		return plugin;
	}

	private class LogHandler extends Handler {

		@Override
		public void close() throws SecurityException { }

		@Override
		public void flush() { }

		@Override
		public void publish(LogRecord record) {
			if (BungeeHelper.BUNGEE_CHANNEL.equals(record.getLoggerName())) {
				if (Level.WARNING.equals(record.getLevel())) {
					if (record.getMessage().endsWith("read timed out"))
						record.setMessage("");
				}
			}
		}

	}

}