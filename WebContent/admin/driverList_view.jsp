<%@page import="carcaro.util.PagingHelper"%>
<%@page import="carcaro.bean.Driver"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List" %>
<%@page import="carcaro.util.Util"%>
<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%
	long plTotalCnt = (Long) request.getAttribute("plTotalCnt"); // 전체 레코드 수
	long pageSize = (Long) request.getAttribute("pageSize"); // 한 페이지당 레코드 수
	long plPageRange = 5; // 페이지 출력 범위
	long plCurrPage = (Long) request.getAttribute("pageNum"); // 현재 페이지
	boolean certifyCheck = Boolean.parseBoolean((String) request.getAttribute("certifyCheck")); //승인(true)/미승인(false) 사용자
			
	String msg1 = (String) request.getAttribute("msg1");
	ArrayList<String> ids = (ArrayList<String>) request.getAttribute("ids");
	String name = Util.NVL((String)request.getAttribute("name"));
	String driverId = (String) request.getAttribute("driverId");
	String level = (String) request.getAttribute("level");
	String gubun = (String) request.getAttribute("gubun");
	String keyword = (String) request.getAttribute("keyword");
%>
<jsp:include page="../js/header.jsp" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title></title>
<script>

	var ids =<%=ids != null ? ids.size() : 0%>;
	var msg1 ="<%=msg1%>";
	var idsArray = "<%=ids %>";
	var name = "<%=name%>";
	
	if( ids ){
		//selectPopup();
		
	} else {
		if( msg1 != "null" )
		alert(msg1);
	}
	
	function selectPopup() {
		window.open( "carcaro/selectId.html", "selectId", 
		"status = 1, height = 200, width = 340, resizable = 0" );
	}
	
	function selectDriverIdFromPopup(id){
		var n = document.getElementsByName("name")[0];
		var f = document.getElementsByName("driverId")[0];
		n.value = name;
		f.value = id;
	
	//	alert(f.value);
		document.search.submit();
	}
	function goPage(curr){
		var frm = document.search;
		frm.curr_page.value = curr;
		frm.submit();
	}
	function certifing() {
			document.check;
	}

	
	function driverRegist() 
	{ 
	        window.showModalDialog("admin/driverRegister_view.jsp", self, "(window.screen.width / 2) - (Number(iWidth) / 2); (window.screen.height / 2) - (Number(iHeight) / 2); dialogWidth:800px; dialogHeight:450px"); 
	} 
	 
	function driverUpdate(checkForm) {
		var selectedCheckId=0;
		var selected = false;
		  var num = 0;
		  var form = document.checkForm;
		  if(form.check.length > 1){
		   for(i = 0; i< form.check.length; i++){
		    if(form.check[i].checked){
		     num = form.check[i].value;
		     selectedCheckId = i;
		     selected = true;
		     break; 
		    }
		   }
		  } else {
		   if(form.check.checked){
		    num = form.check.value;
		    selected = true;
		   }
		  }
		  if(selected){
			 	var driverId = form.driverId[selectedCheckId].value;
			 	alert(driverId);
			  	window.showModalDialog('./ccr?cmd=driverManager&mode=updateDetail&driverId='+driverId, "", "(window.screen.width / 2) - (Number(iWidth) / 2); (window.screen.height / 2) - (Number(iHeight) / 2); dialogWidth:1050px; dialogHeight:500px"); 
		  }else{
		   alert("수정할 항목을 먼저 선택해 주세요.");
		  }
		
	}	

	function driverDelete(checkForm) {
		var selectedCheckId=0;
		var selected = false;
		  var num = 0;
		  var form = document.checkForm;
		  if(form.check.length > 1){
		   for(i = 0; i< form.check.length; i++){
		    if(form.check[i].checked){
		     num = form.check[i].value;
		     selectedCheckId = i;
		     selected = true;
		     break; 
		    }
		   }
		  } else {
		   if(form.check.checked){
		    num = form.check.value;
		    selected = true;
		   }
		  }
		  if(selected){
			  var up_con = confirm("삭제하시겠습니까?");
			   if(up_con == true){    
				   	var driverId = form.driverId[selectedCheckId].value;
				   	alert(driverId);
				 	var url = "./ccr?cmd=driverManager&mode=delete";
					var params = "driverId="+driverId;
					//sendRequest(url, params, registerResult, "POST");
					var response = $.ajax({
						type : 'post',
						async: false,
						url : url,
						data : params
					}).responseText;
					alert(response);
				} 
		  	}else{
		   alert("삭제할 항목을 먼저 선택해 주세요.");
		 }
	}
	
	function check(checkForm) {
		var selectedCheckId=0;
		var selected = false;
		  var num = 0;
		  var form = document.checkForm;
		  if(form.check.length > 1){
		   for(i = 0; i< form.check.length; i++){
		    if(form.check[i].checked){
		     num = form.check[i].value;
		     selectedCheckId = i;
		     selected = true;
		     break; 
		    }
		   }
		  } else {
		   if(form.check.checked){
		    num = form.check.value;
		    selected = true;
		   }
		  }
		  if(selected){
			  var up_con = confirm("승인하시겠습니까");
			   if(up_con == true){    
				var driverId = form.driverId[selectedCheckId].value; 
				var level = form.level[selectedCheckId].value;
				//alert(driverId);
				//alert(level);
				var url = "./ccr?cmd=driverManager&mode=levelUp";
				var params = "driverId="+driverId;
				params += "&level="+level;
				//alert(params);
				//sendRequest(url, params, registerResult, "POST");
				var response = $.ajax({
				    type : 'post',
				    async: false,
				    url : url,
				    data : params
				}).responseText;
				alert(response);
				//alert("승인되었습니다");
				goPage(1);
				} 
		  	}else{
		   alert("항목을 먼저 선택해 주세요.");
		 }
	}
