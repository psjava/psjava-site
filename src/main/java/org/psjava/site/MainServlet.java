package org.psjava.site;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.JavadocComment;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import org.json.JSONArray;
import org.psjava.ds.array.Array;
import org.psjava.ds.array.DynamicArray;
import org.psjava.util.AssertStatus;
import org.psjava.util.DataKeeper;
import org.psjava.util.Triple;
import org.psjava.util.ZeroTo;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainServlet extends HttpServlet {

	private static final String REF_NAME = "psjava-0.1.18";
	private static final String GITHUB_RAW_ROOT = "https://raw.github.com/psjava/psjava/" + REF_NAME;
	private static final String ALGO_LIST_URL = constructApiUrl("src/test/java/org/psjava/example/algo");
	private static final String DS_LIST_URL = constructApiUrl("src/test/java/org/psjava/example/ds");
	private static final String EXAMPLE_FILE_SUFFIX = "Example.java";
	private static final String EXAMPLE_PATH = "src/test/java/org/psjava/example";
	private static final String GITHUB_PAGE_ROOT = "https://github.com/psjava/psjava/blob/" + REF_NAME;

	private static String constructApiUrl(String dirPath) {
		return "https://api.github.com/repos/psjava/psjava/contents/" + dirPath;
	}

	@Override
	public void doGet(final HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String jspFileName = process(req.getPathInfo(), new AttributeSettable() {
			@Override
			public void set(String key, Object value) {
				req.setAttribute(key, value);
			}
		});
		forward(this, req, res, jspFileName);
	}

	private static String process(String path, AttributeSettable attributeSettable) throws IOException, ServletException {
		if(path.equals("/")) {
			attributeSettable.set("psjavaVersion", receivePsjavaVersion());
			attributeSettable.set("dsItemList", getDsList());
			attributeSettable.set("algoItemList", getAlgoList());
			return "index.jsp";
		} else if (path.equals("/license")) {
			attributeSettable.set("licenseText", CachedHttpClient.getBody(GITHUB_RAW_ROOT + "/LICENSE"));
			return "license.jsp";
		} else if(path.startsWith("/ds/")) {
			String category = "ds";
			String id = path.substring(category.length()+2);
			return processDetail(attributeSettable, category, id);
		} else {
			throw new ServletException("no mapping for " + path);
		}
	}

	private static ArrayList<Item> getAlgoList() throws IOException {
		return extractItems(CachedHttpClient.getBody(ALGO_LIST_URL + "?ref=" + REF_NAME));
	}

	private static ArrayList<Item> getDsList() throws IOException {
		return extractItems(CachedHttpClient.getBody(DS_LIST_URL + "?ref=" + REF_NAME));
	}

	private static String processDetail(AttributeSettable attributeSettable, String category, String id) throws IOException {
		final String content = CachedHttpClient.getBody(GITHUB_RAW_ROOT + "/" + EXAMPLE_PATH + "/" + category + "/" + id.replace("_", "") + EXAMPLE_FILE_SUFFIX);
		ArrayList<Item> algo = getAlgoList();
		ArrayList<Item> ds = getDsList();

		CompilationUnit cu = null;
		try {
			cu = JavaParser.parse(new ByteArrayInputStream(content.getBytes("UTF-8")), "UTF-8");
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		final DynamicArray<String> implementationClassName = DynamicArray.create();
		final DynamicArray<String> seeAlsoSimpleClassName = DynamicArray.create();
		new VoidVisitorAdapter<Object>() {
			@Override
			public void visit(ClassOrInterfaceDeclaration n, Object arg) {
				JavadocComment docOrNull = n.getJavaDoc();
				if (docOrNull == null)
					return;
				for (String line : StringUtil.toLines(docOrNull.getContent())) {
					if (line.contains("@")) {
						if (line.contains("@implementation")) {
							implementationClassName.addToLast(extractClassNameFromTagLine(line, content));
						} else if (line.contains("@see")) {
							if (line.contains("{@link")) { // TODO this is temporary deal of BinarySearchExample 0.1.17. remove from 0.1.17
								String classNameOrSimple = extractClassNameOrSimpleFromTagLine(line);
								if(classNameOrSimple.contains("."))
									classNameOrSimple = toSimpleClassName(classNameOrSimple);
								seeAlsoSimpleClassName.addToLast(classNameOrSimple);
							}
						} else
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

		DynamicArray<ImplementationLink> impls = DynamicArray.create();
		for (String className : implementationClassName)
			impls.addToLast(new ImplementationLink(toSimpleClassName(className), GITHUB_PAGE_ROOT + "/" + "src/main/java" + "/" + className.replace('.', '/') + ".java"));

		DynamicArray<SeeAlsoLink> seeAlsos = DynamicArray.create();
		for (String s : seeAlsoSimpleClassName) {
			if(s.contains("BinarySearch")) // TODO remove after 0.1.18
				continue;
			String name = extractName(s + ".java");
			Item found = null;
			for (Item item : ds)
				if (item.getName().equals(name))
					found = item;
			if (found != null)
				seeAlsos.addToLast(new SeeAlsoLink(name, "ds", found.getId()));
			Item found2 = null;
			for (Item item : algo)
				if (item.getName().equals(name))
					found2 = item;
			if (found2 != null)
				seeAlsos.addToLast(new SeeAlsoLink(name, "algo", found2.getId()));
		}

		attributeSettable.set("psjavaVersion", receivePsjavaVersion());
		attributeSettable.set("name", getName(id));
		attributeSettable.set("exampleCode", exampleBody.get().trim());
		attributeSettable.set("implementations", Util.toList(impls));
		attributeSettable.set("seeAlsos", Util.toList(seeAlsos));
		return "detail.jsp";
	}

	private static String extractClassNameFromTagLine(String line, String content) {
		String classNameOrSimple = extractClassNameOrSimpleFromTagLine(line);
		String className;
		if(classNameOrSimple.contains("."))
			className = classNameOrSimple;
		else
			className = getClassNameFromImport(content, classNameOrSimple);
		return className;
	}

	private static String getClassNameFromImport(String content, String s) {
		Array<String> lines = StringUtil.toLines(content);
		for (String line : lines)
			if (line.startsWith("import") && line.contains("." + s))
				return line.substring(line.indexOf(' '), line.indexOf(';')).trim();
		throw new RuntimeException("Cannot find class from import statement: " + s);
	}

	private static String extractClassNameOrSimpleFromTagLine(String line) {
		String temp = line.substring(line.indexOf("{@link")).trim();
		temp = temp.replace("{@link", "").replace("}", "").trim();
		return temp;
	}

	private static String receivePsjavaVersion() throws IOException {
		String pomContent = CachedHttpClient.getBody(GITHUB_RAW_ROOT + "/pom.xml");
		return extractVersion(pomContent);
	}

	private static String extractVersion(String pomFileText) {
		String start = "<version>";
		int s = pomFileText.indexOf(start);
		int e = pomFileText.indexOf("</version>");
		return pomFileText.substring(s + "<version>".length(), e);
	}

	private static ArrayList<Item> extractItems(String body) {
		JSONArray dsJson = new JSONArray(body);
		ArrayList<Item> r = new ArrayList<Item>();
		for (int i : ZeroTo.get(dsJson.length())) {
			String name = extractName(dsJson.getJSONObject(i).getString("name"));
			String id = name.replace(' ', '_');
			r.add(new Item(id, name));
		}
		return r;
	}

	private static String toSimpleClassName(String className) {
		return className.substring(className.lastIndexOf('.')+1);
	}

	protected static String extractName(String fileName) {
		AssertStatus.assertTrue(fileName.endsWith(EXAMPLE_FILE_SUFFIX));
		String sub = fileName.substring(0, fileName.length() - EXAMPLE_FILE_SUFFIX.length());
		return getCamelResolved(sub);
	}

	private static String getName(String id) {
		return id.replace('_', ' ');
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

	private static void forward(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res, String jspFileName) throws ServletException, IOException {
		servlet.getServletContext().getRequestDispatcher(req.getContextPath() + "/WEB-INF/" + jspFileName).forward(req, res);
	}

}
