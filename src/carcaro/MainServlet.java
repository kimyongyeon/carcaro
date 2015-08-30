package carcaro;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import carcaro.bean.Customer;
import carcaro.bean.Driver;
import carcaro.dao.APNSDAO;
//import carcaro.dao.AdminDAO;
import carcaro.dao.AdminDAO;
import carcaro.dao.ChargeHistoryDAO;
import carcaro.dao.CouponDAO;
import carcaro.dao.DriverDAO;
import carcaro.dao.SettlementDAO;
import carcaro.service.CarcaroServiceJSON;
import carcaro.service.MqttConnectionListner;
import carcaro.util.Util;

public class MainServlet extends HttpServlet implements MqttConnectionListner {

	private static final Logger logger = Logger.getRootLogger();

	private static final long serialVersionUID = 1L;
	public static final double LIMITAREA = 10000; //10 km

	private static final String RespOK = "OK.";

	private static final int DRIVER_WAITING_TIME = 60000;
	private static final int CUSTOM_WATING_TIME = 60000;
	private static final int DISCONNECT_TIME = 600000;
	// Connection Pool
	private ConnectionPool connPool;

	// DAO Class
	private DriverDAO driverDAO;
	private SettlementDAO settlementDAO;
	private CouponDAO couponDAO;
	private AdminDAO adminDAO;
	private APNSDAO apnsDAO;

	// 대리기사
	private ConcurrentHashMap<String, Driver> drivers;
	public ConcurrentHashMap<String, Driver> getDrivers() {
		return drivers;
	}
	public void setDrivers(ConcurrentHashMap<String, Driver> drivers) {
		this.drivers = drivers;
	}

	// 사용자
	private ConcurrentHashMap<String, Customer> customers;

	//안심서비스를 받는 고객 정보
	private ConcurrentHashMap<String, Customer> safers;

	// private LinkedList<Map<String, String[]>> RequestList;
	private ConcurrentLinkedQueue<HashMap<String, String>> requestList;

	private ServerProcess mServerProcess1;
	private ServerProcess mServerProcess2;
	private ServerProcess mServerProcess3;
	private ServerProcess mServerProcess4;
	private ServerProcess mServerProcess5;

	private TimeCheck mTimeCheck;

	private MQTTService mqttService;

	private APNService	apnService;

