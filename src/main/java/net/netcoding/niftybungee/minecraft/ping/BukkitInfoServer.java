package net.netcoding.niftybungee.minecraft.ping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import net.md_5.bungee.api.config.ServerInfo;
import net.netcoding.niftybungee.NiftyBungee;
import net.netcoding.niftybungee.minecraft.messages.BukkitHelper;
import net.netcoding.niftybungee.mojang.BungeeMojangProfile;
import net.netcoding.niftycore.minecraft.ping.BukkitServer;
import net.netcoding.niftycore.minecraft.ping.MinecraftPingListener;
import net.netcoding.niftycore.mojang.MojangProfile;
import net.netcoding.niftycore.util.ByteUtil;

public class BukkitInfoServer extends BukkitServer<BungeeMojangProfile> {

	protected final ServerInfo serverInfo;
	private Runnable runnable;

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
		return Collections.unmodifiableCollection(new HashSet<>(Arrays.asList(profiles)));
	}

	@Override
	public void onPing() {
		super.onPing();

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

			for (MojangProfile profile : this.getPlayerList())
				objs.add(BukkitHelper.parsePlayerInfo(profile, true));
		}

		byte[] data = ByteUtil.toByteArray(objs);

		for (BungeeInfoServer target : NiftyBungee.getBukkitHelper().getServers())
			target.sendData(BukkitHelper.NIFTY_CHANNEL, data);

		if (this.runnable != null)
			this.runnable.run();
	}

	public void sendData(String channel, byte[] data) {
		this.serverInfo.sendData(channel, data);
	}

	public void setRunnable(Runnable runnable) {
		this.runnable = runnable;
	}

}