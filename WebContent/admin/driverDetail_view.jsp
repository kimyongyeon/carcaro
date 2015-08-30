<%@page import="carcaro.util.Util"%>
<%@page import="carcaro.util.PagingHelper" %>
<%@page import="carcaro.Codes"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.concurrent.ConcurrentHashMap"%>
<%@page import="carcaro.bean.ChargeHistory"%>
<%@page import="carcaro.bean.ChargeHistoryPeriod"%>
<%@page import="carcaro.ConnectionPool" %>
<%@page import="carcaro.dao.LocationDAO" %>
<%@page import="java.util.List" %>
<%@page import="net.sf.json.JSONObject" %>
<%@page import="net.sf.json.JSONArray" %>
<%@page import="org.joda.time.LocalDate" %>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
	
<%
String today = Util.getCurrDate();
String msg1 = (String) request.getAttribute("msg1");
String dateA= (String)request.getAttribute("dateA");
String dateB= (String)request.getAttribute("dateB");
String local = (String)request.getAttribute("local");
String result = (String) session.getAttribute("result");
String name = Util.NVL((String)request.getAttribute("name"));
	
	// Session info
	String aL = request.getParameter("aL"); // access Level
	String location = request.getParameter("location");
	
	ArrayList<String> ids = (ArrayList<String>) request.getAttribute("ids");
	ChargeHistory[] list = (ChargeHistory[]) request.getAttribute("list");
	
	// connPool
	ConnectionPool connPool = ConnectionPool.getInstance();
	LocationDAO locationDAO = new LocationDAO(connPool);
	
	JSONArray locations = locationDAO.getSido(local);
	
%>

<html>
	
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script src="./js/jquery-1.4.4.min.js"></script>
<script src="./js/jquery.mousewheel.min.js"></script>
<script src="./js/calendar_ahmax.js"></script>
<link rel='stylesheet' href='./js/calendar_ahmax.css' type='text/css' />
<link rel='stylesheet' href='/css/style.css' type='text/css' />
<script type="text/javascript">

</script>
</head>
<body>
<table class ="res">

  <tr>
 
    <td colspan="4">대리운전 상세내역</td>
  </tr>
  <tr>
    <td>날짜</td>
    <td>Bid</td>
    <td>기사이름</td>
    <td></td>
  </tr>
  <tr>
    <td>시간</td>
    <td></td>
    <td>기사전화</td>
    <td>&nbsp;</td>
  </tr>
  <tr>
    <td>고객위치</td>
    <td>&nbsp;</td>
    <td>결과</td>
    <td></td>
  </tr>
  <tr>
    <td>출발지</td>
    <td></td>
    <td>수수료</td>
    <td>&nbsp;</td>
  </tr>
  <tr>
    <td>목적지</td>
    <td></td>
    <td>요금</td>
    <td></td>
  </tr>
  <tr>
    <td width="90">고객이름</td>
    <td width="225"></td>
    <td width="76">쿠폰금액</td>
    <td width="215">&nbsp;</td>
  </tr>
  <tr>
    <td>고객전화</td>
    <td></td>
    <td>결제금</td>
    <td>&nbsp;</td>
  </tr>
  <tr>
    <td>쿠폰</td>
    <td>&nbsp;</td>
    <td colspan="2">&nbsp;</td>
  </tr>
  <tr>
    <td colspan="4" align="right"><input type="button" value="닫기" onClick="self.close()"/></td>

		
  </tr>
  
</table>


</body>
</html>