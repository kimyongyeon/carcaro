<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="carcaro.bean.Driver"%>
<%
String id = (String)session.getAttribute("id");
boolean check = Boolean.parseBoolean((String)request.getAttribute("check"));
Driver driver = (Driver)request.getAttribute("driver");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>정보수정</title>
<script>
function checking(){
	var pw1 = document.writeForm.passwd.value;
	var pw2 = document.writeForm.check_pw.value;
	var phone = document.writeForm.phone.value;
	var email1 = document.writeForm.email1.value;
	var email2 = document.writeForm.email2.value;
	var addr = document.writeForm.address.value;
	var authNo = document.writeForm.authorizationNo.value;
	var career = document.writeForm.career.value;
	var company = document.writeForm.company.value;
	
	if(pw1 !="" && pw2 == "") alert("비밀번호 한번 더 입력하세요")
	else if(pw1 != pw2) alert("비밀번호가 잘못 입력되었습니다")
	else if(pw1 !="" && pw2.length < 4) alert("비밀번호는 4글자 이상이어야 합니다.")
	else if(phone == "") alert("휴대 전화를 입력해 주세요")
	else if(phone.length>11 || phone.length<10) alert("제대로 된 휴대전화를 입력해주세요")
	else if(email1 == "" || email2 == "") alert("이메일을 입력해주세요")
	else if(addr == "") alert("주소를 입력해주세요")
	else if(authNo == "") alert("증권번호를 입력해주세요")
	else if(career == "") alert("대리 경력을 입력해주세요")
	else if(company == "") alert("근무업체 입력해주세요")
	else document.writeForm.submit()
}
function checkNo(){
	if((event.keyCode>57)||(event.keyCode<48)) {
	    event.returnValue=false;
		alert("숫자만 입력해 주세요");
	}
}
</script>
</head>
<body>
	<%
if(!check) { 
%>
	<form method="post" action="ccr?cmd=modifyInfo">
		<input type="hidden" name="id" value="<%=id %>"> 비밀번호 확인 <input
			type="password" name="pw"> <input type="submit" value="확인" />
	</form>
	<%} else if(check && driver!=null) {
%>
	<form name="writeForm" method="post" action="ccr?cmd=updateInfo">
		<!-- 회원 가입 폼 -->
		<table width="85%" border="0" cellspacing="0" cellpadding="5"
			align="center">
			<tr>
				<td width="30%" align=center>아이디</td>
				<td colspan=2><input type=hidden size=10 name="driverId"
					value="<%=driver.getDriverId()%>"><%=driver.getDriverId() %></td>
			</tr>
			<tr>
				<td width="30%" align=center>비밀번호</td>
				<td colspan=2><input type="password" size=10 name="passwd"
					style="ime-mode: disabled;" class="input01"></td>
			</tr>
			<tr>
				<td width="30%" align=center>비밀번호 확인</td>
				<td colspan=2><input type="password" size=10 name="check_pw"
					style="ime-mode: disabled;" class="input01"></td>
			</tr>
			<tr>
				<td width="30%" align=center>이름</td>
				<td colspan=2><%=driver.getName() %></td>
			</tr>
			<tr>
				<td width="30%" align=center>주민등록번호</td>
				<td><%=driver.getResidentNo().substring(0,5)%>-<%=driver.getResidentNo().substring(6)%></td>
			</tr>
			<tr>
				<td width="30%" align=center>주소</td>
				<td colspan=2><input type=text size=40 name="address"
					style="ime-mode: active;" class="input01"
					value="<%=driver.getAddress() %>"></td>
			</tr>
			<tr>
				<td width="30%" align=center>휴대전화</td>
				<td colspan=2><input type=text size=11
					onkeypress="javascript:checkNo()" name="phone"
					value="<%=driver.getPhoneNum() %>"></td>
			</tr>
			<tr>
				<td width="30%" align=center>이메일</td>
				<%String[] email = driver.getEmail().split("@"); %>
				<td colspan=2><input type=text size=10 name="email1"
					value="<%=email[0]%>">@<input type="text"
					name="email2" value="<%=email[1]%>">
			</tr>
			<tr>
				<td width="30%" align=center>증권번호</td>
				<td colspan=2><input type=text size=10 name="authorizationNo"
					value="<%=driver.getAuthorizationNo() %>"></td>
			</tr>
			<tr>
				<td width="30%" align=center>대리 경력</td>
				<td colspan=2><input type=text size=4 name="career"
					value="<%=driver.getCareer() %>" onkeypress="javascript:checkNo()">개월</td>
			</tr>
			<tr>
				<td width="30%" align=center>근무 업체</td>
				<td colspan=2><input type=text size=40 name="company"
					value="<%=driver.getCompany() %>"></td>
			</tr>
			<tr>
				<td width="30%" align=center>운전 면허</td>
				<td colspan=2>
					<%int m = driver.getLicenseType(); %> <input name="licenseType"
					type="radio" value="1" <%=m==1 ? "checked=checked" : "" %>>1종대형
					<input name="licenseType" type="radio" value="2"
					<%=m==2 ? "checked=checked" : "" %>>1종보통 <input
					name="licenseType" type="radio" value="3"
					<%=m==3 ? "checked=checked" : "" %>>2종보통 <input
					name="licenseType" type="radio" value="4"
					<%=m==4 ? "checked=checked" : "" %>>기타
				</td>
			</tr>
			<tr>
				<td width="30%" align=center>자동/수동</td>
				<td colspan=2><input name="licenseAuto" type="radio"
					value="true" <%=driver.isLicenseAuto() ? "checked=checked" : "" %>>자동
					<input name="licenseAuto" type="radio" value="false"
					<%=!driver.isLicenseAuto() ? "checked=checked" : "" %>>수동</td>
			</tr>
			<tr>
				<td width="30%" align=center>수신동의</td>
				<td colspan=2>
					<%int n = driver.getAgreeReceive();%> <input type="checkbox"
					name="agreeReceive" value="1"
					<%=n==1 || n==3 ? "checked=checked" : "" %>>sms <input
					type="checkbox" name="agreeReceive" value="2"
					<%=n==2 || n==3 ? "checked=checked" : "" %>>email
				</td>
			</tr>
		</table>

		<br>
		<br>

		<!-- 가입 완료, 취소 -->
		<center>
			<input type="button" value="정보수정" onClick="javascript:checking()" />
		</center>
	</form>
	<%} %>
</body>
</html>