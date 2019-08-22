package nosqlpkg;

import java.io.*;
import main.Student;

public class Database {
	private int rootIndex;
	private int recordCount;
	private int indexCount;
	private int recordSize;
	private int indexSize;
	private String indexFileName;
	private String recordFileName;
	private DBRecord referenceObject;
	private RandomAccessFile indexRandom;
	private RandomAccessFile recordRandom;

	public Database(DBRecord dbRecord)
	{
		try {
			referenceObject = dbRecord;
			indexSize = BtreeNode.totalSize;
			recordSize = dbRecord.getRecordSize();
			indexFileName = new String( dbRecord.getClass().getSimpleName() + ".index");
			recordFileName = new String( dbRecord.getClass().getSimpleName() + ".data");
			File indexFile = new File(indexFileName);
			File recordFile = new File(recordFileName);
			indexRandom = new RandomAccessFile(indexFile,"rw");
			recordRandom = new RandomAccessFile(recordFile,"rw");
			if(!indexFile.exists() || !recordFile.exists()) {
				rootIndex = -1;
				recordCount = 0;
				indexCount = 0;
				indexFile.delete();
				recordFile.delete();
				indexFile.createNewFile();
				recordFile.createNewFile();
				return;
			}
			byte bBuffer[] = new byte[indexSize];
			for(indexCount = 0; ;indexCount++) {
				indexRandom.seek(indexCount * indexSize);
				if(indexRandom.read(bBuffer) == -1) {
					break;
				}
				BtreeNode bNode = new BtreeNode(bBuffer);
				if(bNode.isRootNode()) {
					rootIndex = indexCount;
				}
			}
			byte bRecord[] = new byte[recordSize];
			for(recordCount = 0; ;recordCount++) {
				recordRandom.seek(recordCount * recordSize);
				if(recordRandom.read(bRecord) == -1) {
					break;
				}
			}

		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void info()
	{
		System.out.println();
		System.out.println(" |- Index File   : " + indexFileName);
		System.out.println(" |- Data File    : " + recordFileName);
		System.out.println(" |- Record size  : " + recordSize);
		System.out.println(" |- Index size   : " + indexSize);
		System.out.println(" |- Record count : " + recordCount);
		System.out.println(" |- Index count  : " + indexCount);
		System.out.println(" |- Root Index   : " + rootIndex);
	}

	int recursiveInsert(int index,int key,DBRecord dbRecord,Arguments arg)
	{
		int  r;
		try {
			int i,j;
			indexRandom.seek(index * indexSize);
			byte bBuffer[] = new byte[indexSize];
			if(indexRandom.read(bBuffer) == -1) {
				return -1;
			}
			BtreeNode bNode = new BtreeNode(bBuffer);
			if(bNode.isLeafNode()) {
				if(bNode.inUse < BtreeNode.maxCount) {
					for(i=0;i<bNode.inUse;i++) {
						if(key == bNode.getKey(i)) {
							return -1;
						}
						if(key < bNode.getKey(i)) {
							bNode.nodeShift(i);
							break;
						}
					}
					bNode.inUse++;
					bNode.setKey(i,key);
					bNode.setRight(i,recordCount);
					indexRandom.seek(index*indexSize);
					bNode.createIndex(bBuffer);
					indexRandom.write(bBuffer);
					recordRandom.seek(recordCount * recordSize);
					byte bRecord[] = new byte[recordSize];
					dbRecord.createRecord(bRecord);
					if(recordCount == 128){
						Student dbr = new Student(1); 
						dbr.createObject(bRecord);
					}
					recordRandom.write(bRecord);
					recordCount++;
					return 0;
				}
				BtreeNode newBtreeNode = new BtreeNode(bNode,key,recordCount);
				indexRandom.seek(index*indexSize);
				bNode.createIndex(bBuffer);
				indexRandom.write(bBuffer);
				indexRandom.seek(indexCount*indexSize);
				newBtreeNode.createIndex(bBuffer);
				indexRandom.write(bBuffer);
				recordRandom.seek(recordCount * recordSize);
				byte bRecord[] = new byte[recordSize];
				dbRecord.createRecord(bRecord);
				recordRandom.write(bRecord);
				arg.key = newBtreeNode.getKey(0);
				arg.right = indexCount;
				recordCount++;
				indexCount++;
				return 1;
			}
			for (i = 0; i < bNode.inUse; i++) {
				if (key < bNode.getKey(i)) {
					break;
				}
				if (key == bNode.getKey(i)) {
					return -1;
				}
			}

			if ((r = recursiveInsert
					(bNode.getLeft(i), key, dbRecord, arg)) == 1) {
				if (bNode.inUse < BtreeNode.maxCount) {
					for (j = 0; j < bNode.inUse; j++) {
						if (arg.key < bNode.getKey(j)) {
							bNode.nodeShift(j);
							break;
						}
					}
					bNode.inUse++;
					bNode.setKey(j,arg.key);
					bNode.setRight(j,arg.right);
					indexRandom.seek(index*indexSize);
					bNode.createIndex(bBuffer);
					indexRandom.write(bBuffer);
					return 0;
				}
				BtreeNode newBtreeNode = new BtreeNode(bNode,arg.key,arg.right);
				indexRandom.seek(index*indexSize);
				bNode.createIndex(bBuffer);
				indexRandom.write(bBuffer);
				indexRandom.seek(indexCount*indexSize);
				newBtreeNode.createIndex(bBuffer);
				indexRandom.write(bBuffer);
				arg.key = newBtreeNode.getKey(0);
				arg.right = indexCount;
				indexCount++;
				return 1;
			}
		}catch (ArrayIndexOutOfBoundsException e) {
			System.out.println(e);
			return -1;
		}catch (FileNotFoundException e) {
			System.out.println(e);
			return -1;
		}catch (IOException e) {
			System.out.println(e);
			return -1;
		}
		return r;
	}

	public int insert(DBRecord dbRecord)
	{
		int r;
		try{
			if(recordSize != dbRecord.getRecordSize()) {
				System.out.println("record size missmatch\n");
				return -1;
			}
			byte record[] = new byte[recordSize];
			dbRecord.createRecord(record);
			if(indexCount == 0) {
				BtreeNode bNode = new BtreeNode(dbRecord.getPrimaryKey());
				byte bBuffer[] = new byte[indexSize];
				bNode.createIndex(bBuffer);
				indexRandom.seek(0);
				recordRandom.seek(0);
				indexRandom.write(bBuffer);
				recordRandom.write(record);
				indexCount++;
				recordCount++;
				rootIndex = 0;
				return 0;
			}

			Arguments arg = new Arguments();
			if((r = recursiveInsert(rootIndex,dbRecord.getPrimaryKey(),dbRecord,arg)) == 1) {
				BtreeNode bNode = new BtreeNode(arg.key,rootIndex,arg.right);
				byte bBuffer[] = new byte[indexSize];
				bNode.createIndex(bBuffer);
				indexRandom.seek(indexCount * indexSize);
				indexRandom.write(bBuffer);
				rootIndex = indexCount;
				indexCount++;
				return 0;
			}

		}catch (ArrayIndexOutOfBoundsException e) {
			System.out.println(e);
			return -1;
		}catch (FileNotFoundException e) {
			System.out.println(e);
			return -1;
		}catch (IOException e) {
			System.out.println(e);
			return -1;
		}
		return r;
	}

	int recursiveSearch(int index,DBRecord dbRecord)
	{
		try {
			int i;
			indexRandom.seek(index * indexSize);
			byte bBuffer[] = new byte[indexSize];
			if(indexRandom.read(bBuffer) == -1) {
				return -1;
			}
			BtreeNode bNode = new BtreeNode(bBuffer);
			if(bNode.isLeafNode()) {
				for(i=0;i<bNode.inUse;i++) {
					if(bNode.getKey(i) == dbRecord.getPrimaryKey()) {
						byte recordBuffer[] = new byte[recordSize];
						recordRandom.seek(bNode.getRight(i) * recordSize);
						recordRandom.read(recordBuffer);
						dbRecord.createObject(recordBuffer);
						return 0;
					}
				}
				return -1;
			}
			for (i = 0; i < bNode.inUse; i++) {
				if (dbRecord.getPrimaryKey() < bNode.getKey(i)) {
					break;
				}
			}
			return recursiveSearch(bNode.getLeft(i),dbRecord);
		}catch (FileNotFoundException e) {
			System.out.println(e);
		}catch (IOException e) {
			System.out.println(e);
			System.out.println("place find 1\n");
		}
		return -1;	
	}

	public int search(DBRecord dbRecord)
	{
		if(recordSize != dbRecord.getRecordSize()) {
			System.out.println("record size missmatch\n");
			return -1;
		}
		return recursiveSearch(rootIndex,dbRecord);
	}

	int recursiveUpdate(int index,DBRecord dbRecord)
	{
		try {
			int i;
			indexRandom.seek(index * indexSize);
			byte bBuffer[] = new byte[indexSize];
			if(indexRandom.read(bBuffer) == -1) {
				return -1;
			}
			BtreeNode bNode = new BtreeNode(bBuffer);
			if(bNode.isLeafNode()) {
				for(i=0;i<bNode.inUse;i++) {
					if(bNode.getKey(i) == dbRecord.getPrimaryKey()) {
						byte recordBuffer[] = new byte[recordSize];
						dbRecord.createRecord(recordBuffer);
						recordRandom.seek(bNode.getRight(i) * recordSize);
						recordRandom.write(recordBuffer);
						return 0;
					}
				}
				return -1;
			}
			for (i = 0; i < bNode.inUse; i++) {
				if (dbRecord.getPrimaryKey() < bNode.getKey(i)) {
					break;
				}
			}
			return recursiveUpdate(bNode.getLeft(i),dbRecord);
		}catch (FileNotFoundException e) {
			System.out.println(e);
		}catch (IOException e) {
			System.out.println("place find 2\n");
			System.out.println(e);
		}
		return -1;	
	}

	public int update(DBRecord dbRecord)
	{
		if(recordSize != dbRecord.getRecordSize()) {
			System.out.println("record size missmatch\n");
			return -1;
		}
		return recursiveUpdate(rootIndex,dbRecord);
	}

	public void recursiveTraverse(int index)
	{ 
		int gl = 0;
		try{
			if(index == -1) {
				return;
			}


			indexRandom.seek(index * indexSize);
			byte indexBuffer[] = new byte[indexSize];
			indexRandom.read(indexBuffer);
			BtreeNode btreeNode = new BtreeNode(indexBuffer);
			int i;
			if(btreeNode.isLeafNode()){
				byte recordBuffer[] = new byte[recordSize];
				for(i=0;i<btreeNode.inUse;i++) {
					recordRandom.seek(btreeNode.getRight(i) * recordSize);
					recordRandom.read(recordBuffer);
					referenceObject.createObject(recordBuffer);
					referenceObject.displayRecord();
				}
				return;
			}
			for(i=0;i<btreeNode.inUse;i++) {
				recursiveTraverse(btreeNode.getLeft(i));
			}
			recursiveTraverse(btreeNode.getLeft(i));

		}catch (FileNotFoundException e) {
			System.out.println(e);
		}catch (IOException e) {
			System.out.println("place find 3 " + index +" " + gl);
			System.out.println(e);
		}
	}

	public int traverse()
	{
		recursiveTraverse(rootIndex);
		return 0;
	}
}
