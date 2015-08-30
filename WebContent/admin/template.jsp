<!-- HEADER BEGIN -->
<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<% 	String id 				=(String)session.getAttribute("id");					// 세션 아이디
	
	String pageTitle 		=(String)request.getAttribute("pageTitle");			// 제목 
	String contentPage 		=request.getParameter("contentPage");		// 컨텐츠 페이지 경로
	
	if(id!=null && !id.equals("")){
		String aL = (String)session.getAttribute("accessLevel"); 	// 관리자 로그인시 접근 권한 (본사/지사/대리점)
		String location	=(String)session.getAttribute("location");  // 관리자 로그인시 사업장 위치
		%>
		<jsp:include page="./top.jsp" flush="false">
		<jsp:param name="id" value="<%=id %>" />
		<jsp:param name="aL" value="<%=aL %>" />
		<jsp:param name="pageTitle" value="<%=pageTitle %>" />
		<jsp:param name="location" value="<%=location %>" />
		</jsp:include>
<!-- HEADER END -->
<!-- CONTENT BEGIN -->


		<jsp:include page="<%=contentPage %>" flush="false">
		<jsp:param name="id" value="<%=id %>" />
		<jsp:param name="aL" value="<%=aL %>" />
		<jsp:param name="location" value="<%=location %>" />
		</jsp:include>


<!-- CONTENT END -->		
<!--FOOTER BEGIN -->
		<jsp:include page="foot.jsp" flush="false" />
<%}else {%>
	<%@include file='unauthorized.html' %>
<%} %>
<!-- FOOTER END -->