package net.netcoding.niftybungee.util;

import java.lang.reflect.Array;
import java.util.Collection;

/**
 * Array and List Checking/Converting
 */
public class ListUtil {

	/**
	 * Gets the number of elements in this array.
	 * 
	 * @param array Array to retrieve size of.
	 * @return Number of elements in this array.
	 */
	public static <T> int sizeOf(T[] array) {
		return array.length;
	}

	/**
	 * Gets the number of elements in this collection.
	 * 
	 * @param collection Collection to retrieve size of.
	 * @return Number of elements in this collection.
	 */
	public static <T> int sizeOf(Collection<? extends T> collection) {
		return collection.size();
	}

	/**
	 * Gets if the {@code value} is empty or null.
	 * 
	 * @param value to check
	 * @return true if empty or null, otherwise false
	 */
	public static <T> boolean isEmpty(T[] array) {
		return array == null || array.length == 0;
	}

	/**
	 * Gets if the {@code value} is empty or null.
	 * 
	 * @param value to check
	 * @return true if empty or null, otherwise false
	 */
	public static <T> boolean isEmpty(Collection<? extends T> collection) {
		return collection == null || collection.isEmpty();
	}

	/**
	 * Gets if the {@code value} is not empty.
	 * 
	 * @param value to check
	 * @return true if not empty or null, otherwise false
	 */
	public static <T> boolean notEmpty(T[] array) {
		return !isEmpty(array);
	}

	/**
	 * Gets if the {@code value} is not empty.
	 * 
	 * @param value to check
	 * @return true if not empty or null, otherwise false
	 */
	public static <T> boolean notEmpty(Collection<? extends T> collection) {
		return !isEmpty(collection);
	}

	/**
	 * Gets a list converter to array.
	 * 
	 * @param list to convert to array
	 * @param type of {@code list} elements
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(Collection<? extends T> collection, Class<T> type) {
		try {
			return collection.toArray((T[])Array.newInstance(type, collection.size()));
		} catch (NullPointerException npe) {
			return (T[])new Object[] { };
		}
	}

}