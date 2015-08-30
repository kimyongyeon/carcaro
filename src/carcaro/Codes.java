package carcaro;

import java.util.HashMap;


public class Codes {

	static HashMap<Integer, String> status = new HashMap<Integer, String>();
	
	static {
		status.put(1, "완료");
		status.put(2, "고객취소");
		status.put(3, "기사취소");
	}
	
	public static String getStatus(int val){
		return status.get(val);
	}
	
	
	
	
}
