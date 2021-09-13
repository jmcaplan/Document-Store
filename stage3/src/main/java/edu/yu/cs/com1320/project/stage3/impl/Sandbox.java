package edu.yu.cs.com1320.project.stage3.impl;

import java.io.*;
import java.net.*;

import edu.yu.cs.com1320.project.stage3.DocumentStore.DocumentFormat;

public class Sandbox  {
	
	public static void main (String args[]) {
		DocumentStoreImpl ds = new DocumentStoreImpl();
		try {
			String path = "C://Docs//";
			
			String str1 = "FirstYearWriting.txt";
			File file1 = new File(path+str1);
			FileInputStream fis1 = new FileInputStream(file1);
			URI uri1 = new URI("FirstYearWriting.txt");
			ds.putDocument(fis1, uri1, DocumentFormat.TXT);
			
			String str2 = "PersonalNarrativeCoverLetter.txt";
			File file2 = new File(path+str2);
			FileInputStream fis2 = new FileInputStream(file2);
			URI uri2 = new URI("PersonalNarrativeCoverLetter.txt");
			ds.putDocument(fis2, uri2, DocumentFormat.TXT);
			
			String str3 = "guidanceforCriticalInquiryConversation.pdf";
			File file3 = new File(path+str3);
			FileInputStream fis3 = new FileInputStream(file3);
			URI uri3 = new URI("guidanceforCriticalInquiryConversation.pdf");
			ds.putDocument(fis3, uri3, DocumentFormat.PDF);
			
			String str4 = "PersonalNarrativeCoverLetterFinal.pdf";
			File file4 = new File(path+str4);
			FileInputStream fis4 = new FileInputStream(file4);
			URI uri4 = new URI("PersonalNarrativeCoverLetterFinal.pdf");
			ds.putDocument(fis4, uri4, DocumentFormat.PDF);
			
			String word = "cover";
			System.out.println(str1+" "+ds.getDocument(uri1).wordCount(word));
			System.out.println(str2+" "+ds.getDocument(uri2).wordCount(word));
			System.out.println(str3+" "+ds.getDocument(uri3).wordCount(word));
			System.out.println(str4+" "+ds.getDocument(uri4).wordCount(word));
			System.out.println(ds.searchByPrefix(word));
		} 
		catch (FileNotFoundException e) {
			throw new IllegalArgumentException("file wasn't found");		
		}
		catch (URISyntaxException e) {
			throw new IllegalArgumentException("uri was messed up");		
		}
	}
}


