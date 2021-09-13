package edu.yu.cs.com1320.project.impl;

class NumberHolder implements Comparable<NumberHolder> {
	protected Integer number;
	
	protected NumberHolder(int i) {
		this.number = i;
	}
	
	public int compareTo(NumberHolder other) {
		return this.number.compareTo(other.number);
	}
	
	public void setNumber(int i) {
		this.number = i;
	}
}
