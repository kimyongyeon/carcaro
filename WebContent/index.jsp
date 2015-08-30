<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<html>
<script>
function logging(){
	if(document.loginForm.id.value=="") alert("아이디를 입력해 주세요")
	else if(document.loginForm.pw.value=="") alert("패스워드를 입력해 주세요")
	else document.loginForm.submit()
}
</script>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Admin</title>
</head>
<body style="background-image:url('Img/login_full.jpg');">
<div id="stylized" class="myForm" align="center">
<%
String alert =(String) session.getAttribute("alert");
String msg =(String) request.getAttribute("msg");
if(msg!=null && !msg.equals("")){%>
<%=msg %>
<%}	else if(alert!=null && !alert.equals("")){%>
<%=msg %>
<%}	else {%>
<!--  로그인 하시기 바랍니다<br><br><br>-->
<%} %>

<form name="loginForm" method="post" action="ccr?cmd=loginConfirm">
<table style="position:absolute;top:425;left:600"  >
 <tr>
  <td><font face="Tahoma" color="#8C8C8C"><span style="font-size:7pt;"><b>ID</b></span></font></td>
  <td><font face="Tahoma" color="#8C8C8C"><span style="font-size:7pt;"><b>PassWord</b></span></font></td>
  <td></td>
 </tr>
 <tr>
  <td><input type="text" name="id"/></td>
  <td><input type="password" name="pw"></td> 
  <td><img src=Img/login_btn.jpg onClick="javascript:logging()"></td>
 <tr>
 </table>
 
 <!--<table width="1600" cellpadding="0" cellspacing="0" height="907">
   <tr>
        <td width="1600" valign="top" colspan="3"><img src="Img/login_top.jpg" width="1600" height="410" border="0"></td>
        
    </tr>백그라운드 처리 -->
  <!--   <tr>
        <td width="632" valign="top" rowspan="2" height="497"><img src="Img/login_side1.jpg" width="632" height="490" border="0"></td>
        
        <td width="389" valign="top" bgcolor="black" height="68">

        </td>
        <td width="579" valign="top" rowspan="2" height="497"><img src="Img/login_side2.jpg" width="579" height="490" border="0"></td>
    </tr>
    <tr>
        <td width="389" height="245" valign="top"><img src="Img/login_side3.jpg" width="389" height="421" border="0"></td>
    </tr>

       

 <!-- 
  <tr>

</tr>
</table>
 -->
</form>

</div>
</body>
</html>