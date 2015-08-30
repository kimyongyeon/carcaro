<%@page import="carcaro.util.PagingHelper"%>
<%@page import="carcaro.bean.Admin"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List" %>
<%@page import="carcaro.util.Util"%>
<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%
long plTotalCnt = (Long)request.getAttribute("plTotalCnt"); // 전체 레코드 수
long pageSize = (Long)request.getAttribute("pageSize"); // 한 페이지당 레코드 수
long plPageRange = 5; // 페이지 출력 범위
long plCurrPage = (Long)request.getAttribute("pageNum"); // 현재 페이지
%>
<%
	String msg1 = (String) request.getAttribute("msg1");
	String name = Util.NVL((String)request.getAttribute("name"));
	ArrayList<String> ids = (ArrayList<String>) request.getAttribute("ids");
	
	String no= (String)request.getAttribute("no");
	String access = (String)request.getAttribute("access");//지사
	String sido = (String)request.getAttribute("sido");
	String gu = (String)request.getAttribute("gu");
	Admin[] list =(Admin[])request.getAttribute("list");
	

	
	
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script src="./js/jquery-1.4.4.min.js"></script>
<script src="./js/jquery.mousewheel.min.js"></script>
<script src="./js/calendar_ahmax.js"></script>
<script src="./js/json2.js"></script>
<script src="./js/ajax.js"></script>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>
<script src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/jquery-ui.min.js"></script>
<link href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css" rel="stylesheet" type="text/css"/>
<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script>
	
	var ids =<%=ids != null ? ids.size() : 0%>;
	var msg1 ="<%=msg1%>";
	var idsArray = "<%=ids %>";
	var name = "<%=name%>";
	
	if( ids ){
		//selectPopup();
		
	} else {
		if( msg1 != "null" )
		alert(msg1);
	}
	
	function selectPopup() {
		window.open( "carcaro/selectId.html", "selectId", 
		"status = 1, height = 200, width = 340, resizable = 0" );
	}
	
	function selectDriverIdFromPopup(id){
		var n = document.getElementsByName("name")[0];
		var f = document.getElementsByName("driverId")[0];
		n.value = name;
		f.value = id;

//		alert(f.value);
		document.search.submit();
	}
	function goPage(curr){
		var frm = document.search;
		frm.curr_page.value = curr;
		frm.submit();
	}
	function updateGugun(Sido){
		var gugun = document.search.gugun;
		
	}
function adminUpdate(search) {
		var selected = false;
		  var num = 0;
		  var form = document.search;
		  if(form.radio.length > 1){
		   for(i = 0; i< form.radio.length; i++){
		    if(form.radio[i].checked){
		     num = form.radio[i].value;
		     selected = true;
		     break; 
		    }
		   }
		  } else {
		   if(form.radio.checked){
		    num = form.radio.value;
		    selected = true;
		   }
		  }
		  if(selected){
		   var up_con = confirm("수정하시겠습니까?");
		   if(up_con == true){    
			   window.showModalDialog("admin/adminUpdate_view.jsp", self, "(window.screen.width / 2) - (Number(iWidth) / 2); (window.screen.height / 2) - (Number(iHeight) / 2); dialogWidth:600px; dialogHeight:350px"); 
			
		   }
		  }else{
		   alert("수정할 항목을 먼저 선택해 주세요.");
		  }
		
	}


	function adminRegister()
	{ 
	        window.showModalDialog("admin/adminRegister_view.jsp", self, "(window.screen.width / 2) - (Number(iWidth) / 2); (window.screen.height / 2) - (Number(iHeight) / 2); dialogWidth:600px; dialogHeight:350px"); 
	} 
	
	
						
</script>



</head>
<body>
<br/>
<h3>관리자관리</h3>

<form name="search" method=POST action="./ccr?cmd=adminManager&action=list">
 	<input type="hidden" name="curr_page" value="<%=plCurrPage%>" /> 
	<input type="hidden" name="pageSize" value="<%=pageSize%>" />
 	<table class="res">
 	<thead>
 	<tr>
 	<th>권한</th>
 	<th>시/도</th>
 	<th>구/읍</th>
 	<th>조회</th>
 	<th>수정</th>
 	<th>등록</th>
 	</tr>
 	</thead>
	
	<tr>
	<th><select id="access" name="access">
		<option value="1">본사</option>
		<option value="10">지사</option>
		<option value="20">대리점</option>
	</select></th>
	<th>
	<select name="sido" >
		<option value="%" selected="selected">전국
		<option value="강원">강원
		<option value="경기">경기
		<option value="경남">경남
		<option value="경북">경북
		<option value="광주">광주
		<option value="대구">대구
		<option value="대전">대전
		<option value="부산">부산
		<option value="서울">서울
		<option value="울산">울산
		<option value="인천">인천
		<option value="전남">전남
		<option value="전북">전북
		<option value="제주">제주
		<option value="충남">충남
		<option value="충북">충북
	</select>
	</th>	
	<th>
		<select name="gu">
		<option value=""></option>
	</select>
	</th>
    <th><input type="submit" value="조회"></th> 
	<th><input type="button" value= "수정" onclick='JavaScript:adminUpdate()' ></th> 
	<th><input type="button" value= "생성"onclick='JavaScript:adminRegister()'></th>
	</tr>
		</table>
		<table class="res">
	<thead>
				<tr>
					<th></th>
					<th>No</th>
					<th>ID</th>
					<th>PW</th>
					<th>권한</th>
					<th>시/도</th>
					<th>구/읍</th>
					<th>담당자</th>
					<th>대표전화</th>
					<th>휴대폰</th>
					<th>주소</th>
				</tr>
			</thead>
			<%
				if (list.length == 0) {
			%>
			<tr>
				<td colspan="10"><%=name%>으로 검색한 결과가 없습니다.</td>
			</tr>
			<%
				} else {
			%>

			<%
				for (int i = 0; i < list.length; i++) {
					Admin ad = list[i];
			%>
			<tr class="element">
				<th><input type="radio" name="radio" value=<%=i + 1%> /></th>
				<th><%=ad.getNo() %></th>
				<th><%=ad.getId() %></th>
				<th><%=ad.getPw() %></th>
				<th><%=ad.getAccessLevel() %></th>
				<th>시도</th>
				<th>구읍</th>
				<th><%=ad.getName() %></th>
				<th><%=ad.getTel() %></th>
				<th><%=ad.getSmartPhone() %></th>
				<th><%=ad.getAddress() %></th>
				</tr>
				<%
				}
			%>
			<%
			}
			%>
			
	</table>

	</form>	

	<!-- Paging -->
	<table class="res">
	<tr>
	<td align="center">
	<div class="pager">
		<%=PagingHelper.instance.autoPaging(plTotalCnt, pageSize, plPageRange, plCurrPage)%>
	</div>
	</td>
	</tr>

	</table>	

</body>
</html>
