package carcaro.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;


import carcaro.ConnectionPool;
import carcaro.MainServlet;
import carcaro.ServerProcess;
import carcaro.bean.ChargeHistory;
import carcaro.bean.ChargeHistoryPeriod;
import carcaro.bean.Customer;
import carcaro.bean.Driver;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ChargeHistoryDAO{

	// Connection Pool
	private ConnectionPool connPool;

	// Connection 설정
	public ChargeHistoryDAO(ConnectionPool connPool) {
		this.connPool = connPool;
	}

	public void recordCharge(String driverId, String cDevId, String cPhone, int businessType, String Src, String Dst, int charge) throws SQLException{
		// DB 연결 관련 변수
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql.append(" INSERT INTO charge_history ( driverId, fee, businessTime, businessType, customerDeviceId, customerPhone, " +
					"source, destination, drivingCharge) VALUES (?, ?, NOW(), ?, ?,?,?,?,?);");

			stmt = conn.prepareStatement(sql.toString());

			stmt.setString(1, driverId);
			if ( businessType == Driver.SETTLEMENT ) stmt.setInt(2, Driver.FEE);
			else stmt.setInt(2, 0);
			stmt.setInt(3, businessType);
			stmt.setString(4, cDevId);
			stmt.setString(5, cPhone);
			stmt.setString(6, Src);
			stmt.setString(7, Dst);
			stmt.setInt(8, charge);

			stmt.execute();
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)

				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
	}

	public ChargeHistory[] getChargeHistory(String date, String local, String result, String driverId, String name, int currentPage, int pageSize) throws SQLException {

		List<ChargeHistory> list = new ArrayList<ChargeHistory>();
		ChargeHistory ch;
		//JSONArray retJson = new JSONArray();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = connPool.getConnection();
			// sql 명령
			String sql = "SELECT charge_history.*, name " +
					"FROM charge_history JOIN driver ON driver.driverId = charge_history.driverId " +
					"WHERE businessTime like ? and source like ? and businessType like ? ";

			if (driverId != null && driverId.length() != 0) {
				sql += " and charge_history.driverId =? ";
			}
			if (name != null && name.length() != 0) {
				sql += " and driver.name = ? ";
			}
			sql += " ORDER BY BID DESC";
			sql += " LIMIT ?,?";
			pstmt = conn.prepareStatement(sql);

			int a = 1;
			pstmt.setString(a++, date + "%");
			pstmt.setString(a++, local + "%");
			pstmt.setString(a++, result);
			if (driverId != null && driverId.length() != 0) {
				pstmt.setString(a++, driverId);
			}			
			if(name != null && name.length() != 0) {
				pstmt.setString(a++, name);
			}
			pstmt.setInt(a++, (currentPage-1)*pageSize);
			pstmt.setInt(a++, pageSize);

			rs = pstmt.executeQuery();

			while(rs.next()){
				ch = new ChargeHistory();

				ch.setBid(Integer.parseInt(rs.getString("BID")));
				ch.setDriverId(driverId);
				ch.setBusinessTime(rs.getString("businessTime"));
				ch.setBusinessType(rs.getInt("businessType"));
				ch.setCustomerDeviceId(rs.getString("customerDeviceId"));
				ch.setSource(rs.getString("source"));
				ch.setDestination(rs.getString("destination"));
				ch.setFee(Integer.parseInt(rs.getString("fee")));
				ch.setDrivingCharge(Integer.parseInt(rs.getString("drivingCharge")));
				
				String phone = rs.getString("customerPhone");
				// 주민번호나 전화번호가 있는 부분은 모두 아래와 같이 처리를 해줘야 함.
				// 이유는 클라이언트에서 문자열처리를 하기 때문에 null이나 공백이 오면 오류가 난다고 함.
				if(phone == null || phone.length() == 0){
					phone = "00000000000";
				}
				ch.setCustomerPhone(phone);
				ch.setDriverId(rs.getString("driverId"));
				ch.setDriverName(rs.getString("name"));
				//ch.setLat(rs.getString("Lat")); // 위도
				//ch.setLng(rs.getString("Lat")); // 경도

				list.add(ch);
			}

		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}

		return list.toArray(new ChargeHistory[0]);
	}
	public JSONArray getLocationInfoByLocaName(String sido, String gugun, String dong) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		JSONArray retJson = new JSONArray();

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT * FROM `address` WHERE Sido LIKE ? AND Gugun LIKE ? AND Dong LIKE ?");

			if ( sido == null || sido.length() == 0 ){
				sido = "%";
			}
			if ( gugun == null || gugun.length() == 0 ){
				gugun = "%";
			}
			if ( dong == null || dong.length() == 0 ){
				dong = "%";
			}
			stmt = conn.prepareStatement(sql.toString());
			stmt.setString(1, sido);
			stmt.setString(2, gugun);
			stmt.setString(3, dong);
			rs = stmt.executeQuery();

			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("LID", rs.getInt("LID"));
				json.put("Lat", rs.getString("Lat"));
				json.put("Lng", rs.getString("Lng"));
				json.put("Sido", rs.getString("Sido"));
				json.put("Gugun", rs.getString("Gugun"));
				json.put("Dong", rs.getString("Dong"));
				retJson.add(json);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		return retJson;
	}
	
	public ConcurrentHashMap<String, ChargeHistoryPeriod> getChargeHistoryPeriod(String dateA, String dateB, String local, String result) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ConcurrentHashMap<String, ChargeHistoryPeriod> CHP = new ConcurrentHashMap<String, ChargeHistoryPeriod>();

		// 날짜별로 List생성.
		DateTimeFormatter fmt = DateTimeFormat.forPattern("y-MM-dd");		// FORMATTER YYYY-MM-DD
		DateTime start = DateTime.parse(dateA,fmt);
		DateTime end = DateTime.parse(dateB,fmt);
		end = end.plusDays(1);							// END+1

		List<DateTime> dTime = new ArrayList<DateTime>();
		DateTime tmp = start;

		while(tmp.isBefore(end) || tmp.equals(end)) {
			dTime.add(tmp);

			ChargeHistoryPeriod tmpCHP = new ChargeHistoryPeriod();
			tmpCHP.setBusinessDate(tmp.toString(fmt));
			tmpCHP.setTotalCharge(0);
			tmpCHP.setTotalFee(0);
			tmpCHP.setBusinessTypeCount(0, 0);
			tmpCHP.setBusinessTypeCount(1, 0);
			tmpCHP.setBusinessTypeCount(2, 0);
			tmpCHP.setBusinessTypeCount(3, 0);

			// Put into Concurrent HashMap
			CHP.put(tmp.toString(fmt), tmpCHP);

			tmp = tmp.plusDays(1);
		}

		try {
			conn = connPool.getConnection();
			/*
			 * SQL QUERY 설명
			 * charge_history의 모든 컬럼
			 * 영업시간  dateA < x < dateB 사이에 존재하고 목적지가 지정된 곳
			 */
			String sql = 	"SELECT * " +
					"FROM charge_history " +
					"WHERE businessTime BETWEEN ? AND ? " +
					"AND source LIKE ? AND businessType LIKE ? ";

			sql += " ORDER BY BID DESC ;";
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, start.toString(fmt) + "%");		// 영업날짜
			pstmt.setString(2, end.toString(fmt) + "%");		// 영업날짜
			pstmt.setString(3, local + "%");		// 지역
			pstmt.setString(4, result);			// 대리결과 
			rs = pstmt.executeQuery();

			while(rs.next()){
				String businessTime = rs.getString("businessTime");
				businessTime = businessTime.substring(0, 10);
				// find my CH
				ChargeHistoryPeriod chp = CHP.get(businessTime);
				chp.setTotalCharge(chp.getTotalCharge()+rs.getInt("drivingCharge"));
				chp.setTotalFee(chp.getTotalFee()+rs.getInt("fee"));
				int bType = rs.getInt("businessType")-1;
				if (bType >= 0 && bType <=2){
					chp.addBusinessTypeCount(bType, 1);
				}

				// put to hashmap
				CHP.put(businessTime, chp);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		} 
		return CHP;
	}

	public ConcurrentHashMap<String, ChargeHistoryPeriod> getChargeHistoryMonth(String date, String local, String result) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ConcurrentHashMap<String, ChargeHistoryPeriod> CHP = new ConcurrentHashMap<String, ChargeHistoryPeriod>();
	
		// 첫번 째 날로 강제 지정..
