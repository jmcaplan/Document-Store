package edu.yu.cs.com1320.project.stage4.impl;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import edu.yu.cs.com1320.project.stage4.*;
import edu.yu.cs.com1320.project.stage4.Document;
import edu.yu.cs.com1320.project.stage4.DocumentStore;
import edu.yu.cs.com1320.project.stage4.DocumentStore.DocumentFormat;
import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.impl.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class DocumentStoreImpl implements DocumentStore {
	private HashTableImpl<URI,DocumentImpl> hashTable;
	private DocumentImpl lastDeletedDocument = null;
	private StackImpl<Undoable> commandStack;
	private TrieImpl<DocumentImpl> trie;
	private MinHeapImpl<DocumentImpl> minHeap;
	private int maxDocumentCount;
	private int maxDocumentBytes;
	private int nDocuments;
	private int memoryUsage;
	
	public DocumentStoreImpl() {
		this.hashTable = new HashTableImpl<URI,DocumentImpl>();
		this.commandStack = new StackImpl<Undoable>();
		this.trie = new TrieImpl<>();
		this.minHeap = new MinHeapImpl<>();
		this.maxDocumentCount = Integer.MAX_VALUE;
		this.maxDocumentBytes = Integer.MAX_VALUE;
	}
	
	/*
	 * 
	 */
	private void clearMemoryIfNecessary(DocumentImpl previousDoc, DocumentImpl newDoc) {
		if (previousDoc == null) { // a new put
			clearMemoryIfNecessary(newDoc.getMemoryUsage(), false);
		}
		else { // an overwrite put
			// previousDoc.setLastUseTime(Long.MIN_VALUE);
			// minHeap.removeMin();
			clearMemoryIfNecessary(newDoc.getMemoryUsage() - previousDoc.getMemoryUsage(), true);
		}
	}
	
	private void clearMemoryIfNecessary(int memoryToAdd, boolean isOverWrite) {
		if (isOverWrite) { // the number of docs is constant, only check the memory usage
			while (this.memoryUsage + memoryToAdd > this.maxDocumentBytes) {
				eraseTopDoc();
			}
		}
		else {
			while (this.nDocuments + 1 > this.maxDocumentCount) {
				eraseTopDoc();
			}
			while (this.memoryUsage + memoryToAdd > this.maxDocumentBytes) {
				eraseTopDoc();
			}
		}
	}
	
	private void eraseTopDoc() {
		DocumentImpl topDoc = minHeap.removeMin(); // deletes from minHeap (identifying the proper doc in the process)
		deleteDocumentFromTrie( topDoc); // deletes from trie
		hashTable.put(topDoc.getKey(),  null); // deletes from hashTable
		this.nDocuments--;                            // updates memory usage state
		this.memoryUsage -= topDoc.getMemoryUsage();
		eraseFromCommandStack(topDoc.getKey());
	}
	
	private void eraseFromCommandStack(URI uri) {
		StackImpl<Undoable> helperStack = new StackImpl<Undoable>(); // this represents a reversed version of the final, cleaned stack
    	while (commandStack.size() != 0) { // we will loop through commandStack until it's empty, checking for matches
    		Undoable top = commandStack.pop(); // we pop the top of the command stack, and we'll only add it to the helperStack if 
    									       // it doesn't match the uri
    		if (top instanceof GenericCommand) { // if it's a single command on top, our life is easier
    			if (( (GenericCommand)top ).getTarget() != uri) { // check that this isn't a match, then add to helperStack
        			helperStack.push(top);
        		} 
    		}
    		if (top instanceof CommandSet) {
    			CommandSet topCasted = (CommandSet<URI>)top;
    			Iterator<GenericCommand<URI>> it = topCasted.iterator(); 
    			while (it.hasNext()) {
    				GenericCommand<URI> gc = it.next();
    				if (gc.getTarget() == uri) it.remove();
    			}
    			if (!topCasted.isEmpty()) helperStack.push(topCasted); 
    				// if we haven't emptied the commandSet, it should be pushed onto helperStack
    		}
    	} // popping commands off the commandStack onto helper, peeking each time 
    	  // to see if the next one matches the desired URI
    	// finally put everything back into the commandStack from the helperStack
    	while (helperStack.size() != 0) commandStack.push(helperStack.pop());
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
    		if (deleteDocument(uri)) { // if delete returns true, there was such a key to delete, 
    			                       // if it's false there was no such key in the hashTable
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
			commandStack.push( new GenericCommand<URI>(uri,specificURI -> {return true;}) ); // moot undo
			this.updateLastUsedTime( (DocumentImpl) getDocument(uri) );
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
    		commandStack.push( new GenericCommand<URI>(uri, 
	    			specificURI -> {
	    				putDocument(previousDocumentInput,specificURI,DocumentFormat.TXT);
	    				commandStack.pop(); // this removes the command that was indirectly added by calling putDocument()
	    				return true;}));
    		// b/c of createDocument, the previous doc is no longer in the hashTable, but we still have to 
    		// manually remove it from the trie and the minHeap and the memoryUsage state
    		deleteDocumentFromTrie(previousDocument);
    		deleteDocumentFromMinHeap(previousDocument);
    		this.nDocuments--;
    		this.memoryUsage -= previousDocument.getMemoryUsage();
    		return (previousDocumentHashCode);
    	}
    	catch (NullPointerException e) {
    		commandStack.push( new GenericCommand<URI>(uri, specificURI -> {
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
     * creates the document with the arguments provided and adds it to the trie and hashTable and minHeap
     * @param format
     * @param uri
     * @param documentAsString
     * @param newDocumentHashCode
     * @param documentAsByteArray
     * @return the previous doc with that key
     */
	private DocumentImpl createDocument(DocumentFormat format, URI uri, String documentAsString, int newDocumentHashCode, byte[] documentAsByteArray) {
		if (format == DocumentFormat.PDF) {
        	DocumentImpl newDocument = new DocumentImpl(uri, documentAsString, newDocumentHashCode, documentAsByteArray);
        	clearMemoryIfNecessary(hashTable.get(uri),newDocument);
        	for (String word: newDocument.hashMap.keySet()) {
        		trie.put(word, newDocument);
        	}
    		minHeap.insert(newDocument);
    		this.nDocuments++;
    		this.memoryUsage += newDocument.getMemoryUsage();
        	return hashTable.put(uri, newDocument);
    	}
    	else { // if it's a TXT
        	DocumentImpl newDocument = new DocumentImpl(uri, documentAsString, newDocumentHashCode);
    		clearMemoryIfNecessary(hashTable.get(uri),newDocument);
        	for (String word: newDocument.hashMap.keySet()) {
        		trie.put(word, newDocument);
        	}
    		minHeap.insert(newDocument);
    		this.nDocuments++;
    		this.memoryUsage += newDocument.getMemoryUsage();
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
    		byte[] result = document.getDocumentAsPdf();
    		updateLastUsedTime(document);
    		return result;
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
    		String result = document.getDocumentAsTxt();
    		updateLastUsedTime(document);
    		return result;
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
    		commandStack.push( new GenericCommand<URI>(uri, 
	    			specificURI -> {
	    				putDocument(input,specificURI,DocumentFormat.TXT);
	    				commandStack.pop(); // this removes the command that was indirectly added by calling putDocument()
	    				return true;}));
    		deleteDocumentFromTrie( (DocumentImpl) getDocument(uri)); // deletes from trie
    		deleteDocumentFromMinHeap((DocumentImpl) getDocument(uri)); // deletes from minHeap
    		DocumentImpl deletedDocument = hashTable.put(uri,  null); // deletes from hashTable
    		this.lastDeletedDocument = deletedDocument;
    		this.nDocuments--;
    		this.memoryUsage -= lastDeletedDocument.getMemoryUsage();
    		return true;
    	}
    	catch (IllegalStateException | NullPointerException e) { // will be ISE or NPE if it's not in the hashTable
			commandStack.push( new GenericCommand<URI>(uri,specificURI -> {return true;}) ); 
			// in case they undo this moot deletion, we buffer
			// the commandStack with an equally meaningless Function :)
    		return false;
    	}
    }
    
    private void deleteDocumentFromMinHeap(DocumentImpl doc) {
    	doc.setLastUseTime(Long.MIN_VALUE);
    	minHeap.reHeapify(doc);
    	minHeap.removeMin();
    }
    
    private void deleteDocumentFromTrie(DocumentImpl doc) {
    	for (String word: doc.hashMap.keySet()) {
    		trie.delete(word, doc);
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
    	StackImpl<Undoable> helperStack = new StackImpl<Undoable>();
    	while (commandStack.size() != 0) { // we will loop through commandStack until it's empty, checking for matches
    		Undoable top = commandStack.peek();
    		if (top instanceof GenericCommand) { // if it's a single command on top, our life is easier
    			if (( (GenericCommand)top ).getTarget() == uri) { // check for a match on this top 
        			this.undo(); // if we find a match, we perform stam undo as usual, since it's only 1 command
        			while (helperStack.size() != 0) {
        				commandStack.push(helperStack.pop());
        			} // and then pop everything off helper back onto commandStack
        			return; // and finally return out of the method
        		} 
    		}
    		if (top instanceof CommandSet) {
    			CommandSet topCasted = (CommandSet)top;
    			if (topCasted.undo(uri)) { // it first checks if uri is in the set, if it is, i.e. match, it undoes and returns true
    				if (topCasted.isEmpty()) commandStack.pop();
    				        // the previous step may have emptied the commandset, 
    				        // in which case it should be removed from stack
    				while (helperStack.size() != 0) {
        				commandStack.push(helperStack.pop());
        			} // and then pop everything off helper back onto commandStack
        			return; // and finally return out of the method
    			}
    		}
    		helperStack.push(commandStack.pop()); // if it wasn't a match, pop off commandStack onto helper, making a new top of commandStack
    	} // popping commands off the commandStack onto helper, peeking each time 
    	  // to see if the next one matches the desired URI
    	throw new IllegalStateException("Cannot perform undo: No actions to be undone -- no Document with given URI exists in this DocumentStore");
    }
	

    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////// NEW STAGE3 METHODS //////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////

    
    
    /**
     * Retrieve all documents whose text contains the given keyword.
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE INSENSITIVE.
     * @param keyword
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<String> search(String keyword) {
    	DocumentSorter docSorter = new DocumentSorter(keyword);
    	List<DocumentImpl> docList = trie.getAllSorted(keyword, docSorter);
    	List<String> list = new ArrayList<>();
    	long time = System.nanoTime();
    	for (DocumentImpl doc: docList) {
    		list.add(doc.getDocumentAsTxt());
    		updateLastUsedTime(doc,time);
    	}
    	return list;
    }

    /**
     * same logic as search, but returns the docs as PDFs instead of as Strings
     */
    public List<byte[]> searchPDFs(String keyword) {
    	DocumentSorter docSorter = new DocumentSorter(keyword);
    	List<DocumentImpl> docList = trie.getAllSorted(keyword, docSorter);
    	List<byte[]> list = new ArrayList<>();
    	long time = System.nanoTime();
    	for (DocumentImpl doc: docList) {
    		list.add(doc.getDocumentAsPdf());
    		updateLastUsedTime(doc,time);
    	}
    	return list; 
    }

    /**
     * Retrieve all documents whose text starts with the given prefix
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE INSENSITIVE.
     * @param keywordPrefix
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<String> searchByPrefix(String keywordPrefix) {
    	DocumentSorterWithPrefix docSorterWithPrefix = new DocumentSorterWithPrefix(keywordPrefix);
    	List<DocumentImpl> docList = trie.getAllWithPrefixSorted(keywordPrefix, docSorterWithPrefix);
    	List<String> list = new ArrayList<>();
    	long time = System.nanoTime();
    	for (DocumentImpl doc: docList) {
    		list.add(doc.getDocumentAsTxt());
    		updateLastUsedTime(doc,time);
    	}
    	return list;
    }

    /**
     * same logic as searchByPrefix, but returns the docs as PDFs instead of as Strings
     */
    public List<byte[]> searchPDFsByPrefix(String keywordPrefix) {
    	DocumentSorterWithPrefix docSorterWithPrefix = new DocumentSorterWithPrefix(keywordPrefix);
    	List<DocumentImpl> docList = trie.getAllWithPrefixSorted(keywordPrefix, docSorterWithPrefix);
    	List<byte[]> list = new ArrayList<>();
    	long time = System.nanoTime();
    	for (DocumentImpl doc: docList) {
    		list.add(doc.getDocumentAsPdf());
    		updateLastUsedTime(doc,time);
    	}
    	return list;
    }

    /**
     * Completely remove any trace of any document which contains the given keyword
     * @param keyword
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAll(String keyword) {
    	Set<URI> deletedURIs = new HashSet<>();
    	//List<DocumentImpl> listToDelete = trie.getAllSorted(keyword, new DocumentSorter(keyword));
    	Set<DocumentImpl> setToDelete = trie.deleteAll(keyword);
    	CommandSet<URI> commandSet = new CommandSet<>();
    	for (DocumentImpl doc: setToDelete) {
    		URI uri = doc.getKey();
    		deletedURIs.add(uri); // first we make sure the doc to be deleted is added to the set we'll return

    		// we set up the undo logic, which is a put command on this doc, 
    		InputStream input = this.getDocumentAsInputStream(doc);
    		commandSet.addCommand( new GenericCommand<URI>(uri, 
	    			specificURI -> {
	    				putDocument(input,specificURI,DocumentFormat.TXT);
	    				commandStack.pop(); // this removes the command that was indirectly added by calling putDocument()
	    				return true;})
    				);
    		
    		deleteDocument(uri); // now we finally delete the doc from the store
    		commandStack.pop(); // removing the command added by this delete, we want to ultimately add a commandset not a singleton
    	}
    	commandStack.push(commandSet);
    	return deletedURIs;
    }

    /**
     * Completely remove any trace of any document which contains a word that has the given prefix
     * Search is CASE INSENSITIVE.
     * @param keywordPrefix
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
    	Set<URI> deletedURIs = new HashSet<>();
    	//List<DocumentImpl> listToDelete = trie.getAllWithPrefixSorted(keywordPrefix, new DocumentSorterWithPrefix(keywordPrefix));
    	Set<DocumentImpl> setToDelete = trie.deleteAllWithPrefix(keywordPrefix);
    	System.out.println(setToDelete);
    	CommandSet<URI> commandSet = new CommandSet<>();
    	for (DocumentImpl doc: setToDelete) {
    		URI uri = doc.getKey();
    		deletedURIs.add(uri); // first we make sure the doc to be deleted is added to the set we'll return

    		// we set up the undo logic, which is a put command on this doc, 
    		InputStream input = this.getDocumentAsInputStream(doc);
    		commandSet.addCommand( new GenericCommand<URI>(uri, 
	    			specificURI -> {
	    				putDocument(input,specificURI,DocumentFormat.TXT);
	    				commandStack.pop(); // this removes the command that was indirectly added by calling putDocument()
	    				return true;})
    				);
    		
    		deleteDocument(uri); // now we finally totally erase the doc from the store
    		commandStack.pop(); // removing the command added by this delete, we want to ultimately add a commandset not a singleton
    	}
    	commandStack.push(commandSet);
    	return deletedURIs;
    }
    
    /**
     * set maximum number of documents that may be stored
     * @param limit
     */
    public void setMaxDocumentCount(int limit) {
    	rejectNonPositive(limit);
    	this.maxDocumentCount = limit;
    	while (this.nDocuments > this.maxDocumentCount) eraseTopDoc();
    }

    /**
     * set maximum number of bytes of memory that may be used by all the compressed documents in memory combined
     * @param limit
     */
    public void setMaxDocumentBytes(int limit) {
    	rejectNonPositive(limit);
    	this.maxDocumentBytes = limit;
    	while (this.memoryUsage > this.maxDocumentBytes) eraseTopDoc();
    }
    
    private void rejectNonPositive(int limit) {
    	if (limit < 1) throw new IllegalArgumentException("Cannot set limit less than 1");
    }
    
    private void updateLastUsedTime(DocumentImpl doc) {
    	doc.setLastUseTime(System.nanoTime());
    	minHeap.reHeapify(doc);
    }
    
    private void updateLastUsedTime(DocumentImpl doc, long timeInMilliseconds) {
    	doc.setLastUseTime(timeInMilliseconds);
    	minHeap.reHeapify(doc);
    }  
    
   
}