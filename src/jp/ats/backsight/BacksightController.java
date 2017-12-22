package jp.ats.backsight;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BacksightController extends Remote {

	void setAdministratorNames(String[] names) throws RemoteException;

	void setConcurrentSessionCount(int count) throws RemoteException;

	void setConcurrentRequestCount(int count) throws RemoteException;

	void setSessionTimeoutMinutes(int count) throws RemoteException;

	String[] getAdministratorNames() throws RemoteException;

	int getConcurrentSessionCount() throws RemoteException;

	int getConcurrentRequestCount() throws RemoteException;

	int getSessionTimeoutMinutes() throws RemoteException;

	int getCurrentConcurrentSessionCount() throws RemoteException;

	int getCurrentConcurrentRequestCount() throws RemoteException;

	void restartAccessControlServer() throws RemoteException;

	SessionInfo[] getSessions() throws RemoteException;

	void invalidateSession(String sessionId) throws RemoteException;

	String getAccessControlServerState() throws RemoteException;

	boolean isAccessControlServerShutdowned() throws RemoteException;

	long createLogIterator(String sessionId) throws RemoteException;

	boolean hasNextLog(long id) throws RemoteException;

	String nextLog(long id) throws RemoteException;

	ThreadInfo[] getThreads() throws RemoteException;

	String getMemoryInfo() throws RemoteException;
}
