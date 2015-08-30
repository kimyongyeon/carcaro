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
<!-- 쿠폰 소유 이전 
: 조회후 리스트에서 쿠폰을 선택한 후 이전 버튼을 누르면 쿠폰 소유 이전창이 뜨고 
여기서 양수자(받는 사람) 전화번호를 입력하고 이전 버튼을 누르면 소유가 이전됨.
  이 때 창이 닫히면서 조회리스트에 이전된 쿠폰이 리스팅됨.
 -->
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
	var coupon_id = document.CouponRelocation.coupon_id.value; 
	var phone = document.CouponRelocation.phone.value; 
	var result = confirm("쿠폰소유를 이전하시겠습니까?");
	
	if(result){
		
		var url = "./ccr?cmd=couponManager&action=adminToUser";
		// 파라미터 선언
		var params = "coupon_id="+coupon_id;
		params += "&phone="+phone;
		
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
<form enctype="multipart/form-data" name="CouponRelocation" method="post">

<table width="330"  class="res">
	<thead>
	  <tr bgcolor="#CCCCCC">
	    <th height="33" colspan="2" align="left" bgcolor="#D7EBFF">쿠폰 소유 이전</th>
      </tr>
    </thead>
	<tr>
	  <td align="center">쿠폰번호</td>
	  <td height="36" align="left">쿠폰번호 부모창에서 넘겨주고</td>
    </tr>
	<tr>
	  <td align="center">양도자 전화번호</td>
	  <td height="36" align="left">넘겨줌 </td>
  </tr>
	<tr>
	  <td width="170" align="center"><p>양수자 전화번호</p></td>
  <td width="177" height="36" align="left">
    <input type="text"  name=phone id=phone/>
  </td>
  </tr>
<tr>
  <td height="42" colspan="2" align="center" bgcolor="#D7EBFF"><input type="submit" value="이전" />  
    <input type="button" value="취소" onClick="self.close()"/></td>
</tr>
</table>
</form>
</html>