<%@page import="carcaro.util.PagingHelper"%>
<%@page import="carcaro.bean.Admin"%>
<%@page import="carcaro.dao.AdminDAO"%>
<%@page import="carcaro.ConnectionPool"%>
<%@page import="carcaro.dao.LocationDAO"%>
<%@page import="net.sf.json.JSONObject"%>
<%@page import="net.sf.json.JSONArray"%>
<%@page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
	
<jsp:include page="../js/header.jsp" />
<%
String local = (String) request.getAttribute("local");
ConnectionPool connPool = ConnectionPool.getInstance();
LocationDAO locationDAO = new LocationDAO(connPool);
// 시
JSONArray Si = locationDAO.getSido(local);
// 구
String temp1 = Si.getJSONObject(0).getString("Sido").toString();
JSONArray Gu = locationDAO.getGugun(temp1);
// 동
String temp2 = Gu.getJSONObject(0).getString("Gugun").toString();
JSONArray Dong = locationDAO.getDong(temp2);
%>
<html>
<head>
<title>등록</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script>
function checking(){

	var result = confirm("정말로 추가하겠습니까?");

	if(result){
		
		var no = document.regForm.no.value; // no
		var id = document.regForm.id.value; 
		var pw = document.regForm.pw.value;
		var email = document.regForm.email.value; 
		var accessLevel = document.regForm.accessLevel.value; 
		//var hierarchy = document.regForm.hierarchy.value; 
		var corName = document.regForm.corName.value; 
		var name = document.regForm.name.value; 
		var tel = document.regForm.tel.value; 
		var smartPhone = document.regForm.smartPhone.value; 
		var address = document.regForm.address.value;
		
		// 오류체크는 나중에 이곳에 추가하세요.

		// URL 선언
		var url = "../ccr?cmd=adminManager&action=insertProc";
		// 파라미터 선언
		var params = "no="+no;
		params += "&id="+id;
		params += "&pw="+pw;
		params += "&email="+email;
		params += "&accessLevel="+accessLevel;
		//params += "&hierarchy="+hierarchy;
		params += "&corName="+corName;
		params += "&name="+name;
		params += "&tel="+tel;
		params += "&smartPhone="+smartPhone;
		params += "&address="+address;
	
		//alert(params);
		var response = $.ajax({
		    type : 'post',
		    async: false,
		    url : url,
		    data : params
		}).responseText;
		alert(response);
	}
} 

$(function()
		{
			// 동적 셀렉트 초기화 작업
			// 동적 셀렉트 예제 
			var Si = <%=Si%>;
			var opt = "";
			for(var i=0; i<Si.length; i++){
				opt += "<option value=" + Si[i].Sido + " selected>" + Si[i].Sido + "</option>";
			}
			$("#Si").html(opt);
			
			// 동적 셀렉트 예제
			var Gu = <%=Gu%>;
			var opt = "";
			for(var i=0; i<Gu.length; i++){
				opt += "<option value=" + Gu[i].Gugun + " selected>" + Gu[i].Gugun + "</option>";
			}
			$("#Gu").html(opt);
			
			// 동적 셀렉트 예제
			var Dong = <%=Dong%>;
			var opt = "";
			for(var i=0; i<Dong.length; i++){
				opt += "<option value=" + Dong[i].Dong + " selected>" + Dong[i].Dong + "</option>";
			}
			$("#Dong").html(opt);
			
			// 콤보 박스 선택시 inputbox에 값 대입
			$("select[name=Si]").bind("change", function()
			{
				var url = "./ccr?cmd=Si";
				var params = "Si="+$(this).val();
				var response = $.ajax({
					type : 'post',
					async: false,
					url : url,
					data : params
				}).responseText;
				var evalData = eval(" (" + response +") ");
				var re = evalData;
				var Gu = re.Gu;
				var opt = "";
				for(var i=0; i<Gu.length; i++){
					opt += "<option value=" + Gu[i].Gugun + " selected>" + Gu[i].Gugun + "</option>";
				}
				$("#Gu").html(opt);
			});
			
			// 콤보 박스 선택시 inputbox에 값 대입
			$("select[name=Gu]").bind("change", function()
			{
				var url = "./ccr?cmd=Gu";
				var params = "Gu="+$(this).val();
				var response = $.ajax({
					type : 'post',
					async: false,
					url : url,
					data : params
				}).responseText;
				var evalData = eval(" (" + response +") ");
				var re = evalData;
				var Dong = re.Dong;
				var opt = "";
				for(var i=0; i<Dong.length; i++){
					opt += "<option value=" + Dong[i].Dong + " selected>" + Dong[i].Dong + "</option>";
				}
				$("#Dong").html(opt);
			});
		});
</script>

</head>
<body>

<form name="regForm">

<table class="css">
  <tr>
    <td colspan="4">관리자 생성</td>
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
    <td> 
    	<select id="accessLevel" name="accessLevel">
			<option value="1">본사</option>
			<option value="10">지사</option>
			<option value="20">대리점</option>
		</select> 
	</td>
    <td>대표전화</td>
    <td> <input type="text" size="20" id="tel" name="tel"></td>
  </tr>
  <tr>
    <td width="60">시/도</td>
   <td>
		<select name="Si" id="Si" >
			<option value="">전국</option>
		</select>
	</td>
    <td>휴대전화</td>
    <td> <input type="text" size="20" id="smartPhone" name="smartPhone"></td>
  </tr>
  <tr>
    <td width="60">구/읍</td>
    <td>
		<select name="Gu" id="Gu">
			<option value="">구</option>
		</select>
	</td>
    <td>주소</td>
    <td> <input type="text" size="20" id="address" name="address"></td>
  </tr>
  <tr>
    <td width="60">&nbsp;</td>
    <td> </td>
    <td>email</td>
    <td> <input type="text" size="20" id="email" name="email"></td>
  </tr>
  <tr>
    <td colspan="4"> <input type="button" value="등록" onClick="checking();"/>
        <input type="reset" value="취소"onClick="self.close()"/></td>
  </tr>
</table>
</form>
</body>
</html>