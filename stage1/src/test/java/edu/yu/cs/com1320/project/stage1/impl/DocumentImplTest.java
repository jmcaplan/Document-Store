package edu.yu.cs.com1320.project.stage1.impl;

import static org.junit.Assert.*;

import java.net.*;

import org.junit.Test;

public class DocumentImplTest {

	@Test (expected = IllegalArgumentException.class)
	public void nullTextTest() throws URISyntaxException {
		URI exampleURI = new URI("https://john.doe@www.example.com:123/forum/questions/?");
		DocumentImpl doc = new DocumentImpl(exampleURI, null, 613);
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void nullURITest() throws URISyntaxException {
		DocumentImpl doc = new DocumentImpl(null, "hello", 613);
	}

}
