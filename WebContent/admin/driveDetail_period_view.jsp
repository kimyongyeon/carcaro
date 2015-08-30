<%@page import="carcaro.util.Util"%>
<%@page import="carcaro.util.PagingHelper" %>
<%@page import="carcaro.Codes"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.concurrent.ConcurrentHashMap"%>
<%@page import="org.joda.time.LocalDate" %>
<%@page import="org.joda.time.DurationFieldType" %>
<%@page import="org.joda.time.Days" %>
<%@page import="org.joda.time.DateTime" %>
<%@page import="org.joda.time.format.DateTimeFormat" %>
<%@page import="org.joda.time.format.DateTimeFormatter" %>
<%@page import="org.joda.time.ReadableInstant" %>
<%@page import="carcaro.bean.ChargeHistory"%>
<%@page import="carcaro.bean.ChargeHistoryPeriod"%>
<%@ page import="java.util.List" %>

<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>


<!-- 기간별 조회 페이지 입니다 -->
<%
// NO LONGER PAGED
//long plTotalCnt = (Long)request.getAttribute("plTotalCnt"); // 전체 레코드 수
//long pageSize = (Long)request.getAttribute("pageSize"); // 한 페이지당 레코드 수
//long plPageRange = 5; // 페이지 출력 범위
//long plCurrPage = (Long)request.getAttribute("pageNum"); // 현재 페이지
%>
<%
	String today = Util.getCurrDate();
	String msg1 = (String) request.getAttribute("msg1");
	String dateA= (String)request.getAttribute("dateA");
	String dateB= (String)request.getAttribute("dateB");
	String local = (String)request.getAttribute("local");
	String result = (String) session.getAttribute("result");
	//String name = Util.NVL((String)request.getAttribute("name"));
	
	
	// Session info
	String aL = request.getParameter("aL"); // access Level
	String location = request.getParameter("location");
	
	
	ArrayList<String> ids = (ArrayList<String>) request.getAttribute("ids");
	//ChargeHistoryPeriod[] list = (ChargeHistoryPeriod[]) request.getAttribute("list");
	ConcurrentHashMap<String, ChargeHistoryPeriod> list =  (ConcurrentHashMap<String, ChargeHistoryPeriod>) request.getAttribute("list");
	
	
	// 날짜별로 계산 할 필요가 있다
	// 시작일 -> 마지막 날짜 사이의 날짜를 빼온다.
	
	
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script src="./js/jquery-1.4.4.min.js"></script>
<script src="./js/jquery.mousewheel.min.js"></script>
<script src="./js/calendar_ahmax.js"></script>
<link rel='stylesheet' href='./js/calendar_ahmax.css' type='text/css' />
<link rel='stylesheet' href='/css/style.css' type='text/css' />
<script type="text/javascript">
	
	var ids =<%=ids != null ? ids.size() : 0%>;
	var msg1 ="<%=msg1%>";
	var idsArray = "<%=ids %>";
	
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
<h3>기간별 대리운전 조회</h3>
<body>
<div id="stylized" >

	<br />
	<form name="search" method=POST action="./ccr?cmd=driveDetail&mode=period">
	
	<!-- MENU TABLE -->
	<jsp:include page="driveDetailTopMenu.jsp" flush="false" />
	
	
	
	
	<!-- SEARCH TABLE -->
	<table class="res">
	<thead>
		<tr>
			<th>시작일</th>
			<th>종료일</th>
			<th>시/도</th>
			<th>결과</th>
		</tr>
	</thead>
		<tr><%	if(dateA==null) dateA=today;
		if(dateB==null) dateB=today;
		%>
			<td><input type="text" id="cal1" name="dateA" value="<%=dateA%>">
				<script type="text/javascript">
				<!--
					initCal("cal1");					
				//-->
				</script>
			</td>
			<td><input type="text" id="cal2" name="dateB" value="<%=dateB%>">
				<script type="text/javascript">
				<!--
					initCal("cal2");					
				//-->
				</script>
			</td>
			<td><select name=local >
						 <%if (aL.equals("1")){ %>
						  <option value="%" selected="selected">전국
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
            <%} else if ( aL.equals("10")){ %>
            			  <option value="<%=location %>" selected="selected"><%=location %>
        	<%} else { %>
        				<option value="none" selected="selected">선택불가</option>
        	<%} %>
                </select></td>
			<td><select name=result >
                          <option value="%">모두
                          <option value="1">완료
                          <option value="2">고객취소
                          <option value="3">기사취소
                </select></td>

			<td><input type=submit size=20 value="조회"></td>
		</tr>
	</table>
	</form>
	
	
	<table class="res">
	<thead>
	<tr>
		<th>NO</th>
		<th>날짜</th>
		<th>완료</th>
		<th>고객취소</th>
		<th>기사취소</th>
		<th>거래수</th>
		<th>수수료</th>
		<th>요금</th>
	</tr>
	</thead>
	<%
	int businessSum = 0;
	int feeSum = 0; 
	int chargeSum = 0;
	int b_ok=0; //완료
	int b_cc=0;	//고객취소
	int b_dc=0;	//기사취소

	%>
	<%if(list.size()==0) { %>
		<tr><td colspan="10">결과가 없습니다.</td></tr>
	<%}else { %>
	
	<%
			DateTimeFormatter fmt = DateTimeFormat.forPattern("y-MM-dd");		// FORMATTER YYYY-MM-DD
				DateTime start = DateTime.parse(dateA,fmt);
				DateTime end = DateTime.parse(dateB,fmt);

				List<DateTime> dTime = new ArrayList<DateTime>();
		    	DateTime tmp = end;
		    	int index = 1;
				while(tmp.isAfter(start)|| tmp.equals(start)) {
			ChargeHistoryPeriod ch = list.get(tmp.toString(fmt));
			feeSum += ch.getTotalFee();
			chargeSum += ch.getTotalCharge();
			//int[] businessTypeCount = {0,0,0};
			int[] businessTypeCount = {ch.getBusinessTypeCount(0),ch.getBusinessTypeCount(1),ch.getBusinessTypeCount(2)}; 
			businessSum += businessTypeCount[ch.B_STATUS_CC-1]+businessTypeCount[ch.B_STATUS_CC-1]+businessTypeCount[ch.B_STATUS_OK-1];			
			//총거래수
		 	b_ok += businessTypeCount[ch.B_STATUS_OK-1];		// OK
		 	b_cc += businessTypeCount[ch.B_STATUS_CC-1];	// Customer Cancel
			b_dc += businessTypeCount[ch.B_STATUS_DC-1];		// Driver Cancel
			tmp = tmp.minusDays(1);
		%>
		<tr class="element" >
			<td><%=list.size()-index %></td>
			<td><%=ch.getBusinessDate() %></td>
			<td><%=businessTypeCount[ch.B_STATUS_OK-1] %></td> <!-- 완료 -->
			<td><%=businessTypeCount[ch.B_STATUS_CC-1] %></td>
			<td><%=businessTypeCount[ch.B_STATUS_DC-1] %></td>
			<td><%=businessTypeCount[ch.B_STATUS_CC-1]+businessTypeCount[ch.B_STATUS_CC-1]+businessTypeCount[ch.B_STATUS_OK-1] %></td>
			<td><%=ch.getTotalFee() %></td>
			<td><%=ch.getTotalCharge() %></td>
		</tr>
		<% index ++;
		
		} %>
	<% } %>
	</table>
	
	<!-- Paging -->
	<table class="res">
	<tr>
		<td align="center">
		</td>
	</tr>
	<tr>
		<td>합계 	:거래수 <%=businessSum%>건 | 완료 <%=b_ok%>건 | 고객취소<%=b_cc%> | 기사취소<%=b_dc %> |	수수료<%=feeSum %>원 |   요금:<%=chargeSum %> </td>
	</tr>
	</table>
	
	</div>
</body>
</html>