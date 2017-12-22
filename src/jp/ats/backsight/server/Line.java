package jp.ats.backsight.server;

import static jp.ats.substrate.U.newLinkedList;
import static jp.ats.substrate.U.newTreeMap;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jp.ats.substrate.U;

class Line {

	private final LinkedList<String> columns = newLinkedList();

	private final Map<String, String> columnsForFormatString = newTreeMap();

	private final String timestamp;

	private final String remoteAddress;

	private final String userId;

	private final String sessionId;

	private final String path;

	Line(
		String remoteAddress,
		String userId,
		String sessionId,
		String method,
		String path) {
		timestamp = DateFormat.getDateTimeInstance().format(new Date());
		columns.add(timestamp);

		this.remoteAddress = remoteAddress;
		columns.add(remoteAddress);

		this.userId = userId;
		columns.add(userId);

		this.sessionId = sessionId;
		columns.add(sessionId);

		columns.add(method);

		this.path = path;
		columns.add(path);
	}

	@Override
	public String toString() {
		return U.join(columns, "\t");
	}

	String getFormatString() {
		List<String> messages = new ArrayList<String>();
		messages.add("Timestamp : " + timestamp);
		messages.add("Remote address : " + remoteAddress);
		messages.add("User ID : " + userId);
		messages.add("Session ID : " + sessionId);
		messages.add("Request : " + path);
		messages.add("Parameters : " + columnsForFormatString.toString());
		return U.join(messages, U.LINE_SEPARATOR);
	}

	void addParameter(String key, String value) {
		columns.add(key);
		columns.add(cleanup(value));
		columnsForFormatString.put(key, cleanup(value));
	}

	private static String cleanup(String value) {
		return value.replaceAll("\\s+", " ");
	}
}
