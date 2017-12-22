<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page language="java" %>
<div id="banner">
<span title="トップページへ" id="logo" onclick="location.href='./'">Backsight; <font size="-2">web application controller</font></span>

<%
if (request.getRemoteUser() != null) {
%>
<span title="ログアウト" id="logout" onclick="location.href='logout.jsp'">logout</span>
<%
}
%>
</div>
<div id="currentContextArea">
<%
if (jp.ats.backsight.client.BacksightClient.hasCurrent(session)) {
%>
<span title="メニューへ" id="currentContext" onclick="location.href='menu.jsp?site=<%=jp.ats.backsight.client.BacksightClient.getCurrentSite(session)%>&context=<%=jp.ats.backsight.client.BacksightClient.getCurrentContext(session)%>'"><%=jp.ats.backsight.client.BacksightClient.getSignature(session)%></span>
<%
}
%>
</div>
