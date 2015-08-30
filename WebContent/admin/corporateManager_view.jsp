<%@page import="carcaro.util.PagingHelper" %>
<%@page import="carcaro.bean.Driver" %>
<%@page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%
long plTotalCnt = (Long)request.getAttribute("plTotalCnt"); // 전체 레코드 수
long pageSize = (Long)request.getAttribute("pageSize"); // 한 페이지당 레코드 수
long plPageRange = 5; // 페이지 출력 범위
long plCurrPage = (Long)request.getAttribute("pageNum"); // 현재 페이지
boolean certifyCheck = Boolean.parseBoolean((String)request.getAttribute("certifyCheck")); //승인(true)/미승인(false) 사용자
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel='stylesheet' href='/css/style.css' type='text/css' />
<title></title>

<body>
<ul>
    <li><a href="ccr?cmd=corporateManager&action=mgr">법인 관리</a></li>
 </ul>
</body>