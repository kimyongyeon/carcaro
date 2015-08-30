package carcaro.service;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import carcaro.MainServlet;
import carcaro.bean.Customer;
import carcaro.bean.Driver;

public class CarcaroServiceJSON {
	
	private MainServlet main;
	private String Customer = "Customer";
	private String Driver = "Driver";
	private String Safe = "Safe";
	
	public CarcaroServiceJSON(MainServlet main){
		this.main = main;
	}

	
/**
 * 대리요청 JSON메시지.
 * iOS Implementation Complete
 * @param driverId
 * @param customer
 * @param distance
 * @param title
 * @param body
 * @param deviceId
 */
	public void requestDriver(String driverId,Customer customer, double distance , String title , String body, String deviceId, String driverOS){
		
		
		// 고객정보 (OS정보 포함)
		JSONObject c = (JSONObject) JSONSerializer.toJSON(customer);
		
		// 메시지 내용물		
		JSONObject messageBody = new JSONObject();
		messageBody.put("currentTime", System.currentTimeMillis());
		messageBody.put("cmd", "requestedByCustomer");
		messageBody.put("distance", distance);
		messageBody.put("customer", c);
		messageBody.put("title" , title);
		messageBody.put("body", body);
		
		// OS별로 분기하여 푸시알림..
		// 수신할 Driver의 OS가 iOS일 경우 APNS
		// 수신할 Driver의 OS가 Android일 경우 MQTT
		if ( isiOS(driverOS) ){
			main.apnsMessage(Driver, deviceId, messageBody);
		}else{
			main.mqttMessage(Driver, driverId, messageBody);
		}
	}

	
	/**
	 * 대리기사의 요금 제시를 고객에게 보낸다.
	 * iOS Implementation Complete
	 * @param customer
	 * @param driver
	 * @param title
	 * @param body
	 */
	public void suggestCharge(Customer customer, Driver driver, String title , String body){
		// OS포함한 고객/기사 정보
		JSONObject d = (JSONObject) JSONSerializer.toJSON(driver);
		JSONObject c = (JSONObject) JSONSerializer.toJSON(customer);
		
		double distance = driver.isNear(customer.getLat(), customer.getLng());
		
		JSONObject json = new JSONObject();
		json.put("cmd", "suggestChargeByDriver");
		json.put("currentTime", System.currentTimeMillis());
		json.put("distance", distance);
		json.put("driver", d);
		json.put("customer", c);
		json.put("title" , title);
		json.put("body", body);
		// check the receiver's OS
		if ( isiOS(customer.getOS()) ){
			main.apnsMessage(Customer, customer.getDevId(), json);
		}else{
			main.mqttMessage(Customer, customer.getDevId(), json);
		}
		
	}

	/**
	 * 고객이 대리기사를 선택했을 시 대리기사에게 대리운전이 시작되었음을 알린다.
	 * @param driver
	 * @param customer
	 * @param title
	 * @param body
	 */
	public void startDriving(Driver driver, Customer customer,String title , String body){
		
		JSONObject c = (JSONObject) JSONSerializer.toJSON(customer);
		JSONObject messageBody = new JSONObject();
		messageBody.put("cmd", "startDrivingByCustomer");
		messageBody.put("currentTime", System.currentTimeMillis());
		messageBody.put("customer", c);
		messageBody.put("callPrice", driver.getCharge());
		messageBody.put("distance", driver.isNear(customer.getLat(), customer.getLng()));
		messageBody.put("title" , title);
		messageBody.put("body", body);
		
		if ( isiOS(driver.getOS()) ){
			main.apnsMessage(Driver, driver.getDevId(), messageBody );
		}else {
			main.mqttMessage(Driver, driver.getDriverId(), messageBody);
		}
	}
	
	
	/**
	 * 대리기사가 강제로 상황을 종료...
	 * iOS implementation complete..
	 * @param driverId
	 * @param customer
	 */
	public void cancelByDriver(String driverId, Customer customer){
		//고객에게 Driving Mode가 끝남을 알림
		JSONObject messageBody = new JSONObject();
		messageBody.put("cmd", "cancelByDriver");
		messageBody.put("requestTime", customer.getRequestTime());
		messageBody.put("driverId", driverId);
		messageBody.put("title" , "대리기사가 대리운전을 취소하였습니다");
		messageBody.put("body", "");
		// receiver : customer
		if ( isiOS(customer.getOS()) ){
			main.apnsMessage(Customer,customer.getDevId(), messageBody);
		}else{
			main.mqttMessage(Customer,customer.getDevId(), messageBody);
		}
	}
	
	
	/**
	 * 고객의 취소..
	 * iOS implementation complete
	 * @param driver
	 * @param customer
	 * @param title
	 * @param body
	 */
	public void cancelByCustomer(Driver driver, Customer customer, String title , String body){
		
		JSONObject messageBody = new JSONObject();
		messageBody.put("cmd", "cancelByCustomer");
		messageBody.put("cDevId", customer.getDevId());
		messageBody.put("title" , title);
		messageBody.put("body", body);

		// receiver : driver
		if ( isiOS(driver.getOS()) ){
			main.apnsMessage(Driver,driver.getDevId(), messageBody);
		}else{
			main.mqttMessage(Driver,driver.getDriverId(), messageBody);
		}

	}
	
