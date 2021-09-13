package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.stage5.PersistenceManager;
import edu.yu.cs.com1320.project.stage5.impl.*;
import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

public class BTreeImplTest {
	
	
	
	@Test
	public void simplePutAndGetTest() throws URISyntaxException {
		BTreeImpl<URI,DocumentImpl> btree = new BTreeImpl<>();
		btree.setPersistenceManager((PersistenceManager)new DocumentPersistenceManager());
		String str1 = "document number 1";
		URI uri1 = new URI("http://www.yu.edu/Docs/doc1");
		DocumentImpl doc1 = new DocumentImpl(uri1,str1,str1.hashCode());
		btree.put(uri1, doc1);
		assertTrue(btree.get(uri1).getDocumentAsTxt() == str1);
	}
	
	@Test
	public void writeToDiskStillReturnedPublicGetTest() throws Exception {
		BTreeImpl<URI,DocumentImpl> btree = new BTreeImpl<>();
		btree.setPersistenceManager((PersistenceManager)new DocumentPersistenceManager());		
		String str1 = "document number 1";
		URI uri1 = new URI("http://www.yu.edu/Docs/doc1");
		DocumentImpl doc1 = new DocumentImpl(uri1,str1,str1.hashCode());
		btree.put(uri1, doc1);
		btree.moveToDisk(uri1);
		String result = btree.get(uri1).getDocumentAsTxt(); // calling public get to retrieve
		assertTrue(result.equals(str1));
	}
	
	@Test
	public void writeToDiskNotVisibleToPeekTest() throws Exception {
		BTreeImpl<URI,DocumentImpl> btree = new BTreeImpl<>();
		btree.setPersistenceManager((PersistenceManager)new DocumentPersistenceManager());		
		String str1 = "document number 1";
		URI uri1 = new URI("http://www.yu.edu/Docs/doc1");
		DocumentImpl doc1 = new DocumentImpl(uri1,str1,str1.hashCode());
		btree.put(uri1, doc1);
		assertTrue(btree.peek(uri1).getDocumentAsTxt().equals(str1));
		btree.moveToDisk(uri1);
		assertNull(btree.peek(uri1));
	}
	
	@Test
	public void deleteNotRetrievedFromPublicGetTest() throws Exception {
		BTreeImpl<URI,DocumentImpl> btree = new BTreeImpl<>();
		btree.setPersistenceManager((PersistenceManager)new DocumentPersistenceManager());		
		String str1 = "document number 1";
		URI uri1 = new URI("http://www.yu.edu/Docs/doc1");
		DocumentImpl doc1 = new DocumentImpl(uri1,str1,str1.hashCode());
		btree.put(uri1, doc1);
		btree.put(uri1, null);
		assertNull(btree.peek(uri1));
		assertNull(btree.get(uri1));
	}
	
	@Test
	public void deleteReturnsPreviousDocumentInMemoryTest() throws Exception {
		BTreeImpl<URI,DocumentImpl> btree = new BTreeImpl<>();
		btree.setPersistenceManager((PersistenceManager)new DocumentPersistenceManager());		
		String str1 = "document number 1";
		URI uri1 = new URI("http://www.yu.edu/Docs/doc1");
		DocumentImpl doc1 = new DocumentImpl(uri1,str1,str1.hashCode());
		btree.put(uri1, doc1);
		DocumentImpl returnedDoc = btree.put(uri1, null);
		assertTrue(returnedDoc == doc1);
	}
	
	@Test
	public void overwriteReturnsPreviousDocumentInMemoryTest() throws Exception {
		BTreeImpl<URI,DocumentImpl> btree = new BTreeImpl<>();
		btree.setPersistenceManager((PersistenceManager)new DocumentPersistenceManager());		
		String str1 = "document number 1";
		String str2 = "document number 2";
		URI uri1 = new URI("http://www.yu.edu/Docs/doc1");
		DocumentImpl doc1 = new DocumentImpl(uri1,str1,str1.hashCode());
		DocumentImpl doc2 = new DocumentImpl(uri1,str2,str2.hashCode());
		btree.put(uri1, doc1);
		DocumentImpl returnedDoc = btree.put(uri1, doc2);
		assertTrue(returnedDoc == doc1);
		
		assertTrue(btree.get(uri1) == doc2); // making sure the overwrite worked
	}
	
