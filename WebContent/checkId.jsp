<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=EUC-KR">
<title>Insert title here</title>
<!-- checkId.jsp 종료 시 수행 할 내용 -->
<script>
function checkIdClose(id){
	opener.writeForm.driverId.value=id;
	window.close();
	opener.writeForm.passwd.focus();
}
</script>
</head>
<%
	long ret=0;
	String id = (String) request.getAttribute("id");
	if(id!=null)
		ret = (Long) request.getAttribute("ret");
%>
<body>
<form method="post" action="ccr?cmd=checkId">
<%
	if(ret>0) {%>
		현재 <%=id %>는 사용 불가능합니다. <br><br>
		아이디 <input type="text" name="id"/>
		<input type="submit" value="중복체크"/> 
	<%} else if(id == null) {%>
		아이디 <input type="text" name="id"/>
			<input type="submit" value="중복체크"/>
	<%} else {%>
		현재 <%=id %>는 사용 가능합니다.
		<a href="checkId.jsp">다른 아이디 선택</a><br><br>
		<input type = "button" value="현재 아이디 선택" onClick="javascript:checkIdClose ('<%=id%>')">
	<% }%>
</form>
</body>
</html>