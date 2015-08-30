package carcaro.bean;

import java.util.concurrent.ConcurrentHashMap;


/**
 * Customer
 */
public class Customer {
	
//	public static final int NONE = 0;
	public static final int SEARCHING = 1;
//	public static final int WAITING = 2;
	public static final int DRIVING = 3;
	
	private String name;
	private String phoneNum;
	private int state;
	private long requestTime;
	private ConcurrentHashMap<String,Driver> driverList; // 이 고객에게 요금을 제시한 기사 목록
	private double lat;
	private double lng;
	private long locTime;
	private String src;
	private String dst;
	private String transit;		// 경유지
	private String devId;
	private String OS;			// 고객의 전화기 운영체제  "iOS"/"Android"
	private String driverId; 	// 연결된 기사.
	private boolean safeOn;
	private String safePhone;
	
	private String usesCoupon_id;
	private String wants_auto;
	
	public static final String OS_ANDROID 	= "Android";
	public static final String OS_IOS		= "iOS";
	
	//날짜, 고객전화, 고객이름, 출발, 도착, 기사전화, 기사이름, 요금, 쿠폰
	private String customDriverId; // 기기번호
	private String businessTime; // 날짜
	private String customerName; // 고객이름
	private String customerPhone; // 고객전화
	private String source; // 출발
	private String destination; // 도착
	private String driverPhone; // 기사이름
	private String driverName; // 기사이름
	private String amount; // 요금
	private String coupon_id; // 쿠폰
	
	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getCustomDriverId() {
		return customDriverId;
	}

	public void setCustomDriverId(String customDriverId) {
		this.customDriverId = customDriverId;
	}

	public String getBusinessTime() {
		return businessTime;
	}

	public void setBusinessTime(String businessTime) {
		this.businessTime = businessTime;
	}

	public String getCustomerPhone() {
		return customerPhone;
	}

	public void setCustomerPhone(String customerPhone) {
		this.customerPhone = customerPhone;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getDriverPhone() {
		return driverPhone;
	}

	public void setDriverPhone(String driverPhone) {
		this.driverPhone = driverPhone;
	}

	public String getDriverName() {
		return driverName;
	}

	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getCoupon_id() {
		return coupon_id;
	}

	public void setCoupon_id(String coupon_id) {
		this.coupon_id = coupon_id;
	}
	
	// 기본 생성자
	public Customer(){ 
	}
	
	public Customer(String cDevId, long requestTime){
		devId = cDevId;
		state = SEARCHING;
		this.requestTime = requestTime;
		driverList = new ConcurrentHashMap<String,Driver>();
	}
	
	// TODO iOS Implementation
	public Customer(String cDevId, long requestTime, String os){
		devId = cDevId;
		state = SEARCHING;
		OS = os;
		this.requestTime = requestTime;
		driverList = new ConcurrentHashMap<String,Driver>();
	}

	public void setPhoneNum(String cPhoneNum) {
		phoneNum = cPhoneNum;
	}
	
	public String getCName() {
		return name;
	}

	public void setCName(String cName) {
		name = cName;
	}
	
	public String getSafePhone() {
		return safePhone;
	}

	public void setSafePhone(String safePhone) {
		this.safePhone = safePhone;
	}
	
	public boolean isSafeOn() {
		return safeOn;
	}


	public void setSafeOn(boolean safeOn) {
		this.safeOn = safeOn;
	}
	
	public void setSrc(String cSrc) {
		src = cSrc;
	}
	
	public void setDst(String cDst) {
		dst = cDst;
	}
	
	public void setTransit(String cTransit){
		transit = cTransit;
	}
	
	public String getDst(){
		return dst;
	}
	public String getSrc(){
		return src;
	}
	public String getTransit(){
		return transit;
	}
	public long _getLocTime(){
		return locTime;
	}
	
	public String getDevId() {
		return devId;
	}
	
	public String getOS() {
		// TODO iOS implementation not complete ... Default OS is Android 
		if ( OS == null || OS.length()==0) OS = OS_ANDROID;
		
		return OS;
	}

	public void setOS(String oS) {
		OS = oS;
	}
	
	public String getPhoneNum(){
		return phoneNum;
	}
	
	public synchronized void setState(int CustomerState){
		state = CustomerState;
	}
	public synchronized int getState(){
		return state;
	}
	
	public long getRequestTime(){
		return requestTime;
	}
	public void startDriving(String driverId){
	    this.driverId = driverId;
	    this.state = DRIVING;
	}
	public String getDriverId(){
		return driverId;
	}
	public void _setPosition(double lat, double lng,  long locTime){
		this.lat = lat;
		this.lng = lng;
		this.locTime = locTime;
	}
	public double getLat(){
		return this.lat;
	}
	public double getLng(){
		return this.lng;
	}
	
	public ConcurrentHashMap<String,Driver> _getDriverList(){
		return driverList;
	}
	
	public void removeAllDriver(){
		driverList.clear();
	}
	
	public void putDriverList(String driverId , Driver driver){
		driverList.put(driverId, driver);
	}
	public void removeDriverList(String driverId){
		driverList.remove(driverId);
	}
	
	public int requestListSize()
	{
		return driverList.size();
	}
	
	public boolean isSuggestChargeDriver(String driverId){
		Driver driver = driverList.get(driverId);
		if(driver != null)
			return true;
		else
			return false;
	}
	
	/**
	 * Coupon 관련
	 */
	public void usesCoupon(String coupon_id){
		usesCoupon_id = coupon_id;
	}
	
	public String getUsesCoupon_id()
	{
		return usesCoupon_id;
	}
	
	public void wantsAuto(String auto){
		wants_auto = auto;
	}
	
	public String getWantsAuto(){
		return wants_auto;
	}

	
}
