<!-- $Source: D:/orant/repository/backsight/backsight/accessControlThread.jsp,v $ -->
<!-- $Name: v0_1_20090119a $ -->
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page language="java" %>
<%@ page import="jp.ats.backsight.BacksightController" %>
<%@ page import="jp.ats.backsight.client.BacksightClient" %>
<%
BacksightController controller = BacksightClient.getController(session);

if ("true".equals(request.getParameter("restart"))){
	controller.restartAccessControlServer();
}
%>
<html>
<head>
	<link rel="stylesheet" type="text/css" href="backsight.css">
	<title><%=BacksightClient.getSignature(session)%> - アクセス制限スレッド状況</title>
</head>
<body>

<%@ include file="header.jsp" %>
<div id="content">
<h2>アクセス制限スレッド状況</h2>

<%
if (controller.isAccessControlServerShutdowned()) {
%>
<font color=red>停止しています。</font><br>
<a href="accessControlThread.jsp?restart=true">アクセス制限スレッド起動</a>
<%
} else {
%>
正常に稼働中です。
<br>
<br>
現在の状態は [<%=controller.getAccessControlServerState()%>] です。
<%
}
%>

<%@ include file="back.jsp" %>
</div>
</body>
</html>
