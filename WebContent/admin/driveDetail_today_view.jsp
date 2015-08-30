<%@page import="carcaro.util.Util"%>
<%@page import="carcaro.util.PagingHelper"%>
<%@page import="carcaro.Codes"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.concurrent.ConcurrentHashMap"%>
<%@page import="carcaro.bean.ChargeHistory"%>
<%@page import="carcaro.bean.ChargeHistoryPeriod"%>
<%@page import="carcaro.ConnectionPool"%>
<%@page import="carcaro.dao.LocationDAO"%>
<%@page import="java.util.List"%>
<%@page import="carcaro.bean.ChargeHistory"%>
<%@page import="net.sf.json.JSONObject"%>
<%@page import="net.sf.json.JSONArray"%>

<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>


<%
	long plTotalCnt = (Long) request.getAttribute("plTotalCnt"); // 전체 레코드 수
	long pageSize = (Long) request.getAttribute("pageSize"); // 한 페이지당 레코드 수
	long plPageRange = 5; // 페이지 출력 범위
	long plCurrPage = (Long) request.getAttribute("pageNum"); // 현재 페이지
%>
<%
	String today = Util.getCurrDate();
	String msg1 = (String) request.getAttribute("msg1");
	String dateA = (String) request.getAttribute("dateA");
	String local = (String) request.getAttribute("local");
	String result = (String) session.getAttribute("result");
	String name = Util.NVL((String) request.getAttribute("name"));


	String aL = request.getParameter("aL"); // access Level
	String location = request.getParameter("location");

	ArrayList<String> ids = (ArrayList<String>) request
			.getAttribute("ids");
	ChargeHistory[] list = (ChargeHistory[]) request
			.getAttribute("list");

	// connPool
	ConnectionPool connPool = ConnectionPool.getInstance();
	LocationDAO locationDAO = new LocationDAO(connPool);
	// 시
	JSONArray Si = locationDAO.getSido(local);
	// 구
	String temp1 = Si.getJSONObject(0).getString("Sido").toString();
	JSONArray Gu = locationDAO.getGugun(temp1);
	// 동
	String temp2 = Gu.getJSONObject(0).getString("Gugun").toString();
	JSONArray Dong = locationDAO.getDong(temp2);

%>
<jsp:include page="../js/header.jsp" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script>
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
	
	//	alert(f.value);
		document.search.submit();
	}
	function goPage(curr){
		var frm = document.search;
		frm.curr_page.value = curr;
		frm.submit();
	}

	$(function()
	{
		// 동적 셀렉트 초기화 작업
		// 동적 셀렉트 예제 
		var Si = <%=Si%>;
		var opt = "";
		for(var i=0; i<Si.length; i++){
			opt += "<option value=" + Si[i].Sido + " selected>" + Si[i].Sido + "</option>";
		}
		$("#Si").html(opt);
		
		// 동적 셀렉트 예제
		var Gu = <%=Gu%>;
		var opt = "";
		for(var i=0; i<Gu.length; i++){
			opt += "<option value=" + Gu[i].Gugun + " selected>" + Gu[i].Gugun + "</option>";
		}
		$("#Gu").html(opt);
		
		// 동적 셀렉트 예제
		var Dong = <%=Dong%>;
		var opt = "";
		for(var i=0; i<Dong.length; i++){
			opt += "<option value=" + Dong[i].Dong + " selected>" + Dong[i].Dong + "</option>";
		}
		$("#Dong").html(opt);
		
		// 콤보 박스 선택시 inputbox에 값 대입
		$("select[name=Si]").bind("change", function()
		{
			var url = "./ccr?cmd=Si";
			var params = "Si="+$(this).val();
			var response = $.ajax({
				type : 'post',
				async: false,
				url : url,
				data : params
			}).responseText;
			var evalData = eval(" (" + response +") ");
			var re = evalData;
			var Gu = re.Gu;
			var opt = "";
			for(var i=0; i<Gu.length; i++){
				opt += "<option value=" + Gu[i].Gugun + " selected>" + Gu[i].Gugun + "</option>";
			}
			$("#Gu").html(opt);
		});
		
		// 콤보 박스 선택시 inputbox에 값 대입
		$("select[name=Gu]").bind("change", function()
		{
			var url = "./ccr?cmd=Gu";
			var params = "Gu="+$(this).val();
			var response = $.ajax({
				type : 'post',
				async: false,
				url : url,
				data : params
			}).responseText;
			var evalData = eval(" (" + response +") ");
			var re = evalData;
			var Dong = re.Dong;
			var opt = "";
			for(var i=0; i<Dong.length; i++){
				opt += "<option value=" + Dong[i].Dong + " selected>" + Dong[i].Dong + "</option>";
			}
			$("#Dong").html(opt);
		});
	});
	
