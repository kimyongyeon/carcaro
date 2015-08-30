<%@page import="carcaro.util.PagingHelper"%>
<%@page import="carcaro.bean.Driver"%>
<%@page import="carcaro.dao.DriverDAO"%>
<%@page import="java.util.Calendar"%>
<%@page import="carcaro.util.Util"%>
<%@page import="java.util.ArrayList"%>
<%@page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%
String assurance_complete_date = (String)request.getAttribute("assurance_complete_date"); // 보험만료일
String today = Util.getCurrDate();
%>
<html>
<head>
<title>대리기사 등록</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script src="./js/jquery-1.4.4.min.js"></script>
<script src="./js/jquery.mousewheel.min.js"></script>
<script src="./js/calendar_ahmax.js"></script>
<link rel='stylesheet' href='./js/calendar_ahmax.css' type='text/css' />
<link rel='stylesheet' href='/css/style.css' type='text/css' />
<script src="../js/json2.js"></script>
<script src="../js/ajax.js"></script>
<script>
	function checking(){

		var result = confirm("정말로 추가하겠습니까?");

		if(result){
			var driverId = document.regForm.driverId.value; //  아이디
			var passwd = document.regForm.passwd.value; // 비밀번호
			var name = document.regForm.name.value; // 이름
			var residentNo1 = document.regForm.residentNo1.value; // 주민번호1
			var residentNo2 = document.regForm.residentNo2.value; // 주민번호2
			var phone1 = document.regForm.phone1.value; // 전화번호1
			var phone2 = document.regForm.phone2.value; // 전화번호2
			var phone3 = document.regForm.phone3.value; // 전화번호3
			var address = document.regForm.address.value; // 주소
			var email = document.regForm.email.value; // 이메일
			var licenseType = document.regForm.licenseType.value; // 면허종류
			var assurance_complete_date = document.regForm.assurance_complete_date.value; // 보험완료일
			var authorizationNo = document.regForm.authorizationNo.value; // 보험번호
			var authorizationName = document.regForm.authorizationName.value; // 보험이름
			
			// 오류체크는 나중에 이곳에 추가하세요.

			// URL 선언
			var url = "../ccr?cmd=driverManager&mode=insert";
			// 파라미터 선언
			var params = "driverId="+driverId;
			params += "&passwd="+passwd;
			params += "&name="+name;
			params += "&residentNo1="+residentNo1;
			params += "&residentNo2="+residentNo2;
			params += "&phone1="+phone1;
			params += "&phone2="+phone2;
			params += "&phone3="+phone3;
			params += "&address="+address;
			params += "&email="+email;
			params += "&licenseType="+licenseType;
			params += "&assurance_complete_date="+assurance_complete_date;
			params += "&authorizationNo="+authorizationNo;
			params += "&authorizationName="+authorizationName;
			//alert(params);
			sendRequest(url, params, registerResult, "POST");
		}
	} 
	
	function registerResult(){
		if (httpRequest.readyState == 4) {
			if (httpRequest.status == 200) {
				response = httpRequest.responseText;
				alert(response);
				window.close();
			}
		}
	}
</script>

</head>
<body>

<form name="regForm" enctype="multipart/form-data">

<table  class="res">
  <tr>
    <td height="40">
    <table class="res">
      <tr>
      <%	if(assurance_complete_date==null) assurance_complete_date=today;
	
		%></tr>
      <tr></tr>
      <tr>
        <td colspan="4" height="40" align="center" bgcolor="#FFFFFF"><b>대리기사 등록</b></td>
      </tr>
      <tr>
        <td width="120">대리ID</td><td width="450" height="31">
        	<input type="text" size="10" maxlength="8" name="driverId"/></td>
        <td width="120">면허종류</td><td width="254" height="31" >
        	<input type="text" size="20" name="licenseType"/></td>
      </tr>
      <tr>
        <td>PW</td>
        <td><input type="password" size="10" maxlength="8"  id="passwd" name="passwd"/></td>
        <td>면허번호</td>
        <td height="20"><input type="text" size="20" id="liceseNo" name="liceseNo" /></td>
      </tr>
      <tr>
        <td>PW확인</td>
        <td><input type="password" size="10" id="passwd2" name="passwd2"/></td>
        <td>보험</td>
        <td height="20"><input type="text" size="20" id="authorizationName" name="authorizationName"/></td>
      </tr>
      <tr>
        <td>이름</td>
        <td><input type="text" size="10" id="name" name="name"/></td>
        <td>보험번호</td>
        <td height="40"><input type="text" size="20" id="authorizationNo" name="authorizationNo"/></td>
      </tr>
      <tr>
        <td>주민등록번호</td>
        <td><input type="text" size="6" maxlength="6" id="residentNo1" name="residentNo1"/>
          -
          <input type="text" size="7" maxlength="7"  id="residentNo2" name="residentNo2"/></td>
        <td>보험만료일</td>
        <!-- 여기처리 -->
			<td height="40"><input type="text" id="assurance_complete_date" name="assurance_complete_date" value="<%=assurance_complete_date%>">
				<script type="text/javascript">
				</script>
        </td>
      </tr>
      <tr>
        <td>전화번호</td>
        <td><input type="text" size="3" maxlength="3" id="phone1" name="phone1"/>-
          <input type="text" size="4" maxlength="4" id="phone2" name="phone2"/>-
          <input type="text" size="4" maxlength="4" id="phone3" name="phone3"/></td>
      </tr>
      <tr>
        <td>주소</td>
        <td height="40"><input type="text" size="20" id=address name="address"/></td>
      </tr>
      <tr>
        <td>email</td>
        <td height="40"><input type="text" size="20"  id="email" name="email"/></td>
      </tr>
      <tr>
        <td height="40" colspan="4" align="right"></td>
        <td><input type="button" value="등록" onClick="checking();"/></td>
        <td><input type="button" value="취소" onClick="self.close()"/>
  
          </td>
      </tr>
    </table></td>
  </tr>
</table>
<BR>&nbsp;&nbsp;
</form>
</BODY>
</HTML>
