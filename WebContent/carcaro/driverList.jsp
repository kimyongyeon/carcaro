<%@page import="carcaro.util.PagingHelper"%>
<%@page import="carcaro.bean.Driver"%>
<%@page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%
long plTotalCnt = (Long)request.getAttribute("plTotalCnt"); // 전체 레코드 수
long pageSize = (Long)request.getAttribute("pageSize"); // 한 페이지당 레코드 수
long plPageRange = 5; // 페이지 출력 범위
long plCurrPage = (Long)request.getAttribute("pageNum"); // 현재 페이지
boolean certifyCheck = Boolean.parseBoolean((String)request.getAttribute("certifyCheck")); //승인(true)/미승인(false) 사용자
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=EUC-KR">
<title>기사 목록</title>
<script>
function certifing() {
	document.check
}

function check() {
	goPage(1);
}

function goPage(curr){
	var frm = document.pageForm;
	frm.curr_page.value = curr;
	frm.submit();
}
</script>
</head>
<body>
	<%if(!certifyCheck) {%><h3>승인 대기자</h3>
	<%} else {%><h3>기사 목록</h3>
	<%} %>
	<form method=POST name="pageForm" action="ccr?cmd=driverList">
		<input type="hidden" name="curr_page" value="<%=plCurrPage%>" /> <input
			type="hidden" name="pageSize" value="<%=pageSize%>" /> <input
			type="checkbox" name="certifyCheck"
			<%=certifyCheck ? "checked=checked" : "" %>
			onclick='JavaScript:check()' /> 승인
	</form>
	<br>
	<br>
	<form method=POST name="checkForm" action="ccr?cmd=certifingDriver">
		<table width="90%" cellpadding=0 cellspacing=0>
			<tr>
				<td>선택</td>
				<td>No</td>
				<td>이름</td>
				<td>ID</td>
				<td>휴대폰</td>
				<td>주민번호</td>
				<td>증권번호</td>
				<td>이메일</td>
				<td>운전면허</td>
				<td>경력</td>
				<td>근무회사</td>
				<td>주소</td>
				<td>수신동의</td>
			</tr>
			<%
				Driver rd = new Driver();
				Driver[] list = (Driver[]) request.getAttribute("list");
				if(list.length>0){
					
				for (int i = 0; i < list.length; i++) {
					rd = list[i];
					
					String license = "";
					if(rd.getLicenseType()==2) license = "1종보통";
					else if(rd.getLicenseType()==3) license = "2종보통";
					else if(rd.getLicenseType()==1) license = "1종대형";
					else license = "기타";
					if(rd.isLicenseAuto()) license += " 자동";
					else license += " 수동";
					
					String agree = "";
					if(rd.getAgreeReceive()==1) agree = "SMS,email";
					else if(rd.getAgreeReceive()==1) agree = "SMS";
					else if(rd.getAgreeReceive()==2) agree = "email";
					
			%>
			<tr>
				<td><input type="checkbox" name="check" value=<%=i + 1%> /></td>
				<td><%=i + 1%></td>
				<td><%=rd.getName()%></td>
				<td><%=rd.getDriverId()%><input type="hidden" name="driverId"
					value="<%=rd.getDriverId()%>" /></td>
				<td><%=rd.getPhoneNum()%></td>
				<td><%=rd.getResidentNo()%></td>
				<td><%=rd.getAuthorizationNo()%></td>
				<td><%=rd.getEmail()%></td>
				<td><%=license%></td>
				<td><%=rd.getCareer()%></td>
				<td><%=rd.getCompany()%></td>
				<td><%=rd.getAddress()%></td>
				<td><%=agree%></td>
			</tr>
			<%
				}
			}
			%>
		</table>
		<input type="submit" value="승인" /> <input type="reset" value="취소" />
	</form>
	<table width="90%">
		<tr>
			<td align="center"><%=PagingHelper.instance.autoPaging(plTotalCnt, pageSize, plPageRange, plCurrPage)%>
			</td>
		</tr>
	</table>
</body>
</html>