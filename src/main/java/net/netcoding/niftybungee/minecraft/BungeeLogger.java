package net.netcoding.niftybungee.minecraft;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginDescription;
import net.md_5.bungee.chat.ComponentSerializer;
import net.netcoding.niftycore.minecraft.MinecraftLogger;
import net.netcoding.niftycore.util.RegexUtil;
import net.netcoding.niftycore.util.StringUtil;
import net.netcoding.niftycore.util.json.JsonMessage;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class BungeeLogger extends MinecraftLogger {

	public BungeeLogger(Plugin plugin) {
		super(new BungeePluginLogger(plugin));
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

	private static class BungeePluginLogger extends Logger {

		private final static Yaml yaml;
		private final String pluginName;

		static {
			Constructor yamlConstructor = new Constructor();
	        PropertyUtils propertyUtils = yamlConstructor.getPropertyUtils();
	        propertyUtils.setSkipMissingProperties(true);
	        yamlConstructor.setPropertyUtils(propertyUtils);
	        yaml = new Yaml(yamlConstructor);
		}

		protected BungeePluginLogger(Plugin plugin) {
			super(plugin.getClass().getCanonicalName(), null);
			this.pluginName = StringUtil.format("[{0}] ", this.getPluginName(plugin));
			this.setParent(ProxyServer.getInstance().getLogger());
		}

		private String getPluginName(Plugin plugin) {
			try (JarFile jar = new JarFile(plugin.getFile())) {
				JarEntry pdf = jar.getJarEntry("bungee.yml");

				if (pdf == null)
					pdf = jar.getJarEntry("plugin.yml");

				if (pdf == null)
					return "BungeeCord";

				try (InputStream in = jar.getInputStream(pdf)) {
					PluginDescription desc = yaml.loadAs(in, PluginDescription.class);
					return desc.getName();
				}
			} catch (Exception ex) {
				return "BungeeCord";
			}
		}

		@Override
		public void log(LogRecord logRecord) {
			logRecord.setMessage(this.pluginName + logRecord.getMessage());
			super.log(logRecord);
		}

	}

}