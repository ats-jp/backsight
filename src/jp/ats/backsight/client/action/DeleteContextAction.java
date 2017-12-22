package jp.ats.backsight.client.action;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.ats.backsight.client.Action;
import jp.ats.backsight.client.ApplicationException;
import jp.ats.backsight.client.BacksightFilter;
import jp.ats.backsight.client.persistence.BacksightNode;
import jp.ats.backsight.client.persistence.SiteNode;

public class DeleteContextAction implements Action {

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response)
		throws ApplicationException, ServletException {
		String site = request.getParameter("site");
		String context = request.getParameter("context");
		BacksightNode node = BacksightFilter.getBacksightNode();
		synchronized (node) {
			SiteNode siteNode = node.getSiteNode(site);
			if (siteNode == null) return;

			siteNode.deleteContextNode(context);

			if (siteNode.getContextNodes().length == 0) node.deleteSiteNode(site);

			node.store();
		}
	}
}
