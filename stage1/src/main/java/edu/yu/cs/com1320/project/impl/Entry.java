package edu.yu.cs.com1320.project.impl;

class Entry<K,V> {
	private K key;
	private V value;
	private Entry<K,V> next;
	
	protected Entry(K key, V value) {
		this.key = key;
		this.value = value;
		this.next = null;
	}
	
	protected V getValue() {
		return value;
	}
	protected void setValue(V value) {
		this.value = value;
	}
	protected K getKey() {
		return key;
	}
	protected void setKey(K key) {
		this.key = key;
	}
	protected Entry<K,V> getNext() {
		return next;
	}
	protected void setNext(Entry<K, V> next) {
		this.next = next;
	}
}
