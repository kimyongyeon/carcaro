<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>MQTT Server To App</title>
</head>
<body>
<br>
<br>
<br>

<form name="mqttMessage" method="post" action="appccr?cmd=test_sendMqttMsg">
<h4>MQTT 서버에서 앱으로 푸시푸시~</h4>
<table width="500">
	<tr>
		<td width="200">전송할 메세지</td>
		<td><input type="text" name="mqttmsg" size=50></td>
		<td width="50"><input type="submit" value="전송" width="50">
		</td>
	</tr>
</table>
</form>

</body>
</html>