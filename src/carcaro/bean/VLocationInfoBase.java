package carcaro.bean;

public class VLocationInfoBase {
	
	private int LID;			// PKEY
	private String Sido;		// 시/도
	private String Gugun;	// 구/군
	private String Dong;	// 동
	private String Lat;		// Latitude
	private String Lng;		// Longitude
	private int Status;		// 0 = lat,lng is null 
	
	public static final String LOC_BLANK = " ";
	public static final int GPS_NULL = 0;
	public static final int GPS_NOT_NULL = 1;
	
	public VLocationInfoBase(){
	}
	
	public VLocationInfoBase(int LID){
		this.LID = LID;
	}
	
	public VLocationInfoBase(int LID, String Sido, String Gugun, String Dong, String Lat, String Lng, int Status){
		this.LID = LID;
		this.Sido = Sido;
		this.Gugun = Gugun;
		this.Dong = Dong;
		this.Lat = Lat;
		this.Lng = Lng;
		this.Status = Status;
	}


	public int getLID() {
		return LID;
	}


	public void setLID(int lID) {
		LID = lID;
	}


	public String getSido() {
		return Sido;
	}


	public void setSido(String sido) {
		Sido = sido;
	}


	public String getGugun() {
		return Gugun;
	}


	public void setGugun(String gugun) {
		Gugun = gugun;
	}


	public String getDong() {
		return Dong;
	}


	public void setDong(String dong) {
		Dong = dong;
	}


	public String getLat() {
		return Lat;
	}


	public void setLat(String lat) {
		Lat = lat;
	}


	public String getLng() {
		return Lng;
	}


	public void setLng(String lng) {
		Lng = lng;
	}


	public int getStatus() {
		return Status;
	}


	public void setStatus(int status) {
		Status = status;
	}
	
	

	

}
