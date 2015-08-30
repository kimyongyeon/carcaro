package carcaro.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServlet;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import carcaro.ConnectionPool;
import carcaro.bean.Coupon;
import carcaro.bean.Customer;

 


/**
 * Coupon
 */
public class CouponDAO {
	
	public static final int COUPON_NEW = 0;
	public static final int COUPON_GIV = 1;
	public static final int COUPON_USE = 2;
	public static final int COUPON_DEL = 3;
	public static final int COUPON_MOD = 4;
	
	public ConnectionPool connPool;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CouponDAO(ConnectionPool connPool) {
    	this.connPool = connPool;
        // TODO Auto-generated constructor stub
    }

	
		/** 
	 * INSERT Coupons
	 **/
	//public int register_coupon(String coupon_id, String cPhone, String cName, String amount) throws SQLException{
	public int register_coupon(String coupon_id, String amount) throws SQLException{
		// DB 연결 관련 변수
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int isOK = 0;

		//if ( coupon_id.length()<8 || cPhone.length()==0 || cName.length() == 0 || amount.length()==0) {
		if ( coupon_id.length()<8 ) {
			isOK = 0; // empty value returns 1
			// Nothing is initialized so we can return without cleanup.
			return isOK;
		}
		try {
			// Connection pool 연결
			conn = connPool.getConnection();
	
			//  sql query 
			StringBuffer sql = new StringBuffer();
			//sql.append(" INSERT INTO coupon_a ( coupon_id, amount, customer_phone, customer_name, reg_date, " +
			//		"last_owner, mod_date, status) VALUES (?,?, ?, ?, NOW(), ? ,NOW() ,?);");
			sql.append(" INSERT INTO coupon_a ( coupon_id, amount, reg_date, mod_date, status )" +
						" VALUES (?, ?, NOW(), NOW(), ?);");
			stmt = conn.prepareStatement(sql.toString());

			stmt.setString(1, coupon_id);
			stmt.setString(2, amount);
			//stmt.setString(3, cPhone);
			//stmt.setString(4, cName);
			//stmt.setString(5, cPhone);
			stmt.setString(3, "NUSED");
		
			stmt.execute();
			
			// successfully inserted
			isOK = 1;
		}catch (SQLException e1) {
			e1.printStackTrace();
		}finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		return isOK;
	}
	
	public String register_coupon_bulk(BufferedReader bufRdr) throws SQLException, IOException{
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		JSONArray json = new JSONArray();
		String sqlStr = null;
		String ret = null;
		
		// PROCESS CSV DATA
		String line = null;
		 
		
		/**
		 * BOF Marker 및 EOF Marker 참고사항. 
		 * 열의 개수가 증가하게되면 콤마의 개수도 늘려줘야 합니다.
		 * StringTokenize는 null 값을 인식하지 않아서 
		 * 빈칸이 있을시 콤마사이에 공백 ' '을 추가해 StringTokenizer가 공백을 인식하게 한 후
		 * str.trim()을 이용해 잘라내어 실제 입력합니다.
		 * 
		 *  열의 개수가 4개면 BOF Marker는 '$ , , ," 이며
		 *  열의 개수가 8개면 BOF Marker는 '$ , , , , , , ,"입니다.
		 *  EOF Marker도 동일하게 취급합니다.
		 */
		//read each line of text file
		boolean write = false;
		while((line = bufRdr.readLine()) != null)
		{
			line = line.replaceAll(",", " ,");						// Give a space to allow the StringTokenizer recognize the null string value
			StringTokenizer st = new StringTokenizer(line,",");	
			JSONObject js = new JSONObject();
			int i = 0;
			if ( "$ , , ,".equals(line)) write = false;				/** EOF Marker	**/
			while (st.hasMoreTokens())
			{
				String str = st.nextToken();
				str = str.trim();									// Trim the spaces!
				if ( write ){
					i++;
					//get next token and store it in the array
					js.put(i, str);									
				}
			}
			if ( write ) json.add(js);
			if ( "* , , ,".equals(line)) write = true;				/** BOF Marker	**/
			
		}
		bufRdr.close();	//close the file
		
//		return json.toString();
		
		try {
			// Connection pool 연결
			conn = connPool.getConnection();
	
			//  sql query 
			StringBuffer sql = new StringBuffer();
			sql.append(" INSERT INTO coupon_a ( coupon_id, customer_phone, customer_name, amount, reg_date, status )" +
						" VALUES ");
			
			
			for ( int i=0; i < json.size(); i++){
				JSONObject js = json.getJSONObject(i);
				String c1 = js.getString("1");				// cID
				String c2 = js.getString("2");				// cPhone
				String c3 = js.getString("3");				// cName
				String c4 = js.getString("4");				// amount
				
				sql.append("('"+c1+"','"+c2+"','"+c3+"',"+c4+",NOW(), 'NUSED')");				
				if ( i < json.size()-1) sql.append(",");			// give commas except the final one
			}
			sql.append(" ON DUPLICATE KEY UPDATE customer_phone=VALUES(customer_phone), customer_name=VALUES(customer_name), amount=VALUES(amount)");		// Update duplicate coupon_id
			sql.append(";");									// semicolon at end
		
			sqlStr = sql.toString();							// debug용
			stmt = conn.prepareStatement(sqlStr);
			stmt.execute();
		}catch (SQLException e1) {
			e1.printStackTrace();
			ret = e1.getMessage();
		}finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		return ret;
		
	}
	
	
	
