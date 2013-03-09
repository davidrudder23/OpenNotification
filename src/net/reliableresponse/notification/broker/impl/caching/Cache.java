/*
 * Created on Apr 28, 2005
 *
 *Copyright Reliable Response, 2005
 */
package net.reliableresponse.notification.broker.impl.caching;

import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import net.reliableresponse.notification.UniquelyIdentifiable;

/**
 * @author drig
 *
 * Copyright 2004 - David Rudder
 */
public class Cache extends Vector {
	public static final int METHOD_FIFO = 0;
	public static final int METHOD_LRU = 1;
	
	private Vector objects;
	private Vector times;
	private Hashtable uuids;
	private int maxObjects;
	private int maxSeconds;
	private int method;
	
	public Cache (int maxObjects, int maxSeconds, int method) {
		this.maxObjects = maxObjects;
		this.maxSeconds = maxSeconds;
		this.method = method;
		
		objects = new Vector();
		times = new Vector();
		uuids = new Hashtable();
	}
	
	public synchronized void purge() {
		// Figure out the max size to look at.  Both vectors *should* be the same size, but it can't
		// hurt to check
		int maxSize = objects.size();
		if (times.size() < maxSize) maxSize = times.size();
		
		// Purge by date
		int i = 0;
		while (i < maxSize) {
			Date date = (Date)times.elementAt(i);
			if ((maxSeconds*1000) < (System.currentTimeMillis() - date.getTime())) {
				objects.removeElementAt(i);
				times.removeElementAt(i);
				maxSize--;
			} else {
				i++;
			}
		}
		
		// Purge by maxObjects
		while (objects.size() > maxObjects) {
			objects.removeElementAt(0);
			times.removeElementAt(0);
		}
	}
	
	public void add(int index, Object element) {
		purge();
		objects.add(index, element);
		times.add (index, new Date());
		if (element instanceof UniquelyIdentifiable) {
			UniquelyIdentifiable id = (UniquelyIdentifiable)element;
			uuids.put (id.getUuid(), id);
		}
	}
	public synchronized boolean add(Object o) {
		purge();
		if (o instanceof UniquelyIdentifiable) {
			UniquelyIdentifiable id = (UniquelyIdentifiable)o;
			uuids.put (id.getUuid(), id);
		}

		return (objects.add(o) && times.add (new Date()));
	}
	
	public synchronized boolean addAll(Collection c) {
		purge();
		Iterator iterator = c.iterator();
		while (iterator.hasNext()) {
			Object o = iterator.next();
			add (o);
		}
		return true;
	}
	public synchronized boolean addAll(int index, Collection c) {
		purge();
		Iterator iterator = c.iterator();
		while (iterator.hasNext()) {
			Object o = iterator.next();
			add (index, o);
		}
		return true;
	}
	public synchronized void addElement(Object obj) {
		purge();
		objects.addElement(obj);
		times.addElement(new Date());
		if (obj instanceof UniquelyIdentifiable) {
			UniquelyIdentifiable id = (UniquelyIdentifiable)obj;
			uuids.put (id.getUuid(), id);
		}

	}
	
	public synchronized int capacity() {
		purge();
		return objects.capacity();
	}
	
	public void clear() {
		purge();
		objects.clear();
		times.clear();
	}

	public boolean contains(Object elem) {
		purge();
		return objects.contains(elem);
	}
	
	public synchronized boolean containsAll(Collection c) {
		purge();
		return objects.containsAll(c);
	}

	public synchronized void copyInto(Object[] anArray) {
		purge();
		objects.copyInto(anArray);
	}
	public synchronized Object elementAt(int index) {
		//purge();
		return objects.elementAt(index);
	}

	public Enumeration elements() {
		purge();
		return objects.elements();
	}

	public synchronized void ensureCapacity(int minCapacity) {
		purge();
		objects.ensureCapacity(minCapacity);
		times.ensureCapacity(minCapacity);
	}

	public synchronized Object firstElement() {
		purge();
		return objects.firstElement();
	}

	public synchronized Object get(int index) {
		purge();
		return objects.get(index);
	}

	public synchronized int indexOf(Object elem, int index) {
		purge();
		return objects.indexOf(elem, index);
	}

	public int indexOf(Object elem) {
		purge();
		return objects.indexOf(elem);
	}