//		date = date.substring(0,7).concat("-01");
		date = date.substring(0,7);
		
		
		ChargeHistoryPeriod tmpCHP = new ChargeHistoryPeriod();
		tmpCHP.setBusinessDate(date);
		tmpCHP.setTotalCharge(0);
		tmpCHP.setTotalFee(0);

		// Put into Concurrent HashMap
		CHP.put(date, tmpCHP);
		
		// 날짜별로 List생성.
//		DateTimeFormatter fmt = DateTimeFormat.forPattern("y-MM-dd");		// FORMATTER YYYY-MM-DD
//		DateTime start = DateTime.parse(date,fmt);
//		DateTime end = start.plusMonths(1);
		//		end = end.plusDays(1);							// END+1
	
//		List<DateTime> dTime = new ArrayList<DateTime>();
//		DateTime tmp = start;
//	
//		while(tmp.isBefore(end) ) {
//			dTime.add(tmp);
//	
//			ChargeHistoryPeriod tmpCHP = new ChargeHistoryPeriod();
//			tmpCHP.setBusinessDate(tmp.toString(fmt));
//			tmpCHP.setTotalCharge(0);
//			tmpCHP.setTotalFee(0);
//	
//			// Put into Concurrent HashMap
//			CHP.put(tmp.toString(fmt), tmpCHP);
//	
//			tmp = tmp.plusDays(1);
//		}
	
		try {
			conn = connPool.getConnection();
			/*
			 * SQL QUERY 설명
			 * charge_history의 모든 컬럼
			 * 영업시간  dateA < x < dateB 사이에 존재하고 출발지가 지정된 곳
			 */
			String sql = 	"SELECT * " +
					"FROM charge_history " +
					"WHERE businessTime LIKE ? " +
					"AND source LIKE ? AND businessType LIKE ? ";
	
			sql += " ORDER BY BID DESC ;";
			pstmt = conn.prepareStatement(sql);
	
//			pstmt.setString(1, start.toString(fmt) + "%");		// 영업날짜
//			pstmt.setString(2, end.toString(fmt) + "%");		// 영업날짜
			pstmt.setString(1, date+"%");
			pstmt.setString(2, local + "%");		// 지역
			pstmt.setString(3, result);			// 대리결과 
			rs = pstmt.executeQuery();
	
			while(rs.next()){
				String businessTime = rs.getString("businessTime");
				businessTime = businessTime.substring(0, 7);
				// find my CH
				
				ChargeHistoryPeriod chp = CHP.get(businessTime);
				chp.setTotalCharge(chp.getTotalCharge()+rs.getInt("drivingCharge"));
				chp.setTotalFee(chp.getTotalFee()+rs.getInt("fee"));
				int bType = rs.getInt("businessType")-1;
				if (bType >= 0 && bType <=2){
					chp.addBusinessTypeCount(bType, 1);
				}
	
				// put to hashmap
				CHP.put(businessTime, chp);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		} 
		return CHP;
	}
	
	/**
	 * 입출금내역/정산내역 관리
	 * @param date : 날짜 첫번째 날로 강제 지정
	 * @param local : 1-본사, 10-지사, 20-대리점
	 * @param result : 대리결과
	 * @return
	 * @throws SQLException
	 */
	public ConcurrentHashMap<String, ChargeHistoryPeriod> getChargeHistoryMonth2(String date, String local, String result) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ConcurrentHashMap<String, ChargeHistoryPeriod> CHP = new ConcurrentHashMap<String, ChargeHistoryPeriod>();
	
		// 첫번 째 날로 강제 지정..
		date = date.substring(0,7).concat("-01");
