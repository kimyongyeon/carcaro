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
<%@page import="carcaro.dao.SettlementDAO" %>
<%@page import="carcaro.ConnectionPool" %>
<%@ page import="java.util.List" %>

<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%
	String today = Util.getCurrDate();
	String msg1 = (String) request.getAttribute("msg1");
	String date = (String)request.getAttribute("date");
	String local = (String)request.getAttribute("local");
	String result = (String) session.getAttribute("result");
	String name = Util.NVL((String)request.getAttribute("name"));
	
	
	ArrayList<String> ids = (ArrayList<String>) request.getAttribute("ids");
	ConcurrentHashMap<String, ChargeHistoryPeriod> list =  (ConcurrentHashMap<String, ChargeHistoryPeriod>) request.getAttribute("list");
	
	ConnectionPool connPool = ConnectionPool.getInstance();
	SettlementDAO settlementDAO = new SettlementDAO(connPool);
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
	<br />

		<h3>정산내역</h3>
<div id="stylized" >
	
	<br />
	
	<form name="search" method=POST action="./ccr?cmd=settleManager&mode=settlement">
	
	
	<!-- MENU TABLE -->
	<jsp:include page="settleManagerTopMenu.jsp" flush="false" />
	
	<!-- SEARCH TABLE -->
	<table class="res">
	<thead>
		<tr>
			<th>날짜</th>
			<th>지사/대리점</th>	
			<th>결과</th>
		</tr>
	</thead>
		<tr><%if(date==null) date=today; %>
			<td><input type="text" id="cal1" name=date value="<%=date%>">
				<script type="text/javascript">
				<!--
					initCal("cal1");					
				//-->
				</script></td>
			<td><select name=local >
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
					<th>수수료</th>
					<th>대리점</th>
					<th>지사</th>
					<th>본사</th>
					<th>할당금</th>
					<th>정산금</th>
				</tr>
			</thead>
			<%
				int businessSum = 0;
				int feeSum = 0;
				int chargeSum = 0;
				int totalBalance = 0;
				int totalAllot= 0;
				
			%>
			<%
				if (list.size() == 0) {
			%>
			<tr>
				<td colspan="10">결과가 없습니다.</td>
			</tr>
			<%
				} else {
			%>

			<%
				DateTimeFormatter fmt = DateTimeFormat.forPattern("y-MM-dd"); // FORMATTER YYYY-MM-DD
				String temp = date;
				temp = temp.substring(0,7).concat("-01");
					DateTime start = DateTime.parse(temp, fmt);
					DateTime end = start.plusMonths(1).minusDays(1);

					List<DateTime> dTime = new ArrayList<DateTime>();
					DateTime tmp = end;
					int index = 0;
					
					while (tmp.isAfter(start) || tmp.equals(start)) {
						
						ChargeHistoryPeriod ch = list.get(tmp.toString(fmt));
						int chargeSumByDate = settlementDAO.getChargeSumByDate(tmp.toString(fmt));
						feeSum += ch.getTotalFee();
						chargeSum += chargeSumByDate;
						totalBalance += (chargeSumByDate - ch.getTotalFee());
						//총 할당금
						totalAllot += (ch.getTotalFee()*0.48 +ch.getTotalFee()*0.32);
						
						tmp = tmp.minusDays(1);
						
						//int[] businessTypeCount = {0,0,0};
						/* int[] businessTypeCount = { ch.getBusinessTypeCount(0),
								ch.getBusinessTypeCount(1),
								ch.getBusinessTypeCount(2) };
						businessSum += businessTypeCount[ch.B_STATUS_CC - 1]
								+ businessTypeCount[ch.B_STATUS_CC - 1]
								+ businessTypeCount[ch.B_STATUS_OK - 1]; */
								
								
			
%>
							<tr class="element">
								<td><%=list.size() - index %></td>
								<td><%=ch.getBusinessDate()%></td>
								<td><%=ch.getTotalFee()%></td>
								<td><%=ch.getTotalFee()*0.48 %></td>
								<td><%=ch.getTotalFee()*0.32 %></td>
								<td><%=ch.getTotalFee()*0.20 %></td>
								<!-- 할당금 : 지사(대리점) 수수료할당금으로 입금에 해당된다 (이 페이지엔 본사포함하지 않았습니다)  -->
								<td><%=ch.getTotalFee()*0.48 +ch.getTotalFee()*0.32 %></td> 
								<!-- 정산금 : 지사(대리점)에게 정산해주는 금액으로 출금에 해당된다  할당금-잔액 -->
								<td><%=(ch.getTotalFee()*0.48+ch.getTotalFee()*0.32)- ch.getTotalFee()%>원</td> 
							</tr>
<%
						index++;

					}
%>
<%
				}
%>
		</table>
	
	
	<!-- Paging -->
	<table class="res">
	<tr>
	<td align="center">
	<div class="pager">
	</div>
	</td>
	</tr>
	<tr>
	<td>합계  할당금:<%=totalAllot %>원 | 정산금:<%= totalAllot - totalBalance %>원 | 잔액:<%=totalBalance %>원
	</tr>
	</table>	
			<table class="res">
  <tr>
    <td width="104">지사/대리점명</td>
    <td width="180">&nbsp;</td>
  </tr>
  <tr>
    <td>정산일</td>
    <td>&nbsp;</td>
  </tr>
  <tr>
    <td>정산금</td>
    <td><form id="form1" name="form1" method="post" action="">
      <input type="text"  />
      <input type="submit" name="button" id="button" value="정산" />
    </form></td>
  </tr>
  <tr>
    <td colspan="2" align="center">msg</td>
  </tr>
  <tr>
    <td colspan="2" align="center">잔액 원</td>
  </tr>
</table>
	</div>

</body>
</html>