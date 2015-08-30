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

	var result = confirm("������ �����ϰڽ��ϱ�?");
	
	if(result){
		var lot = document.generate.lot.value; 
		var count = document.generate.count.value; 
		//�������� ������� ���ڸ� �Ѱܾ� �ϴ� ��ũ��Ʈ ó��

		var url = "./ccr?cmd=couponManager&action=generate";
		// �Ķ���� ����
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
	    <th height="33" colspan="2" align="left" bgcolor="#D7EBFF">���� ����</th>
      </tr>
    </thead>
	<tr>
	  <td align="center">����</td>
	  <td height="36" align="left"><input type="text" name=count id=count/></td>
    </tr>
	<tr>
	  <td width="91" align="center"><p>����</p></td>
  <td width="227" height="36" align="left" ><p>
    <input type="text" name=lot id=lot/>
  </p></td>
  </tr>
<tr>
  <td height="42" colspan="2" align="center" bgcolor="#D7EBFF">
  <input type="button" value="����" onClick="checking();"/>
    <input type="button" value="���" onClick="self.close()"/></td>
</tr>
</table>
</form>
</html>