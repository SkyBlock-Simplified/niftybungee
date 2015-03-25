package net.netcoding.niftybungee.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class DataUtil {

	private static final Charset UTF8 = Charset.forName("UTF-8");

	public static ByteArrayDataInput newDataInput(byte[] data) {
		return ByteStreams.newDataInput(data);
	}

	public static ByteArrayDataInput newDataInput(ByteArrayInputStream inputStream) {
		return ByteStreams.newDataInput(inputStream);
	}

	public static ByteArrayDataInput newDataInput(byte[] data, int start) {
		return ByteStreams.newDataInput(data, start);
	}

	public static ByteArrayDataOutput newDataOutput() {
		return ByteStreams.newDataOutput();
	}

	public static ByteArrayDataOutput newDataOutput(ByteArrayOutputStream outputStream) {
		return ByteStreams.newDataOutput(outputStream);
	}

	public static ByteArrayDataOutput newDataOutput(int size) {
		return ByteStreams.newDataOutput(size);
	}

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

	public static int readVarInt(ByteArrayDataInput in) throws IOException {
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

	public static void writeByteArray(ByteArrayDataOutput out, byte[] data) throws IOException {
    	writeVarInt(out, data.length);
        out.write(data);
	}

    public static void writeString(DataOutputStream out, String string) throws IOException {
        writeVarInt(out, string.length());
        out.write(string.getBytes(UTF8));
    }

    public static void writeString(ByteArrayDataOutput out, String string) throws IOException {
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

	public static void writeVarInt(ByteArrayDataOutput out, int paramInt) throws IOException {
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