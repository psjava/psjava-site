package org.psjava.site;

import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.JavadocComment;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Item;

import org.json.JSONArray;
import org.psjava.ds.array.Array;
import org.psjava.ds.array.DynamicArray;
import org.psjava.site.util.HttpUtil;
import org.psjava.site.util.StringUtil;
import org.psjava.site.util.Util;
import org.psjava.util.AssertStatus;
import org.psjava.util.DataKeeper;
import org.psjava.util.Pair;
import org.psjava.util.Triple;
import org.psjava.util.ZeroTo;

import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.WS.Response;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.detail;
import views.html.index;

public class PsjavaSiteController extends Controller {

	private static final HashMap<String, String> EMPTY_PARAM = new HashMap<String, String>();
	private static final String ALGO_LIST_URL = constructApiUrl("src/test/java/org/psjava/example/algo");
	private static final String DS_LIST_URL = constructApiUrl("src/test/java/org/psjava/example/ds");
	private static final String EXAMPLE_PATH = "src/test/java/org/psjava/example";
	private static final String REF_NAME = "psjava-0.1.16-SNAPSHOT";
	private static final String GITHUB_RAW_ROOT = "https://raw.github.com/psjava/psjava/" + REF_NAME;
	private static final String GITHUB_PAGE_ROOT = "https://github.com/psjava/psjava/blob/" + REF_NAME;
	private static final String EXAMPLE_FILE_SUFFIX = "Example.java";

	@SuppressWarnings("unchecked")
	public static Promise<Result> index() {
		Promise<Response> pom = HttpUtil.createCacheableUrlFetchPromise(GITHUB_RAW_ROOT + "/pom.xml", EMPTY_PARAM);
		Promise<Response> ds = HttpUtil.createCacheableUrlFetchPromise(DS_LIST_URL, createRefParam());
		Promise<Response> algo = HttpUtil.createCacheableUrlFetchPromise(ALGO_LIST_URL, createRefParam());
		return Promise.sequence(pom, ds, algo).map(new Function<List<Response>, Result>() {
			@Override
			public Result apply(List<Response> list) throws Throwable {
				String pomFileText = list.get(0).getBody();
				String version = extractVersion(pomFileText);
				List<Item> ds = extractItems(list.get(1));
				List<Item> algo = extractItems(list.get(2));
				return ok(index.render(version, ds, algo));
			}
		});
	}

	private static String constructApiUrl(String dirPath) {
		return "https://api.github.com/repos/psjava/psjava/contents/" + dirPath;
	}

	private static String extractVersion(String pomFileText) {
		String start = "<version>";
		int s = pomFileText.indexOf(start);
		int e = pomFileText.indexOf("</version>");
		return pomFileText.substring(s + "<version>".length(), e);
	}

	public static Promise<Result> showDs(String id) {
		return showDetail("ds", id);
	}

	public static Promise<Result> showAlgo(String id) {
		return showDetail("algo", id);
	}

	@SuppressWarnings("unchecked")
	private static Promise<Result> showDetail(String categoryx, final String id) {
		String exampleUrl = GITHUB_RAW_ROOT + "/" + EXAMPLE_PATH + "/" + categoryx + "/" + id.replace("_", "") + EXAMPLE_FILE_SUFFIX;
		Promise<Response> example = HttpUtil.createCacheableUrlFetchPromise(exampleUrl, EMPTY_PARAM);
		Promise<Response> dsList = HttpUtil.createCacheableUrlFetchPromise(DS_LIST_URL, createRefParam());
		Promise<Response> algoList = HttpUtil.createCacheableUrlFetchPromise(ALGO_LIST_URL, createRefParam());
		return Promise.sequence(example, dsList, algoList).map(new Function<List<Response>, Result>() {
			@Override
			public Result apply(List<Response> list) throws Throwable {
				final String content = list.get(0).getBody();
				List<Item> ds = extractItems(list.get(1));
				List<Item> algo = extractItems(list.get(2));

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
								if (line.contains("@implementation"))
									implementationSimpleClassName.addToLast(extractSimpleClassNameFromTagLine(line));
								else if (line.contains("@see"))
									seeAlsoClassName.addToLast(extractSimpleClassNameFromTagLine(line));
								else
									throw new RuntimeException();
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
					String className = getClassNameFromImport(content, s);
					impls.addToLast(Pair.create(s, GITHUB_PAGE_ROOT + "/" + "src/main/java" + "/" + className.replace('.', '/') + ".java"));
				}

				DynamicArray<Triple<String, String, String>> seeAlsos = DynamicArray.create();
				for (String s : seeAlsoClassName) {
					String name = extractName(s + ".java");
					Item found = null;
					for (Item item : ds)
						if (item.name.equals(name))
							found = item;
					if (found != null)
						seeAlsos.addToLast(Triple.create(name, "ds", found.id));
					Item found2 = null;
					for (Item item : algo)
						if (item.name.equals(name))
							found2 = item;
					if (found2 != null)
						seeAlsos.addToLast(Triple.create(name, "algo", found2.id));
				}

				return ok(detail.render(getName(id), exampleBody.get().trim(), Util.toList(impls), Util.toList(seeAlsos)));
			}
		});
	}

	private static Map<String, String> createRefParam() {
		Map<String, String> param = new HashMap<String, String>();
		param.put("ref", REF_NAME);
		return param;
	}

	private static String getName(String id) {
		return id.replace('_', ' ');
	}

	protected static String extractName(String fileName) {
		AssertStatus.assertTrue(fileName.endsWith(EXAMPLE_FILE_SUFFIX));
		String sub = fileName.substring(0, fileName.length() - EXAMPLE_FILE_SUFFIX.length());
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

	private static ArrayList<Item> extractItems(Response gitHubResponse) {
		JSONArray dsJson = new JSONArray(gitHubResponse.getBody());
		ArrayList<Item> r = new ArrayList<Item>();
		for (int i : ZeroTo.get(dsJson.length())) {
			String name = extractName(dsJson.getJSONObject(i).getString("name"));
			String id = name.replace(' ', '_');
			r.add(new Item(id, name));
		}
		return r;
	}

	private static String getClassNameFromImport(String content, String s) {
		Array<String> lines = StringUtil.toLines(content);
		for (String line : lines)
			if (line.startsWith("import") && line.contains("." + s))
				return line.substring(line.indexOf(' '), line.indexOf(';')).trim();
		throw new RuntimeException("Cannot find class from import statement: " + s);
	}

	public static Promise<Result> license() {
		return HttpUtil.createCacheableUrlFetchPromise(GITHUB_RAW_ROOT + "/LICENSE", EMPTY_PARAM).map(new Function<Response, Result>() {
			@Override
			public Result apply(Response arg0) throws Throwable {
				return ok(views.html.license.render(arg0.getBody()));
			}
		});
	}

}
