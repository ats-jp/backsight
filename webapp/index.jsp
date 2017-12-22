<!-- $Source: D:/orant/repository/backsight/backsight/index.jsp,v $ -->
<!-- $Name: v0_1_20090119a $ -->
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page language="java" %>
<%@ page import="jp.ats.backsight.Common" %>
<%@ page import="jp.ats.backsight.BacksightController" %>
<%@ page import="jp.ats.backsight.client.BacksightFilter" %>
<%@ page import="jp.ats.backsight.client.BacksightClient" %>
<%@ page import="jp.ats.backsight.client.persistence.BacksightNode" %>
<%@ page import="jp.ats.backsight.client.persistence.SiteNode" %>
<%@ page import="jp.ats.backsight.client.persistence.ContextNode" %>
<html>
<head>
	<title>アプリケーション一覧 - Backsight;</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<script type="text/javascript" src="js/prototype.js"></script>
	<script type="text/javascript" src="js/dexter.js"></script>
	<link rel="stylesheet" type="text/css" href="backsight.css">
	<link rel="stylesheet" type="text/css" href="dexter.css">
</head>
<body>

<script>
var intervalSessionIds = new Array();
var intervalRequestIds = new Array();

function startMonitoringCurrentCount(type, intervalIds, site, context, interval) {
	var contexts = intervalIds[site];
	if (!contexts) {
		intervalIds[site] = new Array();
	}

	if (intervalIds[site][context]) {return;}

	doMonitoringCurrentCount(type, site, context);
	intervalIds[site][context] = setInterval("doMonitoringCurrentCount('" + type + "', '" + site + "', '" + context + "')", interval);
}

function stopMonitoringCurrentCount(type, intervalIds, site, context) {
	if (!intervalIds[site] || !intervalIds[site][context]) {
		return;
	}

	clearInterval(intervalIds[site][context]);
	intervalIds[site][context] = null;

	var target = type + "_" + site + "/" + context;

	$(target).currentCountString = "";

	updateGraph(
		target,
		type + "graph/0_0.png");
}

function doMonitoringCurrentCount(type, site, context) {
	var receiveResponse = function(response) {
		var currentCount = response.responseXML.getElementsByTagName("current-count")[0].firstChild.nodeValue;
		var configCount = response.responseXML.getElementsByTagName("config-count")[0].firstChild.nodeValue;

		var target = type + "_" + site + "/" + context;

		updateGraph(
			target,
			type + "graph/" + currentCount + "_" + configCount + ".png");
	};

	new Ajax.Request(
		type + "Count", {
			method: "post",
			postBody: "site=" + site + "&context=" + context,
			onComplete: receiveResponse
		}
	);
}

function updateGraph(target, image) {
	if (getImage($(target).src) != getImage(image)) {
		$(target).src = image;
	}
}

function getImage(image) {
	return image.match(/\d+_\d+.png$/) + "";
}
</script>
<%
BacksightClient.clearCurrentContext(session);
%>
<%@ include file="header.jsp" %>
<div id="content">

<h2>アプリケーション一覧</h2>
現在登録されているアプリケーションの一覧です

<form name="initMenu" method="GET" action="menu.jsp">
<input type="hidden" name="site">
<input type="hidden" name="context">
</form>

<%
BacksightNode node = BacksightFilter.getBacksightNode();
synchronized (node) {
	for (SiteNode site : node.getSiteNodes()) {
%>
<hr>
<font size="+2"><%=site.getAddress()%></font>
<br><br>
<table border="1" cellspacing="0">
<tr>
<th nowrap><font size="-2">名称</font></th>
<th nowrap><font size="-2">セッション<br>モニター</font></th>
<th nowrap><font size="-2">リクエスト<br>モニター</font></th>
<th nowrap><font size="-2">管理</font></th>
</tr>
<%
		for (ContextNode context : site.getContextNodes()) {
%>
<tr>
<%
			if (BacksightClient.getControllerWithoutException(site.getAddress(), context.getName()) == null) {
%>
<td>
<%=context.getName()%>
</td>
<td>　</td>
<td>　</td>
<%
			} else {
%>
<td>
<a href="menu.jsp?site=<%=Common.encode(site.getAddress())%>&context=<%=Common.encode(context.getName())%>" target="_blank"><%=Common.encode(context.getName())%></a>
</td>
<td valign="top">
<img id="session_<%=site.getAddress()%>/<%=context.getName()%>" alt="セッション数 : 現在値 / 設定値" src="sessiongraph/0_0.png">
<script>
startMonitoringCurrentCount('session', intervalSessionIds, '<%=site.getAddress()%>', '<%=context.getName()%>', 60000);
</script>
</td>

<td valign="top">
<img id="request_<%=site.getAddress()%>/<%=context.getName()%>" alt="リクエスト数 : 現在値 / 設定値" src="requestgraph/0_0.png">
<center>
<font size="-1">
[<a href="javascript:void(0);" onclick="startMonitoringCurrentCount('request', intervalRequestIds, '<%=site.getAddress()%>', '<%=context.getName()%>', 1000);">起動</a>
|
<a href="javascript:void(0);" onclick="stopMonitoringCurrentCount('request', intervalRequestIds, '<%=site.getAddress()%>', '<%=context.getName()%>');">停止</a>]
</font>
</center>
</td>
<%
			}
%>
<td>
<font size="-1">
[<a href="DeleteContextAction.do?site=<%=Common.encode(site.getAddress())%>&context=<%=Common.encode(context.getName())%>" onclick="return confirm('<%=context.getName()%> を削除します\nよろしいですか?');"><%=context.getName()%> の削除</a>]
</font>
</td>
</tr>
<%
		}
%>
</table>
<%
	}
}
%>
<hr>
<font size="-1">
新規管理対象アプリケーションの登録
<a href="javascript:void(0);" onclick="$('addContextForm').style.height = '150px'; $('addContextForm').style.visibility = 'visible'; $('site_id').focus(); dexter.showAllValidationMessages();">表示</a>
|
<a href="javascript:void(0);" onclick="$('addContextForm').style.height = '1px'; $('addContextForm').style.visibility = 'hidden'; dexter.hideAllValidationMessages();">非表示</a>
</font>
<br><br>
<div id="addContextForm" style="visibility:hidden; height:1px; overflow:hidden;">
<form method="GET" id="NewContextForm" action="AddContextAction.do">
ホスト名またはIPアドレス
<br>
<input type="text" name="site" id="site_id">
<br><br>
アプリケーション名
<br>
<input type="text" name="context" id="context_id">
<br><br>
<input type="submit" value="登録" id="submit_id">
</form>
</div>
<script>
var dexter = new Dexter("/backsight/dexter", $("NewContextForm"), $("submit_id"));
dexter.changeMessagePositionToRight();
dexter.hideAllValidationMessages();
dexter.start();
</script>
<%@ include file="footer.jsp" %>
</div>
</body>
</html>
