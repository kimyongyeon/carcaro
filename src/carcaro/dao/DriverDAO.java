package carcaro.dao;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import carcaro.ConnectionPool;
import carcaro.MainServlet;
import carcaro.bean.Driver;
import carcaro.bean.SettlementHist;

public class DriverDAO {

	// Connection Pool
	private ConnectionPool connPool;

	public static final int LONGIN_FAIL = 0;
	public static final int LONGIN_OK = 1;
	public static final int IS_NOT_CERTIFIED = 2; // 승인 안됨
	public static final int IS_CERTIFIED = 21; // 승인 안됨
	// public static final int NO_DRIVER = 3; //일치 기사 없음

	public static final int REGIST_OK = 4; // 회원 가입 성공.
	public static final int EXIST_ID = 5; // 회원 가입 실패. 이미 존재하는 아이디.
	public static final int EXIST_RESIDENT = 22; // 주민번호 중복
	public static final int EXIST_WAIT = 6; // 회원 가입 대기.

	// Connection 설정
	public DriverDAO(ConnectionPool connPool) {
		this.connPool = connPool;
	}
	
	/** 
	 * 관리자 페이지에서 대리기사 등록시 사용하는 메소드
	 * @param driver
	 * @return
	 * @throws SQLException
	 */
	public boolean insertDriver(Driver driver) throws SQLException {

		// DB 연결 관련 변수
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			// Connection pool 연결
			conn = connPool.getConnection();
			// 중복 아이디 체크
			boolean exist = existId(conn, driver.getDriverId());
			if (exist == true) {
				System.out.println("중복아이디==>"+exist);
				return false;
			}
			// 중복 주민번호 체크
			boolean resNoExist = existResNo(conn, driver.getResidentNo());
			if (resNoExist == true) {
				System.out.println("중복주민번호==>"+exist);
				return false;
			}

			// 아이디가 존재하지 않을 경우에만 시행.
			// sql 명령
			StringBuffer sql = new StringBuffer();
			// 아이디, 패스워드, 이름, 주민번호, 전화번호, 주소, 이메일, 면허종류(licenseType), 면허번호(licenseNo), 보험(authorizationName),
			// 보험번호(authorizationNo), 보험만료일(Assurance_complete_date)
			sql.append(" INSERT INTO driver (driverId, passwd, name, residentNo, phone, address, email, "
					+ "licenseType, licenseNo, authorizationName, authorizationNo, Assurance_complete_date) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?); ");

			stmt = conn.prepareStatement(sql.toString());

			stmt.setString(1, driver.getDriverId()); // 아이디
			stmt.setString(2, driver.getPasswd()); // 패스워드
			stmt.setString(3, driver.getName()); // 이름
			stmt.setString(4, driver.getResidentNo()); // 주민번호
			stmt.setString(5, driver.getPhone()); // 폰번호
			stmt.setString(6, driver.getAddress()); // 주소
			stmt.setString(7, driver.getEmail()); // 이메일
			stmt.setInt(8, driver.getLicenseType()); // 면허종류
			stmt.setString(9, driver.getLiceseNo()); // 면허번호
			stmt.setString(10, driver.getAuthorizationName()); // 보험이름
			stmt.setString(11, driver.getAuthorizationNo()); // 보험번호
			stmt.setString(12, driver.getAssurance_complete_date()); // 보험만료일
			
			stmt.execute();

			driver.setState(Driver.ISNOTCERTIFIED);
			// 가입시에 미승인 상태로 List에 저장한다.
			MainServlet.main.addDriverList(driver);
			return true;

		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
			
			System.out.println("대리기사 등록이 성공하였습니다.");			
		}
	}

	// 대리기사 회원가입
	public synchronized int registDriver(Driver driver) throws SQLException {

		// DB 연결 관련 변수
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		int ret = EXIST_WAIT;
		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			boolean exist = existId(conn, driver.getDriverId());
			if (exist) {
				ret = EXIST_ID;
			}

			boolean resNoExist = existResNo(conn, driver.getResidentNo());
			if (resNoExist) {
				ret = EXIST_RESIDENT;
			}

			// 아이디가 존재하지 않을 경우에만 시행.
			if (!(ret == EXIST_ID || ret == EXIST_RESIDENT)) {
				// sql 명령
				StringBuffer sql = new StringBuffer();
				sql.append(" INSERT INTO driver (driverId, passwd, name, phone, residentNo, authorizationNo, "
						+ "email, licenseType, licenseAuto, career, company, address, agreeReceive, requestDistance) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?); ");

				stmt = conn.prepareStatement(sql.toString());

				stmt.setString(1, driver.getDriverId());
				stmt.setString(2, driver.getPasswd());
				stmt.setString(3, driver.getName());
				stmt.setString(4, driver.getPhoneNum());
				stmt.setString(5, driver.getResidentNo());
				stmt.setString(6, driver.getAuthorizationNo());
				stmt.setString(7, driver.getEmail());
				stmt.setInt(8, driver.getLicenseType());
				stmt.setBoolean(9, driver.isLicenseAuto());
				stmt.setInt(10, driver.getCareer());
				stmt.setString(11, driver.getCompany());
				stmt.setString(12, driver.getAddress());
				stmt.setInt(13, driver.getAgreeReceive());
				stmt.setString(14, driver.getRequestDistance()); // 대리 요청거리
				stmt.execute();

				driver.setState(Driver.ISNOTCERTIFIED);
				// 가입시에 미승인 상태로 List에 저장한다.
				MainServlet.main.addDriverList(driver);

				ret = REGIST_OK;
			}
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		return ret;
	}

	// 고객 -> 대리기사 추천하기
	public void recommendDriverId(String cDevId, String driverId, int value)
			throws SQLException {
		// DB 연결 관련 변수
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql
			// TODO 나중에 customerPhone 이름 변경할 것CDevNum
			.append(" INSERT INTO recommend (customerDevId, driverId, recommendTime, value) VALUES (?, ?, NOW(), ?); ");

			stmt = conn.prepareStatement(sql.toString());

			stmt.setString(1, cDevId);
			stmt.setString(2, driverId);
			stmt.setInt(3, value); // 1 =good / -1 = bad

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

	// 대리기사 로그인처리
	public int LoginDriver(String input_id, String input_pw)
			throws SQLException {

		int ret = LONGIN_FAIL;
		// DB 연결 관련 변수
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql.append("select level from driver where driverId=? and passwd=? ");

			stmt = conn.prepareStatement(sql.toString());

			stmt.setString(1, input_id);
			stmt.setString(2, input_pw);

			rs = stmt.executeQuery();

			if (rs.next()) {
				int level = rs.getInt("level");
				if (level < 0) {
					ret = IS_NOT_CERTIFIED;
				} else {
					ret = LONGIN_OK;
				}
			}

		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		return ret;
	}

	// 대리기사 위치업데이트
	public boolean sendLike(String driverId) throws SQLException {
		// DB 연결 관련 변수
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean isOk = false;

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE driver SET likeScore=likeScore+1 WHERE driverId=?;");
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setString(1, driverId);

			int ret = pstmt.executeUpdate();
			if (ret == 1)
				isOk = true;

		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		return isOk;
	}

	public boolean sendDislike(String driverId) throws SQLException {
		// TODO Auto-generated method stub
		// DB 연결 관련 변수
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean isOk = false;

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE driver SET likeScore=likeScore-10 WHERE driverId=?;");
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setString(1, driverId);

			int ret = pstmt.executeUpdate();
			if (ret == 1)
				isOk = true;

		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		return isOk;
	}

	// // 대리기사 위치업데이트
	/*public void updateDriverLoca(String gpsX, String gpsY, String gpsTime,
			String driverId) throws SQLException {
		// DB 연결 관련 변수
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql.append(" UPDATE Driver set gpsX = ? , gpsY = ? , gpsTime = ? where driverId = ?");

			stmt = conn.prepareStatement(sql.toString());

			stmt.setString(1, gpsX);
			stmt.setString(2, gpsY);
			stmt.setString(3, gpsTime);
			stmt.setString(4, driverId);

			stmt.execute();

		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
	}*/
	/**
	 * 기사 현재 정보 주기적인 갱신
	 * @param lat
	 * @param lng
	 * @param driverId
	 * @throws SQLException
	 */
	/*public boolean updateDriverLoca(String lat, String lng, String driverId) throws SQLException {
		// DB 연결 관련 변수
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			// Connection pool 연결
			conn = connPool.getConnection();
			
			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql.append(" UPDATE driver set lat = ? , lng = ? where driverId = ?");
			
			stmt = conn.prepareStatement(sql.toString());
			
			stmt.setString(1, lat);
			stmt.setString(2, lng);
			stmt.setString(3, driverId);
			
			stmt.execute();
			
			return true;
			
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
	}*/

	public void updateDriverDevice(String driverId, String devId, String OS)
			throws SQLException {
		// DB 연결 관련 변수
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE driver set deviceId =?, OS =? where driverId = ?");

			stmt = conn.prepareStatement(sql.toString());

			int i = 1;
			stmt.setString(i++, devId);
			stmt.setString(i++, OS);
			stmt.setString(i++, driverId);

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

	/**
	 * 대리기사 사진 경로 저장..
	 * 
	 * @param driverId
	 * @param picture
	 * @param option
	 * @throws SQLException
	 */
	public void updateDriverPic(String driverId, String picture, String option)
			throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql.append(" UPDATE driver SET ");
			if ("license".equals(option))
				sql.append(" licensePic = ? ");
			else
				sql.append(" picture = ? ");
			sql.append(" WHERE driverId = ? ");

			stmt = conn.prepareStatement(sql.toString());

			stmt.setString(1, picture);
			stmt.setString(2, driverId);

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

	// 대리기사 정보수정
	public void updateDriverInfo(String name, String phone, String ad,
			String picture, boolean isWorking, int workingHourTo,
			int workingHourFrom, String driverId, String devId, String OS, String requestDistance)
			throws SQLException {
		// DB 연결 관련 변수
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE driver set name = ? , phone = ? , advertisement = ? , "
					+ "isWorking = ? , workingHourTo = ? , workingHourFrom = ?, deviceId =?, OS =?, requestDistance = ? ");
			if (picture != null) {
				sql.append(", picture = ? ");
			}
			sql.append(" where driverId = ?");

			stmt = conn.prepareStatement(sql.toString());

			int i = 1;
			stmt.setString(i++, name);
			stmt.setString(i++, phone);
			stmt.setString(i++, ad);
			stmt.setBoolean(i++, isWorking);
			stmt.setInt(i++, workingHourTo);
			stmt.setInt(i++, workingHourFrom);
			stmt.setString(i++, devId);
			stmt.setString(i++, OS);
			stmt.setString(i++, requestDistance);

			if (picture != null) {
				stmt.setString(i++, picture);
			}
			stmt.setString(i++, driverId);

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

	// 드라이버 목록 불러오기
	public List<Driver> selectAll() throws SQLException {
		List<Driver> list = new ArrayList<Driver>();

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		PreparedStatement pstmt = null;

		try {
			conn = connPool.getConnection();
			stmt = conn.createStatement();

			String sql = "select * from driver";

			pstmt = conn.prepareStatement(sql);

			rs = pstmt.executeQuery();

			Driver d;
			while (rs.next()) {
				String driverId = rs.getString("driverId");
				String name = rs.getString("name");
				String ad = rs.getString("advertisement");
				// 주민번호나 전화번호가 있는 부분은 모두 아래와 같이 처리를 해줘야 함.
				// 이유는 클라이언트에서 문자열처리를 하기 때문에 null이나 공백이 오면 오류가 난다고 함.
				String phone = rs.getString("phone");
				if(phone == null || phone.length() == 0){
					phone = "00000000000";
				}
				String devId = rs.getString("deviceId");
				String picture = rs.getString("picture");
				String OS = rs.getString("OS"); // iOS Implementation
				// TODO iOS implementation not complete... Android is default...
				if (OS == null || OS.length() == 0)
					OS = Driver.OS_ANDROID;
				// TODO deviceToken==(64) -> iOS
				// if (devId.length() == 64) OS = Driver.OS_IOS;
				int likeScore = rs.getInt("likeScore");
				int level = rs.getInt("level");
				d = new Driver(driverId, name, phone, picture, devId,
						likeScore, ad);
				d.setOS(OS); // set OS
				if (level < 0) {
					d.setState(Driver.ISNOTCERTIFIED);
				} else {
					d.setState(Driver.WAITING);
				}
				list.add(d);
			}

		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		return list;

	}

	/**
	 * 대리기사 승인 시 DB에 들어가 있는 정보들을 기반으로 List를 생성해 준다.
	 * 
	 * @param driverIdList
	 * @return
	 * @throws SQLException
	 */
	public List<Driver> updateDriverList(String[] driverIdList)
			throws SQLException {
		List<Driver> list = new ArrayList<Driver>();

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		PreparedStatement pstmt = null;

		try {
			conn = connPool.getConnection();
			stmt = conn.createStatement();
			String q4in = generateQsForIn(driverIdList.length);
			String sql = "select * from driver where driverId in(" + q4in + ")";
			pstmt = conn.prepareStatement(sql);

			int i = 1;
			for (String driverid : driverIdList) {
				pstmt.setString(i++, driverid);
			}

			rs = pstmt.executeQuery();

			Driver d;
			while (rs.next()) {
				String driverId = rs.getString("driverId");
				String name = rs.getString("name");
				String phone = rs.getString("phone");
				if(phone == null || phone.length() == 0){
					phone = "00000000000";
				}
				//phone = rs.getString("phone");
				String devId = rs.getString("deviceId");
				String picture = rs.getString("picture");
				int likeScore = rs.getInt("likeScore");
				String ad = rs.getString("advertisement");

				d = new Driver(driverId, name, phone, picture, devId,
						likeScore, ad);
				list.add(d);
			}

		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		return list;

	}

	private String generateQsForIn(int numQs) {
		String items = "";
		for (int i = 0; i < numQs; i++) {
			items += "?";
			if (i < numQs - 1) {
				items += ", ";
			}
		}
		return items;
	}

	public Driver getDriverInfo(String driverId) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Driver driver = new Driver();
		
		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT * FROM driver WHERE driverId = ? ");
			stmt = conn.prepareStatement(sql.toString());

			stmt.setString(1, driverId);
			
			System.out.println("sql===" + sql.toString());
			
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				driver.setDevId(rs.getString("driverId"));
				driver.setPasswd(rs.getString("passwd"));
				driver.setName(rs.getString("name"));
				String phone = rs.getString("phone");
				if(phone == null || phone.length() == 0){
					phone = "00000000000";
				}
				driver.setPhone(phone);
				String residentNo = rs.getString("residentNo");
				if(residentNo == null || residentNo.length() == 0){
					phone = "0000000000000";
				}
				driver.setResidentNo(rs.getString("residentNo"));
				driver.setAuthorizationNo(rs.getString("authorizationNo"));
				driver.setEmail(rs.getString("email"));
				driver.setLicenseType(Integer.parseInt(rs.getString("licenseType")));
				driver.setCareer(Integer.parseInt(rs.getString("career")));
				driver.setCompany(rs.getString("company"));
				driver.setAddress(rs.getString("address"));
				driver.setAgreeReceive(Integer.parseInt(rs.getString("agreeReceive")));
				driver.setOS(rs.getString("OS"));
				driver.setAdvertisement(rs.getString("advertisement"));
				driver.setChargeSum(rs.getInt("chargeSum")); // 충전금
				driver.setLikeScore(rs.getInt("likeScore"));
				driver.setPicture(rs.getString("picture"));
				driver.setStarttime(Integer.parseInt(rs.getString("workingHourFrom")));
				driver.setEndtime(Integer.parseInt(rs.getString("workingHourTo")));
				driver.setLevel(rs.getInt("level"));
				// 신규 추가 항목 - KYY
				driver.setAuthorizationName(rs.getString("authorizationName")); // 보험회사
				driver.setAssurance_complete_date(rs.getString("Assurance_complete_date")+""); // 보험완료일
				driver.setRequestDistance(rs.getString("requestDistance")+""); // 대리요청 거리
			}
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		return driver;

	}

	// // 모든 driver의 p
	// public List<String> getAllPhone() throws SQLException {
	//
	// String allphone = null;
	// List<String> list = new ArrayList<String>();
	//
	// Connection conn = null;
	// Statement stmt = null;
	// ResultSet rs = null;
	// PreparedStatement pstmt = null;
	//
	// try {
	// conn = connPool.getConnection();
	// stmt = conn.createStatement();
	//
	// String sql = "select phone from Driver";
	//
	// pstmt = conn.prepareStatement(sql);
	//
	// rs = pstmt.executeQuery();
	//
	// while (rs.next()) {
	// allphone = rs.getString("phone");
	// list.add(allphone);
	// }
	//
	// } finally {
	// if (rs != null)
	// rs.close();
	// if (stmt != null)
	// stmt.close();
	// if (conn != null)
	// connPool.returnConn(conn);
	// }
	// return list;
	//
	// }

	// // 이름으로 기사Id가져오기
	// public List<String> getIdByName(String name) throws SQLException {
	//
	// String id = null;
	// List<String> list = new ArrayList<String>();
	//
	// Connection conn = null;
	// ResultSet rs = null;
	// PreparedStatement pstmt = null;
	//
	// try {
	// conn = connPool.getConnection();
	//
	// String sql = "select driverId from driver where name=?";
	//
	// pstmt = conn.prepareStatement(sql);
	// pstmt.setString(1, name);
	// rs = pstmt.executeQuery();
	//
	// while (rs.next()) {
	// id = rs.getString("driverId");
	// list.add(id);
	// }
	//
	// } finally {
	// if (rs != null)
	// rs.close();
	// if (pstmt != null)
	// pstmt.close();
	// if (conn != null)
	// connPool.returnConn(conn);
	// }
	// return list;
	//
	// }

	// 대리기사 위치업데이트
	public void test_updateLocation(double minX, double minY, double maxX,
			double maxY, String gpsTime) throws SQLException {
		// DB 연결 관련 변수
		Connection conn = null;
		Statement stmt1 = null;
		Statement stmt2 = null;

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			stmt1 = conn.createStatement();
			stmt2 = conn.createStatement();
			conn.setAutoCommit(false);
			ResultSet rs = stmt1.executeQuery("select * from Driver");

			double gpsX, gpsY;
			ArrayList<String> drivers = new ArrayList<String>();
			while (rs.next()) {

				String driverId = rs.getString("driverId");
				drivers.add(driverId);
			}
			rs.close();

			for (String driverId : drivers) {

				gpsX = minX + (Math.random() * ((maxX - minX)));
				gpsY = minY + (Math.random() * ((maxY - minY)));

				String sql = "update Driver set gpsX = '" + gpsX + "', gpsY ='"
						+ gpsY + "' where driverId = '" + driverId + "'";

				stmt2.execute(sql);
				System.out.println(sql);
			}
			conn.commit();

		} finally {
			if (stmt1 != null)
				stmt1.close();
			stmt2.close();
			if (conn != null) {
				conn.setAutoCommit(true);
				connPool.returnConn(conn);
			}
		}
	}

	public static void main(String[] args) throws SQLException {

		ConnectionPool connPool = ConnectionPool.getInstance();
		DriverDAO dao = new DriverDAO(connPool);
		dao.test_updateLocation(37.478868, 126.892855, 37.623673, 127.129057,
				"");

		ConnectionPool.destory();

	}

	public boolean updateChargeSum(int charge, String driverId)
			throws SQLException {
		// DB 연결 관련 변수
		boolean isOk = false;
		Connection conn = null;
		PreparedStatement stmt = null;

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE driver set chargeSum = ? where driverId = ?");
			stmt = conn.prepareStatement(sql.toString());
			stmt.setInt(1, charge);
			stmt.setString(2, driverId);
			int ret = stmt.executeUpdate();

			if (ret == 1) {
				isOk = true;
			}
		} finally {
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		return isOk;
	}
	
	// 대리기사 list
	/**
	 * 대리기사 ID관리 : 
	 * 검색조건 : 승인, 미승인
	 * 검색조건2 : 이름, 아이디, 전화번호
	 * 검색조건3 : 키워드
	 * level - 0 : 승인
	 *         -1 : 미승인
	 * @param pageSize
	 * @param currentPage
	 * @param level
	 * @param gubun
	 * @param keyword
	 * @return
	 * @throws SQLException
	 */
	public Driver[] driverList(int pageSize, int currentPage, String level, String gubun, String keyword)
			throws SQLException {

		// DB 연결 관련 변수
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		List<Driver> list = new ArrayList<Driver>();
		try {
			// Connection pool 연결
			conn = connPool.getConnection();
			// sql 명령
			String sql = "SELECT * FROM driver WHERE ";
			
			if(!level.equals("0")) // 미승인
				sql += " level < ? ";
			else // 승인
				sql += " level = ? ";
			
			if(gubun.equals("0")){ // 이름
				sql += " AND name LIKE ? ";	
			}else if(gubun.equals("1")){ // 아이디
				sql += " AND driverId LIKE ? ";
			}else if(gubun.equals("2")){ // 전화번호
				sql += " AND phone LIKE ? ";
			}
			sql += " LIMIT ?,?";
			
			System.out.println("sql=="+sql);
			
			pstmt = conn.prepareStatement(sql);
			int a=1;
			pstmt.setString(a++, "0");
			pstmt.setString(a++, keyword + "%");
			pstmt.setInt(a++, (currentPage - 1) * pageSize);
			pstmt.setInt(a++, pageSize);

			rs = pstmt.executeQuery();

			Driver driver;
			while (rs.next()) {
				String phone = rs.getString("phone");
				if(phone == null || phone.length() == 0){
					phone = "00000000000";
				}
				String residentNo = rs.getString("residentNo");
				if(residentNo == null || residentNo.length() == 0){
					residentNo = "0000000000000";
				}
				driver = new Driver(rs.getString("driverId"), null,
						rs.getString("name"), phone,
						residentNo,
						rs.getString("authorizationNo"), rs.getString("email"),
						Integer.parseInt(rs.getString("licenseType")),
						Boolean.parseBoolean(rs.getString("licenseAuto")),
						Integer.parseInt(rs.getString("career")),
						rs.getString("company"), rs.getString("address"),
						Integer.parseInt(rs.getString("agreeReceive")),
						rs.getString("OS"));
				// TODO insert Password, insert status
				driver.setState(rs.getInt("level"));
				list.add(driver);
			}

		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		return list.toArray(new Driver[0]);
	}
	/**
	 * 대기리사관리 총 레코드 수
	 * @param level
	 * @param gubun
	 * @param keyword
	 * @return
	 * @throws SQLException
	 */
	public long getNoCertiCount(String level, String gubun, String keyword) throws SQLException {
		int count = 0;

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			// Connection pool 연결
			conn = connPool.getConnection();
			// sql 명령
			String sql = "SELECT count(*) FROM driver WHERE ";
			
			if(!level.equals("0")) // 미승인
				sql += " level < ? ";
			else // 승인
				sql += " level = ? ";
			
			if(gubun.equals("0")){ // 이름
				sql += " AND name LIKE ? ";	
			}else if(gubun.equals("1")){ // 아이디
				sql += " AND driverId LIKE ? ";
			}else if(gubun.equals("2")){ // 전화번호
				sql += " AND phone LIKE ? ";
			}
			
			System.out.println("sql=="+sql);
			
			pstmt = conn.prepareStatement(sql);
			int a=1;
			pstmt.setString(a++, "0");
			pstmt.setString(a++, keyword + "%");

			rs = pstmt.executeQuery();

			if (rs.next()) {
				count = rs.getInt(1);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				connPool.returnConn(conn);

		}
		return count;
	}

	public void certifing(String[] dlist) throws SQLException {

		// DB 연결 관련 변수
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			// Connection pool 연결
			conn = connPool.getConnection();
			String sql = "update driver set level=0 where driverId=?";

			for (int i = 0; i < dlist.length; i++) {

				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, dlist[i]);
				pstmt.executeUpdate();
				pstmt.close();
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

	public long checkId(String id) throws SQLException {
		int count = 0;
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			conn = connPool.getConnection();
			stmt = conn.createStatement();

			String sql = "select count(1) from driver where driverId=\"" + id
					+ "\"";
			rs = stmt.executeQuery(sql);

			if (rs.next()) {
				count = rs.getInt(1);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		return count;
	}

	public boolean existId(Connection conn, String id) throws SQLException {
		boolean ret = true;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = conn.createStatement();
			String sql = "select count(1) from driver where driverId=\"" + id
					+ "\"";
			rs = stmt.executeQuery(sql);

			if (rs.next()) {
				ret = rs.getInt(1) > 0 ? true : false;
			} else {
				ret = false;
			}
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
		}
		return ret;
	}

	public boolean existResNo(Connection conn, String resNo)
			throws SQLException {
		boolean ret = true;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = conn.createStatement();
			String sql = "select count(1) from driver where residentNo=\""
					+ resNo + "\"";
			rs = stmt.executeQuery(sql);

			if (rs.next()) {
				ret = rs.getInt(1) > 0 ? true : false;
			} else {
				ret = false;
			}
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
		}
		return ret;
	}

	public boolean checkPw(String driverId, String pw) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = connPool.getConnection();
			String sql = "SELECT passwd FROM driver WHERE driverId=?";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, driverId);

			rs = pstmt.executeQuery();

			if (rs.next()) {
				if (rs.getString("passwd").equals(pw))
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
		return false;
	}

	/**
	 * 대리기사 삭제
	 * @param driver
	 * @return
	 * @throws SQLException
	 */
	public boolean deleteDriver(Driver driver) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			// Connection pool 연결
			conn = connPool.getConnection();
			StringBuffer sql = new StringBuffer();
			sql.append("DELETE FROM driver ");
			sql.append(" WHERE driverId=?");

			pstmt = conn.prepareStatement(sql.toString());
			int i = 1;
			pstmt.setString(i++, driver.getDriverId());
			pstmt.executeUpdate();
			pstmt.close();
			// 삭제 성공시
			return true;

		} catch(Exception e){
			// 삭제 실패시
			System.out.println("삭제코드: " + e.getMessage());
			return false;
			
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
	 * 충전금 누적 수정
	 * @param driver
	 * @return
	 * @throws SQLException
	 */
	public boolean updateChargeSum(Driver driver) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		// 필수항목 체크 시작
		String driverId = driver.getDriverId(); // 드라이버 아이디
		if(driverId == null || driverId.length() == 0){
			System.out.println("driverId가 존재하지 않습니다.");
			return false;
		}
		// 필수항목 체크 끝

		try {
			// Connection pool 연결
			conn = connPool.getConnection();
			// UPDATE driver SET chargeSum = chargeSum+1000 where driverId='0328'
			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE driver SET");
			sql.append(" chargeSum=chargeSum+? ");
			sql.append(" WHERE driverId=?");

			pstmt = conn.prepareStatement(sql.toString());
			int i = 1;
			pstmt.setInt(i++, driver.getChargeSum());
			pstmt.setString(i++, driver.getDriverId());
			pstmt.executeUpdate();
			pstmt.close();
			
			return true;

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
	 * 대리기사 레벨업
	 * @param driver
	 * @return
	 * @throws SQLException
	 */
	public boolean updateLevel(Driver driver) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		// 필수항목 체크 시작
		String driverId = driver.getDriverId(); // 드라이버 아이디
		if(driverId == null || driverId.length() == 0){
			System.out.println("driverId가 존재하지 않습니다.");
			return false;
		}
		// 필수항목 체크 끝

		try {
			// Connection pool 연결
			conn = connPool.getConnection();
			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE driver SET");
			sql.append(" level=? ");
			sql.append(" WHERE driverId=?");

			pstmt = conn.prepareStatement(sql.toString());
			int i = 1;
			pstmt.setInt(i++, driver.getLevel());
			pstmt.setString(i++, driver.getDriverId());
			pstmt.executeUpdate();
			pstmt.close();
			
			return true;

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
	 * 대리기사 수정
	 * 사진은 사진업로드 함수를 이용하여 업로드 시키고 해당 파일명만 디비에 저장한다.
	 * 충전금 같은경우 충전 버튼 이벤트가 이루어질때 저장한다. 즉, 여기서는 충전금을 업데이트 하지 않는다.
	 * @param driver
	 * @throws SQLException
	 */
	public boolean updateDriver(Driver driver) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		// 필수항목 체크 시작
		String driverId = driver.getDriverId(); // 드라이버 아이디
		if(driverId == null || driverId.length() == 0){
			System.out.println("driverId가 존재하지 않습니다.");
			return false;
		}
		String pwd = driver.getPasswd(); // 패스워드
		if(pwd == null || pwd.length() == 0){
			System.out.println("pwd가 존재하지 않습니다.");
			return false;
		}
		String name = driver.getName(); // 이름
		if(name == null || name.length() == 0){
			System.out.println("name이 존재하지 않습니다.");
			return false;
		}
		String phone = driver.getPhone(); // 전화번호
		if(phone == null || phone.length() == 0){
			System.out.println("phone이 존재하지 않습니다.");
			return false;
		}
		String residentNo = driver.getResidentNo(); // 주민번호
		if(residentNo == null || residentNo.length() == 0){
			System.out.println("residentNo이 존재하지 않습니다.");
			return false;
		}
		String authorizationNo = driver.getAuthorizationNo(); // 보험번호
		if(authorizationNo == null || authorizationNo.length() == 0){
			System.out.println("authorizationNo가 존재하지 않습니다.");
			return false;
		}
		String email = driver.getResidentNo(); // 이메일
		if(email == null || email.length() == 0){
			System.out.println("email이 존재하지 않습니다.");
			return false;
		}
		// 필수항목 체크 끝

		try {
			// Connection pool 연결
			conn = connPool.getConnection();
			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE driver SET");
			if (driver.getPasswd() != "")
				sql.append(" passwd=?,");
			sql.append(" phone=?, authorizationNo=?, email=?, licenseType=?, licenseno=?,");
			sql.append(" name=?, residentNo=?, address=?, assurance_complete_date=?");
			sql.append(" WHERE driverId=?");

			pstmt = conn.prepareStatement(sql.toString());
			int i = 1;
			pstmt.setString(i++, driver.getPasswd()); // 비밀번호
			pstmt.setString(i++, driver.getPhone()); // 폰
			pstmt.setString(i++, driver.getAuthorizationNo()); // 보험번호
			pstmt.setString(i++, driver.getEmail()); // 이메일
			pstmt.setInt(i++, driver.getLicenseType()); // 면허증 종류
			pstmt.setString(i++, driver.getLiceseNo()); // 면허증 번호
			pstmt.setString(i++, driver.getName()); // 이름
			pstmt.setString(i++, driver.getResidentNo()); // 주민번호
			pstmt.setString(i++, driver.getAddress()); // 주소
			pstmt.setString(i++, driver.getAssurance_complete_date()); // 보험만료일
			pstmt.setString(i++, driver.getDriverId());
			pstmt.executeUpdate();
			pstmt.close();
			
			return true;

		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
	}

	public String driver_id_pw(String name, String sn, String type)
			throws SQLException {
		String rt = new String();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			// Connection pool 연결
			conn = connPool.getConnection();
			StringBuffer sql = new StringBuffer();

			sql.append("SELECT ");
			if ("ID".equals(type))
				sql.append(" driverId ");
			else if ("PW".equals(type))
				sql.append(" passwd ");
			else
				sql.append(" * ");
			sql.append(" FROM carcaro.driver WHERE name = ? AND residentNo = ?");

			pstmt = conn.prepareStatement(sql.toString());

			pstmt.setString(1, name);
			pstmt.setString(2, sn);

			rs = pstmt.executeQuery();
			if (rs.next()) {
				rt = rs.getString(1);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		return rt;
	}

	public JSONArray getChargeFeeListDriver(String driverId)
			throws SQLException {
		JSONArray rt = new JSONArray();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			// Connection pool 연결
			conn = connPool.getConnection();
			StringBuffer sql = new StringBuffer();

			sql.append(" SELECT * FROM carcaro.charge_history WHERE driverId = ? ORDER BY BID DESC ;");

			pstmt = conn.prepareStatement(sql.toString());

			pstmt.setString(1, driverId);

			rs = pstmt.executeQuery();
			while (rs.next()) {
				JSONObject arrayObject = new JSONObject();
				arrayObject.put("fee", rs.getInt("fee"));
				arrayObject.put("businessTime", rs.getString("businessTime"));
				arrayObject.put("source", rs.getString("source"));
				arrayObject.put("destination", rs.getString("destination"));
				arrayObject.put("drivingCharge", rs.getString("DrivingCharge"));
				rt.add(arrayObject);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		return rt;
	}
	
	
	// 대리기사 충전내역 정보 list : settlement
	public SettlementHist[] getSettlementList(int pageSize, int currentPage, String gubun, String keyword)
			throws SQLException {

		// DB 연결 관련 변수
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		List<SettlementHist> list = new ArrayList<SettlementHist>();
		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql 명령
			String sql = " SELECT requestTime, settleTime, name, a.driverid, phone, method, amount " +
					"      FROM driver a, pre_settlement b, settlement c " +
					"      WHERE a.driverid = b.driverid " +
					"      AND b.oid = c.oid";
			
			if(gubun.equals("0")){
				sql += "      AND name like ? ";// 이름
			} else if(gubun.equals("1")){
				sql += "      AND a.driverid like ? ";// 드라이버아이디
			} else if(gubun.equals("2")){
				sql += "      AND phone like ? ";// 전화번호
			}
			sql += "	   LIMIT ?,?";
			
			System.out.print("sql=="+sql);
			int a = 1;
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(a++, keyword + "%"); // 검색어
			pstmt.setInt(a++, (currentPage - 1) * pageSize); // 페이지
			pstmt.setInt(a++, pageSize); // 한페이지 보여줄 레코드 수

			rs = pstmt.executeQuery();

			while (rs.next()) {
				SettlementHist slh = new SettlementHist();
				slh.setRequestTime(rs.getString("requestTime"));
				slh.setSettleTime(rs.getString("settleTime"));
				slh.setName(rs.getString("name"));
				slh.setDriverid(rs.getString("driverid"));
				String rltPhone = rs.getString("phone");
				if(rltPhone == null || rltPhone.length() == 0){
					rltPhone = "00000000000";
				}
				slh.setPhone(rltPhone);
				slh.setMethod(rs.getString("method"));
				slh.setAmount(rs.getString("amount"));
				list.add(slh);
			}

		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		return list.toArray(new SettlementHist[0]);
	}
	
	public long getSettlementCount(String gubun, String keyword) throws SQLException {
		int count = 0;
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			conn = connPool.getConnection();

			// sql 명령
			String sql = " SELECT count(*) " +
					"      FROM driver a, pre_settlement b, settlement c " +
					"      WHERE a.driverid = b.driverid " +
					"      AND b.oid = c.oid";
			if(gubun.equals("0")){
				sql += "      AND name like ? ";// 이름
			} else if(gubun.equals("1")){
				sql += "      AND a.driverid like ? ";// 드라이버아이디
			} else if(gubun.equals("2")){
				sql += "      AND phone like ? ";// 전화번호
			}
					
			pstmt = conn.prepareStatement(sql);
			int a = 1;
			pstmt.setString(a++, keyword + "%"); // 검색어
			
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
