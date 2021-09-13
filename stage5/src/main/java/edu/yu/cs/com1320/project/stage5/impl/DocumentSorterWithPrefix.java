package edu.yu.cs.com1320.project.stage5.impl;

import java.net.URI;
import java.util.*;

import edu.yu.cs.com1320.project.impl.BTreeImpl;

class DocumentSorterWithPrefix implements Comparator<URI>  {
	private String prefix;
	private BTreeImpl<URI,DocumentImpl> btree;
	
	public DocumentSorterWithPrefix(String prefix, BTreeImpl<URI,DocumentImpl> btree) {
		this.prefix = prefix.toLowerCase();
		this.btree = btree;
	}
	
	@Override
	public int compare(URI uri1, URI uri2) {
		Integer doc1total = getTotal(btree.get(uri1));
		Integer doc2total = getTotal(btree.get(uri2));
		return  doc2total.compareTo(doc1total);
	}
	
	private int getTotal(DocumentImpl doc) {
		int total = 0;
		Set<String> words = doc.hashMap.keySet();
		for (String word: words) {
			if (word.startsWith(prefix)) {
				total = total + doc.wordCount(word);
			}
		}
		return total;
	}
}