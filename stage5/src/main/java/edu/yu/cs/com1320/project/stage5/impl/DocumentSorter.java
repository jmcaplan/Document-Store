package edu.yu.cs.com1320.project.stage5.impl;

import java.net.URI;
import java.util.Comparator;

import edu.yu.cs.com1320.project.impl.BTreeImpl;

class DocumentSorter implements Comparator<URI>  {
	private String word;
	private BTreeImpl<URI,DocumentImpl> btree;
	
	public DocumentSorter(String word, BTreeImpl<URI,DocumentImpl> btree) {
		this.word = word.toLowerCase();
		this.btree = btree;
	}
	
	@Override
	public int compare(URI uri1, URI uri2) {
		Integer doc2wordCount = btree.get(uri2).wordCount(word);
		Integer doc1wordCount = btree.get(uri1).wordCount(word);
		return  doc2wordCount.compareTo(doc1wordCount);
	}
}