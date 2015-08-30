package carcaro.bean;

/**
 * ChargetHistoryPeriod
 */
public class ChargeHistoryPeriod extends ChargeHistory {

	public static final int MAX_BTYPE_INDEX = 3; // 대리기록 완료여부 반영값
	private String businessDate;					// e: 2010-01-01
	private int businessTypeCount[]	= new int[MAX_BTYPE_INDEX+1];	//
	private int totalFee;							// 총수수료
	private int totalCharge;						// 요금
	
	/**
	 * 대리기록 생성자
	 */
	public ChargeHistoryPeriod() {
		
	}
	/**
	 * 대리시간 출력
	 * @return
	 */
	public String getBusinessDate() {
		return businessDate;
	}
	/**
	 * 대리시간 등록
	 * @param businessDate
	 */
	public void setBusinessDate(String businessDate) {
		this.businessDate = businessDate;
	}

	/**
	 * 요청한 대리기록에 대한 대리완료 여부 출력
	 * @param index
	 * @return
	 */
	public int getBusinessTypeCount(int index) {
		if (index>=0 && index<=MAX_BTYPE_INDEX)		
			return businessTypeCount[index];
		else										
			return -1;
	}
	/**
	 * 대리기록 등록
	 * 성공,대리취소,고객취소 전체 구하기 
	 * addBusinessTypeCount 메소드와 동일함.
	 * @param index
	 * @param bTypeCount
	 */
	public void setBusinessTypeCount(int index, int bTypeCount) {
		if (index>=0 && index<=MAX_BTYPE_INDEX) 
			this.businessTypeCount[index] = bTypeCount;
	}
	/**
	 * 대리기록 등록
	 * 성공,대리취소,고객취소 전체 구하기
	 * @param index
	 * @param value
	 */
	public void addBusinessTypeCount(int index, int value) {
		if (index>=0 && index<=MAX_BTYPE_INDEX) 
			this.businessTypeCount[index] += value;
	}
	/**
	 * 전체수수료 출력
	 * @return
	 */
	public int getTotalFee() {
		return totalFee;
	}
	/**
	 * 전체수수료 입력
	 * @param totalFee
	 */
	public void setTotalFee(int totalFee) {
		this.totalFee = totalFee;
	}
	/**
	 * 전체부과요금 출력
	 * @return
	 */
	public int getTotalCharge() {
		return totalCharge;
	}
	/**
	 * 전체부과요금 입력
	 * @param totalCharge
	 */
	public void setTotalCharge(int totalCharge) {
		this.totalCharge = totalCharge;
	}
	
	
	
	
}