package org.psjava.site;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipException;

import models.Item;

import org.psjava.ds.Collection;
import org.psjava.site.util.ZipUtil;
import org.psjava.util.AssertStatus;
import org.psjava.util.DataKeeper;
import org.psjava.util.ZeroTo;

import play.api.Play;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.detail;
import views.html.index;

public class PsjavaSiteController extends Controller {

	private static final String PATH_PREFIX = "psjava-master/";
	private static final String EXAMPLE_PATH = PATH_PREFIX + "src/test/java/org/psjava/example";
	private static final String SUFFIX = "Example.java";
	private static final String DS_PATH_PREFIX = EXAMPLE_PATH + "/ds/";
	private static final String ALGO_PATH_PREFIX = EXAMPLE_PATH + "/algo/";

	public static Result index() throws IOException {
		File zipFile = getZipFile();
		String version = extractVersionFromFile(zipFile);
		List<Item> ds = extractItems(zipFile, DS_PATH_PREFIX);
		List<Item> algo = extractItems(zipFile, ALGO_PATH_PREFIX);
		return ok(index.render(version, ds, algo));
	}

	private static String extractVersionFromFile(File zipFile) throws ZipException, IOException, UnsupportedEncodingException {
		String text = ZipUtil.loadUTF8StringInZipFileOrNull(zipFile, PATH_PREFIX + "pom.xml");
		String start = "<version>";
		int s = text.indexOf(start);
		int e = text.indexOf("</version>");
		return text.substring(s + "<version>".length(), e);
	}

	private static File getZipFile() {
		return Play.getFile("file-resources/psjava-master.zip", Play.current());
	}

	private static List<Item> extractItems(File zipFile, String pathPrefix) throws ZipException, IOException {
		Collection<String> subEntries = ZipUtil.getSubEntries(zipFile, pathPrefix);
		ArrayList<Item> r = new ArrayList<Item>();
		for (String path : subEntries) {
			String name = extractName(path, pathPrefix);
			r.add(new Item(name.replace(' ', '_'), name));
		}
		return r;
	}

	public static Result showDs(String id) throws IOException, ParseException {
		return showDetail(DS_PATH_PREFIX, id);
	}

	public static Result showAlgo(String id) throws IOException, ParseException {
		return showDetail(ALGO_PATH_PREFIX, id);
	}

	private static Result showDetail(String pathPrefix, String id) throws ZipException, IOException, UnsupportedEncodingException, ParseException {
		String path = pathPrefix + id.replace("_", "") + SUFFIX;
		final String content = ZipUtil.loadUTF8StringInZipFileOrNull(getZipFile(), path);
		AssertStatus.assertTrue(content != null);

		final DataKeeper<String> keeper = new DataKeeper<String>("");
		CompilationUnit cu = JavaParser.parse(new ByteArrayInputStream(content.getBytes("UTF-8")), "UTF-8");
		new VoidVisitorAdapter<Object>() {
			@SuppressWarnings("unused")
			@Override
			public void visit(BlockStmt stmt, Object arg1) {
				String body = "";
				Scanner in = new Scanner(content);
				for (int i : ZeroTo.get(stmt.getBeginLine()))
					in.nextLine();
				for (int i : ZeroTo.get(Math.max(stmt.getEndLine() - stmt.getBeginLine() - 1, 0))) {
					String line = in.nextLine();
					if (!line.trim().startsWith("Assert.assert"))
						if (line.startsWith("\t\t"))
							body += line.substring(2) + "\n";
						else
							body += line + "\n";
				}
				keeper.set(body);
				in.close();
			}
		}.visit(cu, null);
		return ok(detail.render(id.replace('_', ' '), keeper.get()));
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
