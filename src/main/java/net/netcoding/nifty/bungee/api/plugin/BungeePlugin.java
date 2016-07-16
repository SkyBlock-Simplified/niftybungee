package net.netcoding.nifty.bungee.api.plugin;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.netcoding.nifty.bungee.api.BungeeLogger;
import net.netcoding.nifty.bungee.mojang.BungeeMojangProfile;
import net.netcoding.nifty.core.api.plugin.PluginDescription;
import net.netcoding.nifty.core.mojang.MojangProfile;
import net.netcoding.nifty.core.util.StringUtil;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.io.File;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract class BungeePlugin extends Plugin implements net.netcoding.nifty.core.api.plugin.Plugin<BungeeLogger> {

	private static final Yaml yaml;
	private final transient PluginDescription desc;
	private final transient BungeeLogger log;

	static {
		Constructor yamlConstructor = new Constructor();
		PropertyUtils propertyUtils = yamlConstructor.getPropertyUtils();
		propertyUtils.setSkipMissingProperties(true);
		yamlConstructor.setPropertyUtils(propertyUtils);
		yaml = new Yaml(yamlConstructor);
	}

	public BungeePlugin() {
		String name = null;
		String version = "Unknown";

		if (this.getClass().isAnnotationPresent(net.netcoding.nifty.core.api.plugin.annotations.Plugin.class)) {
			net.netcoding.nifty.core.api.plugin.annotations.Plugin pluginAnno = this.getClass().getAnnotation(net.netcoding.nifty.core.api.plugin.annotations.Plugin.class);
			name = pluginAnno.name();
			version = pluginAnno.version();
		} else {
			try (JarFile jar = new JarFile(this.getFile())) {
				JarEntry pdf = jar.getJarEntry("bungee.yml");

				if (pdf == null)
					pdf = jar.getJarEntry("plugin.yml");

				if (pdf != null) {
					try (InputStream in = jar.getInputStream(pdf)) {
						net.md_5.bungee.api.plugin.PluginDescription desc = yaml.loadAs(in, net.md_5.bungee.api.plugin.PluginDescription.class); // TODO: Common Plugin Loading
						name = desc.getName();
						version = desc.getVersion();
					}
				}
			} catch (Exception ignore) { }

			if (name == null)
				name = this.getClass().getCanonicalName();
		}

		this.desc = new PluginDescription(name, this.getFile(), new File(ProxyServer.getInstance().getPluginsFolder(), name), version);
		this.log = new BungeeLogger(this);
	}

	@Override
	public final BungeeLogger getLog() {
		return this.log;
	}

	@Override
	public final PluginDescription getDesc() {
		return this.desc;
	}

	public final boolean hasPermissions(MojangProfile profile, String... permissions) {
		return this.hasPermissions(profile, false, permissions);
	}

	public final boolean hasPermissions(MojangProfile profile, boolean defaultError, String... permissions) {
		BungeeMojangProfile bungeeProfile = (BungeeMojangProfile)profile;
		return bungeeProfile.isOnline() && this.hasPermissions(bungeeProfile.getPlayer(), defaultError, permissions);
	}

	public final boolean hasPermissions(CommandSender sender, String... permissions) {
		return this.hasPermissions(sender, false, permissions);
	}

	public final boolean hasPermissions(CommandSender sender, boolean defaultError, String... permissions) {
		if (isConsole(sender)) return true;
		String permission = StringUtil.format("{0}.{1}", this.getDesc().getName().toLowerCase(), StringUtil.implode(".", permissions));
		boolean hasPerms = sender.hasPermission(permission);
		if (!hasPerms && defaultError) this.noPerms(sender, permission);
		return hasPerms;
	}

	public static boolean isConsole(CommandSender sender) {
		return isConsole(sender.getName());
	}

	public static boolean isConsole(String senderName) {
		return ProxyServer.getInstance().getConsole().getName().equals(senderName) || "@".equals(senderName);
	}

	public static boolean isPlayer(CommandSender sender) {
		return isPlayer(sender.getName());
	}

	public static boolean isPlayer(String senderName) {
		return !isConsole(senderName);
	}

	void noPerms(CommandSender sender, String permission) {
		this.getLog().error(sender, "You do not have the permission {{0}}!", permission);
	}

}