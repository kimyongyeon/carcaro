package carcaro.bean;

public class Admin {
	private String no; // 사업자번호
	private String id; // 아이디
	private String pw; // 비밀번호
	private String email; // 이메일
	private String accessLevel; //접근권한 : 1-본사, 10-지사, 20-대리점
	private String location; // 사업소 위치
	private String hierarchy; // 상위 사업소 번호
	// 신규 추가 항목
	private String corName; // 회사명
	private String name; // 담당자명
	private String tel; // 대표전화
	private String smartPhone; // 휴대전화
	private String address; // 주소
	
	public String getCorName() {
		return corName;
	}
	public void setCorName(String corName) {
		this.corName = corName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTel() {
		return tel;
	}
	public void setTel(String tel) {
		this.tel = tel;
	}
	public String getSmartPhone() {
		return smartPhone;
	}
	public void setSmartPhone(String smartPhone) {
		this.smartPhone = smartPhone;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getNo() {
		return no;
	}
	public void setNo(String no) {
		this.no = no;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPw() {
		return pw;
	}
	public void setPw(String pw) {
		this.pw = pw;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getAccessLevel() {
		return accessLevel;
	}
	public void setAccessLevel(String accessLevel) {
		this.accessLevel = accessLevel;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getHierarchy() {
		return hierarchy;
	}
	public void setHierarchy(String hierarchy) {
		this.hierarchy = hierarchy;
	}
}
