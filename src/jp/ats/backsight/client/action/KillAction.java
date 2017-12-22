package jp.ats.backsight.client.action;

import java.rmi.RemoteException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.ats.backsight.BacksightController;
import jp.ats.backsight.SessionInfo;
import jp.ats.backsight.client.Action;
import jp.ats.backsight.client.ApplicationException;
import jp.ats.backsight.client.BacksightClient;

public class KillAction implements Action {

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response)
		throws ApplicationException, ServletException {
		try {
			BacksightController controller = BacksightClient.getController(request.getSession());
			String killId = request.getParameter("kill");
			if (killId != null && !killId.equals("")) {
				SessionInfo[] sessions = controller.getSessions();
				for (int i = 0; i < sessions.length; i++) {
					synchronized (sessions[i]) {
						if (sessions[i].getId().equals(killId)) {
							controller.invalidateSession(killId);
							break;
						}
					}
				}
			}
		} catch (RemoteException e) {
			throw new ServletException(e);
		}
	}
}
