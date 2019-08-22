package nosqlpkg;

public interface DBRecord {
	abstract public int createRecord(byte out[]);
	abstract public void createObject(byte in[]);
	abstract public void displayRecord();
	abstract public int getRecordSize();
	abstract public int getPrimaryKey();
}
