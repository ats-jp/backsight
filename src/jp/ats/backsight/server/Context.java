package jp.ats.backsight.server;

import static jp.ats.substrate.U.care;
import static jp.ats.substrate.U.cast;
import static jp.ats.substrate.U.isAvailable;
import static jp.ats.substrate.U.newHashMap;
import static jp.ats.substrate.U.newHashSet;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import javax.servlet.http.HttpSession;

import jp.ats.backsight.SessionInfo;
import jp.ats.substrate.util.CollectionMap;
import jp.ats.webkit.util.Mail;

class Context {

	private static final String originalSessionIDKey = Context.class.getName()
		+ ".originalSessionIDKey";

	private static final String format = "yyyy/MM/dd HH:mm:ss";

	private static final long startTime = System.currentTimeMillis();

	private final String name;

	private final BacksightControllerImpl controller;

	private final Map<String, SessionContainer> sessions = newHashMap();

	private final AccessControlServer server;

	private final Logger logger;

	private final Terminal terminal;

	private final UserInfoManager userInfoManager;

	private final LineFactory lineFactory;

	private final Mail exceptionMail;

	private final Pattern dupricateCheckExcludePattern;

	private final CollectionMap<String, String> userSessionCountChecker = new CollectionMap<String, String>() {

		@Override
		protected Collection<String> createNewCollection() {
			return newHashSet();
		}
	};

	private final AtomicInteger currentRequestCount = new AtomicInteger();

	Context(
		String name,
		BacksightControllerImpl controller,
		AccessControlServer server,
		Logger logger,
		Terminal terminal,
		UserInfoManager userInfoManager,
		LineFactory lineFactory,
		Mail exceptionMail,
		Pattern dupricateCheckExcludePattern) {
		this.name = name;
		this.controller = controller;
		this.server = server;
		this.logger = logger;
		this.terminal = terminal;
		this.userInfoManager = userInfoManager;
		this.lineFactory = lineFactory;
		this.exceptionMail = exceptionMail;
		this.dupricateCheckExcludePattern = dupricateCheckExcludePattern;
	}

	String getName() {
		return name;
	}

	BacksightControllerImpl getController() {
		return controller;
	}

	Object getSessionLockKey(String sessionID) {
		synchronized (sessions) {
			return sessions.get(sessionID);
		}
	}

	//Tomcatのsession fixation対策で、セッションIDが切り替わる仕様に対応
	String adjustSessionID(HttpSession session) {
		//セッションID切り替え処理全体をロックするため、ロックをsessionsで行う
		synchronized (sessions) {
			String originalSessionID = (String) session.getAttribute(originalSessionIDKey);
			String currentSessionID = session.getId();

			if (currentSessionID.equals(originalSessionID)) return currentSessionID;

			session.setAttribute(originalSessionIDKey, currentSessionID);

			SessionContainer container = sessions.remove(originalSessionID);

			if (container != null) {
				container.changeID(currentSessionID);
				sessions.put(currentSessionID, container);
			}

			return currentSessionID;
		}
	}

	boolean hasSession(String sessionID) {
		synchronized (sessions) {
			return sessions.containsKey(sessionID);
		}
	}

	boolean addSession(HttpSession session) {
		int concurrentSessionCount = terminal.getConcurrentSessionCount();
		synchronized (sessions) {
			if (concurrentSessionCount > 0
				&& sessions.size() >= concurrentSessionCount) return false;

			String sessionID = session.getId();

			session.setAttribute(originalSessionIDKey, sessionID);

			sessions.put(sessionID, new SessionContainer(session));
		}

		return true;
	}

	boolean checkOneUserSessionCount(
		String user,
		String sessionID,
		int sessionCount) {
		//ログイン前にアクセスされた場合、ここではじかないと
		//同じセッションで今度はログインされた場合、ユーザーなしとありで
		//同じセッションが存在することになってしまう
		if (!isAvailable(user)) return false;
		synchronized (userSessionCountChecker) {
			Set<String> sessions = (Set<String>) userSessionCountChecker.get(user);

			if (sessions.contains(sessionID)) {
				return true;
			}

			if (sessions.size() < sessionCount) {
				sessions.add(sessionID);
				return true;
			}

			return false;
		}
	}

	void removeSession(HttpSession session) {
		String sessionID = session.getId();
		synchronized (sessions) {
			sessions.remove(sessionID);
		}

		synchronized (userSessionCountChecker) {
			for (Entry<String, Collection<String>> entry : userSessionCountChecker.getInnerMap()
				.entrySet()) {
				Set<String> userSessions = (Set<String>) entry.getValue();
				if (userSessions.remove(sessionID)) {
					if (userSessions.size() == 0) userSessionCountChecker.remove(entry.getKey());
					break;
				}
			}
		}
	}

	HttpSession getSession(String sessionID) {
		synchronized (sessions) {
			SessionContainer container = sessions.get(sessionID);
			return container == null ? null : container.getSession();
		}
	}

