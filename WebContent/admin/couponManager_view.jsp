<%@page import="javax.jws.soap.SOAPBinding.Use"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="carcaro.bean.Coupon" %>
<%@ page import="net.sf.json.JSONArray" %>
<%@ page import="net.sf.json.JSONObject" %>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List" %>
<%@page import="carcaro.util.Util"%>
<%@page import="carcaro.util.PagingHelper"%>
<%@page import="carcaro.Codes"%>
<%
long plTotalCnt = (Long) request.getAttribute("plTotalCnt"); // 전체 레코드 수
long pageSize = (Long) request.getAttribute("pageSize"); // 한 페이지당 레코드 수
long plPageRange = 5; // 페이지 출력 범위
long plCurrPage = (Long) request.getAttribute("pageNum"); // 현재 페이지


String gubun = (String) request.getAttribute("gubun"); // 전체, 사용, 미사용
String column = (String) request.getAttribute("column"); // 선택항목 : 쿠폰번호, 고객전화, 고객이름
String value = (String) request.getAttribute("value"); 
String name = Util.NVL((String)request.getAttribute("name"));
Coupon[] list =(Coupon[])request.getAttribute("list");
String coupon_id = (String) request.getAttribute("coupon_id");
String msgTitle = (String) request.getAttribute("msg1");
//String driver_id = (String) request.getAttribute("driver_id");
String customer_phone = (String) request.getAttribute("customer_phone");

%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script src="./js/jquery-1.4.4.min.js"></script>
<script src="./js/jquery.mousewheel.min.js"></script>
<script src="./js/calendar_ahmax.js"></script>
<script src="./js/json2.js"></script>
<script src="./js/ajax.js"></script>
<link rel='stylesheet' href='./js/calendar_ahmax.css' type='text/css' />
<link rel='stylesheet' href='/css/style.css' type='text/css' />
<script type="text/javascript">

	if (ids) {
		//selectPopup();

	} else {
		if (msg1 != "null")
			alert(msg1);
	}

	function selectPopup() {
		window.open("carcaro/selectId.html", "selectId",
				"status = 1, height = 200, width = 340, resizable = 0");
	}

	function selectDriverIdFromPopup(id) {
		var n = document.getElementsByName("name")[0];
		var f = document.getElementsByName("driverId")[0];
		n.value = name;
		f.value = id;

		//	alert(f.value);
		document.search.submit();
	}
	function goPage(curr) {
		var frm = document.search;
		frm.curr_page.value = curr;
		frm.submit();
	}

	function couponCSV() {
		var popW = 345;
		var popH = 210;
		var left = (screen.width - popW) / 2;
		var top = (screen.height - popH) / 3;
		window.open("admin/couponCSV_view.jsp", "", "width=" + popW
				+ ", height=" + popH + " , left=" + left + ",top=" + top,
				"scrollbars=no");
	}

	function couponCreate() {
		var popW = 330;
		var popH = 210;
		var left = (screen.width - popW) / 2;
		var top = (screen.height - popH) / 3;
		window.open("admin/couponCreate_view.jsp", "", "width=" + popW
				+ ", height=" + popH + " , left=" + left + ",top=" + top,
				"scrollbars=no");
	}

	function couponRelocate() {
		//쿠폰 이전시 
		var popW = 345;
		var popH = 260;
		var left = (screen.width - popW) / 2;
		var top = (screen.height - popH) / 3;
		window.open("admin/couponRelocate_view.jsp", "", "width=" + popW
				+ ", height=" + popH + " , left=" + left + ",top=" + top,
				"scrollbars=no");
		//window.showModalDialog("admin/couponCreate_view.jsp", self, "(window.screen.width / 2) - (Number(iWidth) / 2);  (window.screen.height / 2) - (Number(iHeight) / 2); dialogWidth:330px; dialogHeight:190px"); 
	}

	
	function couponDelete(search) {
		var selectedCheckId = 0;
		var selected = false;
		var num = 0;
		var form = document.search;
		if (form.check.length > 1) {
			for (i = 0; i < form.check.length; i++) {
				if (form.check[i].checked) {
					num = form.check[i].value;
					selectedCheckId = i;
					selected = true;
					break;
				}
			}
		} else {
			if (form.check.checked) {
				num = form.check.value;
				selected = true;
			}
		}
		if (selected) {
			var up_con = confirm("삭제하시겠습니까?");
			if (up_con == true) {
				var coupon_id = form.coupon_id[selectedCheckId].value;
				//alert(coupon_id);
				var url = "./ccr?cmd=couponManager&action=del";
				var params = "coupon_id=" + coupon_id;
				sendRequest(url, params, couponMngResult, "POST");
				//alert("삭제완료");
			}
		} else {
			alert("삭제할 항목을 먼저 선택해 주세요.");
		}
	}

	function couponUse() {
		var selectedCheckId = 0;
		var selected = false;
		var num = 0;
		var form = document.search;
		if (form.check.length > 1) {
			for (i = 0; i < form.check.length; i++) {
				if (form.check[i].checked) {
					num = form.check[i].value;
					selectedCheckId = i;
					selected = true;
					break;
				}
			}
		} else {
			if (form.check.checked) {
				num = form.check.value;
				selected = true;
			}
		}
		if (selected) {
			var up_con = confirm("사용하시겠습니까?");
			if (up_con == true) {
				var coupon_id = form.coupon_id[selectedCheckId].value;
				var customer_phone = form.customer_phone[selectedCheckId].value;
				var driver_id = "";
				//alert(coupon_id);
				//alert(customer_phone);
				//alert(driver_id);
				var url = "./ccr?cmd=couponManager&action=use";
				var params = "coupon_id=" + coupon_id;
				params += "&customer_phone=" + customer_phone;
				params += "&driver_id=" + driver_id;
				sendRequest(url, params, couponMngResult, "POST");
				//alert("사용완료");
			}
		} else {
			alert("사용할 항목을 먼저 선택해 주세요.");
		}
	}

	function couponMngResult() {
		if (httpRequest.readyState == 4) {
			if (httpRequest.status == 200) {
				response = httpRequest.responseText;
				alert(response);
				window.location.reload(true);
			}
		}
	}
