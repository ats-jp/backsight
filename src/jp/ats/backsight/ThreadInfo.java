package jp.ats.backsight;

import java.io.Serializable;

public class ThreadInfo implements Serializable {

	private static final long serialVersionUID = -4079270755314768830L;

	private final String threadName;

	private final String requestInfo;

	private final String[] stackTrace;

	public ThreadInfo(String threadName, String requestInfo, String[] stackTrace) {
		this.threadName = threadName;
		this.requestInfo = requestInfo;
		this.stackTrace = stackTrace.clone();
	}

	public String getThreadName() {
		return threadName;
	}

	public String getRequestInfo() {
		return requestInfo;
	}

	public String[] getStackTrace() {
		return stackTrace.clone();
	}
}
