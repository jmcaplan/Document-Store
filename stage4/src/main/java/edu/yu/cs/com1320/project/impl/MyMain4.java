package edu.yu.cs.com1320.project.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MyMain4 {

	public static void main(String[] args) {
		 Method[] methods = MinHeapImpl.class.getDeclaredMethods();
	        int publicMethodCount = 0;
	        for (Method method : methods) {
	            if (Modifier.isPublic(method.getModifiers())) {
	                if (!method.getName().equals("equals") && !method.getName().equals("hashCode")) {
	                    publicMethodCount++;
	                }
	            }
	        }
	        System.out.println(publicMethodCount);
	}

}
