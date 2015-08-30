<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=EUC-KR">
<link rel="stylesheet" type="text/css" href="style.css" />
<title>카카로 쿠폰 관리자 페이지</title>
</head>
<body>

<br>

<center><h2>쿠폰 관리자 페이지</h2></center>

<div id="stylized" class="myform">



<form name="putCoupon" method="post" action="appccr?cmd=register_coupon">

<table>
<thead>
<tr>	
<th colspan="2" >쿠폰 등록하기 (SINGLE)</th>
</tr>
</thead>
<tr>
<td>쿠폰번호</td>
<td><input type="text" name="coupon_id"/></td>
</tr>
<tr>
<!--  
<td>전화번호</td>
<td><input type="text" name="customer_phone"></td>
</tr>
<tr>
<td>고객 성명</td>
<td><input type="text" name="customer_name"></td>
</tr>
-->
<tr>
<td>가격</td>
<td><input type="text" name="amount"></td>
</tr>
<tr>
<td colspan="2" align="center">
<input type="button" value="승인" onClick="submit()"/>
</td>
</tr>
</table>
</form>

<p></p>

<form enctype="multipart/form-data" name="puCouponBulk" method="post" action="appccr?cmd=register_coupon_bulk">
<table>
<thead>
<tr>
<th colspan="2">CSV 쿠폰 등록하기 (BULK) </th>
</tr>
</thead>
<tr>
<td>파일 선택</td><td><input name="file" type="file"></td>
</tr>
<tr>
<td colspan="2"><input type="submit" value="전송" /></td>
</tr>
</table>
</form>

<p></p>

<form name="getCoupon" method="post" action="appccr?cmd=get_coupon">
<table>
<thead>
<tr>
<th colspan="2">쿠폰가져오기</th>
</tr>
</thead>
<tr>
<td><select name="column">
  <option value="coupon_id">쿠폰번호</option>
  <option value="customer_name">고객성명</option>
  <option value="customer_phone">고객전화번호</option>
</select></td>
<td><input type="text" name="value"></td>
</tr>
<tr>
<td colspan="2" align="center">
<input type="button" value="승인" onClick="submit()"/></td>
</tr>
</table>
</form>

<p></p>

<form name="delCoupon" method="post" action="appccr?cmd=delete_coupon">
<table>
<thead>
<tr>
<th colspan="2">쿠폰삭제하기</th>
</tr>
</thead>

<tr>
<td>쿠폰번호</td>
<td><input type="text" name="coupon_id"></td>
</tr>
<tr>
<td colspan="2" align="center">
<input type="button" value="삭제하기" onClick="submit()"/></td>
</tr>
</table>
</form>

<p></p>

<form name="getCoupon" method="post" action="appccr?cmd=get_coupon_list">
<table>
<thead>
<tr>
<th>쿠폰 목록 보기</th>
</tr>
</thead>
<tr>
<td><select name="fetchType">
  <option value="ALL">전체보기</option>
  <option value="USED">사용한 쿠폰</option>
  <option value="NUSED">사용 안한 쿠폰</option>
</select></td></tr>

<tr>
<td><input type="button" value="가져오기" onClick="submit()"/></td>
</tr>
</table>
</form>

<p></p>

<form name="useCoupon" method="post" action="appccr?cmd=use_coupon">
<table>
<thead>
<tr>
<th colspan="2">쿠폰 사용하기</th>
</tr>
</thead>
<tr>
<td><input type="text" name="coupon_id"></td><td><input type="text" name="driver_id"></td>
</tr>
<tr>
<td colspan="2" align="center">
<input type="button" value="사용하기" onClick="submit()"/></td>
</tr>
</table>
</form>

</div>

</body>
</html>