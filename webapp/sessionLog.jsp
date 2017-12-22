<%@ page
	contentType="text/plain;charset=UTF-8"
%><%@ page
	language="java"
%><%@ page
	import="jp.ats.backsight.BacksightController"
%><%@ page
	import="jp.ats.backsight.client.BacksightClient"
%><%
BacksightController controller = BacksightClient.getController(session);

long logId = controller.createLogIterator(request.getParameter("id"));
try {
	while (controller.hasNextLog(logId)) {
%><%=controller.nextLog(logId)%>
<%
	}
} catch (IllegalStateException e) {
%>invalidated session id=<%=request.getParameter("id")%><%
}
%>