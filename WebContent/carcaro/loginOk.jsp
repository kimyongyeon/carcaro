<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=EUC-KR">
<title>로그인 성공</title>
</head>
<body>


	<% String id =(String)session.getAttribute("id");
if(id!=null && !id.equals("")){%>
	<%=id %>님 환영합니다. ^-^*
	<br>
	<br>
	<%}%>
	<center>


		<% if(id.equals("admin")) {%>
		<a href='ccr?cmd=driverList'>기사 승인 및 목록</a><br> <a
			href='ccr?cmd=dayDriveDetail'>일별 대리운전 조회</a><br>
		<% } else {%>
		충전금액
		<form method=post action="./INIpay/INIsecurestart.jsp">
			<input name="price" type="radio" value="1000" checked="checked">1000원
			<input name="price" type="radio" value="20000">20000원<br>
			<input type="submit" value="결제하기">
		</form>
		<a href='ccr?cmd=getSettlementList'>결제내역</a><br> <a
			href='ccr?cmd=modifyInfo'>정보수정</a><br>
		<% } %>
		<a href="./carcaro/logout.jsp">로그아웃</a>
	</center>
</body>
</html>