package jp.ats.backsight.server;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpSession;

import jp.ats.substrate.U;

class SessionValues {

	private static final String sessionBindingKey = SessionValues.class.getName();

	private final Set<String> duplicateRequestChecker = new HashSet<String>();

	private String remoteUser;

	private String remoteAddr;

	private long currentAccessTime;

	private boolean initialized;

	private String userInfo;

	static SessionValues prepare(HttpSession session) {
		synchronized (session) {
			SessionValues values = (SessionValues) session.getAttribute(sessionBindingKey);
			if (values == null) {
				values = new SessionValues();
				session.setAttribute(sessionBindingKey, values);
			}
			return values;
		}
	}

	void checkDuplicate(String uri) throws DuplicateRequestException {
		synchronized (duplicateRequestChecker) {
			//今回リクエストされたuriが、現在実行中のものであれば2重リクエストとみなし、例外をスロー
			if (duplicateRequestChecker.contains(uri)) throw new DuplicateRequestException(
				uri);
			//2重リクエストでなければ、チェック用に追加
			duplicateRequestChecker.add(uri);
		}
	}

	void removeFromDuplicateRequestChecker(String uri) {
		synchronized (duplicateRequestChecker) {
			duplicateRequestChecker.remove(uri);
		}
	}

	@Override
	public String toString() {
		return U.toString(this);
	}

	synchronized String getRemoteUser() {
		return remoteUser;
	}

	synchronized void setRemoteUser(String remoteUser) {
		this.remoteUser = remoteUser;
	}

	synchronized String getRemoteAddr() {
		return remoteAddr;
	}

	synchronized void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}

	synchronized long getCurrentAccessTime() {
		return currentAccessTime;
	}

	synchronized void setCurrentAccessTime(long currentAccessTime) {
		this.currentAccessTime = currentAccessTime;
	}

	synchronized boolean initialize() {
		if (initialized) return false;
		initialized = true;
		return true;
	}

	String getUserInfo() {
		return userInfo;
	}

	void setUserInfo(String userInfo) {
		this.userInfo = userInfo;
	}
}
