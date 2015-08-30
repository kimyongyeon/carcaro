<%@page import="carcaro.util.Util"%>
<%@page import="carcaro.util.PagingHelper" %>
<%@page import="carcaro.Codes"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.concurrent.ConcurrentHashMap"%>
<%@page import="carcaro.bean.Customer"%>
<%@page import="carcaro.ConnectionPool" %>
<%@page import="carcaro.dao.LocationDAO" %>
<%@page import="java.util.List" %>
<%@page import="net.sf.json.JSONObject" %>
<%@page import="net.sf.json.JSONArray" %>
<%@page import="org.joda.time.LocalDate" %>
<%@page import="org.joda.time.DurationFieldType" %>
<%@page import="org.joda.time.Days" %>
<%@page import="org.joda.time.DateTime" %>
<%@page import="org.joda.time.ReadableInstant" %>

<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%
long plTotalCnt = (Long) request.getAttribute("plTotalCnt"); // 전체 레코드 수
long pageSize = (Long) request.getAttribute("pageSize"); // 한 페이지당 레코드 수
long plPageRange = 5; // 페이지 출력 범위
long plCurrPage = (Long) request.getAttribute("pageNum"); // 현재 페이지
%>
<%

String today = Util.getCurrDate(); // 오늘날짜
Customer[] list = (Customer[]) request.getAttribute("list"); // 레코드값
String dateA = (String) request.getAttribute("dateA"); // 시작일시
String dateB = (String) request.getAttribute("dateB"); // 끝일시
String name = (String) request.getAttribute("keyword"); // 키워드

	
	ArrayList<String> ids = (ArrayList<String>) request.getAttribute("ids");
	String gubun = (String)request.getAttribute("searchSelector"); // 구분자: nameName, usrNum, couponNum
	// 신규달력: 추가 사항
	java.util.Calendar cal = java.util.Calendar.getInstance();
	int year = cal.get ( cal.YEAR );
%>
<jsp:include page="../js/header.jsp" />
<html>
<head>
<script type="text/javascript">
	// 신규달력 공통 함수=========================================================S
	$(function() {
		// 신규 달력: 시작달력 
		$("#dateA").datepicker(
		{
			monthNamesShort : [ '1월', '2월', '3월', '4월', '5월', '6월',
					'7월', '8월', '9월', '10월', '11월', '12월' ],
			dayNamesMin : [ '일', '월', '화', '수', '목', '금', '토' ],
			weekHeader : 'Wk',
			dateFormat : 'yy-mm-dd', //형식(2012-03-03)
			autoSize : false, //오토리사이즈(body등 상위태그의 설정에 따른다)
			changeMonth : true, //월변경가능
			changeYear : true, //년변경가능
			showMonthAfterYear : true, //년 뒤에 월 표시
			buttonImageOnly : true, //이미지표시
			buttonImage : './js/images/cal.png', //이미지주소
			showOn : "both", //엘리먼트와 이미지 동시 사용
			yearRange : '2005:<%=year+1%>'
		});
		// 신규 달력: 종료달력
		$("#dateB").datepicker(
		{
			monthNamesShort : [ '1월', '2월', '3월', '4월', '5월', '6월',
					'7월', '8월', '9월', '10월', '11월', '12월' ],
			dayNamesMin : [ '일', '월', '화', '수', '목', '금', '토' ],
			weekHeader : 'Wk',
			dateFormat : 'yy-mm-dd', //형식(2012-03-03)
			autoSize : false, //오토리사이즈(body등 상위태그의 설정에 따른다)
			changeMonth : true, //월변경가능
			changeYear : true, //년변경가능
			showMonthAfterYear : true, //년 뒤에 월 표시
			buttonImageOnly : true, //이미지표시
			buttonImage : './js/images/cal.png', //이미지주소
			showOn : "both", //엘리먼트와 이미지 동시 사용
			yearRange : '2005:<%=year+1%>'
		});
	});
	// 신규달력 공통 함수=========================================================E
	if (ids) {
		//selectPopup();

	} else {
		if (msg1 != "null")
			alert(msg1);
	}

	function selectPopup() {
		window.open("carcaro/selectId.html", "selectId",
				"status = 1, height = 200, width = 340, resizable = 0");
	}

	function selectDriverIdFromPopup(id) {
		var n = document.getElementsByName("name")[0];
		var f = document.getElementsByName("driverId")[0];
		n.value = name;
		f.value = id;

		//	alert(f.value);
		document.search.submit();
	}
	function goPage(curr) {
		var frm = document.search;
		frm.curr_page.value = curr;
		frm.submit();
	}
	
