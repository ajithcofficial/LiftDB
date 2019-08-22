package main;

import nosqlpkg.*;
import java.util.*;

public class Student implements DBRecord {

	public int rollNo;
	String name;

	String generateName(int size) 
	{
		String in = new String("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
		String out = new String("");
		Random r = new Random();

		for(int i=0;i<size;i++) {
			out += in.charAt(r.nextInt(in.length() -1 ));
		}
		return out;
	}

	public Student(int num) 
	{
		rollNo = num;
		name = generateName(15);
	}


	@Override
	public int createRecord(byte[] out) {
		Converter.integerToBytes(rollNo,0,out);
		Converter.stringToBytes(name,20,4,out);
		return 0;
	}

	@Override
	public void createObject(byte[] in) {
		rollNo = Converter.bytesToInteger(in,0);
		name = Converter.bytesToString(in,4,20);
	}

	@Override
	public void displayRecord() {
		System.out.println();
		System.out.println(" |- Name        : "+ name);
		System.out.println(" |- Roll Number : "+ rollNo);
	}

	@Override
	public int getRecordSize() {
		return 24;
	}

	@Override
	public int getPrimaryKey() {
		return rollNo;
	}

}
