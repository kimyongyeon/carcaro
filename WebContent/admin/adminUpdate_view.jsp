<%@page import="carcaro.util.PagingHelper"%>
<%@page import="carcaro.bean.Admin"%>
<%@page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
	
<%
	// 파라미터값을 받는다.
	String id = (String) request.getAttribute("id");
	String corName = (String) request.getAttribute("corName");
	String pw = (String) request.getAttribute("pw");
	String no = (String) request.getAttribute("no");
	String name = (String) request.getAttribute("name");
	String accessLevel = (String) request.getAttribute("accessLevel");
	String tel = (String) request.getAttribute("tel");
	String smartPhone = (String) request.getAttribute("smartPhone");
	String address = (String) request.getAttribute("address");
	String email = (String) request.getAttribute("email");
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

function adminUpate_init(){
	document.getElementById("id").value = "<%=id%>";
	document.getElementById("corName").value = "<%=corName%>";
	document.getElementById("pw").value = "<%=pw%>";
	document.getElementById("pw2").value = "<%=pw%>";
	document.getElementById("no").value = "<%=no%>";
	document.getElementById("name").value = "<%=name%>";
	document.getElementById("accessLevel").value = "<%=accessLevel%>";
	document.getElementById("tel").value = "<%=tel%>";
	document.getElementById("smartPhone").value = "<%=smartPhone%>";
	document.getElementById("address").value = "<%=address%>";
	document.getElementById("email").value = "<%=email%>";
}

</script>
</head>

<body onload="adminUpate_init();">
<form name="regForm" method="post" action="ccr?cmd=adminManager&action=modify">
<table class="css">
  <tr>
    <td colspan="4">관리자 수정</td>
  </tr>
  <tr>
    <td>아이디</td>
    <td> <input type="text" size="20" id="id" name="id"></td>
    <td>회사명</td>
    <td> <input type="text" size="20" id="corName" name="corName"></td>
  </tr>
  <tr>
    <td width="60">PW</td>
    <td width="203"> <input type="password" size="20" id="pw" name="pw"></td>
    <td width="93">사업자번호</td>
    <td width="161"> <input type="text" size="20" id="no" name="no"></td>
  </tr>
  <tr>
    <td>PW확인</td>
    <td> <input type="password" size="20" id="pw2" name="pw2"></td>
    <td>담당자</td>
    <td> <input type="text" size="20" id="name" name="name"></td>
  </tr>
  <tr>
    <td>권한</td>
    <td> <input type="text" size="20" id="accessLevel" name="accessLevel"></td>
    <td>대표전화</td>
    <td> <input type="text" size="20" id="tel" name="tel"></td>
  </tr>
  <tr>
    <td width="60">시/도
    <!-- 동적 select문 -->
    </td>
   
    <td>&nbsp;</td>
    <td>휴대전화</td>
    <td> <input type="text" size="20" id="smartPhone" name="smartPhone"></td>
  </tr>
  <tr>
    <td width="60">구/읍</td>
    <td>&nbsp;</td>
    <td>주소</td>
    <td> <input type="text" size="20" id="address" name="address"></td>
  </tr>
  <tr>
    <td width="60">&nbsp;</td>
    <td> </td>
    <td>email</td>
    <td> <input type="text" size="20" id="email" name="email"></td>
  </tr>
 
</table>
</form>
<table class="css">
 <tr>
    <td ><input type="submit" value="수정"/></td>
     <td ><form name="delete<%=no%>" method="POST" action="ccr?cmd=adminManager&action=deleteProc">
				<input type="hidden" value="<%=no%>" name="no" />
				<a href="#" onClick="document.delete<%=no %>.submit()">삭제</a>
		</form>
     </td>
     <td>
        <input type="reset" value="취소"onClick="self.close()"/></td>
  </tr>
  </table>
</body>
</html>