<%@page import="carcaro.util.Util"%>
<%@page import="carcaro.util.PagingHelper"%>
<%@page import="carcaro.Codes"%>
<%@page import="net.sf.json.JSONArray"%>
<%@page import="net.sf.json.JSONObject"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="java.util.List"%>

<%
	String msg1 = (String) request.getAttribute("msg1");

	// Session info
	String aL = request.getParameter("aL"); // access Level

	JSONObject json = (JSONObject)request.getAttribute("vCallSchedule");
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
	
	var msg1 ="<%=msg1%>";
	
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
	<div id="stylized">

		<br />

		<h3>가상콜 스케줄러</h3>

<% if ( aL.equals("1")){ %>

<form name="createSchedule" method="POST" action="ccr?cmd=vCallManager&mode=modify">
<input type="hidden" name="sID" value="<%=json.getInt("sID")%>">
		<table>
			<thead>
				<tr>
					<th>시작시간</th><td><input type="text" name="sBegin" value="<%=json.getString("sBegin")%>"/><br />시간:분(24시 형식)</td>
				</tr><tr>
					<th>종료시간</th><td><input type="text" name="sEnd" value="<%=json.getString("sEnd")%>"/><br />시간:분(24시 형식)</td>
				</tr><tr>
					<th>지역설정</th><td><input type="text" name="sLocaName" value="<%=json.getString("sLocaName")%>"/><br />시/도의 "시" 또는 "도"는 <br />입력하지 않습니다. <br />지역은 구/군 까지만 입력하세요.</td>
				</tr><tr>
					<th>콜개수</th><td><input type="text" name="sCallCount" value="<%=json.getInt("sCallCount")%>"/></td>
				</tr><tr>
					<th>반복</th><td>
					<% String sRepeat = json.getString("sRepeat"); %>
						<input type="checkBox" name="Mon" value="1" <%if (sRepeat.indexOf("1") > -1){  %>checked<%} %>/>월&nbsp;
						<input type="checkBox" name="Tue" value="2" <%if (sRepeat.indexOf("2") > -1){  %>checked<%} %>/>화&nbsp;
						<input type="checkBox" name="Wed" value="3" <%if (sRepeat.indexOf("3") > -1){  %>checked<%} %>/>수&nbsp;
						<input type="checkBox" name="Thu" value="4" <%if (sRepeat.indexOf("4") > -1){  %>checked<%} %>/>목&nbsp;
						<input type="checkBox" name="Fri" value="5" <%if (sRepeat.indexOf("5") > -1){  %>checked<%} %>/>금&nbsp;
						<input type="checkBox" name="Sat" value="6" <%if (sRepeat.indexOf("6") > -1){  %>checked<%} %>/>토&nbsp;
						<input type="checkBox" name="Sun" value="7" <%if (sRepeat.indexOf("7") > -1){  %>checked<%} %>/>일&nbsp;
					</td>
				</tr>
			</thead>
			
			<tr>
			
			<td colspan="2">콜 개수가 많으면 수정이 오래걸릴 수 있습니다... <br />수정을 누르신 후 기다려주세요.<br /><input type="submit" value="수정"/><input type="button" value="취소" onClick='document.getElementById("createSchedule").reset()'></td>
			</tr>
			
		</table>

</form>
<% } else { %>
	<p align="center">권한이 없습니다.</p>
<% } %>
	</div>
</body>
</html>