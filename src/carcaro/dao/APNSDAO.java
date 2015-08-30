package carcaro.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import carcaro.ConnectionPool;

public class APNSDAO {

	public ConnectionPool connPool;
	
	
	public APNSDAO(ConnectionPool connPool) {
    	this.connPool = connPool;
    }
	
	/**
	 * 메시지를 받아서 DB에 저장후 MID를 돌려준다.
	 * @param topic
	 * @param messageBody
	 * @return MID
	 * @throws SQLException
	 */
	public int createMessageBody(String topic, String messageBody) throws SQLException{
		Connection conn = null;
		PreparedStatement pstmt = null;
		Statement stmt = null;
		ResultSet rs = null;
		int ret = 0;
		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql.append("INSERT INTO apns_message (topic,messageBody,timeStamp) VALUES (?,?,NOW()); ");
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setString(1, topic);
			pstmt.setString(2, messageBody);
			pstmt.execute();

			stmt = conn.createStatement();
			rs = stmt.executeQuery("select LAST_INSERT_ID(); ");
			if(rs.next()){
				ret = rs.getInt(1);
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
	 * MID를 이용해 MessageBody 전문을 Receive
	 * @param mid
	 * @return messageBody
	 * @throws SQLException
	 */
	public String getMessageBody(String mid) throws SQLException {
		int MID = Integer.parseInt(mid);
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		Statement stmt = null;
		ResultSet rs = null;
		String ret = "";
		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			// sql 명령
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT * FROM apns_message WHERE MID=? ");
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setInt(1, MID);
			rs = pstmt.executeQuery();

			if(rs.next()){
				ret = rs.getString("messageBody");
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
}
