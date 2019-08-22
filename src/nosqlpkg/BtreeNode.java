package nosqlpkg;

public class BtreeNode {
	int inUse;
	private int flagRoot;
	private int flagLeaf;
	private int leaf[] = new int[5];
	private int key[] = new int[4];

	public static final int totalSize = 48;
	public static final int maxCount = 4;

	public BtreeNode(int firstKey) {
		inUse = 1;
		flagRoot = 1;
		flagLeaf = 1;
		leaf[0]  = -1;
		key[0] = firstKey;
		leaf[1] = 0;
	}
	public BtreeNode(int k,int l,int r) {
		inUse = 1;
		flagRoot = 1;
		flagLeaf = 0;
		key[0] = k;
		leaf[0] = l;
		leaf[1] = r;
	}
	public BtreeNode(byte bBuffer[]) {
		inUse = Converter.bytesToInteger(bBuffer,0);
		flagRoot = Converter.bytesToInteger(bBuffer,4);
		flagLeaf = Converter.bytesToInteger(bBuffer,8);

		leaf[0] = Converter.bytesToInteger(bBuffer,12);
		leaf[1] = Converter.bytesToInteger(bBuffer,16);
		leaf[2] = Converter.bytesToInteger(bBuffer,20);
		leaf[3] = Converter.bytesToInteger(bBuffer,24);
		leaf[4] = Converter.bytesToInteger(bBuffer,28);

		key[0] = Converter.bytesToInteger(bBuffer,32);
		key[1] = Converter.bytesToInteger(bBuffer,36);
		key[2] = Converter.bytesToInteger(bBuffer,40);
		key[3] = Converter.bytesToInteger(bBuffer,44);
	}

	public BtreeNode(BtreeNode old,int k,int r) {
		int mid,i,j;
		boolean used = false;

		try{	
			mid = (maxCount/2) + 1;

			old.inUse = mid;
			old.flagRoot = 0;

			inUse = (maxCount+1) - mid;
			flagRoot = 0;
			flagLeaf = old.flagLeaf;
			setLeft(0,-1);

			j = maxCount-1;
			for(i=inUse-1;i>=0;i--) {
				if(!used && (k > old.getKey(j))) {
					setKey(i,k);
					setRight(i,r);
					used = true;
					continue;
				}
				if(k == old.getKey(j)) {
					return;
				}
				setKey(i,old.getKey(j));
				setRight(i,old.getRight(j));
				j--;
			}
			if(used) {
				return;
			}
			for(i=old.inUse-1;i>=0;i--) {
				if( k > old.getKey(j)) {
					old.setKey(i,k);
					old.setRight(i,r);
					return;
				}
				if(k == old.getKey(j)) {
					return;
				}
				old.setKey(i,old.getKey(j));
				old.setRight(i,old.getRight(j));
				j--;
			}
		}
		catch (Exception e) {
			System.out.println(e);
		}

	}

	public int getKey(int p) {
		return key[p];
	}

	public void setKey(int p,int k) {
		key[p] = k;
	}

	public int getLeft(int p) {
		return leaf[p];
	}

	public void setLeft(int p,int r) {
		leaf[p] = r;
	}

	public int getRight(int p) {
		return leaf[p+1];
	}

	public void setRight(int p,int r) {
		leaf[p+1] = r;
	}

	public boolean isRootNode() {
		return (flagRoot==1);
	}	

	public boolean isLeafNode() {
		return (flagLeaf==1);
	}

	public void createIndex(byte bBuffer[]) {
		Converter.integerToBytes(inUse,0,bBuffer);
		Converter.integerToBytes(flagRoot,4,bBuffer);
		Converter.integerToBytes(flagLeaf,8,bBuffer);
		Converter.integerToBytes(leaf[0],12,bBuffer);
		Converter.integerToBytes(leaf[1],16,bBuffer);
		Converter.integerToBytes(leaf[2],20,bBuffer);
		Converter.integerToBytes(leaf[3],24,bBuffer);
		Converter.integerToBytes(leaf[4],28,bBuffer);
		Converter.integerToBytes(key[0],32,bBuffer);
		Converter.integerToBytes(key[1],36,bBuffer);
		Converter.integerToBytes(key[2],40,bBuffer);
		Converter.integerToBytes(key[3],44,bBuffer);
	}

	public void nodeShift(int p) {
		for(int i=inUse; i>p ; i--) {
			setKey(i,getKey(i-1));
			setRight(i,getRight(i-1));
		}
	}
}
