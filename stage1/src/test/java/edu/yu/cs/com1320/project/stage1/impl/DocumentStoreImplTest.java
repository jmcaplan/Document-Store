package edu.yu.cs.com1320.project.stage1.impl;

import edu.yu.cs.com1320.project.stage1.*;
import edu.yu.cs.com1320.project.stage1.DocumentStore.DocumentFormat;

import static org.junit.Assert.*;
import java.io.*;
import java.net.*;

import org.junit.Test;

public class DocumentStoreImplTest {
	DocumentStoreImpl documentStore = new DocumentStoreImpl();
	String hiMom = "how are you doing \n today great!";
	byte[] buf = hiMom.getBytes();
	ByteArrayInputStream input = new ByteArrayInputStream(buf);
	
	/*
	@Test
	public void readToBytesThenStringTest() {
		String result = new String(documentStore.readIntoByteArray(input));
		assertEquals("testing conversion into byte array", hiMom, result);
	}
	*/
	
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
