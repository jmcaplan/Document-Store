package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.*;
import java.util.Map;
import java.util.HashMap;
import java.util.NoSuchElementException;

public class MinHeapImpl<E extends Comparable> extends MinHeap<E> {
	
	public MinHeapImpl(int size) {
    	this.elements = (E[]) new Comparable[size];
    	this.count = 0; 
    	this.elementsToArrayIndex = new HashMap<>();
    }
	
	public MinHeapImpl() {
		this(10);
	}
	
	public void reHeapify(E element) {
		int index = this.elementsToArrayIndex.get(element);
		this.upHeap(index);
		this.downHeap(index);
		
	}
	
    protected int getArrayIndex(E element) {
    	Integer i = this.elementsToArrayIndex.get(element);
    	if (i != null) return (int) i;
    	throw new NoSuchElementException(
    			"Cannot get array index of this element: "
    			+ "element not found in heap");
    }
    
    protected void doubleArraySize() {
    	E[] newArray = (E[]) new Comparable[this.elements.length*2];
    	for (int i = 0; i < this.elements.length; i++) {
    		newArray[i] = elements[i];
    	}
    	this.elements = newArray;
    }
    
    /**
     * swap the values stored at elements[i] and elements[j]
     */
    @Override
    protected  void swap(int i, int j)
    {
        E temp = this.elements[i];
        this.elements[i] = this.elements[j];
    	this.elementsToArrayIndex.put(elements[i], i);
        this.elements[j] = temp;
    	this.elementsToArrayIndex.put(elements[j], j);
    }
    
    /**
     *while the key at index k is less than its
     *parent's key, swap its contents with its parent’s
     */
    @Override
    protected  void upHeap(int k)
    {
    	while (k > 1 && this.isGreater(k / 2, k))
        {
            this.swap(k, k / 2);
            k = k / 2;
        }
    	this.elementsToArrayIndex.put(elements[k], k);
    }
    
    /**
     * move an element down the heap until it is less than
     * both its children or is at the bottom of the heap
     */
    @Override
    protected  void downHeap(int k)
    {
        while (2 * k <= this.count)
        {
            //identify which of the 2 children are smaller
            int j = 2 * k;
            if (j < this.count && this.isGreater(j, j + 1))
            {
                j++;
            }
            //if the current value is < the smaller child, we're done
            if (!this.isGreater(k, j))
            {
                break;
            }
            //if not, swap and continue testing
            this.swap(k, j);
            k = j;
        }
    	this.elementsToArrayIndex.put(elements[k], k);
    }
    
    @Override
    public E removeMin()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException("Heap is empty");
        }
        E min = this.elements[1];
        //swap root with last, decrement count
        this.swap(1, this.count--);
        //move new root down as needed
        this.downHeap(1);
        this.elements[this.count + 1] = null; //null it to prepare for GC
        this.elementsToArrayIndex.remove(min);
        return min;
    }
    
}
