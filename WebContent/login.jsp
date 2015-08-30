<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<script>
function logging(){
	if(document.loginForm.id.value=="") alert("아이디를 입력해 주세요")
	else if(document.loginForm.pw.value=="") alert("패스워드를 입력해 주세요")
	else document.loginForm.submit()
}
</script>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="./style.css" />
<title>Login</title>
</head>
<body>


<center><h2> 로그인</h2></center>

<div id="stylized" class="myform">

<form name="loginForm" method="post" action="ccr?cmd=admin_login">
<h3>로그인</h3>
<table border=2>
<tr>
<td>아이디</td>
<td><input type="text" name="id"/></td>
</tr>
<tr>
<td>패스워드</td>
<td><input type="password" name="pw"></td>
</tr>

<tr>
<td colspan="2" align="center">
<input type="button" value="로그인" onClick="javascript:logging()"/>
<input type="button" value="회원가입" onClick="javascript:location.href='termsForUse.jsp'"/>
</td>
</tr>
</table>
</form>

</div>


</body>
</html>