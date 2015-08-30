<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
 <%  String id = request.getParameter("id");
	String aL = request.getParameter("aL");
	String location = request.getParameter("location");
	String access = null;
	if ( aL.equals("1")){
		access = "본사";
	}else if ( aL.equals("10")){
		access = "지사";
	}else if ( aL.equals("20")){
		access = "대리점";
	}else{
		access = "접근권한 없음";
	}
	String pageTitle = request.getParameter("pageTitle");
		%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><%=pageTitle %></title>
<link rel="stylesheet" type="text/css" href="./css/style.css" />
</head>
<body>

<div id="stylized" align="center">

<table class="webAdmin">
	<thead>
	<tr>
		<th width="1600" colspan="3" valign="top"><img src="Img/top.jpg" width="1600" height="54" border="0"><br></th>
	</tr>
	</thead>

	<tr>
		<td colspan="1" align="left"> <%=access%>    <%=location %> </td> 
		<td colspan="2" align="right"> 환영합니다.<%=id %>님 | 프로파일  | <a href="./admin/logout.jsp">로그아웃</a></td>
	</tr>
	
	<tr>
		<p><td colspan="1" align="left" valign="top" width="174" height="243" border="0" usemap="#ImageMap1"><%@include file='menu.jsp' %></td></p>
		

<td>