	@Test
	public void deleteReturnsPreviousDocumentFromDiskTest() throws Exception {
		BTreeImpl<URI,DocumentImpl> btree = new BTreeImpl<>();
		btree.setPersistenceManager((PersistenceManager)new DocumentPersistenceManager());		
		String str1 = "document number 1";
		String str2 = "document number 2";
		URI uri1 = new URI("http://www.yu.edu/Docs/doc1");
		DocumentImpl doc1 = new DocumentImpl(uri1,str1,str1.hashCode());
		btree.put(uri1, doc1);
		btree.moveToDisk(uri1);
		assertNull(btree.peek(uri1)); // ensuring it's not in memory
		DocumentImpl returnedDoc = btree.put(uri1, null);
		assertTrue(returnedDoc.getDocumentAsTxt().equals(str1));
	}
	
	@Test
	public void overwriteReturnsPreviousDocumentFromDiskTest() throws Exception {
		BTreeImpl<URI,DocumentImpl> btree = new BTreeImpl<>();
		btree.setPersistenceManager((PersistenceManager)new DocumentPersistenceManager());		
		String str1 = "document number 1";
		String str2 = "document number 2";
		URI uri1 = new URI("http://www.yu.edu/Docs/doc1");
		DocumentImpl doc1 = new DocumentImpl(uri1,str1,str1.hashCode());
		DocumentImpl doc2 = new DocumentImpl(uri1,str2,str2.hashCode());
		btree.put(uri1, doc1);
		btree.moveToDisk(uri1);
		DocumentImpl returnedDoc = btree.put(uri1, doc2);
		assertTrue(returnedDoc.getDocumentAsTxt().equals(str1));
	}
	
	@Test
	public void overwriteThenToDiskStillGottenWithPublicGetTest() throws Exception {
		BTreeImpl<URI,DocumentImpl> btree = new BTreeImpl<>();
		btree.setPersistenceManager((PersistenceManager)new DocumentPersistenceManager());		
		String str1 = "document number 1";
		String str2 = "document number 2";
		URI uri1 = new URI("http://www.yu.edu/Docs/doc1");
		DocumentImpl doc1 = new DocumentImpl(uri1,str1,str1.hashCode());
		DocumentImpl doc2 = new DocumentImpl(uri1,str2,str2.hashCode());
		btree.put(uri1, doc1);
		btree.moveToDisk(uri1);
		btree.put(uri1, doc2);
		btree.moveToDisk(uri1);
		assertTrue(btree.get(uri1).getDocumentAsTxt().equals(str2));
	}
	
	@Test
	public void proofOfConceptTest() throws Exception {
		BTreeImpl<URI,DocumentImpl> btree = new BTreeImpl<>();
		btree.setPersistenceManager((PersistenceManager)new DocumentPersistenceManager());		
		String str1 = "document number 1";
		String str2 = "document number 2";
		String str3 = "document number 3";
		URI uri1 = new URI("http://www.yu.edu/Docs/doc1");
		URI uri2 = new URI("http://www.yu.edu/Docs/doc2");
		URI uri3 = new URI("http://www.yu.edu/Docs/doc3");
		DocumentImpl doc1 = new DocumentImpl(uri1,str1,str1.hashCode());
		DocumentImpl doc2 = new DocumentImpl(uri2,str2,str2.hashCode());
		DocumentImpl doc3 = new DocumentImpl(uri3,str3,str3.hashCode());
		btree.put(uri1, doc1);
		btree.moveToDisk(uri1);
		btree.put(uri2, doc2);
		DocumentImpl returnedDoc = btree.put(uri1, doc2);
		assertTrue(returnedDoc.getDocumentAsTxt().equals(str1));
		assertTrue(btree.peek(uri1) == btree.peek(uri2));
	}
	
	@Test (expected = IllegalStateException.class)
	public void writeToDiskWhenDeletedThrowsExceptionTest() throws Exception {
		BTreeImpl<URI,DocumentImpl> btree = new BTreeImpl<>();
		btree.setPersistenceManager((PersistenceManager)new DocumentPersistenceManager());		
		String str1 = "document number 1";
		URI uri1 = new URI("http://www.yu.edu/Docs/doc1");
		DocumentImpl doc1 = new DocumentImpl(uri1,str1,str1.hashCode());
		btree.put(uri1, doc1);
		btree.put(uri1, null);
		btree.moveToDisk(uri1);
	}
	
	@Test (expected = IllegalStateException.class)
	public void writeToDiskWhenAlreadyOnDiskThrowsExceptionTest() throws Exception {
		BTreeImpl<URI,DocumentImpl> btree = new BTreeImpl<>();
		btree.setPersistenceManager((PersistenceManager)new DocumentPersistenceManager());		
		String str1 = "document number 1";
		URI uri1 = new URI("http://www.yu.edu/Docs/doc1");
		DocumentImpl doc1 = new DocumentImpl(uri1,str1,str1.hashCode());
		btree.put(uri1, doc1);
		btree.moveToDisk(uri1);
		btree.moveToDisk(uri1);
	}

}
