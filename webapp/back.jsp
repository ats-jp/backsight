<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page language="java" %>
<%@ page import="jp.ats.backsight.client.BacksightClient" %>
<hr>
<font size="-1">
<a href="menu.jsp?site=<%=BacksightClient.getCurrentSite(session)%>&context=<%=BacksightClient.getCurrentContext(session)%>">メニューに戻る</a>
<br><br>
<a href="javascript:void(0);" onclick="window.close();">ウインドウを閉じる</a>
</font>
<%@ include file="footer.jsp" %>