	public static MainServlet main;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public MainServlet() {
		super();
		main = this;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		if (mqttService == null) {
			mqttService = new MQTTService();
			mqttService.setConnectionListner(this);
		}
		mqttService.start();

		if (apnService == null){
			apnService = new APNService(main.getServletContext().getRealPath("/"));
		}

		drivers = new ConcurrentHashMap<String, Driver>();
		customers = new ConcurrentHashMap<String, Customer>();
		safers = new ConcurrentHashMap<String, Customer>();
		// RequestList = new LinkedList<Map<String, String[]>>();

		requestList = new ConcurrentLinkedQueue<HashMap<String, String>>();

		CarcaroServiceJSON json = new CarcaroServiceJSON(this);

		mServerProcess1 = new ServerProcess(json, drivers, customers,safers,
				requestList);
		mServerProcess2 = new ServerProcess(json, drivers, customers,safers,
				requestList);
		mServerProcess3 = new ServerProcess(json, drivers, customers,safers,
				requestList);
		mServerProcess4 = new ServerProcess(json, drivers, customers,safers,
				requestList);
		mServerProcess5 = new ServerProcess(json, drivers, customers,safers,
				requestList);
		mTimeCheck = new TimeCheck();

		mServerProcess1.setDaemon(true);
		mServerProcess2.setDaemon(true);
		mServerProcess3.setDaemon(true);
		mServerProcess4.setDaemon(true);
		mServerProcess5.setDaemon(true);
		mTimeCheck.setDaemon(true);

		mServerProcess1.start();
		mServerProcess2.start();
		mServerProcess3.start();
		mServerProcess4.start();
		mServerProcess5.start();
		mTimeCheck.start();

		// Connection Pool, DB 초기화
		try {
			connPool = ConnectionPool.getInstance();
			driverDAO = new DriverDAO(connPool);
			settlementDAO = new SettlementDAO(connPool);
			couponDAO = new CouponDAO(connPool);
			adminDAO = new AdminDAO(connPool);
			apnsDAO = new APNSDAO(connPool);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Load drivers into memory on init.
		loadDriver();
	}

	private void loadDriver() {
		// TODO 기사가 새로 가입하거나, 할 떄에도 drivers.put()

		List<Driver> _drivers;
		try {
			_drivers = driverDAO.selectAll();
			for (Driver driver : _drivers) {
				//				if (driver.getDevId() != null && driver.getDevId().length() != 0) {
				driver.firstInitState();
//				logger.debug("DriverId : "+driver.getDriverId()+"/DEVID : "+driver.getDevId() + "/OS:"+driver.getOS());
				drivers.put(driver.getDriverId(), driver);
				//				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 수정전 : 웹에서 기사 승인 시 DB에서 승인된 기사 정보를 얻어와 메모리에 저장한다.
	 * 수정후 : 승인된 기사 리스트만 NONE으로 상태변경한다.
	 * WebpageSevlet.java에서 사용한다.
	 * @param driverIdList
	 */
	public void updateDriverList(String[] driverIdList){
		// db에서 읽어, drivers에 put 해줄 것.
		//		try {
		//			List<Driver> driverList = driverDAO.updateDriverList(driverIdList);
		//			
		//			for(Driver driver:driverList){
		//				drivers.put(driver.getDriverId(), driver);
		//			}
		//		} catch (SQLException e) {
		//			e.printStackTrace();
		//		}
		try {
			for(String driverId : driverIdList){
				Driver driver = drivers.get(driverId);

				if(driver == null){
					driver = driverDAO.getDriverInfo(driverId);
					drivers.put(driverId, driver);
				}
				driver.setState(Driver.WAITING);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 기사 가입시 List에 등록할때 사용한다.
	 * DriverDAO.registDriver에 사용
	 * @param driver
	 */
	public void addDriverList(Driver driver){
		drivers.put(driver.getDriverId(), driver);
	}
	
	
	@Override
	public void destroy() {

		if (mqttService != null) {
			mqttService.disconnect();
		}

		if (apnService != null){
			apnService.closeConnection();
		}

		if (mServerProcess1 != null) {
			mServerProcess1.stop();
		}
		if (mServerProcess2 != null) {
			mServerProcess2.stop();
		}
		if (mServerProcess3 != null) {
			mServerProcess3.stop();
		}
		if (mServerProcess4 != null) {
			mServerProcess4.stop();
		}
		if (mServerProcess5 != null) {
			mServerProcess5.stop();
		}
		if (mTimeCheck != null) {
			mTimeCheck.stop();
		}
		if (drivers != null) {
			drivers.clear();
			drivers = null;
		}

		if (customers != null) {
			customers.clear();
			customers = null;
		}

		if( safers != null){
			safers.clear();
			safers = null;
		}

		ConnectionPool.destory();

		super.destroy();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String cmd = req.getParameter("cmd");
		try {

			boolean Async = false;
			System.out.println("cmd: " + cmd);
			// 동기 처리할 것들.
			if("versionCheck".equals(cmd)){
				versionCheck(req, resp);
			}if("versionCheckiOS".equals(cmd)){
				versionCheckiOS(req, resp);
			} else if ("startSettlement".equals(cmd)) {
				startSettlement(req, resp);
			} else if ("registerDriver".equals(cmd)) {
				registerDriver(req, resp);
			} else if ("LoginDriver".equals(cmd)) {
				LoginDriver(req, resp);
			} else if ("getChargeSum".equals(cmd)) {
				getChargeSum(req, resp);
			} else if ("cancelRequest".equals(cmd)) {
				Async = cancelRequest(req, resp);
				resp.getWriter().print(RespOK);
			} else if ("cancelSuggest".equals(cmd)) {
				Async = true;
				resp.getWriter().print(RespOK);
			} else if ("recommendDriverId".equals(cmd)) {
				recommendDriverId(req, resp);
			} else if ("requestDriver".equals(cmd)) {
				Async = requestDriver(req, resp);
			} else if ("suggestCharge".equals(cmd)) {
				Async = suggestCharge(req, resp);
			} else if ("startDriving".equals(cmd)) {
				Async = startDriving(req, resp);
			} else if ("sendLike".equals(cmd)) {
				sendLike(req, resp);
			} else if ("sendDislike".equals(cmd)){
				sendDislike(req,resp);
			}
			else if ("cancelByDriver".equals(cmd)) {
				Async = true;
				resp.getWriter().print(RespOK);
			} else if ("cancelByCustomer".equals(cmd)) {
				Async = cancelByCustomer(req, resp);
				resp.getWriter().print(RespOK);
			} else if ("endDriving".equals(cmd)) {
				Async = true;
				resp.getWriter().print(RespOK);
			} else if ("updateDriverInfo".equals(cmd)) {
				updateDriverInfo(req, resp);
			} else if ("updateDriverPic".equals(cmd)){ // 대리기사 사진 업데이트
				updateDriverPic(req,resp);
			} else if ("updateDriverLoca".equals(cmd)) { // 대리기사 위치 업데이트
				Async = true;
				resp.getWriter().print(RespOK);
			} else if ("updateUserLoca".equals(cmd)) {
				String cDevId = req.getParameter("cDevId");
				try {
					Customer customer = customers.get(cDevId);
					if(customer != null){
						resp.getWriter().print(RespOK);
						Async = true;
					}else
						resp.getWriter().print("Fail");
				}catch (Exception e) {
					e.printStackTrace();
				}
			} else if ("restartDriverApp".equals(cmd)) {
				Async = restartDriverApp(req, resp);
				resp.getWriter().print(RespOK);
			} else if ("restartCustomerApp".equals(cmd)) {
				Async = restartCustomerApp(req, resp);
			}else if ("getSafeInformation".equals(cmd)) {
				getSafeInformation(req, resp);
			}else if("test_driverLocation".equals(cmd)){
				test_driverLocation(req, resp);
			}else if("test_launchApp".equals(cmd)){
				test_launchApp(req, resp);
			}else if ("updateDriverDevId".equals(cmd)){
				updateDriverDevId(req,resp);
			}else if ("checkResidentNo".equals(cmd)){
				checkResidentNo(req,resp);
			}
			/*else if ("reloadDrivers".equals(cmd)){
				reloadDrivers(req,resp);
			}*/
			
			// 수수료 차감 내역 
			else if ("getChargeFeeList".equals(cmd)){
				getChargeFeeList(req,resp);
			}

			else if ("driver_id_pw".equals(cmd)){
				driver_id_pw(req,resp);
			}

			// Coupon 
			else if("register_coupon".equals(cmd)){
				register_coupon(req, resp);
			}else if ("register_coupon_bulk".equals(cmd)){
				register_coupon_bulk(req,resp);
			}else if ("get_coupon".equals(cmd)){
				get_coupon(req,resp);
			}else if ("get_coupon_list".equals(cmd)){
				get_coupon_list(req,resp);
			}else if ("gift_coupon".equals(cmd)){
				gift_coupon(req,resp);				
			}else if ("delete_coupon".equals(cmd)){
				delete_coupon(req,resp);
			}else if ("use_coupon".equals(cmd)){
				use_coupon(req,resp);
			}else if ("modify_coupon".equals(cmd)){
				modify_coupon(req,resp);
			}else if ("get_coupon_amount".equals(cmd)){
				int amount = couponDAO.get_coupon_amount(req.getParameter("coupon_id"));
				resp.getWriter().write(amount);
			}else if ("get_settlement_list".equals(cmd)){
				get_settlement_list(req, resp);
			}
			/*else if ("settlement_coupon".equals(cmd)){
				settlement_coupon(req, resp);
			}
			 */

			 // TODO CLX 2011.12.26 QNA/NOTICE
			else if ("registerQna".equals(cmd)){
				registerQna(req,resp);
			}else if ("getQna".equals(cmd)){
				getQna(req,resp);
			}else if ("registerAns".equals(cmd)){
				//				resp.getWriter().print(req.getParameter("qid"));
				//				resp.getWriter().print(req.getParameter("answer"));
//				register_ans(req,resp);
			}else if ("getDetailQna".equals(cmd)){
				getQnaDetail(req,resp);
			}else if ("getNoticeCountAndLastNoticeDate".equals(cmd)){
				getNoticeCountAndLastNoticeDate(req,resp);
			}else if ("getNotice".equals(cmd)){
				getNotice(req,resp);
			}else if ("getNoticeDetail".equals(cmd)){
				getNoticeDetail(req,resp);
			}else if ("getHelp".equals(cmd)){
				getHelp(req,resp);
			}else if ("getHelpDetail".equals(cmd)){
				getHelpDetail(req,resp);
			}else if ("getYangjapa".equals(cmd)){
				getYangjapa(req,resp);
			}

			
			
			// APNS Implementation Test
			else if ("test_push".equals(cmd)){
				String deviceToken = req.getParameter("deviceToken");
				if ( deviceToken  == null || deviceToken.length() == 0 ){
					deviceToken = "ff63e6ac4705c4bf6e7008e38419e5a8e67f5bdf8280f195b593ea5dc4ccc0bc"; // SANDBOX
				}

				Customer customer = new Customer(deviceToken, 0, "iOS");
				JSONObject c = (JSONObject) JSONSerializer.toJSON(customer);
				JSONObject json = new JSONObject();
				if ( true ){
					json.put("currentTime", System.currentTimeMillis());
					json.put("cmd", "requestedByCustomer");
					json.put("distance", 100);
					json.put("customer", c);
					json.put("title" , "TEST TITLE");
					json.put("body", "TEST BODY");
					resp.getWriter().print("MessageBody Created: \n" + json.toString());
					try {
						resp.getWriter().print("\nRegistering in DB...");
						main.apnsMessage("Driver", deviceToken, json);
						resp.getWriter().print("\nDB Register Success...");
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
				resp.getWriter().print("Resp:OK");
			}else if ("getMessageBody".equals(cmd)){
				String mid = req.getParameter("mid");
				//				mid = "5";
				try{
					String ret = apnsDAO.getMessageBody(mid);
					resp.getWriter().print(ret);
				}catch (Exception e){
					e.printStackTrace();
				}
			}

			if (Async) {

				// 비동기 처리할 것들.
				synchronized (requestList) {
					HashMap<String, String> request = new HashMap<String, String>();

					@SuppressWarnings("rawtypes")
					Enumeration mEnum = req.getParameterNames();

					String key = null;
					String value = null;

					while (mEnum.hasMoreElements()) {
						key = (String) mEnum.nextElement();
						value = (new String(req.getParameter(key)) == null) ? ""
								: new String(req.getParameter(key));

						request.put(key, value);
					}

					// 명령에 따라 분기
					requestList.add(request);
					requestList.notify();
				}
			}

		} catch (SQLException e) {
			PrintWriter w = resp.getWriter();
			e.printStackTrace(w);
			w.close();
		}

	}







/*
	private void reloadDrivers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		logger.debug("reload Drivers");
		List<Driver> _drivers;
		try {
			_drivers = driverDAO.selectAll();
			for (Driver driver : _drivers) {
				//				if (driver.getDevId() != null && driver.getDevId().length() != 0) {
				driver.firstInitState();
				logger.debug("DriverId : "+driver.getDriverId()+"/DEVID : "+driver.getDevId() + "/OS:"+driver.getOS());
				drivers.put(driver.getDriverId(), driver);
				//				}
			}
			resp.getWriter().print("Reload Success");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}*/

	

	

	

	

	
	

	/**
	 * 대리기사의 디바이스 아이디를 업데이트 하는 전용
	 * @param req
	 * @param resp
	 * @throws IOException
	 */
	private void updateDriverDevId(HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		String input_id = req.getParameter("driverId");
		String devId 	= req.getParameter("deviceId");
		String OS 		= req.getParameter("OS");
		try {
			if ( input_id == null || input_id.length() == 0 ){
				resp.getWriter().print("Resp Fail:Invalid ID");
			}else if ( OS != null && OS.length()!=0){
				driverDAO.updateDriverDevice(input_id, devId, OS);
				Driver driv = drivers.get(input_id);
				driv.setDevId(devId);
				driv.setOS(OS);
				resp.getWriter().print("Resp OK");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
	}

	private void versionCheck(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		JSONObject json = new JSONObject();
		json.put("androidAppVersion", Global.ANDROID_APP_VERSION);
		json.put("androidMarketLink", Global.ANDROID_APP_MARKET_LINK);
		resp.getWriter().print(json.toString());

	}

	private void versionCheckiOS(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		JSONObject json = new JSONObject();
		json.put("iOSAppVersion", Global.IOS_APP_VERSION);
		json.put("iOSStoreLink", Global.IOS_APP_STORE_LINK);
		resp.getWriter().print(json.toString());

	}


	private boolean cancelByCustomer(HttpServletRequest req, HttpServletResponse resp) {
		String cDevId = req.getParameter("cDevId");
		return customers.containsKey(cDevId);
	}

	private boolean restartCustomerApp(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		String cDevId = req.getParameter("cDevId");

		boolean isExist = customers.containsKey(cDevId);
		JSONObject json = new JSONObject();
		if(isExist){
			json.put("Return", RespOK);
		} else {
			json.put("Return", "FAIL.");
		}
		resp.getWriter().print(json.toString());

		return isExist;
	}

	private boolean restartDriverApp(HttpServletRequest req,
			HttpServletResponse resp) {
		String driverId = req.getParameter("driverId");
		return drivers.containsKey(driverId);
	}

	private boolean cancelRequest(HttpServletRequest req, HttpServletResponse resp) {
		String cDevId = req.getParameter("cDevId");
		return customers.containsKey(cDevId);
	}

	private void getChargeSum(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, SQLException {

		String driverId = req.getParameter("driverId");
		Driver driver = driverDAO.getDriverInfo(driverId);

		JSONObject json = new JSONObject();
		json.put("chargeSum", driver.getChargeSum());
		json.put("likeScore", driver.getLikeScore());
		resp.getWriter().print(json.toString());
	}


	private void startSettlement(HttpServletRequest req,
			HttpServletResponse resp) {

		String driverId = req.getParameter("driverId");

		try {
			Writer writer = resp.getWriter();
			int oid = -1;

			oid = settlementDAO.startSettlement(driverId);
			JSONObject json = new JSONObject();
			json.put("oid", oid);
			writer.write(json.toString());

			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
			// -1이 리턴되면 비정상
		}

	}

	// 고객 -> 대리기사 추천
	private void recommendDriverId(HttpServletRequest req,
			HttpServletResponse resp) throws IOException, SQLException {

		String cDevId = req.getParameter("cDevId");
		String driverId = req.getParameter("driverId");
		String valueS = req.getParameter("value");
		Integer value = Integer.parseInt(valueS);
		resp.getWriter().print(RespOK);


		driverDAO.recommendDriverId(cDevId, driverId, value);
	}

	// 대리기사 회원가입
	private void registerDriver(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, SQLException {
		String driverId = req.getParameter("driverId");
		String passwd = req.getParameter("passwd");
		String name = req.getParameter("name");
		String residentNo = req.getParameter("residentNo");
		String phone = req.getParameter("phone");
		String authorizationNo = req.getParameter("authorizationNo");
		int career = Integer.parseInt(req.getParameter("career"));
		String company = req.getParameter("company");
		String email = req.getParameter("email");
		int licenseType = Integer.parseInt(req.getParameter("licenseType"));
		boolean licenseAuto = Boolean.parseBoolean(req.getParameter("licenseAuto"));
		String address = req.getParameter("addr");
		int agreeReceive = Integer.parseInt(req.getParameter("agree_receive"));
		String devId = req.getParameter("devId");
		String os = req.getParameter("OS");
		// 대리요청거리 저장
		String requestDistance = req.getParameter("requestDistance");
		
		logger.debug("Register Driver: driverId" + driverId + "// devId:" + devId + " // OS:" + os); // TODO log...
		if (os == null || os.length()==0) os = Driver.OS_ANDROID; 		// Android set to Default.
		if (devId.length() == 64) os = Driver.OS_IOS;					// if DevToken(64) set iOS

		Driver driver = new Driver(driverId, passwd, name, phone, residentNo, authorizationNo, email, 
				licenseType, licenseAuto, career, company, address, agreeReceive, os);
		driver.setDevId(devId);
		driver.setRequestDistance(requestDistance); // 대리요청거리 
		

		int ret = driverDAO.registDriver(driver);
		JSONObject json = new JSONObject();
		if(ret == DriverDAO.EXIST_ID){
			json.put( "Return","existId" );
		}else if ( ret == DriverDAO.EXIST_RESIDENT ){
			json.put( "Return", "ExistResident");
		}else if( ret == DriverDAO.REGIST_OK) {
			json.put( "Return", RespOK );
		}
		resp.getWriter().write( json.toString() );
	}

	// 대리기사 로그인
	private void LoginDriver(HttpServletRequest req, HttpServletResponse resp)
			throws SQLException, IOException {

		String input_id = req.getParameter("id");
		String input_pw = req.getParameter("pw");
		String devId 	= req.getParameter("deviceId");
		String OS 		= req.getParameter("OS");
		// String check = "";
		// Writer out;
		logger.debug("--------- LOGIN DRIVER: "+ input_id + "/" + devId + "/" +OS+ "/");

		HttpSession session = req.getSession();
		//
		// isConfirmed = -2;
		//
		int isConfirmed = driverDAO.LoginDriver(input_id, input_pw);
		JSONObject json = new JSONObject();

		if (isConfirmed == DriverDAO.LONGIN_OK || isConfirmed == DriverDAO.IS_NOT_CERTIFIED) { // 아이디, 비밀번호 일치
			
			// Update Driver Device on Login
			//  TODO remove if block after android market update.
			if ( devId != null && OS != null && devId.length()!=0 && OS.length()!=0){
				driverDAO.updateDriverDevice(input_id, devId, OS);
				Driver driv = drivers.get(input_id);
				driv.setDevId(devId);
				driv.setOS(OS);
			}
			
			session.setAttribute("id", input_id);

			Driver DInfo = driverDAO.getDriverInfo(input_id);

			json.put("Result", "RespOK");
			json.put("name", DInfo.getName());
			json.put("phone", DInfo.getPhoneNum());
			String advertisement = DInfo.getAdvertisement();
			if(advertisement==null) advertisement="";
			json.put("advertisement", advertisement);
			json.put("email", DInfo.getEmail());
			json.put("driverId", DInfo.getDriverId());
			String pic = DInfo.getPicture();
			if(pic == null) pic = "";
			json.put("picture", pic);
			json.put("chargeSum", DInfo.getChargeSum());
			json.put("starttime", DInfo.getStarttime());
			json.put("endtime", DInfo.getEndtime());
			json.put("OS", DInfo.getOS());
			json.put("likeScore", DInfo.getLikeScore());
			json.put("level", DInfo.getLevel());
			json.put("authorizationNo", DInfo.getAuthorizationNo());
			// KYY 
			json.put("authorizationName", DInfo.getAuthorizationName()); // 보험회사
			json.put("assurance_complete_date", DInfo.getAssurance_complete_date()); // 보험완료일
			json.put("requestDistance", DInfo.getRequestDistance()); // 대리요청거리

			
//			if(!drivers.containsKey(input_id))
//			drivers.put(input_id, DInfo);
		}/*else if(){
			session.setAttribute("id", input_id);

			Driver DInfo = driverDAO.getDriverInfo(input_id);
			DInfo.setState(Driver.ISNOTCERTIFIED);
			json.put("Result", "RespOK");
			json.put("name", DInfo.getName());
			json.put("phone", DInfo.getPhoneNum());
			String advertisement = DInfo.getAdvertisement();
			if(advertisement==null) advertisement="";
			json.put("advertisement", advertisement);
			json.put("email", DInfo.getEmail());
			json.put("driverId", DInfo.getDriverId());
			String pic = DInfo.getPicture();
			if(pic == null) pic = "";
			json.put("picture", pic);
			json.put("chargeSum", DInfo.getChargeSum());
			json.put("starttime", DInfo.getStarttime());
			json.put("endtime", DInfo.getEndtime());
			if(!drivers.containsKey(input_id))
				drivers.put(input_id,DInfo);
		}*/
		else
			json.put("Result", "RespFAIL");

		resp.getWriter().print(json.toString());
		// } else if (isConfirmed == RIGHT_ID_AND_WRONG_PW) { // 鍮꾨�踰덊샇媛���┛ 寃쎌슦
		// session.setAttribute("errormsg", "鍮꾨�踰덊샇媛���졇�듬땲�� �ㅼ떆 濡쒓렇���섏꽭��);
		// check = RIGHT_ID_AND_WRONG_PW + "";
		// } else if (isConfirmed == WRONG_ID) { // 아이디가 틀린 경우
		// session.setAttribute("errormsg", "아이디가 틀렸습니다. 다시 로그인 하세요");
		// check = WRONG_ID + "";
		// } else if (isConfirmed == NULL_ID) { // 아이디값이 null
		// session.setAttribute("errormsg", "로그인 해 주세요.");
		// check = NULL_ID + "";
		// } else if (isConfirmed == -1) {
		// session.setAttribute("errormsg",
		// "서버에 문제가 있어 로그인이 되지 않고 있습니다. 관리자에게 문의해 주세요.");
		// check = -1 + "";
		// }
		// out = resp.getWriter();
		// out.write(check);

	}

	// 대리기사 정보수정
	private void updateDriverInfo(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, IOException {
		String name = null;
		String phone = null;
		String ad = null;
		String driverId = null;
		String picture = null;
		String requestDistance = null;
		int workingHourFrom = -1;
		int workingHourTo = -1;
		boolean isWorking = false;
		String devId = "";
		String OS = "";
		try {
			req.setCharacterEncoding("UTF-8");
			// Create a factory for disk-based file items
			DiskFileItemFactory factory = new DiskFileItemFactory();
			// Set factory constraints
			factory.setSizeThreshold(1024 * 1024 * 10);
			factory.setRepository(new File(getServletContext().getRealPath(
					"/WEB-INF/uploadData")));
			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);
			// Set overall request size constraint
			upload.setSizeMax(-1);
			@SuppressWarnings("rawtypes")
			List parameters = upload.parseRequest(req);
			for (int i = 0; i < parameters.size(); i++) {
				FileItem fileItem = (FileItem) parameters.get(i);
				// 첨부파일 체크
				if (fileItem.isFormField()) {

					String fieldName = fileItem.getFieldName();
					if ("name".equals(fieldName)) {
						name = fileItem.getString("utf-8");
					} else if ("phone".equals(fieldName)) {
						phone = fileItem.getString("utf-8");
					} else if("advertisement".equals(fieldName)) {
						ad = fileItem.getString("utf-8");
					}else if ("driverId".equals(fieldName)) {
						driverId = fileItem.getString("utf-8");
					} else if ("workingHourFrom".equals(fieldName)) {
						workingHourFrom = Integer.parseInt(fileItem
								.getString("utf-8"));
					} else if ("workingHourTo".equals(fieldName)) {
						workingHourTo = Integer.parseInt(fileItem
								.getString("utf-8"));
					} else if ("isworking".equals(fieldName)) {
						isWorking = Boolean.parseBoolean(fileItem
								.getString("utf-8"));
					} else if ("deviceId".equals(fieldName)) {
						devId = fileItem.getString("utf-8");
					} else if ("requestDistance".equals(fieldName)) {
						requestDistance = fileItem.getString("utf-8");
					}else if ("OS".equals(fieldName)){
						OS = fileItem.getString("utf-8");
						if ( OS == null || OS.length() == 0 ){ 
							OS = Driver.OS_ANDROID; 
						}
					}
					
				} else {

					if (fileItem.getSize() > 0 && phone != null) {
						String fileName = fileItem.getName(); // Image
						int idx = fileName.lastIndexOf('\\');
						if (idx != -1) {
							fileName = fileName.substring(idx + 1);
						}

						File folder = new File("/carcaro/picture");
						if (!folder.exists()) {
							folder.mkdirs();
						}

						int dotposition = fileName.lastIndexOf(".");
						String extension = fileName.substring(dotposition, fileName.length());
						picture = phone  + '_' + System.currentTimeMillis() + extension;
						File file = new File(folder, picture); // driverId

						OutputStream out = new BufferedOutputStream(
								new FileOutputStream(file));
						InputStream in = new BufferedInputStream(
								fileItem.getInputStream());

						byte[] buf = new byte[8192];
						int len;
						while ((len = in.read(buf)) != -1) {
							out.write(buf, 0, len);
						}
						in.close();
						out.close();

					}
				}
			}
			logger.debug("--->> UpdateDriverInfo: driverId:" + driverId + "/devId:" + devId + "/OS:" + OS + "/Pic:" + picture);

			// 기기를 변경 했을 때, 기기 번호도 바꿔주어야 한다.
			Driver driver = drivers.get(driverId);
			if (driver != null) {
				driver.setDevId(devId);
				driver.setPhoneNum(phone);
				driver.setAdvertisement(ad);
				if( picture != null){
					driver.setPicture(picture);
				}
				int state  = driver.getState();
				if(state != Driver.ISNOTCERTIFIED){
					if (isWorking) {
						//						if (state == Driver.NONE)
						driver.setState(Driver.WAITING);
					} else {
						if(state == Driver.DRIVING){
							JSONObject json = new JSONObject();

							json.put("Return", "DrivingMode");
							resp.getWriter().print(json.toString());
						}else if(state == Driver.SUGGESTED){
							JSONObject json = new JSONObject();

							json.put("Return", "SuggestMode");
							resp.getWriter().print(json.toString());
						}else{
							driver.setState(Driver.WAITING);
							driver.removeAllCustomerList();
						}
					}
				}
			}
			
			driverDAO.updateDriverInfo(name, phone, ad, picture, isWorking,
					workingHourTo, workingHourFrom, driverId, devId, OS, requestDistance);
			JSONObject json = new JSONObject();
			logger.debug("---->> UpdateDriverDataBase:" + driverId);
			json.put("Return", RespOK);
			resp.getWriter().print(json.toString());
		} catch (Exception e) {
			logger.error("updateDriverInfo error.", e);
			try {
				JSONObject json = new JSONObject();
				json.put("Return", "Fail");
				resp.getWriter().print(json.toString());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

	}
	
	private void updateDriverPic(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException {
		String option = null;
		String driverId = null;
		String picture = null;
		try {
			req.setCharacterEncoding("UTF-8");
			// Create a factory for disk-based file items
			DiskFileItemFactory factory = new DiskFileItemFactory();
			// Set factory constraints
			factory.setSizeThreshold(1024 * 1024 * 10);
			factory.setRepository(new File(getServletContext().getRealPath(
					"/WEB-INF/uploadData")));
			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);
			// Set overall request size constraint
			upload.setSizeMax(-1);
			@SuppressWarnings("rawtypes")
			List parameters = upload.parseRequest(req);
			for (int i = 0; i < parameters.size(); i++) {
				FileItem fileItem = (FileItem) parameters.get(i);
				// 첨부파일 체크
				if (fileItem.isFormField()) {

					String fieldName = fileItem.getFieldName();
					if ("driverId".equals(fieldName)) {
						driverId = fileItem.getString("utf-8");
					}else if ("option".equals(fieldName)){
						option = fileItem.getString("utf-8"); // NULL: picture , "license": license Pic
					}
					
				} else {

					if (fileItem.getSize() > 0 && driverId != null) {
						logger.debug("Image file found... fieldName:" + fileItem.getName() +" /fileSize:"+ fileItem.getSize());
						String fileName = fileItem.getName(); // Image
						int idx = fileName.lastIndexOf('\\');
						if (idx != -1) {
							fileName = fileName.substring(idx + 1);
						}

						File folder = new File("/carcaro/picture");
						if (!folder.exists()) {
							folder.mkdirs();
						}

						int dotposition = fileName.lastIndexOf(".");
						String extension = fileName.substring(dotposition, fileName.length());
						picture = driverId  + '_' + System.currentTimeMillis() + extension;
						File file = new File(folder, picture); // driverId

						OutputStream out = new BufferedOutputStream(
								new FileOutputStream(file));
						InputStream in = new BufferedInputStream(
								fileItem.getInputStream());

						byte[] buf = new byte[8192];
						int len;
						while ((len = in.read(buf)) != -1) {
							out.write(buf, 0, len);
						}
						in.close();
						out.close();
						logger.debug("Image upload complete :"+ picture);
					}
				}
			}
			logger.debug("--->> UpdateDriverPic: driverId:" + driverId + " /Pic:" + picture + " /Option:"+ option);

			Driver driver = drivers.get(driverId);
			if (driver != null) {
				if( picture != null){
					driver.setPicture(picture);
					logger.debug("setPic:" + picture);
				}
			}

			// DB 업데이트
			driverDAO.updateDriverPic(driverId, picture, option);
			
			JSONObject json = new JSONObject();
			json.put("Return", RespOK);
			resp.getWriter().print(json.toString());
		} catch (Exception e) {
			logger.error("updateDriverInfo error.", e);
			try {
				JSONObject json = new JSONObject();
				json.put("Return", "Fail");
				resp.getWriter().print(json.toString());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}


	private boolean requestDriver(HttpServletRequest req,
			HttpServletResponse resp) {
		// 고객정보 받아올 것. (폰번호, 위치, 출발지, 도착지)

		try {
			String cDevId = req.getParameter("cDevId");
			String cPhone = req.getParameter("cPhone");
			String os 		= req.getParameter("OS");

			logger.debug("requestDriver() customer : " + cDevId + " cPhone : " + cPhone + "lat,lng" + req.getParameter("cLat")+","+req.getParameter("cLng"));
			Customer customer = customers.get(cDevId);
			if (customer != null) {
				JSONObject json = new JSONObject();
				json.put("Return", "HadRequestBefore");
				resp.getWriter().print(json.toString());
				// deleteCustomerOnServer(cDevId);
				return false;
			} else {
				long requestTime = System.currentTimeMillis();

				customer = new Customer(cDevId, requestTime, os);
				customers.put(cDevId, customer);

				JSONObject json = new JSONObject();
				json.put("Return", RespOK);
				json.put("requestTime", requestTime);
				resp.getWriter().print(json.toString());
				logger.debug("requestDriver() customer : " + cDevId + " cPhone : " + cPhone + "lat,lng" + req.getParameter("cLat")+","+req.getParameter("cLng"));
				return true;		// Asynchronous Request (ServerProcess)
			}

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (NumberFormatException e){
			e.printStackTrace();
			return false;
		}

	}

	private boolean suggestCharge(HttpServletRequest req,
			HttpServletResponse resp) {
		try {
			String driverId = req.getParameter("driverId");
			String cDevId = req.getParameter("cDevId");
			Customer customer = customers.get(cDevId);

			long CurTime = Calendar.getInstance().getTimeInMillis();
			JSONObject json = new JSONObject();

			int chargeSum = driverDAO.getDriverInfo(driverId).getChargeSum();
			Driver driver = drivers.get(driverId);
			//미승인 대리기사의 경우 승인 대기중임을 알림
			if(driver.getState() == Driver.ISNOTCERTIFIED){
				json.put("Return", "isNotCertifiedDriver");
				resp.getWriter().print(json.toString());
				return false;
			}

			// 대리기사의 충전금이 0원인 경우, 요금 제시를 하지 못하도록 한다.
			if (chargeSum == 0 || chargeSum - Driver.FEE < 0) {
				json.put("Return", "NoCharge");
				resp.getWriter().print(json.toString());
				return false;
			}
			// 대리기사가 요금제시를 하였을때 이미 한번 한 경우 "HadSuggestedBefore" 이런식으로 보내주어야 함
			// TODO 받는 부분에서도 이렇게 바꾸어주기
			if (customer != null && driver.getCustomerNum() != null &&
					driver.getCustomerNum().equals(cDevId)&& driver.getState() == Driver.SUGGESTED) {

				json.put("Return", "HadSuggestedBefore");
				resp.getWriter().print(json.toString());
				return false;
			}

			if (customer == null
					|| Math.abs(CurTime - customer.getRequestTime()) >= CUSTOM_WATING_TIME
					|| customer.getState() != Customer.SEARCHING) {
				// 사용자의 대기시간이 끝났거나 다른 대기리사를 선택한 경우
				// 대리기사에게 사용자에게 정보를 보낼 수 없음을 알린다.
				// 요금제시한 고객이 더이상 요금 제시할 상태가 아님을 알림

				json.put("Return", "CantSuggest");
				resp.getWriter().print(json.toString());
				return false;
			}
			json.put("Return", RespOK);
			resp.getWriter().print(json.toString());
			return true;
		} catch (SQLException e1) {
			e1.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private void getChargeFeeList(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException {
		String driverId = req.getParameter("driverId");
		
		JSONArray ret = driverDAO.getChargeFeeListDriver(driverId);
		
		JSONObject print = new JSONObject();
		
		print.put("ChargeFeeList", ret.toString());
		
		try {
			resp.getWriter().print(print.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}


	private void getSafeInformation(HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		String safePhone = req.getParameter("safePhone");
		Customer customer = safers.get(safePhone);
		// 가족번호, Customer
		JSONObject json = new JSONObject();
		if(customer != null){
			String driverId = customer.getDriverId();
			Driver driver = drivers.get(driverId);

			json.put("customer", customer);
			json.put("driver", driver);
			json.put("Return", RespOK);

		} else {
			json.put("Return", "NotSafer");
		}
		resp.getWriter().print(json.toString());
	}


	private boolean startDriving(HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		String driverId = req.getParameter("driverId");
		String cDevId = req.getParameter("cDevId");
		// 여기까지 아직은 고객의 State가 WAITING
		Driver driver = drivers.get(driverId);
		long curTime = Calendar.getInstance().getTimeInMillis();
		if(driver != null){
			long dDelayTime = driver.getSuggestTime();
			Customer customer = customers.get(cDevId);
			if(customer != null){

				if(customer.getState() == Customer.DRIVING){

					JSONObject json = new JSONObject();
					json.put("Return", "HadStartBefore");
					customer.setState(Customer.DRIVING);
					resp.getWriter().print(json.toString());
					return false;

				}else if (Math.abs(curTime - dDelayTime) >= DRIVER_WAITING_TIME
						|| driver.getState() != Driver.SUGGESTED) {
					// customers.remove(CPhoneNum);
					// 고객의 정보를 서버에서 비운다.
					// 대리기사의 대기시간이 끝났음을 알림
					
					
					// 가상콜 
					return true;
				} else {

					JSONObject json = new JSONObject();
					json.put("Return", "StartDriving");
					customer.setState(Customer.DRIVING);
					resp.getWriter().print(json.toString());
					return true;
				}
			}else {
				JSONObject json = new JSONObject();
				json.put("Return", "CustomerNull");
				resp.getWriter().print(json.toString());
				return false;
			}
		}else {
			JSONObject json = new JSONObject();
			json.put("Return", "ImpossibleDriving");
			json.put("Reason", "DriverNull");
			resp.getWriter().print(json.toString());
			return false;
		}		
	}

	private void sendLike(HttpServletRequest req, HttpServletResponse resp) throws SQLException {
		String driverId = req.getParameter("driverId");
		// String CPhoneNum = req.getParameter("CPhoneNum");

		// DB에서 대리기사 정보를 조회해 Like 점수를 올리기
		boolean ret = driverDAO.sendLike(driverId);
		// 점수 갱신 성공 시
		JSONObject json = new JSONObject();
		if (ret) {

			Driver driver = drivers.get(driverId);
			driver.likeScorePlus1();

			json.put("Return", "SendSuccess");
		} else {
			json.put("Return", "SendFailed");
		}
		try {
			resp.getWriter().print(json.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendDislike(HttpServletRequest req, HttpServletResponse resp) throws SQLException {
		String driverId = req.getParameter("driverId");
		// String CPhoneNum = req.getParameter("CPhoneNum");

		// DB에서 대리기사 정보를 조회해 Like 점수를 10점 감점
		boolean ret = driverDAO.sendDislike(driverId);
		// 점수 갱신 성공 시
		JSONObject json = new JSONObject();
		if (ret) {

			Driver driver = drivers.get(driverId);
			driver.likeScoreMinus10();

			json.put("Return", "SendSuccess");
		} else {
			json.put("Return", "SendFailed");
		}
		try {
			resp.getWriter().print(json.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	// TODO 2012.04.05
	/**
	 * 주민번호 중복체크
	 * @param req
	 * @param resp
	 * @throws SQLException 
	 */
	private void checkResidentNo(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException {
		String resNo = req.getParameter("residentNo");
		boolean ret = driverDAO.existResNo(connPool.getConnection(), resNo);
		JSONObject json = new JSONObject();
		if(ret == true){
			json.put( "Return","Exist");
		}else {
			json.put( "Return","OK" );
		}
		try {
			resp.getWriter().print(json.toString());
		} catch (IOException e) {
			e.printStackTrace();		
		}
	}
	
	

	/**
	 * 운전기사 아이디/비밀번호 찾기.
	 * @param req
	 * @param resp
	 * @throws IOException
	 * @throws SQLException
	 */
	private void driver_id_pw(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
		String name = req.getParameter("name");
		String sn = req.getParameter("sn");
		String type = req.getParameter("type");
		String rt = driverDAO.driver_id_pw(name,sn,type);

		JSONObject json = new JSONObject();

		json.put(type, rt);

		try {
			resp.getWriter().print(json.toString());
		} catch (IOException e) {
			e.printStackTrace();		
		}

	}

	private void test_launchApp(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		resp.sendRedirect( "carcaro://safe" );
	}


	/**
	 * 쿠폰을 등록한다
	 * @author cheeselemon
	 *
	 */
	private void register_coupon(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {

		// 2011.12.13 We only get <coupon_id, amount> and we will later update the customer info.
		String coupon_id = req.getParameter("coupon_id");
		//String cPhone = req.getParameter("customer_phone");
		//String cName = req.getParameter("customer_name");
		String amount = req.getParameter("amount");

		JSONObject json = new JSONObject();

		// if there is a duplicate COUPON_ID : resCount > 0
		int resCount = couponDAO.check_coupon_duplicate(coupon_id);


		if ( resCount > 0 ){
			// There is a duplicate coupon.
			json.put("Return", "Duplicate");
		}else{
			// There is no duplicate coupon, try SQL
			//int result = coupon.register_coupon(coupon_id, cPhone, cName, amount);
			int result = couponDAO.register_coupon(coupon_id, amount);
			if ( result == 0 ){
				json.put("Return", "Invalid Value");
			}else{ 
				// returned value is 1
				json.put("Return", "Success");

				// update coupon tracking system
				couponDAO.update_coupon_track(coupon_id, CouponDAO.COUPON_NEW, "reg_by_admin");
			}
		}

		// Output Insert Results
		try {
			resp.getWriter().print(json.toString());
		} catch (IOException e) {
			e.printStackTrace();		
		}



	}

	private void register_coupon_bulk(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {

		/*
		boolean isMultipart = ServletFileUpload.isMultipartContent(req);

		// Create a factory for disk-based file items
		FileItemFactory factory = new DiskFileItemFactory();

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);

		// Parse the request
		List items = upload.parseRequest(req);

		Iterator<E> iter = items.iterator();
		while (iter.hasNext()) {
		    FileItem item = (FileItem) iter.next();

		    if (item.isFormField()) {
		        //
		    } else {
		        processUploadedFile(item);
		    }
		}
		 */

		/*
		//BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
		BufferedReader reader = req.getReader();
		StringBuffer strBuff = new StringBuffer();
		int i = 0;
		for (String line; (line = reader.readLine()) != null; i++) {
			if ( i > 3)
		    strBuff.append(line+"\n");
		}

		try {
			resp.getWriter().print(strBuff);
		}catch ( IOException e){
			e.printStackTrace();
		}
		reader.close();
		 */



		//		JSONArray json = new JSONArray();

		BufferedReader bufRdr  = new BufferedReader(new InputStreamReader(req.getInputStream(), "euc-kr"));
		//req.getReader();

		String str = couponDAO.register_coupon_bulk(bufRdr);
		//int res = 

		//		json = couponDAO.register_coupon_bulk(bufRdr);

		try {
			//			resp.getWriter().print(json.toString());
			resp.getWriter().print(str);
		}catch ( IOException e){
			e.printStackTrace();
		}


	}

	/**
	 * 쿠폰에 고객정보를 입력한다.
	 * @author cheeselemon
	 *
	 */
	private void modify_coupon(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException{
		String coupon_id = req.getParameter("coupon_id");
		String cPhone = req.getParameter("customer_phone");
		String cName = req.getParameter("customer_name");

		JSONObject json = new JSONObject();

		int resCount = couponDAO.check_coupon_duplicate(coupon_id);
		if ( resCount > 0){
			// update coupon info
			couponDAO.update_coupon(coupon_id,cName,cPhone);
			json.put("Return", "Success");

			// update coupon tracking system
			couponDAO.update_coupon_track(coupon_id, CouponDAO.COUPON_MOD, cPhone);
		}else {
			json.put("Return", "Invalid Coupon:NO_COUPON");
		}

		try {
			resp.getWriter().print(json.toString());
		} catch (IOException e) {
			e.printStackTrace();		
		}


	}

	/**
	 * 쿠폰을 가져온다.
	 * @author cheeselemon
	 *
	 */
	private void get_coupon(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {

		String column = req.getParameter("column");
		String value = req.getParameter("value");

		// There may be many coupons, so create a JSONArray
		JSONArray jsonArr = new JSONArray();
		jsonArr = couponDAO.get_coupon(column,value);

		// StringBuffer To Create a JSONObj;
		JSONObject printJSON = new JSONObject();
		printJSON.put("c_coupon", jsonArr);


		// Output JSON Parsed string.
		try {
			resp.getWriter().print(printJSON.toString());
		}catch ( IOException e){
			e.printStackTrace();
		}


		/**
		JSONObject json = new JSONObject();
		json.put("Column", column);
		json.put("Value", value);

		resp.getWriter().print(json.toString());

		 **/

	}

	/**
	 * 쿠폰을 선물한다.
	 * @author cheeselemon
	 *
	 */
	private void gift_coupon(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {

		String coupon_id = req.getParameter("coupon_id");
		String from = req.getParameter("from");

		String to = req.getParameter("to");

		JSONObject json = new JSONObject();

		int res = couponDAO.check_coupon_duplicate(coupon_id);
		if ( res > 0){
			couponDAO.gift_coupon(coupon_id,from,to);
			json.put("Return", "Success");
			// Coupon tracking system update
			couponDAO.update_coupon_track(coupon_id, 1, to);
		}else{
			json.put("Return", "Invalid Coupon:NO_COUPON");
		}


		try {
			resp.getWriter().print(json.toString());
		}catch ( IOException e){
			e.printStackTrace();
		}

	}


	/**
	 * 쿠폰 목록을 가져온다.
	 * @author cheeselemon
	 *
	 */
	private void get_coupon_list(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {


		String fetchType = req.getParameter("fetchType"); //fetchType= "ALL", "NUSED", "USED"

		//resp.getWriter().write(fetchType);

		StringBuffer str = couponDAO.get_coupon_list(fetchType);

		try {
			resp.getWriter().write(str.toString());
		}catch ( IOException e){
			e.printStackTrace();
		}

		/* JSONArray jsonArr = new JSONArray();
		jsonArr = coupon.get_coupon_list(fetchType);

		JSONObject printJSON = new JSONObject();
		printJSON.put("l_coupon", jsonArr);


		// Output JSON Parsed string.
		try {
			resp.getWriter().print(printJSON.toString());
		}catch ( IOException e){
			e.printStackTrace();
		}
		 */
	}

	/**
	 * 쿠폰을 삭제한다.
	 * @author cheeselemon
	 *
	 */
	private void delete_coupon(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
		String coupon_id = req.getParameter("coupon_id");
		JSONObject json = new JSONObject();

		// Delete coupon
		int res = couponDAO.check_coupon_duplicate(coupon_id);
		if ( res > 0){
			couponDAO.delete_coupon(coupon_id);
			json.put("Return", "Success");
			// Coupon tracking system update
			couponDAO.update_coupon_track(coupon_id, CouponDAO.COUPON_DEL, "del_by_admin");
		}else{
			json.put("Return", "Invalid Coupon:NO_COUPON");
		}

		try {
			resp.getWriter().print(json.toString());
		}catch ( IOException e){
			e.printStackTrace();
		}

	}

	/**
	 * 쿠폰을 사용한다.
	 * @author cheeselemon
	 *
	 */
	private void use_coupon(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
		String coupon_id = req.getParameter("coupon_id");
		String driver_id = req.getParameter("driver_id");
		String customer_phone = req.getParameter("customer_phone");

		JSONObject json = new JSONObject();

		//------------------------------------------------------------------------------//
		//		 Use Coupon means the driver will own the coupon						//
		// 				AND the status of the coupon is changed to status="USED"		//
		//------------------------------------------------------------------------------//

		// When trying to work with coupons, always check these...
		int chkUsed 		= couponDAO.check_coupon_used(coupon_id);
		int chkDuplicate 	= couponDAO.check_coupon_duplicate(coupon_id);

		if ( chkUsed > 0 || chkDuplicate == 0){
			// If the coupon is already used or there's no matching coupon, return fail.
			if ( chkUsed > 0) 		json.put("Return", "Invalid Coupon:ALREADY_USED");
			if ( chkDuplicate == 0) json.put("Return", "Invalid Coupon:NO_COUPON");			
		}else {

			// 20111217 Settlement 테이블의 용도는 기사의 충전금액 결제 입니다.
			// 고객과 기사 간, 대리이력은 charge_history 입니다.
			// 20111217 CLX Coupon 가격을 기사의 충전금액으로 충전해주는 기능입니다.

			int oid = settlementDAO.startSettlement(driver_id);								// Create a new OID with given DriverID

			int res = couponDAO.settlement_coupon(coupon_id, oid);							// Using this OID, process settlement by coupon.

			couponDAO.use_coupon(coupon_id, driver_id, customer_phone);						// Set the coupon state= USED
			couponDAO.update_coupon_track(coupon_id, CouponDAO.COUPON_USE, driver_id);		// Update coupon tracking system
			Driver driver = driverDAO.getDriverInfo(driver_id);								// Get my driver
			int chargeSum = driver.getChargeSum() + couponDAO.get_coupon_amount(coupon_id); // Add Coupon Amount to ChargeSum
			driverDAO.updateChargeSum(chargeSum, driver_id);								// Update ChargeSum.

			json.put("oid", oid);

			if (res> 0) json.put("Return", "Success");
			else json.put("Return", "Fail");
		}

		try {
			resp.getWriter().print(json.toString());
		}catch ( IOException e){
			e.printStackTrace();
		}

	}


	// Settlement List W/ Coupon
	private void get_settlement_list(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
		String driver_id = req.getParameter("driver_id");

		JSONArray jsonArr = new JSONArray();
		jsonArr = couponDAO.get_settlement_list(driver_id);

		// StringBuffer To Create a JSONObj;
		JSONObject printJSON = new JSONObject();
		printJSON.put("settlement", jsonArr);


		// Output JSON Parsed string.
		try {
			resp.getWriter().print(printJSON.toString());
		}catch ( IOException e){
			e.printStackTrace();
		}


	}

	/**
	 * NOT USED ANYMORE
	 * @author cheeselemon
	 *
	 */
	/*
	private void settlement_coupon(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {

		String coupon_id = req.getParameter("coupon_id");
		String driver_id = req.getParameter("driver_id");
		JSONObject json = new JSONObject();
		int res = 0;

		int coupon_available = couponDAO.check_coupon_duplicate(coupon_id);// 1 if there's coupon available

		if (coupon_available >0){
			coupon_available = couponDAO.check_coupon_used(coupon_id);// 0 if not used
		}else {
			json.put("Return", "Failed:NO_COUPON");
			try {
				resp.getWriter().print(json.toString());
			}catch ( IOException e){
				e.printStackTrace();
			}
			return;
		}

		if  (coupon_available == 0){

			// Get a New OID

			if ( res == 0 ){
				json.put("Return", "Failed");
			}else {
				json.put("Return", "Success");
				// Use the coupon as a driver
				couponDAO.use_coupon(coupon_id, driver_id, driver_id);
			}

			try {
				resp.getWriter().print(json.toString());
			}catch ( IOException e){
				e.printStackTrace();
			}
		}else {
			json.put("Return", "Failed:ALREADY_USED");
			try {
				resp.getWriter().print(json.toString());
			}catch ( IOException e){
				e.printStackTrace();
			}
		}

	}
	 */

	// END COUPON

	
	// BEGIN QNA
	private void registerQna(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException{
		String 	title 		=req.getParameter("title");
		String 	desc 		=req.getParameter("desc");
//		desc = desc.replaceAll("\"", "\\\"");
		String 	user_phone 	=req.getParameter("user_phone");

		int ret = adminDAO.registerQna(title, desc, user_phone);

		resp.getWriter().print(desc);
		resp.getWriter().print(ret);
	}


	private void getQna(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException, ServletException {
		String usr 				=req.getParameter("usr_phone");
		String option 			=req.getParameter("option"); 		// "JSON"/"HTML"
		String option2 			=req.getParameter("option2");		// "ALL"/NULL
		
		
		
		JSONArray jsonArr = adminDAO.getQna(usr,option2);


		if ( "HTML".equals(option)){
			req.setAttribute("result", jsonArr);
			req.getRequestDispatcher("./reg_list.jsp").forward(req, resp);
			return;
		}else if ( "JSON".equals(option)){
			try {
				JSONObject js = new JSONObject();
				js.put("qnaList", jsonArr);
				resp.getWriter().print(js.toString());
			}catch (IOException e){
				e.printStackTrace();
			}
		}
	}

	
	private void getQnaDetail(HttpServletRequest req, HttpServletResponse resp) throws SQLException {
		String qid 				= req.getParameter("qid");
		JSONObject json 		= new JSONObject();
	
		json = adminDAO.getQnaDetail(qid);
		
		try {
			resp.getWriter().print(json.toString());
		}catch (IOException e){
			e.printStackTrace();
		}
	}

	private void getNotice(HttpServletRequest req, HttpServletResponse resp) {
		
		JSONArray jsonArr = new JSONArray();
		
		try {
			jsonArr = adminDAO.getNotice();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		JSONObject json = new JSONObject();
		json.put("notice", jsonArr);
		
		// 출력
		try {
			resp.getWriter().print(json.toString());
		}catch (IOException e){
			e.printStackTrace();
		}
		
	}
	
	private void getNoticeDetail(HttpServletRequest req,
			HttpServletResponse resp) {
		String no				= req.getParameter("no");
		JSONObject json 		= new JSONObject();
		
		try {
			json = adminDAO.getNoticeDetail(no);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try {
			resp.getWriter().print(json);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	
	

	private void getNoticeCountAndLastNoticeDate(HttpServletRequest req, HttpServletResponse resp) {
		int count 			= 0;
		String noticeDate 	= null;
		
		try {
			count = adminDAO.getNoticeCount();
			noticeDate = adminDAO.getLastNoticeDate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		JSONObject json = new JSONObject();
		
		json.put("noticeCount", count);
		json.put("lastNoticeDate", noticeDate);
		
		try {
			resp.getWriter().print(json);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	private void getHelp(HttpServletRequest req, HttpServletResponse resp) {
		JSONArray jsonArr = new JSONArray();
		
		try {
			jsonArr = adminDAO.getHelp();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		JSONObject json = new JSONObject();
		json.put("help", jsonArr);
		
		// 출력
		try {
			resp.getWriter().print(json.toString());
		}catch (IOException e){
			e.printStackTrace();
		}
	}

	private void getHelpDetail(HttpServletRequest req, HttpServletResponse resp) {
		// TODO Auto-generated method stub
		String hNO				= req.getParameter("hNO");
		JSONObject json 		= new JSONObject();
		
		try {
			json = adminDAO.getHelpDetail(hNO);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try {
			resp.getWriter().print(json);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void getYangjapa(HttpServletRequest req, HttpServletResponse resp) {
		JSONArray jsonArr = new JSONArray();
		
		try {
			jsonArr = adminDAO.getYangjapa();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		JSONObject json = new JSONObject();
		json.put("yangjapa", jsonArr);
		
		// 출력
		try {
			resp.getWriter().print(json.toString());
		}catch (IOException e){
			e.printStackTrace();
		}
	}
	
	

	@SuppressWarnings("unused")
	private void register_ans(HttpServletRequest req, HttpServletResponse resp) throws SQLException {
		String qid 				=req.getParameter("qid");
		String answer 			=req.getParameter("answer");
		answer 					=answer.replace("\"","\\\"");
		Connection conn 		=null;
		PreparedStatement stmt 	=null;
		ResultSet rs 			=null;
		StringBuffer sql 		=new StringBuffer();
		int isOK 				=0;

		try {
			// Connection pool 연결
			conn = connPool.getConnection();

			//  sql query 
			sql.append(" UPDATE usr_qna SET answer=?, answered='Y', answer_date=NOW() WHERE qid = ?");
			stmt = conn.prepareStatement(sql.toString());
			stmt.setString(1,	answer);
			stmt.setInt(2,		Integer.parseInt(qid));
			stmt.execute();
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

		try{
			resp.getWriter().print(isOK);
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	private void test_driverLocation(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		Writer w = resp.getWriter();
		w.write("<head>");
		w.write("<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>");
		w.write("</head>");
		DateFormat df = new SimpleDateFormat( Util.DF);

		Collection<Driver> d = drivers.values();

		w.write("<p>기사</p>");
		for (Driver driver : d) {

			String location = driver.getLat() + "+" + driver.getLng();
			w.write("<a target=_blank href='http://maps.google.co.kr/maps?hl=ko&q=");
			w.write(location);
			w.write("'>");
			w.write(df.format( new Date(driver._getLocTime()) ));
			w.write(" " + driver.getDriverId() + "  ");
			w.write(location);
			w.write("</a><br/>");
		}

		Collection<Customer> c = customers.values();

		w.write("<p>고객</p>");
		for (Customer customer : c) {
			String location = customer.getLat() + "+" + customer.getLng();
			w.write("<a target=_blank href='http://maps.google.co.kr/maps?hl=ko&q=");
			w.write(location);
			w.write("'>");
			w.write(df.format( new Date(customer._getLocTime()) ));
			w.write(" " + customer.getCName() + "  ");
			w.write(location);
			w.write("</a><br/>");
		}
		w.flush();
		w.close();

	}

	public void mqttMessage(String select ,String topic, JSONObject json) {

		topic = "/"+ select +"/" + topic;

		// push메시지의 retain과 구분하기 위한 유니크한 값 추가.
		json.put( "uniqueKey", System.currentTimeMillis() );
		String message = json.toString();
		mqttService.publishToTopic(topic, message);

		logger.debug("MainServlet.mqttMessage() topic : " + topic
				+ "\t message : " + message);

	}

	/**
	 * Apple Push Notification Service Implementation
	 * @param select
	 * @param receiverToken : 수신자의 deviceToken
	 * @param json
	 * @return mid
	 */
	public int apnsMessage(String select, String receiverToken, JSONObject json) {
		String receiver = "/"+ select +"/" + receiverToken;
		int mid = 0;
		json.put( "uniqueKey", System.currentTimeMillis() ); // TODO 필요업으면 제거.
		String title = json.getString("title");		// 간단한 제목 
		String body = json.getString("body");		// 자세한 내용
		String message = json.toString();			// 실 메시지는 DB에 저장
		try {
			mid = apnsDAO.createMessageBody(receiver,message);	// DB에 메시지 전문 저장.

			if ( mid == 0){
				// 실패시 어떻게 할 것인가?
				logger.debug("SEND FAIL : " + receiverToken
						+ "\t message : " + message);
			}else{
				apnService.sendPushNotification(mid,title,body, receiverToken);				// PUSH알림 전송
			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.debug("SQLException: "+e.getMessage());
		}


		logger.debug("Successfully Sent- topic : " + receiverToken
				+ "\t message : " + message);
		return mid;	
	}
	
	public int apnsMessageWithoutAlert(String select, String receiverToken, JSONObject json) {
		String receiver = "/"+ select +"/" + receiverToken;
		int mid = 0;
		json.put( "uniqueKey", System.currentTimeMillis() ); // TODO 필요업으면 제거.
		String title = json.getString("title");		// 간단한 제목 
		String body = json.getString("body");		// 자세한 내용
		String message = json.toString();			// 실 메시지는 DB에 저장
		try {
			mid = apnsDAO.createMessageBody(receiver,message);	// DB에 메시지 전문 저장.

			if ( mid == 0){
				// 실패시 어떻게 할 것인가?
				logger.debug("SEND FAIL : " + receiverToken
						+ "\t message : " + message);
			}else{
				// 알림 없는 메시지 전송
				apnService.sendPushNotificationWithoutAlert(mid,title,body, receiverToken);				// PUSH알림 전송
			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.debug("SQLException: "+e.getMessage());
		}


		logger.debug("Successfully Sent- topic : " + receiverToken
				+ "\t message : " + message);
		return mid;	
	}
	

	/**
	 * 대리기사의 대기시간이 끝났다.
	 * 
	 * @param req
	 * @param resp
	 */
	private void endDriverWaitingTime(String driverId) {

		Driver driver = drivers.get(driverId);
		if(driver != null){
			String cDevId = driver.getCustomerNum();
			Customer customer = customers.get(cDevId);

			if (customer != null)
				customer.removeDriverList(driverId);
			driver.initState();
			driver.removeCustomerList(cDevId);
		}
	}

	/**
	 * 고객의 대기시간이 끝났다.
	 * 
	 * @param req
	 * @param resp
	 */
	private void deleteCustomerOnServer(String _cDevId) {
		String cDevId = _cDevId;
		Customer customer = customers.get(cDevId);
		if(customer != null){
			ConcurrentHashMap<String, Driver> CDriverList = customer
					._getDriverList();
			logger.debug("deleteCustomerOnServer Customer PhoneNum : "
					+ customer.getPhoneNum());
			for (Driver driver : CDriverList.values()) {

				if (driver.isRequestCustomer(cDevId)) {
					driver.removeCustomerList(cDevId);
					logger.debug("Delete Customer : " + cDevId
							+ "  Driver Phone Num : " + driver.getPhoneNum());
				}
			}
			customers.remove(cDevId);
			if(customer.isSafeOn()){
				safers.remove(customer.getSafePhone());
			}
		}
	}

	/**
	 * 고객의 남은 시간을 체크한다.
	 */
	private void checkCustomerTime() {
		for (Customer customer : customers.values()) {
			/* 고객의 남은 시간을 체크하여 시간이 다 되었으면 상태를 바꾼다. */
			// 다른 대리기사들에게 고객을 선택할 수 없음을 알린다.
			if(customer.getState() != Customer.DRIVING){
				long CurTime = System.currentTimeMillis();
				long CDelayTime = customer.getRequestTime();
				logger.debug("Customer PhoneNum : " + customer.getPhoneNum()
						+ "   " + Math.abs(CurTime - CDelayTime));
				if (customer.getState() == Customer.SEARCHING
						&& Math.abs(CurTime - CDelayTime) >= CUSTOM_WATING_TIME)
					deleteCustomerOnServer(customer.getDevId());
			}/*else{
				long disconnecedtime = customer.getDisconnectingtime(); 
				if(disconnecedtime != 0 && System.currentTimeMillis()-disconnecedtime >= DISCONNECT_TIME){
					//TODO 고객의 연결이 끊겼을 시 수행하여야 하는 작업
					// 그리고, 고객, 기사 모두에게 메시지를 보내줘야 함.
				}
			}*/
		}
	}

	/**
	 * 대리기사의 남은 시간을 체크한다.
	 */
	private void checkDriverTime() {
		for (Driver driver : drivers.values()) {
			if (driver != null) {
				/* 대리기사의 남은 시간을 체크하여 시간이 다 되었으면 상태를 바꾼다. */
				long CurTime = System.currentTimeMillis();
				long DDelayTime = driver.getSuggestTime();

				// Driver의 상태가 WAITING인데, 고객을 가지고 있는 경우가 있다.
				// 어디서 고객을 클리어 해줘야 하나?
				if (driver.getState() == Driver.SUGGESTED
						&& Math.abs(CurTime - DDelayTime) >= DRIVER_WAITING_TIME) {
					endDriverWaitingTime(driver.getDriverId());
				}
				//				long disconnecedtime = driver.getDisconnectingtime(); 
				//				if(disconnecedtime != 0 && System.currentTimeMillis()-disconnecedtime >= DISCONNECT_TIME){
				//					//TODO 대리기사의 연결이 끊겼을 시 수행하여야 하는 작업
				//					// 끊어진지 10분이 지나면, 기사는 운행안함
				//					// 그리고, 고객, 기사 모두에게 메시지를 보내줘야 함.
				//				}
			}
		}
	}


	private class TimeCheck extends Thread {

		public TimeCheck(){
			setName("Time Check Thread.");
		}

		public void run() {
			while (true) {

				checkCustomerTime();
				checkDriverTime();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}


	@Override
	public void mqttConnected(String sender) {
		int index = sender.lastIndexOf("/");
		String sendernum = sender.substring(index+1, sender.length());;
		if(sender.startsWith("/Customer/")){
			Customer customer = customers.get(sendernum);
			if(customer != null){
			}
		}else{
			Driver driver = drivers.get(sendernum);
			if(driver != null){
			}
		}
	}

	@Override
	public void mqttDisconnected(String sender) {
		HashMap<String, String> request = new HashMap<String, String>();
		int index = sender.lastIndexOf("/");
		String sendernum = sender.substring(index+1, sender.length());;
		if(sender.startsWith("/Customer/")){
			Customer customer = customers.get(sendernum);
			if(customer != null){
				request.put("cmd", "disconnectedCustomer");
				request.put("cDevId", sendernum);
			}
		}else{
			Driver driver = drivers.get(sendernum);
			if(driver!=null){
				request.put("cmd", "disconnectedDriver");
				request.put("driverId", sendernum);
			}
		}
		synchronized (requestList) {
			requestList.add(request);
			requestList.notify();
		}
	}

}