	/** 
	 * Update Coupon Information
	 **/
	public void update_coupon(String coupon_id, String customer_name, String customer_phone) throws SQLException{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		
		try {
			// Connection pool 연결
			conn = connPool.getConnection();
	
			//  sql query 
			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE coupon_a SET customer_phone=?, last_owner=?, mod_date=NOW(), customer_name=? WHERE coupon_id=?;");
			stmt = conn.prepareStatement(sql.toString());

			stmt.setString(1, customer_phone);
			stmt.setString(2, customer_phone);
			stmt.setString(3, customer_name);
			stmt.setString(4, coupon_id);
		
			stmt.execute();
						
		}catch (SQLException e1) {
			e1.printStackTrace();
		}finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		
	}
	
	/** 
	 * Check Coupon_ID for duplicates and return the count
	 **/
	public int check_coupon_duplicate(String coupon_id) throws SQLException {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		int ret = 0;
		
		try {
			// Establish Connection
			conn = connPool.getConnection();
			
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT count(*) AS rc FROM carcaro.coupon_a WHERE coupon_id = ?;");
			stmt = conn.prepareStatement(sql.toString());
			
			stmt.setString(1, coupon_id);
			
			// The result set will be an integer
			rs = stmt.executeQuery();
			
			// Get the count(*) value
			rs.next();
			ret = rs.getInt("rc");
		}finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		
		return ret;
	}
	
