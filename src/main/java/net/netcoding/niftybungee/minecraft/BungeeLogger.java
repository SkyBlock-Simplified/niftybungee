package net.netcoding.niftybungee.minecraft;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.netcoding.niftycore.minecraft.MinecraftLogger;
import net.netcoding.niftycore.util.StringUtil;

public class BungeeLogger extends MinecraftLogger {

	public BungeeLogger(Plugin plugin) {
		super(plugin.getLogger());
	}

	public void error(CommandSender sender, Object... args) {
		this.error(sender, "", args);
	}

	public void error(CommandSender sender, String message, Object... args) {
		this.error(sender, message, null, args);
	}

	public void error(CommandSender sender, String message, Throwable exception, Object... args) {
		this.message(sender, StringUtil.format("{0} {1}", getPrefix("Error"), message), exception, args);
	}

	public void message(CommandSender sender, String message, Object... args) {
		this.message(sender, message, null, args);
	}

	private void message(CommandSender sender, String message, Throwable exception, Object... args) {
		boolean isConsole = ProxyServer.getInstance().getConsole().equals(sender); // TODO

		if ((isConsole || exception != null) || (isConsole && exception == null))
			console(message, exception, args);

		if (!isConsole)
			sender.sendMessage(parse(message, args));
	}

}