package jp.ats.backsight.server;

import static jp.ats.substrate.U.newHashSetOf;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

class LineFactory {

	private final Set<String> paths;

	private final Set<String> excludes;

	LineFactory(String[] passwordSubmitPaths, String[] passwordInputNames) {
		paths = newHashSetOf(passwordSubmitPaths);
		excludes = newHashSetOf(passwordInputNames);
	}

	Line getInstance(HttpServletRequest request) {
		String path = request.getRequestURI();
		if (paths.contains(path)) return new PasswordEntryLine(
			this,
			request.getRemoteAddr(),
			request.getRemoteUser(),
			request.getRequestedSessionId(),
			request.getMethod(),
			path);
		return new Line(
			request.getRemoteAddr(),
			request.getRemoteUser(),
			request.getRequestedSessionId(),
			request.getMethod(),
			path);
	}

	boolean contains(String target) {
		return excludes.contains(target);
	}
}