</script>

</head>
<body>
	<div id="stylized">

		<br />

		<form name="search" method=POST	action="./ccr?cmd=driveDetail&mode=today">
			<!-- MENU TABLE -->
			<jsp:include page="driveDetailTopMenu.jsp" flush="false" />

			<!-- SEARCH TABLE -->
			<table class="res">
			<thead>
			<tr>
			<th>시/도</th>
			<th>구</th>
			<th>동</th>
			<th>상태</th>
			<th>조회</th>
			</tr>
			</thead>
			
			</table>
			<table class="res">
		
				<tr>
					<td>
						<select name="Si" id="Si" >
							<option value="">전국</option>
						</select>
					</td>
					
					<td>
						<select name="Gu" id="Gu">
							<option value="">구</option>
						</select>
					</td>
					
					<td>
						<select name="Dong" id="Dong">
							<option></option>
						</select>
					</td>
					
					<td><select name=result>
							<option value="%">서비스
							<option value="1">요청
							<option value="2">대기
					</select></td>
					<td><input type=submit size=20 value="조회"></td>
				</tr>
			</table>
			<input type="hidden" name="curr_page" value="<%=plCurrPage%>" /> <input
				type="hidden" name="pageSize" value="<%=pageSize%>" />
		</form>


		<table class="res">
			<thead>
				<tr>
					<th>NO.</th>
					<th>날짜</th>
					<th>시간</th>
					<th>위치</th>
					<th>출발지</th>
					<th>목적지</th>
					<th>고객요금</th>
					<th>고객전화</th>
					<th>기사</th>
					<th>기사전화</th>
					<th>상태</th>
				</tr>
			</thead>
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
					ChargeHistory ch = list[i];
		
			%>
			<tr class="element">
				<td><%=ch.getBid()%></td>
				<td><%=(ch.getBusinessTime()).split(" ")[0]%></td>
				<td><%=((ch.getBusinessTime()).split(" ")[1]).substring(
							0, 5)%></td>
						<td><%=ch.getLat()+":"+ch.getLng() %></td>	
				<td><%=ch.getSource()%></td>
				<td><%=ch.getDestination()%></td>
				<td><%=ch.getDrivingCharge() %></td>
				<td><%=ch.getCustomerPhone().substring(0,3)+"-"+ch.getCustomerPhone().substring(3,7)+"-"+ch.getCustomerPhone().substring(7,11)
				+"<br>기사ID : "+ch.getCustomerDeviceId()
				%></td>
				<td><%=ch.getDriverId()%></td>
				<td><%=ch.getDriverName()%></td>
				<td><%=Codes.getStatus(ch.getBusinessType())%></td>
			</tr>
			<%
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
						<%=PagingHelper.instance.autoPaging(plTotalCnt, pageSize,
					plPageRange, plCurrPage)%>
					</div>
				</td>
			</tr>
			<tr>
				<td>합계 요청<%=plTotalCnt%>건
				</td>
			</tr>
		</table>
	</div>
</body>
</html>