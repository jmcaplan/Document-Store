package edu.yu.cs.com1320.project.stage4.impl;

import edu.yu.cs.com1320.project.stage4.DocumentStore.DocumentFormat;
import edu.yu.cs.com1320.project.stage4.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage4.impl.DocumentStoreImpl;

import static org.junit.Assert.*;
import java.io.*;
import java.net.*;
import java.util.*;

import org.junit.Test;

public class DocumentStoreImplTest {
	DocumentStoreImpl documentStore = new DocumentStoreImpl();
	String hiMom = "to";
	byte[] buf = hiMom.getBytes();
	ByteArrayInputStream input = new ByteArrayInputStream(buf);
	String byeMom = "ll is swell thanks for asking, ooh burn sauce!!!?!?! #winning";
	byte[] buf2 = byeMom.getBytes();
	ByteArrayInputStream input2 = new ByteArrayInputStream(buf2);
	String hiDad = "ll is swell thadad da dad dad";
	byte[] buf3 = hiDad.getBytes();
	ByteArrayInputStream input3 = new ByteArrayInputStream(buf3);
	
	//////////////    NOTE -->  the memory usage of "test number X" is 843, and add 2 for each additional digit on X
	//////////////                  e.g. "test number 1" is 843, "test number 11" is 845, etc...

