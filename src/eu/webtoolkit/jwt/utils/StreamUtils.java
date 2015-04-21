/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class StreamUtils {
	public static String readFile(String fname) {
		FileInputStream s = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			s = new FileInputStream(fname);
			copy(s, baos);
			return baos.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Could not read file: " + fname, e);
		} catch (IOException e) {
			throw new RuntimeException("Could not read file: " + fname, e);
		} finally {
			if (s != null) {
				try {
					s.close();
				} catch (IOException e) {
					// Ignore that
				}
			}
		}
	}
	
	public static void copy(InputStream is, OutputStream os) throws IOException {
		byte[] buffer = new byte[4096];
		int n = 0;
		while (-1 != (n = is.read(buffer))) {
			os.write(buffer, 0, n);
		}
	}

	public static void closeQuietly(InputStream is) {
		try {
			if (is != null) {
				is.close();
			}
		} catch (IOException ioe) {
			// shhhht!
		}
	}
}