	/**
	 * 고객에게 Driving Mode가 끝남을 알림
	 * @param driverId
	 * @param customer
	 * @param title
	 * @param body
	 */
	public void disconnectedDriver(String driverId, Customer customer, String title , String body){
		JSONObject messageBody = new JSONObject();
		messageBody.put("cmd", "disconnectedDriver");
		messageBody.put("requestTime", customer.getRequestTime());
		messageBody.put("driverId", driverId);
		messageBody.put("title" , title);
		messageBody.put("body", body);
		
		if ( isiOS(customer.getOS()) ){
			main.apnsMessage(Customer,customer.getDevId(), messageBody);
		}else {
			main.mqttMessage(Customer,customer.getDevId(), messageBody);
		}
	}
	
	
	/**
	 * 고객의 연결이 끊어짐..
	 * @param driverId
	 * @param cDevId
	 * @param title
	 * @param body
	 */
	public void disconnectedCustomer(Driver driver, Customer customer, String title , String body){
		
		JSONObject messageBody = new JSONObject();
		messageBody.put("cmd", "disconnectedCustomer");
		messageBody.put("cDevId", driver.getDevId());
		messageBody.put("title" , title);
		messageBody.put("body", body);

		if ( isiOS(driver.getOS()) ){
			main.apnsMessage(Driver, driver.getDevId(), messageBody);
		}else{
			main.mqttMessage(Driver, driver.getDriverId(), messageBody);
		}

	}

	/**
	 * 대리운전이 완료되었음을 알림
	 * @param driverId
	 * @param customer
	 */
	public void endDriving(String driverId, Customer customer){
		
		JSONObject messageBody = new JSONObject();
		messageBody.put("cmd", "endDrivingByDriver");
		messageBody.put("requestTime", customer.getRequestTime());
		messageBody.put("driverId", driverId);
		messageBody.put("safePhone", customer.getSafePhone());
		messageBody.put("title" , "대리운전이 완료되었습니다.");
		messageBody.put("body", "");
		messageBody.put("usesCoupon_id", customer.getUsesCoupon_id());
		
		if (isiOS(customer.getOS())){
			main.apnsMessage(Customer,customer.getDevId(), messageBody);
		}else{
			main.mqttMessage(Customer,customer.getDevId(), messageBody);
		}

	}
	
	
	/**
	 * 
	 * @param customer
	 * @param distance
	 * @param driver
	 * @param safePhone
	 * @param dst
	 * @param src
	 * @param title
	 * @param body
	 */
	public void respStartDriving(Customer customer, double distance, Driver driver, String safePhone, String dst, String src ,String title , String body){
		
		JSONObject d = (JSONObject) JSONSerializer.toJSON(driver);
		JSONObject messageBody = new JSONObject();
		messageBody.put("cmd", "respStartDriving");
		messageBody.put("currentTime", System.currentTimeMillis());
		messageBody.put("requestTime", customer.getRequestTime());
		messageBody.put("cDevId", customer.getDevId());
		messageBody.put("distance", distance);
		messageBody.put("driver", d);
		messageBody.put("safePhone", safePhone);
		messageBody.put("dst", dst);
		messageBody.put("src", src);
		messageBody.put("title" , title);
		messageBody.put("body", body);

		if ( isiOS(customer.getOS()) ){
			main.apnsMessage(Customer,customer.getDevId(), messageBody);
		}else{
			main.mqttMessage(Customer,customer.getDevId(), messageBody);
		}
	}
	
	
	/**
	 * 
	 * @param customer
	 * @param driver
	 * @param title
	 * @param body
	 */
	public void respSuggestCharge(Customer customer, Driver driver , String title , String body){
		
		JSONObject messageBody = new JSONObject();
		JSONObject c = (JSONObject) JSONSerializer.toJSON(customer);
		JSONObject d = (JSONObject) JSONSerializer.toJSON(driver);
		double distance = driver.isNear(customer.getLat(), customer.getLng());
		messageBody.put("cmd", "respSuggestCharge");
		messageBody.put("currentTime", System.currentTimeMillis());
		messageBody.put("customer", c);
		messageBody.put("driver", d);
		messageBody.put("distance", distance);
		messageBody.put("title" , title);
		messageBody.put("body", body);
		
		if ( isiOS(driver.getOS()) ){
			main.apnsMessage(Driver,driver.getDevId(), messageBody);
		}else{
			main.mqttMessage(Driver,driver.getDriverId(), messageBody);
		}
	}
	
	
	/**
	 * 고객이 대리요청을 취소하였다
	 * @param cDevId
	 * @param driver
	 * @param title
	 * @param body
	 */
	public void cancelRequest(String cDevId, Driver driver, String title , String body){
		
		JSONObject messageBody = new JSONObject();
		messageBody.put("cmd", "cancelRequest");
		messageBody.put("cDevId", cDevId);
		messageBody.put("title" , title);
		messageBody.put("body", body);
		
		if ( isiOS(driver.getOS()) ){
			main.apnsMessage(Driver, driver.getDevId(), messageBody);
		}else{
			main.mqttMessage(Driver, driver.getDriverId(), messageBody);
		}
	}
	
