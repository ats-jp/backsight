package jp.ats.backsight.client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import jp.ats.backsight.BacksightController;
import jp.ats.backsight.Common;
import jp.ats.backsight.client.persistence.BacksightNode;
import jp.ats.backsight.client.persistence.ContextNode;
import jp.ats.backsight.client.persistence.SiteNode;
import jp.ats.substrate.U;

public class BacksightClient {

	private static final String currentSiteKey = BacksightClient.class.getName()
		+ ".currentSite";

	private static final String currentContextKey = BacksightClient.class.getName()
		+ ".currentContext";

	public static void init(HttpServletRequest request) {
		HttpSession session = request.getSession();
		session.setAttribute(currentSiteKey, request.getParameter("site"));
		session.setAttribute(currentContextKey, request.getParameter("context"));
	}

	public static boolean hasCurrent(HttpSession session) {
		return session.getAttribute(currentSiteKey) != null;
	}

	public static String getCurrentSite(HttpSession session) {
		return U.care((String) session.getAttribute(currentSiteKey));
	}

	public static String getCurrentContext(HttpSession session) {
		return U.care((String) session.getAttribute(currentContextKey));
	}

	public static String getSignature(HttpSession session) {
		return getCurrentSite(session) + " / " + getCurrentContext(session);
	}

	public static void clearCurrentContext(HttpSession session) {
		session.removeAttribute(currentSiteKey);
		session.removeAttribute(currentContextKey);
	}

	public static BacksightController getController(HttpSession session)
		throws ApplicationException {
		if (session == null) throw new ApplicationException("不正なアクセスです");

		String site = getCurrentSite(session);
		String context = getCurrentContext(session);
		if (!U.isAllValuesAvailable(site, context)) {
			throw new ApplicationException("不正なアクセスです");
		}

		try {
			return getController(site, context);
		} catch (NotBoundException e) {
			e.printStackTrace();
			throw new ApplicationException(site
				+ " には接続できましたが、 "
				+ context
				+ " が見つかりませんでした");
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new ApplicationException(site
				+ " が起動していないか、ネットワークのエラーが発生しています");
		}
	}

	public static BacksightController getControllerWithoutException(
		String site,
		String context) {
		try {
			return getController(site, context);
		} catch (NotBoundException e) {
			return null;
		} catch (RemoteException e) {
			return null;
		}
	}

	public static String[] getAdministratorNames(HttpSession session) {
		BacksightNode node = BacksightFilter.getBacksightNode();
		synchronized (node) {
			ContextNode context = getContextNode(session);
			if (context == null) return U.STRING_EMPTY_ARRAY;

			return context.getAdministratorNames();
		}
	}

	public static int getConcurrentSessionCount(HttpSession session) {
		BacksightNode node = BacksightFilter.getBacksightNode();
		synchronized (node) {
			ContextNode context = getContextNode(session);
			if (context == null) return 0;

			return context.getConcurrentSessionCount();
		}
	}

	public static int getConcurrentRequestCount(HttpSession session) {
		BacksightNode node = BacksightFilter.getBacksightNode();
		synchronized (node) {
			ContextNode context = getContextNode(session);
			if (context == null) return 0;

			return context.getConcurrentRequestCount();
		}
	}

	public static int getSessionTimeoutMinutes(HttpSession session) {
		BacksightNode node = BacksightFilter.getBacksightNode();
		synchronized (node) {
			ContextNode context = getContextNode(session);
			if (context == null) return 0;

			return context.getSessionTimeoutMinutes();
		}
	}

	private static ContextNode getContextNode(HttpSession session) {
		String site = BacksightClient.getCurrentSite(session);
		String context = BacksightClient.getCurrentContext(session);
		BacksightNode node = BacksightFilter.getBacksightNode();
		SiteNode siteNode = node.getSiteNode(site);
		if (siteNode == null) return null;

		ContextNode contextNode = siteNode.getContextNode(context);
		if (contextNode == null) return null;

		return contextNode;
	}

	private static BacksightController getController(
		String siteAddress,
		String contextName) throws NotBoundException, RemoteException {
		try {
			return (BacksightController) Naming.lookup("rmi://"
				+ siteAddress
				+ "/"
				+ Common.getRemoteObjectName(contextName));
		} catch (MalformedURLException e) {
			throw new IllegalStateException(e);
		}
	}
}
