package com.bubbleshooter;

public class MyQueue {
	int size;
	int first , last;
	int[] arr;
	public MyQueue(int MAX_SIZE)
	{
		arr = new int[MAX_SIZE];
		first = last = size = 0;
	}
	public boolean isEmpty()
	{
		return size==0;
	}
	public int poll()
	{
		size--;
		int ret = arr[first];
		first = (first+1)%arr.length;
		return ret;
	}
	public void add(int e)
	{
		size++;
		arr[last] = e;
		last = (last+1)%arr.length;
	}
}
