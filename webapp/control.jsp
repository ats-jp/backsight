<!-- $Source: D:/orant/repository/backsight/backsight/control.jsp,v $ -->
<!-- $Name: v0_1_20090119a $ -->
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page language="java" %>
<%@ page import="jp.ats.backsight.BacksightController" %>
<%@ page import="jp.ats.backsight.Common" %>
<%@ page import="jp.ats.backsight.client.BacksightClient" %>
<html>
<head>
	<title><%=BacksightClient.getSignature(session)%> - アプリケーションの設定確認・変更</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<script type="text/javascript" src="js/prototype.js"></script>
	<script type="text/javascript" src="js/dexter.js"></script>
	<link rel="stylesheet" type="text/css" href="backsight.css">
	<link rel="stylesheet" type="text/css" href="dexter.css">
</head>
<body>

<%@ include file="header.jsp" %>
<div id="content">

<h2>アプリケーションの設定確認・変更</h2>

<%
BacksightController controller = BacksightClient.getController(session);
%>

<form id="ControlForm" action="ControlAction.do" method="POST">
管理ユーザー（スペース区切りで複数入力可）
<br>
<input type="text" name="administratorNames" value="<%=Common.join(BacksightClient.getAdministratorNames(session), " ")%>">
<br>
<font size="-1">アプリケーションの現在値 = </font>[<%=Common.join(controller.getAdministratorNames(), " ")%>]
<br><br>
同時接続セッション数
<br>
<input type="text" name="concurrentSessionCount" id="concurrentSessionCount_id" value="<%=BacksightClient.getConcurrentSessionCount(session)%>">
<br>
<font size="-1">アプリケーションの現在値 = </font>[<%=controller.getConcurrentSessionCount()%>]
<br><br>
同時実行リクエスト数
<br>
<input type="text" name="concurrentRequestCount" id="concurrentRequestCount_id" value="<%=BacksightClient.getConcurrentRequestCount(session)%>">
<br>
<font size="-1">アプリケーションの現在値 = </font>[<%=controller.getConcurrentRequestCount()%>]
<br><br>
セッションタイムアウト時間（単位は分）
<br>
<input type="text" name="sessionTimeoutMinutes" id="sessionTimeoutMinutes_id" value="<%=BacksightClient.getSessionTimeoutMinutes(session)%>">
<br>
<font size="-1">アプリケーションの現在値 = </font>[<%=controller.getSessionTimeoutMinutes()%>]
<br><br>
<input type="submit" value="変更" id="submit_id" onclick="return confirm('稼動中のアプリケーションに対して変更を行います\n本当によろしいですか?');">
</form>

<%@ include file="back.jsp" %>
<script>
var dexter = new Dexter("/backsight/dexter", $("ControlForm"), $("submit_id")).start();
dexter.changeMessagePositionToRight();
</script>
</div>
</body>
</html>
