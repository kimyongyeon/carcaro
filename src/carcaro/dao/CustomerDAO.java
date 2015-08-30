package carcaro.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import carcaro.ConnectionPool;
import carcaro.bean.ChargeHistory;
import carcaro.bean.Customer;

public class CustomerDAO {
	
	// Connection Pool
	private ConnectionPool connPool;

	// Connection 설정
	public CustomerDAO(ConnectionPool connPool) {
		this.connPool = connPool;
	}
	/**
	 * 고객리스트  : 시작일, 종료일, 이름, 고객전화, 쿠폰 중 선택해서 리스트에서 출력해야 한다.
	 * @param dateA
	 * @param dateB
	 * @param keyword
	 * @return
	 * @throws SQLException
	 */
	public Customer[] getCustomerList(int pageSize, int currentPage, String dateA, String dateB, String keyword, String gubun) throws SQLException {
		List<Customer> list = new ArrayList<Customer>();
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = connPool.getConnection();
			// sql 명령
			String sql = " SELECT driver.driverId, businessTime, customerPhone, customer_name, source, destination, phone, name, coupon_a.amount, coupon_a.coupon_id " +
						" FROM charge_history right outer join coupon_a on charge_history.customerPhone = coupon_a.customer_phone, driver " +
						" WHERE driver.driverId = charge_history.driverId " +
						" AND businessTime >= ? " +
						" AND businessTime <= ? ";
			if(gubun.equals("nameName")){ // 이름
				sql += " AND customer_name like ? ";
			}else if(gubun.equals("usrNum")){ // phone
				sql += " AND customerPhone like ? ";
			}else if(gubun.equals("couponNum")){ // 쿠폰 아이디 : couponNum
				sql += " AND coupon_id like ? ";
			}
			sql +="LIMIT ?,?";
			pstmt = conn.prepareStatement(sql);

			int a = 1;
			pstmt.setString(a++, dateA + "%");
			pstmt.setString(a++, dateB + "%");
			pstmt.setString(a++, keyword + "%");
			pstmt.setInt(a++, (currentPage - 1) * pageSize);
			pstmt.setInt(a++, pageSize);
			
			System.out.println("sql===="+sql);
			
			rs = pstmt.executeQuery();

			while (rs.next()) {
				Customer ct = new Customer();
				ct.setCustomDriverId(rs.getString("driverId"));
				ct.setBusinessTime(rs.getString("businessTime"));
				String phone = rs.getString("customerPhone");
				if(phone == null || phone.length() == 0){
					phone = "00000000000";
				}
				ct.setCustomerPhone(phone);
				ct.setCustomerName(rs.getString("customer_name"));
				ct.setSource(rs.getString("source"));
				ct.setDestination(rs.getString("destination"));
				// 주민번호나 전화번호가 있는 부분은 모두 아래와 같이 처리를 해줘야 함.
				// 이유는 클라이언트에서 문자열처리를 하기 때문에 null이나 공백이 오면 오류가 난다고 함.
				phone = rs.getString("phone");
				if(phone == null || phone.length() == 0){
					phone = "00000000000";
				}
				ct.setDriverPhone(phone);
				ct.setDriverName(rs.getString("name"));
				ct.setAmount(rs.getString("amount"));
				ct.setCoupon_id(rs.getString("coupon_id"));
				list.add(ct);
			}
		} finally{
			if(rs != null)
				rs.close();
			if(pstmt !=null)
				pstmt.close();
			if(conn != null)
				connPool.returnConn(conn);

		}
		return list.toArray(new Customer[0]);
	}
	
	public long getCustomerCount(String dateA, String dateB, String keyword, String gubun) throws SQLException {
		int count = 0;

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = connPool.getConnection();
			// sql 명령
			String sql = " SELECT count(*) " +
						" FROM charge_history right outer join coupon_a on charge_history.customerPhone = coupon_a.customer_phone, driver " +
						" WHERE driver.driverId = charge_history.driverId " +
						" AND businessTime >= ? " +
						" AND businessTime <= ? ";
			if(gubun.equals("nameName") && !keyword.equals("")){ // 이름
				sql += " AND customer_name like ? ";
			}else if(gubun.equals("usrNum") && !keyword.equals("")){ // phone
				sql += " AND customerPhone like ? ";
			}else if(gubun.equals("couponNum") && !keyword.equals("")){ // 쿠폰 아이디 : couponNum
				sql += " AND coupon_id like ? ";
			}else{
				sql += " AND customer_name like ? ";
			}

			pstmt = conn.prepareStatement(sql);

			int a = 1;
			pstmt.setString(a++, dateA + "%");
			pstmt.setString(a++, dateB + "%");
			pstmt.setString(a++, keyword + "%");
			
			System.out.println("sql===="+sql);
			
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
}