//		date = date.substring(0,7);
		
		
//		ChargeHistoryPeriod tmpCHP = new ChargeHistoryPeriod();
//		tmpCHP.setBusinessDate(date);
//		tmpCHP.setTotalCharge(0);
//		tmpCHP.setTotalFee(0);
//
//		// Put into Concurrent HashMap
//		CHP.put(date, tmpCHP);
		
		// 날짜별로 List생성.
		DateTimeFormatter fmt = DateTimeFormat.forPattern("y-MM-dd");		// FORMATTER YYYY-MM-DD
		DateTime start = DateTime.parse(date,fmt);
		DateTime end = start.plusMonths(1);
		//		end = end.plusDays(1);							// END+1
	
		List<DateTime> dTime = new ArrayList<DateTime>();
		DateTime tmp = start;
	
		while(tmp.isBefore(end) ) {
			dTime.add(tmp);
	
			ChargeHistoryPeriod tmpCHP = new ChargeHistoryPeriod();
			tmpCHP.setBusinessDate(tmp.toString(fmt));
			tmpCHP.setTotalCharge(0);
			tmpCHP.setTotalFee(0);
	
			// Put into Concurrent HashMap
			CHP.put(tmp.toString(fmt), tmpCHP);
	
			tmp = tmp.plusDays(1);
		}
	
		try {
			conn = connPool.getConnection();
			/*
			 * SQL QUERY 설명
			 * charge_history의 모든 컬럼
			 * 영업시간  dateA < x < dateB 사이에 존재하고 출발지가 지정된 곳
			 */
			// businessTime : 대리시간
			// businessType : 완료종류(성공,대리취소,고객취소)
			// source : 출발지
			String sql = 	"SELECT * " +
					"FROM charge_history " +
					"WHERE businessTime BETWEEN ? AND ?  " +
					"AND source LIKE ? AND businessType LIKE ? ";
	
			sql += " ORDER BY BID DESC ;";
			pstmt = conn.prepareStatement(sql);
	
			pstmt.setString(1, start.toString(fmt) + "%");		// 영업날짜
			pstmt.setString(2, end.toString(fmt) + "%");		// 영업날짜
//			pstmt.setString(1, date+"%");
			pstmt.setString(3, local + "%");		// 지역
			pstmt.setString(4, result);			// 대리결과 
			rs = pstmt.executeQuery();
	
			while(rs.next()){
				String businessTime = rs.getString("businessTime");
				businessTime = businessTime.substring(0, 10);
				// find my CH
				
				ChargeHistoryPeriod chp = CHP.get(businessTime);
				chp.setTotalCharge(chp.getTotalCharge()+rs.getInt("drivingCharge"));
				chp.setTotalFee(chp.getTotalFee()+rs.getInt("fee"));
				int bType = rs.getInt("businessType")-1;
				if (bType >= 0 && bType <=2){
					chp.addBusinessTypeCount(bType, 1);
				}
	
				// put to hashmap
				CHP.put(businessTime, chp);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		} 
		return CHP;
	}
	/**
	 * 일별 대리운전 조회
	 * @param dateA
	 * @param dateB
	 * @param local
	 * @param result
	 * @param driverId
	 * @param name
	 * @param currentPage
	 * @param pageSize
	 * @return
	 * @throws SQLException
	 */
	public ChargeHistory[] getChargeHistoryDay(String dateA, String dateB,
			String local, String result, String driverId, String name,
			int currentPage, int pageSize) throws SQLException {
		List<ChargeHistory> list = new ArrayList<ChargeHistory>();

		Customer customer;
		ConcurrentHashMap<String, Customer> customerList = ServerProcess.getCustomers();
		if(customerList != null)
			System.out.println("customerList Size: "+ customerList.size());
		
		ChargeHistory ch;
	
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			conn = connPool.getConnection();
			// sql 명령
			String sql = "SELECT charge_history.*, name, phone " +
					"FROM charge_history JOIN driver ON driver.driverId = charge_history.driverId " +
					"WHERE businessTime >= ? AND businessTime <= ? " +
					"AND source like ? and businessType like ? ";
	
			if (driverId != null && driverId.length() != 0) {
				sql += " and charge_history.driverId =? ";
			}
			if (name != null && name.length() != 0) {
				sql += " and driver.name = ? ";
			}
			sql += " ORDER BY BID DESC";
			sql += " LIMIT ?,?";
			
			System.out.println("sql==="+sql);
			
			pstmt = conn.prepareStatement(sql);
	
			int a = 1;
			pstmt.setString(a++, dateA + "%");
			pstmt.setString(a++, dateB + "%");
			pstmt.setString(a++, local + "%");
			pstmt.setString(a++, result);
			
			if (driverId != null && driverId.length() != 0) {
				pstmt.setString(a++, driverId);
			}			
			if(name != null && name.length() != 0) {
				pstmt.setString(a++, name);
			}
			pstmt.setInt(a++, (currentPage-1)*pageSize);
			pstmt.setInt(a++, pageSize);
	
			rs = pstmt.executeQuery();
	
			while(rs.next()){
				ch = new ChargeHistory();
	
				ch.setBid(Integer.parseInt(rs.getString("BID")));
				ch.setDriverId(driverId);
				ch.setBusinessTime(rs.getString("businessTime"));
				ch.setBusinessType(rs.getInt("businessType"));
				ch.setCustomerDeviceId(rs.getString("customerDeviceId"));
				
				if(customerList != null){
					customer = new Customer();
					customer = customerList.get(rs.getString("customerDeviceId"));
					if(customer != null){
						ch.setLat(customer.getLat()+""); // 위도 
						ch.setLng(customer.getLng()+""); // 경도
						String geoCovertAddress = ""; // 좌표주소 변환 값
						try {
							// 좌표를 주소로 변환
							geoCovertAddress= OpenApiDAO.geoConvert(customer.getLat()+"", customer.getLng()+"");
							// 주소값이 제대로 왔는지 체크
							if(geoCovertAddress != null)
								ch.setGeoCovertAddress(geoCovertAddress);
							else
								ch.setGeoCovertAddress("");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
				ch.setSource(rs.getString("source"));
				ch.setDestination(rs.getString("destination"));
				ch.setFee(Integer.parseInt(rs.getString("fee")));
				ch.setDrivingCharge(Integer.parseInt(rs.getString("drivingCharge")));
				String phone = rs.getString("customerPhone");
				if(phone == null || phone.length() == 0){
					phone = "00000000000";
				}
				ch.setCustomerPhone(phone);
				ch.setDriverId(rs.getString("driverId"));
				ch.setDriverName(rs.getString("name"));
				// 주민번호나 전화번호가 있는 부분은 모두 아래와 같이 처리를 해줘야 함.
				// 이유는 클라이언트에서 문자열처리를 하기 때문에 null이나 공백이 오면 오류가 난다고 함.
				phone = rs.getString("phone");
				if(phone == null || phone.length() == 0){
					phone = "00000000000";
				}
				ch.setPhone(phone);
				list.add(ch);
			}
	
		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
	
		return list.toArray(new ChargeHistory[0]);
	}
	/**
	 * 일별 대리운전 상세정보
	 * @param driverId
	 * @param BID
	 * @return
	 * @throws SQLException
	 */
	public ChargeHistory getChargeHistoryDetail(String driverId, String BID) throws SQLException {
		// 고객현재위치정보
		ConcurrentHashMap<String, Customer> customerList = ServerProcess.getCustomers();
		Customer customer = null;
		if(customerList != null)
			System.out.println("customerList Size: "+ customerList.size());
		
		ChargeHistory ch = null;
	
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		// null, 공백 체크
		if(driverId == null || driverId.length() == 0){
			System.out.println("driverId가 존재하지 않습니다.");
			return ch;
		}
		if(BID == null || BID.length() == 0){
			System.out.println("BID가 존재하지 않습니다.");
			return ch;
		}
		
		try {
			conn = connPool.getConnection();
			// sql 명령
			String sql = "SELECT charge_history.*, name, phone " +
					"FROM charge_history JOIN driver ON driver.driverId = charge_history.driverId " +
					"WHERE driver.drvierId = ? AND BID= ? ";

			System.out.println("sql==="+sql);
			
			pstmt = conn.prepareStatement(sql);
	
			int a = 1;
			pstmt.setString(a++, driverId);
			pstmt.setString(a++, BID);
			
			rs = pstmt.executeQuery();
			    
		    // 결제금
			while(rs.next()){
				ch = new ChargeHistory();
				// 기사정보
				ch.setBusinessTime(rs.getString("businessTime")); // 날짜 - 날짜정보, 시간정보를 잘라서 사용했다고 함.
				ch.setBid(Integer.parseInt(rs.getString("BID"))); // BID
				ch.setDriverId(rs.getString("driverId")); // 기사아이디
				ch.setDriverName(rs.getString("name")); // 기사이름
				String driverPhone = rs.getString("phone"); // 기사전화번호
				if(driverPhone == null || driverPhone.length() == 0){
					driverPhone = "00000000000";
				}
				ch.setPhone(driverPhone); // 기사전화
				ch.setBusinessType(rs.getInt("businessType")); // 결과:성공,대리취소,고객취소
				// 위치정보
				ch.setSource(rs.getString("source")); // 출발지
				ch.setDestination(rs.getString("destination")); // 목적지
				// 금액정보
				ch.setFee(Integer.parseInt(rs.getString("fee"))); // 수수료
				ch.setDrivingCharge(rs.getInt("drivingCharge")); // 부과요금
				// 아래부분은 현재 협의중이므로 나중에 개발해야 함.
				//ch.setCoupon_amount(rs.getString("amount")); // 쿠폰가격
				//ch.setCoupon_id(rs.getString("coupon_id")); // 쿠폰아이디
				// 고객정보
				ch.setCustomerName(rs.getString("customer_name")); // 고객이름
				String customerPhone = rs.getString("customerPhone"); // 고객전화번호
				if(customerPhone == null || customerPhone.length() == 0){
					customerPhone = "00000000000";
				}
				ch.setCustomerPhone(customerPhone);
				
				
				
				// 현재위치 구하기
				if(customerList != null){
					customer = new Customer();
					customer = customerList.get(rs.getString("customerDeviceId"));
					if(customer != null){
						ch.setLat(customer.getLat()+""); // 위도 
						ch.setLng(customer.getLng()+""); // 경도
						String geoCovertAddress = ""; // 좌표주소 변환 값
						try {
							// 좌표를 주소로 변환
							geoCovertAddress= OpenApiDAO.geoConvert(customer.getLat()+"", customer.getLng()+"");
							// 주소값이 제대로 왔는지 체크
							if(geoCovertAddress != null)
								ch.setGeoCovertAddress(geoCovertAddress);
							else
								ch.setGeoCovertAddress("");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
	
		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		
		return ch;
		
	}
	
	public long getCHNum(String date, String local, String result, String driverId, String name) throws SQLException {
		int count = 0;

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = connPool.getConnection();
			// sql 명령
			String sql = "SELECT count(1) " +
					"FROM charge_history JOIN driver ON driver.driverId = charge_history.driverId " +
					"WHERE businessTime like ? and source like ? and businessType like ? ";

			if (driverId != null && driverId.length() != 0) {
				sql += " and charge_history.driverId =? ";
			}
			if (name != null && name.length() != 0) {
				sql += " and driver.name = ? ";
			}

			pstmt = conn.prepareStatement(sql);

			int a = 1;
			pstmt.setString(a++, date + "%");
			pstmt.setString(a++, local + "%");
			pstmt.setString(a++, result);
			if (driverId != null && driverId.length() != 0) {
				pstmt.setString(a++, driverId);
			}			
			if(name != null && name.length() != 0) {
				pstmt.setString(a++, name);
			}

			rs = pstmt.executeQuery();

			if(rs.next()){
				count = rs.getInt(1);
			}
		} finally{
			if(rs != null)
				rs.close();
			if(pstmt !=null)
				pstmt.close();
			if(conn != null)
				connPool.returnConn(conn);

		}
		return count;
	}

	public long getCHNumPeriod(String dateA, String dateB, String local, String result, String driverId, String name) throws SQLException {
		int count = 0;

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = connPool.getConnection();
			// sql 명령
			String sql = "SELECT count(1) " +
					"FROM charge_history JOIN driver ON driver.driverId = charge_history.driverId " +
					"WHERE businessTime BETWEEN ? AND ? " +
					"and source like ? and businessType like ? ";

			if (driverId != null && driverId.length() != 0) {
				sql += " and charge_history.driverId =? ";
			}
			if (name != null && name.length() != 0) {
				sql += " and driver.name = ? ";
			}

			pstmt = conn.prepareStatement(sql);

			int a = 1;
			pstmt.setString(a++, dateA + "%");
			pstmt.setString(a++, dateB + "%");
			pstmt.setString(a++, local + "%");
			pstmt.setString(a++, result);
			if (driverId != null && driverId.length() != 0) {
				pstmt.setString(a++, driverId);
			}			
			if(name != null && name.length() != 0) {
				pstmt.setString(a++, name);
			}

			rs = pstmt.executeQuery();

			if(rs.next()){
				count = rs.getInt(1);
			}
		} finally{
			if(rs != null)
				rs.close();
			if(pstmt !=null)
				pstmt.close();
			if(conn != null)
				connPool.returnConn(conn);

		}
		return count;
	}
	
	public String testFunc(String dateA, String dateB, String local, String result) throws SQLException
	{
		List<ChargeHistoryPeriod> chpList = new ArrayList<ChargeHistoryPeriod>();

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		StringBuffer sB = new StringBuffer("START\n");

		ConcurrentHashMap<String, ChargeHistoryPeriod> CHP = new ConcurrentHashMap<String, ChargeHistoryPeriod>();

		// 날짜별로 List생성.
		DateTimeFormatter fmt = DateTimeFormat.forPattern("y-MM-dd");		// FORMATTER YYYY-MM-DD
		DateTime start = DateTime.parse(dateA,fmt);
		DateTime end = DateTime.parse(dateB,fmt);
		end = end.plusDays(1);							// END+1

		List<DateTime> dTime = new ArrayList<DateTime>();
		DateTime tmp = start;

		sB.append( "SQL:: SELECT * " +
				"FROM charge_history " +
				"WHERE businessTime BETWEEN '"+start.toString(fmt)+"%' AND '"+end.toString(fmt)+"%' "+
				"AND destination LIKE '"+local+"%' AND businessType LIKE '"+result+"%' ORDER BY BID DESC; \n\n");

		while(tmp.isBefore(end) || tmp.equals(end)) {
			dTime.add(tmp);

			ChargeHistoryPeriod tmpCHP = new ChargeHistoryPeriod();
			tmpCHP.setBusinessDate(tmp.toString(fmt));
			tmpCHP.setTotalCharge(0);
			tmpCHP.setTotalFee(0);
			int[] tmpBusinessTotal = {0,0,0};
			//            tmpCHP.setBusinessTypeCount(tmpBusinessTotal);

			// Put into Concurrent HashMap
			CHP.put(tmp.toString(fmt), tmpCHP);
			//            chpList.add(tmpCHP);
			sB.append("\nput:"+tmp.toString(fmt));

			tmp = tmp.plusDays(1);
		}

		try {
			conn = connPool.getConnection();
			/*
			 * SQL QUERY 설명
			 * charge_history의 모든 컬럼
			 * 영업시간  dateA < x < dateB 사이에 존재하고 목적지가 지정된 곳
			 */
			String sql = 	"SELECT * " +
					"FROM charge_history " +
					"WHERE businessTime BETWEEN ? AND ? " +
					"AND destination LIKE ? AND businessType LIKE ? ";

			sql += " ORDER BY BID DESC ;";
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, start.toString(fmt) + "%");		// 영업날짜
			pstmt.setString(2, end.toString(fmt) + "%");		// 영업날짜
			pstmt.setString(3, local + "%");		// 지역
			pstmt.setString(4, result);			// 대리결과 
			rs = pstmt.executeQuery();

			/*
			int cnt = 0;
			sB.append("RESULT SEARCH::\n");
			while(rs.next()){
				cnt++;
				String businessTime = rs.getString("businessTime");
				sB.append("\n"+businessTime + " TO ");
				String businessDate = DateTime.parse(businessTime, fmt).toString(fmt);
				sB.append(businessDate);
			}
			sB.append(cnt);
			 */

			sB.append("\nRESULT SEARCH::");

			while(rs.next()){
				String businessTime = rs.getString("businessTime");
				businessTime = businessTime.substring(0, 10);
				sB.append("\nsearch:"+businessTime);
				// find my CH
				ChargeHistoryPeriod chp = CHP.get(businessTime);
				chp.setTotalCharge(chp.getTotalCharge()+rs.getInt("drivingCharge"));
				chp.setTotalFee(chp.getTotalFee()+rs.getInt("fee"));
				//						
				//				int[] businessTypeCount = chp.getBusinessTypeCount();
				//				int bType = rs.getInt("businessType");
				//				businessTypeCount[bType-1]++;
				//				sB.append("\n"+bType+":"+businessTypeCount[bType-1]);
				//				businessTypeCount[rs.getInt("businessType")-1]++;
				//				chp.setBusinessTypeCount(businessTypeCount);

				// replace the CHP to a new one
				//				sB.append("\n	updated chp:"+businessTime+" : "+chp.getTotalFee() + " : "+ chp.getBusinessTypeCount()[0]+":" + chp.getBusinessTypeCount()[1]+":" + chp.getBusinessTypeCount()[2]);
				CHP.put(businessTime, chp);
			}

			sB.append("\n\nRESULT SEARCH END.\n\n");
		} catch (SQLException e){
			e.printStackTrace();
		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		} 
		return sB.toString();
	}
}
