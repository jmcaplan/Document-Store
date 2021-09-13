package edu.yu.cs.com1320.project.stage3.impl;

import java.util.*;

class DocumentSorterWithPrefix implements Comparator<DocumentImpl>  {
	private String prefix;
	
	public DocumentSorterWithPrefix(String prefix) {
		this.prefix = prefix.toLowerCase();
	}
	
	@Override
	public int compare(DocumentImpl doc1, DocumentImpl doc2) {
		Integer doc1total = getTotal(doc1);
		Integer doc2total = getTotal(doc2);
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