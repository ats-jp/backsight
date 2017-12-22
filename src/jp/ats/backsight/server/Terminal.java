package jp.ats.backsight.server;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import jp.ats.substrate.U;

public class Terminal {

	private static final Map<String, Terminal> terminals = U.newHashMap();

	private String[] administratorNames = U.STRING_EMPTY_ARRAY;

	private int concurrentSessionCount = 0;

	private int concurrentRequestCount = 0;

	private int sessionTimeoutMinutes = 0;

	private final LinkedBlockingQueue<Object> requestTickets = new LinkedBlockingQueue<Object>();

	private final AtomicInteger currentTicketCapacity = new AtomicInteger();

	static Terminal getInstance(String contextName) {
		synchronized (terminals) {
			Terminal terminal = terminals.get(contextName);
			if (terminal == null) {
				terminal = new Terminal();
				terminals.put(contextName, terminal);
			}
			return terminal;
		}
	}

	synchronized String[] getAdministratorNames() {
		return administratorNames;
	}

	synchronized int getConcurrentSessionCount() {
		return concurrentSessionCount;
	}

	synchronized int getConcurrentRequestCount() {
		return concurrentRequestCount;
	}

	synchronized int getSessionTimeoutMinutes() {
		return sessionTimeoutMinutes;
	}

	synchronized void setAdministratorNames(String[] administratorNames) {
		this.administratorNames = administratorNames;
	}

	synchronized void setConcurrentSessionCount(int concurrentSessionCount) {
		this.concurrentSessionCount = concurrentSessionCount;
	}

	synchronized void setConcurrentRequestCount(int concurrentRequestCount) {
		this.concurrentRequestCount = concurrentRequestCount;
		adjustTickets();
	}

	synchronized void setSessionTimeoutMinutes(int sessionTimeoutMinutes) {
		this.sessionTimeoutMinutes = sessionTimeoutMinutes;
	}

	Object getTicket() {
		try {
			return requestTickets.take();
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}

	void receiveTicket(Object ticket) {
		requestTickets.add(ticket);
	}

	void adjustTickets() {
		int concurrentRequestCount = getConcurrentRequestCount();

		//同時実行可能リクエスト数に変更がなければreturn
		if (concurrentRequestCount == currentTicketCapacity.intValue()) return;

		//同時実行可能リクエスト数が縮小された場合
		while (concurrentRequestCount < currentTicketCapacity.intValue()) {
			try {
				requestTickets.take();
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
			currentTicketCapacity.decrementAndGet();
		}

		//同時実行可能リクエスト数が拡大された場合
		while (concurrentRequestCount > currentTicketCapacity.intValue()) {
			requestTickets.add(new Object());
			currentTicketCapacity.incrementAndGet();
		}
	}
}
