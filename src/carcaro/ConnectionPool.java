package carcaro;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

/**
 * db Connection???�러 �??�어??공유?�다.
 * 
 * connection???�구???�레?�들???�서??�� ?�어?�서 connection??받아간다.
 * 
 * ServerAlive같�? 경우 ?�서??�� 받아�?? ?�으�?최근???�태�?반영?��? ?�는 경우�?발생?�다.
 * 
 */
public class ConnectionPool {

	/** connection 처음 개수 */
	private static final int nMinCount = 1;

	/** jdbc driver ?�래??*/
	String driver = null;

	String connection = null;

	String user;

	String passwd;

	Connection[] aConnection;

	private int cntUsing;

	boolean m_bClosing = false;

	private static final Logger logger = Logger.getRootLogger();
	
	private static ConnectionPool connPool;
	
	public synchronized static ConnectionPool getInstance() throws SQLException {
		
		if(connPool == null){
			
//			ResourceBundle resource = ResourceBundle.getBundle("/conf.properties");
//
//			final String driver = resource.getString("db.driver");
//			final String connection = resource.getString("db.connection");
//			final String userId = resource.getString("db.userId");
//			final String userPw = resource.getString("db.userPw");
//			final int connCount = Integer.parseInt(resource.getString("db.connCount"));
			
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			
			Properties prop = new Properties();
			try {
				prop.load(classLoader.getResourceAsStream("../conf.properties"));
			} catch (IOException e) {
				logger.error("can't find conf.propertes. broker hostname set localhost.");
				e.printStackTrace();
			}
			
			final String driver = prop.getProperty("db.driver");
			final String connection = prop.getProperty("db.connection");
			final String userId = prop.getProperty("db.userId");
			final String userPw = prop.getProperty("db.userPw");
			final int connCount = Integer.parseInt(prop.getProperty("db.connCount"));
			
			
			connPool = new ConnectionPool(driver, connection, userId, userPw, connCount);
		}
		return connPool;
	}
	
	public synchronized static void destory(){
		
		if(connPool != null){
			connPool.close(); 
			
			for (Enumeration<Driver> e = DriverManager.getDrivers(); e.hasMoreElements();) {
				Driver driver = e.nextElement();

				try {
					DriverManager.deregisterDriver(driver);
				} catch (SQLException x) {
					logger.error("Driver deristration failed", x);
				}
			}

			connPool = null;
		}
	}
	
	
	private ConnectionPool(String _driver, String _connection,
			String _user, String _passwd, int maxConnSize) throws SQLException {

		driver = _driver;
		connection = _connection;
		user = _user;
		passwd = _passwd;
		try {
			Class.forName(_driver);
			Connection connTemp;
			int nMaxCount = maxConnSize < nMinCount ? nMinCount : maxConnSize;
			this.aConnection = new Connection[nMaxCount];
			for (int i = 0; i < nMinCount; i++) {
				connTemp = DriverManager.getConnection(_connection, _user,
						_passwd);
				this.aConnection[i] = connTemp;
			}
		} catch (ClassNotFoundException e) {
			logger.error(_connection + " " + _user + " " + _passwd, e);
		}
	}

	private Connection createConnection() throws SQLException {
		Connection conn = DriverManager.getConnection(this.connection,
				this.user, this.passwd);
		return conn;
	}

	private int getIndex(Connection c) {
		// ?�용중인 커넥?�이 배열???��?분에 ?�다.
		for (int i = this.aConnection.length; --i >= 0;) {
			if (aConnection[i] == c) {
				return i;
			}
		}
		return -1;
	}

	private boolean isConnected(Connection conn) {

		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = conn.createStatement();
			String sql = "SELECT 1";
			rs = stmt.executeQuery(sql);
			if (rs.next()) {
				return true;
			}
		} catch (Exception e) {
			logger.error("커넥?�이 ?�어졌습?�다.", e);
		}

		try {
			if (rs != null) {
				rs.close();
				rs = null;
			}
		} catch (SQLException e) {
		}
		try {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		} catch (SQLException e) {
		}
		try {
			conn.close();
		} catch (SQLException e) {
		}

		return false;
	}

	protected synchronized Connection repairConnection(Connection retiree)
			throws SQLException {
		try {
			retiree.close();
		} catch (Exception e) {
			logger.error("repairConnection()", e);
		}

		int idx = getIndex(retiree);
		Connection conn = createConnection();
		aConnection[idx] = conn;
		return conn;
	}

	/**
	 * Connection???�나 ?�어?�다.
	 * 
	 * <P>
	 * ?�용?��? ?�는 ?�결???�으�??��? 반환?�다. 기존???�결??모두 ?�용?�는 경우 최�? 값까�??�결???�로 만든?? ?�용???�결??
	 * ?�으�??�결??반환???�까�?기다린다.
	 */
	public synchronized Connection getConnection() throws SQLException {

		Connection conn = null;

		if (m_bClosing) {
			return null;
		}

		try {
			while (this.cntUsing == this.aConnection.length) {
				if (logger.isDebugEnabled()) {
					logger.debug("wait connection(1). ");
				}
				wait(10000);
				if (logger.isDebugEnabled()) {
					logger.debug("wait connection(2). ");
				}
				if (m_bClosing) {
					return null;
				}
			}

			// if (this.cntUsing == this.aConnection.length) {
			// cntUsing --;
			// conn = this.aConnection[0];
			// System.arraycopy(this.aConnection, 1, this.aConnection, 0,
			// cntUsing);
			// this.aConnection[this.cntUsing] = null;
			// try {
			// conn.close();
			// throw new RuntimeException();
			// }
			// catch (Exception e) {
			// logger.error("DeadLock Connection closed. conn count : " +
			// (cntUsing +1),e);
			// }
			// }

			conn = this.aConnection[cntUsing];
			if (conn == null || !isConnected(conn)) {
				this.aConnection[cntUsing] = conn = this.createConnection();
			}
			cntUsing++;
		} catch (InterruptedException e) {
			logger.error("", e);
			;
		}

		return conn;
	}

	/**
	 * connection??반환 받는??
	 * 
	 * ?�결??종료??경우??그냥 버린?? ?�결??종료??경우 Connection#close�??�출????리턴?�다.
	 */
	public synchronized void returnConn(Connection retiree) {
		if (retiree == null) {
			return;
		}
		int idx = getIndex(retiree);
		if (idx < 0) {
			try {
				retiree.close();
			} catch (Exception e) {
				logger.error("", e);
				;
			}
			return;
		}
		if (idx < cntUsing) {
			Connection tmp = this.aConnection[--cntUsing];
			this.aConnection[cntUsing] = retiree;
			this.aConnection[idx] = tmp;
			this.notifyAll();
		}
	}

	/**
	 * 모든 ?�결??종료?�다.
	 */
	private synchronized void close() {
		Connection conn = null;
		m_bClosing = true;

		notifyAll();
		Thread.yield();

		for (int i = 0; i < aConnection.length; i++) {
			try {
				conn = aConnection[i];
				conn.close();
			} catch (Exception e) {
				;
			}
		}
	}

	public String logStatus() {

		StringBuffer buf = new StringBuffer();
		buf.append("?�태 ?�보");
		buf.append("\t ** ?�용 ?�결=" + cntUsing);
		buf.append("\t ** �?�� ?�결=" + (this.aConnection.length - cntUsing));

		logger.debug(buf.toString());

		return buf.toString();
	}
}