	@Test
	public void commandSetErasedMemoryTest() throws URISyntaxException {
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		String str1 = "test number 1"; // mem = 843
		String str2 = "test number 22"; // mem = 845
		String str2b = "test number 222"; // mem = 847
		String str3 = "test number 333 with undo"; // mem = 867
		String str4 = "test number 4444 with undo"; // mem = 867
		URI uri1 = new URI("google1.com");
		URI uri2 = new URI("google2.com");
		URI uri3 = new URI("google3.com");
		URI uri4 = new URI("google4.com");
		docStore.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); // 
		docStore.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT); //
		docStore.putDocument(new ByteArrayInputStream(str3.getBytes()), uri3, DocumentFormat.TXT); //
		docStore.putDocument(new ByteArrayInputStream(str4.getBytes()), uri4, DocumentFormat.TXT); //
		//System.out.println("Expect 4: "+docStore.commandStack.size());
		// total memory is 3422
		assertTrue(docStore.getDocumentAsTxt(uri2).equals(str2)
				   && docStore.getDocumentAsTxt(uri1).equals(str1) 
				   && docStore.getDocumentAsTxt(uri3).equals(str3)
				   && docStore.getDocumentAsTxt(uri4).equals(str4)
		           );
		// there is an undo on stack for each of the 4 docs that were put
		docStore.deleteAll("undo");
	    //System.out.println("Expect 5: "+docStore.commandStack.size()); // [del1,del2,del3,del4,put(3-4)]
		// 3 and 4 should be gone, and there should be a commandset with puts for 3 and 4
		assertTrue(docStore.getDocumentAsTxt(uri2).equals(str2)
				   && docStore.getDocumentAsTxt(uri1).equals(str1) 
				   && docStore.getDocument(uri3)==null
				   && docStore.getDocument(uri4)==null
		           );
		//System.out.println("Expect 5: "+docStore.commandStack.size()); // [del1,del2,del3,del4,put(3-4)]
		docStore.putDocument(new ByteArrayInputStream(str3.getBytes()), uri3, DocumentFormat.TXT);
		//System.out.println("Expect 6: "+docStore.commandStack.size()); // [del1,del2,del3,del4,put(3-4),del3]
		docStore.getDocumentAsTxt(uri2);
		docStore.getDocumentAsTxt(uri1);
		docStore.setMaxDocumentCount(2);
		// should erase doc3 totally, including from the commandSet
		//System.out.println("Expect 4: "+docStore.commandStack.size()); // [del1,del2,del4,put(4)]
		assertTrue(docStore.getDocumentAsTxt(uri2).equals(str2)
				   && docStore.getDocumentAsTxt(uri1).equals(str1) 
				   && docStore.getDocument(uri3)==null
				   && docStore.getDocument(uri4)==null
		           );	
		docStore.undo();
		assertTrue(docStore.getDocument(uri2) == null &&
				docStore.getDocument(uri1)!=null &&
				docStore.getDocument(uri4)!=null
				&& docStore.getDocument(uri3)==null
	           );	
		
		
	}
	
	@Test
	public void alreadyOverClearsMemoryTest() throws URISyntaxException {
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		String str1 = "test number 1"; // mem = 843
		String str2 = "test number 22"; // mem = 845
		String str3 = "test number 333"; // mem = 847
		
		URI uri1 = new URI("google1.com");
		URI uri2 = new URI("google2.com");
		URI uri3 = new URI("google3.com");
		docStore.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT); //
		docStore.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); // switched up order
		docStore.putDocument(new ByteArrayInputStream(str3.getBytes()), uri3, DocumentFormat.TXT); //
		// total memory is 2535, we set limit just below that, should erase doc2 since that was put least recently
		docStore.setMaxDocumentBytes(2534);
		assertTrue(docStore.getDocument(uri2) == null && docStore.searchByPrefix("2").isEmpty()); 
     // docStore.undo(uri2); // this line activated should throw IllegalStateException
		// total docs is now 2, we will set limit to 1, should remove doc1 since 3 was used after 1
		docStore.setMaxDocumentCount(1);
		assertTrue(docStore.getDocument(uri1) == null && docStore.search("1").isEmpty()); 
	 // docStore.undo(uri1); // this line activated should throw IllegalStateException
		// assuming the erase worked on commandStack too, undo should delete doc3
		assertTrue(docStore.getDocument(uri3) != null);
		docStore.undo(uri3);
		assertTrue(docStore.getDocument(uri3) == null);
	}
	
	@Test
	public void putNewClearsBytesTest() throws URISyntaxException {
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		String str1 = "test number 1"; // mem = 843
		String str2 = "test number 22"; // mem = 845
		String str3 = "test number 333"; // mem = 847
		URI uri1 = new URI("google1.com");
		URI uri2 = new URI("google2.com");
		URI uri3 = new URI("google3.com");
		docStore.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT); //
		docStore.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); // switched up order
		// total memory is 1688, we setMax to 1690, add doc3, should remove doc2 but keep doc1 b/c doc1+doc3=1690
		docStore.setMaxDocumentBytes(1690);
		assertTrue(docStore.getDocument(uri2) != null && docStore.getDocument(uri1) != null); 
		docStore.putDocument(new ByteArrayInputStream(str3.getBytes()), uri3, DocumentFormat.TXT);
		assertTrue(docStore.getDocument(uri2) == null && docStore.searchByPrefix("2").isEmpty()); 
		assertTrue(docStore.getDocument(uri1) != null && !docStore.searchByPrefix("1").isEmpty()); // doc1 is still there, great
		assertTrue(docStore.getDocument(uri3) != null && !docStore.searchByPrefix("3").isEmpty()); // doc3 is still there, great

       //docStore.undo(uri2); //this line activated should throw IllegalStateException
	}
	
	
	@Test
	public void putNewClearsDocsTest() throws URISyntaxException {
		// long t0 = System.nanoTime();
		// System.out.println("t0 -> "+ (System.nanoTime()-t0) );
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		// System.out.println("t1 -> "+ (System.nanoTime()-t0) );
		String str1 = "test number 1"; // mem = 843
		String str2 = "test number 22"; // mem = 845
		String str3 = "test number 333"; // mem = 847
		// System.out.println("t2 -> "+ (System.nanoTime()-t0) );
		URI uri1 = new URI("google1.com");
		URI uri2 = new URI("google2.com");
		URI uri3 = new URI("google3.com");
		// System.out.println("t3 -> "+ (System.nanoTime()-t0) );
		docStore.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT); //
		docStore.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); // switched up order
		// System.out.println("t4 -> "+ (System.nanoTime()-t0) );
		// total docs is 2, we setMax to 2, then add doc3, should remove doc2 but keep doc1 
		docStore.setMaxDocumentCount(2);
		// System.out.println("t5 -> "+ (System.nanoTime()-t0) );
		assertTrue(docStore.getDocument(uri2) != null && docStore.getDocument(uri1) != null);  // 1 and 2 are there
		// System.out.println("t6 -> "+ (System.nanoTime()-t0) );
		docStore.putDocument(new ByteArrayInputStream(str3.getBytes()), uri3, DocumentFormat.TXT);
		// System.out.println("t7 -> "+ (System.nanoTime()-t0) );
		assertTrue(docStore.getDocument(uri2) == null && docStore.searchByPrefix("2").isEmpty()); 
		// System.out.println("t8 -> "+ (System.nanoTime()-t0) );
		assertTrue(docStore.getDocument(uri1) != null && !docStore.searchByPrefix("1").isEmpty()); // doc1 is still there, great
		// System.out.println("t9 -> "+ (System.nanoTime()-t0) );
		assertTrue(docStore.getDocument(uri3) != null && !docStore.searchByPrefix("3").isEmpty()); // doc3 is still there, great
		// System.out.println("t10 -> "+ (System.nanoTime()-t0) );
      // docStore.undo(uri2); this line activated should throw IllegalStateException
	}
	
	
	@Test
	public void putOverwriteClearsBytesTest() throws URISyntaxException {
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		String str1 = "test number 1"; // mem = 843
		String str2 = "test number 22"; // mem = 845
		String str2b = "test number 222"; // mem = 847
		URI uri1 = new URI("google1.com");
		URI uri2 = new URI("google2.com");
		docStore.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT); //
		docStore.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); // switched up order
		// total memory is 1688, we set max there so any add of memory will erase top of heap
		docStore.setMaxDocumentBytes(1688); 
		assertTrue(docStore.getDocumentAsPdf(uri1) != null && docStore.getDocumentAsPdf(uri2) != null); // state: doc1 and doc2 still
		docStore.putDocument(new ByteArrayInputStream(str2b.getBytes()), uri2, DocumentFormat.TXT); 
		         // overwrite doc2 with more bytes, should remove doc1 to make room
		assertTrue(docStore.getDocument(uri1) == null && docStore.searchByPrefix("1").isEmpty()); 
		assertTrue(docStore.getDocument(uri2) != null && !docStore.search("222").isEmpty()); // doc2 is still there

      //docStore.undo(uri1); //this line activated should throw IllegalStateException
	}
	
	@Test
	public void putOverwriteNotClearDocsTest() throws URISyntaxException {
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		String str1 = "test number 1"; // mem = 843
		String str2 = "test number 22"; // mem = 845
		String str2b = "test number 222"; // mem = 847
		String str3 = "test number 333"; // mem = 847
		URI uri1 = new URI("google1.com");
		URI uri2 = new URI("google2.com");
		URI uri3 = new URI("google3.com");
		docStore.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT); //
		docStore.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT); // switched up order
		docStore.putDocument(new ByteArrayInputStream(str3.getBytes()), uri3, DocumentFormat.TXT); //

		// total docs is 3, we set max there 
		docStore.setMaxDocumentCount(3); 
		
		assertTrue(docStore.getDocumentAsPdf(uri2) != null && docStore.getDocumentAsPdf(uri1) != null && docStore.getDocumentAsPdf(uri3) != null); 
		assertTrue(docStore.getDocument(uri2) != null && docStore.search("222").isEmpty()); // doc2 is there but not strb
			// state: doc1 and doc2 and doc3
		docStore.putDocument(new ByteArrayInputStream(str2b.getBytes()), uri2, DocumentFormat.TXT); 
		         // overwrite doc2 with more bytes, nothing should change, except uri2 should associate with str2b
		assertTrue(docStore.getDocumentAsPdf(uri2) != null && docStore.getDocumentAsPdf(uri1) != null && docStore.getDocumentAsPdf(uri3) != null); 
		assertTrue(docStore.getDocument(uri2) != null && !docStore.search("222").isEmpty()); // doc2 is now str2b

	}
	
	@Test
	public void updateLastUsedTimePutTest() throws URISyntaxException {
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		String str1 = "test number one";
		URI uri1 = new URI("google1.com");
		long time0 = System.nanoTime();
		docStore.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT);
		long time1 = docStore.getDocument(uri1).getLastUseTime();
		docStore.deleteDocument(uri1);
		docStore.undo(); // new put
		long time2 = docStore.getDocument(uri1).getLastUseTime();
		assertTrue(time1 > time0 && time1 < time2);
	}
	
	@Test
	public void updateLastUsedTimeGetTest() throws URISyntaxException {
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		String str1 = "test number one";
		URI uri1 = new URI("google1.com");
		docStore.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT);
		long time1 = docStore.getDocument(uri1).getLastUseTime();
		docStore.getDocumentAsTxt(uri1);
		long time2 = docStore.getDocument(uri1).getLastUseTime();
		docStore.getDocumentAsPdf(uri1);
		long time3 = docStore.getDocument(uri1).getLastUseTime();
		assertTrue(time1 < time2 && time2 < time3);
	}
	
	@Test
	public void updateLastUsedTimeSearchTest() throws URISyntaxException {
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		String str1 = "test number one";
		String str2 = "test number two";
		URI uri1 = new URI("google1.com");
		URI uri2 = new URI("google2.com");
		docStore.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT);
		docStore.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT);
		long time1a = docStore.getDocument(uri1).getLastUseTime(); // original times
		long time2a = docStore.getDocument(uri2).getLastUseTime();
		
		docStore.search("number");
		long time1b = docStore.getDocument(uri1).getLastUseTime(); // times after update b/c of search
		long time2b = docStore.getDocument(uri1).getLastUseTime();
		assertTrue(time1a < time1b && time2a < time2b && time1b == time2b); // checks that they were updated, and to the same time
	}
	
	@Test
	public void updateLastUsedTimeSearchPDFsTest() throws URISyntaxException {
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		String str1 = "test number one";
		String str2 = "test number two";
		URI uri1 = new URI("google1.com");
		URI uri2 = new URI("google2.com");
		docStore.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT);
		docStore.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT);
		long time1a = docStore.getDocument(uri1).getLastUseTime(); // original times
		long time2a = docStore.getDocument(uri2).getLastUseTime();
		
		docStore.searchPDFs("number");
		long time1b = docStore.getDocument(uri1).getLastUseTime(); // times after update b/c of search
		long time2b = docStore.getDocument(uri1).getLastUseTime();
		assertTrue(time1a < time1b && time2a < time2b && time1b == time2b); // checks that they were updated, and to the same time
	}
	
	@Test
	public void updateLastUsedTimeSearchByPrefixTest() throws URISyntaxException {
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		String str1 = "test number one";
		String str2 = "test number two";
		URI uri1 = new URI("google1.com");
		URI uri2 = new URI("google2.com");
		docStore.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT);
		docStore.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT);
		long time1a = docStore.getDocument(uri1).getLastUseTime(); // original times
		long time2a = docStore.getDocument(uri2).getLastUseTime();
		
		docStore.searchByPrefix("nu");
		long time1b = docStore.getDocument(uri1).getLastUseTime(); // times after update b/c of search
		long time2b = docStore.getDocument(uri1).getLastUseTime();
		assertTrue(time1a < time1b && time2a < time2b && time1b == time2b); // checks that they were updated, and to the same time
	}
	
	@Test
	public void updateLastUsedTimeSearchPDFsByPrefixTest() throws URISyntaxException {
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		String str1 = "test number one";
		String str2 = "test number two";
		URI uri1 = new URI("google1.com");
		URI uri2 = new URI("google2.com");
		docStore.putDocument(new ByteArrayInputStream(str1.getBytes()), uri1, DocumentFormat.TXT);
		docStore.putDocument(new ByteArrayInputStream(str2.getBytes()), uri2, DocumentFormat.TXT);
		long time1a = docStore.getDocument(uri1).getLastUseTime(); // original times
		long time2a = docStore.getDocument(uri2).getLastUseTime();
		
		docStore.searchPDFsByPrefix("nu");
		long time1b = docStore.getDocument(uri1).getLastUseTime(); // times after update b/c of search
		long time2b = docStore.getDocument(uri1).getLastUseTime();
		assertTrue(time1a < time1b && time2a < time2b && time1b == time2b); // checks that they were updated, and to the same time
	}
	
	
	
	@Test
	public void simpleSearchTest() throws URISyntaxException {
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		String str1 = "test number one";
		String str2 = "test number number number two";
		String str3 = "test number number three";
		docStore.putDocument(new ByteArrayInputStream(str1.getBytes()), new URI("google1.com"), DocumentFormat.TXT);
		docStore.putDocument(new ByteArrayInputStream(str2.getBytes()), new URI("google2.com"), DocumentFormat.TXT);
		docStore.putDocument(new ByteArrayInputStream(str3.getBytes()), new URI("google3.com"), DocumentFormat.TXT);
		assertEquals("testing basic search", 
				"[test number number number two, test number number three, test number one]",
				docStore.searchByPrefix("number").toString());
	}
	
	@Test
	public void simpleSearchByPrefixTest() throws URISyntaxException {
		DocumentStoreImpl docStore = new DocumentStoreImpl();
		String str1 = "too a t to"; // has 3 words with prefix t
		String str2 = "b t";        // has 1 word  with prefix t
		String str3 = "trumpet t";  // has 2 words with prefix t
		String str4 = "no matches"; // has 0 words with prefix t
		docStore.putDocument(new ByteArrayInputStream(str1.getBytes()), new URI("google1.com"), DocumentFormat.TXT);
		docStore.putDocument(new ByteArrayInputStream(str2.getBytes()), new URI("google2.com"), DocumentFormat.TXT);
		docStore.putDocument(new ByteArrayInputStream(str3.getBytes()), new URI("google3.com"), DocumentFormat.TXT);
		docStore.putDocument(new ByteArrayInputStream(str4.getBytes()), new URI("google4.com"), DocumentFormat.TXT);
		assertEquals("testing basic search by prefix", 
				"[too a t to, trumpet t, b t]",
				docStore.searchByPrefix("t").toString());
	}
	
	@Test
	public void undoMultipleOverWriteTest() throws URISyntaxException {
		URI exampleURI = new URI("https://john.doe@www.example.com:123/forum/questions/?");
		documentStore.putDocument(input, exampleURI, DocumentFormat.TXT);
		documentStore.putDocument(input2,exampleURI,DocumentFormat.TXT);
		documentStore.putDocument(input3,exampleURI,DocumentFormat.TXT);
		documentStore.undo(exampleURI);
		documentStore.undo(exampleURI);
		String result = documentStore.getDocumentAsTxt(exampleURI);
		assertEquals("testing undo() when deleted by passing null",  hiMom.trim().replace("\n", " "),result);
	}
	
	/////////////////// BASIC UNDO TESTS ///////////////////
	
	@Test
	public void undoDeletePassingNullTest() throws URISyntaxException {
		URI exampleURI = new URI("https://john.doe@www.example.com:123/forum/questions/?");
		documentStore.putDocument(input, exampleURI, DocumentFormat.TXT);
		documentStore.putDocument(null,exampleURI,DocumentFormat.TXT);
		documentStore.undo();
		String result = documentStore.getDocumentAsTxt(exampleURI);
		assertEquals("testing undo() when deleted by passing null", result, hiMom.trim().replace("\n", " "));
	}
	
	@Test
	public void undoDeleteDocumentTest() throws URISyntaxException {
		URI exampleURI = new URI("https://john.doe@www.example.com:123/forum/questions/?");
		documentStore.putDocument(input, exampleURI, DocumentFormat.TXT);
		documentStore.deleteDocument(exampleURI);
		documentStore.undo();
		String result = documentStore.getDocumentAsTxt(exampleURI);
		assertEquals("testing undo() when deleted by deleteDocument()", result, hiMom.trim().replace("\n", " "));
	}
	
	@Test
	public void undoMootDeletePassingNullTest() throws URISyntaxException {
		URI exampleURI = new URI("https://john.doe@www.example201.com:123/forum/questions/?");
		URI exampleURI2 = new URI("https://john.doe@www.example202.com:123/forum/questions/?");
		documentStore.putDocument(input, exampleURI, DocumentFormat.TXT);
		documentStore.putDocument(null,exampleURI2,DocumentFormat.TXT);
		documentStore.undo(); // this should undo the moot delete, and not undo the first put, so getting the txt of the first put should still work
		String result = documentStore.getDocumentAsTxt(exampleURI);
		assertEquals("testing undo() when did moot delete by passing null", result, hiMom.trim().replace("\n", " "));
	}
	
	@Test
	public void undoMootDeleteDocumentTest() throws URISyntaxException {
		URI exampleURI = new URI("https://john.doe@www.example201.com:123/forum/questions/?");
		URI exampleURI2 = new URI("https://john.doe@www.example202.com:123/forum/questions/?");
		documentStore.putDocument(input, exampleURI, DocumentFormat.TXT);
		documentStore.deleteDocument(exampleURI2);
		documentStore.undo(); // this should undo the moot delete, and not undo the first put, so getting the txt of the first put should still work
		String result = documentStore.getDocumentAsTxt(exampleURI);
		assertEquals("testing undo() when did moot delete by deleteDocument()", result, hiMom.trim().replace("\n", " "));
	}
	
	@Test
	public void undoPutNoPreviousTest() throws URISyntaxException {
		URI exampleURI = new URI("https://john.doe@www.example201.com:123/forum/questions/?");
		documentStore.putDocument(input, exampleURI, DocumentFormat.TXT);
		documentStore.undo(); // this should undo the put, and a get on that URI should return null
		assertEquals("testing undo() of a put when no previous doc", null, documentStore.getDocumentAsPdf(exampleURI));
	}
	
	@Test
	public void undoPutOverWriteTest() throws URISyntaxException {
		URI exampleURI = new URI("https://john.doe@www.example201.com:123/forum/questions/?");
		documentStore.putDocument(input, exampleURI, DocumentFormat.TXT);
		documentStore.putDocument(input2, exampleURI, DocumentFormat.TXT);
		documentStore.undo(); // this should undo the overwrite, and get should return the first input's text
		String result = documentStore.getDocumentAsTxt(exampleURI);
		assertEquals("testing undo() of a put when there was a previous doc", result, hiMom.trim().replace("\n", " "));
	}
	
	@Test
	public void twoPutUndosTest() throws URISyntaxException {
		// long t0 = System.nanoTime();
		URI exampleURI = new URI("https://john.doe@www.example201.com:123/forum/questions/?");
		URI exampleURI2 = new URI("https://john.doe@www.example202.com:123/forum/questions/?");
		// System.out.println("t1 -> "+ (System.nanoTime()-t0) );
		documentStore.putDocument(input, exampleURI, DocumentFormat.TXT);
		// System.out.println("t2 -> "+ (System.nanoTime()-t0) );
		documentStore.putDocument(input2, exampleURI2, DocumentFormat.TXT);
		// System.out.println("t3 -> "+ (System.nanoTime()-t0) );
		documentStore.undo(); // this should undo the overwrite, and get should return the first input's text
		documentStore.undo(); // let's assume this is supposed to undo the first put
		assertEquals("testing undo() of a put when there was a previous doc", null, documentStore.getDocumentAsTxt(exampleURI));
	}
	
	@Test
	public void twoDeleteUndosTest() throws URISyntaxException {
		URI exampleURI = new URI("https://john.doe@www.example201.com:123/forum/questions/?");
		URI exampleURI2 = new URI("https://john.doe@www.example202.com:123/forum/questions/?");
		documentStore.putDocument(input, exampleURI, DocumentFormat.TXT);
		documentStore.putDocument(input2, exampleURI2, DocumentFormat.TXT);
		documentStore.deleteDocument(exampleURI);
		documentStore.deleteDocument(exampleURI2);
		documentStore.undo(); // this should undo the second delete, and put back the second doc
		documentStore.undo(); // this should undo the first delete, and put back the first doc
		assertEquals("testing undo() of a put when there was a previous doc", hiMom.trim().replace("\n", " "), documentStore.getDocumentAsTxt(exampleURI));
	}
	
	//////////////////// SAME TESTS BUT WITH UNDO BY URI //////////////////////
	
	@Test
	public void undoByURIDeletePassingNullTest() throws URISyntaxException {
		URI exampleURI = new URI("https://john.doe@www.example.com:123/forum/questions/?");
		documentStore.putDocument(input, exampleURI, DocumentFormat.TXT);
		documentStore.putDocument(null,exampleURI,DocumentFormat.TXT);
		documentStore.undo(exampleURI);
		String result = documentStore.getDocumentAsTxt(exampleURI);
		assertEquals("testing undo() when deleted by passing null", result, hiMom.trim().replace("\n", " "));
	}
	
	@Test
	public void undoByURIDeleteDocumentTest() throws URISyntaxException {
		URI exampleURI = new URI("https://john.doe@www.example.com:123/forum/questions/?");
		documentStore.putDocument(input, exampleURI, DocumentFormat.TXT);
		documentStore.deleteDocument(exampleURI);
		documentStore.undo(exampleURI);
		String result = documentStore.getDocumentAsTxt(exampleURI);
		assertEquals("testing undo() when deleted by deleteDocument()", result, hiMom.trim().replace("\n", " "));
	}
	
	@Test
	public void undoByURIMootDeletePassingNullTest() throws URISyntaxException {
		URI exampleURI = new URI("https://john.doe@www.example201.com:123/forum/questions/?");
		URI exampleURI2 = new URI("https://john.doe@www.example202.com:123/forum/questions/?");
		documentStore.putDocument(input, exampleURI, DocumentFormat.TXT);
		documentStore.putDocument(null,exampleURI2,DocumentFormat.TXT);
		documentStore.undo(exampleURI2); // this should undo the moot delete, and not undo the first put, so getting the txt of the first put should still work
		String result = documentStore.getDocumentAsTxt(exampleURI);
		assertEquals("testing undo() when did moot delete by passing null", result, hiMom.trim().replace("\n", " "));
	}
	
	@Test
	public void undoByURIMootDeleteDocumentTest() throws URISyntaxException {
		URI exampleURI = new URI("https://john.doe@www.example201.com:123/forum/questions/?");
		URI exampleURI2 = new URI("https://john.doe@www.example202.com:123/forum/questions/?");
		documentStore.putDocument(input, exampleURI, DocumentFormat.TXT);
		documentStore.deleteDocument(exampleURI2);
		documentStore.undo(exampleURI2); // this should undo the moot delete, and not undo the first put, so getting the txt of the first put should still work
		String result = documentStore.getDocumentAsTxt(exampleURI);
		assertEquals("testing undo() when did moot delete by deleteDocument()", result, hiMom.trim().replace("\n", " "));
	}
	
	@Test
	public void undoByURIPutNoPreviousTest() throws URISyntaxException {
		URI exampleURI = new URI("https://john.doe@www.example201.com:123/forum/questions/?");
		documentStore.putDocument(input, exampleURI, DocumentFormat.TXT);
		documentStore.undo(exampleURI); // this should undo the put, and a get on that URI should return null
		assertEquals("testing undo() of a put when no previous doc", null, documentStore.getDocumentAsPdf(exampleURI));
	}
	
	@Test
	public void undoByURIPutOverWriteTest() throws URISyntaxException {
		URI exampleURI = new URI("https://john.doe@www.example201.com:123/forum/questions/?");
		documentStore.putDocument(input, exampleURI, DocumentFormat.TXT);
		documentStore.putDocument(input2, exampleURI, DocumentFormat.TXT);
		documentStore.undo(exampleURI); // this should undo the overwrite, and get should return the first input's text
		String result = documentStore.getDocumentAsTxt(exampleURI);
		assertEquals("testing undo() of a put when there was a previous doc", result, hiMom.trim().replace("\n", " "));
	}
	
	
	@Test
	public void undoProofOfConceptTest() throws URISyntaxException {
		String prefix = "https://john.doe@www.example000";
		String suffix = ".com:123/forum/questions/?";
		URI uri = null;;
		for (int i = 1; i<=20; i++) {
			uri = new URI(prefix+i+suffix);
			String str = hiMom+i;
			documentStore.putDocument(new ByteArrayInputStream(str.getBytes()), uri, DocumentFormat.TXT);
		}
		for (int i=1;i<=5;i++) documentStore.undo();
		assertEquals("testing undo() with many puts", hiMom+15, documentStore.getDocumentAsTxt(new URI(prefix+15+suffix)));
		assertEquals("testing undo() with many puts", null, documentStore.getDocumentAsTxt(new URI(prefix+16+suffix)));
	}
	
	@Test
	public void undoByURIProofOfConceptTest() throws URISyntaxException {
		String prefix = "https://john.doe@www.example000";
		String suffix = ".com:123/forum/questions/?";
		URI uri = null;;
		URI toSave = null;
		for (int i = 1; i<=20; i++) {
			uri = new URI(prefix+i+suffix);
			if (i==15) toSave = uri;
			String str = hiMom+i;
			documentStore.putDocument(new ByteArrayInputStream(str.getBytes()), uri, DocumentFormat.TXT);
		}
		documentStore.undo(toSave);
		assertEquals("testing undo(URI) with many puts", null, documentStore.getDocumentAsTxt(new URI(prefix+15+suffix)));
		assertEquals("testing undo(URI) with many puts", hiMom+16, documentStore.getDocumentAsTxt(new URI(prefix+16+suffix)));
		assertEquals("testing undo(URI) with many puts", hiMom+14, documentStore.getDocumentAsTxt(new URI(prefix+14+suffix)));
	}
	
	@Test
	public void getDocumentAsTextTest() throws URISyntaxException {
		URI exampleURI = new URI("https://john.doe@www.example.com:123/forum/questions/?");
		documentStore.putDocument(input, exampleURI, DocumentFormat.TXT);
		String result = documentStore.getDocumentAsTxt(exampleURI);
		assertEquals("testing conversion into byte array", result, hiMom.trim().replace("\n", " "));
	}
	
	
	
	/*
	@Test
	public void getDocumentAsPDFTest() throws URISyntaxException {
		URI exampleURI = new URI("https://john.doe@www.example.com:123/forum/questions/?");
		documentStore.putDocument(input, exampleURI, DocumentFormat.TXT);
		byte[] byteArray = documentStore.getDocumentAsPdf(exampleURI);
		String backToText = documentStore.pdfToText(byteArray);
		assertEquals("testing that a text doc put and retrieved as pdf is accurate", hiMom.trim().replace("\n", " "), backToText);
	}	
	*/
	
	@Test
	public void deleteFalseWhenDoesntExistTest() throws URISyntaxException {
		URI doesNotExist = new URI("https://john.doe@www.example2.com:123/forum/questions/?");
		assertEquals("testing delete returns false if no such URI in the table", false, documentStore.deleteDocument(doesNotExist));
	}	
	
	/*
	@Test
	public void textToPDFandBackTest() throws URISyntaxException {
		URI example3URI = new URI("https://john.doe@www.example3.com:123/forum/questions/?");
		int hash = (example3URI.hashCode() & 0x7fffffff) % 5;
		DocumentImpl newDocument = new DocumentImpl(example3URI, hiMom, hash);
		byte[] asPDF = newDocument.getDocumentAsPdf();
		String asTextAgain = documentStore.pdfToText(asPDF);
		assertEquals("testing that a String is converted to PDF and back smoothly", hiMom.trim().replace("\n", " "), asTextAgain);
	}	
	*/
	
	@Test
	public void putPDFRetrieveAsTXTTest() throws URISyntaxException {
		URI example4URI = new URI("https://john.doe@www.example4.com:123/forum/questions/?");
		int hash = (example4URI.hashCode() & 0x7fffffff) % 5;
		DocumentImpl newDocument = new DocumentImpl(example4URI, hiMom, hash);
		byte[] asPDF = newDocument.getDocumentAsPdf();
		ByteArrayInputStream pdfInputStream = new ByteArrayInputStream(asPDF);
		documentStore.putDocument(pdfInputStream, example4URI, DocumentFormat.PDF);
		String result = documentStore.getDocumentAsTxt(example4URI);
		assertEquals("testing that a String is converted to PDF and back smoothly", hiMom.trim().replace("\n", " "), result);
	}
	
	/*
	@Test
	public void putPDFRetrieveAsPDFTest() throws URISyntaxException {
		URI example4URI = new URI("https://john.doe@www.example4.com:123/forum/questions/?");
		int hash = (example4URI.hashCode() & 0x7fffffff) % 5;
		DocumentImpl newDocument = new DocumentImpl(example4URI, hiMom, hash);
		byte[] asPDF = newDocument.getDocumentAsPdf();
		ByteArrayInputStream pdfInputStream = new ByteArrayInputStream(asPDF);
		documentStore.putDocument(pdfInputStream, example4URI, DocumentFormat.PDF);
		byte[] middleStep = documentStore.getDocumentAsPdf(example4URI);
		String result = documentStore.pdfToText(middleStep);
		assertEquals("testing that a String is converted to PDF and back smoothly", hiMom.trim().replace("\n", " "), result);
	}
	*/
	
	
	@Test
	public void deleteByPuttingNullValueTest() throws URISyntaxException {
		URI example5URI = new URI("https://john.doe@www.example5.com:123/forum/questions/?");
		documentStore.putDocument(input, example5URI, DocumentFormat.TXT);
		documentStore.putDocument(null, example5URI, DocumentFormat.TXT);
		assertEquals("testing that putting with null inputstream deletes the entry with that key", null, documentStore.getDocumentAsPdf(example5URI));
	}
	
	@Test
	public void deleteByPuttingNullValueTest2() throws URISyntaxException {
		URI example5URI = new URI("https://john.doe@www.example5.com:123/forum/questions/?");
		documentStore.putDocument(input, example5URI, DocumentFormat.TXT);
		documentStore.putDocument(null, example5URI, DocumentFormat.TXT);
		assertEquals("testing that putting with null inputstream deletes the entry with that key", null, documentStore.getDocumentAsTxt(example5URI));
	}
	
	@Test
	public void getDocumentPdfNotExistReturnsNullTest() throws URISyntaxException {
		URI example30URI = new URI("https://john.doe@www.example30.com:123/forum/questions/?");
		assertEquals("testing that a getDocAsPdf returns null if no entry exists with that URI", null, documentStore.getDocumentAsPdf(example30URI));
	}
	
	@Test
	public void getDocumentTxtNotExistReturnsNullTest() throws URISyntaxException {
		URI example31URI = new URI("https://john.doe@www.example31.com:123/forum/questions/?");
		assertEquals("testing that a getDocAsTxt returns null if no entry exists with that URI", null, documentStore.getDocumentAsTxt(example31URI));
	}
	
	@Test 
	public void deleteWhenNotExistTest() throws URISyntaxException {
		URI example32URI = new URI("https://john.doe@www.example32.com:123/forum/questions/?");
		assertEquals("testing deletion when not exist from putDoc returns 0", 0, documentStore.putDocument(null, example32URI, DocumentFormat.PDF));
	}
	
	/*
	@Test (expected = IllegalStateException.class)
	 public void nullURIrejected() {
		URI nullURI = null;
		documentStore.putDocument(input, nullURI, DocumentFormat.PDF);
	} 
	*/
	
	

}
