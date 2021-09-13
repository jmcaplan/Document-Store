package edu.yu.cs.com1320.project.impl;

import static org.junit.Assert.*;

import org.junit.Test;

public class HashTableImplTest {

	HashTableImpl<Integer,String> hashTable = new HashTableImpl<Integer,String>();
	
	@Test
	public void getTest() {
		hashTable.put(613, "mitzvos");
		assertEquals("testing get with 613, mitzvos", "mitzvos", hashTable.get(613));
	}
	
	@Test
	public void nullGetTest() {
		hashTable.put(64234, "mitzvos");
		assertEquals("testing where key is not present", null, hashTable.get(200));
	}
	
	@Test
	public void putNegativeIndexTest() {
		hashTable.put(-25, "mitzvos");
		assertEquals("testing where key is not present", "mitzvos", hashTable.get(-25));
	}
	
	@Test
	public void putReturnsPreviousValueTest() {
		hashTable.put(613, "mitzvos");
		assertEquals("testing put returns the previous value", "mitzvos", hashTable.put(613, "second torah?"));
	}
	
	@Test
	public void putReturnsNullTest() {
		assertEquals("testing put returns null if no previous value", null, hashTable.put(1, "HaShem"));
	}
	
	@Test
	public void deleteTest() {
		hashTable.put(1, "a");
		hashTable.put(2, "b");
		hashTable.put(3, "c");
		hashTable.put(4, "d");
		hashTable.put(5, "e");
		hashTable.put(6, "f");
		hashTable.put(7, "g");
		hashTable.put(8, "h");
		hashTable.put(9, "i");
		hashTable.put(10, "j");
		hashTable.put(11, "k");
		hashTable.put(12, "l");
		hashTable.put(16, "p");
		hashTable.put(17, "q");
		hashTable.put(18, "r");
		hashTable.put(19, "s");
		hashTable.put(20, "t");
		hashTable.put(21, "u");
		hashTable.put(2, null);
		assertEquals("testing deletion", null, hashTable.get(2));
	}
	
	/*
	 * Test of array doubling, only works if fields of hashTable are made public
	 *
	@Test
	public void arrayDoublingFunctionalityTest() {
		hashTable.put(1, "a");
		hashTable.put(2, "b");
		hashTable.put(3, "c");
		hashTable.put(4, "d");
		hashTable.put(5, "e");
		hashTable.put(6, "f");
		hashTable.put(7, "g");
		hashTable.put(8, "h");
		hashTable.put(9, "i");
		hashTable.put(10, "j");
		hashTable.put(11, "k");
		hashTable.put(12, "l");
		hashTable.put(13, "m");
		hashTable.put(14, "n");
		hashTable.put(15, "o");
		hashTable.put(16, "p");
		hashTable.put(17, "q");
		hashTable.put(18, "r");
		hashTable.put(19, "s");
		hashTable.put(20, "t");
		hashTable.put(21, "u");
		assertEquals("testing array doubling, nElements", 21, hashTable.nElements);
		assertEquals("testing array doubling, array size", 10, hashTable.array.length);
		assertEquals("testing array doubling, still have functional getting", "q", hashTable.get(17));
	} */
	
	
	@Test (expected = IllegalStateException.class)
	public void deleteWhenNotExistTest() {
		hashTable.put(1, "one");
		hashTable.put(2, "two");
		hashTable.put(3, null);
	}

}