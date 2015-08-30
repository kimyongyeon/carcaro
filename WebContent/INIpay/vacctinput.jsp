<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.io.*"%>
<%@ page import="java.util.Calendar"%>
<%@page import="carcaro.bean.Settlement"%>
<%@page import="carcaro.dao.SettlementDAO"%>
<%@page import="carcaro.MainServlet"%>
<%@page import="carcaro.ConnectionPool"%>
<%@page import="carcaro.dao.DriverDAO"%>
<%
/*******************************************************************************
 * FILE NAME : vacctinput.jsp
 * DATE : 2009.07
 * 이니시스 가상계좌 입금내역 처리demon으로 넘어오는 파라메터를 control 하는 부분 입니다.
 * [수신정보] 자세한 내용은 메뉴얼 참조
 * 변수명           한글명                           
 * no_tid           거래번호                         
 * no_oid           주문번호                         
 * cd_bank          거래발생 기관코드                
 * cd_deal          취급기관코드                     
 * dt_trans         거래일자                         
 * tm_trans         거래시각                         
 * no_vacct         계좌번호                         
 * amt_input        입금금액                         
 * amt_check        미결제타점권금액                 
 * flg_close        마감구분                         
 * type_msg         거래구분                         
 * nm_inputbank     입금은행명                       
 * nm_input         입금자명                         
 * dt_inputstd      입금기준일자                     
 * dt_calculstd     정산기준일자                     
 * dt_transbase     거래기준일자                     
 * cl_trans         거래구분코드 "1100"              
 * cl_close         마감전후 구분,  0:마감점, 1마감후
 * cl_kor           한글구분코드, 2:KSC5601          
 *
 * (가상계좌채번시 현금영수증 자동발급신청시에만 전달)
 * dt_cshr          현금영수증 발급일자              
 * tm_cshr          현금영수증 발급시간              
 * no_cshr_appl     현금영수증 발급번호              
 * no_cshr_tid      현금영수증 발급TID               
 *******************************************************************************/

/***********************************************************************************
 * 이니시스가 전달하는 가상계좌이체의 결과를 수신하여 DB 처리 하는 부분 입니다.	
 * 필요한 파라메터에 대한 DB 작업을 수행하십시오.
 ***********************************************************************************/	

	String file_path = "/carcaro/vacct";

    id_merchant = request.getParameter("id_merchant");
    no_tid 		= request.getParameter("no_tid");
    no_oid 		= request.getParameter("no_oid");
    no_vacct 	= request.getParameter("no_vacct");
    amt_input 	= request.getParameter("amt_input");
    nm_inputbank = request.getParameter("nm_inputbank");
    nm_input 	= request.getParameter("nm_input");
    dt_inputstd = request.getParameter("dt_inputstd");
    

    // 매뉴얼을 보시고 추가하실 파라메터가 있으시면 아래와 같은 방법으로 추가하여 사용하시기 바랍니다.
    // String value = reqeust.getParameter("전문의 필드명");
    
    Settlement settle = new Settlement();
    settle.setOid(no_oid);
    settle.setTid(no_tid);
    settle.setAmount(Integer.parseInt(amt_input)+ "");
    settle.setMethod("VBank");
    settle.setSettleTime(dt_inputstd);

	try {
        writeLog(file_path);
        ConnectionPool connPool = ConnectionPool.getInstance();
		SettlementDAO dao = new SettlementDAO(connPool);
		DriverDAO driverDAO = new DriverDAO(connPool);
		boolean ret1 = false, ret2 = false;
		String driverId = dao.getDriverIdbyOid(no_oid);
		
		ret1 = dao.resultSettlement(settle);
		int amount = Integer.parseInt(settle.getAmount());
		int charge = driverDAO.getDriverInfo(driverId).getChargeSum();
		ret2 = driverDAO.updateChargeSum(charge+amount, driverId);
		
		if(ret1){
			if(ret2) {
				out.print("OK"); // 절대로 지우지 마세요}
			}else {
				out.print("FAIL");
			}
		}else {
			out.print("FAIL");
		}

	} catch(Exception e) {
		out.print(e.getMessage());
		writeFailLog(file_path, e.getMessage());
	}

%>
<%!

    private String id_merchant;
    private String no_tid;
    private String no_oid;
    private String no_vacct;
    private String amt_input;
    private String nm_inputbank;
    private String nm_input;
    private StringBuffer times;
    private String dt_inputstd;

    private String getDate()
    {
    	Calendar calendar = Calendar.getInstance();
    	
    	times = new StringBuffer();
        times.append(Integer.toString(calendar.get(Calendar.YEAR)));
		if((calendar.get(Calendar.MONTH)+1)<10)
        { 
            times.append("0"); 
        }
		times.append(Integer.toString(calendar.get(Calendar.MONTH)+1));
		if((calendar.get(Calendar.DATE))<10) 
        { 
            times.append("0");	
        } 
		times.append(Integer.toString(calendar.get(Calendar.DATE)));
    	
    	return times.toString();
    }
    
    private String getTime()
    {
    	Calendar calendar = Calendar.getInstance();
    	
    	times = new StringBuffer();

    	times.append("[");
    	if((calendar.get(Calendar.HOUR_OF_DAY))<10) 
        { 
            times.append("0"); 
        } 
 		times.append(Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)));
 		times.append(":");
 		if((calendar.get(Calendar.MINUTE))<10) 
        { 
            times.append("0"); 
        }
 		times.append(Integer.toString(calendar.get(Calendar.MINUTE)));
 		times.append(":");
 		if((calendar.get(Calendar.SECOND))<10) 
        { 
            times.append("0"); 
        }
 		times.append(Integer.toString(calendar.get(Calendar.SECOND)));
 		times.append("]");
 		
 		return times.toString();
    }

    private void writeLog(String file_path) throws Exception
    {

        File file = new File(file_path);
        file.createNewFile();

        FileWriter file2 = new FileWriter(file_path+"/vacctinput_"+getDate()+".log", true);


        file2.write("\n************************************************\n");
        file2.write("PageCall time : " + getTime());
        file2.write("\nID_MERCHANT : " + id_merchant);
        file2.write("\nNO_TID : " + no_tid);
        file2.write("\nNO_OID : " + no_oid);
        file2.write("\nNO_VACCT : " + no_vacct);
        file2.write("\nAMT_INPUT : " + amt_input);
        file2.write("\nNM_INPUTBANK : " + nm_inputbank);
        file2.write("\nNM_INPUT : " + nm_input);
        file2.write("\n************************************************\n");

        file2.close();

    }
    
    private void writeFailLog(String file_path, String errorMsg) throws Exception
    {

        File file = new File(file_path);
        file.createNewFile();

        FileWriter file2 = new FileWriter(file_path+"/vacctinput_err_"+getDate()+".log", true);


        file2.write("\n************************************************\n");
        file2.write("PageCall time : " + getTime());
        file2.write("\nID_MERCHANT : " + id_merchant);
        file2.write("\nNO_TID : " + no_tid);
        file2.write("\nNO_OID : " + no_oid);
        file2.write("\nNO_VACCT : " + no_vacct);
        file2.write("\nAMT_INPUT : " + amt_input);
        file2.write("\nNM_INPUTBANK : " + nm_inputbank);
        file2.write("\nNM_INPUT : " + nm_input);
        file2.write("\nerrMsg : " + errorMsg);
        file2.write("\n************************************************\n");

        file2.close();

    }
%>
