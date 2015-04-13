package org.psjava.site;

import org.json.JSONArray;
import org.psjava.util.AssertStatus;
import org.psjava.util.ZeroTo;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

public class MainServlet extends HttpServlet {

	private static final String REF_NAME = "psjava-0.1.18";
	private static final String GITHUB_RAW_ROOT = "https://raw.github.com/psjava/psjava/" + REF_NAME;
	private static final String ALGO_LIST_URL = constructApiUrl("src/test/java/org/psjava/example/algo");
	private static final String DS_LIST_URL = constructApiUrl("src/test/java/org/psjava/example/ds");
	private static final String EXAMPLE_FILE_SUFFIX = "Example.java";

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

	private String process(String path, AttributeSettable attributeSettable) throws IOException, ServletException {
		if(path.equals("/")) {
			String pomContent = CachedHttpClient.getBody(GITHUB_RAW_ROOT + "/pom.xml");
			String ds = CachedHttpClient.getBody(DS_LIST_URL + "?ref=" + REF_NAME);
			String algo = CachedHttpClient.getBody(ALGO_LIST_URL+ "?ref=" + REF_NAME);
			attributeSettable.set("psjavaVersion", extractVersion(pomContent));
			attributeSettable.set("dsItemList", extractItems(ds));
			attributeSettable.set("algoItemList", extractItems(algo));
			return "index.jsp";
		} else if (path.equals("/license")) {
			attributeSettable.set("licenseText", CachedHttpClient.getBody(GITHUB_RAW_ROOT + "/LICENSE"));
			return "license.jsp";
		} else {
			throw new ServletException("no mapping for " + path);
		}
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

	private static void forward(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res, String jspFileName) throws ServletException, IOException {
		servlet.getServletContext().getRequestDispatcher(req.getContextPath() + "/WEB-INF/" + jspFileName).forward(req, res);
	}

}
