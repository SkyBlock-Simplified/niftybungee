package net.netcoding.niftybungee.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * A collection of byte array methods for easy object converting.
 */
public class ByteUtil {

	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

	/**
	 * Gets a byte array of converted objects.
	 * 
	 * @param data to convert
	 * @return converted objects in byte array
	 */
	public static byte[] toByteArray(Object... data) {
		return toByteArray(Arrays.asList(data));
	}

	/**
	 * Gets a byte array of converted objects.
	 * 
	 * @param data to convert
	 * @return converted objects in byte array
	 */
	public static byte[] toByteArray(Collection<Object> data) {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();

		for (Object obj : data) {
			if (obj instanceof byte[])
				output.write((byte[])obj);
			else if (obj instanceof Byte)
				output.writeByte((int)obj);
			else if (obj instanceof Boolean)
				output.writeBoolean((boolean)obj);
			else if (obj instanceof Character)
				output.writeChar((char)obj);
			else if (obj instanceof Short)
				output.writeShort((short)obj);
			else if (obj instanceof Integer)
				output.writeInt((int)obj);
			else if (obj instanceof Long)
				output.writeLong((long)obj);
			else if (obj instanceof Double)
				output.writeDouble((double)obj);
			else if (obj instanceof Float)
				output.writeFloat((float)obj);
			else if (obj instanceof String)
				output.writeUTF((String)obj);
			else if (obj instanceof UUID)
				output.writeUTF(((UUID)obj).toString());
		}

		return output.toByteArray();
	}

	/**
	 * Gets the hexadecimal string of a byte array.
	 * 
	 * @param bytes to convert
	 * @return converted byte array as hexadecimal string
	 */
	public static String toHexString(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];

		for (int i = 0; i < bytes.length; i++) {
			int v = bytes[i] & 0xFF;
			hexChars[i * 2] = hexArray[v >>> 4];
			hexChars[i * 2 + 1] = hexArray[v & 0x0F];
		}

		return new String(hexChars);
	}

	/**
	 * Gets human readable ascii of a hexadecimal string.
	 * 
	 * @param hex to convert
	 * @return converted hexadecimal string into ascii
	 */
	public static String toAsciiString(String hex) {
		StringBuilder output = new StringBuilder();

		for (int i = 0; i < hex.length(); i += 2) {
			String str = hex.substring(i, i + 2);
			output.append((char)Integer.parseInt(str, 16));
		}

		return output.toString();
	}

}