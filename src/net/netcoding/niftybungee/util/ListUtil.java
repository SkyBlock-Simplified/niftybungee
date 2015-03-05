package net.netcoding.niftybungee.util;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Set;

/**
 * Array and List Checking/Converting
 */
public class ListUtil {

	/**
	 * Gets if the {@code value} is empty or null.
	 * 
	 * @param value to check
	 * @return true if empty or null, otherwise false
	 */
	public static <T> boolean isEmpty(T[] value) {
		return value == null || value.length == 0;
	}

	/**
	 * Gets if the {@code value} is empty or null.
	 * 
	 * @param value to check
	 * @return true if empty or null, otherwise false
	 */
	public static <T> boolean isEmpty(List<T> value) {
		return value == null || value.size() == 0;
	}

	/**
	 * Gets if the {@code value} is empty or null.
	 * 
	 * @param value to check
	 * @return true if empty or null, otherwise false
	 */
	public static <T> boolean isEmpty(Set<T> value) {
		return value == null || value.size() == 0;
	}

	/**
	 * Gets if the {@code value} is not empty.
	 * 
	 * @param value to check
	 * @return true if not empty or null, otherwise false
	 */
	public static <T> boolean notEmpty(T[] value) {
		return !isEmpty(value);
	}

	/**
	 * Gets if the {@code value} is not empty.
	 * 
	 * @param value to check
	 * @return true if not empty or null, otherwise false
	 */
	public static <T> boolean notEmpty(List<T> value) {
		return !isEmpty(value);
	}

	/**
	 * Gets if the {@code value} is not empty.
	 * 
	 * @param value to check
	 * @return true if not empty or null, otherwise false
	 */
	public static <T> boolean notEmpty(Set<T> value) {
		return !isEmpty(value);
	}

	/**
	 * Gets a list converter to array.
	 * 
	 * @param list to convert to array
	 * @param type of {@code list} elements
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(List<T> list, Class<T> type) {
		try {
			return list.toArray((T[])Array.newInstance(type, list.size()));
		} catch (NullPointerException npe) {
			return (T[])new Object[] { };
		}
	}

}