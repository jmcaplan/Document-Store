package edu.yu.cs.com1320.project.stage3.impl;

import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.lang.Character;
import edu.yu.cs.com1320.project.stage3.*;
import edu.yu.cs.com1320.project.stage3.Document;

import org.apache.pdfbox.pdmodel.*;
//import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;


public class DocumentImpl implements Document {

	private int textHashCode;
	private URI uri;
	private String documentAsTxt;
	private byte[] documentAsPDF;
	protected HashMap<String,Integer> hashMap;
	
	public DocumentImpl(URI uri, String documentAsTxt, int textHashCode) {
		rejectNulls(uri,documentAsTxt);
		this.textHashCode = textHashCode;
		this.uri = uri;
		this.documentAsTxt = documentAsTxt.trim().replace("\n", " ");
		this.hashMap = new HashMap<>();
		createHashMap(this.documentAsTxt);
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
	
	public DocumentImpl(URI uri, String documentAsTxt, int textHashCode, byte[] documentAsPDF) {
		this(uri, documentAsTxt, textHashCode);
		this.documentAsPDF = documentAsPDF;
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
    	if (documentAsPDF != null) {
    		return documentAsPDF;
    	} // if this instance of Document was originally constructed with byte[], or if getDocumentAsPDF has been called previously,
    	  // we have assigned value to field documentAsPDF, so we just return that
    	else {
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
    
    /**
     * how many times does the given word appear in the document?
     * @param word
     * @return the number of times the given words appears in the document
     */
    public int wordCount(String word) {
    	return hashMap.getOrDefault(makeAlphaNumeric(word.toLowerCase()),0);
    }
    
    
}