<!-- $Source: D:/orant/repository/backsight/demo/heavyProcess.jsp,v $ -->
<!-- $Name: v0_1_20090119a $ -->
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page language="java" %>
<html>
<head>
	<title>heavy process</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>
<body>
<h1>heavy process</h1>
<%
Thread.sleep(60 * 1000);
%>
<br><br>
終了しました。
<br><br>
<a href="./">メニューに戻る</a>
</body>
</html>
