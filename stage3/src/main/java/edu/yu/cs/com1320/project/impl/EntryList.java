package edu.yu.cs.com1320.project.impl;

class EntryList<K,V> {
	private Entry<K,V> first;
	
	protected EntryList() {
		this.first = null;
	}
	
	protected void add(Entry<K,V> newEntry) {
		if (first == null) {
			first = newEntry;
		}
		else {
			Entry<K,V> oldFirst = first;
			this.first = newEntry;
			newEntry.setNext(oldFirst);
		}
	}
	
	protected Entry<K,V> getFirst() {
		return first;
	}
	protected void setFirst(Entry<K,V> newFirst) {
		this.first = newFirst;
	}
}