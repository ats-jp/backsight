package jp.ats.backsight.server;

import static java.rmi.registry.Registry.REGISTRY_PORT;
import static jp.ats.substrate.U.LINE_SEPARATOR;
import static jp.ats.substrate.U.care;
import static jp.ats.substrate.U.isAvailable;
import static jp.ats.substrate.U.newHashMap;
import static jp.ats.substrate.U.newLinkedList;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import jp.ats.backsight.Common;
import jp.ats.backsight.SessionInfo;
import jp.ats.backsight.ThreadInfo;
import jp.ats.substrate.U;
import jp.ats.webkit.util.Mail;

public class BacksightManager implements HttpSessionListener, Filter {

	//このクラスが複数コンテキストで共用される場合に備えて複数保持できるようにする
	private static final Map<String, Context> contexts = newHashMap();

	private static final Map<Thread, String> requestInfo = newHashMap();

	private static final Map<Long, LogIterator> currentLogIterators = newHashMap();

	private static final int defaultLogInterval = 10000;

	private static Registry registry;

	private static long logIteratorId = 0;

	private Context context;

	private volatile int oneUserSessionCount = 0;

	private volatile boolean redirectToRoot = true;

	private volatile String invalidSessionRedirectPath;

	private volatile String sessionCountOverRedirectPath;

	public static ThreadInfo[] getThreads() {
		Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();

		List<ThreadInfo> result = newLinkedList();

		for (Entry<Thread, StackTraceElement[]> entry : map.entrySet()) {
			Thread thread = entry.getKey();

			StackTraceElement[] elements = entry.getValue();
			String[] stackTrace = new String[elements.length];
			for (int i = 0; i < elements.length; i++) {
				stackTrace[i] = elements[i].toString();
			}

			result.add(new ThreadInfo(
				thread.getName(),
				getRequestInfo(thread),
				stackTrace));
		}

		return result.toArray(new ThreadInfo[result.size()]);
	}

	public static void restartAccessControlServer(String contextName) {
		AccessControlServer server = getContext(contextName).getAccessControlServer();
		if (server == null) return;
		server.restartControlServer();
	}

	public static boolean isAccessControlServerShutdowned(String contextName) {
		AccessControlServer server = getContext(contextName).getAccessControlServer();
		if (server == null) return true;
		return server.isShutdowned();
	}

	public static String getAccessControlServerState(String contextName) {
		AccessControlServer server = getContext(contextName).getAccessControlServer();
		if (server == null) return "";
		return server.getStateInfo();
	}

	public static SessionInfo[] getSessions(String contextName) {
		return getContext(contextName).getSessions();
	}

	public static int getSessionCount(String contextName) {
		return getContext(contextName).getSessionCount();
	}

	public static void invalidateSession(String contextName, String sessionId) {
		getContext(contextName).invalidateSession(sessionId);
	}

	public static long createLogIterator(String contextName, String sessionId) {
		Context context = getContext(contextName);

		HttpSession session = context.getSession(sessionId);

		if (session == null) return -1;

		Logger logger = context.getLogger();
		LogIterator iterator = new LogIterator(
			logger.getDirectory(),
			logger.getPrefix(),
			new Date(session.getCreationTime()),
			sessionId);

		synchronized (currentLogIterators) {
			currentLogIterators.put(++logIteratorId, iterator);
			return logIteratorId;
		}
	}

	public static boolean hasNextLog(long id) {
		synchronized (currentLogIterators) {
			LogIterator iterator = currentLogIterators.get(id);
			if (iterator == null) return false;
			boolean hasNext = iterator.hasNext();
			if (!hasNext) currentLogIterators.remove(id);
			return hasNext;
		}
	}

	public static String nextLog(long id) {
		synchronized (currentLogIterators) {
			return currentLogIterators.get(id).next();
		}
	}

