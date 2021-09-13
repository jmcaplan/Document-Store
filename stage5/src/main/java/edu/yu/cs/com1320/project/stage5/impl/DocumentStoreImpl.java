package edu.yu.cs.com1320.project.stage5.impl;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import edu.yu.cs.com1320.project.stage5.*;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import edu.yu.cs.com1320.project.stage5.DocumentStore.DocumentFormat;
import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.impl.*;
import edu.yu.cs.com1320.project.impl.BTreeImpl.Entry;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class DocumentStoreImpl implements DocumentStore {
	protected CustomBTree<URI,DocumentImpl> btree;
	private DocumentImpl lastDeletedDocument = null;
	private StackImpl<Undoable> commandStack;
	private TrieImpl<URI> trie;
	private CustomMinHeap<DocumentImpl> minHeap;
	private int maxDocumentCount;
	private int maxDocumentBytes;
	
	protected String fellHere; // for testing what is causing delete to return false 
	
	class CustomBTree<Key extends Comparable<Key>, Value> extends BTreeImpl<Key,Value> {
    	@Override
		protected Value peek(Key key) {
        	if (key == null)
            {
                throw new IllegalArgumentException("argument to get() is null");
            }
            Entry entry = this.get(this.root, key, this.height);
            if(entry != null)
            {
            	Value value = (Value)entry.getValue(); // this will be the desired value,
            									// or null if it's currently on disk
            	                                // or if it was deleted
            	return value; 
            	// don't change disk, just return whatever is mapped to by this key currently
            }
            return null; // key was not in the btree
        }
    	protected Value getAndRestructure(Key key) {
    		Value peekValue = this.peek(key);
    		Value getValue = this.get(key);
    		if (getValue == null) return null; // wasn't even on disk, no restructure necessary
    		// if we make it here, it was either on disk or on memory
    		if (peekValue == null) {           // this shows it was on disk
    			minHeap.insert((DocumentImpl)getValue);
    			while (btree.docsInMemory() > maxDocumentCount || btree.bytesInMemory() > maxDocumentBytes) {
    				eraseTopDoc();
    			}
    		}
    		return getValue;
    	}
    	protected int docsInMemory() {
    		return this.getOrderedEntries().size();
    	}
    	protected Set<URI> urisInMemory() {
    		Set<URI> result = new HashSet<>();
    		for (Entry e: this.getOrderedEntries()) {
    			DocumentImpl doc = (DocumentImpl)e.getValue();
    			result.add(doc.getKey());
    		}
    		return result;
    	}
    	protected Set<DocumentImpl> documentsInMemory() {
    		Set<DocumentImpl> result = new HashSet<>();
    		for (Entry e: this.getOrderedEntries()) {
    			DocumentImpl doc = (DocumentImpl)e.getValue();
    			result.add(doc);
    		}
    		return result;
    	}
    	
    	protected int bytesInMemory() {
    		int total = 0;
    		for (Entry e: this.getOrderedEntries()) {
    			DocumentImpl doc = (DocumentImpl)e.getValue();
    			if (doc != null) total += doc.getMemoryUsage();
    		}
    		return total;
    	}
    	
    }
	
	class CustomMinHeap<E extends Comparable> extends MinHeapImpl<E> {
		
	}
	
	public DocumentStoreImpl() {
		this.btree = new CustomBTree<URI,DocumentImpl>();
		this.btree.setPersistenceManager((PersistenceManager)new DocumentPersistenceManager());
		   // setting a default persistence manager, which uses user.dir 
		try {
			this.btree.put(new URI(""), null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} // putting sentinal value into the btree
		this.commandStack = new StackImpl<Undoable>();
		this.trie = new TrieImpl<>();
		this.minHeap = new CustomMinHeap<>();
		this.maxDocumentCount = Integer.MAX_VALUE;
		this.maxDocumentBytes = Integer.MAX_VALUE;
	}
	
	public DocumentStoreImpl(DocumentPersistenceManager pm) {
		this();
		btree.setPersistenceManager((PersistenceManager)pm);
	}
	
	public DocumentStoreImpl(File baseDir) {
		this(new DocumentPersistenceManager(baseDir));
	}
	
	/*
	 * 
	 */
	private void clearMemoryIfNecessary(DocumentImpl previousDoc, DocumentImpl newDoc) {
		if (previousDoc == null) { // a new put
			clearMemoryIfNecessary(newDoc.getMemoryUsage(), false);
		}
		else { // an overwrite put
			clearMemoryIfNecessary(newDoc.getMemoryUsage() - previousDoc.getMemoryUsage(), true);
		}
	}
	
	private void clearMemoryIfNecessary(int memoryToAdd, boolean isOverWrite) {
		if (isOverWrite) { // the number of docs is constant, only check the memory usage
			while (btree.bytesInMemory() + memoryToAdd > this.maxDocumentBytes) {
				eraseTopDoc();
			}
		}
		else {
			while (btree.docsInMemory() + 1 > this.maxDocumentCount) {
				eraseTopDoc();
			}
			while (btree.bytesInMemory() + memoryToAdd > this.maxDocumentBytes) {
				eraseTopDoc();
			}
		}
	}
	
	private void eraseTopDoc() {
		DocumentImpl topDoc = minHeap.removeMin(); // deletes from minHeap (identifying the proper doc in the process)
		//deleteDocumentFromTrie(topDoc); // commenting this caused errors in alreadyOverClearsMemoryTest
										// and putNewClearsBytesTest but none in ProfDocStoreTest
		try {
			btree.moveToDisk(topDoc.getKey());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		//btree.put(topDoc.getKey(),  null); // deletes from btree
		//eraseFromCommandStack(topDoc.getKey());
	}
	
	/*
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
	*/
	
	
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
    		if (deleteDocument(uri)) { // if delete returns true, there was such a key to delete, either disk or memory,
    			                       // if it's false there was no such key in the btree at all
    			return (this.lastDeletedDocument.getDocumentAsTxt().hashCode());
    		} // delete if null input, returning hashCode of deleted doc
    		else {
    			return 0;
    		} // return 0 if no such URI in the btree, i.e. if delete returned false
    	}  
    	byte[] documentAsByteArray = readIntoByteArray(input);
    	String documentAsString = documentToString(documentAsByteArray, format);
    	int newDocumentHashCode = (documentAsString.hashCode());
    	if ( (btree.peek(uri) != null) && (btree.peek(uri).getDocumentTextHashCode() == newDocumentHashCode) ) { // only should be true if on memory lechora
    		commandStack.push( new GenericCommand<URI>(uri,specificURI -> {return true;}) ); // moot undo
			this.updateLastUsedTime( (DocumentImpl) getDocument(uri) );
    		return newDocumentHashCode;
    	} // if identical doc is already in memory, just update its time, return its hashcode, add a moot undo to commandStack
    	
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
    		// b/c of createDocument, the previous doc is no longer in the btree, but we still have to 
    		// manually remove it from the trie and the minHeap
    		deleteDocumentFromTrie(previousDocument);
    		deleteDocumentFromMinHeap(previousDocument);
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
    
    /*
    private InputStream getDocumentAsInputStream(URI uri) {
    	DocumentImpl doc = btree.get(uri); // we want to call get which will retrieve it even from disk, but won't restructure, b/c
    	return new ByteArrayInputStream(doc.getDocumentAsTxt().getBytes()); // could be a problem to call this getDOcTxt b/c it will bring it back to memory
    }
    */
    
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
     * creates the document with the arguments provided and adds it to the trie and btree and minHeap
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
        	clearMemoryIfNecessary(btree.peek(uri),newDocument); // we only have to clear room on memory based on those docs in memory, so we use peek
        	for (String word: newDocument.hashMap.keySet()) {
        		trie.put(word, uri);
        	}
    		minHeap.insert(newDocument);
        	return btree.put(uri, newDocument);
    	}
    	else { // if it's a TXT
        	DocumentImpl newDocument = new DocumentImpl(uri, documentAsString, newDocumentHashCode);
    		clearMemoryIfNecessary(btree.peek(uri),newDocument); // we only have to clear room on memory based on those docs in memory, so we use peek
        	for (String word: newDocument.hashMap.keySet()) {
        		trie.put(word, uri);
        	}
    		minHeap.insert(newDocument);
        	return btree.put(uri, newDocument);
    	}
	}


	/**
     * @param uri the unique identifier of the document to get
     * @return the given document as a PDF, or null if no document exists with that URI
     */
    public byte[] getDocumentAsPdf(URI uri) {
    	/*
    	//old version
    	try {
    		DocumentImpl document = btree.get(uri);
    		byte[] result = document.getDocumentAsPdf();
    		updateLastUsedTime(document);
    		return result;
    	}
    	catch (NullPointerException e) {
    		return null;
    	}
    	*/
    	
    	/*
    	//new version
    	DocumentImpl document = btree.get(uri);
    	if (document == null) return null; // it wasn't even on disk, return
    	minHeap.insert(document); 
    	byte[] result = document.getDocumentAsPdf();
    	updateLastUsedTime(document);
    	return result;
    	*/
    	
    	//newest version
    	DocumentImpl document = btree.getAndRestructure(uri);
    	if (document == null) return null; // it wasn't even on disk, return
    	byte[] result = document.getDocumentAsPdf();
    	updateLastUsedTime(document);
    	return result;
    	
    }

    /**
     * @param uri the unique identifier of the document to get
     * @return the given document as TXT, i.e. a String, or null if no document exists with that URI
     */
    public String getDocumentAsTxt(URI uri) {
    	
    	//old version
    	/*
    	try {
    		DocumentImpl document = btree.get(uri);
    		System.out.println("\n\n\n\ndocument is null: "+(document==null)+"\n\n\n");
    		String result = document.getDocumentAsTxt();
    		System.out.println("\n\n\n\ntext is: "+result+"\n\n\n");
    		updateLastUsedTime(document);
    		return result;
    	}
    	catch (NullPointerException e) {
    		return null;
    	}
    	*/
    	
    	/*
    	//new version
		DocumentImpl document = btree.get(uri);
		if (document == null) return null;
    	minHeap.insert(document);
		String result = document.getDocumentAsTxt();
		updateLastUsedTime(document);
		return result;
		*/
		
		//newest version
    	DocumentImpl document = btree.getAndRestructure(uri);
    	if (document == null) return null; // it wasn't even on disk, return
    	String result = document.getDocumentAsTxt();
    	updateLastUsedTime(document);
    	return result;
    	
    }
    
    
    
    /**
     * @return the Document object stored at that URI, 
     * or null if either there is no such Document 
     * or if the Doc is on Disk.
     * Should not alter the btree, i.e. should not
     * cause anything to be brought from disk to memory
     */ 
    protected Document getDocument(URI uri){ 
    	return btree.peek(uri);
    } 

    /**
     * @param uri the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    public boolean deleteDocument(URI uri) {
		boolean wasOnDisk = btree.peek(uri) == null; // really might also have been totally absent, but documentToDelete == null 
		                                             // checks for that and returns b4 we use this boolean, so it's fine
		DocumentImpl documentToDelete = btree.get(uri); // we use regular get b/c don't want restructuring (logically delete shouldn't kick other things to disk)
									// But it is now in memory, even if it had been on disk. That's fine
		if (documentToDelete == null) {
    		commandStack.push( new GenericCommand<URI>(uri,specificURI -> {return true;}) ); 
			return false; // no doc in memory nor disk has this URI
		}
		InputStream input = this.getDocumentAsInputStream(documentToDelete); // getting the inputstream for future undo
		commandStack.push( new GenericCommand<URI>(uri, 
    			specificURI -> {
    				putDocument(input,specificURI,DocumentFormat.TXT);
    				commandStack.pop(); // this removes the command that was indirectly added by calling putDocument()
    				return true;}));
		deleteDocumentFromTrie(documentToDelete); // deletes from trie
		// since we called regular get, if this uri was on disk, it was not inserted back into the minheap (to avoid restructure)
		// so we only deleteFromMinHeap if it was no on the disk before, i.e. was on memory
		if (!wasOnDisk) deleteDocumentFromMinHeap(documentToDelete); // deletes from minHeap
		DocumentImpl deletedDocument = btree.put(uri,  null); // deletes from btree
		if (deletedDocument == null) {
			this.fellHere = "first false";
			return false;
		}
		this.lastDeletedDocument = deletedDocument;
		return true;
    }
    
    private void deleteDocumentFromMinHeap(DocumentImpl doc) {
    	doc.setLastUseTime(Long.MIN_VALUE);
    	minHeap.reHeapify(doc);
    	minHeap.removeMin();
    }
    
    private void deleteDocumentFromTrie(DocumentImpl doc) {
    	for (String word: doc.hashMap.keySet()) {
    		trie.delete(word, doc.getKey()); // in reality, maybe we should never delete from trie, because it only gets docs from btree anyway!
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

    /*
    // this is the version for once minheap holds uris
    protected void restructure() {
    	Set<URI> urisInMemory = btree.urisInMemory();
    	CustomMinHeap<URI> newMinHeap = new CustomMinHeap<>();
    	for (URI uri: urisInMemory) {
    		newMinHeap.insert(uri);
    	}
    	this.minHeap = newMinHeap;
    	while (btree.docsInMemory() > this.maxDocumentCount 
    			|| btree.bytesInMemory() > this.maxDocumentBytes) {
    		eraseTopDoc();
    	}
    }
    */
    
    protected void restructure() {
    	Set<DocumentImpl> documentsInMemory = btree.documentsInMemory();
    	CustomMinHeap<DocumentImpl> newMinHeap = new CustomMinHeap<>();
    	for (DocumentImpl doc: documentsInMemory) {
    		newMinHeap.insert(doc);
    	}
    	this.minHeap = newMinHeap;
    	while (btree.docsInMemory() > this.maxDocumentCount 
    			|| btree.bytesInMemory() > this.maxDocumentBytes) {
    		eraseTopDoc();
    	}
    }
    
    /**
     * Retrieve all documents whose text contains the given keyword.
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE INSENSITIVE.
     * @param keyword
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<String> search(String keyword) {   // as of now, search does not restructure!!!
    	DocumentSorter docSorter = new DocumentSorter(keyword, btree);
    		// docSorter uses btree.get, so it brings them off disk but does not restructure
    	List<URI> uriList = trie.getAllSorted(keyword, docSorter);
    	List<String> list = new ArrayList<>();
    	long time = System.nanoTime();
    	for (URI uri: uriList) {
    		DocumentImpl doc = btree.get(uri);
    		list.add(doc.getDocumentAsTxt()); // all the docs are on memory anyway, b/c the trie did not restructure
    		doc.setLastUseTime(time);
    		//updateLastUsedTime(doc,time);
    	}
    	restructure();
    	return list;
    }

    /**
     * same logic as search, but returns the docs as PDFs instead of as Strings
     */
    public List<byte[]> searchPDFs(String keyword) { // as of now, search does not restructure!!!
    	DocumentSorter docSorter = new DocumentSorter(keyword, btree); 
    		// docSorter uses btree.get, so it brings them off disk but does not restructure
    	List<URI> uriList = trie.getAllSorted(keyword, docSorter);
    	List<byte[]> list = new ArrayList<>();
    	long time = System.nanoTime();
    	for (URI uri: uriList) {
    		DocumentImpl doc = btree.get(uri);
    		list.add(doc.getDocumentAsPdf());
    		doc.setLastUseTime(time);
    		//updateLastUsedTime(doc,time);
    	}
    	restructure();
    	return list; 
    }

    /**
     * Retrieve all documents whose text starts with the given prefix
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE INSENSITIVE.
     * @param keywordPrefix
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    public List<String> searchByPrefix(String keywordPrefix) {   // as of now, search does not restructure!!!
    	DocumentSorterWithPrefix docSorterWithPrefix = new DocumentSorterWithPrefix(keywordPrefix, btree);
		// docSorter uses btree.get, so it brings them off disk but does not restructure
    	List<URI> uriList = trie.getAllWithPrefixSorted(keywordPrefix, docSorterWithPrefix);
    	List<String> list = new ArrayList<>();
    	long time = System.nanoTime();
    	for (URI uri: uriList) {
    		DocumentImpl doc = btree.get(uri);
    		list.add(doc.getDocumentAsTxt());
    		doc.setLastUseTime(time); 
    		//updateLastUsedTime(doc,time);
    	}
    	restructure();
    	return list;
    }

    /**
     * same logic as searchByPrefix, but returns the docs as PDFs instead of as Strings
     * I was lazy and copied and pasted from searchByPrefix without thinking too much...
     */
    public List<byte[]> searchPDFsByPrefix(String keywordPrefix) { // as of now, search does not restructure!!!
    	DocumentSorterWithPrefix docSorterWithPrefix = new DocumentSorterWithPrefix(keywordPrefix, btree);
		// docSorter uses btree.get, so it brings them off disk but does not restructure
    	List<URI> uriList = trie.getAllWithPrefixSorted(keywordPrefix, docSorterWithPrefix);
    	List<byte[]> list = new ArrayList<>();
    	long time = System.nanoTime();
    	for (URI uri: uriList) {
    		DocumentImpl doc = btree.get(uri);
    		list.add(doc.getDocumentAsPdf());
    		doc.setLastUseTime(time);  
    		//updateLastUsedTime(doc,time);
    	}
    	restructure();
    	return list;
    }

    /**
     * Completely remove any trace of any document which contains the given keyword
     * @param keyword
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAll(String keyword) {
    	Set<URI> deletedURIs = new HashSet<>();
    	Set<URI> setToDelete = trie.deleteAll(keyword);
    	CommandSet<URI> commandSet = new CommandSet<>();
    	for (URI uri: setToDelete) {
    		//URI uri = doc.getKey();
    		DocumentImpl doc = btree.get(uri); // now it's on memory, but no restructuring done, b/c delete doesn't restructure!
    		deletedURIs.add(uri); //we make sure the doc to be deleted is added to the set we'll return

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
    	Set<URI> setToDelete = trie.deleteAllWithPrefix(keywordPrefix);
    	CommandSet<URI> commandSet = new CommandSet<>();
    	for (URI uri: setToDelete) {
    		//URI uri = doc.getKey();
    		DocumentImpl doc = btree.get(uri); // now it's on memory, but no restructuring done, b/c delete doesn't restructure!
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
    	while (btree.docsInMemory() > this.maxDocumentCount) eraseTopDoc();
    }

    /**
     * set maximum number of bytes of memory that may be used by all the compressed documents in memory combined
     * @param limit
     */
    public void setMaxDocumentBytes(int limit) {
    	rejectNonPositive(limit);
    	this.maxDocumentBytes = limit;
    	while (btree.bytesInMemory() > this.maxDocumentBytes) eraseTopDoc();
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