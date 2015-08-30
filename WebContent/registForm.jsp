<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>회원 가입</title>

<script>
function checking(){
	var id = document.writeForm.driverId.value;
	var name = document.writeForm.name.value;
	var resi1 = document.writeForm.residentNo1.value;
	var resi2 = document.writeForm.residentNo2.value;
	var pw1 = document.writeForm.passwd.value;
	var pw2 = document.writeForm.check_pw.value;
	var phone = document.writeForm.phone.value;
	var email1 = document.writeForm.email1.value;
	var email2 = document.writeForm.email2.value;
	var addr = document.writeForm.address.value;
	var authNo = document.writeForm.authorizationNo.value;
	var career = document.writeForm.career.value;
	var company = document.writeForm.company.value;

	if(id == "") alert("아이디를 입력하세요")
	else if(id.length < 3) alert("아이디는 3글자 이상이어야 합니다.")
	else if(id.length > 10) alert("아이디는 10글자 내로 입력해 주세요.")
	else if(pw1 == "") alert("비밀번호를 입력하세요")
	else if(pw1.length < 4) alert("비밀번호는 4글자 이상이어야 합니다.")
	else if(pw1.length > 13) alert("비밀번호를 13글자 내로 입력해 주세요")
	else if(pw2 == "") alert("비밀번호 한번 더 입력하세요")
	else if(pw1 != pw2) alert("비밀번호가 잘못 입력되었습니다")
	else if(name == "") alert("이름을 입력하세요")
	else if(name.length > 10) alert("이름은 10글자 내로 입력해 주세요.")
	else if(resi1 == "") alert("주민등록 번호를 입력하세요")
	else if(resi2 == "") alert("주민등록 번호를 입력하세요")
	else if(resi1.length != 6 && resi2.length != 7) alert("주민등록 번호가 잘못된 형식입니다")
	else if(addr == "") alert("주소를 입력해주세요")
	else if(phone == "") alert("휴대 전화를 입력해 주세요")
	else if(phone.length>11 || phone.length<10) alert("제대로 된 휴대전화를 입력해주세요")
	else if(email1 == "" || email2 == "") alert("이메일을 입력해주세요")
	else if(authNo == "") alert("증권번호를 입력해주세요")
	else if(career == "") alert("대리 경력을 입력해주세요")
	else if(company == "") alert("근무업체 입력해주세요")
	else document.writeForm.submit()
}

function checkId(){
	window.open("checkId.jsp", "", "width=400 height=150")
}

function checkNo(){
	if((event.keyCode>57)||(event.keyCode<48))
		{
	    event.returnValue=false;
		alert("숫자만 입력해 주세요");
		}
		
}
function SetEmailTail(emailValue) {
	var email = document.all("email")    // 사용자 입력
	var emailTail = document.all("email2") // Select box
	   
	if ( emailValue == "notSelected" )
		return;
	else if ( emailValue == "etc" ) {
		emailTail.readOnly = false;
		emailTail.value = "";
		emailTail.focus();
	} else {
		emailTail.readOnly = true;
		emailTail.value = emailValue;
	}
}

</script>
</head>
<body>

<form name="writeForm" method="post" action="ccr?cmd=registDriver">
	<!-- 회원 가입 폼 -->
	<table width="85%" border="0" cellspacing="0" cellpadding="5"
		align="center">
		<tr>
			<td width="30%" align=center>아이디</td>
			<td colspan=2><input type=text size=10 name="driverId" style="ime-mode:disabled" onClick='JavaScript:checkId()' readonly/></td>			
		</tr>
		<tr>
			<td width="30%" align=center>비밀번호</td>
			<td colspan=2><input type="password" size=10 name="passwd" style="ime-mode:disabled;"
				class="input01">
			</td>
		</tr>
		<tr>
			<td width="30%" align=center>비밀번호 확인</td>
			<td colspan=2><input type="password" size=10 name="check_pw" style="ime-mode:disabled;"
				class="input01">
			</td>
		</tr>
		<tr>
			<td width="30%" align=center>이름</td>
			<td colspan=2><input type=text size=10 name="name" style="ime-mode:active;"
				class="input01"></td>
		</tr>
		<tr>
			<td width="30%" align=center>주민등록번호</td>
			<td><input type=text size=10 name="residentNo1" onkeypress="javascript:checkNo()"
				>-<input type=text size=10 name="residentNo2" onkeypress="javascript:checkNo()"
				></td>
		</tr>
		<tr>
			<td width="30%" align=center>주소</td>
			<td colspan=2><input type=text size=40 name="address" style="ime-mode:active;"
				class="input01"></td>
		</tr>
		<tr>
			<td width="30%" align=center>휴대전화</td>
			<td colspan=2><input type=text size=11 onkeypress="javascript:checkNo()"
				name="phone" ></td></td>
		</tr>
		<tr>
			<td width="30%" align=center>이메일</td>
			<td colspan=2><input type=text size=10 name="email1"
				 >@<input type="text" name="email2" value="" ReadOnly="true"/>
				 <select name="emailCheck" 
					onchange="SetEmailTail(emailCheck.options[this.selectedIndex].value)">
					<option value="">::::::::::: 선택 :::::::::::</option>
					<option value="chol.com">chol.com
					<option value="dreamwiz.com">dreamwiz.com
					<option value="empal.com">empal.com
					<option value="hanmir.com">hanmir.com
					<option value="hanafos.com">hanafos.com
					<option value="hotmail.com">hotmail.com
					<option value="lycos.co.kr">lycos.co.kr
					<option value="nate.com">nate.com
					<option value="naver.com">naver.com
					<option value="netian.com">netian.com
					<option value="paran.com">paran.com
					<option value="yahoo.co.kr">yahoo.co.kr
					<option value="etc" >직접입력
			</select>
		</tr>
		<tr>
		<td width="30%" align=center>증권번호</td>
			<td colspan=2><input type=text size=10 name="authorizationNo"
				 ></td>
		</tr>
		<tr>
		<td width="30%" align=center>대리 경력</td>
			<td colspan=2><input type=text size=4 name="career" onkeypress="javascript:checkNo()"
				>개월</td>
		</tr>
		<tr>
		<td width="30%" align=center>근무 업체</td>
			<td colspan=2><input type=text size=40 name="company"
				 ></td>
		</tr>
		<tr>
		<td width="30%" align=center>운전 면허</td>
			<td colspan=2><input name="licenseType" type="radio" value="1" checked="checked">1종대형 
			<input name="licenseType" type="radio" value="2" >1종보통 
			<input name="licenseType" type="radio" value="3" >2종보통 
			<input name="licenseType" type="radio" value="4" >기타 </td>
		</tr>
		<tr>
		<td width="30%" align=center>자동/수동</td>
			<td colspan=2><input name="licenseAuto" type="radio" value="true" checked="checked">자동 
			<input name="licenseAuto" type="radio" value="false" >수동</td>
		</tr>
		<tr>
		<td width="30%" align=center>수신동의</td>
			<td colspan=2><input type="checkbox" name="agreeReceive" value="1">sms 
			<input type="checkbox" name="agreeReceive" value="2">email</td>
		</tr>
	</table>
	
	<br><br>
	
	<!-- 가입 완료, 취소 -->
	<center>
	<input type="button" value="회원가입" onClick="javascript:checking()"/>
	<input type="reset" value="다시작성" />
	</center>
	</form>
</body>
</html>