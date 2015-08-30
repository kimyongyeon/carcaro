package carcaro;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import carcaro.bean.Customer;
import carcaro.bean.Driver;
import carcaro.dao.ChargeHistoryDAO;
import carcaro.dao.DriverDAO;
import carcaro.dao.CouponDAO;
import carcaro.service.CarcaroServiceJSON;

//ToDo Thread를 이용하여 for loop를 수행 할때에 중간에 삽입 삭제를 하면 Error
public class ServerProcess extends Thread{

	// Replacing Deprecated Thread.stop(); - 2012-01-12
	//	private volatile Thread blinker;

	private static final Logger logger = Logger.getRootLogger();

	// 대리기사
	private ConcurrentHashMap<String, Driver> drivers;

	// 사용자
	private static ConcurrentHashMap<String, Customer> customers;
	// chageHistoryDAO에서 사용하기 위해 GET함수로 뺀것.
	public static ConcurrentHashMap<String, Customer> getCustomers() {
		return customers;
	}

	//안심서비스를 받는 고객 정보
	private ConcurrentHashMap<String, Customer> safers;

	private ConcurrentLinkedQueue<HashMap<String, String>> requestList;

	private final double LIMITAREA = 10000; // 10km

	// private static MainServlet main;
	private CarcaroServiceJSON json;

	private ConnectionPool connPool;

	private DriverDAO driverDAO;


	private ChargeHistoryDAO chargeHistoryDAO;

