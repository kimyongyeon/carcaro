<%@page import="carcaro.util.Util"%>
<%@page import="carcaro.bean.Admin" %>
<%@page import="carcaro.util.PagingHelper"%>
<%@page import="carcaro.Codes"%>
<%@page import="net.sf.json.JSONArray"%>
<%@page import="net.sf.json.JSONObject"%>
<%@ page import="java.util.List"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>

<%
String msg1 = (String) request.getAttribute("msg1");

JSONObject detail = (JSONObject)request.getAttribute("detail");
%>


<html>
<head>
<title>1:1문의 수정</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script type="text/javascript"
	src="http://code.jquery.com/jquery.min.js"></script>
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
<body >
	<div id="stylized">

		<br />
		<h3>1:1문의 수정</h3>
		
			<% 	String QID= detail.getString("QID");
				String title = detail.getString("title");
				String desc = detail.getString("desc");
		%>
					
		<form name="modify" method="POST" action="ccr?cmd=qnaManager&action=modifyFinish">
		<input type="hidden" name="QID" value="<%=QID %>" />
			<table class="res">
				<thead>
					<tr><th>NO</th><td><%=QID %></td></tr>
					
				<!-- 	<tr><th>제목</th><td><input type="text" name="title" value="DB에 존재하지 않음" size="200" /></td></tr>
				-->
					<tr><th>내용</th><td><textarea name="desc" cols="120" rows="15"><%=desc %></textarea></td></tr>
				</thead>
				<tr>
				<td colspan="2"><input type="submit" value="올리기" /></td>
				</tr>
			</table>

		</form>
	</div>
</body>
</html>