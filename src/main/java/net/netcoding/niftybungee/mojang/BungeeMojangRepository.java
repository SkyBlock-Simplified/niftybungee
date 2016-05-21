package net.netcoding.niftybungee.mojang;

import com.google.gson.JsonObject;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.event.EventHandler;
import net.netcoding.niftybungee.NiftyBungee;
import net.netcoding.niftybungee.minecraft.BungeeListener;
import net.netcoding.niftycore.mojang.MojangProfile;
import net.netcoding.niftycore.mojang.MojangRepository;
import net.netcoding.niftycore.mojang.exceptions.ProfileNotFoundException;
import net.netcoding.niftycore.util.ListUtil;
import net.netcoding.niftycore.util.concurrent.ConcurrentList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BungeeMojangRepository extends MojangRepository<BungeeMojangProfile, ProxiedPlayer> {

	static {
		new RepositoryListener();
	}

	private BungeeMojangProfile createProfile(ProxiedPlayer player) {
		JsonObject json = new JsonObject();
		json.addProperty("id", player.getUniqueId().toString());
		json.addProperty("name", player.getName());
		json.addProperty("ip", player.getAddress().getAddress().getHostAddress());
		json.addProperty("port", player.getAddress().getPort());
		return GSON.fromJson(json.toString(), BungeeMojangProfile.class);
	}

	@Override
	protected final boolean isOnline() {
		return ProxyServer.getInstance().getConfig().isOnlineMode();
	}

	@Override
	protected final void processOfflineUsernames(List<BungeeMojangProfile> profiles, ConcurrentList<String> userList) { }

	@Override
	protected final void processOnlineUsernames(List<BungeeMojangProfile> profiles, ConcurrentList<String> userList) {
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
	protected final BungeeMojangProfile processOfflineUniqueId(UUID uniqueId) {
		return null;
	}

	@Override
	protected final BungeeMojangProfile processOnlineUniqueId(UUID uniqueId) {
		for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
			for (ProxiedPlayer player : server.getPlayers()) {
				if (player.getUniqueId().equals(uniqueId))
					return this.createProfile(player);
			}
		}

		return null;
	}

	/**
	 * Locates the profile for this server associated with the given offline player.
	 *
	 * @param player Proxied player to search with.
	 * @return Profile associated with the given player.
	 * @throws ProfileNotFoundException If unable to locate the players profile.
	 */
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

	/**
	 * Locates the profiles for this server associated with the given offline players.
	 *
	 * @param players Offline players to search with.
	 * @return Profiles associated with the list of players.
	 * @throws ProfileNotFoundException If unable to locate any players profile.
	 */
	@Override
	public BungeeMojangProfile[] searchByPlayer(ProxiedPlayer[] players) throws ProfileNotFoundException {
		return this.searchByPlayer(Arrays.asList(players));
	}

	/**
	 * Locates the profiles for this server associated with the given offline players.
	 *
	 * @param  players Proxied players to search with.
	 * @return Profiles associated with the list of players.
	 * @throws ProfileNotFoundException If unable to locate any players profile.
	 */
	@Override
	public BungeeMojangProfile[] searchByPlayer(Collection<? extends ProxiedPlayer> players) throws ProfileNotFoundException {
		final ProfileNotFoundException.LookupType type = ProfileNotFoundException.LookupType.OFFLINE_PLAYERS;
		List<BungeeMojangProfile> profiles = new ArrayList<>();

		try {
			// Create Matching Profiles
			for (ProxiedPlayer player : players)
				profiles.add(this.createProfile(player));

			return ListUtil.toArray(profiles, BungeeMojangProfile.class);
		} catch (ProfileNotFoundException pnfex) {
			throw new ProfileNotFoundException(pnfex.getReason(), type, pnfex.getCause(), ListUtil.toArray(profiles, BungeeMojangProfile.class));
		} catch (Exception ex) {
			throw new ProfileNotFoundException(ProfileNotFoundException.Reason.EXCEPTION, type, ex, ListUtil.toArray(profiles, BungeeMojangProfile.class));
		}
	}

	protected static class RepositoryListener extends BungeeListener {

		private RepositoryListener() {
			super(NiftyBungee.getPlugin());
		}

		@EventHandler
		public void onServerConnected(ServerConnectedEvent event) {
			ProxiedPlayer player = event.getPlayer();
			BungeeMojangProfile profile = NiftyBungee.getMojangRepository().searchByPlayer(player);

			for (MojangProfile cache : CACHE) {
				if (cache.equals(profile))
					CACHE.remove(cache);
			}
		}

	}

}