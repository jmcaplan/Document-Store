package edu.yu.cs.com1320.project.stage3.impl;

import java.util.Comparator;

class DocumentSorter implements Comparator<DocumentImpl>  {
	private String word;
	
	public DocumentSorter(String word) {
		this.word = word.toLowerCase();
	}
	
	@Override
	public int compare(DocumentImpl doc1, DocumentImpl doc2) {
		Integer doc2wordCount = doc2.wordCount(word);
		return  doc2wordCount.compareTo(doc1.wordCount(word));
	}
}