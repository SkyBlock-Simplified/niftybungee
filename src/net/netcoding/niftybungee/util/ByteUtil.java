package net.netcoding.niftybungee.util;

import java.util.Arrays;
import java.util.List;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class ByteUtil {

	public static byte[] toByteArray(Object... data) {
		return toByteArray(Arrays.asList(data));
	}

	public static byte[] toByteArray(List<Object> data) {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();

		for (Object obj : data) {
			if (obj instanceof Boolean)
				output.writeBoolean((boolean)obj);
			else if (obj instanceof Byte)
				output.writeByte((int)obj);
			else if (obj instanceof Character)
				output.writeChar((char)obj);
			else if (obj instanceof Double)
				output.writeDouble((double)obj);
			else if (obj instanceof Float)
				output.writeFloat((float)obj);
			else if (obj instanceof Integer)
				output.writeInt((int)obj);
			else if (obj instanceof Long)
				output.writeLong((long)obj);
			else if (obj instanceof Short)
				output.writeShort((short)obj);
			else if (obj instanceof String)
				output.writeUTF((String)obj);
		}

		return output.toByteArray();
	}

}