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
		hashTable.put(1, "one");
		hashTable.put(2, "two");
		hashTable.put(1, null);
		assertEquals("testing deletion", null, hashTable.get(1));
	}
	
	@Test (expected = IllegalStateException.class)
	public void deleteWhenNotExistTest() {
		hashTable.put(1, "one");
		hashTable.put(2, "two");
		hashTable.put(3, null);
	}
	
	/*@Test
	public void secondDeleteTest() {
		hashTable.put(1, "one");
		hashTable.put(2, "two");
		hashTable.put(3, "three");
		hashTable.put(3, "shalosh");
		hashTable.put(3, null);
		assertEquals("testing deletion", null, hashTable.array[hashTable.hashFunction(3)]);
	}*/
	

}
