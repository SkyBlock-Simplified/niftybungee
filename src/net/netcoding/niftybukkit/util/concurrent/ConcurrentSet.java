package net.netcoding.niftybukkit.util.concurrent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A concurrent set that allows for simultaneously fast reading, iteration and
 * modification utilizing {@link AtomicReference}.
 * <p>
 * The AtomicReference changes the methods that modify the set by replacing the
 * entire set each modification. This allows for maintaining the original speed of
 * {@link HashSet#contains(Object)} and makes it cross-thread-safe.
 * 
 * @param <T> type of elements
 */
public class ConcurrentSet<T> implements Set<T> {

	private final AtomicReference<Set<T>> ref;

	/**
	 * Create a new concurrent set.
	 */
	public ConcurrentSet() {
		this.ref = new AtomicReference<Set<T>>(new HashSet<T>());
	}

	/**
	 * Create a new concurrent set and fill it with the given collection.
	 */
	public ConcurrentSet(Collection<? extends T> collection) {
		this.ref = new AtomicReference<Set<T>>(new HashSet<T>(collection));
	}

	@Override
	public boolean add(T item) {
		while (true) {
			Set<T> current = this.ref.get();
			if (current.contains(item)) return false;
			Set<T> modified = new HashSet<T>(current);
			modified.add(item);
			if (this.ref.compareAndSet(current, modified)) return true;
		}
	}

	@Override
	public boolean addAll(Collection<? extends T> collection) {
		while (true) {
			Set<T> current = this.ref.get();
			Set<T> modified = new HashSet<T>(current);
			modified.addAll(collection);
			if (this.ref.compareAndSet(current, modified)) return true;
		}
	}

	@Override
	public void clear() {
		this.ref.get().clear();
	}

	@Override
	public boolean contains(Object item) {
		return ref.get().contains(item);
	}

	@Override
	public boolean containsAll(Collection<?> collection) {
		return this.ref.get().containsAll(collection);
	}

	@Override
	public boolean isEmpty() {
		return this.ref.get().isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return this.ref.get().iterator();
	}

	@Override
	public boolean remove(Object item) {
		while (true) {
			Set<T> current = this.ref.get();
			if (!current.contains(item)) return false;
			Set<T> modified = new HashSet<T>(current);
			modified.remove(item);
			if (this.ref.compareAndSet(current, modified)) return true;
		}
	}

	@Override
	public boolean removeAll(Collection<?> collection) {
		boolean changed = false;
		for (Object item : collection) changed = this.remove(item) || changed;
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> collection) {
		return this.ref.get().retainAll(collection);
	}

	@Override
	public int size() {
		return this.ref.get().size();
	}

	@Override
	public Object[] toArray() {
		return this.ref.get().toArray();
	}

	@Override
	public <U> U[] toArray(U[] array) {
		return this.ref.get().toArray(array);
	}

}