	public ServerProcess(CarcaroServiceJSON json,
			ConcurrentHashMap<String, Driver> Drivers,
			ConcurrentHashMap<String, Customer> Customers,ConcurrentHashMap<String, Customer> Safers,
			ConcurrentLinkedQueue<HashMap<String, String>> requestList) {
		this.json = json;
		drivers = Drivers;
		customers = Customers;
		safers = Safers;
		this.requestList = requestList;
		try {
			connPool = ConnectionPool.getInstance();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		driverDAO = new DriverDAO(connPool);
		chargeHistoryDAO = new ChargeHistoryDAO(connPool);
		new CouponDAO(connPool);
	}



	@Override
	public void run() {
		//		Thread thisThread = Thread.currentThread();
		while (true) {
			// 명령에 따라 분기
			Map<String, String> req = null;
			synchronized (requestList) {
				if (requestList.isEmpty()) {
					try {
						requestList.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					req = requestList.poll();
				}
			}
			if (req != null) {
				String cmd = req.get("cmd");
				try {
					if ("requestDriver".equals(cmd)) {
						requestDriver(req);
					} else if ("suggestCharge".equals(cmd)) {
						suggestCharge(req);
					} else if ("startDriving".equals(cmd)) {
						startDriving(req);
					} else if ("updateDriverLoca".equals(cmd)) {
						updateDriverLoca(req);
					} /*
					 * else if ("endCustomerWaitingTime".equals(cmd)) {
					 * endCustomerWaitingTime(req); }
					 */else if ("cancelByCustomer".equals(cmd)) {
						 cancelByCustomer(req);
					 } else if ("cancelByDriver".equals(cmd)) {
						 cancelByDriver(req);
					 } else if ("endDriving".equals(cmd)) {
						 endDriving(req);
					 } else if ("restartDriverApp".equals(cmd)) {
						 restartDriverApp(req);
					 } else if ("restartCustomerApp".equals(cmd)) {
						 restartCustomerApp(req);
					 } else if ("cancelRequest".equals(cmd)) {
						 cancelRequest(req);
					 } else if ("cancelSuggest".equals(cmd)) {
						 cancelSuggest(req);
					 } else if ("updateUserLoca".equals(cmd)) {
						 updateUserLoca(req);
					 } else if ("disconnectedDriver".equals(cmd)) {
						 disconnectedDriver(req);
					 } else if ("disconnectedCustomer".equals(cmd)) {
						 disconnectedCustomer(req);
					 }
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("critical execption. !! ", e);
				} 
			}

		}
	}

	private void updateUserLoca(Map<String, String> req) {
		String cDevId = req.get("cDevId");

		double cLat = Double.parseDouble(req.get("cLat"));
		double cLng = Double.parseDouble(req.get("cLng"));
		long locTime = Long.parseLong( req.get( "locTime" ) );
		
		System.out.println("cLat,cLng"+cLat+", " +cLng);

		try {
			Customer customer = customers.get(cDevId);
			if (customer != null) {
				customer._setPosition(cLat, cLng, locTime);
				if (customer.getState() == Customer.DRIVING && customer.isSafeOn()) {
					json.safePushService(customer.getSafePhone(), cLat, cLng);
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * 요금제시 취소
	 * @param req
	 */
	private void cancelSuggest(Map<String, String> req) {

		String cDevId = req.get("cDevId");
		String driverId = req.get("driverId");

		Driver driver = drivers.get(driverId);
		Customer customer = customers.get(cDevId);
		if (driver != null) {
			driver.initState();
			if (customer != null) {
				customer.removeDriverList(driverId);
				json.cancelSuggest(customer, driverId , "가격제시를 취소하였습니다." , "");
			}
			searchCustomer(driverId);
		}
	}

	/**
	 * 고객이 서버에 대리기사를 요청한다. 요청자 고객
	 * 
	 * @param req
	 * @param resp
	 * @throws IOException
	 */
	private void requestDriver(Map<String, String> req) throws IOException {
		// 고객정보 받아올 것. (폰번호, 위치, 출발지, 도착지)
		String cName 		= req.get("cName");
		String cDevId 		= req.get("cDevId");
		String cPhone 		= req.get("cPhone");
		
		logger.debug("cLat:"+ req.get("cLat") + "cLng" + req.get("cLng"));

		double cLat 		= 0.0;// Double.parseDouble(req.get("cLat")); 
		double cLng 		= 0.0;// Double.parseDouble(req.get("cLng"));
		long locTime		= 0;
		try {
			// TODO 아이폰 쪽에서 위치데이터 손실.. 인코딩 문제로 보임.
			if ( req.get("cLat") != null && req.get("cLng")!=null){
				cLat = Double.parseDouble(req.get("cLat")); 
				cLng = Double.parseDouble(req.get("cLng"));
			}
			if ( req.get("locTime") != null ) {
				locTime = Long.parseLong(req.get("locTime"));
			}
			//			logger.debug("Double: cLat:"+cLat + "/cLng:"+cLng);
		}catch (NumberFormatException e){
			e.printStackTrace();
		}
		//		long CRequestTime 	= Long.parseLong(req.get("CRequestTime"));	
		//		long cRequestTime 	= System.currentTimeMillis();
		String cDst 		= req.get("cDst");
		String cSrc 		= req.get("cSrc");
		String cTransit 	= req.get("cTransit");			// CLX 경유지
		boolean safeOn 		= Boolean.parseBoolean(req.get("safeOn"));
		String safePhone 	= req.get("safePhone");
		String coupon_id 	= req.get("coupon_id");			// Coupon 번호를 받아온다. 고객이 쿠폰을 사용하지 않으면  NULL
		String auto 		= req.get("wantsAuto");			// Auto 오토스틱 여부, YES이면 오토.
		String customerOS	= req.get("OS");				// 고객 의 OS를 받아온다.


		// TODO client 현재 쪽에서 데이터가 안넘어오고 있음 체크해서 수정할것 기본 안드로이드로 입력.. 2012.01.18
		if ( auto == null || auto.length() == 0) auto = "YES";	// Default YES.
		if ( customerOS == null || customerOS.length() == 0) customerOS = Customer.OS_ANDROID;


		/* CUSTOMER INFO SET BEGIN */
		Customer customer = customers.get(cDevId);		// Put @ MainServlet
		customer.setCName(cName);
		customer.setSafeOn(safeOn);
		customer.setSafePhone(safePhone);
		customer.setPhoneNum(cPhone);
		customer.setSrc(cSrc);
		customer.setDst(cDst);
		customer.setTransit(cTransit);					// CLX 경유지 추가 2011.12.30
		customer._setPosition( cLat, cLng, locTime );
		customer.usesCoupon(coupon_id);					// CLX 고객이 쿠폰 사용하면 이항목에 쿠폰이 들어감. 쿠폰을 사용하지 않으면 빈칸임.
		customer.wantsAuto(auto);						// CLX 고객이 수동/오토 원하는 값을 저장한다. YES이면 오토.
		customer.setOS(customerOS);						// 고객의 OS
		customers.put(cDevId, customer);
		/* CUSTOMER INFO SET END */


		if(safeOn){
			safers.put(safePhone, customer);
		}
		int count = 0;

		for (Driver driver : drivers.values()) {
			// 대리기사의 상태가 NONE이며 대리기사가 일정 거리 이하에 있는지를 Check

			double distance = driver.isNear(cLat, cLng);
			int state = driver.getState();
			//미승인 상태에서도 보내야하기 때문에 상태체크
			double LArea = driver.getDist(); // 대리기사가 설정한 범위로 지정한다. 2012.03.18
			if ( LArea < 1000.0 ) LArea = LIMITAREA; // 설정값 없는 대리기사 보정. 2012.03.22
//			logger.debug("-----> Compensated LArea " + LArea + " VS : " + distance);
//			if (!driver.isRequestCustomer(cDevId) && (state == Driver.WAITING || state == Driver.ISNOTCERTIFIED) && LIMITAREA > distance) {
			if (!driver.isRequestCustomer(cDevId) && (state == Driver.WAITING || state == Driver.ISNOTCERTIFIED) && LArea > distance) {
				// 일정 범위 내의 운전자를 찾은 경우
				customer.setState(Customer.SEARCHING); // 고객의 상태를 SEARCHING으로 변형시켜준다
				// 서버에 고개정보(요청 시간, 위치 등등)를 저장한다.
				// 대리기사에게 전송하여 준다
				String driverId = driver.getDriverId();
				String deviceId = driver.getDevId();
				String driverOS = driver.getOS();
				json.requestDriver(driverId, customer, distance, "고객이 대리요청을 하였습니다." , "출발지 : " + customer.getSrc() + "  목적지 : "
						+ customer.getDst(), deviceId, driverOS);
				driver.putCustomerList(cDevId, customer);
				customer.putDriverList(driverId, driver);
				count++;
			}
		}
		logger.debug("몇명에게 요청이되나??    " + count);
	}

	/**
	 * 대리기사가 고객에게 요금을 제시한다.
	 * 
	 * @param req
	 * @param resp
	 */
	private void suggestCharge(Map<String, String> req) {

		String driverId = req.get("driverId");
		String cDevId = req.get("cDevId");
		int charge = Integer.parseInt(req.get("charge"));
		Customer customer = customers.get(cDevId);
		if (customer != null) {
			// 대리기사의 요금 요청이 가능 할 경우
			Driver driver = drivers.get(driverId);
			if (driver != null) {
				driver.suggest(charge, cDevId);

				// 사용자에게 대리기사의 요청정보를 보낸다(DriverPhoneNum , DriverPosition(거리),
				// charge(요금))
				json.suggestCharge(customer, driver , "기사가 요금을 제시 하였습니다.", "금액 : " + charge + "원   기사와의 거리 : "
						+ driver.isNear(customer.getLat(), customer.getLng()));
				//				json.respSuggestCharge(customer, driver);
			}
		}else {
			logger.debug("고객 정보가 존재하지 않습니다. 고객 driverID:"+driverId+ "/cDevID:" + cDevId);
		}
	}

	/**
	 * 사용자가 대리기사의 요청을 수락하였다.
	 * 
	 * @param req
	 * @param resp
	 */
	private void startDriving(Map<String, String> req) {
		String driverId = req.get("driverId");
		String cDevId = req.get("cDevId");
		// 여기까지 아직은 고객의 State가 WAITING
		Driver driver = drivers.get(driverId);
		Customer customer = customers.get(cDevId);
		if (customer != null && driver != null) {

			// 고객과 대리기사의 State를 DRIVING으로 전환
			customer.startDriving(driverId);
			driver.startDriving();

			// 대리기사와 고객에게 Driving Mode가 시작됨을 알림
			// 같은 고객을 선택한 다른 대리기사들에게 고객을 선택할 수 없음을 알린다.
			double distance = driver.isNear(customer.getLat(),
					customer.getLng());

			String safephone = null;
			String dst = "";
			if (customer.isSafeOn()) {
				safephone = customer.getSafePhone();
				//				dst = customer.getDst();
			}
			dst = customer.getDst();
			String src = customer.getSrc();
			json.respStartDriving(customer, distance, driver, safephone, dst, src ,"" ,"");
			json.startDriving(driver, customer , "대리운전이 시작 되었습니다." , "");


			// 수수료 차감 이전 endDriving->StartDriving 2012.01.07 
			// 수수료만 차감한다. 대리 기록은 나중에 END/CANCEL에서 .
			try {
				// 수수료 차감
				int charge = driverDAO.getDriverInfo(driverId).getChargeSum();
				boolean res = driverDAO.updateChargeSum(charge - Driver.FEE, driverId);

			} catch (SQLException e) {
				e.printStackTrace();
				logger.debug("Mainservlet_startDriving : " + e);
				logger.debug("대리요청 수락:  D:" + driver.getDriverId() + " C:" + customer.getPhoneNum());
			}


			// 고객 주위에 있던 기사들.
			ConcurrentHashMap<String, Driver> cDriverList = customer._getDriverList();

			for (Driver d : cDriverList.values()) {
				// 이 고객에게 요금제시를 한사람과 대기상태의 기사에게 요청 취소를 보낸다.
				if(( cDevId.equals(d.getCustomerNum()) && d.getState() == Driver.SUGGESTED ) ){ 
					// 대리기사의 상태가 SUGGEST->WAITING으로 된 상태
					// 대리기사가 다른 고객에세 요금 제시를 하였다가 고객이 취소를 하여 WAITING이 된 상태
					// 이때의 대리사는 WAITING이지만 고객의 정보를 가지고 있지는 않다

					json.refuseSuggestCharge(cDevId, d , "고객이 다른대리기사와 연결되었습니다." , "", true);
					d.removeCustomerList(cDevId);
					d.initState();
					searchCustomer(d.getDriverId());
					
					// 대기중 기사
				}else if ( (d.getState() == Driver.WAITING && d.isRequestCustomer(cDevId)) ){
					// 처읍부터 가격제시를 안한 기사
					json.refuseSuggestCharge(cDevId, d , "고객이 다른대리기사와 연결되었습니다." , "", false);
					d.removeCustomerList(cDevId);
					d.initState();
					searchCustomer(d.getDriverId());
				}
			}
			customer.removeAllDriver();
		}


	}

	/**
	 * 대리기사의 정보를 주기적으로 Update 한다.
	 * 
	 * @param req
	 * @param resp
	 */
	private void updateDriverLoca(Map<String, String> req) {
		String driverId = req.get("driverId");
		double dLat = Double.parseDouble(req.get("dLat"));
		double dLng = Double.parseDouble(req.get("dLng"));
		long locTime = Long.parseLong( req.get( "locTime" ) );
		String gDist = req.get("setDist");
		logger.debug("--------------->>> setDist: "+ gDist + "/id:"+driverId);
		double dist = LIMITAREA;	// 기본값 10km
		if ( gDist != null && gDist.length() > 0){
			// 킬로미터로 넘어오므로 미터로 변환 ..
			dist = 1000 * Double.parseDouble(gDist);
		}else {
			dist = LIMITAREA;
		}
		// 파싱불량 보정 1킬로미터 이내에서 보정
		if ( dist < 1.0 ) dist = LIMITAREA;

		Driver driver = drivers.get(driverId);
		if (driver != null) {
			driver.setPosition(dLat, dLng, locTime);
			driver.setDist(dist);
			searchCustomer(driverId);
		}
		
		// KYY, 실시간 기사좌표 업데이트 기능 추가
		/*try {
			boolean result = driverDAO.updateDriverLoca(String.valueOf(dLat), String.valueOf(dLng), driverId);
			if(result)
				System.out.println("위치정보 업데이트 성공"+result);
			else
				System.out.println("위치정보 업데이트 실패"+result);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}


	/**
	 * Search Customer
	 * @param driverId
	 */
	public void searchCustomer(String driverId){
		Driver driver = drivers.get(driverId);
		for (Customer customer : customers.values()) {
			// 대리기사의 상태가 NONE이며 대리기사가 일정 거리 이하에 있는지를 Check
			String cDevId = customer.getDevId();
			//			String cPhone = customer.getPhoneNum();

			double distance = driver.isNear(customer.getLat(),
					customer.getLng());
			if (LIMITAREA > distance) {
				// 일정 범위 내의 고객을 찾은 경우 대리기사에게 전송하여 준다

				// DPhoneNum 기사가 customer에게 이미 요금제시를 하였는가.
				int State = driver.getState();
				String driverOS = driver.getOS();

				if ((State == Driver.WAITING || State == Driver.ISNOTCERTIFIED)
						&& !driver.isRequestCustomer(cDevId)
						&& customer.getState() == Customer.SEARCHING) {
					json.requestDriver(driverId, customer, distance , "고객이 대리요청을 하였습니다." , "출발지 : " + customer.getSrc() + "  목적지 : "
							+ customer.getDst(), driver.getDevId(), driverOS);
					driver.putCustomerList(cDevId, customer);
					customer.putDriverList(driverId, driver);
				}
			}
		}
	}

	/**
	 * 대리기사가 강제로 상황을 종료한 경우
	 * 
	 * @param req
	 * @param resp
	 */
	private void cancelByDriver(Map<String, String> req) {
		String driverId = req.get("driverId");
		String cDevId = req.get("cDevId");
		Driver driver = drivers.get(driverId);
		if (driver != null) {

			
			
			Customer customer = customers.remove(cDevId);
			// DB에 취소 내역을 저장
			try {
				// 대리기사의 수수료를 복구
				int charge = driverDAO.getDriverInfo(driverId).getChargeSum();
				boolean res = driverDAO.updateChargeSum(charge + Driver.FEE, driverId);
				
				
				if (customer != null) {
					if(customer.isSafeOn()){
						safers.remove(customer.getSafePhone());
						// 안심서비스 on시 취소 알림
						json.cancelSafe(customer.getSafePhone());
					}

					chargeHistoryDAO.recordCharge(driver.getDriverId(), cDevId,
							customer.getPhoneNum(), Driver.CANCEL_DRIVER,
							customer.getSrc(), customer.getDst(),
							driver.getCharge());
				} else {
					chargeHistoryDAO.recordCharge(driver.getDriverId(), cDevId,
							"", Driver.UNKNOWN, "", "", driver.getCharge());
				}
			} catch (SQLException e) {
				e.printStackTrace();
				logger.debug("cancelByDriver / ERR : " + e);
			}
			driver.initState();
			searchCustomer(driverId);			
			json.cancelByDriver(driverId, customer);
			logger.debug("대리요청 취소:  D:" + driver.getDriverId() + " C:" + customer.getPhoneNum());
		}
	}

	/**
	 * 사용자가 강제로 대리운전을 종료한 경우
	 * 
	 * @param req
	 * @param resp
	 */
	private void cancelByCustomer(Map<String, String> req) {
		String cDevId = req.get("cDevId");
		String driverId = req.get("driverId");
		Driver driver = drivers.get(driverId);

		Customer customer = customers.remove(cDevId);

		// 기사가 완료하는 사이에 취소 올 경우 있음.
		if (driver != null && driver.getState() == Driver.DRIVING) {

			// DB에 취소 내역을 저장
			try {
				// 대리기사의 차감된 수수료 복구 
				int charge = driverDAO.getDriverInfo(driverId).getChargeSum();
				boolean res = driverDAO.updateChargeSum(charge + Driver.FEE, driverId);
				
				if (customer != null) {
					
					// 안심서비스 on시 취소 알림
					if (customer.isSafeOn()){
						json.cancelSafe(customer.getSafePhone());
						safers.remove(customer.getSafePhone());
					}

					chargeHistoryDAO.recordCharge(driver.getDriverId(), cDevId,
							customer.getPhoneNum(), Driver.CANCEL_CUSTOMER,
							customer.getSrc(), customer.getDst(),
							driver.getCharge());
				} else {
					chargeHistoryDAO.recordCharge(driver.getDriverId(), cDevId,
							"", Driver.UNKNOWN, "", "", driver.getCharge());
				}
			} catch (SQLException e) {
				e.printStackTrace();
				logger.debug("cancelByCustomer / ERR : " + e);
			}
			driver.initState();
			searchCustomer(driverId);


			// 대리기사에게 Driving Mode가 끝남을 알림
			json.cancelByCustomer(driver, customer , "고객이 대리운전을 취소하였습니다.", "");

		}
	}

	/**
	 * 정상적으로 대리 운전이 종료되었을 경우
	 * 
	 * @param req
	 * @param resp
	 */
	private void endDriving(Map<String, String> req) throws IOException {
		String driverId = req.get("driverId");
		String cDevId = req.get("cDevId");
		Driver driver = drivers.get(driverId);

		Customer customer = customers.remove(cDevId);

		// 고객이 취소하는 사이에 기사가 종료할 경우 있음.
		if (driver != null && driver.getState() == Driver.DRIVING) {


			// 대리기사의 충전금을 차감시킨다. DB에 차감 내역을 저장한다.
			// 수수료 차감은 StartDriving으로 이전됨 2012.01.07
			try{
				int charge = driverDAO.getDriverInfo(driverId).getChargeSum();
				boolean res = driverDAO.updateChargeSum(charge, driverId);
				if (res) {
					// businessList에 기록
					if (customer.isSafeOn())
						json.endSafe(customer.getSafePhone());
					chargeHistoryDAO.recordCharge(driverId, cDevId,
							customer.getPhoneNum(), Driver.SETTLEMENT,
							customer.getSrc(), customer.getDst(),
							driver.getCharge());
				} else {
					// TODO 요금 차금이 실패한 경우에 대한 처리
				}
			}catch (SQLException e){
				e.printStackTrace();
			}
			if(customer != null){
				if (customer.isSafeOn()) {
					safers.remove(customer.getSafePhone());
				}
				// 고객에게 Driving Mode가 끝남을 알림
				json.endDriving(driverId, customer);
			}

			driver.initState();
			searchCustomer(driverId);
		}
	}

	private void restartDriverApp(Map<String, String> req) {
		String driverId = req.get("driverId");
		Driver driver = drivers.get(driverId);
		int State = driver.getState();
		if (driver != null) {
			switch (State) {
			case Driver.WAITING:
				for (Customer customer : driver._getCustomerList().values()) {
					double cLat = customer.getLat();
					double cLng = customer.getLng();
					double distance = driver.isNear(cLat, cLng);
					json.requestDriver(driverId, customer, distance , "NOMSG" , "NOMSG", driver.getDevId(), driver.getOS());
				}
				break;
			case Driver.SUGGESTED: {

				//				for (Customer customer : driver._getCustomerList().values()) {
				//					double cLat = customer.getLat();
				//					double cLng = customer.getLng();
				//					double distance = driver.isNear(cLat, cLng);
				//					json.requestDriver(driverId, customer, distance);
				//				}
				Customer customer = customers.get(driver.getCustomerNum());
				if (customer != null) {
					json.respSuggestCharge(customer, driver , "NOMSG" , "NOMSG");
				}
				break;
			}
			case Driver.DRIVING:
				Customer customer = customers.get(driver.getCustomerNum());
				if (customer != null)
					json.startDriving(driver, customer , "NOMSG" , "NOMSG");
				break;
			}
		}
	}

	private void restartCustomerApp(Map<String, String> req) {
		String cDevId = req.get("cDevId");
		Customer customer = customers.get(cDevId);
		if (customer != null) {
			int State = customer.getState();
			switch (State) {
			case Customer.SEARCHING:
				for (Driver driver : customer._getDriverList().values()) {
					if (driver.getState() == Driver.SUGGESTED) {

						json.suggestCharge(customer, driver , "NOMSG", "NOMSG");
					}
				}
				break;
			case Customer.DRIVING:

				String driverId = customer.getDriverId();
				Driver driver = drivers.get(driverId);
				if (driver != null) {

					double distance = driver.isNear(customer.getLat(),customer.getLng());
					json.respStartDriving(customer, distance, driver, null,"", "" , "NOMSG" , "NOMSG");
				}
				break;
			}
		}

	}

	private void cancelRequest(Map<String, String> req) {

		String cDevId = req.get("cDevId");
		Customer customer = customers.remove(cDevId);

		if (customer != null) {

			if(customer.isSafeOn()){
				safers.remove(customer.getSafePhone());
			}

			ConcurrentHashMap<String, Driver> cDriverList = customer._getDriverList();

			for (Driver driver : cDriverList.values()) {

				// 이 고객에게 요금제시를 한사람과 대기상태의 기사에게 요청 취소를 보낸다.
				if(( cDevId.equals(driver.getCustomerNum()) && driver.getState() == Driver.SUGGESTED ) 
						|| ((driver.getState() == Driver.WAITING || driver.getState() == Driver.ISNOTCERTIFIED )&& driver.isRequestCustomer(cDevId))){
					//(driver.getState() == Driver.WAITING && driver.isRequestCustomer(cDevId))
					// 대리기사의 상태가 SUGGEST->WAITING으로 된 상태
					// 대리기사가 다른 고객에세 요금 제시를 하였다가 고객이 취소를 하여 WAITING이 된 상태
					// 이때의 대리사는 WAITING이지만 고객의 정보를 가지고 있지는 않다
					json.cancelRequest(cDevId, driver , "고객이 대리요청을 취소하였습니다", "");
					driver.removeCustomerList(cDevId);
					//미승인 상태시에는 상태변경은 하지 않고 초기화한다.
					if(driver.getState() == Driver.ISNOTCERTIFIED){
						driver.firstInitState();
					}else if(driver.getState() == Driver.SUGGESTED){
						driver.initState();
						searchCustomer(driver.getDriverId());
					}else{
						driver.initState();
					}
				}
			}
		}
	}

	private void disconnectedCustomer(Map<String, String> req) {
		//		String cDevId = req.get("cDevId");
		//		Customer customer = customers.get(cDevId);
		//		if(customer.isSafeOn()){
		//			safers.remove(customer.getSafePhone());
		//		}
		//		if (customer != null) {
		//			switch (customer.getState()) {
		//			case Customer.DRIVING:
		//				String driverId = customer.getDriverId();
		//				Driver driver = drivers.get(driverId);
		//				if (driver != null) {
		//					driver.setState(Driver.STANDBY);
		//					deleteCustomerOnServer(cDevId);
		//					driver.setCharge(0);
		//					driver.setCustomerNum(null);
		//					driver.removeAllCustomerList();
		//					// 대리기사에게 Driving Mode가 끝남을 알림
		//					json.disconnectedCustomer(driverId, cDevId);
		//					// 안심서비스 on시 취소 알림
		//					if (customer.isSafeOn())
		//						json.cancelSafe(customer.getSafePhone());
		//				}
		//				break;
		//			default:
		//				break;
		//			}
		//		}
	}

	private void disconnectedDriver(Map<String, String> req) {
		//		String driverId = req.get("driverId");
		//		Driver driver = drivers.get(driverId);
		//		if (driver != null) {
		//			switch (driver.getState()) {
		//			case Driver.DRIVING:	
		//				String cDevId = driver.getCustomerNum();
		//					driver.setState(Driver.STANDBY);
		//					Customer customer = customers.get(cDevId);
		//					deleteCustomerOnServer(cDevId);
		//					driver.setCharge(0);
		//					driver.setCustomerNum(null);
		//					driver.removeAllCustomerList();
		//					json.disconnectedDriver(driverId, customer);
		//					// 안심서비스 on시 취소 알림
		//					if (customer.isSafeOn())
		//						json.cancelSafe(customer.getSafePhone());
		//				break;
		//			default:
		//				break;
		//			}
		//		}
	}


}
