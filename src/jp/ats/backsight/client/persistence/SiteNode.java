package jp.ats.backsight.client.persistence;

import java.util.Collection;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import jp.ats.substrate.U;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SiteNode {

	private static final String addressAttribute = "address";

	private final Map<String, ContextNode> nodes = U.newTreeMap();

	private final String address;

	public SiteNode(String address) {
		this.address = address;
	}

	SiteNode(XPath xpath, Node node) {
		try {
			address = xpath.evaluate("@" + addressAttribute, node);

			NodeList list = (NodeList) xpath.evaluate(
				"context",
				node,
				XPathConstants.NODESET);

			int length = list.getLength();
			for (int i = 0; i < length; i++) {
				ContextNode context = new ContextNode(this, xpath, list.item(i));
				nodes.put(context.getName(), context);
			}
		} catch (XPathExpressionException e) {
			throw new IllegalStateException(e);
		}
	}

	public synchronized String getAddress() {
		return address;
	}

	public synchronized ContextNode[] getContextNodes() {
		Collection<ContextNode> values = nodes.values();
		return values.toArray(new ContextNode[values.size()]);
	}

	public synchronized ContextNode getContextNode(String name) {
		return nodes.get(name);
	}

	public synchronized void addContextNode(ContextNode context) {
		nodes.put(context.getName(), context);
	}

	public synchronized void deleteContextNode(String name) {
		nodes.remove(name);
	}

	synchronized void decorateXMLElement(Element element) {
		element.setAttribute(addressAttribute, address);

		Document document = element.getOwnerDocument();
		for (ContextNode context : nodes.values()) {
			Element xmlElement = document.createElement("context");
			context.decorateXMLElement(xmlElement);
			element.appendChild(xmlElement);
		}
	}
}
