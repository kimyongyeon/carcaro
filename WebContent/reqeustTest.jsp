<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Insert title here</title>
</head>
<body>

RequestedByCustomer 테스트

<form name="reqTesetForm" method="post" action="appccr?cmd=test_RequestedByCustomer">
<h4>푸시푸시~</h4>
		고객번호<input type="text" name="CPhoneNum" size=50><br>
		기사번호<input type="text" name="DPhoneNum" size=50 value="01049228105"><br>
		도착지<input type="text" name="CDst" size=50><br>
		출발지<input type="text" name="CSrc" size=50><br>
		요청시간<input type="text" name="CRequestTime" size=50><br>
		거리<input type="text" name="Distance" size=50><br>
		<input type="submit" value="전송" width="50"><br>
</form>
</body>
</html>