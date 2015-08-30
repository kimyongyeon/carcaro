<%@page import="carcaro.bean.Settlement"%>
<%@page import="carcaro.util.PagingHelper"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%
long plTotalCnt = (Long)request.getAttribute("plTotalCnt"); // 전체 레코드 수
long pageSize = (Long)request.getAttribute("pageSize"); // 한 페이지당 레코드 수
long plPageRange = 5; // 페이지 출력 범위
long plCurrPage = (Long)request.getAttribute("pageNum"); // 현재 페이지
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>결제 내역</title>
<script>
function goPage(curr){
	var frm = document.pageForm;
	frm.curr_page.value = curr;
	frm.submit();
}
</script>
</head>
<body>
	<h3>결제 내역</h3>
	<br>
	<br>
	<table width="90%" cellpadding=0 cellspacing=0>
		<tr>
			<td></td>
			<td>주문번호</td>
			<td>결제번호</td>
			<td>가격</td>
			<td>승인시간</td>
			<td>결제수단</td>
		</tr>
		<%
				Settlement settle = new Settlement();
				Settlement[] list = (Settlement[]) request.getAttribute("list");
				out.write("데이터 가져옴");
				if(list.length>0){
					
				for (int i = 0; i < list.length; i++) {
					settle = list[i];
			%>
		<tr>
			<td><%=i + 1%></td>
			<td><%=settle.getOid()%></td>
			<td><%=settle.getTid()%></td>
			<td><%=settle.getAmount()%></td>
			<td><%=settle.getSettleTime()%></td>
			<td><%=settle.getMethod()%></td>
		</tr>
		<%
				}
			}
			%>
	</table>
	</form>
	<table width="90%">
		<tr>
			<td align="center">
				<form name="pageForm" method="post">
					<input type="hidden" name="curr_page" value="<%=plCurrPage%>" /> <input
						type="hidden" name="pageSize" value="<%=pageSize%>" />
				</form> <%=PagingHelper.instance.autoPaging(plTotalCnt, pageSize, plPageRange, plCurrPage)%>
			</td>
		</tr>
	</table>
</body>
</html>