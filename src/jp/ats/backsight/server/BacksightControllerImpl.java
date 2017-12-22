package jp.ats.backsight.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import jp.ats.backsight.BacksightController;
import jp.ats.backsight.Common;
import jp.ats.backsight.SessionInfo;
import jp.ats.backsight.ThreadInfo;

public class BacksightControllerImpl extends UnicastRemoteObject
	implements BacksightController {

	private static final long serialVersionUID = -3025178704922832744L;

	private final String contextName;

	public BacksightControllerImpl(String contextName) throws RemoteException {
		this.contextName = contextName;
	}

	@Override
	public SessionInfo[] getSessions() {
		return BacksightManager.getSessions(contextName);
	}

	@Override
	public void invalidateSession(String sessionId) {
		BacksightManager.invalidateSession(contextName, sessionId);
	}

	@Override
	public String getAccessControlServerState() {
		return BacksightManager.getAccessControlServerState(contextName);
	}

	@Override
	public boolean isAccessControlServerShutdowned() {
		return BacksightManager.isAccessControlServerShutdowned(contextName);
	}

	@Override
	public void restartAccessControlServer() {
		BacksightManager.restartAccessControlServer(contextName);
	}

	@Override
	public void setAdministratorNames(String[] names) {
		Terminal.getInstance(contextName).setAdministratorNames(names);
	}

	@Override
	public void setConcurrentRequestCount(int count) {
		Terminal.getInstance(contextName).setConcurrentRequestCount(count);
	}

	@Override
	public void setConcurrentSessionCount(int count) {
		Terminal.getInstance(contextName).setConcurrentSessionCount(count);
	}

	@Override
	public void setSessionTimeoutMinutes(int count) {
		Terminal.getInstance(contextName).setSessionTimeoutMinutes(count);
	}

	@Override
	public String[] getAdministratorNames() throws RemoteException {
		return Terminal.getInstance(contextName).getAdministratorNames();
	}

	@Override
	public int getConcurrentRequestCount() throws RemoteException {
		return Terminal.getInstance(contextName).getConcurrentRequestCount();
	}

	@Override
	public int getConcurrentSessionCount() throws RemoteException {
		return Terminal.getInstance(contextName).getConcurrentSessionCount();
	}

	@Override
	public int getSessionTimeoutMinutes() throws RemoteException {
		return Terminal.getInstance(contextName).getSessionTimeoutMinutes();
	}

	@Override
	public int getCurrentConcurrentSessionCount() throws RemoteException {
		return BacksightManager.getSessionCount(contextName);
	}

	@Override
	public int getCurrentConcurrentRequestCount() throws RemoteException {
		return BacksightManager.getCurrentRequestCount(contextName);
	}

	@Override
	public long createLogIterator(String sessionId) {
		return BacksightManager.createLogIterator(contextName, sessionId);
	}

	@Override
	public boolean hasNextLog(long id) {
		return BacksightManager.hasNextLog(id);
	}

	@Override
	public String nextLog(long id) {
		return BacksightManager.nextLog(id);
	}

	@Override
	public ThreadInfo[] getThreads() {
		return BacksightManager.getThreads();
	}

	@Override
	public String getMemoryInfo() {
		Runtime runtime = Runtime.getRuntime();
		return Common.addComma(runtime.freeMemory())
			+ " / "
			+ Common.addComma(runtime.totalMemory());
	}
}
