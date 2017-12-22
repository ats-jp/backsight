package jp.ats.backsight.client;

import java.rmi.RemoteException;

import jp.ats.backsight.BacksightController;

@SuppressWarnings("serial")
public class CurrentSessionCountServlet extends CurrentCountServlet {

	@Override
	protected int getConfigCount(BacksightController controller)
		throws RemoteException {
		return controller.getConcurrentSessionCount();
	}

	@Override
	protected int getCurrentCount(BacksightController controller)
		throws RemoteException {
		return controller.getCurrentConcurrentSessionCount();
	}
}
