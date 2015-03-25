package net.netcoding.niftybungee.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.text.NumberFormat;
import java.text.ParsePosition;

/**
 * A collection of number utilities to assist in number checking,
 * random number generating as well as {@link #readVarInt(DataInputStream) readVarInt}
 * and {@link #writeVarInt(DataOutputStream, int) writeVarInt} used in bukkits network protocols.
 */
public class NumberUtil {

	/**
	 * Gets if {@code value} is a valid number.
	 * 
	 * @param value the value to check
	 * @return true if the value can be casted to a number, otherwise false
	 */
	public static boolean isInt(String value) {
		NumberFormat formatter = NumberFormat.getInstance();
		ParsePosition position = new ParsePosition(0);
		formatter.parse(value, position);
		return value.length() == position.getIndex();
	}

	/**
	 * Gets a truely random number.
	 * 
	 * @param minimum the lowest number allowed
	 * @return a random integer between the specified boundaries
	 */
	public static int rand(int minimum) {
		return rand(minimum, Integer.MAX_VALUE);
	}

	/**
	 * Gets a truely random number.
	 * 
	 * @param minimum the lowest number allowed
	 * @param maximum the highest number allowed
	 * @return a random integer between the specified boundaries
	 */
	public static int rand(int minimum, int maximum) {
		return minimum + (int)(Math.random() * ((maximum - minimum) + 1));
	}

}