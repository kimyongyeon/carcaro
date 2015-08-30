<%@ page import="carcaro.bean.Coupon" %>
<%@ page import="net.sf.json.JSONArray" %>
<%@ page import="net.sf.json.JSONObject" %>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List" %>
<%@page import="carcaro.util.Util"%>
<%@page import="carcaro.util.PagingHelper"%>
<%@page import="carcaro.Codes"%>
<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script src="./js/jquery-1.4.4.min.js"></script>
<script src="./js/jquery.mousewheel.min.js"></script>
<script src="./js/calendar_ahmax.js"></script>
<link rel='stylesheet' href='./js/calendar_ahmax.css' type='text/css' />
<link rel='stylesheet' href='/css/style.css' type='text/css' />
<script src="../js/json2.js"></script>
<script src="../js/ajax.js"></script>
<script>
function checking(){

	var result = confirm("쿠폰을 생성하겠습니까?");
	
	if(result){
		var lot = document.generate.lot.value; 
		var count = document.generate.count.value; 
		//쿠폰생성 같은경우 숫자만 넘겨야 하니 스크립트 처리

		var url = "./ccr?cmd=couponManager&action=generate";
		// 파라미터 선언
		var params = "lot="+lot;
		params += "&count="+count;
		
		sendRequest(url, params, registerResult, "POST");
			}
} 
function registerResult(){
	 if (httpRequest.readyState == 4) {
	  if (httpRequest.status == 200) {
	   response = httpRequest.responseText;
	   alert(response);
	   opener.location.reload(true);
	   window.close();
		}
	}
}

</script>
</head>
<body>

<form name="generate">

<table  class="res">
	<thead>
	  <tr bgcolor="#CCCCCC">
	    <th height="33" colspan="2" align="left" bgcolor="#D7EBFF">쿠폰 생성</th>
      </tr>
    </thead>
	<tr>
	  <td align="center">갯수</td>
	  <td height="36" align="left"><input type="text" name=count id=count/></td>
    </tr>
	<tr>
	  <td width="91" align="center"><p>가격</p></td>
  <td width="227" height="36" align="left" ><p>
    <input type="text" name=lot id=lot/>
  </p></td>
  </tr>
<tr>
  <td height="42" colspan="2" align="center" bgcolor="#D7EBFF">
  <input type="button" value="생성" onClick="checking();"/>
    <input type="button" value="취소" onClick="self.close()"/></td>
</tr>
</table>
</form>
</html>