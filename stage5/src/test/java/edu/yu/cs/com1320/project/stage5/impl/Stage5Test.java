package edu.yu.cs.com1320.project.stage5.impl;

import static org.junit.Assert.*;

import edu.yu.cs.com1320.project.Utils;
import edu.yu.cs.com1320.project.impl.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import edu.yu.cs.com1320.project.stage5.DocumentStore;
import edu.yu.cs.com1320.project.stage5.DocumentStore.DocumentFormat;

public class Stage5Test {

	@Test
	public void simpleWriteToDiskFromCountTest() throws URISyntaxException {
		DocumentStoreImpl ds = new DocumentStoreImpl();
		String str1 = "test number 1"; // mem = 843
		String str2 = "test number 22"; // mem = 845
		URI uri1 = new URI("http://simpleWriteToDiskFromCountgoogle1.com/docs/doc1");
		URI uri2 = new URI("http://simpleWriteToDiskFromCountgoogle2.com/docs/doc2");
		ds.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); 
		ds.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT); 
		ds.setMaxDocumentCount(1); // should move doc1 to disk
		assertTrue(ds.getDocument(uri1) == null);
	}
	
	@Test
	public void simpleWriteToDiskFromUsageTest() throws URISyntaxException {
		DocumentStoreImpl ds = new DocumentStoreImpl();
		String str1 = "test number 1"; // mem = 843
		String str2 = "test number 22"; // mem = 845
		URI uri1 = new URI("http://simpleWriteToDiskFromUsagegoogle1.com/docs/doc1");
		URI uri2 = new URI("http://simpleWriteToDiskFromUsagegoogle2.com/docs/doc2");
		ds.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); 
		ds.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT); 
		ds.setMaxDocumentBytes(900);; // should move doc1 to disk
		assertTrue(ds.getDocument(uri1) == null);
	}
	
	@Test
	public void backToMemoryWithGetTxtTimeUpdatedTest() throws URISyntaxException {
		DocumentStoreImpl ds = new DocumentStoreImpl();
		String str1 = "test number 1"; // mem = 843
		String str2 = "test number 22"; // mem = 845
		URI uri1 = new URI("http://backToMemoryWithGetTxtTimeUpdatedgoogle1.com/docs/doc1");
		URI uri2 = new URI("http://backToMemoryWithGetTxtTimeUpdatedgoogle2.com/docs/doc2");
		ds.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); 
		long originalTime = ds.getDocument(uri1).getLastUseTime();
		ds.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT); 
		ds.setMaxDocumentCount(1); // should move doc1 to disk
		assertTrue(ds.getDocument(uri1) == null);
		assertTrue(ds.getDocumentAsTxt(uri1).equals(str1)); // no longer on disk
		long timeAfterBroughtBack = ds.getDocument(uri1).getLastUseTime();
		assertTrue(originalTime < timeAfterBroughtBack);
	}
	
	@Test
	public void backToMemoryWithGetPdfTimeUpdatedTest() throws URISyntaxException {
		DocumentStoreImpl ds = new DocumentStoreImpl();
		String str1 = "test number 1"; // mem = 843
		String str2 = "test number 22"; // mem = 845
		URI uri1 = new URI("http://backToMemoryWithGetPdfTimeUpdatedgoogle1.com/docs/doc1");
		URI uri2 = new URI("http://backToMemoryWithGetPdfTimeUpdatedgoogle2.com/docs/doc2");
		ds.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); 
		long originalTime = ds.getDocument(uri1).getLastUseTime();
		ds.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT); 
		ds.setMaxDocumentCount(1); // should move doc1 to disk
		assertTrue(ds.getDocument(uri1) == null);
		assertTrue(ds.getDocumentAsPdf(uri1) != null); // no longer on disk
		long timeAfterBroughtBack = ds.getDocument(uri1).getLastUseTime();
		assertTrue(originalTime < timeAfterBroughtBack);
	}
	
	@Test
	public void backToMemoryWithPutSameTimeUpdatedTest() throws URISyntaxException {
		DocumentStoreImpl ds = new DocumentStoreImpl();
		String str1 = "test number 1"; // mem = 843
		String str2 = "test number 22"; // mem = 845
		URI uri1 = new URI("http://backToMemoryWithPutSameTimeUpdatedgoogle1.com/docs/doc1");
		URI uri2 = new URI("http://backToMemoryWithPutSameTimeUpdatedgoogle2.com/docs/doc2");
		ds.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); 
		long originalTime = ds.getDocument(uri1).getLastUseTime();
		ds.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT); 
		ds.setMaxDocumentCount(1); // should move doc1 to disk
		assertTrue(ds.getDocument(uri1) == null); // it's on disk
		ds.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); // back to memory 
		long timeAfterBroughtBack = ds.getDocument(uri1).getLastUseTime();
		assertTrue(originalTime < timeAfterBroughtBack);
	}
	
	@Test
	public void backToMemoryWithPutOverwriteTimeUpdatedTest() throws URISyntaxException {
		DocumentStoreImpl ds = new DocumentStoreImpl();
		String str1 = "test number 1"; // mem = 843
		String str1b = "test number 12"; // mem = 845
		String str2 = "test number 22"; // mem = 845
		URI uri1 = new URI("http://backToMemoryWithPutOverwriteTimeUpdatedgoogle1.com/docs/doc1");
		URI uri2 = new URI("http://backToMemoryWithPutOverwriteTimeUpdatedgoogle2.com/docs/doc2");
		ds.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); 
		long originalTime = ds.getDocument(uri1).getLastUseTime();
		ds.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT); 
		ds.setMaxDocumentCount(1); // should move doc1 to disk
		assertTrue(ds.getDocument(uri1) == null); // it's on disk
		ds.putDocument(new ByteArrayInputStream(str1b.getBytes()), uri1, DocumentFormat.TXT); // back to memory 
		long timeAfterBroughtBack = ds.getDocument(uri1).getLastUseTime();
		assertTrue(originalTime < timeAfterBroughtBack);
		assertTrue(ds.getDocumentAsTxt(uri1).equals(str1b));
	}
	
	/*
	// should fail when have correct API
	@Test
	public void simpleWriteToDiskFromBytesWithinBTreeTest() throws URISyntaxException {
		DocumentStoreImpl ds = new DocumentStoreImpl();
		BTreeImpl<URI,DocumentImpl> bt = ds.btree;
		String str1 = "test number 1"; // mem = 843
		String str2 = "test number 22"; // mem = 845
		URI uri1 = new URI("http://simpleWriteToDiskFromBytesWithinBTreegoogle1.com/docs/doc1");
		URI uri2 = new URI("http://simpleWriteToDiskFromBytesWithinBTreegoogle2.com/docs/doc2");
		ds.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); 
		ds.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT); 
		ds.setMaxDocumentBytes(900);; // should move doc1 to disk
		BTreeImpl.Entry entry = bt.get(bt.root, uri1, bt.height);
		assertNull(entry.getValue()); // if it's on disk the val should be null
		assertTrue(bt.get(uri1).getDocumentAsTxt().equals(str1));
	}
	*/
	
	@Test
	public void simpleWriteToDiskAndBackWithinDocStoreTest() throws URISyntaxException {
		DocumentStoreImpl ds = new DocumentStoreImpl();
		String str1 = "test number 1"; // mem = 843
		String str2 = "test number 22"; // mem = 845
		URI uri1 = new URI("http://simpleWriteToDiskAndBackWithinDocStoregoogle1.com/docs/doc1");
		URI uri2 = new URI("http://simpleWriteToDiskAndBackWithinDocStoregoogle2.com/docs/doc2");
		ds.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); 
		//byte[] pdf1 = ds.getDocumentAsPdf(uri1);
		ds.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT); 
		ds.setMaxDocumentCount(1); // should move doc1 to disk
		assertTrue(ds.getDocument(uri1) == null); // it should be on disk, so peek returns null
		assertTrue(ds.getDocumentAsTxt(uri1).equals(str1));
		assertTrue(ds.getDocument(uri1) != null); // it should be on memory again
		//System.out.println("original pdf was this: "+pdf1);
		//System.out.println("getDocAsPDF returns this: "+ds.getDocumentAsPdf(uri1));
	}
	
	@Test
	public void deleteFromDiskRemovesFromDiskTest() throws URISyntaxException {
		DocumentStoreImpl ds = new DocumentStoreImpl();
		String str1 = "test number 1"; // mem = 843
		String str2 = "test number 22"; // mem = 845
		URI uri1 = new URI("http://deleteFromDiskRemovesFromDiskgoogle1.com/docs/doc1");
		URI uri2 = new URI("http://deleteFromDiskRemovesFromDiskgoogle2.com/docs/doc2");
		ds.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); 
		ds.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT); 
		ds.setMaxDocumentCount(1); // should move doc1 to disk
		assertTrue(ds.getDocument(uri1) == null); // it should be on disk, so peek returns null
		assertTrue(ds.deleteDocument(uri1));
		// to use getDocument(uri) instead of just uri, this is false. Look into delete logic!
		assertTrue(ds.getDocumentAsTxt(uri1) == null);	// not even accessible from disk, yay!
	}	
	
	@Test
	public void deleteFromMemoryRemovesFromDiskTest() throws URISyntaxException {
		DocumentStoreImpl ds = new DocumentStoreImpl();
		String str1 = "test number 1"; // mem = 843
		URI uri1 = new URI("http://deleteFromMemoryRemovesFromDiskgoogle1.com/docs/doc1");
		ds.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); 
		assertTrue(ds.getDocument(uri1) != null); // it should be in memory
		assertTrue(ds.deleteDocument(uri1)); // when i changed the inputstream method call in deleteDoc
		// to use getDocument(uri) instead of just uri, this is false. Look into delete logic!
		assertTrue(ds.getDocument(uri1) == null); // it should no longer be in memory
		assertTrue(ds.getDocumentAsPdf(uri1) == null); // and no longer on disk
	}	
	
	@Test
    public void testDeleteDoc() throws IOException, URISyntaxException {
        String pdfTxt1 = "This is some PDF text for doc1, hat tip to Adobe.";
		byte[] pdfData1 = Utils.textToPdfData(pdfTxt1);
		URI uri1 = new URI("http://yairtestDeleteDedu.yu.cs/com1320/project/doc1");
		DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(pdfData1),uri1, DocumentStore.DocumentFormat.PDF);
        store.deleteDocument(uri1);
        assertEquals("calling get on URI from which doc was deleted should've returned null", null, store.getDocumentAsPdf(uri1));
    }
	
	@Test
	public void getPutsBackIntoMemoryAndRestructuresTest() throws URISyntaxException {
		DocumentStoreImpl ds = new DocumentStoreImpl();
		String str1 = "test number 1"; // mem = 843
		String str2 = "test number 22"; // mem = 845
		URI uri1 = new URI("http://getPutsBackIntoMemoryAndRestructuresgoogle1.com/docs/doc1");
		URI uri2 = new URI("http://getPutsBackIntoMemoryAndRestructuresgoogle2.com/docs/doc2");
		ds.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); 
		ds.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT); 
		ds.setMaxDocumentCount(1); // should move doc1 to disk
		assertTrue(ds.getDocument(uri1) == null && ds.getDocumentAsTxt(uri1).equals(str1)); 
				// it should be on disk, so peek returns null, and then get puts back in memory and returns the text
		assertTrue(ds.getDocument(uri2) == null); // it should be on disk, b/c we called get on 1, which should restructure memory
	}
	
	@Test
	public void searchPutsBackIntoMemoryAndRestructuresTest() throws URISyntaxException {
		DocumentStoreImpl ds = new DocumentStoreImpl();
		String str1 = "test number 1"; // mem = 843
		String str2 = "test number 22"; // mem = 845
		URI uri1 = new URI("http://searchPutsBackIntoMemoryAndRestructuresgoogle1.com/docs/doc1");
		URI uri2 = new URI("http://searchPutsBackIntoMemoryAndRestructuresgoogle2.com/docs/doc2");
		ds.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); 
		ds.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT); 
		ds.setMaxDocumentCount(1); // should move doc1 to disk
		assertTrue(ds.getDocument(uri1) == null); // doc1 is on disk 
		ds.search("1");
		assertTrue(ds.getDocument(uri1) != null); // doc1 is back in memory
		assertTrue(ds.getDocument(uri2) == null); // it should be on disk, b/c we called search on 1, which should restructure memory
	}
	
	@Test
	public void searchByPrefixPutsBackIntoMemoryAndRestructuresTest() throws URISyntaxException {
		DocumentStoreImpl ds = new DocumentStoreImpl();
		String str1 = "test number 11"; // mem = 845
		String str2 = "test number 22"; // mem = 845
		URI uri1 = new URI("http://searchByPrefixPutsBackIntoMemoryAndRestructuresgoogle1.com/docs/doc1");
		URI uri2 = new URI("http://searchByPrefixPutsBackIntoMemoryAndRestructuresgoogle2.com/docs/doc2");
		ds.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); 
		ds.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT); 
		ds.setMaxDocumentCount(1); // should move doc1 to disk
		assertTrue(ds.getDocument(uri1) == null); // doc1 is on disk 
		ds.searchByPrefix("1");
		assertTrue(ds.getDocument(uri1) != null); // doc1 is back in memory
		assertTrue(ds.getDocument(uri2) == null); // it should be on disk, b/c we called search on 1, which should restructure memory
	}
	
	@Test
	public void searchPDFsPutsBackIntoMemoryAndRestructuresTest() throws URISyntaxException {
		DocumentStoreImpl ds = new DocumentStoreImpl();
		String str1 = "test number 1"; // mem = 843
		String str2 = "test number 22"; // mem = 845
		URI uri1 = new URI("http://searchPDFsPutsBackIntoMemoryAndRestructuresgoogle1.com/docs/doc1");
		URI uri2 = new URI("http://searchPDFsPutsBackIntoMemoryAndRestructuresgoogle2.com/docs/doc2");
		ds.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); 
		ds.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT); 
		ds.setMaxDocumentCount(1); // should move doc1 to disk
		assertTrue(ds.getDocument(uri1) == null); // doc1 is on disk 
		ds.searchPDFs("1");
		assertTrue(ds.getDocument(uri1) != null); // doc1 is back in memory
		assertTrue(ds.getDocument(uri2) == null); // it should be on disk, b/c we called search on 1, which should restructure memory
	}
	
	@Test
	public void searchPDFsByPrefixPutsBackIntoMemoryAndRestructuresTest() throws URISyntaxException {
		DocumentStoreImpl ds = new DocumentStoreImpl();
		String str1 = "test number 11"; // mem = 845
		String str2 = "test number 22"; // mem = 845
		URI uri1 = new URI("http://searchPDFsByPrefixPutsBackIntoMemoryAndRestructuresgoogle1.com/docs/doc1");
		URI uri2 = new URI("http://searchPDFsByPrefixPutsBackIntoMemoryAndRestructuresgoogle2.com/docs/doc2");
		ds.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); 
		ds.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT); 
		ds.setMaxDocumentCount(1); // should move doc1 to disk
		assertTrue(ds.getDocument(uri1) == null); // doc1 is on disk 
		ds.searchPDFsByPrefix("1");
		assertTrue(ds.getDocument(uri1) != null); // doc1 is back in memory
		assertTrue(ds.getDocument(uri2) == null); // it should be on disk, b/c we called search on 1, which should restructure memory
	}
	
	@Test
	public void putSameWhenOnDiskPutsBackIntoMemoryAndRestructuresTest() throws URISyntaxException {
		DocumentStoreImpl ds = new DocumentStoreImpl();
		String str1 = "test number 1"; // mem = 843
		String str2 = "test number 22"; // mem = 845
		URI uri1 = new URI("http://putSameWhenOnDiskPutsBackIntoMemoryAndRestructuresgoogle1.com/docs/doc1");
		URI uri2 = new URI("http://putSameWhenOnDiskPutsBackIntoMemoryAndRestructuresgoogle2.com/docs/doc2");
		ds.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); 
		ds.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT); 
		ds.setMaxDocumentCount(1); // should move doc1 to disk
		assertTrue(ds.getDocument(uri1) == null); // it's on disk
		// now we put the same doc back in
		ds.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); 
		assertTrue(ds.getDocument(uri1) != null); // it's now back from disk
			// we put at the same key that's on disk, should knock doc2 out of memory
		assertTrue(ds.getDocument(uri2) == null); // it should be on disk, b/c we called get on 1, which should restructure memory
		ds.undo();
		assertTrue(ds.getDocument(uri2) == null && ds.getDocumentAsTxt(uri2) != null); 
			// it should be on disk, as per @434, so peek returns null, and get returns it
		
	}
	
	@Test
	public void writtenToDiskUndoneNotBroughtToMemoryTest() throws URISyntaxException {
		DocumentStoreImpl ds = new DocumentStoreImpl();
		String str1 = "test number 1"; // mem = 843
		String str2 = "test number 22"; // mem = 845
		URI uri1 = new URI("http://putBroughtToMemoryThenUndoneStillInMemorygoogle1.com/docs/doc1");
		URI uri2 = new URI("http://putBroughtToMemoryThenUndoneStillInMemorygoogle2.com/docs/doc2");
		ds.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); 
		ds.setMaxDocumentCount(1);
		ds.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT); 
		assertTrue(ds.getDocument(uri1) == null); // should be on disk b/c of 2 being put
		ds.undo(); // as per @434, this should NOT bring doc1 back from disk
		assertTrue(ds.getDocument(uri1) == null); 
		// just checking that it is at least still on disk:
		assertTrue(ds.getDocumentAsTxt(uri1).equals(str1));
	}
	
	@Test
	public void getBroughtToMemoryThenUndoneStillInMemoryTest() throws URISyntaxException {
		DocumentStoreImpl ds = new DocumentStoreImpl();
		String str1 = "test number 1"; // mem = 843
		String str1b = "test number 12"; // mem = 845
		String str2 = "test number 22"; // mem = 845
		URI uri1 = new URI("http://getBroughtToMemoryThenUndoneStillInMemorygoogle1.com/docs/doc1");
		URI uri2 = new URI("http://getBroughtToMemoryThenUndoneStillInMemorygoogle2.com/docs/doc2");
		ds.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); 
		ds.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT); 
		ds.setMaxDocumentCount(1); // should move doc1 to disk
		assertTrue(ds.getDocument(uri1) == null);
		// testing different ways to get it back in memory:
		ds.getDocumentAsTxt(uri1);
		//ds.search("number");
		assertTrue(ds.getDocument(uri1) != null);
		ds.undo();
		assertTrue(ds.getDocument(uri1) != null); // as per @434_f1, it should remain in memory
	}
	
	/*  I argued that the logic here was wrong, and that really it should be deleted
	@Test
	public void putBroughtToMemoryThenUndoneStillInMemoryTest() throws URISyntaxException {
		DocumentStoreImpl ds = new DocumentStoreImpl();
		String str1 = "test number 1"; // mem = 843
		String str1b = "test number 12"; // mem = 845
		String str2 = "test number 22"; // mem = 845
		URI uri1 = new URI("http://getBroughtToMemoryThenUndoneStillInMemorygoogle1.com/docs/doc1");
		URI uri2 = new URI("http://getBroughtToMemoryThenUndoneStillInMemorygoogle2.com/docs/doc2");
		ds.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); 
		ds.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT); 
		ds.setMaxDocumentCount(1); // should move doc1 to disk
		assertTrue(ds.getDocument(uri1) == null);
		ds.setMaxDocumentCount(2); // now when we bring it back to memory it shouldn't know out doc2
		ds.putDocument(new ByteArrayInputStream(str1b.getBytes()), uri1, DocumentFormat.TXT); // should bring 1 back to memory
		assertTrue(ds.getDocument(uri1) != null);
		ds.undo();
		assertTrue(ds.getDocument(uri1) != null); // as per @434_f1, it should remain in memory
	}
	*/
	
	@Test
	public void searchBroughtToMemoryThenUndoneStillInMemoryTest() throws URISyntaxException {
		DocumentStoreImpl ds = new DocumentStoreImpl();
		String str1 = "test number 1"; // mem = 843
		String str1b = "test number 12"; // mem = 845
		String str2 = "test number 22"; // mem = 845
		URI uri1 = new URI("http://searchBroughtToMemoryThenUndoneStillInMemorygoogle1.com/docs/doc1");
		URI uri2 = new URI("http://searchBroughtToMemoryThenUndoneStillInMemorygoogle2.com/docs/doc2");
		ds.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); 
		ds.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT); 
		ds.setMaxDocumentCount(1); // should move doc1 to disk
		assertTrue(ds.getDocument(uri1) == null);
		ds.setMaxDocumentCount(2); // making it so that now they won't boot each other, so we can see if 
								   // undo on search puts back onto disk 
		ds.search("number");
		assertTrue(ds.getDocument(uri1) != null);
		ds.undo(); // this should not put doc1 back on disk, rather it should delete doc2
		assertTrue(ds.getDocument(uri1) != null); // as per @434_f1, it should remain in memory
		assertTrue(ds.getDocument(uri2) == null); // doc2 should be deleted b/c undo undid its put
	}
	
	
	
	@Test
	public void putAlreadyInMemoryTest() throws URISyntaxException {
		DocumentStoreImpl ds = new DocumentStoreImpl();
		String str1 = "test number 1"; // mem = 843
		URI uri1 = new URI("http://putAlreadyInMemorygoogle1.com/docs/doc1");
		ds.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); 
		ds.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); 
		assertTrue(ds.getDocument(uri1) != null);
		// optional undo test to make sure moot undo is working. Can comment out if need be
		ds.undo();
		assertTrue(ds.getDocumentAsTxt(uri1).equals(str1));
		// hopefully this doesn't blow up!
	}
	
	@Test
	public void putOverwriteWhenOnDiskPutsBackIntoMemoryAndRestructuresTest() throws URISyntaxException {
		DocumentStoreImpl ds = new DocumentStoreImpl();
		String str1 = "test number 1"; // mem = 843
		String str1b = "test number 12"; // mem = 845
		String str2 = "test number 22"; // mem = 845
		URI uri1 = new URI("http://putOverwriteWhenOnDiskPutsBackIntoMemoryAndRestructuresgoogle1.com/docs/doc1");
		URI uri2 = new URI("http://putOverwriteWhenOnDiskPutsBackIntoMemoryAndRestructuresgoogle2.com/docs/doc2");
		ds.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); 
		ds.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT); 
		ds.setMaxDocumentCount(1); // should move doc1 to disk
		assertTrue(ds.getDocument(uri1) == null); // it's on disk
		// now we put a different doctext at that uri, i.e. an overwrite
		ds.putDocument(new ByteArrayInputStream(str1b.getBytes()), uri1, DocumentFormat.TXT); 
		assertTrue(ds.getDocument(uri1) != null); // it's now back from disk
			// we put at the same key that's on disk, should knock doc2 out of memory
				// it should be on disk, so peek returns null, and then get puts back in memory and returns the text
		assertTrue(ds.getDocument(uri2) == null); // it should be on disk, b/c we called get on 1, which should restructure memory
		assertTrue(ds.getDocumentAsTxt(uri1).equals(str1b)); // the text at uri1 should now be the overwrite, i.e. str1b
	}
	
}