	/**
	 * 고객이 다른 대리기사와 연결이 되었음을 알려준다.
	 * @param cDevId
	 * @param driverId
	 */
	public void refuseSuggestCharge(String cDevId, Driver driver, String title , String body, boolean alert){
		
		JSONObject messageBody = new JSONObject();
		messageBody.put("cmd", "refuseSuggestCharge");
		messageBody.put("cDevId", cDevId);
		messageBody.put("title" , title);
		messageBody.put("body", body);
		
		if ( isiOS(driver.getOS()) ){
			if ( alert ){
				main.apnsMessage(Driver, driver.getDevId(), messageBody);
			}else {
				main.apnsMessageWithoutAlert(Driver, driver.getDevId(), messageBody);
			}
		}else{
			main.mqttMessage(Driver, driver.getDriverId(), messageBody);
		}
	}
	
	/**
	 * 대리기사가 대리가격제시를 취소하였다.
	 * @param customer
	 * @param driverId
	 * @param title
	 * @param body
	 */
	public void cancelSuggest(Customer customer, String driverId, String title , String body) {
		JSONObject messageBody = new JSONObject();
		messageBody.put("cmd", "cancelSuggest");
		messageBody.put("requestTime", customer.getRequestTime());
		messageBody.put("driverId", driverId);
		messageBody.put("title" , title);
		messageBody.put("body", body);
		messageBody.put("title" , title);
		messageBody.put("body", body);
		
		if ( isiOS(customer.getOS()) ){
			main.apnsMessage(Customer,customer.getDevId(), messageBody);
		}else{
			main.mqttMessage(Customer,customer.getDevId(), messageBody);
		}
	}
	
	// TODO 안심서비스는 어떻게 하지?
	/**
	 * 안심서비스 종료를 안심대상에게 알려준다
	 */
	public void endSafe(String safePhone) {
		JSONObject messageBody = new JSONObject();
		messageBody.put("cmd", "endSafe");
		main.mqttMessage(Safe,safePhone, messageBody);
	}
	
	/**
	 * 안심서비스 취소를 안심대상에게 알려준다
	 */
	public void cancelSafe(String safePhone) {
		JSONObject messageBody = new JSONObject();
		messageBody.put("cmd", "cancelSafe");
		main.mqttMessage(Safe,safePhone, messageBody);
	}

	
	public void safePushService(String safeNum , double cLat, double cLng) {

		JSONObject messageBody = new JSONObject();
		messageBody.put("cmd", "safePushService");
		messageBody.put("cLat", cLat);
		messageBody.put("cLng", cLng);
		main.mqttMessage(Safe,safeNum, messageBody);
	}
	
	
	/**
	 * iOS 인지 체크한다
	 * @param OS
	 * @return
	 */
	private boolean isiOS(String OS){
		if ( OS.length() == 0 || OS == null ) return false;
		
		if ( OS.equals("iOS")){
			return true;
		}return false;
	}
}