</script>
</head>
<body>
	<br />
		<h3>고객관리</h3>
<form name="search" method=POST action="./ccr?cmd=customerManager&mode=list">
	<input type="hidden" name="curr_page" value="<%=plCurrPage%>" /> 
	<input type="hidden" name="pageSize" value="<%=pageSize%>" />

	<!-- SEARCH TABLE -->
	<table class="res">
	<thead>
	<tr>
	<th>시작일</th>
	<th>종료일</th>
	<th>선택</th>
	<th>입력</th>
	<th>검색</th>
	</tr>
	</thead>
		<tr><%	if(dateA==null) dateA=today;
		if(dateB==null) dateB=today;
		%>
			<!-- 신규달력: 달력표시 영역 S -->
			<td><input type="text" id="dateA" name="dateA" value="<%=dateA%>"></td>
			<td><input type="text" id="dateB" name="dateB" value="<%=dateB%>"></td>
			<!-- 신규달력: 달력표시 영역 E -->
			
			<td><select id="searchSelector" name="searchSelector">
			<option value="0">이름 </option>
			<option value="1">고객전화</option>
			<option value="2">쿠폰</option>
			</select></td>
			
			<td><input type=text name="keyword" id="keyword" value="<%=name%>"></td>
			<td><input type=submit size=20 value="조회"></td>
		</tr>
	</table>

	
	<table class="res">
	<thead>

	<tr>
		<th>NO.</th>
		<th>날짜</th>
		<th>고객전화</th>
		<th>고객이름</th>
		<th>출발지</th>
		<th>목적지</th>
		<th>기사전화</th>
		<th>기사이름</th>
		<th>요금</th>
		<th>쿠폰</th>
	</tr>
	<%
	long totAmount =0;
	long avg=0;
	%>
	
<%if(list.length==0) { %>
		<tr><td colspan="10"><%=name %>으로 검색한 결과가 없습니다.</td></tr>
<%	}else{%>
<%		for(int i=0; i<list.length; i++) {
		Customer ct = list[i];	
		totAmount += Long.valueOf(ct.getAmount()).longValue()*plTotalCnt;
		avg= totAmount / plTotalCnt;
%>
		<tr class="element">
			<td><%=i+1%></td>
			<td><%=(ct.getBusinessTime()).split(" ")[0] %></td>
			<td><%=ct.getCustomerPhone().substring(0, 3)+"-"+ct.getCustomerPhone().substring(3, 7)+"-"+ct.getCustomerPhone().substring(7,11) %></td>
			<td><%=ct.getCustomerName() %></td>
			<td><%=ct.getSource() %></td>
			<td><%=ct.getDestination() %></td>
			<td><%=ct.getDriverPhone().substring(0,3)+"-"+ct.getDriverPhone().substring(3,7)+"-"+ct.getDriverPhone().substring(7,11) %></td>
			<td><%=ct.getDriverName() %></td>
			<td><%=ct.getAmount() %></td>
			<td><%=ct.getCoupon_id() %></td>
		</tr>
		
		<%
	} 
%>
<%
	} 
%>


	</thead>
	
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
	<td>합계 총: 콜수<%=plTotalCnt %>회 | 대리요금<%=totAmount %> 원 | 1회 평균요금<%=avg%>원 </td>
	</tr>
	</table>	

</body>
</html>


