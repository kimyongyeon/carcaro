<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=EUC-KR">
<title>Insert title here</title>
</head>
<body>
	
	
	<!-- 추가 시켜야 할 것 -->
	<!-- 고객 출발지, 고객 목적지 , 고개 요청 시간 -->
	<a href="appccr?cmd=requestDriver&CPhoneNum=01000000000&CLat=37.508879&CLng=127.063264&CSrc=삼성동&CDst=신촌&CrequestTime=123">삼성동 대리요청1</a><br>
	<a href="appccr?cmd=requestDriver&CPhoneNum=01049228105&CLat=37.510714&CLng=127.062106&CSrc=삼성동&CDst=신촌&CrequestTime=123">삼성동 대리요청2</a><br>
	<a href="appccr?cmd=requestDriver&CPhoneNum=01000002222&CLat=37.508126&CLng=127.061205">대치동 대리요청1</a><br>
	<a href="appccr?cmd=requestDriver&CPhoneNum=01000003333&CLat=37.507717&CLng=127.06395">대치동 대리요청2</a><br>
	<a href="appccr?cmd=requestDriver&CPhoneNum=01000004444&CLat=37.555083&CLng=126.936866">신촌 대리요청1</a><br>
	<a href="appccr?cmd=requestDriver&CPhoneNum=01000005555&CLat=37.556406&CLng=126.936951">신촌 대리요청2</a><br>
	<a href="appccr?cmd=requestDriver&CPhoneNum=01000006666&CLat=37.555759&CLng=126.938368">신촌 대리요청3</a><br>
	<a href="appccr?cmd=requestDriver&CPhoneNum=01000007777&CLat=37.548716&CLng=126.91353">합정역 대리요청</a><br>
	<a href="appccr?cmd=requestDriver&CPhoneNum=01000008888&CLat=37.562938&CLng=126.975672">시청역 대리요청</a><br>
	<a href="appccr?cmd=requestDriver&CPhoneNum=01000009999&CLat=37.476719&CLng=126.981743">사당역 대리요청</a><br>
	<a href="appccr?cmd=requestDriver&CPhoneNum=01000001234&CLat=37.476788&CLng=126.984918">방배동 대리요청</a><br>
	<a href="appccr?cmd=requestDriver&CPhoneNum=010000012342&CLat=20.476788&CLng=110.984918">방배동 대리요청1</a><br><br>
	
	<a href="appccr?cmd=suggestCharge&CPhoneNum=01049228105&DPhoneNum=01086525935&Charge=9000&DLat=37.510714&DLng=127.062106&DSuggestTime=1928">01086525935(차봉진)대리기사가 01049228105고객에게 요금을 제시함</a><br><br>
	
	
	<a href="appccr?cmd=startDriving&CPhoneNum=01000001234&DPhoneNum=01086525935">01000001234고객이 01086525935I(차봉진)대리기사를 선택함</a><br>
	<a href="appccr?cmd=startDriving&CPhoneNum=01049228105&DPhoneNum=01086525935">01049228105고객이 01086525935(차봉진)대리기사를 선택함</a><br><br>
	
	<a href="appccr?cmd=endDriving&CPhoneNum=01049228105&DPhoneNum=01086525935">01086525935(차봉진)대리기사가 01049228105고객과의 대리운전이 끝났음을 알림</a><br><br>
	

</body>
</html>