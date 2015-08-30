<%@page import="carcaro.util.Util"%>
<%@page import="carcaro.util.PagingHelper"%>
<%@page import="carcaro.Codes"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.ArrayList"%>
<%@page import="carcaro.bean.ChargeHistory"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="java.util.List"%>
<%@ page import="carcaro.bean.ChargeHistory"%>
<%
long plTotalCnt = (Long)request.getAttribute("plTotalCnt"); // 전체 레코드 수
long pageSize = (Long)request.getAttribute("pageSize"); // 한 페이지당 레코드 수
long plPageRange = 5; // 페이지 출력 범위
long plCurrPage = (Long)request.getAttribute("pageNum"); // 현재 페이지
%>
<%
	String today = Util.getCurrDate();
	String msg1 = (String) request.getAttribute("msg1");
	String date = (String)request.getAttribute("date");
	String local = (String)request.getAttribute("local");
	String result = (String) session.getAttribute("result");
	String name = Util.NVL((String)request.getAttribute("name"));
	
	
	ArrayList<String> ids = (ArrayList<String>) request.getAttribute("ids");
	ChargeHistory[] list = (ChargeHistory[]) request.getAttribute("list");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>일일대리운전조회</title>
<script src="./js/jquery-1.4.4.min.js"></script>
<script src="./js/jquery.mousewheel.min.js"></script>
<script src="./js/calendar_ahmax.js"></script>
<link rel='stylesheet' href='./js/calendar_ahmax.css' type='text/css' />
<script type="text/javascript">
	
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

//		alert(f.value);
		document.search.submit();
	}
	function goPage(curr){
		var frm = document.search;
		frm.curr_page.value = curr;
		frm.submit();
	}
</script>
</head>
<body>
	<h3>일일대리운전조회</h3>
	<br>
	<br>
	<form name="search" method=POST action="./ccr?cmd=dayDriveDetail">
		<table width="90%" cellpadding=0 cellspacing=0>
			<tr>
				<td>조회 날짜</td>
				<td>지역</td>
				<td>결과</td>
				<td>기사 이름</td>
				<td></td>
			</tr>
			<tr>
				<%if(date==null) date=today; %>
				<td><input type="text" id="cal1" name=date value="<%=date%>">
					<script type="text/javascript">
				<!--
					initCal("cal1");					
				//-->
				</script></td>
				<td><select name=local>
						<option value="%" selected="true">전국
						<option value="강원">강원
						<option value="경기">경기
						<option value="경남">경남
						<option value="경북">경북
						<option value="광주">광주
						<option value="대구">대구
						<option value="대전">대전
						<option value="부산">부산
						<option value="서울">서울
						<option value="울산">울산
						<option value="인천">인천
						<option value="전남">전남
						<option value="전북">전북
						<option value="제주">제주
						<option value="충남">충남
						<option value="충북">충북
				</select></td>
				<td><select name=result>
						<option value="%">모두
						<option value="1">완료
						<option value="2">고객취소
						<option value="3">기사취소
				</select></td>
				<td><input type=text name=name size=20 value="<%=name%>"></td>

				<td><input type=submit size=20 value="조회"></td>
			</tr>
		</table>
		<input type="hidden" name="curr_page" value="<%=plCurrPage%>" /> <input
			type="hidden" name="pageSize" value="<%=pageSize%>" />
	</form>

	<table width="90%" cellpadding=0 cellspacing=0 border=1>
		<tr>
			<th>BID</th>
			<th>날짜</th>
			<th>출발지</th>
			<th>도착지</th>
			<th>차감액</th>
			<th>대리기사ID</th>
			<th>대리기사이름</th>
			<th>고객번호</th>
			<th>결과</th>
			<th>대리금액</th>
		</tr>
		<%if(list.length==0) { %>
		<tr>
			<td colspan="10"><%=name %>으로 검색한 결과가 없습니다.</td>
		</tr>
		<%}else { %>

		<%for(int i=0; i<list.length; i++) {
			ChargeHistory ch = list[i];%>
		<tr>
			<td><%=ch.getBid() %></td>
			<td><%=ch.getBusinessTime() %></td>
			<td><%=ch.getSource() %></td>
			<td><%=ch.getDestination() %></td>
			<td><%=ch.getFee() %></td>
			<td><%=ch.getDriverId() %></td>
			<td><%=ch.getDriverName() %></td>
			<td><%=ch.getCustomerPhone() %></td>
			<td><%=Codes.getStatus(ch.getBusinessType()) %></td>
			<td><%=ch.getDrivingCharge() %></td>
		</tr>
		<% } %>
		<%} %>
	</table>
	<table width="90%">
		<tr>
			<td align="center"><%=PagingHelper.instance.autoPaging(plTotalCnt, pageSize, plPageRange, plCurrPage)%>
			</td>
		</tr>
	</table>

</body>
</html>