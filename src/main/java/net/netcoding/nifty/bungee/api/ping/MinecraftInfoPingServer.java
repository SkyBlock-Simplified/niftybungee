package net.netcoding.nifty.bungee.api.ping;

import net.md_5.bungee.api.config.ServerInfo;
import net.netcoding.nifty.bungee.NiftyBungee;
import net.netcoding.nifty.bungee.api.plugin.messaging.BukkitHelper;
import net.netcoding.nifty.bungee.mojang.BungeeMojangProfile;
import net.netcoding.nifty.core.api.ping.MinecraftPingServer;
import net.netcoding.nifty.core.api.ping.MinecraftPingListener;
import net.netcoding.nifty.core.util.ByteUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MinecraftInfoPingServer extends MinecraftPingServer<BungeeMojangProfile> {

	protected final ServerInfo serverInfo;
	protected Runnable runnable;

	public MinecraftInfoPingServer(ServerInfo serverInfo, MinecraftPingListener<BungeeMojangProfile> listener) {
		super(serverInfo.getAddress(), listener);
		this.serverInfo = serverInfo;
		super.serverName = serverInfo.getName();
		super.motd = serverInfo.getMotd();
	}

	@Override
	public final Collection<BungeeMojangProfile> getPlayerList() {
		BungeeMojangProfile[] profiles = NiftyBungee.getMojangRepository().searchByPlayer(this.serverInfo.getPlayers());
		return Collections.unmodifiableCollection(Arrays.asList(profiles));
	}

	@Override
	public final void onPing() {
		List<Object> objs = new ArrayList<>();
		objs.add("ServerInfo");
		objs.add(this.getName());
		objs.add(this.isOnline());

		if (this.isOnline()) {
			objs.add(this.getMotd());
			objs.add(this.getVersion().getName());
			objs.add(this.getVersion().getProtocol());
			objs.add(this.getMaxPlayers());
			objs.add(this.getPlayerList().size());
			objs.addAll(this.getPlayerList().stream().map(profile -> BukkitHelper.parsePlayerInfo(profile, true)).collect(Collectors.toList()));
		}

		byte[] data = ByteUtil.toByteArray(objs);

		for (MinecraftInfoPingServer target : NiftyBungee.getBukkitHelper().getServers())
			target.sendData(BukkitHelper.NIFTY_CHANNEL, data);

		if (this.runnable != null)
			this.runnable.run();

		super.onPing();
	}

	public void sendData(String channel, byte[] data) {
		this.sendData(channel, data, true);
	}

	public void sendData(String channel, byte[] data, boolean queue) {
		this.serverInfo.sendData(channel, data, queue);
	}

	public void setRunnable(Runnable runnable) {
		this.runnable = runnable;
	}

}