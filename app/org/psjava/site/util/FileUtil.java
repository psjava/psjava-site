package org.psjava.site.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class FileUtil {

	public static String loadUTF8(File file) throws IOException {
		FileInputStream is = new FileInputStream(file);
		try {
			return loadUTF8(is);
		} finally {
			is.close();
		}
	}

	public static String loadUTF8(InputStream is) throws IOException, UnsupportedEncodingException {
		byte[] buf = new byte[1024];
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while (true) {
			int read = is.read(buf);
			if (read == -1)
				break;
			bos.write(buf, 0, read);
		}
		return new String(bos.toByteArray(), "UTF-8");
	}

}
