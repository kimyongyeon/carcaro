package carcaro.bean;

/**
 * Vcall Object 
 * @author CLX
 *
 */
public class VCall {
	
	private String cNum;
	private int 	cSID;
	private String cLat;
	private String cLng;
	private String cOS;
	private String cSrc;
	private String cDest;
	private String cCallTime;
	private int		cEnabled;
	private String sRepeat;

	public VCall(){
		
	}
	
	

	public VCall(String cNum, int cSID, String cLat, String cLng,
			String cOS, String cSrc, String cDest, String cCallTime,
			int cEnabled, String sRepeat) {
		this.cNum = cNum;
		this.cSID = cSID;
		this.cLat = cLat;
		this.cLng = cLng;
		this.cOS = cOS;
		this.cSrc = cSrc;
		this.cDest = cDest;
		this.cCallTime = cCallTime;
		this.cEnabled = cEnabled;
		this.sRepeat = sRepeat;
		
	}



	public String getcNum() {
		return cNum;
	}



	public void setcNum(String cNum) {
		this.cNum = cNum;
	}



	public int getcSID() {
		return cSID;
	}



	public void setcSID(int cSID) {
		this.cSID = cSID;
	}



	public String getcLat() {
		return cLat;
	}



	public void setcLat(String cLat) {
		this.cLat = cLat;
	}



	public String getcLng() {
		return cLng;
	}



	public void setcLng(String cLng) {
		this.cLng = cLng;
	}



	public String getcOS() {
		return cOS;
	}



	public void setcOS(String cOS) {
		this.cOS = cOS;
	}



	public String getcSrc() {
		return cSrc;
	}



	public void setcSrc(String cSrc) {
		this.cSrc = cSrc;
	}



	public String getcDest() {
		return cDest;
	}



	public void setcDest(String cDest) {
		this.cDest = cDest;
	}



	public String getcCallTime() {
		return cCallTime;
	}



	public void setcCallTime(String cCallTime) {
		this.cCallTime = cCallTime;
	}



	public int getcEnabled() {
		return cEnabled;
	}



	public void setcEnabled(int cEnabled) {
		this.cEnabled = cEnabled;
	}



	public String getsRepeat() {
		return sRepeat;
	}



	public void setsRepeat(String sRepeat) {
		this.sRepeat = sRepeat;
	}

	
	
	
	
	
}
