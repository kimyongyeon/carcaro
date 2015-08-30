package carcaro.bean;


import java.util.concurrent.ConcurrentHashMap;

import carcaro.MainServlet;
import carcaro.util.Util;


public class Driver {
	
	public static final int ISNOTCERTIFIED = -1; //미승인
	public static final int ISCERTIFIED = 0; // 영업 안함
//	public static final int STANDBY = 1; // 영업중
	public static final int WAITING = 2;  // 요금 제시 전, 대기 중.
	public static final int SUGGESTED = 3; // 요금 제시 후, 대기 중.
	public static final int DRIVING = 4; // 운전중
	
	//차감 금액
	public static final int FEE = 500; 
	
	//결제 모드
	public static final int SETTLEMENT=1;
	public static final int CANCEL_CUSTOMER=2;
	public static final int CANCEL_DRIVER=3;
	public static final int UNKNOWN = 4;
	
	public static final String OS_ANDROID 	= "Android";
	public static final String OS_IOS		= "iOS";
	
	private String driverId;
	private String phone;
	private int dState;
	private double dLat;
	private double dLng;
	private long locTime;
	private String devId;
	private String OS;				// Driver's Operationg System
	
	/** <고객devId,고객> */
	private ConcurrentHashMap<String,Customer> DCustomerList;

	// 제시 했을 때 set되는 변수들.
	private int charge;
	private long suggestTime;
	private String suggestCustomerId;
	
	// TODO DB에서 읽어올 것.
	private int likeScore;
	private String picture;
	private String licensePic;
	private String advertisement;
	
	// 회원 가입 변수
	private String passwd;
	private String name;
	private String residentNo;
	private String authorizationNo;
	private int licenseType;
	private String email;
	private boolean licenseAuto;
	private int career;
	private String company;
	private String address;
	private int agreeReceive;

	//그외 정보
	private int chargeSum;
	private int starttime;
	private int endtime;
	
	private double dist; // 대리기사의 원하는 대리요청거리
	private int level; // 승인 비승인. 승인시 0 비승인시 -1
	
	private String authorizationName; // 보험이름
	
	private String requestDistance; // 대리요청거리
	
	public String getRequestDistance() {
		return requestDistance;
	}

	public void setRequestDistance(String requestDistance) {
		this.requestDistance = requestDistance;
	}

	// 신규 추가 항목
	//private String driverId; // driverID
	//private String licenseType; // 면허종류
	//private String passwd; // 비밀번호
	private String liceseNo; // 면허번호
	//private String authorizationNo; // 증권번호(보험번호)
	//private String name; // 이름
	private String assurance; // 보험만료일
	//private String residentNo; // 주민번호
	private String assurance_complete_date; // 보험만료일
	
	public String getAuthorizationName() {
		return authorizationName;
	}

	public void setAuthorizationName(String authorizationName) {
		this.authorizationName = authorizationName;
	}

	public void setLicenseAuto(boolean licenseAuto) {
		this.licenseAuto = licenseAuto;
	}
	
	public void setAgreeReceive(int agreeReceive) {
		this.agreeReceive = agreeReceive;
	}
	
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getLiceseNo() {
		return liceseNo;
	}

	public void setLiceseNo(String liceseNo) {
		this.liceseNo = liceseNo;
	}

	public String getAssurance() {
		return assurance;
	}

	public void setAssurance(String assurance) {
		this.assurance = assurance;
	}

	public String getAssurance_complete_date() {
		return assurance_complete_date;
	}

