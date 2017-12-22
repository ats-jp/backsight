<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page language="java" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Map.Entry" %>
<%@ page import="jp.ats.backsight.BacksightController" %>
<%@ page import="jp.ats.backsight.client.BacksightClient" %>
<%@ page import="jp.ats.backsight.ThreadInfo" %>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" type="text/css" href="backsight.css">
	<title><%=BacksightClient.getSignature(session)%> - 実行中全リクエスト一覧</title>
</head>
<body>

<%@ include file="header.jsp" %>
<div id="content">
<h2>実行中全リクエスト一覧</h2>
現在実行中の処理の一覧です
<br><br>

<%=DateFormat.getDateTimeInstance().format(new Date())%>
<br><br>

<div style="height:350px; width:700px; overflow:scroll; background-color:white;">
<pre>
<%
BacksightController controller = BacksightClient.getController(session);

ThreadInfo[] threads = controller.getThreads();

for (ThreadInfo thread : threads) {
	if (thread.getRequestInfo().trim().length() == 0) continue;

%><%=thread.getThreadName()%>

<%=thread.getRequestInfo()%>

<%
	for (String line : thread.getStackTrace()) {
%><%=line%>
<%
	}
%>
--------------------
<%
}
%>
</pre>
</div>

<%@ include file="back.jsp" %>
</div>
</body>
</html>
