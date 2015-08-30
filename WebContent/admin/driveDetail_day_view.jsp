<%@page import="carcaro.util.Util"%>
<%@page import="carcaro.util.PagingHelper" %>
<%@page import="carcaro.Codes"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.concurrent.ConcurrentHashMap"%>
<%@page import="carcaro.bean.ChargeHistory"%>
<%@page import="carcaro.bean.ChargeHistoryPeriod"%>
<%@page import="carcaro.ConnectionPool" %>
<%@page import="carcaro.dao.LocationDAO" %>
<%@page import="carcaro.dao.OpenApiDAO" %>
<%@page import="java.util.List" %>
<%@page import="net.sf.json.JSONObject" %>
<%@page import="net.sf.json.JSONArray" %>
<%@page import="org.joda.time.LocalDate" %>
<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%
long plTotalCnt = (Long)request.getAttribute("plTotalCnt"); // 전체 레코드 수
long pageSize = (Long)request.getAttribute("pageSize"); // 한 페이지당 레코드 수
long plPageRange = 5; // 페이지 출력 범위
long plCurrPage = (Long)request.getAttribute("pageNum"); // 현재 페이지
%>
<%
	String today = Util.getCurrDate();
	String msg1 = (String) request.getAttribute("msg1");
	String dateA= (String)request.getAttribute("dateA");
	String dateB= (String)request.getAttribute("dateB");
	String local = (String)request.getAttribute("local");
	String result = (String) session.getAttribute("result");
	String name = Util.NVL((String)request.getAttribute("name"));

// Session info
	String aL = request.getParameter("aL"); // access Level
	String location = request.getParameter("location");
	
	ArrayList<String> ids = (ArrayList<String>) request.getAttribute("ids");
	ChargeHistory[] list = (ChargeHistory[]) request.getAttribute("list");
	
	// connPool
	ConnectionPool connPool = ConnectionPool.getInstance();
	LocationDAO locationDAO = new LocationDAO(connPool);
	

	JSONArray Si = locationDAO.getSido(local);
	String temp1 = Si.getJSONObject(0).getString("Sido").toString();
	JSONArray Gu = locationDAO.getGugun(temp1);
	String temp2 = Gu.getJSONObject(0).getString("Gugun").toString();
	JSONArray Dong = locationDAO.getDong(temp2);
	
	// 신규달력: 추가 사항
	java.util.Calendar cal = java.util.Calendar.getInstance();
	int year = cal.get ( cal.YEAR );
%>
<jsp:include page="../js/header.jsp" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<script language="javascript">
	$(function() {
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

	var ids =<%=ids != null ? ids.size() : 0%>;
	var msg1 ="<%=msg1%>";
	var idsArray = "<%=ids %>";
	var name = "<%=name%>";

	if (ids) {
		//selectPopup();

	} else {
		if (msg1 != "null")
			alert(msg1);
	}

	function selectPopup() {
		window.open("carcaro/selectId.html", "selectId", "status = 1, height = 200, width = 340, resizable = 0");
	}

	function selectDriverIdFromPopup(id) {
		var n = document.getElementsByName("name")[0];
		var f = document.getElementsByName("driverId")[0];
		n.value = name;
		f.value = id;

		//		alert(f.value);
		document.search.submit();
	}

	function goPage(curr) {
		var frm = document.search;
		frm.curr_page.value = curr;
		frm.submit();
	}

	function driverDetail(search) {
		var selected = false;
		var num = 0;
		var form = document.search;
		if (form.radio.length > 1) {
			for (i = 0; i < form.radio.length; i++) {
				if (form.radio[i].checked) {
					num = form.radio[i].value;
					selected = true;
					break;
				}
			}
		} else {
			if (form.radio.checked) {
				num = form.radio.value;
				selected = true;
			}
		}
		if (selected) {
			var up_con = confirm("상세정보를 확인하시겠습니까?");
			if (up_con == true) {
				window.showModalDialog("admin/driverDetail_view.jsp", self, "(window.screen.width / 2) - (Number(iWidth) / 2); (window.screen.height / 2) - (Number(iHeight) / 2); dialogWidth:600px; dialogHeight:300px");
			}
		} else {
			alert("항목을 먼저 선택해 주세요.");
		}

	}
			
</script>
</head>
<body>
<div id="stylized" >
	
	<br />
	<h3>일별 대리운전 조회</h3>
	<form name="search" method=POST action="./ccr?cmd=driveDetail&mode=day">
	<input type="hidden" name="curr_page" value="<%=plCurrPage%>"/>
	<input type="hidden" name="pageSize" value="<%=pageSize%>"/>
	
	
	<!-- MENU TABLE -->
	<jsp:include page="driveDetailTopMenu.jsp" flush="false" />
	
	<!-- SEARCH TABLE -->
	<table class="res">
	<thead>
		<tr>
		
			<th>조회 날짜 시작</th>
			<th>조회 날짜 끝</th>
			<th>시/도</th>
			<th>구</th>
			<th>동</th>
			<th>결과</th>
			<th>조회</th>
			<th>상세조회</th>
		</tr>
	</thead>
		<tr>
		
		<%	if(dateA==null) dateA=today;
		if(dateB==null) dateB=today;
		%>
			<td><input type="text" id="dateA" name="dateA" value="<%=dateA%>"></td>
			<td><input type="text" id="dateB" name="dateB" value="<%=dateB%>"></td>
	
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
					
               
		
			<td><select name=result >
                          <option value="%">결과
                          <option value="1">완료
                          <option value="2">고객취소
                          <option value="3">기사취소
                </select></td>
		
			<td><input type="submit" size=20 value="조회"></td>
			<td><input type="button" size=20 value="상세조회"  onclick="JavaScript:driverDetail()"/></td>

		</tr>
	</table>
	

	
	
	<table class="res">
			<thead>
				<tr>
					<th></th><!-- 일별조회 상세 -->
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
					String phone = ch.getCustomerPhone();

					if(phone == null || phone.length() <11 ||phone.length() ==13)
						phone = "00000000000"; // 대충 해당길이를 넣어줘라
					
//자를 갯수길이 많큼 null
			%>
			<tr class="element">
				<td><input type="radio" name="radio" value=<%=i + 1%> />
				<td><%=ch.getBid()%></td>
				<td><%=(ch.getBusinessTime()).split(" ")[0]%></td>
				<td><%=((ch.getBusinessTime()).split(" ")[1]).substring(
							0, 5)%></td>
				<td><%=ch.getLat()+":"+ch.getLng() %></td>	
				<td><%=ch.getSource()%></td>
				<td><%=ch.getDestination()%></td>
				<td><%=ch.getDrivingCharge() %></td>
				<td><%=phone.substring(0,3)+"-"+phone.substring(3,7)+"-"+phone.substring(7)%></td>
				<td><%=ch.getDriverId()%></td>
				<td><%=ch.getPhone().substring(0,3)+"-"+ch.getPhone().substring(3,7)+"-"+ch.getPhone().substring(7) %></td>
				<td><%=Codes.getStatus(ch.getBusinessType())%></td>
			</tr>
			<%
				}
			%>
			<%
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
	<td>합계 총:<%=plTotalCnt %>건 </td>
	</tr>
	</table>	
	</div>

</body>
</html>