	public void setAssurance_complete_date(String assurance_complete_date) {
		this.assurance_complete_date = assurance_complete_date;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public void setResidentNo(String residentNo) {
		this.residentNo = residentNo;
	}

	public void setLicenseType(int licenseType) {
		this.licenseType = licenseType;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	//private String phone; // 전화번호
	private String amount; // 충전금(현금)
	//private String address; // 주소
	//private String licensePic; // 면허증사진
	//private String email; // 이메일
	
	
	
	
	
	//생성자
	public Driver() {
	}
	
	public Driver(String driverId, String name, String phone, String picture, String devId, int likeScore, String advertisement ){
//		this.dState = NONE;
		this.dState = WAITING;
		this.driverId = driverId;
		this.name = name;
		this.picture = picture;
		this.phone = phone;
		this.dLat = 0;
		this.dLng = 0;
		this.suggestTime = -1;
		this.DCustomerList = new ConcurrentHashMap<String,Customer>();
		this.suggestCustomerId = null;
		this.charge=0;
		this.devId=devId;
		this.likeScore = likeScore;
		this.advertisement = advertisement;
	}
	
	public Driver(String driverId, String passwd, String name, String phone, String residentNo, 
			String authorizationNo, String email, int licenseType, boolean licenseAuto, 
			int career, String company, String address, int agreeReceive, String OS) {
		this.driverId = driverId;
		this.passwd = passwd;
		this.name = name;
		this.phone = phone;
		this.residentNo = residentNo;
		this.authorizationNo = authorizationNo;
		this.DCustomerList = new ConcurrentHashMap<String,Customer>();
		this.email = email;
		this.licenseType = licenseType;
		this.licenseAuto = licenseAuto;
		this.career = career;
		this.company = company;
		this.address = address;
		this.agreeReceive = agreeReceive;
	}
	
	public String getDriverId() {
        return driverId;
    }
	
	public void setDriverId(String driverId) {
		this.driverId = driverId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getChargeSum() {
		return chargeSum;
	}

	public void setChargeSum(int chargeSum) {
		this.chargeSum = chargeSum;
	}
	
	public double isNear(double lat, double lng){
		//ToDo lat,lng를 meter로 바꾸기
	    if(this.dLat == 0 || this.dLng == 0){
	        return MainServlet.LIMITAREA;
	    } else {
	        return Util.distance(this.dLat, this.dLng, lat, lng);
	    }
	}
	
	
	public void setPhoneNum(String DriverPhoneNum){
		this.phone = DriverPhoneNum;
	}
	public String getPhoneNum(){
		return phone;
	}
	
	public String getDevId() {
		return devId;
	}

	public void setDevId(String devNum) {
		devId = devNum;
	}
	
	public String getOS() {
		// TODO iOS implementation not complete ... Default OS is Android 
		if ( OS == null || OS.length()==0) OS = OS_ANDROID;
		return OS;
	}

	public void setOS(String os) {
		OS = os;
	}
	
	public String getPasswd() {
		return passwd;
	}

	public String getResidentNo() {
		return residentNo;
	}

	public int getLicenseType() {
		return licenseType;
	}

	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isLicenseAuto() {
		return licenseAuto;
	}

	public int getCareer() {
		return career;
	}
	
	public void setCareer(int career) {
		this.career = career;
	}
	
	public void setAuthorizationNo(String authorizationNo) {
		this.authorizationNo = authorizationNo;
	}

	public String getCompany() {
		return company;
	}
	
	public void setCompany(String company) {
		this.company = company;
	}

	public String getAddress() {
		return address;
	}

	public int getAgreeReceive() {
		return agreeReceive;
	}

//	public void setCustomerNum(String customer){
//		this.suggestCustomerId = customer;
//	}
	
	public String getCustomerNum(){
		return suggestCustomerId;
	}
	public synchronized void setState(int DriverState){
		this.dState = DriverState;
	}
	public synchronized int getState(){
		return dState;
	}
	public ConcurrentHashMap<String,Customer> _getCustomerList(){
		return DCustomerList;
	}
	
	public void startDriving(){
		this.dState = DRIVING;
	}
	
	/**
	 * 대리요청이 들어옴을 알리는 푸시
	 * 
	 * @param cDevId
	 * @param customer
	 */
	public void putCustomerList(String cDevId , Customer customer){
		DCustomerList.put(cDevId, customer);
	}
	
	/**
	 * 사용자가 취소했음을 기사에게 알려야 한다.
	 * 
	 * @param cDevId
	 */
	public void removeCustomerList(String cDevId){
		DCustomerList.remove(cDevId);
		if(DCustomerList.isEmpty() && dState != ISNOTCERTIFIED)
			dState = WAITING;
		//미승인 상태시에는 상태변경 없음
		// 
	}
	public void removeAllCustomerList(){
		DCustomerList.clear();
		//미승인 상태시에는 상태변경 없음
		if(dState != ISNOTCERTIFIED)
			dState = WAITING;
	}
	
	/**
	 * 고객이 이 기사에게 요청을 한 상태인가?
	 * 
	 * @param cDevId
	 * @return
	 */
	public boolean isRequestCustomer(String cDevId){
		Customer customer =	DCustomerList.get(cDevId);
		if(customer == null){
			return false;
		}else{
			return true;
		}
	}
	
	public int requestListSize()
	{
		return DCustomerList.size();
	}
	
	public void suggest(int charge, String cDevId){
		this.charge = charge;
		this.suggestCustomerId = cDevId;
		this.suggestTime = System.currentTimeMillis();
		this.dState = SUGGESTED;
		
		this.DCustomerList.clear();
	}
	
	public long getSuggestTime(){
		return suggestTime;
	}
	public void setPosition(double lat, double lng, long locTime){
		this.dLat = lat;
		this.dLng = lng;
		this.locTime = locTime;
	}
	public long _getLocTime(){
		return this.locTime;
	}
	
	public double getLat(){
		return this.dLat;
	}
	public double getLng(){
		return this.dLng;
	}
	
	public int getCharge() {
		return charge;
	}

//	public void setCharge(int charge) {
//		this.charge = charge;
//	}
	
	public void initState(){
		this.dState = WAITING;
		this.charge = 0;
		this.suggestTime = 0;
		this.suggestCustomerId = null;
	}
	
	//처음 서버에 올라올 때 혹은 기사가 미승인 상태일때 사용
	public void firstInitState(){
		this.charge = 0;
		this.suggestTime = 0;
		this.suggestCustomerId = null;
	}
	
	
	public int getLikeScore() {
		return likeScore;
	}
	
	public void setLikeScore(int likeScore) {
		this.likeScore = likeScore;
	}
	
	public void setPicture(String picture) {
		this.picture = picture;
	}
	
	public String getPicture() {
		return picture;
	}
	
	public String getLicensePic() {
		return licensePic;
	}

	public void setLicensePic(String licensePic) {
		this.licensePic = licensePic;
	}

	public String getAdvertisement() {
		return advertisement;
	}
	
	public void setAdvertisement(String advertisement) {
		this.advertisement = advertisement;
	}
	
	public String getAuthorizationNo() {
		return authorizationNo;
	}
	
	public void likeScorePlus1() {
		this.likeScore++;
	}
	
	public void likeScoreMinus10() {
		this.likeScore-=10;
	}

	public void setStarttime(int starttime) {
		this.starttime = starttime;
	}
	
	public int getStarttime() {
		return starttime;
	}
	
	public void setEndtime(int endtime) {
		this.endtime = endtime;
	}
	
	public int getEndtime() {
		return endtime;
	}
	
	public double getDist() {
		return dist;
	}

	public void setDist(double dist) {
		this.dist = dist;
	}

	@Override
	public String toString() {
		
		StringBuffer buf = new StringBuffer();
		buf.append("driverId : ").append(driverId);
		buf.append(", phone : ").append(phone);
		buf.append(", state : ").append(dState);
		buf.append(", lat : ").append(dLat);
		buf.append(", lng : ").append(dLng);

		return buf.toString();
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	
}
