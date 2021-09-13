/*
 * houston we have a problem!
 * my undo logic is calling put or delete, which means that a new 
 * command is being indirectly added to the stack! If so, when 
 * the user calls undo, the stack's size remains the same, and 
 * a subsequent undo will essentially redo the previous undo!
 * see question @183.  The thing is, maybe that's what we want,
 * so you can undo an undo, and you can only undo deeper into
 * history if you use undo(URI)? If not, and I have to change
 * the code, that stinks, b/c i'll have to re-implement adding
 * and deleting things from the hashTable, all within the undo
 * logic! Shoot!
 * actually, a hack to consider is to overload put and deleteDocument 
 * with another method that is basically my old put and delete b4
 * i put in the command stack business! then call that overloaded one
 * from my undo logic instead of the current one! overload with some
 * silly string or integer or something!
 */
 
package edu.yu.cs.com1320.project.stage2.impl;

import java.io.*;
import java.net.URI;
import edu.yu.cs.com1320.project.stage2.*;
import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.impl.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class DocumentStoreImpl implements DocumentStore {
	private HashTableImpl<URI,DocumentImpl> hashTable;
	private DocumentImpl lastDeletedDocument = null;
	private StackImpl<Command> commandStack;
	
	public DocumentStoreImpl() {
		this.hashTable = new HashTableImpl<URI,DocumentImpl>();
		this.commandStack = new StackImpl<Command>();
	}
	
    /**
     * @param input the document being put
     * @param uri unique identifier for the document
     * @param format indicates which type of document format is being passed
     * @return if there is no previous doc at the given URI, return 0. 
     * If there is a previous doc, return the hashCode of the 
     * String version of the previous doc. If InputStream is null, this
     *  is a delete, and thus return either the hashCode of the deleted doc
     *   or 0 if there is no doc to delete.
     */
    public int putDocument(InputStream input, URI uri, DocumentFormat format) {
    	if (uri == null) {
    		throw new IllegalArgumentException("Cannot create document, URI was null");
    	}
    	if (input == null) {
    		if (deleteDocument(uri)) { // if delete returns true, there was such a key to delete, if it's false there was no such key in the hashTable
    			return (this.lastDeletedDocument.getDocumentAsTxt().hashCode());
    		} // delete if null doc, returning hashCode of deleted doc
    		else {
    			return 0;
    		} // return 0 if no such URI in the hashTable, i.e. if delete returned false
    	}  
    	byte[] documentAsByteArray = readIntoByteArray(input);
    	String documentAsString = documentToString(documentAsByteArray, format);
    	int newDocumentHashCode = (documentAsString.hashCode());
    	if ( (hashTable.get(uri) != null) && (hashTable.get(uri).getDocumentTextHashCode() == newDocumentHashCode) ) {
			commandStack.push( new Command(uri,specificURI -> {return true;}) ); 
    		return newDocumentHashCode;
    	} // if identical doc is already in the table, just return its hashcode, adding a moot undo to commandStack
    	DocumentImpl previousDocument;
    	try {
    		previousDocument = createDocument(format, uri,documentAsString,newDocumentHashCode,documentAsByteArray);
    		int previousDocumentHashCode = previousDocument.getDocumentTextHashCode(); 
    		// this will return a normal integer if there was a previous doc, so we 
    		// continue with undo logic to put back the previous doc with this URI.
    		// if there was no previous doc with this URI, NPE is thrown and we move there...
    		InputStream previousDocumentInput = getDocumentAsInputStream(previousDocument);
    		commandStack.push( new Command(uri, 
	    			specificURI -> {
	    				putDocument(previousDocumentInput,specificURI,DocumentFormat.TXT);
	    				commandStack.pop(); // this removes the command that was indirectly added by calling putDocument()
	    				return true;}));
    		return (previousDocumentHashCode);
    	}
    	catch (NullPointerException e) {
    		commandStack.push( new Command(uri, specificURI -> {
    			deleteDocument(specificURI);
				commandStack.pop(); // this removes the command that was indirectly added by calling deleteDocument()
    			return true;}) );
    		return 0;
    	} // if no previous doc, getDoc... will throw NPE, so catch 
    	  // that, undo logic is to just delete this doc, and finally return 0
    }
    
    private InputStream getDocumentAsInputStream(URI uri) {
    	return new ByteArrayInputStream(this.getDocumentAsTxt(uri).getBytes());
    }
    
    private InputStream getDocumentAsInputStream(Document document) {
    	return new ByteArrayInputStream(document.getDocumentAsTxt().getBytes());
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
    
    public String documentToString(byte[] documentAsByteArray, DocumentFormat format) {
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
     * @return the Document object stored at that URI, 
     * or null if there is no such Document  
     */ 
    protected Document getDocument(URI uri){ 
    	return hashTable.get(uri);
    } 

    /**
     * @param uri the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    public boolean deleteDocument(URI uri) {
    	try {
    		// first we make sure to get an inputStream to be used for re-putting back if undo is called
    		InputStream input = this.getDocumentAsInputStream(uri);
    		commandStack.push( new Command(uri, 
	    			specificURI -> {
	    				putDocument(input,specificURI,DocumentFormat.TXT);
	    				commandStack.pop(); // this removes the command that was indirectly added by calling putDocument()
	    				return true;}));
    		DocumentImpl deletedDocument = hashTable.put(uri,  null);
    		this.lastDeletedDocument = deletedDocument;
    		return true;
    	}
    	catch (IllegalStateException | NullPointerException e) { // will be ISE or NPE if it's not in the hashTable
			commandStack.push( new Command(uri,specificURI -> {return true;}) ); 
			// in case they undo this moot deletion, we buffer
			// the commandStack with an equally meaningless function :)
    		return false;
    	}
    }
    
    /**
     * undo the last put or delete command
     * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
     */
    public void undo() throws IllegalStateException {
    	if (commandStack.size() == 0) {
    		throw new IllegalStateException("Cannot perform undo: No actions to be undone -- command stack is empty");
    	}
    	commandStack.pop().undo(); // we remove the command on top of the stack, making sure to call undo on it as well
    }

    /**
     * undo the last put or delete that was done with the given URI as its key
     * @param uri
     * @throws IllegalStateException if there are no actions on the command stack for the given URI
     */
    public void undo(URI uri) throws IllegalStateException {
    	/*
    	 * My plan here is to create a helper stack, and 
    	 * peek on the commandStack and pop to helper until
    	 * we find the command with that URI, call the 
    	 * normal undo(), and push everything from helper
    	 * back onto the commandStack!
    	 */
    	StackImpl<Command> helperStack = new StackImpl<>();
    	while (commandStack.size() != 0) { // we will loop through commandStack until it's empty
    		if (commandStack.peek().getUri() == uri) { // check for a match
    			this.undo(); // when we find a match, we perform stam undo as usual...
    			while (helperStack.size() != 0) {
    				commandStack.push(helperStack.pop());
    			} // and then pop everything off helper back onto commandStack
    			return;
    		} 
    		helperStack.push(commandStack.pop()); // if it wasn't a match, pop off commandStack onto helper
    	} // popping commands off the commandStack onto helper, peeking each time 
    	  // to see if the next one matches the desired URI
    	throw new IllegalStateException("Cannot perform undo: No actions to be undone -- no Document with given URI exists in this DocumentStore");
    }

}

