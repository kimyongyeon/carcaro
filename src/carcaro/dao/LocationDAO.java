package carcaro.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import carcaro.ConnectionPool;
import carcaro.bean.VCall;
import carcaro.bean.VLocationInfoBase;

public class LocationDAO {

	private ConnectionPool connPool;
	
	public LocationDAO(ConnectionPool connPool){
		// share ConnectionPool
		this.connPool = connPool;
	}
	
	// 시 
	public JSONArray getSido(String currentLocal) throws SQLException{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		JSONArray ret = new JSONArray();
		JSONObject obj = new JSONObject();
		
		try {
			conn = connPool.getConnection();
			
			StringBuffer sql = new StringBuffer();
			
			sql.append("SELECT DISTINCT `Sido` FROM `address` ");
			
			stmt = conn.prepareStatement(sql.toString());
			
			rs = stmt.executeQuery();
			
			while(rs.next()){
				JSONObject json = new JSONObject();
				json.put("Sido", rs.getString("Sido"));
				ret.add(json);
			}
			
		}finally {
			if ( rs != null) {
				rs.close();
			}
			if (stmt != null){
				stmt.close();
			}
			if (conn != null){
				connPool.returnConn(conn);
			}
		}
		
		return ret;
	}
	
	// 구
	public JSONArray getGugun(String curSido) throws SQLException{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		JSONArray ret = new JSONArray();
		
		try {
			conn = connPool.getConnection();
			
			StringBuffer sql = new StringBuffer();
			
			sql.append("SELECT DISTINCT `Gugun` FROM `address` WHERE Sido = ? ");
			
			stmt = conn.prepareStatement(sql.toString());
			stmt.setString(1, curSido);
			
			rs = stmt.executeQuery();
			
			while(rs.next()){
				JSONObject json = new JSONObject();
				json.put("Gugun", rs.getString("Gugun"));
				ret.add(json);
			}
			
		}finally {
			if ( rs != null) {
				rs.close();
			}
			if (stmt != null){
				stmt.close();
			}
			if (conn != null){
				connPool.returnConn(conn);
			}
		}
		
		return ret;
	}
	
	// 동
	public JSONArray getDong(String curGu) throws SQLException{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		JSONArray ret = new JSONArray();
		
		try {
			conn = connPool.getConnection();
			
			StringBuffer sql = new StringBuffer();
			
			sql.append("SELECT DISTINCT `Dong` FROM `address` WHERE Gugun = ? ");
			
			stmt = conn.prepareStatement(sql.toString());
			stmt.setString(1, curGu);
			
			rs = stmt.executeQuery();
			
			while(rs.next()){
				JSONObject json = new JSONObject();
				json.put("Dong", rs.getString("Dong"));
				ret.add(json);
			}
			
		}finally {
			if ( rs != null) {
				rs.close();
			}
			if (stmt != null){
				stmt.close();
			}
			if (conn != null){
				connPool.returnConn(conn);
			}
		}
		
		return ret;
	}
	
	
	public JSONObject getLatLng(int LID) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		JSONObject ret = new JSONObject();
		
		try {
			conn = connPool.getConnection();
			
			StringBuffer sql = new StringBuffer();
			
			sql.append("SELECT `Lat`, `Lng` FROM `address` WHERE LID = ?");
			
			stmt = conn.prepareStatement(sql.toString());
			
			rs = stmt.executeQuery();
			
			while(rs.next()){
				String Lat = rs.getString("Lat");
				String Lng = rs.getString("Lng");
				ret.put("LID", LID);
				ret.put("Lat", Lat);
				ret.put("Lng", Lng);
			}
			
		}finally {
			if ( rs != null) {
				rs.close();
			}
			if (stmt != null){
				stmt.close();
			}
			if (conn != null){
				connPool.returnConn(conn);
			}
		}
		
		return ret;
		
	}
	
	
	public List<VCall> loadCallList() throws SQLException {
		List<VCall> vCallList = new ArrayList<VCall>();
			
		Connection conn 		=null;
		PreparedStatement stmt 	=null;
		ResultSet rs 			=null;
		String sql 				=new String();
	
		try {
			// Connection pool 연결
			conn = connPool.getConnection();
//			SELECT vcall_list.*, vcall_schedule.sRepeat FROM `vcall_list` JOIN vcall_schedule WHERE vcall_list.cSID = vcall_schedule.sID AND cNum = 1
			sql = " SELECT vcall_list.*, vcall_schedule.sRepeat FROM vcall_list JOIN vcall_schedule WHERE vcall_list.cSID = vcall_schedule.sID ORDER BY cNum DESC ";
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
	
			while (rs.next()){
				String cNum 		= rs.getString("cNum");
				int cSID			= rs.getInt("cSID");
				String cLat			= rs.getString("cLat");
				String cLng			= rs.getString("cLng");
				String cOS			= rs.getString("cOS");
				String cSrc			= rs.getString("cSrc");
				String cDest		= rs.getString("cDest");
				String cCallTime 	= rs.getString("cCallTime");
				int cEnabled 		= rs.getInt("cEnabled");
				String sRepeat		= rs.getString("sRepeat");
				
				VCall _vc = new VCall(cNum, cSID ,cLat,cLng,cOS,cSrc,cDest,cCallTime, cEnabled, sRepeat);
				
				vCallList.add(_vc);
			}
	
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
			
		return vCallList;
	}


	public List<VLocationInfoBase> loadLocationInfo() throws SQLException {
		List<VLocationInfoBase> vLIBList = new ArrayList<VLocationInfoBase>();
		
		Connection conn 		=null;
		PreparedStatement stmt 	=null;
		ResultSet rs 			=null;
		String sql 				=new String();
	
		try {
			// Connection pool 연결
			conn = connPool.getConnection();
	
			sql = " SELECT * FROM `address` ORDER BY LID DESC ";
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
	
			while (rs.next()){
				int LID 			= rs.getInt("LID");
				String Sido			= rs.getString("Sido");
				String Gugun		= rs.getString("Gugun");
				String Dong			= rs.getString("Dong");
				String Lat			= rs.getString("Lat");
				String Lng			= rs.getString("Lng");
				int Status			= rs.getInt("Status");
				
				VLocationInfoBase _vLIB = new VLocationInfoBase(LID, Sido, Gugun, Dong, Lat, Lng, Status);
				vLIBList.add(_vLIB);
			}
	
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
			
		return vLIBList;
	}


	public void updateLatLng(int index, String Lat, String Lng) throws SQLException {
		Connection conn 		=null;
		PreparedStatement stmt 	=null;
		ResultSet rs 			=null;
		String sql 				=new String();
	
		try {
			// Connection pool 연결
			conn = connPool.getConnection();
	
			sql = " UPDATE `address` SET Lat = ?, Lng = ?, Status = 1 WHERE LID = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, Lat);
			stmt.setString(2, Lng);
			stmt.setInt(3, index);
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
}
