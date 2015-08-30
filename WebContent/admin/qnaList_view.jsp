<%@page import="carcaro.util.Util"%>
<%@page import="carcaro.util.PagingHelper"%>
<%@page import="carcaro.Codes"%>
<%@page import="net.sf.json.JSONArray"%>
<%@page import="net.sf.json.JSONObject"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="java.util.List"%>

<%
	long plTotalCnt = (Long) request.getAttribute("plTotalCnt"); // 전체 레코드 수
	long pageSize = (Long) request.getAttribute("pageSize"); // 한 페이지당 레코드 수
	long plPageRange = 5; // 페이지 출력 범위
	long plCurrPage = (Long) request.getAttribute("pageNum"); // 현재 페이지

	String msg1 = (String) request.getAttribute("msg1");

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

		<h3>1:1 문의 목록</h3>
<form name="search" method="POST" action="./ccr?cmd=qnaManager&action=list">
	<input type="hidden" name="curr_page" value="<%=plCurrPage%>" /> 
	<input type="hidden" name="pageSize" value="<%=pageSize%>" />
		<table class="res">
			<thead>
				<tr>
					<th>NO</th>
				<!-- <th>제목</th>-->
					<th>내용</th>
					<th>등록일</th>
					<th>답변</th>
					<th>수정</th>
				</tr>
			</thead>
			<%
				if (list.size() == 0) {
			%>
			<tr>
				<td colspan="10">내용물이 없습니다.</td>
			</tr>
			<%
				} else {
			%>

			<%
				for (int i = 0; i < list.size(); i++) {
						JSONObject row = list.getJSONObject(i);
						int QID = row.getInt("QID");
						String answered = row.getString("answered");
						String answer = "";
						if("Y".equals(answered)){
							answer = row.getString("answer");
						}
			%>
			</table>
	</form>
<table class="res">
			<tr class="element">
				<td><%=QID%></td>
				<!-- <td>제목DB에 존재하지 않음</td>-->
				<td><%=row.getString("desc")%></td>
				<td><%=row.getString("reg_date")%></td>
				<td>
				<%if ("Y".equals(answered)){ %>
					<a href="#" title="<%=answer%>">답변완료</a>
					<%}else{ %>
					<form name="answer<%=QID%>" method="POST" action="ccr?cmd=qnaManager&action=answer">
					<input type="hidden" value="<%=QID%>" name="QID" />
					<a href="#" onClick="document.answer<%=QID %>.submit()">답변</a>
					</form>
					<%} %>
				</td>
				<td>
				<form name="detail<%=QID%>" method="POST" action="ccr?cmd=qnaManager&action=modify">
					<input type="hidden" value="<%=QID%>" name="QID" />
					<a href="#" onClick="document.detail<%=QID %>.submit()">수정</a>
					</form>
				<form name="delete<%=QID%>" method="POST" action="ccr?cmd=qnaManager&action=delete">
					<input type="hidden" value="<%=QID%>" name="QID" />
					<a href="#" onClick="document.delete<%=QID %>.submit()">삭제</a>
					</form>
				</td>
			</tr>
			<%
				}
			%>
			<%
				}
			%>
		</table>
	</div>
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
</body>
</html>

	
</body>
</html>