	public synchronized void insertElementAt(Object obj, int index) {
		purge();
		objects.insertElementAt(obj, index);
		times.insertElementAt(new Date(), index);
		if (obj instanceof UniquelyIdentifiable) {
			UniquelyIdentifiable id = (UniquelyIdentifiable)obj;
			uuids.put (id.getUuid(), id);
		}
	}
	
	public synchronized boolean isEmpty() {
		purge();
		return objects.isEmpty();
	}

	public synchronized Object lastElement() {
		purge();
		return objects.lastElement();
	}

	public synchronized int lastIndexOf(Object elem, int index) {
		purge();
		return objects.lastIndexOf(elem, index);
	}

	public synchronized int lastIndexOf(Object elem) {
		purge();
		return objects.lastIndexOf(elem);
	}

	public synchronized Object remove(int index) {
		purge();
		times.remove(index);
		Object element = elementAt(index);
		if (element instanceof UniquelyIdentifiable) {
			UniquelyIdentifiable id = (UniquelyIdentifiable)element;
			uuids.remove (id.getUuid());
		}

		return objects.remove(index);
	}

	public boolean remove(Object o) {
		purge();
		times.remove(o);
		if (o instanceof UniquelyIdentifiable) {
			UniquelyIdentifiable id = (UniquelyIdentifiable)o;
			uuids.remove (id.getUuid());
		}
		return objects.remove(o);
	}

	public synchronized boolean removeAll(Collection c) {
		purge();
		Iterator iterator = c.iterator();
		while (iterator.hasNext()) {
			Object object = iterator.next();
			if (object instanceof UniquelyIdentifiable) {
				UniquelyIdentifiable id = (UniquelyIdentifiable)object;
				uuids.remove (id.getUuid());
			}
			if (!remove(object)) {
				return false;
			}
		}
		return true;
	}

	public synchronized void removeAllElements() {
		purge();
		times.removeAllElements();
		objects.removeAllElements();
		uuids.clear();
	}

	public synchronized boolean removeElement(Object obj) {
		purge();
		int index = objects.indexOf(obj);
		if (index < 0) return false;
		times.remove(index);
		
		if (obj instanceof UniquelyIdentifiable) {
			UniquelyIdentifiable id = (UniquelyIdentifiable)obj;
			uuids.remove (id.getUuid());
		}

		return objects.removeElement(obj);
	}

	public synchronized void removeElementAt(int index) {
		purge();

		Object element = elementAt(index);
		if (element instanceof UniquelyIdentifiable) {
			UniquelyIdentifiable id = (UniquelyIdentifiable)element;
			uuids.remove (id.getUuid());
		}

		times.removeElementAt(index);
		objects.removeElementAt(index);
	}

	public synchronized boolean retainAll(Collection c) {
		// This will not work in the cache
		// I can probably do it, but it's too much work
		purge();
		return false;
	}

	public synchronized Object set(int index, Object element) {
		purge();
		times.set (index, new Date());
		if (element instanceof UniquelyIdentifiable) {
			UniquelyIdentifiable id = (UniquelyIdentifiable)element;
			uuids.put (id.getUuid(), id);
		}
		return objects.set(index, element);
	}

	public synchronized void setElementAt(Object obj, int index) {
		purge();
		times.setElementAt(new Date(), index);
		objects.setElementAt(obj, index);
		if (obj instanceof UniquelyIdentifiable) {
			UniquelyIdentifiable id = (UniquelyIdentifiable)obj;
			uuids.put (id.getUuid(), id);
		}
	}

	public synchronized void setSize(int newSize) {
		purge();
		times.setSize(newSize);
		objects.setSize(newSize);
	}

	public synchronized int size() {
		purge();
		return objects.size();
	}

	public synchronized List subList(int fromIndex, int toIndex) {
		purge();
		return objects.subList(fromIndex, toIndex);
	}

	public synchronized Object[] toArray() {
		purge();
		return objects.toArray();
	}

	public synchronized Object[] toArray(Object[] a) {
		purge();
		return objects.toArray(a);
	}

	public synchronized void trimToSize() {
		purge();
		times.trimToSize();
		objects.trimToSize();
	}
	
	public Object getByUuid(String uuid) {
		if (uuid == null) return null;
		return uuids.get(uuid);
	}
}