	/** 
	 * Get Coupons
	 **/
	public JSONArray get_coupon(String column, String value) throws SQLException {
	//public int get_coupon(String column, String value) throws SQLException {
		
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		// A JSONObject to return to the MainServlet
		
		
		JSONArray jsonArr = new JSONArray();
		
		try {
			// Establish Connection
			conn = connPool.getConnection();
			
			// sql
			StringBuffer sql = new StringBuffer();
			if ( column.equals("coupon_id")){
				sql.append("SELECT * FROM carcaro.coupon_a WHERE coupon_id LIKE ? ORDER BY reg_date DESC;");
				if (value.length() < 8) value = value.concat("%");
			}else if (column.equals("customer_phone")){
				sql.append("SELECT * FROM carcaro.coupon_a WHERE customer_phone = ? ORDER BY reg_date DESC;");
			}else if (column.equals("customer_name")){
				sql.append("SELECT * FROM carcaro.coupon_a WHERE customer_name = ? ORDER BY reg_date DESC;");
			}
			stmt = conn.prepareStatement(sql.toString());
			stmt.setString(1, value);
			rs = stmt.executeQuery();
			
			// get results
			while ( rs.next() ) {
                String coupon_id = rs.getString("coupon_id");
                String cName = rs.getString("customer_name");
                String cPhone = rs.getString("customer_phone");
                String regDate = rs.getString("reg_date");
                String lastOwner = rs.getString("last_owner");
                String modDate = rs.getString("mod_date");
                String status = rs.getString("status");
                int amount = rs.getInt("amount");
                
                JSONObject json = new JSONObject();
        		json.put("coupon_id", coupon_id);		// 쿠폰번호
        		json.put("amount", amount);				// 가격
        		json.put("customer_name", cName);		// 고객 이름
        		json.put("customer_phone", cPhone);		// 고객번호
        		json.put("register_date", regDate);		// 등록일
        		json.put("last_owner", lastOwner);		// 바로이전 소유지
        		json.put("mod_date", modDate);			// 상태 변경일
        		json.put("status", status);				// 현상태
        		
        		jsonArr.add(json);
        		json=null;
            }
			
		}catch (SQLException e1) {
			e1.printStackTrace();
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		return jsonArr;//.size();
	}
	/**
	 * 쿠폰 리스트 출력
	 * @param gubun
	 * @param column
	 * @param value
	 * @return
	 * @throws SQLException
	 */
	public Coupon[] getCouponList(int pageSize, int currentPage, String gubun, String column, String value) throws SQLException {
		
		List<Coupon> list = new ArrayList<Coupon>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = connPool.getConnection();
			// sql
			StringBuffer sql = new StringBuffer();
			// 쿠폰번호, 고객전화, 고객이름
			if ( column.equals("coupon_id")){
				sql.append("SELECT * FROM carcaro.coupon_a WHERE coupon_id LIKE ? ");
			}else if (column.equals("customer_phone")){
				sql.append("SELECT * FROM carcaro.coupon_a WHERE customer_phone LIKE ? ");
			}else if (column.equals("customer_name")){
				sql.append("SELECT * FROM carcaro.coupon_a WHERE customer_name LIKE ? ");
			}else{
				sql.append("SELECT * FROM carcaro.coupon_a WHERE coupon_id LIKE ? ");
			}
			// 전체,사용,미사용 : 전체일 경우 조건이 붙질 않는다.
			if(gubun.equals("used")){
				sql.append("AND status = USED ");	
			}else if(gubun.equals("nused")){
				sql.append("AND status = NUSED ");
			}
			sql.append(" ORDER BY reg_date DESC LIMIT ?,?");
			int a=1;
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setString(a++, value + "%");
			pstmt.setInt(a++, (currentPage - 1) * pageSize);
			pstmt.setInt(a++, pageSize);
			
			System.out.println("sql===" + sql.toString());
			
			rs = pstmt.executeQuery();
			
			// get results
			while ( rs.next() ) {
				/*System.out.println("coupon_id==="+rs.getString("coupon_id"));
				System.out.println("customer_name==="+rs.getString("customer_name"));
				System.out.println("customer_phone==="+rs.getString("customer_phone"));
				System.out.println("reg_date==="+rs.getString("reg_date"));
				System.out.println("last_owner==="+rs.getString("last_owner"));
				System.out.println("mod_date==="+rs.getString("mod_date"));
				System.out.println("status==="+rs.getString("status"));
				System.out.println("amount==="+rs.getString("amount"));*/
				
				Coupon cp = new Coupon();
	            String coupon_id = rs.getString("coupon_id");
	            String cName = rs.getString("customer_name");
	            String cPhone = rs.getString("customer_phone");
	            if(cPhone == null || cPhone.length() == 0){
	            	cPhone = "00000000000";
	            }
	            String regDate = rs.getString("reg_date");
	            String lastOwner = rs.getString("last_owner");
	            String modDate = rs.getString("mod_date");
	            String status = rs.getString("status");
	            int amount = rs.getInt("amount");
	            
	            cp.setCoupon_id(coupon_id);
	            cp.setcName(cName);
	            cp.setcPhone(cPhone);
	            cp.setRegDate(regDate);
	            cp.setLastOwner(lastOwner);
	            cp.setModDate(modDate);
	            cp.setStatus(status);
	            cp.setAmount(amount);
	            
	            list.add(cp);
			}
		} finally{
			if(rs != null)
				rs.close();
			if(pstmt !=null)
				pstmt.close();
			if(conn != null)
				connPool.returnConn(conn);

		}
		return list.toArray(new Coupon[0]);
	}
	/**
	 * 쿠폰리스트 총 개수
	 * @param gubun
	 * @param column
	 * @param value
	 * @return
	 * @throws SQLException
	 */
	public long getCouponCount(String gubun, String column, String value) throws SQLException {
		int count = 0;

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = connPool.getConnection();
			// sql
			StringBuffer sql = new StringBuffer();
			// 쿠폰번호, 고객전화, 고객이름
			if ( column.equals("coupon_id")){
				sql.append("SELECT count(*) FROM carcaro.coupon_a WHERE coupon_id LIKE ? ");
			}else if (column.equals("customer_phone")){
				sql.append("SELECT count(*) FROM carcaro.coupon_a WHERE customer_phone LIKE ? ");
			}else if (column.equals("customer_name")){
				sql.append("SELECT count(*) FROM carcaro.coupon_a WHERE customer_name LIKE ? ");
			}else{
				sql.append("SELECT count(*) FROM carcaro.coupon_a WHERE coupon_id LIKE ? ");
			}
			// 전체,사용,미사용 : 전체일 경우 조건이 붙질 않는다.
			if(gubun.equals("used")){
				sql.append("AND status = USED ORDER BY reg_date DESC;");	
			}else if(gubun.equals("nused")){
				sql.append("AND status = NUSED ORDER BY reg_date DESC;");
			}else{
				sql.append("ORDER BY reg_date DESC;");
			}
			
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setString(1, value + "%");
			
			System.out.println("sql===" + sql.toString());
			
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
	 * Get Coupon amount
	 **/
	public int get_coupon_amount(String coupon_id) throws SQLException {
			
			
			Connection conn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			int amount = 0;
			
			try {
				// Establish Connection
				conn = connPool.getConnection();
				
				// sql
				StringBuffer sql = new StringBuffer();
				sql.append("SELECT amount FROM carcaro.coupon_a WHERE coupon_id = ? ;");
				stmt = conn.prepareStatement(sql.toString());
				stmt.setString(1, coupon_id);
				
				rs = stmt.executeQuery();
				
				// get results
				if ( rs.next() ) {
	                amount = rs.getInt("amount");
	            }
				
			}catch (SQLException e1) {
				e1.printStackTrace();
			}catch (Exception e){
				e.printStackTrace();
			}finally {
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (conn != null)
					connPool.returnConn(conn);
			}
			return amount;
		}
	
	/** 
	 * Change coupons's owner to a new person
	 **/
	public void gift_coupon(String coupon_id, String from, String to) throws SQLException{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		
		try {
			// Establish Connection
			conn = connPool.getConnection();
			
			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE coupon_a SET customer_phone=?, last_owner=?, mod_date=NOW() WHERE coupon_id LIKE ? AND customer_phone LIKE ?");
			
			stmt = conn.prepareStatement(sql.toString());
			
			stmt.setString(1, to);			// new owner (SET)
			stmt.setString(2, from);		// set last owner (SET)
			
			stmt.setString(3, coupon_id);	// find current coupon to change owner (WHERE)
			stmt.setString(4, from);		// last owner (WHERE)
			
			// The result set will be an integer
			stmt.execute();
		}finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
	}
	
	
	
	public int check_coupon_used(String coupon_id) throws SQLException {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		int ret = 0;
		
		try {
			// Establish Connection
			conn = connPool.getConnection();
			
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT count(*) AS rc FROM carcaro.coupon_a WHERE coupon_id = ? AND status='USED';");
			stmt = conn.prepareStatement(sql.toString());
			
			stmt.setString(1, coupon_id);
			
			// The result set will be an integer
			rs = stmt.executeQuery();
			
			// Get the count(*) value
			rs.next();
			ret = rs.getInt("rc");
		}finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		
		return ret;
	}
	/**
	 * 쿠폰사용
	 */
	public void use_coupon(String coupon_id, String driver_id, String customer_phone) throws SQLException{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		
		try {
			// Establish Connection
			conn = connPool.getConnection();
			
			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE coupon_a SET customer_phone=?, last_owner=?, mod_date=NOW(), status='USED' WHERE coupon_id=?");
		
			stmt = conn.prepareStatement(sql.toString());
			stmt.setString(1, customer_phone);
			stmt.setString(2, driver_id);
			stmt.setString(3, coupon_id);
			
			
			// The result set will be an integer
			stmt.execute();
		}finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
	}
	


	/** 
	 * Get a List of coupons (will print a web table)
	 **/
	
	public StringBuffer get_coupon_list(String fetch_type) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		// A StringBuffer to return to the MainServlet
		StringBuffer str = new StringBuffer();
		
		
		
		//JSONArray jsonArr = new JSONArray();
		
		try {
			// Establish Connection
			conn = connPool.getConnection();
			
			// sql
			StringBuffer sql = new StringBuffer();
			if ( fetch_type.equals("ALL")){
				sql.append("SELECT * FROM carcaro.coupon_a ORDER BY reg_date DESC;");
			}else if ( fetch_type.equals("USED") || fetch_type.equals("NUSED")){
				// fetch "USED" and "NUSED" coupons
				sql.append("SELECT * FROM carcaro.coupon_a WHERE status = ? ORDER BY reg_date DESC;");
			}else {
				sql.append("SELECT * FROM carcaro.coupon_a ORDER BY reg_date DESC;");
			}
			stmt = conn.prepareStatement(sql.toString());
			
			if ( !fetch_type.equals("ALL")){
				stmt.setString(1, fetch_type);
			}
			
			rs = stmt.executeQuery();
			
			str.append("<head>");
			str.append("<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>");
			str.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />");
			str.append("</head>");
			
			str.append("<table class='res'>");
			str.append("<thead>");
			str.append("<tr bgcolor='#cccccc'><td>쿠폰번호</td><td>가격</td><td>고객명</td><td>고객전화</td>" +
					"<td>등록시간</td><td>바로이전 소유자</td><td>수정시간</td><td>현재상태</td></tr>");
			str.append("</thead>");
			while(rs.next()){
				String coupon_id = rs.getString("coupon_id");
				String amount = rs.getString("amount");
	            String cName = rs.getString("customer_name");
	            String cPhone = rs.getString("customer_phone");
	            
	            String regDate = rs.getString("reg_date");
	            String lastOwner = rs.getString("last_owner");
	            String modDate = rs.getString("mod_date");
	            String status = rs.getString("status");
	            
	            
	            str.append("<tr>");
	            str.append("<td>" + coupon_id + "</td>");
	            str.append("<td>" + amount + "</td>");
	            str.append("<td>" + cName + "</td>");
	            str.append("<td>" + cPhone + "</td>");
	            str.append("<td>" + regDate + "</td>");
	            str.append("<td>" + lastOwner + "</td>");
	            str.append("<td>" + modDate + "</td>");
	            str.append("<td>" + status + "</td>");
	            str.append("</tr>");
	            
	            
			}
			str.append("</table>");
			
		}catch (SQLException e1) {
			e1.printStackTrace();
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		//return Array;
		return str;
	}
	
	
	/** 
	 * Delete a coupon
	 **/
	public int delete_coupon(String coupon_id) throws SQLException{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		
		try {
			// Establish Connection
			conn = connPool.getConnection();
			
			StringBuffer sql = new StringBuffer();
			sql.append("DELETE FROM coupon_a WHERE coupon_id = ?");
			
			stmt = conn.prepareStatement(sql.toString());
			stmt.setString(1, coupon_id);
			
			
			stmt.execute();
		}finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		return 0;
	}
	
	
	
	/** 
	 * Coupon Tracking System
	 **/
	public void update_coupon_track(String coupon_id, int stat, String owner) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String str = new String();
		if ( stat == COUPON_NEW ){
			str = "NEW";			// New Coupons has been registered
		}else if ( stat == COUPON_GIV ){
			str = "GIV";			// Gifted Coupon
		}else if ( stat == COUPON_USE){
			str = "USE";			// Used Coupon
		}else if ( stat == COUPON_DEL){
			str = "DEL";			// Deleted Coupon
		}else if ( stat == COUPON_MOD){
			str = "MOD";
		}else {
			str = "IDK"; 			// Cannot track. (unidentified modification)
		}
		
		try {
			// Establish Connection
			conn = connPool.getConnection();
			
			StringBuffer sql = new StringBuffer();
			sql.append("INSERT INTO coupon_track (coupon_id, owner, status, mod_date) VALUES (?, ?, ?, NOW());");
			
			stmt = conn.prepareStatement(sql.toString());
			stmt.setString(1, coupon_id);
			stmt.setString(2, owner);			// FINAL OWNER
			stmt.setString(3, str);
			
			stmt.execute();
			
		}finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
	
	}
	
	
	public int settlement_coupon(String coupon_id, int oid) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int isOK = 0;
		try {
			// Connection pool 연결
			conn = connPool.getConnection();
	
			//  sql query 
			StringBuffer sql = new StringBuffer();
			sql.append(" INSERT INTO settlement (OID, TID, amount, settleTime, method)" +
					" VALUES (?, ?, ?, NOW(), ?); ");
			stmt = conn.prepareStatement(sql.toString());

			int amount = get_coupon_amount(coupon_id);
			stmt.setInt(1, oid);
			stmt.setString(2, "CPM_"+coupon_id+"_"+amount);
			stmt.setInt(3, amount);
			stmt.setString(4, "COUPON");
		
			stmt.execute();
			
			// successfully inserted.
			isOK = 1;
		}catch (SQLException e1) {
			e1.printStackTrace();
		}finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		return isOK;
	}
	
	
	public JSONArray get_settlement_list(String driver_id) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		JSONArray json  = new JSONArray();

		try {
			conn = connPool.getConnection();

			// sql 명령
			String sql = " SELECT settlement.* FROM settlement JOIN pre_settlement ON pre_settlement.OID = settlement.OID" +
					" WHERE pre_settlement.driverId=? ORDER BY OID DESC;";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, driver_id);
						
			rs = pstmt.executeQuery();
			while(rs.next()){
				JSONObject js = new JSONObject();
				
				js.put("OID",rs.getString("OID"));
				js.put("TID", rs.getString("TID"));	
				js.put("amount", rs.getString("amount"));
				js.put("settleTime",rs.getString("settleTime"));
				js.put("method", rs.getString("method"));
				
				
				json.add(js);
				
				js=null;
			}
			
		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		
		return json;



	}
	
	/**
	 * 쿠폰 아이디 존재여부 체크
	 * @param coupon_id
	 * @return
	 * @throws SQLException
	 */
	private boolean couponIdfind(String coupon_id) throws SQLException{
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = connPool.getConnection();

			// sql 명령
			String sql = " SELECT count(*) AS couponid FROM coupon_a where coupon_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, coupon_id);
						
			rs = pstmt.executeQuery();
			
			int result = 0;
			if(rs.next()){
				result = rs.getInt("couponid");
			}
			
			if(result > 0){ // 쿠폰이 존재하는 경우
				return false;	
			}else{ // 쿠폰이 존재 하지 않은경우
				return true;
			}
			
		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
	}
	
	/**
	 * 쿠폰아이디, 쿠폰가격을 등록한다.
	 * coupon_id(쿠폰아이디), amount(가격), reg_date(쿠폰생성일자), status(미사용:신규사용이므로)
	 * @param coupon_id
	 * @param amount
	 * @return
	 * @throws SQLException
	 */
	private boolean couponInsert(String coupon_id, int amount) throws SQLException{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		boolean result = false;

		try {
			conn = connPool.getConnection();
			StringBuffer sql = new StringBuffer();
			sql.append(" INSERT INTO coupon_a ( coupon_id, amount, reg_date, mod_date, status )" +
					" VALUES (?, ?, NOW(), NOW(), ?);");
			stmt = conn.prepareStatement(sql.toString());
	
			stmt.setString(1, coupon_id);
			stmt.setInt(2, amount);
			stmt.setString(3, "NUSED");
			stmt.execute();
			result = true;
			return result;
			
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
	}
	
	/**
	 * 랜덤 쿠폰 아이디 생성
	 * @param count : 쿠폰생성 갯수
	 * @param lot : 가격
	 * @return
	 * @throws SQLException
	 */
	public boolean couponMgrGen(int count, int lot) throws SQLException{
		// 신규 랜덤 로직
		Random rnd =new Random();
		ArrayList<String> couponList = new ArrayList<String>();
		
		int totalCount = count;// 쿠폰 갯수
		int totalLength = 8; // 8자리 셋팅
		int price = lot;
		
		// 생성할 갯수만큼 돌림
		int k=0;
		for(int j=0; j<totalCount; j++){ // 쿠폰 갯수 셋팅
			k = j;
			StringBuffer str =new StringBuffer(); // 하나의 쿠폰번호를 담을 공간
			for(int i=0;i<totalLength;i++){ // 8자리수 셋팅
			    if(rnd.nextBoolean()){
			    	str.append(String.valueOf((char)((int)(rnd.nextInt(26))+97)).toUpperCase());
			    }else{
			    	str.append((rnd.nextInt(10))); 
			    }
			}
			
			// 기존의 쿠폰ID가 존재하는지 DB에서 찾는다.
			// result 값이 false이면 j=k;
			boolean result = couponIdfind(str.toString().trim());
			
			if(result == true){
				// 기존의 쿠폰ID가 존재하지 않을겨우 coupon_a로 INSERT한다.
				// 필요한 컬럼 정의가 필요하다.coupon_id(쿠폰아이디), amount(가격), reg_date(쿠폰생성일자), status(미사용:신규사용이므로)
				boolean insertResult = couponInsert(str.toString().trim(), price);
				if(insertResult == true){
					// 랜덤으로 생성된 쿠폰을 리스트에 담는다.
					System.out.println("쿠폰 아이디 등록이 정상 처리 되었습니다.");
					// 쿠폰 트래킹 등록한다.
					update_coupon_track(str.toString().trim(), CouponDAO.COUPON_NEW, "reg_by_admin");
					couponList.add(str.toString());	
				}else{
					System.out.println("쿠폰 아이디 등록중 알수 없는 오류가 발생하였습니다.");
				}
			}else{
				j = k;
			}
		}
		
		return true;	
	}
	/**
	 * 쿠폰 이전 
	 * @param coupon_id : 쿠폰아이디
	 * @param phone : 고객전화번호(양수자 전화번호)
	 * @return boolean(true, false)
	 * @throws SQLException
	 */
	public boolean couponMgrTransfer(String coupon_id, String phone) throws SQLException{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			// Connection pool 연결
			conn = connPool.getConnection();
	
			//  sql query 
			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE coupon_a SET customer_phone=?, mod_date=NOW() WHERE coupon_id=?;");
			stmt = conn.prepareStatement(sql.toString());

			stmt.setString(1, phone);
			stmt.setString(2, coupon_id);
		
			stmt.execute();
			
			return true;
						
		}catch (SQLException e1) {
			e1.printStackTrace();
			return false;
		}finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
	}
}

