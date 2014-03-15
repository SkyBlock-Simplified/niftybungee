package net.netcoding.niftybungee.minecraft;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.netcoding.niftybungee.NiftyBungee;

public class BungeeHelper implements Listener {

	@EventHandler
	public void onPluginMessage(PluginMessageEvent event) {
		if (event.getTag().equals("NiftyBungee") && event.getSender() instanceof Server) {
			ByteArrayDataInput input = ByteStreams.newDataInput(event.getData());
			String subChannel = input.readUTF();
			String data = input.readUTF();
			Object[] message = null;

			if (subChannel.equals("PlayerIP")) {
				ProxiedPlayer player = NiftyBungee.getInstance().getProxy().getPlayer(data);

				if (player != null)
					message = new Object[] { player.getAddress().getAddress().getHostAddress(), player.getAddress().getPort() };
			} else {
				ServerInfo server = NiftyBungee.getInstance().getProxy().getServerInfo(data);

				if (server != null) {
					if (subChannel.equals("ServerIP"))
						message = new Object[] { server.getAddress().getAddress().getHostAddress(), server.getAddress().getPort() };
					else if (subChannel.equals("ServerMotd"))
						message = new Object[] { server.getMotd() };
				}
			}


			if (message != null)
				sendPluginMessage(event, message);
		}
	}

	public void sendPluginMessage(PluginMessageEvent event, Object... objs) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();

		for (Object obj : objs) {
			if (obj instanceof String)
				out.writeUTF((String)obj);
			else if (obj instanceof Short)
				out.writeShort((short)obj);
			else if (obj instanceof Integer)
				out.writeInt((int)obj);
			else if (obj instanceof Boolean)
				out.writeBoolean((boolean)obj);
		}

		((Server)event.getSender()).sendData("NiftyBungee", out.toByteArray());
	}

}