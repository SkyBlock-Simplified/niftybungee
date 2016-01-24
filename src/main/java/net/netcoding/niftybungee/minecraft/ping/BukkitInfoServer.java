package net.netcoding.niftybungee.minecraft.ping;

import net.md_5.bungee.api.config.ServerInfo;
import net.netcoding.niftybungee.NiftyBungee;
import net.netcoding.niftybungee.minecraft.messages.BukkitHelper;
import net.netcoding.niftybungee.mojang.BungeeMojangProfile;
import net.netcoding.niftycore.minecraft.ping.BukkitServer;
import net.netcoding.niftycore.minecraft.ping.MinecraftPingListener;
import net.netcoding.niftycore.util.ByteUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BukkitInfoServer extends BukkitServer<BungeeMojangProfile> {

	protected final ServerInfo serverInfo;
	protected Runnable runnable;

	public BukkitInfoServer(ServerInfo serverInfo, MinecraftPingListener<BungeeMojangProfile> listener) {
		super(serverInfo.getAddress(), listener);
		this.serverInfo = serverInfo;
	}

	@Override
	public String getMotd() {
		return this.serverInfo.getMotd();
	}

	@Override
	public String getName() {
		return this.serverInfo.getName();
	}

	@Override
	public int getPlayerCount() {
		return this.serverInfo.getPlayers().size();
	}

	@Override
	public Collection<BungeeMojangProfile> getPlayerList() {
		BungeeMojangProfile[] profiles = NiftyBungee.getMojangRepository().searchByPlayer(this.serverInfo.getPlayers());
		return Collections.unmodifiableCollection(Arrays.asList(profiles));
	}

	@Override
	public void onPing() {
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

			for (BungeeMojangProfile profile : this.getPlayerList())
				objs.add(BukkitHelper.parsePlayerInfo(profile, true));
		}

		byte[] data = ByteUtil.toByteArray(objs);

		for (BukkitInfoServer target : NiftyBungee.getBukkitHelper().getServers())
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