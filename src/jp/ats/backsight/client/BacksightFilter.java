package jp.ats.backsight.client;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.ats.backsight.client.persistence.BacksightNode;
import jp.ats.substrate.U;

public class BacksightFilter implements Filter {

	private static final File tempDirectory = new File(
		System.getProperty("java.io.tmpdir"));

	private static final String xmlFileName = "backsight.xml";

	private static final Pattern pattern = Pattern.compile("([^/]+)\\.do$");

	private static final String packageName = BacksightFilter.class.getPackage()
		.getName();

	private static BacksightNode node;

	static {
		File xmlFile = new File(tempDirectory, xmlFileName);

		try {
			node = new BacksightNode(xmlFile);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void init(FilterConfig config) throws ServletException {}

	@Override
	public void doFilter(
		ServletRequest baseRequest,
		ServletResponse baseResponse,
		FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) baseRequest;
		HttpServletResponse response = (HttpServletResponse) baseResponse;
		String requested = request.getRequestURI();

		Matcher matcher = pattern.matcher(requested);
		if (matcher.find()) {
			Action action = getAction(matcher.group(1));

			try {
				action.execute(request, response);
			} catch (ApplicationException e) {
				throw new ServletException(e);
			}

			String referer = request.getHeader("Referer");
			if (!U.isAvailable(referer)) referer = "/";
			response.sendRedirect(referer);
			return;
		}

		chain.doFilter(baseRequest, baseResponse);
	}

	@Override
	public void destroy() {
		synchronized (node) {
			node.store();
		}
	}

	public static synchronized BacksightNode getBacksightNode() {
		return node;
	}

	private static Action getAction(String name) throws ServletException {
		try {
			return (Action) Class.forName(packageName + ".action." + name)
				.newInstance();
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}
