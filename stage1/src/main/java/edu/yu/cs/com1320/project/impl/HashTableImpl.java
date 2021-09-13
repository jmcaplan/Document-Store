package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.*;


public class HashTableImpl<Key,Value> implements HashTable<Key,Value> {
	private final int ARRAY_LENGTH = 5;
	private EntryList<Key,Value>[] array;
	
	public HashTableImpl() {
		this.array = new EntryList[ARRAY_LENGTH];
	}
	
	/**
     * @param k the key whose value should be returned
     * @return the value that is stored in the HashTable for k, or null if there is no such key in the table
     */
    public Value get(Key k) {
    	int index = this.hashFunction(k); 
    	if (array[index] == null) {
    		return null;
    	}
    	Entry<Key,Value> currentEntry = array[index].getFirst();
    	while (currentEntry != null) {
    		if (currentEntry.getKey().equals(k)) {
    			return currentEntry.getValue();
    		} 
			currentEntry = currentEntry.getNext();
    	}
    	return null;
    }

    /**
     * @param k the key at which to store the value
     * @param v the value to store
     * @return if the key was already present in the HashTable, return the previous value stored for the key. If the key was not already present, return null.
     */
    public Value put(Key k, Value v) {
    	if (k == null) {
    		throw new IllegalArgumentException("Cannot add Entry to HashTable, Key was null");
    	}
    	if (v == null) {
    		Value previousValue = this.delete(k);
    		return previousValue;
    	}
    	int index = this.hashFunction(k);
    	if (array[index] == null) {
    		array[index] = new EntryList<Key,Value>();
    		array[index].add(new Entry<Key,Value>(k,v));
    		return null;
    	} // zero collision case
    	Entry<Key,Value> currentEntry = array[index].getFirst();
    	while (currentEntry != null) {
    		if (currentEntry.getKey().equals(k)) {
    			Value oldValue = currentEntry.getValue();
    			currentEntry.setValue(v);
    			return oldValue;
    		} 
    		currentEntry = currentEntry.getNext();
    	} // check if key is present, if so put new value
		  // there and return old value	
		array[index].add(new Entry<Key,Value>(k,v));
		return null; // last case, if key not present
    }
    
    private int hashFunction(Key key) {
    	return (key.hashCode() & 0x7fffffff) % this.ARRAY_LENGTH;
    }
    
    private Value delete(Key k) {
    	Value value;
    	if ( (value = this.get(k)) == null) {
    		throw new IllegalStateException("Could not delete entry, no such key in the hashTable");
    	}
    	int index = this.hashFunction(k);
    	Entry<Key,Value> currentEntry = array[index].getFirst(); // getting the head pointer to the list I want to search
    	if (currentEntry.getKey().equals(k)) {
    		array[index].setFirst(currentEntry.getNext());
    		return value;
    	} // case where the first entry in the list matches the given key
    	while (currentEntry != null) {
    		currentEntry = currentEntry.getNext();
    		Entry<Key,Value> previousEntry = currentEntry;
    		if (currentEntry.getKey().equals(k)) {
    			previousEntry.setNext(currentEntry.getNext());
    		}
    	}
		return value;
    }
}

