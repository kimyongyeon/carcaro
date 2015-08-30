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

	JSONObject detail = (JSONObject)request.getAttribute("detail");
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

		<h3>1:1문의 답변</h3>

<%

%>
		<form name="answer" method="POST" action="ccr?cmd=qnaManager&action=answerFinish">
		<input type="hidden" name="QID" value="<%=detail.getString("QID") %>" />
			<table class="res">
				<thead>
					
					<!-- <tr><th>제목</th><td></td></tr>  DB에 내용없음-->
					<tr><th>내용</th><td><textarea name="desc" cols="120" rows="15" readonly><%=detail.getString("desc") %></textarea></td></tr>
					<tr><th>답변</th><td><textarea name="answer" cols="120" rows="15" ></textarea></td></tr>
				</thead>
				<tr>
				<td colspan="2"><input type="submit" value="답변등록" /></td>
				</tr>
			</table>

		</form>
	</div>
</body>
</html>