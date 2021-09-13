package edu.yu.cs.com1320.project.stage5.impl;

import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.lang.Character;

import edu.yu.cs.com1320.project.stage5.*;
import edu.yu.cs.com1320.project.stage5.Document;
import java.lang.System;

import org.apache.pdfbox.pdmodel.*;
//import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;


public class DocumentImpl implements Document {

	private int textHashCode;
	private URI uri;
	private String documentAsTxt;
	private byte[] documentAsPDF;
	protected HashMap<String,Integer> hashMap;
	private long lastUsedTime;
	
	public DocumentImpl(URI uri, String documentAsTxt, int textHashCode) {
		construct(uri,documentAsTxt,textHashCode);
		this.documentAsPDF = txtToPdf(documentAsTxt);
	}
	
	/*
	public DocumentImpl(URI uri, String documentAsTxt, int textHashCode, Map<String,Integer> wordMap) {
		rejectNulls(uri,documentAsTxt);
		this.textHashCode = textHashCode;
		this.uri = uri;
		this.documentAsTxt = documentAsTxt.trim().replace("\n", " ");
		this.lastUsedTime = System.nanoTime();
		this.hashMap = (HashMap<String,Integer>)wordMap;
		this.documentAsPDF = txtToPdf(documentAsTxt);
	} */

	public DocumentImpl(URI uri, String documentAsTxt, int textHashCode, byte[] documentAsPDF) {
		construct(uri,documentAsTxt,textHashCode);
		this.documentAsPDF = documentAsPDF;
	}
	
	private void construct(URI uri, String documentAsTxt, int textHashCode) {
		rejectNulls(uri,documentAsTxt);
		this.textHashCode = textHashCode;
		this.uri = uri;
		this.documentAsTxt = documentAsTxt.trim().replace("\n", " ");
		this.hashMap = new HashMap<>();
		createHashMap(this.documentAsTxt);
		this.lastUsedTime = System.nanoTime();
	}
	
	private void createHashMap(String documentAsTxt) {
		String[] tokens = makeAlphaNumeric(documentAsTxt).split(" ");
		int currentValue; // to be used later to check current status
		                  // of each word in the hashMap
		for (String token: tokens) {
			token = token.toLowerCase(); // making it lowercase
			if ( (currentValue = hashMap.getOrDefault(token,0)) != 0) {
				hashMap.put(token,currentValue+1);
			} // the word has been found already in the doc, so
			  // we just increment the value at the word's key
			else {
				hashMap.put(token, 1);
			} // the word hasn't been found in the doc, so we add it 
			  // with 1 as its wordCount
		}
	}
	
	/*
	 * takes a string, returns a new string which only contains
	 * letters, digits, and spaces
	 */
	private String makeAlphaNumeric(String string) {
		String newString = "";
		for (char c: string.toCharArray()) {
			if (Character.isLetterOrDigit(c) || c == ' ') {
				newString += c;
			}
		}
		return newString;
	}
	
	private void rejectNulls(URI uri,String documentAsTxt) {
		if (uri == null || documentAsTxt == null) {
			throw new IllegalArgumentException("Cannot create DocumentImpl with null URI or Text");
		}
	}
	
	/**
     * @return the document as a PDF
     */
    public byte[] getDocumentAsPdf() {
    	return documentAsPDF;
    }
    
    private byte[] txtToPdf(String documentAsTxt) {
    	PDDocument document = new PDDocument();
		PDPage newPage = new PDPage();
		document.addPage(newPage);
		
		try (PDPageContentStream contents = new PDPageContentStream(document, newPage)) {
			contents.setFont(PDType1Font.TIMES_BOLD, 8.0f);
			contents.beginText();
			contents.newLineAtOffset(0, 0);
			contents.showText(documentAsTxt);
			contents.endText();
			contents.close();
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			document.save(byteArrayOutputStream);
			//document.save("Hello_Mom2.pdf");
			document.close(); 
    		return byteArrayOutputStream.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
    }

    /**
     * @return the document as a Plain String
     */
    public String getDocumentAsTxt() {
    	return documentAsTxt;
    }

    /**
     * @return hash code of the plain text version of the document
     */
    public int getDocumentTextHashCode() {
    	return textHashCode;
    }

    /**
     * @return URI which uniquely identifies this document
     */
    public URI getKey() {
    	return uri;
    }
    
    protected int getMemoryUsage() {
    	return this.documentAsTxt.getBytes().length + this.documentAsPDF.length;
    }
    
    /**
     * how many times does the given word appear in the document?
     * @param word
     * @return the number of times the given words appears in the document
     */
    public int wordCount(String word) {
    	return hashMap.getOrDefault(makeAlphaNumeric(word.toLowerCase()),0);
    }
    
    /**
     * return the last time this document was used, via put/get or via a search result
     * (for stage 4 of project)
     */
    public long getLastUseTime() {
    	return this.lastUsedTime;
    }
    
    public void setLastUseTime(long timeInNanoseconds) {
    	this.lastUsedTime = timeInNanoseconds; 
    }
    
    @Override
    public int compareTo(Document other) {
    	Long thisLastUsedTime = (Long) this.lastUsedTime;
    	Long otherLastUsedTime = (Long) other.getLastUseTime();
    	return thisLastUsedTime.compareTo(otherLastUsedTime);
    }
    
    /**
     * @return a copy of the word to count map so it can be serialized
     */
    public Map<String,Integer> getWordMap() {
    	return this.hashMap;
    }

    /**
     * This must set the word to count map during deserialization
     * @param wordMap
     */
    public void setWordMap(Map<String,Integer> wordMap) {
    	this.hashMap = (HashMap<String,Integer>) wordMap;
    }
    
}