package net.netcoding.niftybungee;

import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.netcoding.niftybungee.minecraft.BungeeHelper;

public class NiftyBungee extends Plugin {

	private static transient NiftyBungee plugin;
	private static transient BungeeHelper listener;

	static {
		System.setOut(new SystemInterceptor());
	}

	@Override
	public void onEnable() {
		plugin = this;
		ProxyServer.getInstance().getLogger().addHandler(new LogHandler());
		BungeeCord.getInstance().getLogger().addHandler(new LogHandler());
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

	private static class SystemInterceptor extends PrintStream {

		public SystemInterceptor() {
			super(System.out, true);
		}

		@Override
		public void print(String s) {
			if (!s.contains("read timed out") && !s.contains("Command not found"))
				super.print(s);
		}

		@Override
		public void println(String x) {
			if (!x.contains("read timed out") && !x.contains("Command not found"))
				super.println(x);
		}
	}

	private class LogHandler extends Handler {

		@Override
		public void close() throws SecurityException { }

		@Override
		public void flush() { }

		@Override
		public void publish(LogRecord record) {
			if (BungeeHelper.BUNGEE_CHANNEL.equals(record.getLoggerName())) {
				if (record.getMessage().contains("read timed out"))
					record.setMessage(null);
				else if (record.getMessage().contains("Command not found"))
					record.setMessage(null);
			}
		}

	}

}