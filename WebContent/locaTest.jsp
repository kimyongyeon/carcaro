<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
<%@ page import="carcaro.ConnectionPool" %>
<%@ page import="carcaro.dao.LocationDAO" %>
<%@ page import="java.util.List" %>
<%@ page import="net.sf.json.JSONArray" %>
<%@ page import="net.sf.json.JSONObject" %>

<%

ConnectionPool connPool = ConnectionPool.getInstance();
LocationDAO locationDAO = new LocationDAO(connPool);

JSONArray sido = locationDAO.getSido(null);

String curSido = request.getParameter("curSido");
String curGugun = request.getParameter("curGugun");



%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=EUC-KR">
<title>Insert title here</title>


</head>
<body>


<form name="locationTest" action="">

<script>
function loadGugun(){
	var cSido = document.getElementById('curSido');
	

}
</script>

<select name="curSido" onChange="loadGugun()">
	<option value="sel" selected>시/도</option>
<% for ( int i = 0 ; i < sido.size(); i ++ ){
	JSONObject val = sido.getJSONObject(i);
	String sidoString = val.getString("Sido");
	%>
	<option value="<%=sidoString%>" <%if(curSido != null && curSido.equals(sidoString)){%>selected<%}%>><%=sidoString%></option>
	<%
	
}
%>
</select>
<select name="curGugun">
	
</select>

</form>



</body>
</html>