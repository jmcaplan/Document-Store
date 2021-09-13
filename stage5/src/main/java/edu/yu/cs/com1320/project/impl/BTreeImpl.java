package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.impl.BTreeImpl.Entry;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;
import edu.yu.cs.com1320.project.stage5.DocumentStore.DocumentFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

public class BTreeImpl<Key extends Comparable<Key>, Value> implements BTree<Key, Value> {
	
	////////////////////////////////////////////
	/////////// INSTANCE VARIABLES /////////////
	////////////////////////////////////////////
	private PersistenceManager<Key,Value> pm;
    private static final int MAX = 6;
    protected Node root; 
    private Node leftMostExternalNode;
    protected int height;
    private int n; // number of key-value pairs in the B-Tree
    
    
    ////////////////////////////////////////////
    /////////// INTERNAL CLASSES ///////////////
    ////////////////////////////////////////////
    private static final class Node {
    	private int entryCount;
        private Entry[] entries = new Entry[BTreeImpl.MAX]; // the array of children
        private Node next;        // i have a feeling i won't need next or previous
        private Node previous;

     // create a node with k entries
        private Node(int k)
        {
            this.entryCount = k;
        }
        
     // getters and setters  
        private void setNext(Node next)
        {
            this.next = next;
        }
        private Node getNext()
        {
            return this.next;
        }
        private void setPrevious(Node previous)
        {
            this.previous = previous;
        }
        private Node getPrevious()
        {
            return this.previous;
        }
        
        private Entry[] getEntries()
        {
            return Arrays.copyOf(this.entries, this.entryCount);
        }
    }
    
    
    public static class Entry
    {
    	//internal nodes: only use key and child
        //external nodes: only use key and value       
    	private Comparable key;
        public Object val; 
        private Node child;

        public Entry(Comparable key, Object val, Node child)
        {
            this.key = key;
            this.val = val;
            this.child = child;
        }
        public Object getValue()
        {
            return this.val;
        }
        public Comparable getKey()
        {
            return this.key;
        }
    }
    
	
    ////////////////////////////////////////////
    /////////// ACTUAL METHODS /////////////////
    ////////////////////////////////////////////
    
    /**
     * Initializes an empty B-tree.
     */
    public BTreeImpl()
    {
        this.root = new Node(0);
        this.leftMostExternalNode = this.root;
    }
    
    public void moveToDisk(Key k) throws Exception {
        Entry entry = get(this.root, k, this.height);
    	if (entry == null) throw new IllegalStateException(
    			"Cannot move to disk, key is not in the btree");
        Value v = (Value) entry.val; 
        if (v == null) throw new IllegalStateException(
        		"Cannot move to disk, was already on disk or was deleted");
        pm.serialize(k, v); // now it's on the disk
        entry.val = null;   // we reflect that by setting val to null
    }
    
    public void setPersistenceManager(PersistenceManager<Key,Value> pm) {
    	this.pm = pm;
    }
    
    protected Value peek(Key key) {
    	if (key == null)
        {
            throw new IllegalArgumentException("argument to get() is null");
        }
        Entry entry = this.get(this.root, key, this.height);
        if(entry != null)
        {
        	Value value = (Value)entry.getValue(); // this will be the desired value,
        									// or null if it's currently on disk
        	                                // or if it was deleted
        	return value; 
        	// don't change disk, just return whatever is mapped to by this key currently
        }
        return null; // key was not in the btree
    }    
    
    /**
     * Returns the value associated with the given key.
     *
     * @param key the key
     * @return the value associated with the given key if the key is in the
     *         symbol table and {@code null} if the key is not in the symbol
     *         table
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    public Value get(Key key)
    {
        if (key == null)
        {
            throw new IllegalArgumentException("argument to get() is null");
        }
        Entry entry = this.get(this.root, key, this.height);
        if(entry != null)
        {
        	Value value = (Value)entry.val; // this will be the desired value,
        									// or null if it's currently on disk
        	                                // or if it was deleted
        	if (value != null) return value;
        	// if made it this far, it's on disk or was deleted. We try to retrieve it
        	Value fromDisk = null;
			try {
				fromDisk = pm.deserialize((Key)entry.key);
				// this will be the Value object if it is on disk,
				// otherwise deserialize will return null
			} catch (IOException e) {
				e.printStackTrace(); // something taka went wrong
			}
        	entry.val = fromDisk; // it must be placed back into memory
        	return fromDisk; // if on disk, we retrieve it
        }
        return null; // key was not in the btree
    }
    
    //change to private!
    public Entry get(Node currentNode, Key key, int height)
    {
        Entry[] entries = currentNode.entries;

        //current node is external (i.e. height == 0)
        if (height == 0)
        {
            for (int j = 0; j < currentNode.entryCount; j++)
            {
                if(isEqual(key, entries[j].key))
                {
                    //found desired key. Return its value
                    return entries[j];
                }
            }
            //didn't find the key
            return null;
        }

        //current node is internal (height > 0)
        else
        {
            for (int j = 0; j < currentNode.entryCount; j++)
            {
                //if (we are at the last key in this node OR the key we
                //are looking for is less than the next key, i.e. the
                //desired key must be in the subtree below the current entry),
                //then recurse into the current entry’s child
                if (j + 1 == currentNode.entryCount || less(key, entries[j + 1].key))
                {
                    return this.get(entries[j].child, key, height - 1);
                }
            }
            //didn't find the key
            return null;
        }
    }
    
    /**
    *
    * @param key
    */
   protected void delete(Key key)
   {
       put(key, null);
   }

