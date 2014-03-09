package org.psjava.site.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.psjava.ds.Collection;
import org.psjava.ds.array.DynamicArray;

public class ZipUtil {

	public static String loadUTF8StringInZipFileOrNull(File zipFile, String path) throws ZipException, IOException, UnsupportedEncodingException {
		ZipFile z = new ZipFile(zipFile);
		try {
			ZipEntry e = z.getEntry(path);
			if (e != null) {
				InputStream is = z.getInputStream(e);
				try {
					return FileUtil.loadUTF8(is).trim();
				} finally {
					is.close();
				}
			} else {
				return null;
			}
		} finally {
			z.close();
		}
	}

	public static Collection<String> getSubEntries(File zipFile, String prefix) throws ZipException, IOException {
		DynamicArray<String> r = DynamicArray.create();
		ZipFile z = new ZipFile(zipFile);
		try {
			Enumeration<? extends ZipEntry> entries = z.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.getName().startsWith(prefix) && !entry.isDirectory())
					r.addToLast(entry.getName());
			}
		} finally {
			z.close();
		}
		return r;
	}

}
