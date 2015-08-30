<%@page import="carcaro.util.PagingHelper"%>
<%@page import="carcaro.bean.Driver"%>
<%@page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%
	// 파라미터값을 받는다.
	String driverId = (String) request.getAttribute("driverId");
	String licenseType = (String) request.getAttribute("licenseType");
	String passwd = (String) request.getAttribute("passwd");
	String liceseNo = (String) request.getAttribute("liceseNo");
	String authorizationNo = (String) request
			.getAttribute("authorizationNo");
	String authorizationName = (String) request
			.getAttribute("authorizationName");
	String name = (String) request.getAttribute("name");
	String residentNo = (String) request.getAttribute("residentNo");
	String assurance_complete_date = (String) request
			.getAttribute("assurance_complete_date");
	String phone = (String) request.getAttribute("phone");
	String amount = (String) request.getAttribute("amount");
	String address = (String) request.getAttribute("address");
	String licensePic = (String) request.getAttribute("licensePic");
	String email = (String) request.getAttribute("email");
%>
<html>
<head>
<title>대리기사 수정</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script type="text/javascript"
	src="http://code.jquery.com/jquery.min.js"></script>
<script src="../js/json2.js"></script>
<script src="../js/ajax.js"></script>
<link rel='stylesheet' href='/css/style.css' type='text/css' />

