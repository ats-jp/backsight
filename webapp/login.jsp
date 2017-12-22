<!-- $Source: D:/orant/repository/backsight/backsight/login.jsp,v $ -->
<!-- $Name: v0_1_20090119a $ -->
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page language="java" %>
<html>
<head>
	<title>login - Backsight;</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" type="text/css" href="backsight.css">
</head>
<body onload="document.forms[0].j_username.focus();">

<%@ include file="header.jsp" %>
<br><br>
<form method="POST" action="j_security_check">
<table border="0">
<tr>
<td>username</td><td><input type="text" name="j_username" style="width: 150px;"></td>
</tr>
<tr>
<td>password</td><td><input type="password" name="j_password" style="width: 150px;"></td>
</tr>
</table>
<br>
<input type="submit" value="login">
</form>
<%@ include file="footer.jsp" %>
</body>
</html>
