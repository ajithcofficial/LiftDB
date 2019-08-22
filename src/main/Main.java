package main;


import nosqlpkg.*;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
/*
		File indexFile = new File("Student.index");
		File recordFile = new File("Student.data");
		indexFile.delete();
		recordFile.delete();
*/
		Student st = new Student(10);
		Database db = new Database(st);
		db.info();

		for(int i=0;i<100000;i++) {
			st = new Student(i);
			db.insert(st);
		}

		db.info();

				//db.traverse();
				db.search(st);
				st.displayRecord();
	}

}
