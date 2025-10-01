package jp.ats.backsight.client.action;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jp.ats.backsight.client.Action;
import jp.ats.backsight.client.BacksightFilter;
import jp.ats.backsight.client.persistence.BacksightNode;
import jp.ats.backsight.client.persistence.ContextNode;
import jp.ats.backsight.client.persistence.SiteNode;

public class AddContextAction implements Action {

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response)
		throws ServletException {
		String site = request.getParameter("site");
		String context = request.getParameter("context");
		BacksightNode node = BacksightFilter.getBacksightNode();
		synchronized (node) {
			SiteNode siteNode = node.getSiteNode(site);
			if (siteNode == null) {
				siteNode = new SiteNode(request.getParameter("site"));
				node.addSiteNode(siteNode);
			}

			ContextNode contextNode = siteNode.getContextNode(context);
			if (contextNode == null) {
				contextNode = new ContextNode(context);
				siteNode.addContextNode(contextNode);
			}

			node.store();
		}
	}
}
