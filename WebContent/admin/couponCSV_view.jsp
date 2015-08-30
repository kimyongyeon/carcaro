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
</script>
</head>
<body>
<form  enctype="multipart/form-data" name="CouponBulk" method="post" action="ccr?cmd=couponManager&action=registerBulk">

<table width="330" class="res">
	<thead>
	  <tr bgcolor="#CCCCCC">
	    <th height="33" colspan="2" align="left" bgcolor="#D7EBFF">CSV 업데이트</th>
      </tr>
    </thead>
	<tr>
  <td width="72" height="80">파일 선택</td><td width="239"><input type="file" name="file"/>
  <br /><a href="/media/Coupon_Bulk_Register_Form.csv">sample.csv</a></td>
</tr>
<tr>
<td height="42" colspan="2" align="center" bgcolor="#D7EBFF"><input type="submit" value="업데이트" />  
  <input type="button" value="취소"onClick="self.close()" /></td>
</tr>
</table>
</form>

</html>