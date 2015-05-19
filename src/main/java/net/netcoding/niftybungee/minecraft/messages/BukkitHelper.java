package net.netcoding.niftybungee.minecraft.messages;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.netcoding.niftybungee.minecraft.ping.BungeeInfoServer;
import net.netcoding.niftycore.minecraft.MinecraftServer;
import net.netcoding.niftycore.minecraft.ping.MinecraftPingListener;

public class BukkitHelper {

	public static final String BUNGEE_CHANNEL = "BungeeCord";
	public static final String NIFTY_CHANNEL = "NiftyBungee";
	private static final ConcurrentHashMap<String, BungeeInfoServer> SERVERS = new ConcurrentHashMap<>();
	private static final BukkitPingListener LISTENER = new BukkitPingListener();
	private static boolean LOADED_ONCE = false;

	public BukkitHelper() {
		if (!LOADED_ONCE) {
			LOADED_ONCE = true;

			for (ServerInfo server : ProxyServer.getInstance().getServers().values())
				SERVERS.put(server.getName(), new BungeeInfoServer(server, LISTENER));
		}
	}

	public BungeeInfoServer getServer(String serverName) {
		return SERVERS.get(serverName);
	}

	public final Set<BungeeInfoServer> getServers() {
		return Collections.unmodifiableSet(new HashSet<>(SERVERS.values()));
	}

	private static class BukkitPingListener implements MinecraftPingListener {

		@Override
		public void onPing(MinecraftServer server) {
			// TODO
		}
		
	}

}