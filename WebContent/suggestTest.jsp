<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=EUC-KR">
<title>Insert title here</title>
</head>
<body>

요금제시 테스트 ~~

<form name="sugTesetForm" method="post" action="appccr?cmd=suggestCharge">
<h4>푸시푸시~</h4>
		고객번호<input type="text" name="CPhoneNum" size=50 value="01049228105"><br>
		기사번호<input type="text" name="DPhoneNum" size=50><br>
		금액<input type="text" name="Charge" size=50><br>
		위도<input type="text" name="DLat" size=50><br>
		경도<input type="text" name="DLng" size=50><br>
		제시시간<input type="text" name="DSuggestTime" size=50><br>
		<input type="submit" value="전송" width="50"><br>
</form>

</body>
</html>