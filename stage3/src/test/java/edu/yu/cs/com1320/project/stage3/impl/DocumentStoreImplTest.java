package edu.yu.cs.com1320.project.stage3.impl;

import edu.yu.cs.com1320.project.stage3.DocumentStore.DocumentFormat;
import edu.yu.cs.com1320.project.stage3.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage3.impl.DocumentStoreImpl;

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
				docStore.search("number").toString());
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
		docStore.putDocument(new ByteArrayInputStream(str4.getBytes()), new URI("google3.com"), DocumentFormat.TXT);
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
		URI exampleURI = new URI("https://john.doe@www.example201.com:123/forum/questions/?");
		URI exampleURI2 = new URI("https://john.doe@www.example202.com:123/forum/questions/?");
		documentStore.putDocument(input, exampleURI, DocumentFormat.TXT);
		documentStore.putDocument(input2, exampleURI2, DocumentFormat.TXT);
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
