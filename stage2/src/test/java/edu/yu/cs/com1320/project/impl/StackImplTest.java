package edu.yu.cs.com1320.project.impl;

import static org.junit.Assert.*;

import org.junit.Test;

public class StackImplTest {

	
	///////////////////// BASIC FUNCTIONALITY ///////////////////////
	
	@Test
	public void popTest() {
		StackImpl<String> stack = new StackImpl<>();
		stack.push("a");
		stack.push("b");
		assertEquals("simple pop", "b", stack.pop());
	}
	
	@Test
	public void peekTest() {
		StackImpl<String> stack = new StackImpl<>();
		stack.push("a");
		stack.push("b");
		assertEquals("simple peek", "b", stack.peek());
	}
	
	@Test
	public void peekReturnNullTest() {
		StackImpl<String> stack = new StackImpl<>();
		stack.push("a");
		stack.pop();
		assertEquals("peek should return null if stack is empty", null, stack.peek());
	}
	
	@Test
	public void pushTest() {
		StackImpl<String> stack = new StackImpl<>();
		stack.push("a");
		stack.push("b");
		assertEquals("simple push", "b", stack.peek());
	}
	
	@Test
	public void sizeTest() {
		StackImpl<String> stack = new StackImpl<>();
		stack.push("a");
		stack.push("b");
		stack.pop();
		assertEquals("simple size", 1, stack.size());
	}
	
	@Test
	public void heavyPushTest() {
		StackImpl<Integer> stack = new StackImpl<>();
		for (int i = 1; i<=100; i++) {
			stack.push(i);
		}
		assertEquals((Integer)100, stack.peek());
	}
	
	@Test
	public void heavyPopTest() {
		StackImpl<Integer> stack = new StackImpl<>();
		for (int i = 1; i<=100; i++) {
			stack.push(i);
		}
		for (int i = 1; i<=90; i++) {
			stack.pop();
		}
		assertEquals((Integer)10, stack.peek());
	}
	
	@Test
	public void heavySizeTest() {
		StackImpl<Integer> stack = new StackImpl<>();
		for (int i = 1; i<=100; i++) {
			stack.push(i);
		}
		for (int i = 1; i<=58; i++) {
			stack.pop();
		}
		assertEquals(42, stack.size());
	}

	
	////////////////// COMMAND SPECIFIC TESTS ///////////////////

}