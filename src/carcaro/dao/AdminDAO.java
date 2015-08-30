package carcaro.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import carcaro.ConnectionPool;
import carcaro.bean.Admin;
import carcaro.bean.Customer;
import carcaro.bean.Driver;

public class AdminDAO {

	public ConnectionPool connPool;

	// Login Stub
	public static final int LOGIN_FAIL = 0;
	public static final int LOGIN_OK = 1;

	// Access Level Stub
	public static final int ACCESS_SUPER = 1; // 본사
	public static final int ACCESS_BRANCH = 4; // 지사
	public static final int ACCESS_AGENCY = 8; // 대리점
	public static final int ACCESS_NONE = 99;

	// private Util util;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AdminDAO(ConnectionPool connPool) {
		this.connPool = connPool;
		// TODO Auto-generated constructor stub
	}

	/**
	 * 관리자 로그인 컨펌
	 * 
	 * @param input_id
	 * @param input_pw
	 * @return
	 * @throws SQLException
	 */
	public int LoginAdmin(String input_id, String input_pw) throws SQLException {

		int ret = LOGIN_FAIL;
		// DB 연결 관련 변수
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT count(*) AS rc FROM usr_admin WHERE id=? and pw=md5(?) ");

			stmt = conn.prepareStatement(sql.toString());

			stmt.setString(1, input_id);
			stmt.setString(2, input_pw);

			rs = stmt.executeQuery();

			if (rs.next()) {
				int level = rs.getInt("rc");
				if (level > 0) {
					ret = LOGIN_OK;
				} else {
					ret = LOGIN_FAIL;
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

	/**
	 * 주어진 관리자의 접근 권한을 돌려준다.
	 * 
	 * @param input_id
	 * @return
	 * @throws SQLException
	 */
	public int accessLevel(String input_id) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int ret = ACCESS_NONE;
		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT accessLevel FROM usr_admin WHERE id=?");

			stmt = conn.prepareStatement(sql.toString());

			stmt.setString(1, input_id);

			rs = stmt.executeQuery();

			if (rs.next()) {
				ret = rs.getInt("accessLevel");
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

	public String adminLocation(String input_id) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String ret = null;
		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT location FROM carcaro.usr_admin WHERE id = ? ");

			stmt = conn.prepareStatement(sql.toString());

			stmt.setString(1, input_id);

			rs = stmt.executeQuery();

			if (rs.next()) {
				ret = rs.getString("location");
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

	/**
	 * 1:1문의 등록
	 * 
	 * @param title
	 * @param desc
	 * @param usr
	 * @return
	 * @throws SQLException
	 */
	public int registerQna(String title, String desc, String usr)
			throws SQLException {

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int isOK = 0;
		StringBuffer sql = new StringBuffer();

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql query

			sql.append(" INSERT INTO usr_qna ( title, usr_qna.desc, reg_date, usr_phone, answered )"
					+ " VALUES (?, ?, NOW(), ?, 'N');");
			stmt = conn.prepareStatement(sql.toString());

			stmt.setString(1, title);
			stmt.setString(2, desc);
			stmt.setString(3, usr);
			stmt.execute();

			// successfully inserted
			isOK = 1;
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		return isOK;
	}

	// Q&A 답변
	public void answerQna(String qid, String answer) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			sql.append(" UPDATE `usr_qna` SET answer=?, answered=?, answer_date=NOW() WHERE QID = ?");
			stmt = conn.prepareStatement(sql.toString());
			stmt.setString(1, answer);
			stmt.setString(2, "Y");
			stmt.setInt(3, Integer.parseInt(qid));

			stmt.execute();

		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
	}

	// Q&A 수정
	public void modifyQna(String qid, String title, String desc)
			throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			sql.append(" UPDATE `usr_qna` SET title=?, desc=? WHERE QID = ?");
			stmt = conn.prepareStatement(sql.toString());
			stmt.setString(1, title);
			stmt.setString(2, desc);
			stmt.setString(2, "Y");
			stmt.setInt(3, Integer.parseInt(qid));

			stmt.execute();

		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
	}

	// Q&A
	public void deleteQna(String qid) throws NumberFormatException,
			SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = connPool.getConnection();

			String sql = " DELETE FROM `usr_qna` WHERE QID = ? ";
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, Integer.parseInt(qid));

			pstmt.execute();

		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
	}

	public JSONObject getQnaDetail(String qid) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		JSONObject json = new JSONObject();

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			sql.append(" SELECT * FROM usr_qna WHERE QID=?");
			stmt = conn.prepareStatement(sql.toString());
			stmt.setInt(1, Integer.parseInt(qid));
			rs = stmt.executeQuery();

			if (rs.next()) {
				json.put("QID", rs.getInt("QID"));
				json.put("title", rs.getString("title"));
				json.put("usr_phone", rs.getString("usr_phone"));
				json.put("desc", rs.getString("desc"));
				json.put("reg_date", rs.getString("reg_date"));
				String ans = rs.getString("answered"); // 'Y' if answered. 'N'
														// if not
				json.put("answered", ans);
				if ("Y".equals(ans)) { // If there's an Answer
					json.put("answer", rs.getString("answer"));
					json.put("answer_date", rs.getString("answer_date"));
				}
			}

		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}

		return json;
	}
	/**
	 * 웹용 Q&A 페이징처리가 들어가 있다.
	 * @param pageSize
	 * @param currentPage
	 * @param user_phone
	 * @param option2
	 * @return
	 * @throws SQLException
	 */
	public JSONArray getQna(int pageSize, int currentPage, String user_phone, String option2)
			throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		JSONArray jsonArr = new JSONArray();

		try {
			// Connection pool 연결
			conn = connPool.getConnection();
			int a=1;
			// sql query
			if ("ALL".equals(option2)) {
				// ignore user_phone
				sql.append(" SELECT * FROM usr_qna ORDER BY QID DESC");
				sql.append(" LIMIT ?,?");
				stmt = conn.prepareStatement(sql.toString());
				stmt.setInt(a++, (currentPage - 1) * pageSize);
				stmt.setInt(a++, pageSize);
			} else {
				sql.append(" SELECT * FROM usr_qna WHERE usr_phone = ? ORDER BY QID DESC");
				sql.append(" LIMIT ?,?");
				stmt = conn.prepareStatement(sql.toString());
				stmt.setString(a++, user_phone);
				stmt.setInt(a++, (currentPage - 1) * pageSize);
				stmt.setInt(a++, pageSize);
			}
			rs = stmt.executeQuery();

			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("QID", rs.getInt("QID"));
				json.put("title", rs.getString("title"));
				json.put("usr_phone", rs.getString("usr_phone"));
				String desc = rs.getString("desc");
				// desc = desc.replaceAll("\"", "\\\"");
				json.put("desc", desc);
				json.put("reg_date", rs.getString("reg_date"));

				String ans = rs.getString("answered"); // 'Y' if answered. 'N'
														// if not
				json.put("answered", ans);
				// If there's an Answer
				if ("Y".equals(ans)) {
					json.put("answer", rs.getString("answer"));
					json.put("answer_date", rs.getString("answer_date"));
				}
				jsonArr.add(json);
				json = null;
			}

		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}

		return jsonArr;
	}
	/**
	 * Q&A 전체글수
	 * @param pageSize
	 * @param currentPage
	 * @param user_phone
	 * @param option2
	 * @return
	 * @throws SQLException
	 */
	public long getQnaCnt(String user_phone, String option2)
			throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		long totalCnt=0;

		try {
			// Connection pool 연결
			conn = connPool.getConnection();
			int a=1;
			// sql query
			if ("ALL".equals(option2)) {
				// ignore user_phone
				sql.append(" SELECT count(*) as count FROM usr_qna ORDER BY QID DESC");
				stmt = conn.prepareStatement(sql.toString());
			} else {
				sql.append(" SELECT count(*) as count FROM usr_qna WHERE usr_phone = ? ORDER BY QID DESC");
				stmt = conn.prepareStatement(sql.toString());
				stmt.setString(a++, user_phone);
			}
			rs = stmt.executeQuery();

			if (rs.next()) {
				totalCnt = rs.getInt("count");
			}

		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}

		return totalCnt;
	}
	/**
	 * 앱용 Q&A 
	 * @param user_phone
	 * @param option2
	 * @return
	 * @throws SQLException
	 */
	public JSONArray getQna(String user_phone, String option2)
			throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		JSONArray jsonArr = new JSONArray();
		
		try {
			// Connection pool 연결
			conn = connPool.getConnection();
			int a=1;
			// sql query
			if ("ALL".equals(option2)) {
				// ignore user_phone
				sql.append(" SELECT * FROM usr_qna ORDER BY QID DESC");
				sql.append(" LIMIT ?,?");
				stmt = conn.prepareStatement(sql.toString());
			} else {
				sql.append(" SELECT * FROM usr_qna WHERE usr_phone = ? ORDER BY QID DESC");
				sql.append(" LIMIT ?,?");
				stmt = conn.prepareStatement(sql.toString());
				stmt.setString(a++, user_phone);
			}
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("QID", rs.getInt("QID"));
				json.put("title", rs.getString("title"));
				json.put("usr_phone", rs.getString("usr_phone"));
				String desc = rs.getString("desc");
				// desc = desc.replaceAll("\"", "\\\"");
				json.put("desc", desc);
				json.put("reg_date", rs.getString("reg_date"));
				
				String ans = rs.getString("answered"); // 'Y' if answered. 'N'
				// if not
				json.put("answered", ans);
				// If there's an Answer
				if ("Y".equals(ans)) {
					json.put("answer", rs.getString("answer"));
					json.put("answer_date", rs.getString("answer_date"));
				}
				jsonArr.add(json);
				json = null;
			}
			
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		
		return jsonArr;
	}

	/**
	 * 공지사항의 갯수를 가져온다.
	 * 
	 * @return 공지사항의 전체 갯수
	 * @throws SQLException
	 */
	public int getNoticeCount() throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int ret = 0;

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT count(*) as `rc` FROM `bbs_notice`");

			stmt = conn.prepareStatement(sql.toString());
			rs = stmt.executeQuery();

			if (rs.next()) {
				ret = rs.getInt("rc");
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

	/**
	 * 가장 최근의 공지사항 날짜를 가져온다.
	 * 
	 * @return 최근의 공지사항 날짜
	 * @throws SQLException
	 */
	public String getLastNoticeDate() throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String retDate = null;

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT `reg_date` FROM `bbs_notice` ORDER BY `reg_date` DESC LIMIT 0,1");

			stmt = conn.prepareStatement(sql.toString());
			rs = stmt.executeQuery();

			if (rs.next()) {
				retDate = rs.getString("reg_date");
			}
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		return retDate;
	}
	/**
	 * 웹용 공지사항 페이징처리가 들어가 있음.
	 * @param pageSize
	 * @param currentPage
	 * @return
	 * @throws SQLException
	 */
	public JSONArray getNotice(int pageSize, int currentPage) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		JSONArray retJson = new JSONArray();

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT * FROM `bbs_notice` ORDER BY `no` DESC");
			sql.append(" LIMIT ?,?");
			stmt = conn.prepareStatement(sql.toString());
			stmt.setInt(1, (currentPage - 1) * pageSize);
			stmt.setInt(2, pageSize);
			rs = stmt.executeQuery();

			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("no", rs.getInt("no"));
				json.put("title", rs.getString("title"));
				json.put("description", rs.getString("description"));
				json.put("reg_date", rs.getString("reg_date"));
				json.put("register_by", rs.getString("register_by"));
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
	/**
	 * 공지시항 게시글 전체글수
	 * @param pageSize
	 * @param currentPage
	 * @return
	 * @throws SQLException
	 */
	public long getNoticeCnt() throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		long totalCnt=0;

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT count(*) as count FROM `bbs_notice` ORDER BY `no` DESC");
			stmt = conn.prepareStatement(sql.toString());
			rs = stmt.executeQuery();

			if (rs.next()) {
				totalCnt = rs.getInt("count");
			}
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		return totalCnt;
	}
	/**
	 * 앱용 공지사항
	 * @return
	 * @throws SQLException
	 */
	public JSONArray getNotice() throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		JSONArray retJson = new JSONArray();
		
		try {
			// Connection pool 연결
			conn = connPool.getConnection();
			
			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT * FROM `bbs_notice` ORDER BY `no` DESC");
			stmt = conn.prepareStatement(sql.toString());
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("no", rs.getInt("no"));
				json.put("title", rs.getString("title"));
				json.put("description", rs.getString("description"));
				json.put("reg_date", rs.getString("reg_date"));
				json.put("register_by", rs.getString("register_by"));
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

	public JSONObject getNoticeDetail(String no) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		JSONObject json = new JSONObject();

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			sql.append(" SELECT * FROM bbs_notice WHERE no=?");
			stmt = conn.prepareStatement(sql.toString());
			stmt.setInt(1, Integer.parseInt(no));
			rs = stmt.executeQuery();

			if (rs.next()) {
				json.put("no", rs.getInt("no"));
				json.put("title", rs.getString("title"));
				json.put("description", rs.getString("description"));
				json.put("reg_date", rs.getString("reg_date"));
				json.put("register_by", rs.getString("register_by"));
			}

		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}

		return json;
	}

	public void modifyNotice(String no, String title, String description)
			throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			sql.append(" UPDATE `bbs_notice` SET title = ?, description = ? WHERE no = ?");
			stmt = conn.prepareStatement(sql.toString());
			stmt.setString(1, title);
			stmt.setString(2, description);
			stmt.setInt(3, Integer.parseInt(no));

			stmt.execute();

		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}

	}

	public void createNotice(String title, String description)
			throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			sql.append(" INSERT INTO `bbs_notice` (title,description,reg_date) VALUES (?, ?, NOW()) ");
			stmt = conn.prepareStatement(sql.toString());
			stmt.setString(1, title);
			stmt.setString(2, description);

			stmt.execute();

		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
	}

	public void deleteNotice(String no) throws NumberFormatException,
			SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = connPool.getConnection();

			String sql = " DELETE FROM `bbs_notice` WHERE no = ? ";
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, Integer.parseInt(no));

			pstmt.execute();

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
	 * 웹용 도움말 페이징 처리가 들어가 있다.
	 * @param pageSize
	 * @param currentPage
	 * @return
	 * @throws SQLException
	 */
	public JSONArray getHelp(int pageSize, int currentPage) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		JSONArray retJson = new JSONArray();

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT * FROM `bbs_help` ORDER BY `hNO` DESC");
			sql.append(" LIMIT ?,?");
			
			stmt = conn.prepareStatement(sql.toString());
			stmt.setInt(1, (currentPage - 1) * pageSize);
			stmt.setInt(2, pageSize);
			rs = stmt.executeQuery();

			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("hNO", rs.getInt("hNO"));
				json.put("hTitle", rs.getString("hTitle"));
				json.put("hDescription", rs.getString("hDescription"));
				json.put("reg_date", rs.getString("reg_date"));
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
	/**
	 * 도움말 전체 글수
	 * @return
	 * @throws SQLException
	 */
	public long getHelpCnt() throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		long totalCnt=0;
		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT count(*) as count FROM `bbs_help` ORDER BY `hNO` DESC");
			stmt = conn.prepareStatement(sql.toString());
			rs = stmt.executeQuery();

			if (rs.next()) {
				totalCnt = rs.getInt("count");
			}
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		return totalCnt;
	}
	/**
	 * 앱용 도움말
	 * @return
	 * @throws SQLException
	 */
	public JSONArray getHelp() throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		JSONArray retJson = new JSONArray();
		
		try {
			// Connection pool 연결
			conn = connPool.getConnection();
			
			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT * FROM `bbs_help` ORDER BY `hNO` DESC");
			
			stmt = conn.prepareStatement(sql.toString());
			rs = stmt.executeQuery();
			String mainPath = "http://vcarcaro.cafe24.com:8080/picture/";
			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("hNO", rs.getInt("hNO"));
				json.put("hTitle", rs.getString("hTitle"));
				json.put("hDescription", rs.getString("hDescription"));
				json.put("reg_date", rs.getString("reg_date"));
				json.put("picture", mainPath + rs.getString("picture"));
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

	public JSONObject getHelpDetail(String hNO) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		JSONObject json = new JSONObject();

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			sql.append(" SELECT * FROM `bbs_help` WHERE hNO=?");
			stmt = conn.prepareStatement(sql.toString());
			stmt.setInt(1, Integer.parseInt(hNO));
			rs = stmt.executeQuery();

			if (rs.next()) {
				json.put("hNO", rs.getInt("hNO"));
				json.put("hTitle", rs.getString("hTitle"));
				json.put("hDescription", rs.getString("hDescription"));
				json.put("reg_date", rs.getString("reg_date"));
			}

		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}

		return json;
	}

	public JSONArray getYangjapa() throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		JSONArray retJson = new JSONArray();

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT * FROM `bbs_yangjapa` ORDER BY `yNO` DESC");

			stmt = conn.prepareStatement(sql.toString());
			rs = stmt.executeQuery();

			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("yNO", rs.getInt("yNO"));
				json.put("yTitle", rs.getString("yTitle"));
				json.put("hURL", rs.getString("yURL"));
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

	public int createVCallSchedule(String sBegin, String sEnd,
			String sLocaName, int sCallCount, String sRepeat, int sEnabled)
			throws SQLException {
		int ret = 0;

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = connPool.getConnection();

			String sql = " INSERT INTO `vcall_schedule` (sBegin, sEnd, sLocaName, sCallCount, sRepeat, sEnabled) VALUES (?,?,?,?,?,?) ; ";
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, sBegin);
			pstmt.setString(2, sEnd);
			pstmt.setString(3, sLocaName);
			pstmt.setInt(4, sCallCount);
			pstmt.setString(5, sRepeat);
			pstmt.setInt(6, sEnabled);

			pstmt.execute();

			pstmt.clearBatch();

			rs = pstmt.executeQuery("select LAST_INSERT_ID()");

			if (rs.next()) {
				ret = rs.getInt(1);
			}

		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}

		return ret;
	}

	public int setVCallScheduleEnabled(int sID, int sEnabled)
			throws SQLException {
		int ret = 0;

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = connPool.getConnection();

			String sql = " UPDATE `vcall_schedule` SET sEnabled =? WHERE sID = ? ; ";
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, sEnabled);
			pstmt.setInt(2, sID);

			pstmt.execute();

			ret = 1;
		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}

		return ret;
	}
	/**
	 * 가상콜 목록
	 * @param pageSize
	 * @param currentPage
	 * @return
	 * @throws SQLException
	 */
	public JSONArray getScheduleList(int pageSize, int currentPage) throws SQLException {

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		JSONArray retJson = new JSONArray();

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT * FROM `vcall_schedule` ORDER BY `sID` DESC");
			sql.append(" LIMIT ?,?");
			stmt = conn.prepareStatement(sql.toString());
			stmt.setInt(1, (currentPage - 1) * pageSize);
			stmt.setInt(2, pageSize);
			rs = stmt.executeQuery();

			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("sID", rs.getInt("sID"));
				json.put("sBegin", rs.getString("sBegin"));
				json.put("sEnd", rs.getString("sEnd"));
				json.put("sLocaName", rs.getString("sLocaName"));
				json.put("sCallCount", rs.getInt("sCallCount"));
				json.put("sRepeat", rs.getString("sRepeat"));
				json.put("sEnabled", rs.getInt("sEnabled"));
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
	/**
	 * 가상콜 전체 글수
	 * @param pageSize
	 * @param currentPage
	 * @return
	 * @throws SQLException
	 */
	public long getScheduleList() throws SQLException {

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		long totalCnt = 0;
		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT count(*) as count FROM `vcall_schedule` ORDER BY `sID` DESC");
			stmt = conn.prepareStatement(sql.toString());
			rs = stmt.executeQuery();

			if (rs.next()) {
				totalCnt = rs.getInt("count");
			}
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		return totalCnt;
	}
	/**
	 * 주소 이름에 따라 정보를 가져온다.
	 * 
	 * @param sido
	 * @param gugun
	 * @param dong
	 * @return
	 * @throws SQLException
	 */
	public JSONArray getLocationInfoByLocaName(String sido, String gugun,
			String dong) throws SQLException {
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

			if (sido == null || sido.length() == 0) {
				sido = "%";
			}
			if (gugun == null || gugun.length() == 0) {
				gugun = "%";
			}
			if (dong == null || dong.length() == 0) {
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

	public int createVCallList(int cSID, String lat, String lng, String oS,
			String src, String dest, String callTime, int sEnabled)
			throws SQLException {
		int ret = 0;

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = connPool.getConnection();

			String sql = " INSERT INTO `vcall_list` (cSID, cLat, cLng, cOS, cSrc, cDest, cCallTime, cEnabled) VALUES (?,?,?,?,?,?,?,?) ; ";
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, cSID);
			pstmt.setString(2, lat);
			pstmt.setString(3, lng);
			pstmt.setString(4, oS);
			pstmt.setString(5, src);
			pstmt.setString(6, dest);
			pstmt.setString(7, callTime);
			pstmt.setInt(8, sEnabled);

			pstmt.execute();

		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}

		return ret;

	}

	public void removeSchedule(String sID) throws SQLException {

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = connPool.getConnection();

			String sql = " DELETE FROM vcall_schedule WHERE sID = ? ";
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, Integer.parseInt(sID));

			pstmt.execute();

		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}

	}

	public JSONObject getVCallScheduleDetail(String sID) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		JSONObject retJson = new JSONObject();

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT * FROM `vcall_schedule` WHERE sID = ?");

			stmt = conn.prepareStatement(sql.toString());
			stmt.setInt(1, Integer.parseInt(sID));

			rs = stmt.executeQuery();

			if (rs.next()) {
				retJson.put("sID", rs.getInt("sID"));
				retJson.put("sBegin", rs.getString("sBegin"));
				retJson.put("sEnd", rs.getString("sEnd"));
				retJson.put("sLocaName", rs.getString("sLocaName"));
				retJson.put("sCallCount", rs.getInt("sCallCount"));
				retJson.put("sRepeat", rs.getString("sRepeat"));
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

	public void removeVCall(String cSID) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = connPool.getConnection();

			String sql = " DELETE FROM vcall_list WHERE cSID = ? ";
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, Integer.parseInt(cSID));

			pstmt.execute();

		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
	}

	public void createHelp(String title, String description,String picture)
			throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			sql.append(" INSERT INTO `bbs_help` (hTitle,hDescription,picture,reg_date) VALUES (?, ?, ?, NOW()) ");
			stmt = conn.prepareStatement(sql.toString());
			stmt.setString(1, title);
			stmt.setString(2, description);
			stmt.setString(3, picture);

			stmt.execute();

		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
	}

	public void deleteHelp(String hNO) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = connPool.getConnection();

			String sql = " DELETE FROM `bbs_help` WHERE hNO = ? ";
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, Integer.parseInt(hNO));

			pstmt.execute();

		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
	}

	public void modifyHelp(String hNO, String hTitle, String hDescription)
			throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			sql.append(" UPDATE `bbs_help` SET hTitle = ?, hDescription = ? WHERE hNO = ?");
			stmt = conn.prepareStatement(sql.toString());
			stmt.setString(1, hTitle);
			stmt.setString(2, hDescription);
			stmt.setInt(3, Integer.parseInt(hNO));

			stmt.execute();

		} catch (SQLException e1) {
			e1.printStackTrace();
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
	 * 관리자관리 조회
	 * 
	 * @param access
	 * @param sido
	 * @param gu
	 * @return
	 * @throws SQLException
	 */
	public Admin[] getAdminList(int pageSize, int currentPage, String access, String sido, String gu)
			throws SQLException {
		List<Admin> list = new ArrayList<Admin>();

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = connPool.getConnection();

			StringBuffer sql = new StringBuffer();
			sql.append("SELECT * FROM usr_admin WHERE accessLevel like ? ");
			sql.append(" AND location like ? ");
			sql.append(" AND address like ? ");
			sql.append(" LIMIT ?,? ");
			
			pstmt = conn.prepareStatement(sql.toString());

			int a = 1;
			pstmt.setString(a++, access + "%"); // 위치
			pstmt.setString(a++, "%" + sido + "%"); // 사업자 위치
			pstmt.setString(a++, "%" + gu + "%"); // 주소
			pstmt.setInt(a++, (currentPage - 1) * pageSize);
			pstmt.setInt(a++, pageSize);

			System.out.println("sql====" + sql);

			rs = pstmt.executeQuery();

			while (rs.next()) {
				Admin am = new Admin();
				am.setNo(rs.getString("no"));
				am.setId(rs.getString("id"));
				am.setPw(rs.getString("pw"));
				am.setEmail(rs.getString("email"));
				am.setAccessLevel(rs.getString("accessLevel"));
				am.setLocation(rs.getString("location"));
				am.setHierarchy(rs.getString("hierarchy"));
				am.setCorName(rs.getString("corName"));
				am.setName(rs.getString("name"));
				am.setTel(rs.getString("tel"));
				am.setSmartPhone(rs.getString("smartPhone"));
				am.setAddress(rs.getString("address"));
				list.add(am);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				connPool.returnConn(conn);

		}
		return list.toArray(new Admin[0]);
	}

	/**
	 * 관리자관리 총조회 개수
	 * 
	 * @param access
	 * @param sido
	 * @param gu
	 * @return
	 * @throws SQLException
	 */
	public long getAdminCount(String access, String sido, String gu)
			throws SQLException {
		int count = 0;

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = connPool.getConnection();
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT count(*) FROM usr_admin WHERE accessLevel like ? ");
			sql.append(" AND location like ? ");
			sql.append(" AND address like ? ");

			pstmt = conn.prepareStatement(sql.toString());

			int a = 1;
			pstmt.setString(a++, access + "%"); // 위치
			pstmt.setString(a++, "%" + sido + "%"); // 사업자 위치
			pstmt.setString(a++, "%" + gu + "%"); // 주소

			System.out.println("sql====" + sql);

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

	/**
	 * 관리자관리 상세보기
	 * 
	 * @param no
	 * @return
	 * @throws SQLException
	 */
	public Admin getAdminInfo(String no) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Admin am = null;

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT * FROM usr_admin WHERE no = ?;");
			stmt = conn.prepareStatement(sql.toString());

			stmt.setString(1, no);
			rs = stmt.executeQuery();

			while (rs.next()) {
				am.setNo(rs.getString("no"));
				am.setId(rs.getString("id"));
				am.setPw(rs.getString("pw"));
				am.setEmail(rs.getString("email"));
				am.setAccessLevel(rs.getString("accessLevel"));
				am.setLocation(rs.getString("location"));
				am.setHierarchy(rs.getString("hierarchy"));
				am.setCorName(rs.getString("corName"));
				am.setName(rs.getString("name"));
				am.setTel(rs.getString("tel"));
				am.setSmartPhone(rs.getString("smartPhone"));
				am.setAddress(rs.getString("address"));
			}
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		return am;
	}
	/** 
	 * 관리자관리 삭제처리
	 * @param admin
	 * @throws SQLException
	 */
	public boolean deleteAdmin(Admin admin) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			// Connection pool 연결
			conn = connPool.getConnection();
			StringBuffer sql = new StringBuffer();
			sql.append("DELETE FROM usr_admin ");
			sql.append(" WHERE no=?");

			pstmt = conn.prepareStatement(sql.toString());
			int i = 1;
			pstmt.setString(i++, admin.getNo());
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
	 * 관리자관리 수정처리
	 * @param admin
	 * @throws SQLException
	 */
	public boolean updateAdmin(Admin admin) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			// Connection pool 연결
			conn = connPool.getConnection();
			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE usr_admin SET");
			sql.append(" id=?,");
			sql.append(" pw=md5(?), email=?, accessLevel=?,");
			sql.append(" corName=?, name=?, tel=?, smartPhone=?, address=?");
			sql.append(" WHERE no=?");

			pstmt = conn.prepareStatement(sql.toString());
			int i = 1;
			pstmt.setString(i++, admin.getId());
			pstmt.setString(i++, admin.getPw());
			pstmt.setString(i++, admin.getEmail());
			pstmt.setString(i++, admin.getAccessLevel());
			pstmt.setString(i++, admin.getCorName());
			pstmt.setString(i++, admin.getName());
			pstmt.setString(i++, admin.getTel());
			pstmt.setString(i++, admin.getSmartPhone());
			pstmt.setString(i++, admin.getAddress());
			pstmt.setString(i++, admin.getNo());
			//pstmt.setString(i++, admin.getLocation());
			//pstmt.setString(i++, admin.getHierarchy());
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
	 * 관리자관리 등록처리
	 * @param admin
	 * @return
	 * @throws SQLException
	 */
	public int registerAdmin(Admin admin)
			throws SQLException {

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int isOK = 0;
		StringBuffer sql = new StringBuffer();

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql query
			// pw 항목은 암호화 해야 한다.
			sql.append(" INSERT INTO usr_admin ( no, id, pw, email, accessLevel, corName, name, tel, smartPhone, address)"
					+ " VALUES (?, ?, md5(?), ?, ?, ?, ?, ?, ?, ?);");
			stmt = conn.prepareStatement(sql.toString());
			int i = 1;
			stmt.setString(i++, admin.getNo()); // 사업자번호
			stmt.setString(i++, admin.getId()); // 아이디
			stmt.setString(i++, admin.getPw()); // 비밀번호
			stmt.setString(i++, admin.getEmail()); // 이메일
			stmt.setString(i++, admin.getAccessLevel()); // 권한
			stmt.setString(i++, admin.getCorName()); // 회사명
			stmt.setString(i++, admin.getName()); // 이름
			stmt.setString(i++, admin.getTel()); // 전화번호
			stmt.setString(i++, admin.getSmartPhone()); // 휴대폰
			stmt.setString(i++, admin.getAddress()); // 주소
			stmt.execute();

			// successfully inserted
			isOK = 1;
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				connPool.returnConn(conn);
		}
		return isOK;
	}

}
