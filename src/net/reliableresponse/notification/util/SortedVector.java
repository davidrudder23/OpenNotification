/*
 * Created on Aug 13, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.reliableresponse.notification.util;

import java.util.Comparator;
import java.util.Vector;

public class SortedVector extends Vector {

	public SortedVector() {
	}
	
	public SortedVector (Object[] contents) {
		for (int i = 0; i < contents.length; i++) {
			super.addElement(contents[i]);
		}
		sort();
	}

	/* (non-Javadoc)
	 * @see java.util.Vector#addElement(java.lang.Object)
	 */
	public synchronized void addElement(Object obj) {
		// TODO Auto-generated method stub
		super.addElement(obj);

		sort();
	}
	
	public synchronized void addElement(Object obj, boolean sort) {
		// TODO Auto-generated method stub
		super.addElement(obj);

		if (sort)
			sort();
	}
	
	private Comparable getComparable(Object element) {
		if (element instanceof Comparable) {
			return (Comparable)element;
		} else {
			return element.toString();
		}
	}
	
	public void sort () {
		int n = size();
		for (int i = 1; i < n; i++) {
			Object element = elementAt(i);
			
			int j = i - 1;
			while (j >= 0 && 
			  (getComparable(element).compareTo(getComparable(elementAt(j))) < 0)) {
				Object val = elementAt(j);
				setElementAt(val, j+1);
				j = j - 1;
			}
		setElementAt(element, j+1);
		}
	}
}