<!-- $Source: D:/orant/repository/backsight/backsight/memory.jsp,v $ -->
<!-- $Name: v0_1_20090119a $ -->
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page language="java" %>
<%@ page import="jp.ats.backsight.BacksightController" %>
<%@ page import="jp.ats.backsight.client.BacksightClient" %>
<html>
<head>
	<title><%=BacksightClient.getSignature(session)%> - メモリ状況</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" type="text/css" href="backsight.css">
</head>
<body>

<%@ include file="header.jsp" %>
<div id="content">

<h2>メモリ状況</h2>

free / total = <%=BacksightClient.getController(session).getMemoryInfo()%>

<%@ include file="back.jsp" %>
</div>
</body>
</html>