   /**
    * Inserts the key-value pair into the symbol table, overwriting the old
    * value with the new value if the key is already in the symbol table. If
    * the value is {@code null}, this effectively deletes the key from the
    * symbol table.
    *
    * @param key the key
    * @param val the value
    * @return previous val stored at the given key, null if key wasn't already there
    *  or was there but val was null (it was previously deleted)
    * @throws IllegalArgumentException if {@code key} is {@code null}
    */
   public Value put(Key key, Value val)
   {
       if (key == null)
       {
           throw new IllegalArgumentException("argument key to put() is null");
       }
       //if the key already exists in the b-tree, grab the previous value,
       //put in the new value, return previous, whether that previous was
       //non-null, actual null, or null representing an onDIsk value
       Entry alreadyThere = this.get(this.root, key, this.height);
       if(alreadyThere != null)
       {
    	   Value previousValue = (Value) alreadyThere.val;
    	   alreadyThere.val = val; // the new val at this key is what they passed
           if (previousValue != null) { // the prev val was on memory, no cleaning of disk needed
        	   
        	   return previousValue;
           }
           // if we got here, that means the previous value was null,
           // so either it had been deleted OR it is on disk. Either way
           // we call deserialize, b/c if it had been deleted, it will return
           // null and that's fine, and if it was on disk, it will fetch
           // it, return it, and will have also deleted it from disk
           // b/c of the deserialize call
           try {
			  previousValue = pm.deserialize(key);
		   } catch (IOException e) {
			  e.printStackTrace();
		   }
           return previousValue;
       }

       Node newNode = this.put(this.root, key, val, this.height);
       this.n++;
       if (newNode == null)
       {
           return null;
       }

       //split the root:
       //Create a new node to be the root.
       //Set the old root to be new root's first entry.
       //Set the node returned from the call to put to be new root's second entry
       Node newRoot = new Node(2);
       newRoot.entries[0] = new Entry(this.root.entries[0].key, null, this.root);
       newRoot.entries[1] = new Entry(newNode.entries[0].key, null, newNode);
       this.root = newRoot;
       //a split at the root always increases the tree height by 1
       this.height++;
       return null;
   }
   
   /**
   *
   * @param currentNode
   * @param key
   * @param val
   * @param height
   * @return null if no new node was created (i.e. just added a new Entry into an existing node). If a new node was created due to the need to split, returns the new node
   */
  private Node put(Node currentNode, Key key, Value val, int height)
  {
      int j;
      Entry newEntry = new Entry(key, val, null);

      //external node
      if (height == 0)
      {
          //find index in currentNode’s entry[] to insert new entry
          //we look for key < entry.key since we want to leave j
          //pointing to the slot to insert the new entry, hence we want to find
          //the first entry in the current node that key is LESS THAN
          for (j = 0; j < currentNode.entryCount; j++)
          {
              if (less(key, currentNode.entries[j].key))
              {
                  break;
              }
          }
      }

      // internal node
      else
      {
          //find index in node entry array to insert the new entry
          for (j = 0; j < currentNode.entryCount; j++)
          {
              //if (we are at the last key in this node OR the key we
              //are looking for is less than the next key, i.e. the
              //desired key must be added to the subtree below the current entry),
              //then do a recursive call to put on the current entry’s child
              if ((j + 1 == currentNode.entryCount) || less(key, currentNode.entries[j + 1].key))
              {
                  //increment j (j++) after the call so that a new entry created by a split
                  //will be inserted in the next slot
                  Node newNode = this.put(currentNode.entries[j++].child, key, val, height - 1);
                  if (newNode == null)
                  {
                      return null;
                  }
                  //if the call to put returned a node, it means I need to add a new entry to
                  //the current node
                  newEntry.key = newNode.entries[0].key;
                  newEntry.val = null;
                  newEntry.child = newNode;
                  break;
              }
          }
      }
      //shift entries over one place to make room for new entry
      for (int i = currentNode.entryCount; i > j; i--)
      {
          currentNode.entries[i] = currentNode.entries[i - 1];
      }
      //add new entry
      currentNode.entries[j] = newEntry;
      currentNode.entryCount++;
      if (currentNode.entryCount < BTreeImpl.MAX)
      {
          //no structural changes needed in the tree
          //so just return null
          return null;
      }
      else
      {
          //will have to create new entry in the parent due
          //to the split, so return the new node, which is
          //the node for which the new entry will be created
          return this.split(currentNode, height);
      }
  	}
    
  
  
   
    