</script>

</head>
<body>
<br />
<h3>쿠폰 관리</h3>
<form name="search" method=POST  action="ccr?cmd=couponManager&action=mgr">

		<input type="hidden" name="curr_page" value="<%=plCurrPage%>" /> 
		<input type="hidden" name="pageSize" value="<%=pageSize%>" /> 
		<select id="gubun" name="gubun">
			<option value="0">전체</option>
			<option value="1">사용</option>
			<option value="2">미사용</option>
		</select>
		<select id="column" name="column">
			<option value="0">쿠폰번호</option>
			<option value="1">고객전화</option>
			<option value="2">고객이름</option>
		</select>
		<input type="text" name="value" value=<%=value%> >
		<input type="submit" value="조회" >
		<input type="button" value="사용" onclick="JavaScript:couponUse()">
		<input type="button" value="삭제" onclick="JavaScript:couponDelete()">
		<input type="button" value="이전" onclick="JavaScript:couponRelocate()">
		<input type="button" value="생성" onclick="JavaScript:couponCreate()">
		<input type="button" value="CSV" onclick="JavaScript:couponCSV()">

	
		

	<br>
	<br>
	
		<table class="res">
			<thead>
				<tr>
					<th></th>
					<th>쿠폰번호</th>
					<th>가격</tH>
					<th>고객명</th>
					<th>고객전화</th>
					<th>등록시간</th>
					<th>이전소유자</th>
					<th>수정시간</th>
					<th>현재상태</th>
	
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
				Coupon cp = list[i];

				String phone = cp.getcPhone();
				String oPhone=cp.getLastOwner();
				String regDate = cp.getRegDate();
				String modDate = cp.getModDate();
				String couponId=cp.getCoupon_id();
				String use =cp.getStatus();
				if(phone == null || phone.length() <11)
					phone = "00000000000"; // 대충 해당길이를 넣어줘라
				if(oPhone == null || oPhone.length() <11)
					oPhone = "00000000000"; // 대충 해당길이를 넣어줘라
				if(regDate == null || regDate.length() == 0)
					regDate = "0000-00-00 00:00:00.0";
				if(modDate == null || modDate.length() == 0)
					modDate = "0000-00-00 00:00:00.0";
				if(couponId==null)
					System.out.println("쿠폰 id는 null입니다");
					
			
		
			%>
			
			<tr class="element">
				<td><input type="checkbox" name="check"/></td>
				<td><%=cp.getCoupon_id() %><input type="hidden" name="coupon_id" id="coupon_id" value='<%=cp.getCoupon_id() %>'/></td>
				<td><%=cp.getAmount() %></td>
				<td><%=cp.getcName() %></td>
				<td><%=phone.substring(0, 3)+"-"+phone.substring(3,7)+"-"+phone.substring(7,11)%>
				<input type="hidden" name="customer_phone" id="customer_phone" value='<%=phone.substring(0, 3)+"-"+phone.substring(3,7)+"-"+phone.substring(7,11)%>'/>
				</td>
				<td><%= regDate.split(" ")[0]+" "+regDate.split(" ")[1].substring(0, 5) %></td>
				<td><%=oPhone.substring(0,3)+"-"+oPhone.substring(3,7)+"-"+oPhone.substring(7,11) %></td>
				<td><%=modDate.split(" ")[0]+" "+modDate.split(" ")[1].substring(0, 5)%></td>
				<td><%=cp.getStatus() %></td>
			</tr>
			
			
				<%
				
				}
			%>
			<%
			}
			%>
		</table>
</form>
	<table class="res">
	<tr>
	<td align="center">
	<div class="pager">
		<%=PagingHelper.instance.autoPaging(plTotalCnt, pageSize, plPageRange, plCurrPage)%>
	</div>
	</td>
	</tr>
	<tr>
	<td>합계 총: 쿠폰 수<%=plTotalCnt %>개 | 사용 개 | 미사용 개 </td>
	</tr>
	</table>	

</body>
</html>