<script>
		function driverUpate_init(){
			//null처리 전화번호 이름 주민번호
		<%
		if(phone == null || phone.length() <11)
			phone = "00000000000";
		if(residentNo==null||residentNo.length()!=13)
			residentNo="0000000000000";
	
		%>	
		
			document.getElementById("driverId").value = "<%=driverId%>";
			document.getElementById("licenseType").value = "<%=licenseType%>";
			document.getElementById("passwd").value = "<%=passwd%>";
			document.getElementById("passwd2").value = "<%=passwd%>";
			document.getElementById("name").value = "<%=name%>";
			document.getElementById("liceseNo").value = "<%=liceseNo%>";
			document.getElementById("authorizationNo").value = "<%=authorizationNo%>";
		
			document.getElementById("residentNo1").value = "<%=residentNo.substring(0, 6)%>";
			document.getElementById("residentNo2").value = "<%=residentNo.substring(6)%>";
			document.getElementById("assurance_complete_date").value = "<%=assurance_complete_date%>";
		
			document.getElementById("phone1").value = "<%=phone.substring(0, 3)%>";
			document.getElementById("phone2").value = "<%=phone.substring(3, 7)%>";
			document.getElementById("phone3").value = "<%=phone.substring(7,11)%>";
			
			// 비활성화 시키시오.
			document.getElementById("amount1").value = "<%=amount%>";
			// 아래 내용은 수정이 가능함.
			document.getElementById("amount2").value = "<%=amount%>";
			
			document.getElementById("address").value = "<%=address%>";
			document.getElementById("licensePic").value = "<%=licensePic%>";
			document.getElementById("email").value = "<%=email%>";
	}
		
		function driverModify() {
			var result  = confirm("수정하시겠습니까?");
			//값이 넘어가질않음
			 if(result){ 
					var driverId = document.updateForm.driverId.value; 
					var licenseType = document.updateForm.licenseType.value; 
					var  passwd = document.updateForm.passwd.value; 
					var liceseNo = document.updateForm.liceseNo.value; 
					var authorizationNo = document.updateForm.authorizationNo.value; 
					var name = document.updateForm.name.value;  
					var assurance_complete_date = document.updateForm.assurance_complete_date.value; 
					var residentNo1 = document.updateForm.residentNo1.value; // 주민번호1
					var residentNo2 = document.updateForm.residentNo2.value; // 주민번호2
					var phone1 = document.updateForm.phone1.value; // 전화번호1
					var phone2 = document.updateForm.phone2.value; // 전화번호2
					var phone3 = document.updateForm.phone3.value; // 전화번호3
					var amount = document.updateForm.amount.value; 
					var address = document.updateForm.address.value; 
					var licensePic = document.updateForm.licensePic.value; 
					var email = document.updateForm.email.value; 
					
					
					
					// 오류체크는 나중에 이곳에 추가하세요.

					// URL 선언
					var url = "..//ccr?cmd=driverManager&mode=update";
					// 파라미터 선언
					var params = "driverId="+driverId;
					params += "&licenseType="+licenseType;
					params += "&passwd="+passwd;
					params += "&liceseNo"+liceseNo;
					params += "&authorizationNo="+authorizationNo;
					params += "&name="+name;
					params += "&residentNo1="+residentNo1;
					params += "&residentNo2="+residentNo2;
					params += "&phone1="+phone1;
					params += "&phone2="+phone2;
					params += "&phone3="+phone3;
					params += "&assurance_complete_date="+assurance_complete_date;
					params += "&amount="+amount; 
					params += "&address="+address;
					params += "&licensePic="+licensePic;
					params += "&email="+email;
				
					alert(params);
					sendRequest(url, params, registerResult, "POST");
				 
					alert("수정완료");
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
		function driverCharge(){
			var result = confirm("충전하시겠습니까?");

			if(result){
				//여기 casting int형으로 
			//	var amount1= updateForm.amount1.value;
			//	var amount2= updateForm.amount2.value;
			//	var amount= amount1+amount2; 
				var driverId = document.updateForm.driverId.value; 
				var charge =document.updateForm.amount2.value;
				// 오류체크는 나중에 이곳에 추가하세요.

				// URL 선언
				var url = "./ccr?cmd=driverManager&mode=chargeSum";
				// 파라미터 선언
				var params = "driverId="+driverId;
				params += "&charge="+charge;
			
			//alert(amount2+"원이 충전되었습니다 \n 충전후 잔액 : "+amount+"원");
			sendRequest(url, params, registerResult, "POST");
			}
		}

</script>


</head>
<body onload="driverUpate_init();">
	<form name="updateForm" enctype="multipart/form-data">
		<table class="res">
			<tr>
			<tr>
				<td width="145" height="40" align="center"></td>
			</tr>
			<tr>
				<td colspan="5" height="40" align="center" bgcolor="#FFFFFF"><b>대리기사 수정</b></td>
			</tr>
			<tr>
				<td width="145" rowspan="6" align="center">&nbsp;</td>
				<td width="87">대리ID</td>
				<td width="314" height="31">
				<input type="text" size="10" maxlength="8" name="driverId"/></td>
				<td width="96">면허종류</td>
				<td width="243" height="31"><input type="text" size="20" name="licenseType"/></td>
			</tr>
			<tr>
				<td>PW</td>
				<td><input type="password" size="10" maxlength="8" id="passwd" name="passwd"/></td>
				<td>면허번호</td>
				<td height="20"><input type="text" size="20"  id="liceseNo" name="liceseNo"/></td>
			</tr>
			<tr>
				<td>PW확인</td>
				<td><input type="password" size="10"  id="passwd2" name="passwd2"/></td>
			<!-- <td>보험</td>
				<td height="20"><input type="text" size="20"  id="authorizationName" name="authorizationName"/></td>
			-->
			</tr>
			<tr>
				<td>이름</td>
				<td><input type="text" size="10"  id="name" name="name"/></td>
				<td>보험번호</td>
				<td height="40"><input type="text" size="20" id="authorizationNo" name="authorizationNo"/></td>
			</tr>
			<tr>
				<td>주민등록번호</td>
				<td><input type="text" size="6" maxlength="6"  id="residentNo1" name="residentNo1"/> 
					- <input type="text" size="7" maxlength="7" id="residentNo2" name="residentNo2"/></td>
				<td>보험만료일</td>
				<td height="40"><input type="text" size="20" id="assurance_complete_date" name="assurance_complete_date"/></td>
			</tr>
			<tr>
				<td>전화번호</td>
				<td><input type="text" size="3" maxlength="3" id="phone1" name="phone1"/> 
					- <input type="text" size="4" maxlength="4"  id="phone2" name="phone2"/>
					- <input type="text" size="4" maxlength="4"  id="phone3" name="phone3"/></td>
				<td>충전금</td>
				<td height="40"><input type="text" size="20" id="amount1" name="amount1" readonly /></td>
			</tr>
			<tr>
				<td width="145" height="40" align="center">&nbsp;</td>
				<td>주소</td>
				<td height="40"><input type="text" size="20"  id=address name="address"/></td>
				<td>면허증</td>
				<td height="40"><input type="text" size="20"  id="licensePic" name="licensePic"/> 
				<input type="button" value="변경" /></td>
			</tr>
			<tr>
				<td height="40" align="center">&nbsp;</td>
				<td>Email</td>
				<td><input type="text" size="20" id="email" name="email"/></td>
				<td>&nbsp;</td>
				<td height="40">&nbsp;</td>
			</tr>
			<tr>
				<td height="40" colspan="5" align="center">현금 <input
					type="text" size="20"  id="amount2" name="amount2" /> 원 
					
					<input type="button"value="충전"onClick="JavaScript:driverCharge()"/> 
					<input type="button" value="수정"onClick="JavaScript:driverModify()"/> 
					<input type="button" value="취소" onclick="self.close()" /></td>
			</tr>
		</table>
	</form>
</BODY>
</HTML>