    /////////// UTILITIES ///////////////
    /**
     * split node in half
     * @param currentNode
     * @return new node
     */
    private Node split(Node currentNode, int height)
    {
        Node newNode = new Node(BTreeImpl.MAX / 2);
        //by changing currentNode.entryCount, we will treat any value
        //at index higher than the new currentNode.entryCount as if
        //it doesn't exist
        currentNode.entryCount = BTreeImpl.MAX / 2;
        //copy top half of h into t
        for (int j = 0; j < BTreeImpl.MAX / 2; j++)
        {
            newNode.entries[j] = currentNode.entries[BTreeImpl.MAX / 2 + j];
        }
        //external node
        if (height == 0)
        {
            newNode.setNext(currentNode.getNext());
            newNode.setPrevious(currentNode);
            currentNode.setNext(newNode);
        }
        return newNode;
    }
    // comparison functions - make Comparable instead of Key to avoid casts
    private static boolean less(Comparable k1, Comparable k2)
    {
        return k1.compareTo(k2) < 0;
    }
    private static boolean isEqual(Comparable k1, Comparable k2)
    {
        return k1.compareTo(k2) == 0;
    }

    // --> not sure if i need these, isEmpty, size, height, getOrderedEntries, getMin/Max
    // i'll remove later if don't need...
    
    /**
     * Returns true if this symbol table is empty.
     *
     * @return {@code true} if this symbol table is empty; {@code false}
     *         otherwise
     */
    protected boolean isEmpty()
    {
        return this.size() == 0;
    }
    /**
     * @return the number of key-value pairs in this symbol table
     */
    protected int size()
    {
        return this.n;
    }
    /**
     * @return the height of this B-tree
     */
    protected int height()
    {
        return this.height;
    }
    /**
     * returns a list of all the entries in the Btree, ordered by key
     * @return
     */
    protected ArrayList<Entry> getOrderedEntries()
    {
        Node current = this.leftMostExternalNode;
        ArrayList<Entry> entries = new ArrayList<>();
        while(current != null) 
        {
            for(Entry e : current.getEntries())
            {
                if(e.val != null)
                {
                    entries.add(e);
                }
            }
            current = current.getNext();
        }
        return entries;
    }
    protected Entry getMinEntry()
    {
        Node current = this.leftMostExternalNode;
        while(current != null)
        {
            for(Entry e : current.getEntries())
            {
                if(e.val != null)
                {
                    return e;
                }
            }
        }
        return null;
    }
    protected Entry getMaxEntry()
    {
        ArrayList<Entry> entries = this.getOrderedEntries();
        return entries.get(entries.size()-1);
    }
    
    /**
     * Unit tests the {@code WrongBTree} data type.
     *
     * @param args the command-line arguments
     */
    /*
    public static void main(String[] args)
    {
        BTreeImpl<Integer, String> st = new BTreeImpl<Integer, String>();
        st.put(1, "one");
        st.put(2, "two");
        st.put(3, "three");
        st.put(4, "four");
        st.put(5, "five");
        st.put(6, "six");
        st.put(7, "seven");
        st.put(8, "eight");
        st.put(9, "nine");
        st.put(10, "ten");
        st.put(11, "eleven");
        st.put(12, "twelve");
        st.put(13, "thirteen");
        st.put(14, "fourteen");
        st.put(15, "fifteen");
        st.put(16, "sixteen");
        st.put(17, "seventeen");
        st.put(18, "eighteen");
        st.put(19, "nineteen");
        st.put(20, "twenty");
        st.put(21, "twenty one");
        st.put(22, "twenty two");
        st.put(23, "twenty three");
        st.put(24, "twenty four");
        st.put(25, "twenty five");
        st.put(26, "twenty six");
        System.out.println("Size: " + st.size());
        System.out.println("Height: " + st.height);
        System.out.println("Key-value pairs, sorted by key:");
        ArrayList<Entry> entries = st.getOrderedEntries();
        for(Entry e : entries)
        {
            System.out.println("key = " + e.getKey() + "; value = " + e.getValue());
        }
        Entry min = st.getMinEntry();
        System.out.println("Minimum Entry: " + "key = " + min.getKey() + "; value = " + min.getValue());
        Entry max = st.getMaxEntry();
        System.out.println("Maximum Entry: " + "key = " + max.getKey() + "; value = " + max.getValue());
        st.delete(1);
        min = st.getMinEntry();
        System.out.println("Minimum Entry after deleting 1: " + "key = " + min.getKey() + "; value = " + min.getValue());
        st.delete(26);
        max = st.getMaxEntry();
        System.out.println("Maximum Entry after deleting 26: " + "key = " + max.getKey() + "; value = " + max.getValue());
        System.out.println("Size after deleting: " + st.size());
        System.out.println("Size without nulls after deleting: " + st.getOrderedEntries().size());
        
        System.out.println("Key-value pairs, sorted by key:");
        entries = st.getOrderedEntries();
        for(Entry e : entries)
        {
            System.out.println("key = " + e.getKey() + "; value = " + e.getValue());
        }
    } */
    
}