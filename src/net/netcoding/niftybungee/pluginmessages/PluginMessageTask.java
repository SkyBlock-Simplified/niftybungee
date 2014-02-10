package net.netcoding.niftybungee.pluginmessages;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.netcoding.niftybungee.NiftyBungee;

public class PluginMessageTask extends Thread {

	private final PluginMessageEvent event;

	public PluginMessageTask(PluginMessageEvent event) {
		this.event = event;
	}

	@Override
	public void run() {
		if (event.getTag().equals("NiftyBungee") && event.getSender() instanceof Server) {
			ByteArrayDataInput b = ByteStreams.newDataInput(event.getData());
			String sCommand = b.readUTF();

			switch (sCommand) {
			case "ServerIP":
				String servername1 = b.readUTF();

				for (ServerInfo si : NiftyBungee.getInstance().getProxy().getServers().values()) {
					if (si.getName().equalsIgnoreCase(servername1)) {
						sendPluginMessage(new String[]{si.getAddress().getAddress().getHostAddress(), String.valueOf(si.getAddress().getPort())});
						break;
					}
				}

				break;
			case "GetServerInfo":
				String servername2 = b.readUTF();

				for (ServerInfo si : NiftyBungee.getInstance().getProxy().getServers().values()) {
					if (si.getName().equalsIgnoreCase(servername2)) {
						sendPluginMessage(new String[]{si.getAddress().getAddress().getHostAddress(), String.valueOf(si.getAddress().getPort()), si.getMotd()});
						break;
					}
				}

				break;
			case "IPOther":
				String playername2 = b.readUTF();

				for (ProxiedPlayer pp : NiftyBungee.getInstance().getProxy().getPlayers()) {
					if (pp.getName().toLowerCase().startsWith(playername2.toLowerCase())) {
						sendPluginMessage(new String[]{pp.getAddress().getAddress().getHostAddress()});
						break;
					}
				}

				break;
			}
		}
	}

	public void sendPluginMessage(String[] message) {
		ByteArrayDataOutput b = ByteStreams.newDataOutput();
		for (String str : message) b.writeUTF(str);
		((Server)event.getSender()).sendData("NiftyBungee", b.toByteArray());
	}

}