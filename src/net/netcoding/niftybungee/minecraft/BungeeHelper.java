package net.netcoding.niftybungee.minecraft;

import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.netcoding.niftybungee.NiftyBungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class BungeeHelper implements Listener {

	@EventHandler
	public void onPluginMessage(PluginMessageEvent event) {
		if (!(event.getTag().equals("NiftyBungee") && event.getSender() instanceof Server)) return;

		ByteArrayDataInput input = ByteStreams.newDataInput(event.getData());
		String subChannel = input.readUTF();
		ServerInfo server = NiftyBungee.getInstance().getProxy().getServerInfo(input.readUTF());
		List<Object> objs = new ArrayList<>();
		objs.add(subChannel);
		objs.add(server.getName());

		if (subChannel.equals("ServerAddress")) {
			objs.add(server.getAddress().getAddress().getHostAddress());
			objs.add(server.getAddress().getPort());
			sendPluginMessage(event, objs);
		}
	}

	private void sendPluginMessage(PluginMessageEvent event, List<Object> objs) {
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