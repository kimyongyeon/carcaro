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
<!-- ���� ���� ���� 
: ��ȸ�� ����Ʈ���� ������ ������ �� ���� ��ư�� ������ ���� ���� ����â�� �߰� 
���⼭ �����(�޴� ���) ��ȭ��ȣ�� �Է��ϰ� ���� ��ư�� ������ ������ ������.
  �� �� â�� �����鼭 ��ȸ����Ʈ�� ������ ������ �����õ�.
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
	var result = confirm("���������� �����Ͻðڽ��ϱ�?");
	
	if(result){
		
		var url = "./ccr?cmd=couponManager&action=adminToUser";
		// �Ķ���� ����
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
	    <th height="33" colspan="2" align="left" bgcolor="#D7EBFF">���� ���� ����</th>
      </tr>
    </thead>
	<tr>
	  <td align="center">������ȣ</td>
	  <td height="36" align="left">������ȣ �θ�â���� �Ѱ��ְ�</td>
    </tr>
	<tr>
	  <td align="center">�絵�� ��ȭ��ȣ</td>
	  <td height="36" align="left">�Ѱ��� </td>
  </tr>
	<tr>
	  <td width="170" align="center"><p>����� ��ȭ��ȣ</p></td>
  <td width="177" height="36" align="left">
    <input type="text"  name=phone id=phone/>
  </td>
  </tr>
<tr>
  <td height="42" colspan="2" align="center" bgcolor="#D7EBFF"><input type="submit" value="����" />  
    <input type="button" value="���" onClick="self.close()"/></td>
</tr>
</table>
</form>
</html>