	public static int getCurrentRequestCount(String contextName) {
		return getContext(contextName).getCurrentRequestCount();
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		String host = config.getInitParameter("backsight-host");
		if (!isAvailable(host)) throw new IllegalArgumentException(
			"backsight-host は必須です");

		String contextName = config.getInitParameter("name");
		if (contextName == null) contextName = config.getServletContext()
			.getServletContextName();
		if (!isAvailable(contextName)) throw new IllegalArgumentException(
			"filter/init-param/param-name が name のもの、または context-param/display-name のどちらかが必要です");

		BacksightControllerImpl controller = null;
		synchronized (BacksightManager.class) {
			//複数コンテキストでBacksightManagerを使用する場合、
			//一度しかRMIレジストリの登録及びリモートオブジェクト
			//のバインドをしないようにフラグをチェック
			//コンテキストの再ロード時など、クラスごとリロードされると、
			//遠隔から設定する情報が古いクラスオブジェクトに対して
			//行われてしまい、操作できなくなってしまう
			//そのときのために再度バインドできるように常にフラグをチェックする
			if (registry == null) {
				try {
					try {
						RMISocketFactory factory = new BacksightRMISocketFactory(
							InetAddress.getByName(host));
						registry = LocateRegistry.createRegistry(
							REGISTRY_PORT,
							factory,
							factory);
					} catch (ExportException e) {
						//コンテキストの再ロード時など、クラスがリロードされた場合
						//フラグがリセットされてしまうので、再度RMIレジストリの登録
						//を行ってしまう
						//その場合は仕方がないので、ExportExceptionをキャッチし無視する
					}

					controller = new BacksightControllerImpl(contextName);
					Naming.rebind(
						Common.getRemoteObjectName(contextName),
						controller);
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}
		}

		Terminal terminal = Terminal.getInstance(config.getServletContext()
			.getServletContextName());

		String defaultAdministratorNames = config.getInitParameter("default-administrator-names");
		if (isAvailable(defaultAdministratorNames)) {
			terminal.setAdministratorNames(defaultAdministratorNames.trim()
				.split(" +"));
		}

		String defaultConcurrentSessionCount = config.getInitParameter("default-concurrentsession-count");
		if (isAvailable(defaultConcurrentSessionCount)) {
			terminal.setConcurrentSessionCount(Integer.parseInt(defaultConcurrentSessionCount));
		}

		String defaultConcurrentRequestCount = config.getInitParameter("default-concurrentrequest-count");
		if (isAvailable(defaultConcurrentRequestCount)) {
			terminal.setConcurrentRequestCount(Integer.parseInt(defaultConcurrentRequestCount));
		}

		String defaultSessionTimeoutMinutes = config.getInitParameter("default-sessiontimeout-minutes");
		if (isAvailable(defaultSessionTimeoutMinutes)) {
			terminal.setSessionTimeoutMinutes(Integer.parseInt(defaultSessionTimeoutMinutes));
		}

		String oneUserSessionCount = config.getInitParameter("oneuser-session-count");
		if (isAvailable(oneUserSessionCount)) {
			this.oneUserSessionCount = Integer.parseInt(oneUserSessionCount);
		}

		AccessControlServer server = null;
		String useAccessContorl = config.getInitParameter("use-accesscontrol");
		if (isAvailable(useAccessContorl)) {
			if (Boolean.parseBoolean(useAccessContorl)) {

				String port = config.getInitParameter("accesscontrol-server-port");
				if (!isAvailable(port)) throw new IllegalArgumentException(
					"use-accesscontrol を true とした場合、 accesscontrol-server-port は必須となります");

				server = new AccessControlServer(
					Integer.parseInt(port),
					terminal);
			}
		}

		String redirectToRoot = config.getInitParameter("redirect-to-root");
		if (isAvailable(redirectToRoot)) {
			this.redirectToRoot = Boolean.parseBoolean(redirectToRoot);
		}

		String invalidSessionRedirectPath = care(config.getInitParameter("invalidsession-redirect-path"));
		if (!invalidSessionRedirectPath.startsWith("/")) {
			invalidSessionRedirectPath = "/" + invalidSessionRedirectPath;
		}
		this.invalidSessionRedirectPath = invalidSessionRedirectPath;

		String sessionCountOverRedirectPath = care(config.getInitParameter("sessioncountover-redirect-path"));
		if (!sessionCountOverRedirectPath.startsWith("/")) {
			sessionCountOverRedirectPath = "/" + sessionCountOverRedirectPath;
		}
		this.sessionCountOverRedirectPath = sessionCountOverRedirectPath;

		LineFactory lineFactory = new LineFactory(
			care(config.getInitParameter("password-submit-paths")).split(
				" *, *"),
			care(config.getInitParameter("password-input-names")).split(" *, *"));

		Mail mail = null;
		String exceptionMailHost = config.getInitParameter("exceptionmail-host");
		if (isAvailable(exceptionMailHost)) {
			String exceptionMailUser = config.getInitParameter("exceptionmail-user");
			if (isAvailable(exceptionMailUser)) {
				mail = new Mail(
					exceptionMailHost,
					exceptionMailUser,
					config.getInitParameter("exceptionmail-password"));
			} else {
				mail = new Mail(exceptionMailHost);
			}
			try {
				mail.addMailTo(config.getInitParameter("exceptionmail-address"));
				mail.setFrom(config.getInitParameter("exceptionmail-from"));
			} catch (MessagingException e) {
				throw new IllegalArgumentException(e);
			}
		}

		File logDirectory;
		String logDirectoryParam = config.getInitParameter("log-directory");
		if (isAvailable(logDirectoryParam)) {
			logDirectory = new File(logDirectoryParam);
		} else {
			logDirectory = new File(System.getProperty("java.io.tmpdir"));
		}

		int logInterval;
		String logIntervalParam = config.getInitParameter("log-interval");
		if (isAvailable(logIntervalParam)) {
			logInterval = Integer.parseInt(logIntervalParam);
		} else {
			logInterval = 0;
		}

		String logFilePrefix = config.getInitParameter("logfile-prefix");
		if (!isAvailable(logFilePrefix)) {
			logFilePrefix = contextName;
		}

		Logger logger = new Logger(logInterval <= 0
			? defaultLogInterval
			: logInterval, logDirectory, logFilePrefix);

		String userInfoManagerClass = config.getInitParameter("userinfo-manager-class");
		UserInfoManager userInfoManager = null;
		if (isAvailable(userInfoManagerClass)) {
			try {
				userInfoManager = (UserInfoManager) Class.forName(
					userInfoManagerClass).newInstance();
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}

		Pattern dupricateCheckExcludePattern = null;
		String dupricateCheckExcludePatternParam = config.getInitParameter("dupricatecheck-exclude-pattern");
		if (isAvailable(dupricateCheckExcludePatternParam)) {
			dupricateCheckExcludePattern = Pattern.compile(dupricateCheckExcludePatternParam);
		}

		context = new Context(
			contextName,
			controller,
			server,
			logger,
			terminal,
			userInfoManager,
			lineFactory,
			mail,
			dupricateCheckExcludePattern);

		synchronized (contexts) {
			if (contexts.containsKey(contextName)) throw new IllegalStateException(
				contextName + " は既に使用されています");
			contexts.put(contextName, context);
		}

		terminal.adjustTickets();
	}

	@Override
	public void doFilter(
		ServletRequest request,
		ServletResponse response,
		FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;

		final String username = httpRequest.getRemoteUser();

		AccessControlServer server = context.getAccessControlServer();
		if (server != null) server.checkState(username);

		final HttpSession session = httpRequest.getSession(true);

		final String sessionID = context.adjustSessionID(session);

		session.setMaxInactiveInterval(context.getTerminal()
			.getSessionTimeoutMinutes() * 60);

		final SessionValues sessionValues = SessionValues.prepare(session);

		//ログ用の情報をセットする
		sessionValues.setRemoteUser(username);

		String remoteAddr = httpRequest.getRemoteAddr();
		sessionValues.setRemoteAddr(remoteAddr);

		sessionValues.setCurrentAccessTime(System.currentTimeMillis());

		UserInfoManager userInfoManager = context.getUserInfoManager();
		if (userInfoManager != null) sessionValues.setUserInfo(userInfoManager.getUserInfo(username));

		String uri = httpRequest.getRequestURI();

		if (!context.hasSession(sessionID)) {
			if (invalidSessionRedirectPath != null) {
				request.getRequestDispatcher(invalidSessionRedirectPath)
					.forward(request, response);
				//ここで無効化しないと遷移先のページがセッション外となり表示できない可能性がある
				session.invalidate();
				return;
			}

			session.invalidate();
			throw new InvalidSessionException("セッションは既に無効化されています");
		}

		//ユーザーが存在しないと意味がないチェックなので、ユーザーがあるか確認
		if (isAvailable(username) && oneUserSessionCount > 0) if (!context.checkOneUserSessionCount(
			username,
			sessionID,
			oneUserSessionCount)) {
			if (sessionCountOverRedirectPath != null) {
				try {
					request.getRequestDispatcher(sessionCountOverRedirectPath)
						.forward(request, response);
					return;
				} finally {
					//ここで無効化しないと遷移先のページがセッション外となり表示できない可能性がある
					session.invalidate();
				}
			}

			session.invalidate();
			throw new SessionCountOverException("使用できる最大セッション数 "
				+ oneUserSessionCount
				+ " を超えています");
		}

		boolean excludesDupricateCheck = context.excludesDupricateCheck(uri);

		synchronized (context.getSessionLockKey(sessionID)) {
			//この仕組みで初期化されているか
			if (sessionValues.initialize() && redirectToRoot) {
				//初回ということで / へ強制遷移
				((HttpServletResponse) response).sendRedirect(httpRequest.getContextPath()
					+ "/");
				return;
			}

			if (!excludesDupricateCheck) {
				sessionValues.checkDuplicate(uri);
			}
		}

		Thread thread = Thread.currentThread();
		Line logLine = context.getLineFactory().getInstance(httpRequest);
		try {
			Enumeration<String> names = U.cast(request.getParameterNames());
			while (names.hasMoreElements()) {
				String key = names.nextElement();
				String value = request.getParameter(key);
				logLine.addParameter(key, value);
			}

			context.getLogger().log(logLine.toString());

			synchronized (requestInfo) {
				requestInfo.put(thread, logLine.getFormatString());
			}

			context.incrementRequestCount();

			if (context.getTerminal().getConcurrentRequestCount() > 0) {
				Terminal terminal = context.getTerminal();
				Object ticket = terminal.getTicket();
				try {
					chain.doFilter(request, response);
				} finally {
					terminal.receiveTicket(ticket);
				}
			} else {
				chain.doFilter(request, response);
			}
		} catch (ServletException e) {
			sendExceptionMail(
				httpRequest,
				context,
				logLine.getFormatString(),
				e);
			throw e;
		} catch (IOException e) {
			sendExceptionMail(
				httpRequest,
				context,
				logLine.getFormatString(),
				e);
			throw e;
		} catch (RuntimeException e) {
			sendExceptionMail(
				httpRequest,
				context,
				logLine.getFormatString(),
				e);
			throw e;
		} catch (Error e) {
			sendExceptionMail(
				httpRequest,
				context,
				logLine.getFormatString(),
				e);
			throw e;
		} finally {
			context.decrementRequestCount();

			synchronized (requestInfo) {
				requestInfo.remove(thread);
			}

			if (!excludesDupricateCheck) sessionValues.removeFromDuplicateRequestChecker(uri);
		}
	}

	@Override
	public void destroy() {
		AccessControlServer server = context.getAccessControlServer();
		if (server != null) server.shutdown();

		try {
			Naming.unbind(Common.getRemoteObjectName(context.getName()));
		} catch (Exception e) {}

		try {
			UnicastRemoteObject.unexportObject(context.getController(), true);
		} catch (NoSuchObjectException e) {}

		synchronized (BacksightManager.class) {
			if (registry != null) {
				try {
					UnicastRemoteObject.unexportObject(registry, true);
				} catch (NoSuchObjectException e) {}
				registry = null;
			}
		}
	}

	@Override
	public void sessionCreated(HttpSessionEvent event) {
		HttpSession session = event.getSession();
		if (!getContext(session.getServletContext().getServletContextName()).addSession(
			session)) return;

		SessionValues.prepare(session);
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		HttpSession session = event.getSession();
		//全セッション管理から削除
		getContext(session.getServletContext().getServletContextName()).removeSession(
			session);
	}

	@Override
	protected void finalize() {
		destroy();
	}

	private static void sendExceptionMail(
		HttpServletRequest request,
		Context context,
		String message,
		Throwable t) {
		Mail mail = context.getExceptionMail();
		if (mail == null) return;

		String contextName = context.getName()
			+ " ("
			+ request.getLocalAddr()
			+ ")";
		try {
			mail.setSubject(contextName + " 例外発生通知");

			StringWriter writer = new StringWriter();
			t.printStackTrace(new PrintWriter(writer));

			mail.setMessage("このメールは "
				+ contextName
				+ " から自動配信されています。"
				+ LINE_SEPARATOR
				+ "不要な方は、お手数ですが削除をお願いいたします。"
				+ LINE_SEPARATOR
				+ LINE_SEPARATOR
				+ message
				+ LINE_SEPARATOR
				+ LINE_SEPARATOR
				+ writer.toString());
			mail.send();
		} catch (MessagingException e) {
			throw new IllegalStateException(e);
		}
	}

	private static Context getContext(String contextName) {
		synchronized (contexts) {
			return contexts.get(contextName);
		}
	}

	private static String getRequestInfo(Thread thread) {
		synchronized (requestInfo) {
			String info = requestInfo.get(thread);
			return info == null ? "" : info;
		}
	}
}
