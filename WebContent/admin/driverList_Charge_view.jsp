<%@page import="org.apache.commons.httpclient.methods.GetMethod"%>
<%@page import="carcaro.util.PagingHelper" %>
<%@page import="carcaro.bean.Driver" %>
<%@page import="carcaro.bean.SettlementHist" %>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List" %>
<%@page import="carcaro.util.Util"%>
<%@page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%
long plTotalCnt = (Long)request.getAttribute("plTotalCnt"); // 전체 레코드 수
long pageSize = (Long)request.getAttribute("pageSize"); // 한 페이지당 레코드 수
long plPageRange = 5; // 페이지 출력 범위
long plCurrPage = (Long)request.getAttribute("pageNum"); // 현재 페이지


String msg1 = (String) request.getAttribute("msg1");
ArrayList<String> ids = (ArrayList<String>) request.getAttribute("ids");
String name = Util.NVL((String)request.getAttribute("name"));

SettlementHist[] list = (SettlementHist[]) request.getAttribute("list");
String gubun = (String) request.getAttribute("gubun"); // 0:이름, 1:ID, 2:전화번호
String keyword = (String) request.getAttribute("keyword"); // 검색어
%>
<html>
<head>
<script src="./js/jquery-1.4.4.min.js"></script>
<script src="./js/jquery.mousewheel.min.js"></script>
<script src="./js/calendar_ahmax.js"></script>
<link rel='stylesheet' href='./js/calendar_ahmax.css' type='text/css' />
<link rel='stylesheet' href='/css/style.css' type='text/css' />

<script type="text/javascript">
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
</script>
</head>
<body>
<br>
<h3>충전내역</h3>
	
<form name="search" method=POST action="./ccr?cmd=driverManager&mode=chargeList">
<jsp:include page="driverListTopMenu.jsp" flush="false" />

		<input type="hidden" name="curr_page" value="<%=plCurrPage%>" /> 
		<input type="hidden" name="pageSize" value="<%=pageSize%>" /> 

<table class="res">
	<thead>
		<tr>
			<th>구분</th>
			<th>검색어</th>
			<th>조회</th>
		</tr>
		</thead>
		<tr>	
			<td><select id="gubun" name="gubun">
				<option value="0">이름</option>
				<option value="1">ID</option>
				<option value="2">전화번호</option>
			</select></td>
			<td><input type="text" name="keyword" id="keyword" value="<%=name%>"></td>
			<td><input type="submit" value="조회"></td>
	</tr>

</table>
	<table class="res">
		<thead>
				<tr>
					<th></th>
					<th>No</th>
					<th>날짜</tH>
					<th>시간</th>
					<th>이름</th>
					<th>ID</th>
					<th>전화번호</th>
					<th>구분</th>
					<th>금액</th>
				</tr>
			
		<%
				if (list.length == 0) {
			%>
			<tr>
				<td colspan="10"><%=name%>으로 검색한 결과가 없습니다.</td>
			</tr>
			<%
				} else {
			%>

			<%
				for (int i = 0; i < list.length; i++) {
					SettlementHist sh = list[i];
			%>
			<tr class="element">
				<td><input type="checkbox" name="check"  value=<%=i + 1%> /></td>
				<td><%=i+1 %></td>
				<td><%=(sh.getRequestTime()).split(" ")[0] %></td>
				<td><%=((sh.getSettleTime()).split(" ")[1]).substring(0, 5) %></td>
				<td><%=sh.getName() %></td>
				<td><%=sh.getDriverid() %></td>
				<td><%=sh.getPhone().substring(0,3)+"-"+sh.getPhone().substring(3,7)+"-"+sh.getPhone().substring(7) %></td>
				<td><%=sh.getMethod() %></td>
				<td><%=sh.getAmount() %></td>
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
	<td>합계 : 입금 원 | 출금원 | 잔액 원 </td>
	</tr>
	</table>	

</body>
</html>