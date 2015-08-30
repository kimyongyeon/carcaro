<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<link rel="stylesheet" type="text/css" href="style.css" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>1:1문의 보기/답변하기</title>
</head>
<body>

<%

boolean answered = false;
// Get My Parameters
String qid = 		request.getParameter("qid");
String cPhone = 	request.getParameter("usr_phone");
String title = 		request.getParameter("title");
String regDate = 	request.getParameter("reg_date");
String desc =		request.getParameter("desc");
desc = desc.replaceAll("\\\"", "\"");

String ans = 		request.getParameter("answered");
String ansDate =	null;
String answer = 	null;
if ( "Y".equals(ans)){
	answered= 		true;
	ansDate =		request.getParameter("answer_date");
	answer =		request.getParameter("answer");
	answer = 		answer.replaceAll("\\\"", "\"");
}
%>

<div id="stylized" class="myform">




<form id="register_ans" action="appccr?cmd=register_ans" method="POST">
<input type="hidden" name="qid" value="<%=qid%>" />

<table>
<thead>
<tr>
	<td colspan="2">1:1문의 내용 보기</td>
</tr>
</thead>
<tr>
	<td width="80">글번호</td>
	<td><%=qid %></td>
</tr>
<tr>
	<td width="80">제목</td>
	<td><%=title %></td>
</tr>
<tr height="200">
	<td width="80">내용</td>
	<td><textarea cols="40" rows="15" readonly="readonly"><%=desc%></textarea></td>
</tr>
<%if (answered){ %>
<tr>
	<td>답변보기</td>
	<td><textarea cols="40" rows="15" name="answer"><%=answer %></textarea></td>
</tr>
<tr>
	<td colspan="2"><input type="submit" value="답변수정하기" /></td>
</tr>
<%}else{ %>
<tr>
	<td>답변하기</td>
	<td><textarea cols="40" rows="15" name="answer"></textarea></td>
</tr>
<tr>
	<td colspan="2"><input type="submit" value="답변하기" /></td>
</tr>
<%} %>

</table>
</form>

</div>
</body>
</html>