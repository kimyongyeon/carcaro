<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="style.css" />
<title>Insert title here</title>
</head>
<body>


<center><h2> 1:1문의 서비스</h2></center>
<br><br>

<div id="stylized" class="myform">

<form name="putQna" method="post" action="appccr?cmd=register_qna">

<table>
<thead>
<tr>	
<th colspan="2" >질문등록</th>
</tr>
</thead>
<tr>
<td>작성자 전화번호</td>
<td><input type="text" name="usr_phone"/></td>
</tr>

<tr>
<td>제목</td>
<td><input type="text" name="title"/></td>
</tr>
<tr>
<td>내용</td>
<td>
<textarea cols="40" rows="8" name="desc"></textarea>
</td>
</tr>
<tr>
<td colspan="2" align="center">
<input type="button" value="승인" onClick="submit()"/>
</td>
</tr>
</table>
</form>

<br><br><br>

<form name="getMyQna" method="post" action="appccr?cmd=get_qna">

<table>
<thead>
<tr>
<th colspan="2" >질문보기</th>
</tr>
</thead>
<tr>
<td colspan="2">
<input type="radio" name="option" value="JSON" > JSON<br>
<input type="radio" name="option" value="HTML" checked> HTML<br>
<input type="checkbox" name="option2" value="ALL" > 모두보기 <br>
</td>
</tr>
<tr>
<td>작성자 전화번호<br />(전체보기를 선택하면 입력필요없음.)</td>
<td><input type="text" name="usr_phone"/></td>
</tr>

<tr>
<td colspan="2" align="center">
<input type="button" value="승인" onClick="submit()"/>
</td>
</tr>
</table>
</form>


</div>




</body>
</html>