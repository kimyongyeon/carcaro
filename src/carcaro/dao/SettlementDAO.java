package carcaro.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import carcaro.ConnectionPool;
import carcaro.bean.Settlement;

public class SettlementDAO {
	
	ConnectionPool connPool;

	public SettlementDAO(ConnectionPool connPool) {
		this.connPool = connPool;
	}
	
	
	/**
	 * 주문번호로 기사아이디 가져오기 
	 * @throws SQLException 
	 */
	public String getDriverIdbyOid(String oid) throws SQLException{
		
		String driverId=null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = connPool.getConnection();
			
			String sql = " SELECT driverId FROM pre_settlement WHERE OID=?; ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, oid);
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				driverId = rs.getString("driverID");
			}
		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
			
		return driverId;
	}
	
	/**
	 * 결제가 시작 되면, 주문번호를 생성한다. 
	 * @throws SQLException 
	 */
	public int startSettlement(String driverId) throws SQLException{
		
		int oid = -1;
		Connection conn = null;
		PreparedStatement pstmt = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			conn = connPool.getConnection();

			// sql 명령
			String sql = " INSERT INTO pre_settlement (driverId, requestTime) VALUES (?, NOW()); ";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, driverId);
			pstmt.execute();
			
			stmt = conn.createStatement();
			//rs = stmt.executeQuery("select LAST_INSERT_ID() ");
			rs = stmt.executeQuery("SELECT * FROM `pre_settlement` order by requestTime desc limit 0,1 ");
			if(rs.next()){
				oid = rs.getInt(1);
			}
			
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		
		return oid;
	}
	
	/**
	 * 앱 결과값을 기록한다
	 */
	public boolean appLogSettlement(String oid, String auth_date, String status,
			String type, String tid, String fn_cd1, String fn_cd2, String fn_nm, String amount, String uname,
			String rmsg1, String rmsg2, String noti, String auth_no) throws SQLException{
		
		boolean isOk = false;
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try {
			conn = connPool.getConnection();
			String sql = " INSERT INTO applog_settlement (OID, AUTH_DT, STATUS, TYPE, TID, FN_CD1, FN_CD2, " +
					"FN_NM, AMT, UNAME, REMESG1, REMESG2, NOTI, AUTH_NO) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, oid);
			pstmt.setString(2, auth_date);
			pstmt.setString(3, status);
			pstmt.setString(4, type);
			pstmt.setString(5, tid);
			pstmt.setString(6, fn_cd1);
			pstmt.setString(7, fn_cd2);
			pstmt.setString(8, fn_nm);
			pstmt.setString(9, amount);
			pstmt.setString(10, uname);
			pstmt.setString(11, rmsg1);
			pstmt.setString(12, rmsg2);
			pstmt.setString(13, noti);
			pstmt.setString(14, auth_no);
			
			int ret = pstmt.executeUpdate();
			if(ret == 1)
				isOk = true;
			
		} finally {
			if(pstmt!=null)
				pstmt.close();
			if(conn!=null)
				connPool.returnConn(conn);
		}
		
		return isOk;
	}
	
	/**
	 * 결제가 완료되면, 결제 결과를 기록한다.
	 * WEB, APP 공통
	 */
	public boolean resultSettlement(Settlement settle) throws SQLException{
		
		boolean isOk = false;
		Connection conn = null;
		PreparedStatement pstmt = null;

		try {
			conn = connPool.getConnection();

//			String sql = " update settlement set AUTH_DT=?, STATUS=?, TYPE=?, TID=?, FN_CD1=?, FN_CD2=?, " +
//					"FN_NM=?, AMT=?, UNAME=?, REMESG1=?, REMESG2=?, NOTI=?, AUTH_NO=?, driverId=? where OID = ?; ";
			String sql = " INSERT INTO settlement (OID, TID, amount, settleTime, method)" +
					" VALUES (?, ?, ?, ?, ?); ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, settle.getOid());
			pstmt.setString(2, settle.getTid());
			pstmt.setString(3, settle.getAmount());
			pstmt.setString(4, settle.getSettleTime());
			pstmt.setString(5, settle.getMethod());
			
			int ret = pstmt.executeUpdate();
			if(ret == 1){
				isOk = true;
			}
			
		} finally {
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		return isOk;
	}
	
	public Settlement[] getSettlementList(String driverId, int currentPage, int pageSize) throws SQLException {
		
		ArrayList<Settlement> list = new ArrayList<Settlement>();
		Settlement settle;
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = connPool.getConnection();

			// sql 명령
			String sql = " SELECT settlement.* FROM settlement JOIN pre_settlement ON pre_settlement.OID = settlement.OID" +
					" WHERE pre_settlement.driverId=? ORDER BY OID DESC LIMIT ?,?;";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, driverId);
			pstmt.setInt(2, (currentPage-1)*pageSize);
			pstmt.setInt(3, pageSize);
						
			rs = pstmt.executeQuery();
			while(rs.next()){
				settle = new Settlement();
				
				settle.setOid(rs.getString("OID"));
				settle.setTid(rs.getString("TID"));
				settle.setAmount(rs.getString("amount"));
				settle.setSettleTime(rs.getString("settleTime"));
				settle.setMethod(rs.getString("method"));
				
				list.add(settle);
			}
			
		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		
		return list.toArray(new Settlement[0]);
	}
	public long getSettlementCount(String driverId) throws SQLException {
		int count = 0;
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			conn = connPool.getConnection();

			// sql 명령
			String sql = " SELECT count(1) FROM settlement JOIN pre_settlement ON pre_settlement.OID = settlement.OID" +
					" WHERE pre_settlement.driverId=? ;";
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, driverId);
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
	/**
	 * 충전시간에 대한 전체 충전금
	 * @param date
	 * @return
	 * @throws SQLException
	 */
	public int getChargeSumByDate(String date) throws SQLException{
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int total = 0;
		try {
			conn = connPool.getConnection();

			// sql 명령
			String sql = " SELECT amount FROM settlement WHERE settleTime LIKE ?";
			pstmt = conn.prepareStatement(sql);
			
			String dateExcludingTime = date.substring(0,10);
			
			pstmt.setString(1, dateExcludingTime+"%");
			rs = pstmt.executeQuery();
			
			
			
			while(rs.next()){
				total += rs.getInt("amount");
			}
		} finally{
			if(rs != null)
				rs.close();
			if(pstmt !=null)
				pstmt.close();
			if(conn != null)
				connPool.returnConn(conn);
		}
		
		return total;
	}
	/**
	 * 정산금 관리 입력
	 * 일일에 대한 입력이다.
	 * @param date : 오늘날짜
	 * @param charge : 정산금
	 * @return
	 * @throws SQLException
	 */
	public boolean setAdjustment(String date, String source, long adjustment) throws SQLException{
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		long totalFee = 0;
		try {
			conn = connPool.getConnection();

			// sql 명령 : 수수료의 받은날짜의 전체 수수료를 구한다.
			// 날짜는 YYYY-MM-DD 까지만 와야 한다. 이유는 일자 
			String sql = " SELECT sum(fee) as totalFee FROM charge_history WHERE businessTime LIKE ? AND source LIKE ? ";
			pstmt = conn.prepareStatement(sql);
			String dateExcludingTime = date.substring(0,10);
			System.out.println("dateExcludingTime: "+dateExcludingTime);
			
			pstmt.setString(1, dateExcludingTime+"%");
			pstmt.setString(2, source+"%");
			rs = pstmt.executeQuery();
			System.out.println("sum(fee): "+sql);
			// 전체 수수료 구하기
			if(rs.next()){
				totalFee = rs.getInt("totalFee");
			}
			
			// sql 명령 : 수수료의 받은날짜의 전체 수수료를 구한다.
			// driverBID을 모두 가지고 안다.
			sql = " SELECT BID FROM charge_history WHERE businessTime LIKE ? AND source LIKE ? ";
			pstmt = conn.prepareStatement(sql);
			dateExcludingTime = date.substring(0,10);
			
			pstmt.setString(1, dateExcludingTime+"%");
			pstmt.setString(2, source+"%");
			rs = pstmt.executeQuery();
			System.out.println("BID: "+sql);
			
			String driverBID = "";
			// 일일 전체 driver 유일한 식별자 구하기
			while(rs.next()){
				driverBID += rs.getString("BID") + ","; // driver ID를 콤바로 저장한다.
			}
			// 마지막 콤바는 빼줌.
			if(driverBID.length() > 0)
				driverBID = driverBID.substring(0, driverBID.length()-1);
			System.out.println("totalFee: ["+ totalFee + "] driverBID: ["+driverBID + "]");
			
			// 정산금 테이블에 저장한다.
			// 잔액 : charge - totalFee
			long balance = adjustment - totalFee; // 잔액
			//bID - 정산아이디
			//adjustment - 정산금 
			//balance - 잔액
			//totalFee - 일리 수수료 합계
			//driverBID - 드라이버 식별자
			//businessTime_date - 정산일자
			//reg_date - 오늘일자
			//edit_date - 수정일자
			sql = " INSERT INTO adjustment (adjustment, balance, totalFee, driverBID, businessTime_date, reg_date, source) VALUES " +
					"(?, ?, ?, ?, ?, NOW(), ?); ";
			pstmt = conn.prepareStatement(sql);
			int a=1;
			pstmt.setString(a++, String.valueOf(adjustment)); // 정산금
			pstmt.setString(a++, String.valueOf(balance));    // 잔액 
			pstmt.setString(a++, String.valueOf(totalFee));   // 해당지역 수수료
			pstmt.setString(a++, driverBID);                  // 해당지역 드라이버 전체
			pstmt.setString(a++, dateExcludingTime);          // 정산날짜
			pstmt.setString(a++, source);                     // 지역
			pstmt.execute();
			
			return true;
			
		} finally{
			if(rs != null)
				rs.close();
			if(pstmt !=null)
				pstmt.close();
			if(conn != null)
				connPool.returnConn(conn);
		}
	}
	/**
	 * 각 지사별 수수료 구하기
	 * 프로세스 : 
	 * 1. 해당일에 대해서 정산테이블에서 정산금, BID(driver식별자) 코드를 찾는다.
	 * 2. 일 대한 식별자를 통해서 charge_history에서 전체 수수료를 찾는다.
	 * @param source : 지역(예:서울, 경기, 전남 등등...)
	 * @param date : 월을 받는다.
	 * @return
	 * @throws SQLException
	 */
	public boolean getJisaAdjustment(String source, String date) throws SQLException{
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			conn = connPool.getConnection();
			// 정산금 테이블에서 정산금, 잔액, 총수수료, driverBID를 가져온다.
			// 해당 월에 대한 전체 정산 데이터를 추출한다.
			String sql = " SELECT adjustment, balance, totalFee, driverBID, businessTime_date FROM adjustment WHERE businessTime_date  LIKE ? ";
			pstmt = conn.prepareStatement(sql);
			String dateExcludingTime = date.substring(0,8); // 월까지만 조회한다.
			System.out.println("dateExcludingTime: "+dateExcludingTime);
			int a=1;
			pstmt.setString(a++, dateExcludingTime+"%");
			rs = pstmt.executeQuery();
			System.out.println("adjustment: "+sql);
			
			while(rs.next()){
				 
			}
			
			return true;
			
		} finally{
			if(rs != null)
				rs.close();
			if(pstmt !=null)
				pstmt.close();
			if(conn != null)
				connPool.returnConn(conn);
		}
	}

	
	
}
