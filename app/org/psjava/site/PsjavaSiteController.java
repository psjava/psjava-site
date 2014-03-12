package org.psjava.site;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.JavadocComment;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import models.Item;

import org.psjava.ds.Collection;
import org.psjava.ds.array.Array;
import org.psjava.ds.array.DynamicArray;
import org.psjava.site.util.StringUtil;
import org.psjava.site.util.Util;
import org.psjava.site.util.ZipUtil;
import org.psjava.util.AssertStatus;
import org.psjava.util.DataKeeper;
import org.psjava.util.Pair;
import org.psjava.util.ZeroTo;

import play.api.Play;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.detail;
import views.html.index;

public class PsjavaSiteController extends Controller {

	private static final String REF_NAME = "master";
	private static final String PATH_PREFIX = "psjava-master/";
	private static final String EXAMPLE_PATH_IN_ZIP = PATH_PREFIX + "src/test/java/org/psjava/example";
	private static final String EXAMPLE_FILE_SUFFIX = "Example.java";
	private static final String DS_PATH_PREFIX = EXAMPLE_PATH_IN_ZIP + "/ds/";
	private static final String ALGO_PATH_PREFIX = EXAMPLE_PATH_IN_ZIP + "/algo/";

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
		for (String pathInZip : subEntries) {
			String name = extractName(pathInZip);
			String id = getId(name);
			r.add(new Item(id, name));
		}
		return r;
	}

	private static String getId(String name) {
		return name.replace(' ', '_');
	}

	public static Result showDs(String id) throws IOException, ParseException {
		return showDetail(DS_PATH_PREFIX, id);
	}

	public static Result showAlgo(String id) throws IOException, ParseException {
		return showDetail(ALGO_PATH_PREFIX, id);
	}

	private static Result showDetail(String pathPrefix, String id) throws ZipException, IOException, UnsupportedEncodingException, ParseException {
		final String content = ZipUtil.loadUTF8StringInZipFileOrNull(getZipFile(), pathPrefix + id.replace("_", "") + EXAMPLE_FILE_SUFFIX);
		AssertStatus.assertTrue(content != null);

		CompilationUnit cu = JavaParser.parse(new ByteArrayInputStream(content.getBytes("UTF-8")), "UTF-8");
		final DynamicArray<String> implementationSimpleClassName = DynamicArray.create();
		final DynamicArray<String> seeAlsoClassName = DynamicArray.create();
		new VoidVisitorAdapter<Object>() {
			@Override
			public void visit(ClassOrInterfaceDeclaration n, Object arg) {
				JavadocComment docOrNull = n.getJavaDoc();
				if (docOrNull == null)
					return;
				for (String line : StringUtil.toLines(docOrNull.getContent())) {
					if (line.contains("@")) {
						if (line.contains("@implementation")) {
							implementationSimpleClassName.addToLast(extractSimpleClassNameFromTagLine(line));
						} else if (line.contains("@see")) {
							seeAlsoClassName.addToLast(extractSimpleClassNameFromTagLine(line));
						} else {
							throw new RuntimeException();
						}
					}
				}
			}
		}.visit(cu, null);
		final DataKeeper<String> exampleBody = new DataKeeper<String>("");
		new VoidVisitorAdapter<Object>() {
			@Override
			public void visit(BlockStmt stmt, Object arg1) {
				String body = "";
				Array<String> lines = StringUtil.toLines(content);
				for (int i : ZeroTo.get(lines.size())) {
					if (stmt.getBeginLine() <= i && i < stmt.getEndLine() - 1) {
						String line = lines.get(i);
						if (!line.trim().startsWith("Assert.assert")) {
							if (line.startsWith("\t\t"))
								body += line.substring(2) + "\n";
							else
								body += line + "\n";
						}
					}
				}
				AssertStatus.assertTrue(exampleBody.get().length() == 0);
				exampleBody.set(body);
			}
		}.visit(cu, null);
		AssertStatus.assertTrue(exampleBody.get().length() > 0);

		DynamicArray<Pair<String, String>> impls = DynamicArray.create();
		for (String s : implementationSimpleClassName) {
			String pathInZip = getEntryPath("/" + s + ".java");
			impls.addToLast(Pair.create(s, "https://github.com/psjava/psjava/blob/" + REF_NAME + "/" + pathInZip.substring(PATH_PREFIX.length())));
		}

		DynamicArray<Pair<String, String>> seeAlsos = DynamicArray.create();
		for (String s : seeAlsoClassName) {
			String pathInZip = getEntryPath("/" + s + ".java");
			String name = extractName(pathInZip);
			String category = pathInZip.substring(EXAMPLE_PATH_IN_ZIP.length(), pathInZip.lastIndexOf('/'));
			String urlPath = category + "/" + getId(name);
			seeAlsos.addToLast(Pair.create(name, urlPath));
		}

		return ok(detail.render(getName(id), exampleBody.get().trim(), Util.toList(impls), Util.toList(seeAlsos)));
	}

	private static String getName(String id) {
		return id.replace('_', ' ');
	}

	private static String getEntryPath(String suffix) throws ZipException, IOException {
		ZipFile z = new ZipFile(getZipFile());
		String pathOrNull = null;
		try {
			Enumeration<? extends ZipEntry> entries = z.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.getName().endsWith(suffix)) {
					AssertStatus.assertTrue(pathOrNull == null);
					pathOrNull = entry.getName();
				}
			}
		} finally {
			z.close();
		}
		AssertStatus.assertTrue(pathOrNull != null);
		return pathOrNull;
	}

	protected static String extractName(String pathInZip) {
		AssertStatus.assertTrue(pathInZip.endsWith(EXAMPLE_FILE_SUFFIX));
		String sub = pathInZip.substring(pathInZip.lastIndexOf('/') + 1, pathInZip.length() - EXAMPLE_FILE_SUFFIX.length());
		return getCamelResolved(sub);
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

	private static String extractSimpleClassNameFromTagLine(String line) {
		String temp = line.substring(line.indexOf("{@link")).trim();
		temp = temp.replace("{@link", "").replace("}", "").trim();
		return temp;
	}

}
