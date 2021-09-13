package edu.yu.cs.com1320.project.stage1.impl;

import java.io.*;
import java.net.URI;
import edu.yu.cs.com1320.project.stage1.*;
import edu.yu.cs.com1320.project.impl.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class DocumentStoreImpl implements DocumentStore {
	private HashTableImpl<URI,DocumentImpl> hashTable;
	private DocumentImpl lastDeletedDocument = null;
	
	public DocumentStoreImpl() {
		this.hashTable = new HashTableImpl<URI,DocumentImpl>();
	}
	
    /**
     * @param input the document being put
     * @param uri unique identifier for the document
     * @param format indicates which type of document format is being passed
     * @return the hashcode of the String version of the document
     */
    public int putDocument(InputStream input, URI uri, DocumentFormat format) {
    	if (uri == null) {
    		throw new IllegalArgumentException("Cannot create document, URI was null");
    	}
    	if (input == null) {
    		if (deleteDocument(uri)) { // if delete returns true, there was such a key to delete, if it's false there was no such key in the hashtable
    	    	return (this.lastDeletedDocument.getDocumentAsTxt().hashCode() & 0x7fffffff);
    		} // delete if null doc, returning hashCode of deleted doc
    		else {
    			return 0;
    		} // return 0 if no such URI in the hashTable
    	}  
    	byte[] documentAsByteArray = readIntoByteArray(input);
    	String documentAsString = documentToString(documentAsByteArray, format);
    	int newDocumentHashCode = (documentAsString.hashCode() & 0x7fffffff);
    	if ( (hashTable.get(uri) != null) && (hashTable.get(uri).getDocumentTextHashCode() == newDocumentHashCode) ) {
    		return newDocumentHashCode;
    	} // if identical doc is already in the table, just return its hashcode before adding
    	DocumentImpl previousDocument;
    	try {
    		previousDocument = createDocument(format, uri,documentAsString,newDocumentHashCode,documentAsByteArray);
    		return (previousDocument.getDocumentTextHashCode() & 0x7fffffff);

    	}
    	catch (NullPointerException e) {
    		return 0;
    	}
    	
    	/*
    	if ( (previousDocument = createDocument(format, uri, documentAsString, newDocumentHashCode, documentAsByteArray) ) != null) {
    		return (previousDocument.getDocumentTextHashCode() & 0x7fffffff);
    	} // if there was a previous doc at this key, createDocument will return that doc, and we return its hashcode
    	return 0; // we fall here if createDocument returned null, meaning there was no previousDocument, so we return 0
    	*/
    }
    
    private byte[] readIntoByteArray(InputStream input) {
	   	ByteArrayOutputStream output = new ByteArrayOutputStream();
	   	try {
	   		int nextByte = input.read();
		   	while (nextByte != -1) {
		   		output.write(nextByte);
		   		nextByte = input.read();
		   	}
	   	}
	   	catch (IOException e) {
	   		e.printStackTrace();
	   	}
	   	return output.toByteArray();
	}    
    
    private String documentToString(byte[] documentAsByteArray, DocumentFormat format) {
    	String documentAsString;
    	if (format == DocumentFormat.PDF) {
    		documentAsString = pdfToText(documentAsByteArray);
    	} // If the document format is PDF, extract the text of the PDF
    	  // and hold onto it as a String
    	else if (format == DocumentFormat.TXT) {
    		documentAsString = new String(documentAsByteArray);
    	} // If the document format is TXT, just hold on to it as a String
    	  // – no need to create a PDF at this point
    	else {
    		throw new IllegalArgumentException("Cannot put document, must be PDF or TXT only");
    	}
    	return documentAsString.trim().replace("\n", " ");
    }
    
    private String pdfToText(byte[] input) {
    	String output = "";
    	try {
    		PDDocument document = PDDocument.load(input);
        	PDFTextStripper pdfStripper = new PDFTextStripper();
        	for (int page = 1; page <= document.getNumberOfPages(); ++page) {
        		//pdfStripper.setStartPage(page);
        		//pdfStripper.setEndPage(page);
        		output += pdfStripper.getText(document);
        	}
        	document.close();
    	}
        catch (IOException e) {
        	e.printStackTrace();
        	System.err.println("ERROR: Likely entered txt document with PDF format argument");
        }
    	return output;
    }
    
    /*
     * @param format
     * @param uri
     * @param documentAsString
     * @param newDocumentHashCode
     * @param documentAsByteArray
     * creates the document with the arguments provided and adds it to the hashTable
     * @return the previous doc with that key
     */
	private DocumentImpl createDocument(DocumentFormat format, URI uri, String documentAsString, int newDocumentHashCode, byte[] documentAsByteArray) {
		if (format == DocumentFormat.PDF) {
        	DocumentImpl newDocument = new DocumentImpl(uri, documentAsString, newDocumentHashCode, documentAsByteArray);
        	return hashTable.put(uri, newDocument);
    	}
    	else { // if it's a TXT
        	DocumentImpl newDocument = new DocumentImpl(uri, documentAsString, newDocumentHashCode);
        	return hashTable.put(uri, newDocument);
    	}
	}


	/**
     * @param uri the unique identifier of the document to get
     * @return the given document as a PDF, or null if no document exists with that URI
     */
    public byte[] getDocumentAsPdf(URI uri) {
    	try {
    		DocumentImpl document = hashTable.get(uri);
    		return document.getDocumentAsPdf();
    	}
    	catch (NullPointerException e) {
    		return null;
    	}
    }

    /**
     * @param uri the unique identifier of the document to get
     * @return the given document as TXT, i.e. a String, or null if no document exists with that URI
     */
    public String getDocumentAsTxt(URI uri) {
    	try {
    		DocumentImpl document = hashTable.get(uri);
    		return document.getDocumentAsTxt();
    	}
    	catch (NullPointerException e) {
    		return null;
    	}
    }

    /**
     * @param uri the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    public boolean deleteDocument(URI uri) {
    	try {
    		DocumentImpl deletedDocument = hashTable.put(uri,  null); // this will only work once I build into the HashTableImpl a delete function
    		this.lastDeletedDocument = deletedDocument;
    		return true;
    	}
    	catch (IllegalStateException e) {
    		return false;
    	}
    }
}
