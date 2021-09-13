package edu.yu.cs.com1320.project.impl;

import static org.junit.Assert.*;

import org.junit.Test;

public class MinHeapImplTest {
	
	@Test
	public void test() {
		MinHeapImpl<NumberHolder> mh = new MinHeapImpl<>();
		NumberHolder six = new NumberHolder(6);
		NumberHolder twelve = new NumberHolder(12);
		NumberHolder eighteen = new NumberHolder(18);
		NumberHolder twentyFour = new NumberHolder(24);
		NumberHolder thirty = new NumberHolder(30);
		mh.insert(twelve);
		mh.insert(twentyFour);
		mh.insert(six);
		mh.insert(eighteen);
		mh.insert(thirty);
		assertEquals("making sure our state is correct", true,
				mh.getArrayIndex(six)==1 &&
				mh.getArrayIndex(twelve)/2==1 &&
				mh.getArrayIndex(eighteen)/2 == 1 &&
				mh.getArrayIndex(twentyFour)/2 == 2 &&
				mh.getArrayIndex(thirty)/2 == 2
			);
		System.out.println("6 is at: "+mh.getArrayIndex(six));
		System.out.println("12 is at: "+mh.getArrayIndex(twelve));
		System.out.println("18 is at: "+mh.getArrayIndex(eighteen));
		System.out.println("24 is at: "+mh.getArrayIndex(twentyFour));
		System.out.println("30 is at: "+mh.getArrayIndex(thirty));
		
		assertEquals("testing simple removal", 6, (int)mh.removeMin().number);
		System.out.println("\nAfter removing 6...\n");
		System.out.println("12 is at: "+mh.getArrayIndex(twelve));
		System.out.println("18 is at: "+mh.getArrayIndex(eighteen));
		System.out.println("24 is at: "+mh.getArrayIndex(twentyFour));
		System.out.println("30 is at: "+mh.getArrayIndex(thirty));
		
		eighteen.setNumber(36);
		mh.reHeapify(eighteen);
		System.out.println("\nAfter changing 18 to 36 and reHeapifying...\n");
		System.out.println("12 is at: "+mh.getArrayIndex(twelve));
		System.out.println("36 nee 18 is at: "+mh.getArrayIndex(eighteen));
		System.out.println("24 is at: "+mh.getArrayIndex(twentyFour));
		System.out.println("30 is at: "+mh.getArrayIndex(thirty));
		assertEquals("testing a simple reHeapify", 4, mh.getArrayIndex(eighteen));
		assertEquals("testing a simple reHeapify", 2, mh.getArrayIndex(twentyFour));
		
	}

}
