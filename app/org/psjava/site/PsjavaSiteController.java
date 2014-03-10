package org.psjava.site;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.zip.ZipException;

import models.Item;

import org.psjava.ds.Collection;
import org.psjava.site.util.ZipUtil;
import org.psjava.util.AssertStatus;

import play.api.Play;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.detail;
import views.html.index;

public class PsjavaSiteController extends Controller {

	private static final String EXAMPLE_PATH = "psjava-master/src/test/java/org/psjava/example";
	private static final String SUFFIX = "Example.java";
	private static final String DS_PATH_PREFIX = EXAMPLE_PATH + "/ds/";
	private static final String ALGO_PATH_PREFIX = EXAMPLE_PATH + "/algo/";

	public static Result index() throws IOException {
		File zipFile = getZipFile();
		ArrayList<Item> r = extractItems(zipFile, DS_PATH_PREFIX);
		ArrayList<Item> r2 = extractItems(zipFile, ALGO_PATH_PREFIX);
		return ok(index.render(r, r2));
	}

	private static File getZipFile() {
		return Play.getFile("file-resources/psjava-master.zip", Play.current());
	}

	private static ArrayList<Item> extractItems(File zipFile, String pathPrefix) throws ZipException, IOException {
		Collection<String> subEntries = ZipUtil.getSubEntries(zipFile, pathPrefix);
		ArrayList<Item> r = new ArrayList<Item>();
		for (String path : subEntries) {
			String name = extractName(path, pathPrefix);
			r.add(new Item(name.replace(' ', '_'), name));
		}
		return r;
	}

	public static Result showDs(String id) throws IOException {
		return showDetail(DS_PATH_PREFIX, id);
	}

	public static Result showAlgo(String id) throws IOException {
		return showDetail(ALGO_PATH_PREFIX, id);
	}

	private static Result showDetail(String pathPrefix, String id) throws ZipException, IOException, UnsupportedEncodingException {
		String path = pathPrefix + id.replace("_", "") + SUFFIX;
		System.out.println(path);
		String example = ZipUtil.loadUTF8StringInZipFileOrNull(getZipFile(), path);
		AssertStatus.assertTrue(example != null);
		return ok(detail.render(id.replace('_', ' '), example));
	}

	protected static String extractName(String path, String pathPrefix) {
		AssertStatus.assertTrue(path.endsWith(SUFFIX));
		String name = path.substring(pathPrefix.length(), path.length() - SUFFIX.length());
		return getCamelResolved(name);
	}

	public static String getCamelResolved(String name) {
		String r = "";
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (i > 0 && Character.isUpperCase(c))
				r += " ";
			r += c;
		}
		return r;
	}

}
