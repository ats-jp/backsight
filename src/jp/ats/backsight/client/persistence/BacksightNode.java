package jp.ats.backsight.client.persistence;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import jp.ats.substrate.U;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class BacksightNode {

	private final File xml;

	private final Map<String, SiteNode> nodes = U.newTreeMap();

	public BacksightNode(File xml) throws IOException {
		this.xml = xml;

		if (!xml.exists()) {
			store();
			return;
		}

		XPath xpath = XPathFactory.newInstance().newXPath();

		InputSource input;
		input = new InputSource(new BufferedInputStream(
			new FileInputStream(xml)));

		try {
			Node node = (Node) xpath.evaluate(
				"/backsight",
				input,
				XPathConstants.NODE);

			NodeList list = (NodeList) xpath.evaluate(
				"site",
				node,
				XPathConstants.NODESET);

			int length = list.getLength();
			for (int i = 0; i < length; i++) {
				SiteNode site = new SiteNode(xpath, list.item(i));
				nodes.put(site.getAddress(), site);
			}
		} catch (XPathExpressionException e) {
			throw new IllegalStateException(e);
		}
	}

	public synchronized SiteNode[] getSiteNodes() {
		Collection<SiteNode> values = nodes.values();
		return values.toArray(new SiteNode[values.size()]);
	}

	public synchronized SiteNode getSiteNode(String address) {
		return nodes.get(address);
	}

	public synchronized void addSiteNode(SiteNode site) {
		nodes.put(site.getAddress(), site);
	}

	public synchronized void deleteSiteNode(String address) {
		nodes.remove(address);
	}

	public synchronized void store() {
		Document document;
		try {
			document = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder()
				.newDocument();
		} catch (ParserConfigurationException e) {
			throw new IllegalStateException(e);
		}

		document.setXmlStandalone(true);

		Element root = document.createElement("backsight");
		document.appendChild(root);

		for (SiteNode site : nodes.values()) {
			Element xmlElement = document.createElement("site");
			site.decorateXMLElement(xmlElement);
			root.appendChild(xmlElement);
		}

		try {
			Transformer transformer = TransformerFactory.newInstance()
				.newTransformer();

			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");

			transformer.transform(
				new DOMSource(document),
				new StreamResult(xml));
		} catch (TransformerException e) {
			throw new IllegalStateException(e);
		}
	}
}
