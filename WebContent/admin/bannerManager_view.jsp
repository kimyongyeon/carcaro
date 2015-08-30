<%@page import="carcaro.util.PagingHelper" %>
<%@page import="carcaro.bean.Driver" %>
<%@page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel='stylesheet' href='/css/style.css' type='text/css' />
</head>
<title></title>
 <body>
<ul>
    <li><a href="ccr?cmd=bannerManager&mode">배너관리</a></li>
 </ul>
<!--
  <form name="frm" method="post" target="fileUp" action="/파일업로드" encType="multipart/form-data">	  
-->
   <!-- 동적으로 증/감하는 파일 NAME/ID 를 기본값(DYNAMIC_FILE)으로 설정 -->
   <!--<jsp:include page="./fileattach.jsp" flush="true"/>-->  

   <!-- 동적으로 증/감하는 파일 NAME/ID 를 직접원하는값(foo)으로 설정 -->
 <!--  
 <jsp:include page="/admin/fileattach.jsp" flush="true">
   <jsp:param name="DYNAMIC_FILE" value="foo" />			 
   <jsp:param name="MAX_SIZE" 	value="60" />	
   <jsp:param name="DISPLAY_HEIGHT" value="80" />	      
 </jsp:include>   
   
  </form>  
 -->
  </body>
</body>
</html>