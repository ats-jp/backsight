<!-- $Source: D:/orant/repository/backsight/demo/logout.jsp,v $ -->
<!-- $Name: v0_1_20090119a $ -->
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page language="java" %>
<%
session.invalidate();
%>
ログアウトしました
<br><br>
<a href="./">ログイン</a>
