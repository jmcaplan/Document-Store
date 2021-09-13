package edu.yu.cs.com1320.project.impl;

import java.util.Comparator;
import java.util.*;

import edu.yu.cs.com1320.project.*;

public class TrieImpl<Value> implements Trie<Value> {
	
	private Node<Value> root;
	private int ALPHABET_SIZE;
	
	public TrieImpl() {
		this(256);
	}
	
	public TrieImpl(int alphabetSize) {
		this.ALPHABET_SIZE = alphabetSize;
		this.root = new Node<Value>(ALPHABET_SIZE);
	}
	
	/**
     * add the given value to the Set at the given key
     * @param key
     * @param val
     */
    public void put(String key, Value val) {
    	//deleteAll the value from this key
        if (val == null) return;
        else
        {
        	key = key.toLowerCase();
        	this.root = put(this.root, key, val, 0);
        }
    }
    
    /**
    *
    * @param node
    * @param key
    * @param val
    * @param d
    * @return
    */
   private Node<Value> put(Node<Value> node, String key, Value val, int d)
   {
	   //create a new node
       if (node == null)
       {
           node = new Node<Value>(ALPHABET_SIZE);
       }
       //we've reached the last node in the key,
       //set the value for the key and return the node
       if (d == key.length())
       {
           node.values.add(val);
           return node;
       }
       //proceed to the next node in the chain of nodes that
       //forms the desired key
       char c = key.charAt(d);
       node.children[c] = this.put(node.children[c], key, val, d + 1);
       return node;
   }

    /**
     * get all exact matches for the given key, sorted in descending order.
     * Search is CASE INSENSITIVE.
     * @param key
     * @param comparator used to sort values
     * @return a List of matching Values, in descending order
     */
    public List<Value> getAllSorted(String key, Comparator<Value> comparator) {
    	key = key.toLowerCase();
    	Node<Value> node = this.get(this.root, key,0);
    	if (node == null || node.values.isEmpty()) return new ArrayList<Value>(); 
    	// no such key in the trie -or- no value at this key, return empty list
    	return nodeToList(node,comparator);
    }
    
    protected Node<Value> get(Node<Value> node, String key, int d) {
    	if (node == null) return null; // miss!
    	if (key.equals("")) return root;
    	if (d == key.length()) return node;
    	char c = key.charAt(d);
    	return this.get(node.children[c], key, d+1);
    }
    
    private List<Value> nodeToList(Node<Value> node, Comparator<Value> comparator) {
    	List<Value> list = new ArrayList<>();
    	list.addAll(node.values); // add the values from the Set at this node
    	list.sort(comparator);  // sort them in ascending order
    	// Collections.reverse(list); // apparently we should let them do the reversing
    	return list;
    }
    

