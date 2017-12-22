<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional-dtd">
<!-- $Source: D:/orant/repository/backsight/backsight/sessionInfo.jsp,v $ -->
<!-- $Name: v0_1_20090119a $ -->
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page language="java" %>
<%@ page import="jp.ats.backsight.BacksightController" %>
<%@ page import="jp.ats.backsight.client.BacksightClient" %>
<%@ page import="jp.ats.backsight.SessionInfo" %>
<%@ taglib uri="/WEB-INF/webkit.tld" prefix="webkit" %>
<%
BacksightController controller = BacksightClient.getController(session);
SessionInfo[] sessions = controller.getSessions();
%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="ja" lang="ja">
<head>
	<title><%=BacksightClient.getSignature(session)%> - セッション一覧</title>
	<script src="js/prototype.js"></script>
	<script src="js/scrotable.js"></script>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<link rel="stylesheet" type="text/css" href="backsight.css" />
</head>
<body>

<%@ include file="header.jsp" %>
<div id="content">

<h2>セッション一覧</h2>
現在ログイン中のユーザーが使用しているセッションの一覧です
<br /><br />
<webkit:scrotable type="XY" height="350" width="700">
	<webkit:scrotableX>
		<table border="1" cellspacing="0">
			<tr>
				<th class="sessionInfo" nowrap="nowrap">ユーザー名</th>
				<th class="sessionInfo" nowrap="nowrap">ユーザー情報</th>
				<th class="sessionInfo" nowrap="nowrap">接続IP</th>
				<th class="sessionInfo" nowrap="nowrap">ログイン時刻</th>
				<th class="sessionInfo" nowrap="nowrap">最終アクセス時刻</th>
				<th class="sessionInfo" nowrap="nowrap">自動ログアウト予定時刻</th>
				<th class="sessionInfo" nowrap="nowrap">滞在時間（分）</th>
				<th class="sessionInfo" nowrap="nowrap">セッションID</th>
				<th class="sessionInfo" nowrap="nowrap">ログアウト</th>
			</tr>
		</table>
	</webkit:scrotableX>

	<webkit:scrotableY>
		<table border="1" cellspacing="0">
<%
for (int i = 0; i < sessions.length; i++) {
%>
			<tr><th class="sessionInfo" align="right"><%=i + 1%></th></tr>
<%
}
%>
		</table>
	</webkit:scrotableY>

	<webkit:scrotableBody>
		<table border="1" cellspacing="0" style="empty-cells: show;">
<%
for (SessionInfo sessionInfo : sessions) {
	synchronized (sessionInfo) {
%>
			<tr>
				<td class="sessionInfo" nowrap="nowrap"><%
		String userCode = sessionInfo.getRemoteUser();
		if (userCode == null || userCode.equals("")) {
%>ログインしていません<%
		} else {
%><%=userCode%><%
		}
%></td>
				<td class="sessionInfo" nowrap="nowrap"><%=sessionInfo.getUserInfo()%></td>
				<td class="sessionInfo" nowrap="nowrap"><%=sessionInfo.getRemoteAddr() == null ? "" : sessionInfo.getRemoteAddr()%></td>
				<td class="sessionInfo" nowrap="nowrap"><%=sessionInfo.getCreationTime()%></td>
				<td class="sessionInfo" nowrap="nowrap"><%=sessionInfo.getLastAccessedTime()%></td>
				<td class="sessionInfo" nowrap="nowrap"><%=sessionInfo.getSessionInactiveTime()%></td>
				<td class="sessionInfo" nowrap="nowrap" align="right"><%=sessionInfo.getStayMinutes()%></td>
				<td class="sessionInfo" nowrap="nowrap"><a href="sessionLog.jsp?id=<%=sessionInfo.getId()%>" target="_blank"><%=sessionInfo.getId()%></a></td>
				<td class="sessionInfo" nowrap="nowrap" align="center"><a href="KillAction.do?kill=<%=sessionInfo.getId()%>">実行</a></td>
			</tr>
<%
	}
}
%>
		</table>
	</webkit:scrotableBody>
</webkit:scrotable>

<%@ include file="back.jsp" %>
</div>
</body>
</html>
