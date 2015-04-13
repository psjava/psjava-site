package org.psjava.site;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class MainFilter implements Filter {
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) servletRequest;
		String path = req.getRequestURI().substring(req.getContextPath().length());
		if (path.startsWith("/WEB-INF/")) {
			filterChain.doFilter(servletRequest, servletResponse); // Goes to default servlet.
		} else {
			servletRequest.getRequestDispatcher("/servlet" + path).forward(servletRequest, servletResponse);
		}
	}

	@Override
	public void destroy() {
	}
}