</script>
</head>
<body>
	<%
		if (!certifyCheck) {
	%>
	<%
		} else {
	%>
	<%
		}
	%>

	
<table class="res">
<tr>
	<td><form method=POST name="search" action="ccr?cmd=driverManager&mode=driverList">
		<jsp:include page="driverListTopMenu.jsp" flush="false" />
		
		<input type="hidden" name="curr_page" value="<%=plCurrPage%>" /> 
		<input type="hidden" name="pageSize" value="<%=pageSize%>" /> 
		<select	id="level" name="level">
		<!-- <option value="%">전체</option>-->
			<option value="0">승인</option>
			<option value="1">대기</option>
		</select> 
		<select id="gubun" name="gubun">
			<option value="0">이름</option>
			<option value="1">ID</option>
			<option value="2">전화번호</option>
		</select>
			<input type="text" name="keyword" id="keyword" value="<%=name%>"> 
			<input type="submit" size=20 value="조회"> 
			<input type="button" value="승인" name="certifyCheck"	<%=certifyCheck ? "checked=checked" : ""%> onclick='JavaScript:check()' />
			<input type="button" value="등록"  onclick="JavaScript:driverRegist()" /> 
			<input type="button" value="수정" <%=certifyCheck ? "checked=checked" : ""%> onclick="JavaScript:driverUpdate()" />
			<input type="button" value="삭제"onClick="driverDelete();">
	</form>

 
	</td>
	</tr>
</table>	
	<br>
	<br>
	<form method=POST name="checkForm" action="ccr?cmd=driverManager&mode=certify">
		<table class="res">
			<thead>
				<tr>
					<th></th>
					<th>No</th>
					<th>이름</tH>
					<th>ID</th>
					<th>PW</th>
					<th>전화번호</th>
					<th>주민번호</th>
					<th>면허</th>
					<th>보험</th>
					<th>보험만료일</th>
					<th>충전금</th>
					<th>승인상태</th>
				</tr>
			</thead>
			<%
				Driver rd = new Driver();
				Driver[] list = (Driver[]) request.getAttribute("list");
				if (list.length > 0) {

					for (int i = 0; i < list.length; i++) {
						rd = list[i];

						String license = "";
						if (rd.getLicenseType() == 2)
							license = "1종보통";
						else if (rd.getLicenseType() == 3)
							license = "2종보통";
						else if (rd.getLicenseType() == 1)
							license = "1종대형";
						else
							license = "기타";
						if (rd.isLicenseAuto())
							license += " 자동";
						else
							license += " 수동";

						String stateString = "";
						int state = rd.getState();
						if (state == Driver.ISNOTCERTIFIED) {
							stateString = "대기";
						} else if (state == Driver.ISCERTIFIED) {
							stateString = "승인";
						} else {
							stateString = "알 수 없음";
						}

						String agree = "";
						if (rd.getAgreeReceive() == 1)
							agree = "SMS,email";
						else if (rd.getAgreeReceive() == 1)
							agree = "SMS";
						else if (rd.getAgreeReceive() == 2)
							agree = "email";
			%>
			<tr class="element">
				<td><input type="checkbox" name="check" value=<%=i + 1%> /></td>
				<td><%=i + 1%></td>
				<td><%=rd.getName()%></td>
				<td><%=rd.getDriverId()%><input type="hidden" name="driverId" id="driverId"  value='<%=rd.getDriverId() %>'/></td>
				<td><%=rd.getPasswd()%></td>
				<td><%=rd.getPhoneNum().substring(0, 3) + "-"
							+ rd.getPhoneNum().substring(3, 7) + "-"
							+ rd.getPhoneNum().substring(7)%></td>
				<td><%=rd.getResidentNo().substring(0, 6) + "-"
							+ rd.getResidentNo().substring(6)%></td>
				<td><%=license%></td>
				<td><%=rd.getAuthorizationName() %></td>
				<td><%=rd.getLiceseNo() %></td>
				<td><%=rd.getChargeSum()%></td>
				<td><%=stateString%></td>
			</tr>
			<input type="hidden" name="level" id="level" value='<%=level %>'/>
			<%
				}
				}
			%>
		</table>
</form>
			<!-- Paging -->
	<table class="res">
	<tr>
	<td align="center">
	<div class="pager">
		<%=PagingHelper.instance.autoPaging(plTotalCnt, pageSize, plPageRange, plCurrPage)%>
	</div>
	</td>
	</tr>
	<tr>
	<td>지사 개 | 대리점 개 </td>
	</tr>
	</table>	

</body>
</html>