<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="net.sf.json.JSONArray" %>
<%@ page import="net.sf.json.JSONObject" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<link rel="stylesheet" type="text/css" href="style.css" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>QNA List</title>



</head>
<body>

<table class='res'>
<thead>
<tr><td>질문번호</td><td>제목</td><td>고객전화</td><td>등록시간</td>
<td>답변유무</td></tr>
</thead>

<%


JSONArray json = (JSONArray)request.getAttribute("result"); // Fetch Tossed Object

StringBuffer str = new StringBuffer();

for (int i = 0 ; i < json.size(); i ++){
	JSONObject js = 	json.getJSONObject(i);
	String qid = 		js.getString("QID");
    String cPhone = 	js.getString("usr_phone");
    String title = 		js.getString("title");
    String regDate = 	js.getString("reg_date");
    String desc =		js.getString("desc");
    
    String ans = 		js.getString("answered");
    String ansDate = null;
    String answer = null;
    if ( "Y".equals(ans)){
   		ansDate =	js.getString("answer_date");
    	answer =		js.getString("answer");
    }

	%>
	<tr>
	<td><%=qid %></td>
	<td><div title="<%=desc %>"><%=title %></div></td>
	<td><%=cPhone %></td>
	<td><%=regDate %></td>
	<td><form name="form<%=qid %>" action="./reg_answer.jsp" method="post">
		<!-- 목록 관련  -->
		<input type="hidden" name="qid" value="<%=qid%>">
		<input type="hidden" name="title" value="<%=title%>">
		<input type="hidden" name="desc" value="<%=desc%>">
		<input type="hidden" name="usr_phone" value="<%=cPhone%>">
		<!-- 답변 관련  -->
		<input type="hidden" name="answered" value="<%=ans%>">
		<input type="hidden" name="answer_date" value="<%=ansDate%>">
		<input type="hidden" name="answer" value="<%=answer%>">
		
		
		<% if ( "N".equals(ans) ){ %><a onClick="document.form<%=qid %>.submit()">답변하기</a>
		<%}else{%>
									<a onClick="document.form<%=qid %>.submit()">답변완료/보기</a>
		<%}%></form></td>
	<%}%>
</tr>
</table>


</body>
</html>