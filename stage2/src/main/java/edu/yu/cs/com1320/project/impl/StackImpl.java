package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.*;

public class StackImpl<T> implements Stack<T> {
	private int size;
	private StackElement<T> headPointer;
	
	/*
	 * No-argument constructor
	 */
	public StackImpl() {
		this.size = 0;
		this.headPointer = null;
	}
	
	/**
     * @param element object to add to the Stack
     */
    public void push(T element) {
    	if (this.headPointer == null) {
    		this.headPointer = new StackElement<T>(element);
    		this.size++;
    		return;
    	}
    	else {
    		StackElement<T> newHead = new StackElement<T>(element);
    		newHead.setNext(headPointer);
    		headPointer = newHead;
    		this.size++;
    	}
    }

    /**
     * removes and returns element at the top of the stack
     * @return element at the top of the stack, null if the stack is empty
     */
    public T pop() {
    	if (this.size == 0) {
    		return null;
    	}
    	StackElement<T> oldHeadPointer = this.headPointer;
		this.headPointer = this.headPointer.getNext();
		this.size--;
		return oldHeadPointer.getValue();
    }

    /**
     *
     * @return the element at the top of the stack without removing it
     */
    public T peek() {
    	if (this.size == 0) {
    		return null;
    	}
    	return headPointer.getValue();
    }

    /**
     *
     * @return how many elements are currently in the stack
     */
    public int size() {
    	return this.size;
    }
}

