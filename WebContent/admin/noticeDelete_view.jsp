<%@page import="javax.swing.text.Document"%>
<%@page import="carcaro.util.Util"%>
<%@page import="carcaro.Codes"%>
<%@page import="net.sf.json.JSONArray"%>
<%@page import="net.sf.json.JSONObject"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="java.util.List"%>

<%
	// Session info
	String aL = request.getParameter("aL"); // access Level
	String location = request.getParameter("location");

	JSONArray list = (JSONArray) request.getAttribute("list");
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
			<%
				for (int i = 0; i < list.size(); i++) {
						JSONObject row = list.getJSONObject(i);
						
						int no = row.getInt("no");						
			%>	
					<form name="delete<%=no%>" method="POST" action="ccr?cmd=noticeManager&mode=delete">
					<input type="hidden" value="<%=no%>" name="no" />
					<a href="#" onClick="document.delete<%=no %>.submit()">삭제</a>
					<input type="button" value="취소" onClick="self.close()">
					
					</form>
			
			<%
				}
			%>
			
		
</body>
</html>