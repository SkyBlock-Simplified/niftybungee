package net.netcoding.nifty.bungee.api;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.netcoding.nifty.bungee.api.plugin.BungeePlugin;
import net.netcoding.nifty.core.api.logger.BroadcastLogger;
import net.netcoding.nifty.core.util.RegexUtil;
import net.netcoding.nifty.core.util.StringUtil;
import net.netcoding.nifty.core.util.json.JsonMessage;

public class BungeeLogger extends BroadcastLogger {

	public BungeeLogger(BungeePlugin plugin) {
		super(plugin);
	}

	@Override
	public void broadcast(String message, Throwable exception, Object... args) {
		message = StringUtil.isEmpty(message) ? "null" : message;
		message = StringUtil.format(RegexUtil.replace(message, RegexUtil.LOG_PATTERN), args);

		if (exception != null)
			this.console(exception);

		ProxyServer.getInstance().broadcast(ComponentSerializer.parse(new JsonMessage(this.parse(message, args)).toJSONString()));
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
		boolean isConsole = BungeeHelper.isConsole(sender);

		if ((isConsole || exception != null) || (isConsole && exception == null))
			console(message, exception, args);

		if (!isConsole)
			sender.sendMessage(TextComponent.fromLegacyText(parse(message, args)));
	}

}