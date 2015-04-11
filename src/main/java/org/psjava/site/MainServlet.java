package org.psjava.site;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MainServlet extends HttpServlet {

	private static final String REF_NAME = "psjava-0.1.18";
	private static final String GITHUB_RAW_ROOT = "https://raw.github.com/psjava/psjava/" + REF_NAME;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String path = req.getPathInfo();
//		System.out.println(req.getServletPath() + ":" + req.getContextPath() + ":" + req.getRequestURL() + ":" + req.getPathInfo() + ":");
		if(path.equals("/")) {
			forward(this, req, res, "index.jsp");
		} else if (path.equals("/license")) {
			req.setAttribute("licenseText", CachedHttpClient.getBody(GITHUB_RAW_ROOT + "/LICENSE"));
			forward(this, req, res, "license.jsp");
		} else {
			throw new ServletException("no mapping");
		}

	}

	private static void forward(HttpServlet servlet, HttpServletRequest req, HttpServletResponse res, String jspFileName) throws ServletException, IOException {
		servlet.getServletContext().getRequestDispatcher(req.getContextPath() + "/WEB-INF/" + jspFileName).forward(req, res);
	}

}
