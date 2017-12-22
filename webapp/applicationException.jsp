<!-- $Source: D:/orant/repository/backsight/backsight/applicationException.jsp,v $ -->
<!-- $Name: v0_1_20090119a $ -->
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page language="java" %>
<%@ page import="jp.ats.backsight.client.BacksightClient" %>
<%@ page import="jp.ats.backsight.client.ApplicationException" %>
<html>
<head>
	<title>エラー - Backsight;</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" type="text/css" href="backsight.css">
</head>
<body>

<%@ include file="header.jsp" %>
<div id="content"></div>

<h2>エラー</h2>

<%=ApplicationException.getExceptionMessage()%>

<%@ include file="back.jsp" %>
<div></div>
</body>
</html>
