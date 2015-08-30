package carcaro.bean;

/**
 * ChargetHistory
 */
public class ChargeHistory {
	
	
	private int bid;
	private String driverId; // 운전자ID
	private int fee; // 수수료
	private String businessTime; // 작업시간
	private int businessType; // 결과
	private String customerDeviceId; // 고객기기번호 
	private String customerPhone; //고객전화번호
	private String customerName; // 고객이름
	private String coupon_amount; // 쿠폰금액
	private String coupon_id; // 쿠폰아이디
	
	private String source; // 출발지
	private String destination; // 목적지
	private int drivingCharge; // 부과요금
	private String geoCovertAddress; // 좌표를 주소로 변환한 값.
	private String driverName; // 운전자이름
	private String Lat; // 위도
	private String Lng; // 경도
	private String phone; // 운전자전화번호
	
	public final int B_STATUS_OK = 1;		// OK
	public final int B_STATUS_CC = 2;		// Customer Cancel
	public final int B_STATUS_DC = 3;		// Driver Cancel
	public final int B_STATUS_NU = 4;		// ?
	
	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getCoupon_amount() {
		return coupon_amount;
	}

	public void setCoupon_amount(String coupon_amount) {
		this.coupon_amount = coupon_amount;
	}

	public String getCoupon_id() {
		return coupon_id;
	}

	public void setCoupon_id(String coupon_id) {
		this.coupon_id = coupon_id;
	}

	public String getGeoCovertAddress() {
		return geoCovertAddress;
	}

	public void setGeoCovertAddress(String geoCovertAddress) {
		this.geoCovertAddress = geoCovertAddress;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}
	
	public String getDriverName() {
		return driverName;
	}
	
	public int getBid() {
		return bid;
	}
	public void setBid(int bid) {
		this.bid = bid;
	}
	
	public String getDriverId() {
		return driverId;
	}
	public void setDriverId(String driverId) {
		this.driverId = driverId;
	}
	
	public int getFee() {
		return fee;
	}
	public void setFee(int fee) {
		this.fee = fee;
	}
	
	public String getBusinessTime() {
		return businessTime;
	}
	public void setBusinessTime(String businessTime) {
		this.businessTime = businessTime;
	}
	
	public int getBusinessType() {
		return businessType;
	}
	public void setBusinessType(int businessType) {
		this.businessType = businessType;
	}
	
	public String getCustomerDeviceId() {
		return customerDeviceId;
	}
	public void setCustomerDeviceId(String customerDeviceId) {
		this.customerDeviceId = customerDeviceId;
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
	
	public int getDrivingCharge() {
		return drivingCharge;
	}
	public void setDrivingCharge(int drivingCharge) {
		this.drivingCharge = drivingCharge;
	}
	public String getCustomerPhone() {
		return customerPhone;
	}
	public void setCustomerPhone(String customerPhone) {
		this.customerPhone = customerPhone;
	}

	public String getLng() {
		return Lng;
	}

	public void setLng(String lng) {
		this.Lng = lng;
	}

	public String getLat() {
		return Lat;
	}

	public void setLat(String lat) {
		this.Lat = lat;
	}

}



