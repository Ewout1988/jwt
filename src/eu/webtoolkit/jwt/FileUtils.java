package eu.webtoolkit.jwt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
	public static List<Integer> fileHeader(String fileName, int size) {
		List<Integer> header = new ArrayList<Integer>();
		try {
			InputStream is = new FileInputStream(new File(fileName));
			for (int i = 0; i < size; i++) {
				header.add(is.read());
			}
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return header;
	}

	public static boolean isDirectory(String directory) {
		return new File(directory).isDirectory();
	}

	public static boolean exists(String directory) {
		return new File(directory).exists();
	}

	public static void listFiles(String path, List<String> files) {
		File dir = new File(path);
		
		if (!dir.isDirectory())
			throw new RuntimeException("listFiles: \"" + path + "\" is not a directory");

		for (File f : dir.listFiles())
			files.add(f.getAbsolutePath());
	}

	public static String leaf(String path) {
		return new File(path).getName();
	}
}