	void invalidateSession(String sessionID) {
		synchronized (sessions) {
			SessionContainer container = sessions.get(sessionID);
			if (container == null) return;
			container.invalidate();
			sessions.remove(sessionID);
		}
	}

	SessionInfo[] getSessions() {
		List<SessionContainer> containers;
		synchronized (sessions) {
			containers = new LinkedList<SessionContainer>(sessions.values());
		}

		Collections.sort(containers);

		SessionInfo[] result = new SessionInfo[containers.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = containers.get(i).createSessionInfo();
		}

		return result;
	}

	int getSessionCount() {
		synchronized (sessions) {
			return sessions.size();
		}
	}

	boolean excludesDupricateCheck(String uri) {
		if (dupricateCheckExcludePattern == null) return false;
		return dupricateCheckExcludePattern.matcher(uri).matches();
	}

	AccessControlServer getAccessControlServer() {
		return server;
	}

	Logger getLogger() {
		return logger;
	}

	Terminal getTerminal() {
		return terminal;
	}

	UserInfoManager getUserInfoManager() {
		return userInfoManager;
	}

	LineFactory getLineFactory() {
		return lineFactory;
	}

	Mail getExceptionMail() {
		return exceptionMail;
	}

	void incrementRequestCount() {
		currentRequestCount.incrementAndGet();
	}

	void decrementRequestCount() {
		currentRequestCount.decrementAndGet();
	}

	int getCurrentRequestCount() {
		return currentRequestCount.intValue();
	}

	private static Object getAttribute(HttpSession session, String key) {
		synchronized (session) {
			return session.getAttribute(key);
		}
	}

	public class SessionContainer implements Comparable<SessionContainer> {

		private String id;

		private final WeakReference<HttpSession> sessionReference;

		private SessionContainer(HttpSession session) {
			id = session.getId();
			sessionReference = new WeakReference<HttpSession>(session);
		}

		@Override
		public int compareTo(SessionContainer another) {
			return (int) (another.getOrder() - getOrder());
		}

		synchronized void invalidate() {
			try {
				getSession().invalidate();
			} catch (IllegalStateException e) {}
		}

		public synchronized SessionInfo createSessionInfo() {
			String creationTime;
			String lastAccessedTime;
			String sessionInactiveTime;
			String stayMinutes;
			String remoteUser;
			String remoteAddr;
			String userInfo;
			LinkedHashMap<String, String> attributes = new LinkedHashMap<String, String>();
			try {
				HttpSession session = getSession();

				SimpleDateFormat formatter = new SimpleDateFormat(format);

				creationTime = formatTimestamp(
					formatter,
					session.getCreationTime());

				stayMinutes = Long.toString((System.currentTimeMillis() - session.getCreationTime()) / 1000 / 60);

				SessionValues sessionValues = SessionValues.prepare(session);

				remoteUser = sessionValues.getRemoteUser();
				remoteAddr = sessionValues.getRemoteAddr();
				userInfo = care(sessionValues.getUserInfo());

				long rawCurrentAccessTime = sessionValues.getCurrentAccessTime();

				lastAccessedTime = formatTimestamp(
					formatter,
					rawCurrentAccessTime);

				sessionInactiveTime = formatTimestamp(
					formatter,
					rawCurrentAccessTime
						+ session.getMaxInactiveInterval()
						* 1000);

				Enumeration<String> enumeration;
				synchronized (session) {
					enumeration = cast(session.getAttributeNames());
				}
				while (enumeration.hasMoreElements()) {
					String name = enumeration.nextElement();
					Object value = getAttribute(session, name);
					attributes.put(name, value == null ? "" : value.toString());
				}
			} catch (Exception e) {
				creationTime = "";
				lastAccessedTime = "";
				sessionInactiveTime = "";
				stayMinutes = "";
				remoteUser = "";
				remoteAddr = "";
				userInfo = "";
				attributes.clear();
			}

			return new SessionInfo(
				id(),
				creationTime,
				lastAccessedTime,
				sessionInactiveTime,
				stayMinutes,
				remoteUser,
				remoteAddr,
				userInfo,
				attributes);
		}

		synchronized File getLogDirectory() {
			return logger.getDirectory();
		}

		synchronized String getLogFilePrefix() {
			return logger.getPrefix();
		}

		private synchronized void changeID(String newID) {
			id = newID;
		}

		private synchronized String id() {
			return id;
		}

		private HttpSession getSession() {
			HttpSession session = sessionReference.get();
			if (session == null) {
				synchronized (sessions) {
					sessions.remove(id());
				}
				throw new IllegalStateException();
			}
			return session;
		}

		private synchronized long getOrder() {
			HttpSession session = sessionReference.get();
			if (session == null) return 0L;
			try {
				return SessionValues.prepare(session).getCurrentAccessTime();
			} catch (Exception e) {
				return 0L;
			}
		}

		private String formatTimestamp(SimpleDateFormat formatter, long time) {
			if (time < startTime) return "";
			return formatter.format(new Date(time));
		}
	}
}
