package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class FileUtil {
	public static File openFile(String path) {
		File file = new File(path);
		return file;
	}
	
	public static boolean isExist(File file) {
		if (file.isFile() && file.exists()) {
			return true;
		} else {
			return false;
		}
	}
	
	public static BufferedReader getReader(File file) {
		if (isExist(file)) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				return reader;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
}
