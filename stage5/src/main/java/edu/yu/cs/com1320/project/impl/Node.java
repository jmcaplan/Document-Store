package edu.yu.cs.com1320.project.impl;

import java.util.*;

class Node<Value> {
	protected Node<Value>[] children;
	protected Set<Value> values;
	//protected boolean isRoot; might be useful later
	
	public Node(int alphabetSize) {
		this.children = new Node[alphabetSize];
		this.values = new HashSet<>();
	}	
	
}