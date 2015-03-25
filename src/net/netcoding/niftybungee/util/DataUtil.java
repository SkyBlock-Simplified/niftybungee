package net.netcoding.niftybungee.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class DataUtil {

	private static final Charset UTF8 = Charset.forName("UTF-8");

	public static int readVarInt(DataInputStream in) throws IOException {
		int i = 0;
		int j = 0;

		while (true) {
			int k = in.readByte();
			i |= (k & 0x7F) << j++ * 7;
			if (j > 5) throw new RuntimeException("VarInt too big");
			if ((k & 0x80) != 128) break;
		}

		return i;
	}

    public static void writeByteArray(DataOutputStream out, byte[] data) throws IOException {
    	writeVarInt(out, data.length);
        out.write(data);
    }

    public static void writeString(DataOutputStream out, String string) throws IOException {
        writeVarInt(out, string.length());
        out.write(string.getBytes(UTF8));
    }

	public static void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
		while (true) {
			if ((paramInt & 0xFFFFFF80) == 0) {
				out.writeByte(paramInt);
				return;
			}

			out.writeByte(paramInt & 0x7F | 0x80);
			paramInt >>>= 7;
		}
	}

}