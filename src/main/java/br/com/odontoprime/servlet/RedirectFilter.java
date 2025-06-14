package br.com.odontoprime.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RedirectFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		String uri = req.getRequestURI(); // /OdontoPrime/Login.xhtml

		if (uri.endsWith(".xhtml") && !uri.contains("/resources/")) {
			String jsfUri = uri.replace(".xhtml", ".jsf");

			// Redireciona o navegador para a nova URL
			res.sendRedirect(jsfUri);
			return;
		}

		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {

	}

}
