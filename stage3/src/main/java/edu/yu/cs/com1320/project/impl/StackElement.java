package edu.yu.cs.com1320.project.impl;

class StackElement<T> {
	private T value;
	private StackElement<T> next;
	
	protected StackElement(T value) {
		this.value = value;
		this.next = null;
	}
	
	protected T getValue() {
		return value;
	}
	protected StackElement<T> getNext() {
		return next;
	}
	protected void setNext(StackElement<T> next) {
		this.next = next;
	}
	
}