    /**
     * get all matches which contain a String with the given prefix, sorted in descending order.
     * For example, if the key is "Too", you would return any value that contains "Tool", "Too", "Tooth", "Toodle", etc.
     * Search is CASE INSENSITIVE.
     * @param prefix
     * @param comparator used to sort values
     * @return a List of all matching Values containing the given prefix, in descending order
     */
    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator) {
    	prefix = prefix.toLowerCase();
    	Node<Value> prefixNode = this.get(this.root, prefix, 0);
    	if (prefixNode == null) return new ArrayList<Value>(); // miss!
    	Set< Node<Value> > nodeSet = getSubTrieInclusive(prefixNode);
    	return nodesToList(nodeSet, comparator);
    }
    
    private List<Value> nodesToList(Set<Node<Value>> nodeSet, Comparator<Value> comparator) {
    	Set<Value> vals = new HashSet<>();
    	for (Node<Value> n: nodeSet) {
    		vals.addAll(n.values);
    	} // first add all the values at all the nodes to one big set
    	  // -- note that this avoids duplicate values
    	List<Value> list = new ArrayList<>();
    	list.addAll(vals); // add to the list all the values from the Set of all values of all nodes
    	//list.addAll(node.values); // add the values from the Set at this node
    	list.sort(comparator);  // sort them in ascending order
    	// Collections.reverse(list); // apparently we should let them do the reversing
    	return list;
    }
    
    
    /*
     * returns a Set of all the nodes beneath this one plus itself
     */
    private Set< Node<Value> > getSubTrieInclusive(Node<Value> node) {
    	Set< Node<Value> > nodeSet = new HashSet<>();
    	nodeSet.add(node);
    	for (int c = 0; c < ALPHABET_SIZE; c++) {
    		Node<Value> child;
    		if ( (child = node.children[c]) != null) {
    			nodeSet.addAll(getSubTrieInclusive(child));
    		}
    	}
    	return nodeSet;
    }
    
    private Set< Node<Value> > getSubTrieExclusive(Node<Value> node) {
    	Set< Node<Value> > nodeSet = getSubTrieInclusive(node);
    	nodeSet.remove(node);
    	return nodeSet;
    }

    /**
     * Delete the subtree rooted at the last character of the prefix.
     * Search is CASE INSENSITIVE.
     * @param prefix
     * @return a Set of all Values that were deleted.
     */
    public Set<Value> deleteAllWithPrefix(String prefix) {
    	return version1(prefix);
    	// return version2(prefix);
    }
    private Set<Value> version1(String prefix) {
    	Node<Value> node = this.get(this.root, prefix,0);
    	if (node == null) return new HashSet<Value>();
    	Set< Node<Value> > nodeSet = getSubTrieExclusive(node);
    		// first we get all nodes under the node assoc. w/ this prefix
    	Set<Value> deletedValues = new HashSet<>();
    	for (Node<Value> n: nodeSet) {
    		deletedValues.addAll(n.values);
    	} // we grab the values from all those nodes into a big set
    	deletedValues.addAll(deleteAll(prefix)); 
    		// add this node's to the set, --note that node now has no vals
    	String parentKey = prefix.substring(0, prefix.length()-1);
    	Node<Value> parentNode = this.get(root, parentKey, 0);
	    parentNode.children[prefix.charAt(prefix.length()-1)] = null; // delete this node from parent's children
	    return deletedValues;
    }
    

    /**
     * Delete all values from the node of the given key (do not remove the values from other nodes in the Trie)
     * @param key
     * @return a Set of all Values that were deleted.
     */
    public Set<Value> deleteAll(String key) {
    	Node<Value> node = this.get(this.root, key,0);
    	Set<Value> deletedValues = new HashSet<>();
    	if (node == null) return deletedValues;
    	deletedValues.addAll(node.values);
    	node.values.clear();
    	cleanIfLeaf(node,key);
    	return deletedValues;
    }

    /**
     * Remove the given value from the node of the given key (do not remove the value from other nodes in the Trie)
     * @param key
     * @param val
     * @return the value which was deleted. If the key did not contain the given value, return null.
     */
    public Value delete(String key, Value val) {
    	Node<Value> node = this.get(this.root, key,0);
    	if (node == null || node.values.isEmpty()) return null; // did not contain val
    	// if the key isn't in the table or it is but has no vals
    	if (node.values.remove(val)) { // if val was in this key, we remove it
    		if (node.values.isEmpty()) cleanIfLeaf(node,key); 
    		      // if we just made this node valueless, 
    		      // we must clean if it's a leaf
    		return val;
    	}
    	return null; // if the val wasn't there, remove returns false, so we fall here and return null
    }
    
    /*
     * to be used with a node that has no values
     * if it's not a leaf, returns false
     * if it is a leaf, cuts off the 
     */
    private void cleanIfLeaf(Node<Value> node, String key) {
    	if (node == root) return; // we can't clean away the root!
    	for (Node<Value> child: node.children) {
    		if (child != null) return; // it's not a leaf, return false, no recursive calls anymore
    	}
    	// if we made it this far, it's a leaf, so go up tree deleting leaves 
    	String parentKey = key.substring(0, key.length()-1);
    	Node<Value> parentNode = this.get(root, parentKey, 0);
	    parentNode.children[key.charAt(key.length()-1)] = null; // delete this node from parent's children
	    if (parentNode.values.isEmpty()) cleanIfLeaf(parentNode, parentKey); 
	    								// recursive call on parent, it's possible we 
	    								// just made the parent into a leaf
    }
}