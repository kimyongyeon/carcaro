<%@ page contentType = "text/html; charset=utf-8" pageEncoding="utf-8"%>
<%
    request.setAttribute("pageTitle", "Administrator Page");
%>
<jsp:forward page="./template.jsp">
    <jsp:param name="contentPage" value="./couponManager_view.jsp" />
</jsp:forward>