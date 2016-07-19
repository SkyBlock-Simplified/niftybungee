package net.netcoding.nifty.bungee.mojang;

import com.google.gson.JsonObject;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.event.EventHandler;
import net.netcoding.nifty.bungee.NiftyBungee;
import net.netcoding.nifty.bungee.api.BungeeListener;
import net.netcoding.nifty.core.mojang.MojangRepository;
import net.netcoding.nifty.core.mojang.exceptions.ProfileNotFoundException;
import net.netcoding.nifty.core.util.ListUtil;
import net.netcoding.nifty.core.util.concurrent.ConcurrentList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class BungeeMojangRepository extends MojangRepository<BungeeMojangProfile, ProxiedPlayer> {

	public BungeeMojangRepository() {
		new RepositoryListener();
	}

	private BungeeMojangProfile createProfile(ProxiedPlayer player) {
		JsonObject json = new JsonObject();
		json.addProperty("id", player.getUniqueId().toString());
		json.addProperty("name", player.getName());
		String ip;
		int port;

		try {
			ip = player.getAddress().getAddress().getHostAddress();
			port = player.getAddress().getPort();
		} catch (NullPointerException npex) {
			ip = "";
			port = 0;
		}

		json.addProperty("ip", ip);
		json.addProperty("port", port);
		return GSON.fromJson(json.toString(), BungeeMojangProfile.class);
	}

	@Override
	protected boolean isOnline() {
		return ProxyServer.getInstance().getConfig().isOnlineMode();
	}

	@Override
	protected void processOfflineUsernames(List<BungeeMojangProfile> profiles, ConcurrentList<String> userList) { }

	@Override
	protected void processOnlineUsernames(List<BungeeMojangProfile> profiles, ConcurrentList<String> userList) {
		for (String name : userList) {
			String criteriaName = name.toLowerCase();

			for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
				BungeeMojangProfile found = null;

				for (ProxiedPlayer player : server.getPlayers()) {
					if (player.getName().equalsIgnoreCase(criteriaName)) {
						found = this.createProfile(player);
						break;
					}
				}

				if (found == null) {
					for (ProxiedPlayer player : server.getPlayers()) {
						if (player.getName().toLowerCase().startsWith(criteriaName)) {
							found = this.createProfile(player);
							break;
						}
					}
				}

				if (found != null) {
					profiles.add(found);
					userList.remove(name);
					break;
				}
			}
		}
	}

	@Override
	protected BungeeMojangProfile processOfflineUniqueId(UUID uniqueId) {
		return null;
	}

	@Override
	protected BungeeMojangProfile processOnlineUniqueId(UUID uniqueId) {
		for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
			for (ProxiedPlayer player : server.getPlayers()) {
				if (player.getUniqueId().equals(uniqueId))
					return this.createProfile(player);
			}
		}

		return null;
	}

	@Override
	public BungeeMojangProfile searchByPlayer(ProxiedPlayer player) throws ProfileNotFoundException {
		try {
			return this.searchByPlayer(Collections.singletonList(player))[0];
		} catch (ProfileNotFoundException pnfex) {
			if (ProfileNotFoundException.Reason.NO_PREMIUM_PLAYER == pnfex.getReason()) {
				JsonObject json = new JsonObject();
				json.addProperty("id", player.getUniqueId().toString());
				json.addProperty("name", player.getName());
				throw new ProfileNotFoundException(ProfileNotFoundException.Reason.NO_PREMIUM_PLAYER, ProfileNotFoundException.LookupType.OFFLINE_PLAYER, GSON.fromJson(json, BungeeMojangProfile.class));
			}

			throw pnfex;
		}
	}

	@Override
	public BungeeMojangProfile[] searchByPlayer(ProxiedPlayer[] players) throws ProfileNotFoundException {
		return this.searchByPlayer(Arrays.asList(players));
	}

	@Override
	public BungeeMojangProfile[] searchByPlayer(Collection<? extends ProxiedPlayer> players) throws ProfileNotFoundException {
		final ProfileNotFoundException.LookupType type = ProfileNotFoundException.LookupType.OFFLINE_PLAYERS;
		List<BungeeMojangProfile> profiles = new ArrayList<>();

		try {
			// Create Matching Profiles
			profiles.addAll(players.stream().map((Function<ProxiedPlayer, BungeeMojangProfile>) this::createProfile).collect(Collectors.toList()));

			return ListUtil.toArray(profiles, BungeeMojangProfile.class);
		} catch (ProfileNotFoundException pnfex) {
			throw new ProfileNotFoundException(pnfex.getReason(), type, pnfex.getCause(), ListUtil.toArray(profiles, BungeeMojangProfile.class));
		} catch (Exception ex) {
			throw new ProfileNotFoundException(ProfileNotFoundException.Reason.EXCEPTION, type, ex, ListUtil.toArray(profiles, BungeeMojangProfile.class));
		}
	}

	protected class RepositoryListener extends BungeeListener {

		private RepositoryListener() {
			super(NiftyBungee.getPlugin());
		}

		@EventHandler
		public void onServerConnected(ServerConnectedEvent event) {
			ProxiedPlayer player = event.getPlayer();
			BungeeMojangProfile profile = NiftyBungee.getMojangRepository().searchByPlayer(player);
			cache.stream().filter(cache -> cache.equals(profile)).forEach(cache::remove);
		}

	}

}