<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Test Page</title>
</head>
<body>
<br>

<a href="./mqttServerToApp.jsp">기사로 등록된 모든 사람에게 푸시</a><br/>
<a href="./testPage.jsp">대리기사 요청</a><br/>
<a href="appccr?cmd=test_RequestedByCustomer&DPhoneNum=01031113722&CDst=수원시영통동&CSrc=서울시삼성동&CRequestTime=123&Distance=0">01031113722 에게 대리요청 푸시</a><br>
<a href="appccr?cmd=test_RequestedByCustomer&DPhoneNum=01049228105&CDst=수원시영통동&CSrc=서울시삼성동&CRequestTime=123&Distance=0">01049228105 에게 대리요청 푸시</a><br>
<a href="appccr?cmd=test_RequestedByCustomer&DPhoneNum=01086525935&CDst=수원시영통동&CSrc=서울시삼성동&CRequestTime=123&Distance=0">01086525935 에게 대리요청 푸시</a>

<a href="./reqeustTest.jsp">대리 요청 테스트</a>
<a href="./suggestTest.jsp">요금 제시 테스트</a>

</body>
</html>