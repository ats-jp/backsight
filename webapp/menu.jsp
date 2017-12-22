<!-- $Source: D:/orant/repository/backsight/backsight/menu.jsp,v $ -->
<!-- $Name: v0_1_20090119a $ -->
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page language="java" %>
<%@ page import="jp.ats.backsight.client.BacksightClient" %>
<%
BacksightClient.init(request);
%>
<html>
<head>
	<title><%=BacksightClient.getSignature(session)%> - メニュー</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<script type="text/javascript" src="js/prototype.js"></script>
	<link rel="stylesheet" type="text/css" href="backsight.css">
</head>
<body>

<%@ include file="header.jsp" %>
<div id="content">
<h2><%=BacksightClient.getCurrentSite(session)%> / <%=BacksightClient.getCurrentContext(session)%> の管理</h2>
機能を選択してください
<br><br>
<a href="sessionInfo.jsp">セッション一覧</a>
<br><br>
<a href="allRequest.jsp">実行中全リクエスト一覧</a>
<br><br>
<a href="control.jsp">アプリケーションの設定表示・変更</a>
<br><br>
<hr>

<font size="-1">
デバッグ用機能
<a href="javascript:void(0);" onclick="$('forDebug').style.height = '150px'; $('forDebug').style.visibility = 'visible';">表示</a>
|
<a href="javascript:void(0);" onclick="$('forDebug').style.height = '1px'; $('forDebug').style.visibility = 'hidden';">非表示</a>
</font>
<br><br>

<div id="forDebug" style="visibility:hidden; height:1px; overflow:hidden;">
<a href="sessionAttributeList.jsp">セッション属性値一覧</a>
<br><br>
<a href="allThread.jsp">全スレッド一覧</a>
<br><br>
<a href="accessControlThread.jsp">アクセス制限スレッド状況</a>
<br><br>
<a href="memory.jsp">メモリ状況</a>
</div>
<hr>
<font size="-1">
<a href="javascript:void(0);" onclick="window.close();">ウインドウを閉じる</a>
</font>
<%@ include file="footer.jsp" %>
</div>
</body>
</html>
