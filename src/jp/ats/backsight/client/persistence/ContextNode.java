package jp.ats.backsight.client.persistence;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import jp.ats.substrate.U;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ContextNode {

	private static final String nameAttribute = "name";

	private static final String administratorNamesAttribute = "administrator-names";

	private static final String concurrentSessionCountAttribute = "concurrent-session-count";

	private static final String concurrentRequestCountAttribute = "concurrent-request-count";

	private static final String sessionTimeoutMinutesAttribute = "session-timeout-minutes";

	private final String name;

	private String[] administratorNames = U.STRING_EMPTY_ARRAY;

	private int concurrentSessionCount;

	private int concurrentRequestCount;

	private int sessionTimeoutMinutes;

	public ContextNode(String name) {
		this.name = name;
	}

	ContextNode(SiteNode parent, XPath xpath, Node node) {
		try {
			name = xpath.evaluate("@" + nameAttribute, node);
			administratorNames = xpath.evaluate(
				"@" + administratorNamesAttribute,
				node)
				.trim()
				.split(" ");
			concurrentSessionCount = Integer.parseInt(xpath.evaluate("@"
				+ concurrentSessionCountAttribute, node));
			concurrentRequestCount = Integer.parseInt(xpath.evaluate("@"
				+ concurrentRequestCountAttribute, node));
			sessionTimeoutMinutes = Integer.parseInt(xpath.evaluate("@"
				+ sessionTimeoutMinutesAttribute, node));
		} catch (XPathExpressionException e) {
			throw new IllegalStateException(e);
		}
		parent.addContextNode(this);
	}

	public synchronized String getName() {
		return name;
	}

	public synchronized String[] getAdministratorNames() {
		return administratorNames;
	}

	public synchronized void setAdministratorNames(String[] administratorNames) {
		this.administratorNames = administratorNames;
	}

	public synchronized int getConcurrentSessionCount() {
		return concurrentSessionCount;
	}

	public synchronized void setConcurrentSessionCount(
		int concurrentSessionCount) {
		this.concurrentSessionCount = concurrentSessionCount;
	}

	public synchronized int getConcurrentRequestCount() {
		return concurrentRequestCount;
	}

	public synchronized void setConcurrentRequestCount(
		int concurrentRequestCount) {
		this.concurrentRequestCount = concurrentRequestCount;
	}

	public synchronized int getSessionTimeoutMinutes() {
		return sessionTimeoutMinutes;
	}

	public synchronized void setSessionTimeoutMinutes(int sessionTimeoutMinutes) {
		this.sessionTimeoutMinutes = sessionTimeoutMinutes;
	}

	synchronized void decorateXMLElement(Element element) {
		element.setAttribute(nameAttribute, name);
		element.setAttribute(
			administratorNamesAttribute,
			U.join(administratorNames, " "));
		element.setAttribute(
			concurrentSessionCountAttribute,
			String.valueOf(concurrentSessionCount));
		element.setAttribute(
			concurrentRequestCountAttribute,
			String.valueOf(concurrentRequestCount));
		element.setAttribute(
			sessionTimeoutMinutesAttribute,
			String.valueOf(sessionTimeoutMinutes));
	}
}
