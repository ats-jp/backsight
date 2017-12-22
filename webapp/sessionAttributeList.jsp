<!-- $Source: D:/orant/repository/backsight/backsight/sessionAttributeList.jsp,v $ -->
<!-- $Name: v0_1_20090119a $ -->
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page language="java" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Map.Entry" %>
<%@ page import="jp.ats.backsight.BacksightController" %>
<%@ page import="jp.ats.backsight.client.BacksightClient" %>
<%@ page import="jp.ats.backsight.SessionInfo" %>
<html>
<head>
	<title><%=BacksightClient.getSignature(session)%> - セッション属性値一覧</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" type="text/css" href="backsight.css">
</head>
<body>

<%@ include file="header.jsp" %>
<div id="content">

<h2>セッション属性値一覧</h2>

<%
BacksightController controller = BacksightClient.getController(session);

SessionInfo[] sessions = controller.getSessions();
for (int i = 0; i < sessions.length; i++) {
	StringBuilder builder = new StringBuilder();
	synchronized (sessions[i]) {
		String userCode = sessions[i].getRemoteUser();

		builder.append("<hr>");

		builder.append("ユーザー名 = ");
		if (userCode == null) {
			builder.append("");
		} else if (userCode.equals("")) {
			builder.append("logouted");
		} else {
			builder.append(userCode);
		}

		builder.append("<br>");
		builder.append("ユーザー情報 = " + sessions[i].getUserInfo());
		builder.append("<br>");
		builder.append("接続IP = " + (sessions[i].getRemoteAddr() == null ? "" : sessions[i].getRemoteAddr()));
		builder.append("<br>");
		builder.append("自動ログアウト予定時刻 = " + sessions[i].getSessionInactiveTime());
		builder.append("<br>");
		builder.append("セッションID = " + sessions[i].getId());

		builder.append("<table border='1'><tr><th>key</th><th>value</th></tr><tr>");

		Set<Entry<String, String>> entrySet = sessions[i].getAttributes().entrySet();
		for (Entry<String, String> entry : entrySet) {
			String name = entry.getKey();
			builder.append("<td nowrap>" + name + "</td>");

			String value = entry.getValue();

			if (value == null) {
				builder.append("<td nowrap>-</td><td align='center' nowrap>-</td><td align='center' nowrap>-</td>");
			} else {
				builder.append("<td nowrap>" + value + "</td>");
			}
			builder.append("</tr>");
		}
		builder.append("</table>");
	}
%>
<%=builder%>
<%
}
%>

<%@ include file="back.jsp" %>
</div>
</body>
</html>
