package jp.ats.backsight.client.action;

import java.rmi.RemoteException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.ats.backsight.BacksightController;
import jp.ats.backsight.client.Action;
import jp.ats.backsight.client.ApplicationException;
import jp.ats.backsight.client.BacksightClient;
import jp.ats.backsight.client.BacksightFilter;
import jp.ats.backsight.client.persistence.BacksightNode;
import jp.ats.backsight.client.persistence.ContextNode;
import jp.ats.backsight.client.persistence.SiteNode;
import jp.ats.substrate.U;

public class ControlAction implements Action {

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response)
		throws ApplicationException, ServletException {
		BacksightController controller = BacksightClient.getController(request.getSession());

		request.getParameter("administratorNames");

		String[] administratorNames = U.care(
			request.getParameter("administratorNames")).split(" +");
		int concurrentSessionCount = Integer.parseInt(request.getParameter("concurrentSessionCount"));
		int concurrentRequestCount = Integer.parseInt(request.getParameter("concurrentRequestCount"));
		int sessionTimeoutMinutes = Integer.parseInt(request.getParameter("sessionTimeoutMinutes"));

		HttpSession session = request.getSession();
		String site = BacksightClient.getCurrentSite(session);
		String context = BacksightClient.getCurrentContext(session);
		BacksightNode node = BacksightFilter.getBacksightNode();
		synchronized (node) {
			SiteNode siteNode = node.getSiteNode(site);
			if (siteNode == null) return;

			ContextNode contextNode = siteNode.getContextNode(context);
			if (contextNode == null) return;

			contextNode.setAdministratorNames(administratorNames);
			contextNode.setConcurrentSessionCount(concurrentSessionCount);
			contextNode.setConcurrentRequestCount(concurrentRequestCount);
			contextNode.setSessionTimeoutMinutes(sessionTimeoutMinutes);

			node.store();
		}

		try {
			//administratorNamesだけは入力検査しないので、万が一nullであった場合を考慮
			controller.setAdministratorNames(administratorNames);
			controller.setConcurrentSessionCount(concurrentSessionCount);
			controller.setConcurrentRequestCount(concurrentRequestCount);
			controller.setSessionTimeoutMinutes(sessionTimeoutMinutes);
		} catch (RemoteException e) {
			throw new ApplicationException("リモート接続で障害が発生しました ["
				+ e.getMessage()
				+ "]");
		}
	}
}
