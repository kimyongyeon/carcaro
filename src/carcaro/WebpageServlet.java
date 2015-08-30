package carcaro;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import carcaro.bean.Admin;
import carcaro.bean.ChargeHistory;
import carcaro.bean.ChargeHistoryPeriod;
import carcaro.bean.Coupon;
import carcaro.bean.Customer;
import carcaro.bean.Driver;
import carcaro.bean.Settlement;
import carcaro.bean.SettlementHist;
import carcaro.dao.AdminDAO;
import carcaro.dao.ChargeHistoryDAO;
import carcaro.dao.CouponDAO;
import carcaro.dao.CustomerDAO;
import carcaro.dao.DriverDAO;
import carcaro.dao.LocationDAO;
import carcaro.dao.SettlementDAO;
import carcaro.util.Util;

/**
 * Servlet implementation class RegistMemberServlet
 */
public class WebpageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getRootLogger();

	// Connection Pool
	private ConnectionPool connPool;

	// DAO Class
	SettlementDAO settlementDAO;
	ChargeHistoryDAO chargeHistoryDAO;
	DriverDAO driverDAO;
	AdminDAO adminDAO;
	CouponDAO couponDAO;
	LocationDAO locationDAO;
	CustomerDAO customerdao; // 고객정보 리스트

	private ConcurrentHashMap<String, ChargeHistoryPeriod> CHP;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public WebpageServlet() {
		super();
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		// Connection Pool, DB 초기화
		try {
			connPool = ConnectionPool.getInstance();
			driverDAO = new DriverDAO(connPool);
			settlementDAO = new SettlementDAO(connPool);
			chargeHistoryDAO = new ChargeHistoryDAO(connPool);
			adminDAO = new AdminDAO(connPool);
			couponDAO = new CouponDAO(connPool);
			locationDAO = new LocationDAO(connPool);
			customerdao = new CustomerDAO(connPool);

			// Hashmap
			CHP = new ConcurrentHashMap<String, ChargeHistoryPeriod>();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void destroy() {

		ConnectionPool.destory();

		if (CHP != null) {
			CHP.clear();
			CHP = null;
		}

		super.destroy();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String cmd = req.getParameter("cmd");

		try {
			if ("registDriver".equals(cmd)) {
				registDriver(req, resp);
			} else if ("driverList".equals(cmd)) {
				driverList(req, resp);
			} else if ("certifingDriver".equals(cmd)) {
				certifingDriver(req, resp);
			} else if ("loginConfirm".equals(cmd)) {
				loginConfirm(req, resp);
			} else if ("checkId".equals(cmd)) {
				checkId(req, resp);
			} else if ("getSettlementList".equals(cmd)) {
				getSettlementList(req, resp);
			} else if ("modifyInfo".equals(cmd)) {
				modifyInfo(req, resp);
			} else if ("updateInfo".equals(cmd)) {
				updateInfo(req, resp);
			}
			/**
			 * TODO webadmin
			 */
			else if ("admin_login".equals(cmd)) {
				// check_admin(req,resp);
			} else if ("test".equals(cmd)) {
				// testFunction(req,resp);
				driveDetailPeriod(req, resp);
			} else if ("driverManager".equals(cmd)) {
				// Driver Manager (대리기사 관리자)
				driverManager(req, resp);
			} else if ("settleManager".equals(cmd)) {
				// 입출금 관리
				settleManager(req, resp);
			} else if ("driveDetail".equals(cmd)) {
				// 대리운전 관리
				driveDetail(req, resp);
			} else if ("couponManager".equals(cmd)) {
				// 쿠폰관리
				couponManager(req, resp);
			} else if ("noticeManager".equals(cmd)) {
				notieManager(req, resp);
			} else if ("helpManager".equals(cmd)) {
				helpManager(req, resp);
			} else if ("qnaManager".equals(cmd)) {
				qnaManager(req, resp); // 1:1문의 관리
			} else if ("yangjapa".equals(cmd)) {
				// 양자파..
				yangjapa(req, resp);
			} else if ("customerManager".equals(cmd)) {
				// 고객관리
				customerManager(req, resp);
			} else if ("adminManager".equals(cmd)) {
				// 관리자 관리
				adminManager(req, resp);
			} else if ("bannerManager".equals(cmd)) {
				bannerManager(req, resp);
			} else if ("etcManager".equals(cmd)) {
				etcManager(req, resp);
			} else if ("vCallManager".equals(cmd)) {
				// TODO 가상콜 스케줄러
				vCallManager(req, resp);
			} else if ("Si".equals(cmd)) {
				// TODO 가상콜 스케줄러
				SiManager(req, resp);
			} else if ("Gu".equals(cmd)) {
				// TODO 가상콜 스케줄러
				GuManager(req, resp);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 시가 들어왔을때 구를 동적으로 생성하여 전송한다.
	private void SiManager(HttpServletRequest req, HttpServletResponse resp)
			throws SQLException, ServletException, IOException {
		String Si = req.getParameter("Si");
		JSONArray guData = locationDAO.getGugun(Si);
		// 한글을 보내기 위해서는 꼭 선언해야 함.
		req.setCharacterEncoding("UTF-8");
		resp.setContentType("text/html; charset=utf-8");
		System.out.println("Si=="+Si);
		// 자바에서 json으로 값 넘기는 방법
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("Gu", guData);
		JSONObject jsonObject = JSONObject.fromObject(map);
		resp.setContentType("text/javascript");
		PrintWriter pw = resp.getWriter();
		pw.println(jsonObject.toString());
		pw.flush();
		pw.close();
	}

	// 구가 들어왔을때 동을 동적으로 생성하여 전송한다.
	private void GuManager(HttpServletRequest req, HttpServletResponse resp)
			throws SQLException, ServletException, IOException {
		String Gu = req.getParameter("Gu");
		JSONArray dongData = locationDAO.getDong(Gu);
		// 한글을 보내기 위해서는 꼭 선언해야 함.
		req.setCharacterEncoding("UTF-8");
		resp.setContentType("text/html; charset=utf-8");
		// 자바에서 json으로 값 넘기는 방법
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("Dong", dongData);
		JSONObject jsonObject = JSONObject.fromObject(map);
		resp.setContentType("text/javascript");
		PrintWriter pw = resp.getWriter();
		pw.println(jsonObject.toString());
		pw.flush();
		pw.close();
	}

	private void bannerManager(HttpServletRequest req, HttpServletResponse resp)
			throws SQLException, ServletException, IOException {
		// TODO Auto-generated method stub
		req.getRequestDispatcher("./admin/bannerManager_proc.jsp").forward(req,
				resp);
	}

	private void etcManager(HttpServletRequest req, HttpServletResponse resp)
			throws SQLException, ServletException, IOException {
		// TODO Auto-generated method stub
		req.getRequestDispatcher("./admin/etcManager_proc.jsp").forward(req,
				resp);
	}

	/*
	 * private void corporateManager(HttpServletRequest req, HttpServletResponse
	 * resp)throws SQLException,ServletException,IOException { // TODO
	 * Auto-generated method stub
	 * 
	 * }
	 */
	/**
	 * 관리자모드 분기처리
	 * 
	 * @param req
	 * @param resp
	 * @throws SQLException
	 * @throws ServletException
	 * @throws IOException
	 */
	private void adminManager(HttpServletRequest req, HttpServletResponse resp)
			throws SQLException, ServletException, IOException {
		// TODO Auto-generated method stub

		String action = req.getParameter("action");
		if ("list".equals(action)) { // 조회
			adminManagerList(req, resp);
		} else if ("modify".equals(action)) { // 수정 상세보기
			adminManagerModify(req, resp);
		} else if ("modifyProc".equals(action)) { // 수정 처리
			adminManagerModifyProc(req, resp);
		} else if ("insert".equals(action)) { // 생성 보기
			adminManagerInsert(req, resp);
		} else if ("insertProc".equals(action)) { // 생성처리
			adminManagerInsertProc(req, resp);
		} else if ("deleteProc".equals(action)) { // 삭제처리
			adminManagerDeleteProc(req, resp); 
		} else{
			adminManagerList(req, resp);
		}
	}
	/**
	 * 관리자관리 삭제처리
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 * @throws SQLException
	 */
	private void adminManagerDeleteProc(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException,
			SQLException {
		// 서버에서 값 받기
		String no = req.getParameter("no"); // no
		
		// 관리자 객체에 삽입
		Admin admin = new Admin();
		admin.setNo(no);
		
		// 삭제 처리
		System.out.println("------adminManagerDeleteProc START-------");
		boolean result = adminDAO.deleteAdmin(admin);
		System.out.println("------adminManagerDeleteProc START-------");
		if(result){
			// 한글을 보내기 위해서는 꼭 선언해야 함.
			resp.setContentType("text/html;charset=utf-8");
			resp.getWriter().println("관리자관리 삭제가 정상 처리되었습니다.");
		}else{
			resp.setContentType("text/html;charset=utf-8");
			resp.getWriter().println("관리자에게 문의 바랍니다.");
		}
		// 수정 -> 조회
		//adminManagerList(req, resp);
	}

	/**
	 * 관리자관리 수정 상세보기
	 * 
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 * @throws SQLException
	 */
	private void adminManagerModify(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException,
			SQLException {

		String no = req.getParameter("no"); // driverID
		System.out.println("------adminManagerModify START-------");
		Admin ad = adminDAO.getAdminInfo(no);
		System.out.println("------adminManagerModify START-------");

		req.setAttribute("no", ad.getNo());
		req.setAttribute("id", ad.getId());
		req.setAttribute("pw", ad.getPw());
		req.setAttribute("email", ad.getEmail());
		req.setAttribute("location", ad.getLocation());
		req.setAttribute("hierarchy", ad.getHierarchy());
		req.setAttribute("corName", ad.getCorName());
		req.setAttribute("name", ad.getName());
		req.setAttribute("tel",	ad.getTel());
		req.setAttribute("smartPhone", ad.getSmartPhone());
		req.setAttribute("address", ad.getAddress());
		
		// 아래에 상세보기 jsp파일을 넣으시오.
		//req.getRequestDispatcher("./admin/driverUpdate_view.jsp").forward(req,	resp);

	}

	/**
	 * 관리자관리 수정 처리
	 * 
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 * @throws SQLException
	 */
	private void adminManagerModifyProc(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException,
			SQLException {
		// 서버에서 값 받기
		String no = req.getParameter("no"); // no
		String id = req.getParameter("id"); // 아이디
		String pw = req.getParameter("pw"); // 패스워드
		String email = req.getParameter("email"); // 이메일
		String accessLevel = req.getParameter("accessLevel"); // 이메일
		String corName = req.getParameter("corName"); // 회사명
		String name = req.getParameter("name"); // 고객명
		String tel = req.getParameter("tel"); // 대표전화
		String smartPhone = req.getParameter("smartPhone"); // 휴대폰
		String address = req.getParameter("address"); // 주소
		String sido = req.getParameter("sido"); // 주소
		String gu = req.getParameter("gu"); // 주소
		//String location = req.getParameter("location"); // 위치
		//String hierarchy = req.getParameter("hierarchy"); // 상위위치
		
		System.out.println("no=="+no); // 사업소 번호
		System.out.println("id=="+id); // 아이디
		System.out.println("pw=="+pw); // 비밀번호
		System.out.println("email=="+email); // 이메일
		System.out.println("accessLevel=="+accessLevel); // 권한
		System.out.println("corName=="+corName); // 회사명
		System.out.println("name=="+name); // 이름
		System.out.println("tel=="+tel); // 전화번호
		System.out.println("smartPhone=="+smartPhone); // 휴대폰
		System.out.println("address=="+address); // 주소
		System.out.println("sido=="+sido); // 주소
		System.out.println("gu=="+gu); // 주소
		
		// 관리자 객체에 삽입
		Admin admin = new Admin();
		admin.setNo(no);
		admin.setId(id);
		admin.setPw(pw);
		admin.setEmail(email);
		admin.setAccessLevel(accessLevel);
		admin.setCorName(corName);
		admin.setName(name);
		admin.setTel(tel);
		admin.setSmartPhone(smartPhone);
		admin.setAddress(address);
		//admin.setLocation(location);
		//admin.setHierarchy(hierarchy);
		
		// 수정 처리
		System.out.println("------adminManagerModifyProc START-------");
		boolean result = adminDAO.updateAdmin(admin);
		System.out.println("------adminManagerModifyProc START-------");
		if(result){
			// 한글을 보내기 위해서는 꼭 선언해야 함.
			resp.setContentType("text/html;charset=utf-8");
			resp.getWriter().println("관리자관리 수정이 정상 처리되었습니다.");
		}else{
			resp.setContentType("text/html;charset=utf-8");
			resp.getWriter().println("관리자에게 문의 바랍니다.");
		}
		// 수정 -> 조회
		//adminManagerList(req, resp);
	}

	/**
	 * 관리자관리 생성 보기
	 * 
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 * @throws SQLException
	 */
	private void adminManagerInsert(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException,
			SQLException {
		// 이곳에 관리생성 팝업이 뜰 jsp파일을 매칭하세요.
		req.getRequestDispatcher("./admin/adminManager_proc.jsp").forward(req,
				resp);
	}

	/**
	 * 관리자관리 생성 처리
	 * 
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 * @throws SQLException
	 */
	private void adminManagerInsertProc(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException,
			SQLException {
		// 서버에서 값 받기
		String no = req.getParameter("no"); // no
		String id = req.getParameter("id"); // 아이디
		String pw = req.getParameter("pw"); // 패스워드
		String email = req.getParameter("email"); // 이메일
		String accessLevel = req.getParameter("accessLevel"); // 이메일
		String corName = req.getParameter("corName"); // 회사명
		String name = req.getParameter("name"); // 고객명
		String tel = req.getParameter("tel"); // 대표전화
		String smartPhone = req.getParameter("smartPhone"); // 휴대폰
		String address = req.getParameter("address"); // 주소
		String sido = req.getParameter("sido"); // 주소
		String gu = req.getParameter("gu"); // 주소
		//String location = req.getParameter("location"); // 위치
		//String hierarchy = req.getParameter("hierarchy"); // 상위위치
		
		System.out.println("no=="+no); // 사업소 번호
		System.out.println("id=="+id); // 아이디
		System.out.println("pw=="+pw); // 비밀번호
		System.out.println("email=="+email); // 이메일
		System.out.println("accessLevel=="+accessLevel); // 권한
		System.out.println("corName=="+corName); // 회사명
		System.out.println("name=="+name); // 이름
		System.out.println("tel=="+tel); // 전화번호
		System.out.println("smartPhone=="+smartPhone); // 휴대폰
		System.out.println("address=="+address); // 주소
		System.out.println("sido=="+sido); // 시도
		System.out.println("gu=="+gu); // 구읍
		//System.out.println("hierarchy=="+hierarchy);
		
		// 관리자 객체에 삽입
		Admin admin = new Admin();
		admin.setNo(no);
		admin.setId(id);
		admin.setPw(pw);
		admin.setEmail(email);
		admin.setAccessLevel(accessLevel);
		admin.setCorName(corName);
		admin.setName(name);
		admin.setTel(tel);
		admin.setSmartPhone(smartPhone);
		admin.setAddress(address);
		//admin.setLocation(location);
		//admin.setHierarchy(hierarchy);
		
		// 생성처리
		System.out.println("------adminManagerInsertProc START-------");
		int result = adminDAO.registerAdmin(admin);
		System.out.println("------adminManagerInsertProc END-------");
		if(result == 1){
			// 성공
			resp.setContentType("text/html;charset=utf-8");
			resp.getWriter().println("관리자 생성이 정상 처리되었습니다.");
		}else{
			// 실패
			resp.setContentType("text/html;charset=utf-8");
			resp.getWriter().println("관리자에게 문의 바랍니다.");
		}
		// 생성 -> 조회
		//adminManagerList(req, null);
	}

	/**
	 * 관리자관리 조회
	 * 
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 * @throws SQLException
	 */
	private void adminManagerList(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException,
			SQLException {

		String pageNum = req.getParameter("curr_page");
		String _pageSize = req.getParameter("pageSize");
		if (pageNum == null)
			pageNum = "1";
		if (_pageSize == null)
			_pageSize = "10"; // 페이지

		int pageSize = Integer.parseInt(_pageSize);
		int currentPage = Integer.parseInt(pageNum);

		String access = req.getParameter("access");
		String sido = req.getParameter("sido");
		String gu = req.getParameter("gu");

		if (access == null)
			access = "";
		if (sido == null)
			sido = "";
		if (gu == null)
			gu = "";

		Admin[] ad = adminDAO.getAdminList(pageSize, currentPage, access, sido, gu); // 한페이지에 표지될 목록
		long plTotalCnt = adminDAO.getAdminCount(access, sido, gu); // 레코드 전체 개수
		
		System.out.println("admin size====" + ad.length);
		System.out.println("plTontalCnt====" + plTotalCnt);

		req.setAttribute("plTotalCnt", new Long(plTotalCnt));
		req.setAttribute("pageNum", new Long(currentPage));
		req.setAttribute("pageSize", new Long(pageSize));

		req.setAttribute("list", ad);
		req.setAttribute("access", access);// 지사
		req.setAttribute("sido", sido);//
		req.setAttribute("gu", gu);//

		req.getRequestDispatcher("./admin/adminManager_proc.jsp").forward(req,
				resp);

	}

	private void vCallManager(HttpServletRequest req, HttpServletResponse resp)
			throws SQLException, ServletException, IOException {
		// TODO Auto-generated method stub
		String mode = req.getParameter("mode");
		if ("scheduler".equals(mode)) { // 스케줄러
			vCallManagerScheduler(req, resp);
		} else if ("create".equals(mode)) { // 가상콜 글생성 by pass
			vCallManagerCreate(req, resp);
		} else if ("createFinish".equals(mode)) { // 가상콜 글 생성처리
			vCallManagerCreateFinish(req, resp);
		} else if ("delete".equals(mode)) { // 가상콜 글 삭제처리
			vCallManagerDelete(req, resp);
		} else if ("detail".equals(mode)) { // 가상콜 글 상세보기 
			vCallManagerDetail(req, resp);
		} else if ("modify".equals(mode)) { // 가상콜 글 수정처리
			vCallManagerModify(req, resp);
		}
	}

	private void vCallManagerDetail(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException,
			SQLException {
		JSONObject json = new JSONObject();

		String sID = req.getParameter("sID");
		json = adminDAO.getVCallScheduleDetail(sID);
		req.setAttribute("vCallSchedule", json);

		req.getRequestDispatcher("./admin/vCallScheduler_modify_proc.jsp")
				.forward(req, resp);
	}

	private void vCallManagerDelete(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, IOException,
			ServletException {
		String sID = req.getParameter("sID");

		// 스케줄 삭제
		adminDAO.removeSchedule(sID);
		// 리스트 삭제
		adminDAO.removeVCall(sID);

		// 목록으로 전환
		vCallManagerScheduler(req, resp);
	}

	private void vCallManagerCreate(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		req.getRequestDispatcher("./admin/vCallScheduler_create_proc.jsp")
				.forward(req, resp);
	}

	private void vCallManagerModify(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, IOException,
			ServletException {

		String sID = req.getParameter("sID");

		// 스케줄 삭제
		adminDAO.removeSchedule(sID);
		// 리스트 삭제
		adminDAO.removeVCall(sID);

		vCallManagerCreateFinish(req, resp);
	}

	private void vCallManagerCreateFinish(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, ServletException,
			IOException {
		logger.debug("vCallCreateFinish");
		String sBegin = req.getParameter("sBegin");
		String sEnd = req.getParameter("sEnd");
		String sLocaName = req.getParameter("sLocaName");
		String sCallCount = req.getParameter("sCallCount");
		String sMon = req.getParameter("Mon");
		String sTue = req.getParameter("Tue");
		String sWed = req.getParameter("Wed");
		String sThu = req.getParameter("Thu");
		String sFri = req.getParameter("Fri");
		String sSat = req.getParameter("Sat");
		String sSun = req.getParameter("Sun");
		if (sMon == null)
			sMon = "0";
		if (sTue == null)
			sTue = "0";
		if (sWed == null)
			sWed = "0";
		if (sThu == null)
			sThu = "0";
		if (sFri == null)
			sFri = "0";
		if (sSat == null)
			sSat = "0";
		if (sSun == null)
			sSun = "0";

		// Enabled by Default (사용 X . 나중에 추가될 경우 대비해 예약 컬럼임)
		int sEnabled = 1;

		// 1234567
		String sRepeat = sMon + sTue + sWed + sThu + sFri + sSat + sSun;

		// 스케줄 생성
		int cSID = adminDAO.createVCallSchedule(sBegin, sEnd, sLocaName,
				Integer.parseInt(sCallCount), sRepeat, sEnabled);

		// 지역에 따른 좌표 가져오기
		StringTokenizer tokenizer = new StringTokenizer(sLocaName);
		int index = 0;
		String Sido = null;
		StringBuffer GugunB = new StringBuffer();
		String Dong = null;
		while (tokenizer.hasMoreTokens()) {
			if (index == 0) {
				// 시도
				Sido = tokenizer.nextToken();
			} else if (index == 1) {
				// 구군
				GugunB = GugunB.append(tokenizer.nextToken());
			} else if (index == 2) {
				GugunB = GugunB.append(" ");
				GugunB = GugunB.append(tokenizer.nextToken());
			}
			// }else if ( index == 2 ){
			// Dong = tokenizer.nextToken();
			// }
			index++;
		}

		DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm"); // FORMATTER
																	// HH:mm
		DateTimeFormatter fmtSec = DateTimeFormat.forPattern("HH:mm:ss"); // vcall_list
																			// precision
																			// to
																			// a
																			// second
																			// HH:mm:ss
		DateTime begin = DateTime.parse(sBegin, fmt);
		DateTime end = DateTime.parse(sEnd, fmt);
		if (begin.isAfter(end))
			end = end.plusDays(1);

		// TODO 콜개수에 따른 콜 시간대 분리
		// 인터벌 생성
		Interval interval = new Interval(begin, end);
		Duration duration = interval.toDuration();
		// 조각을 구한다.
		int cake = duration.toStandardSeconds().getSeconds();
		cake = cake / Integer.parseInt(sCallCount);

		Random rand = new Random();
		rand.setSeed(System.currentTimeMillis());

		// 출발지 랜덤 선택
		JSONArray locaInfoArray = adminDAO.getLocationInfoByLocaName(Sido,
				GugunB.toString(), null);
		// 도착지는 출발지의 시/도 에서 랜덤하게 선택
		JSONArray dstLocaInfo = adminDAO.getLocationInfoByLocaName(Sido, null,
				null);

		for (int i = 0; i < Integer.parseInt(sCallCount); i++) {
			// 지역정보에서 랜덤하게 빼옴
			int randomNumber = rand.nextInt(locaInfoArray.size() - 1);
			JSONObject locaInfo = locaInfoArray.getJSONObject(randomNumber);

			// 랜덤한 도착지
			randomNumber = rand.nextInt(dstLocaInfo.size() - 1);
			JSONObject destinationInfo = dstLocaInfo
					.getJSONObject(randomNumber);

			DateTime callTime = begin.plusSeconds(cake * i);

			// 정보 입력
			String Lat = locaInfo.getString("Lat");
			String Lng = locaInfo.getString("Lng");
			String OS = "Android";
			String Src = locaInfo.getString("Sido") + " "
					+ locaInfo.getString("Gugun") + " "
					+ locaInfo.getString("Dong");
			String Dest = destinationInfo.getString("Sido") + " "
					+ destinationInfo.getString("Gugun") + " "
					+ destinationInfo.getString("Dong");
			String callTimeString = callTime.toString(fmtSec);

			// 스케줄에 따른 리스트 생성
			adminDAO.createVCallList(cSID, Lat, Lng, OS, Src, Dest,
					callTimeString, sEnabled);

			// logger.debug("vcall List : "+ cSID + "/" + Lat + "/" + Lng + "/"
			// + OS + "/" + Src + "/" + Dest + "/" + callTimeString + "/"+
			// sEnabled);
		}

		// return to list
		vCallManagerScheduler(req, resp);
	}

	private void vCallManagerScheduler(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, ServletException,
			IOException {
		
		// 페이징 처리
		String pageNum = req.getParameter("curr_page");
		String _pageSize = req.getParameter("pageSize");
		if (pageNum == null) {
			pageNum = "1";
		}
		if (_pageSize == null) {
			_pageSize = "10";
		}

		int pageSize = Integer.parseInt(_pageSize);
		int currentPage = Integer.parseInt(pageNum);
		
		JSONArray json = adminDAO.getScheduleList(pageSize, currentPage);
		long plTotalCnt = adminDAO.getScheduleList();

		req.setAttribute("plTotalCnt", new Long(plTotalCnt));
		req.setAttribute("pageNum", new Long(currentPage));
		req.setAttribute("pageSize", new Long(pageSize));
		req.setAttribute("list", json);

		req.getRequestDispatcher("./admin/vCallScheduler_proc.jsp").forward(
				req, resp);
	}

	private void yangjapa(HttpServletRequest req, HttpServletResponse resp) {
		// TODO Auto-generated method stub
		String mode = req.getParameter("mode");
		if ("list".equals(mode)) {

		} else {

		}
	}

	private void customerManager(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, ServletException,
			IOException {
		// TODO 고객 관리
		String mode = req.getParameter("mode");
		if ("list".equals(mode)) {
			customerManagerList(req, resp);
		}
	}

	private void customerManagerList(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, ServletException,
			IOException {
		// TODO Auto-generated method stub
		// 고객리스트:시작일 종료일 전체일반 법인 이름 고객 id 전화번호 쿠폰
		// 고객정보 쿠폰 내역주회 지사 대리점의 경우 해당지역 가시만 관리
		// no,날짜,고객신분?(구분),고객이름,출발지 목적지 기사전화 기사이름 요금 쿠폰,불만접수
		// 콜수 대리요금 1회 평균요금

		String pageNum = req.getParameter("curr_page");
		String _pageSize = req.getParameter("pageSize");

		System.out.println("pageNum=====" + pageNum);
		System.out.println("_pageSize=====" + _pageSize);

		if (pageNum == null)
			pageNum = "1";

		if (_pageSize == null) 
			_pageSize = "10"; // 페이지

		int currentPage = Integer.parseInt(pageNum);
		int pageSize = Integer.parseInt(_pageSize);

		System.out.println("pageSize=====" + pageSize);
		System.out.println("currentPage=====" + currentPage);

		// 서버에서 받는 값
		String dateA = req.getParameter("dateA"); // 시작일: ca1
		String dateB = req.getParameter("dateB"); // 종료일: cal2
		String keyword = req.getParameter("keyword"); // 키워드: name
		String gubun = req.getParameter("searchSelector"); // 구분자: nameName, usrNum, couponNum
		
		System.out.println("receice Data Start==============================");
		System.out.println("dateA=====" + dateA);
		System.out.println("dateB=====" + dateB);
		System.out.println("keyword=====" + keyword);
		System.out.println("gubun=====" + gubun);
		System.out.println("receice Data End==============================");
		
		if (gubun == null)
			gubun = "0";
		
		if(gubun.equals("0")){
			gubun = "nameName";
		}else if(gubun.equals("1")){
			gubun = "usrNum";
		}else{
			gubun = "couponNum";
		}

		if (dateA == null || dateA.length() == 0) {
			dateA = Util.getCurrDate();
		}
		if (dateB == null || dateB.length() == 0) {
			dateB = Util.getCurrDate();
		}
		if (keyword == null)
			keyword = "";

		System.out.println("dateA=====" + dateA);
		System.out.println("dateB=====" + dateB);
		System.out.println("keyword=====" + keyword);
		System.out.println("gubun=====" + gubun);

		try {
			// 현재 목록 레코드수
			Customer[] ct = customerdao.getCustomerList(pageSize, currentPage, dateA, dateB, keyword, gubun);
			System.out.println("ct=====" + ct.length);
			
			// 전체 목록 레코드수
			long plTotalCnt = customerdao.getCustomerCount(dateA, dateB, keyword, gubun);
			System.out.println("plTotalCnt=====" + plTotalCnt);

			// 페이징 처리 : 전체 페이지수가 0일때 500 에러 발생
			req.setAttribute("plTotalCnt", new Long(plTotalCnt)); // 전체레코드 수
			req.setAttribute("pageNum", new Long(currentPage)); // 1
			req.setAttribute("pageSize", new Long(pageSize)); // 10

			req.setAttribute("list", ct);
			req.setAttribute("dateA", dateA);
			req.setAttribute("dateB", dateB);
			req.setAttribute("keyword", keyword);

			System.out.println("list===" + ct.length);

		} catch (SQLException e) {
			//e.printStackTrace();
			req.setAttribute("msg1", "조회에 실패했습니다.\\n잠시 후에  다시 시도해주세요");
		}

		req.getRequestDispatcher("./admin/customerManager_proc.jsp").forward(req, resp);
	}

	private void notieManager(HttpServletRequest req, HttpServletResponse resp)
			throws SQLException, ServletException, IOException {
		// TODO 공지사항 관리
		String mode = req.getParameter("mode");
		if ("list".equals(mode)) { // 공지사항 조회
			noticeManagerList(req, resp);
		} else if ("create".equals(mode)) { // 생성
			noticeManagerCreate(req, resp);
		} else if ("createFinish".equals(mode)) { // 공지사항 글 생성 처리
			noticeManagerCreateFinish(req, resp);
		} else if ("delete".equals(mode)) { // 공지사항 글 삭제 처리
			noticeManagerDelete(req, resp);
		} else if ("modify".equals(mode)) { // 공지사항 글 수정 처리
			noticeManagerModify(req, resp);
		} else if ("detail".equals(mode)) { // 공지사항 상세글 보기
			noticeManagerDetail(req, resp);
		}
	}

	private void noticeManagerDelete(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, ServletException,
			IOException {
		// 삭제
		String no = req.getParameter("no");
		adminDAO.deleteNotice(no);

		// 리스트로 돌아간다.
		noticeManagerList(req, resp);
	}

	private void noticeManagerCreateFinish(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, ServletException,
			IOException {

		// 등록
		String title = req.getParameter("title");
		String description = req.getParameter("description");
		adminDAO.createNotice(title, description);

		// 리스트로 돌아간다.
		noticeManagerList(req, resp);
	}

	private void noticeManagerCreate(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		req.getRequestDispatcher("./admin/noticeCreate_proc.jsp").forward(req,
				resp);
	}

	private void noticeManagerModify(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException,
			SQLException {
		// 수정
		String no = req.getParameter("no");
		String title = req.getParameter("title");
		String description = req.getParameter("description");
		adminDAO.modifyNotice(no, title, description);

		// 리스트로 돌아간다.
		noticeManagerList(req, resp);
	}

	private void noticeManagerList(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, ServletException,
			IOException {

		// 페이징 처리
		String pageNum = req.getParameter("curr_page");
		String _pageSize = req.getParameter("pageSize");
		if (pageNum == null) {
			pageNum = "1";
		}
		if (_pageSize == null) {
			_pageSize = "10";
		}

		int pageSize = Integer.parseInt(_pageSize);
		int currentPage = Integer.parseInt(pageNum);

		JSONArray json = adminDAO.getNotice(pageSize, currentPage);
		long plTotalCnt = adminDAO.getNoticeCnt();

		req.setAttribute("plTotalCnt", new Long(plTotalCnt));
		req.setAttribute("pageNum", new Long(currentPage));
		req.setAttribute("pageSize", new Long(pageSize));
		req.setAttribute("list", json);

		req.getRequestDispatcher("./admin/noticeList_proc.jsp").forward(req,
				resp);
	}

	private void noticeManagerDetail(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, ServletException,
			IOException {
		// TODO Auto-generated method stub
		String no = req.getParameter("no");

		JSONObject json = adminDAO.getNoticeDetail(no);

		req.setAttribute("detail", json);

		req.getRequestDispatcher("./admin/noticeModify_proc.jsp").forward(req,
				resp);
	}

	private void helpManager(HttpServletRequest req, HttpServletResponse resp)
			throws SQLException, ServletException, IOException {
		String action = req.getParameter("action");
		if ("list".equals(action)) { // 도움말 목록 조회
			helpManagerList(req, resp);
		} else if ("create".equals(action)) { // 도움말 생성 by pass
			helpManagerCreate(req, resp);
		} else if ("createFinish".equals(action)) { // 도움말 글 생성 처리
			helpManagerCreateFinish(req, resp);
		} else if ("delete".equals(action)) { // 도움말 글 삭제 처리
			helpManagerDelete(req, resp);
		} else if ("modify".equals(action)) { // 도움말 글 수정 처리
			helpManagerModify(req, resp);
		} else if ("detail".equals(action)) { // 도움말 글 상세보기
			helpManagerDetail(req, resp);
		}
	}

	private void helpManagerDetail(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, ServletException,
			IOException {
		String hNO = req.getParameter("hNO");

		JSONObject json = adminDAO.getHelpDetail(hNO);

		req.setAttribute("detail", json);

		req.getRequestDispatcher("./admin/helpModify_proc.jsp").forward(req,
				resp);
	}

	private void helpManagerModify(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, ServletException,
			IOException {
		// 수정
		String hNO = req.getParameter("hNO");
		String hTitle = req.getParameter("hTitle");
		String hDescription = req.getParameter("hDescription");
		adminDAO.modifyHelp(hNO, hTitle, hDescription);

		// 리스트로 돌아간다.
		helpManagerList(req, resp);
	}

	private void helpManagerDelete(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, ServletException,
			IOException {
		// 삭제
		String hNO = req.getParameter("hNO");
		adminDAO.deleteHelp(hNO);

		// 리스트로 돌아간다.
		helpManagerList(req, resp);
	}

	private void helpManagerCreateFinish(HttpServletRequest req, HttpServletResponse resp) throws SQLException, ServletException, IOException {
				   
		// http://vcarcaro.cafe24.com:8080/picture/ + 파일명
		// multipart/form-data 로 보내면 stream으로 받는다.
		
		String picture = null;
		String title = null;
		String description = null;
		
		try {
			req.setCharacterEncoding("UTF-8");
			// Create a factory for disk-based file items
			DiskFileItemFactory factory = new DiskFileItemFactory();
			// Set factory constraints
			factory.setSizeThreshold(1024 * 1024 * 10);
			factory.setRepository(new File(getServletContext().getRealPath("/WEB-INF/uploadData")));
			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);
			// Set overall request size constraint
			upload.setSizeMax(-1);
			@SuppressWarnings("rawtypes")
			List parameters = upload.parseRequest(req);
			for (int i = 0; i < parameters.size(); i++) {
				FileItem fileItem = (FileItem) parameters.get(i);
				// 첨부파일 체크
				if (fileItem.isFormField()) {
					String fieldName = fileItem.getFieldName();
					if ("hTitle".equals(fieldName)) {
						title = fileItem.getString("utf-8");
						logger.debug("hTitle:" + title);
					} else if("hDescription".equals(fieldName)) {
						description = fileItem.getString("utf-8");
						logger.debug("hDescription:" + description);
					}
				} else {

					if (fileItem.getSize() > 0 && title != null) {
						logger.debug("Image file found... fieldName:" + fileItem.getName() + " /fileSize:" + fileItem.getSize());
						String fileName = fileItem.getName(); // Image
						int idx = fileName.lastIndexOf('\\');
						if (idx != -1) {
							fileName = fileName.substring(idx + 1);
						}

						File folder = new File("/carcaro/picture");
						if (!folder.exists()) {
							folder.mkdirs();
						}
						
						logger.debug("folderName:" + folder.getName());
						
						int dotposition = fileName.lastIndexOf(".");
						String extension = fileName.substring(dotposition, fileName.length());
						picture = "help" + '_' + System.currentTimeMillis() + extension;
						File file = new File(folder, picture); // driverId

						OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
						InputStream in = new BufferedInputStream(fileItem.getInputStream());

						byte[] buf = new byte[8192];
						int len;
						while ((len = in.read(buf)) != -1) {
							out.write(buf, 0, len);
						}
						in.close();
						out.close();
						logger.debug("Image upload complete :" + picture);
					}
				}
			}
			logger.debug("--->> helpManagerCreateFinish: title:" + title + " /description:" + description + " /picture:" + picture);
			// 파일명 저장
			adminDAO.createHelp(title, description, picture);
			// 리스트로 돌아간다.
			helpManagerList(req, resp);
			
		} catch (Exception e) {
			logger.error("helpManagerCreateFinish error.", e);
		}
	}

	private void helpManagerCreate(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		req.getRequestDispatcher("./admin/helpCreate_proc.jsp").forward(req,
				resp);
	}

	private void helpManagerList(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, ServletException,
			IOException {
		
		// 페이징 처리
		String pageNum = req.getParameter("curr_page");
		String _pageSize = req.getParameter("pageSize");
		if (pageNum == null) {
			pageNum = "1";
		}
		if (_pageSize == null) {
			_pageSize = "10";
		}

		int pageSize = Integer.parseInt(_pageSize);
		int currentPage = Integer.parseInt(pageNum);
		
		JSONArray json = adminDAO.getHelp(pageSize, currentPage);
		long plTotalCnt = adminDAO.getHelpCnt();

		req.setAttribute("plTotalCnt", new Long(plTotalCnt));
		req.setAttribute("pageNum", new Long(currentPage));
		req.setAttribute("pageSize", new Long(pageSize));
		req.setAttribute("list", json);

		req.getRequestDispatcher("./admin/helpList_proc.jsp")
				.forward(req, resp);
	}

	private void qnaManager(HttpServletRequest req, HttpServletResponse resp)
			throws SQLException, ServletException, IOException {
		String action = req.getParameter("action");
		if ("list".equals(action)) { // 1:1문의 목록 조회
			qnaManagerList(req, resp); 
		} else if ("answer".equals(action)) { // 1:1문의 답변 by pass 페이지 전환만.
			qnaManagerAnswer(req, resp); 
		} else if ("modify".equals(action)) { // 1:1문의 수정 상세보기
			qnaManagerModify(req, resp); 
		} else if ("answerFinish".equals(action)) { // 1:1문의 답변 처리
			qnaManagerAnswerFinish(req, resp);
		} else if ("modifyFinish".equals(action)) { // 1:1문의 글 수정 처리
			qnaManagerModifyFinish(req, resp); 
		} else if ("delete".equals(action)) { // 1:1문의 글 삭제 처리
			qnaManagerDelete(req, resp); 
		}
	}

	// Q&A 상세보기
	private void qnaManagerModify(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, ServletException,
			IOException {
		String QID = req.getParameter("QID");

		JSONObject json = adminDAO.getQnaDetail(QID);

		req.setAttribute("detail", json);

		// 한소영 - 여기에 뿌릴 주소를 넣으세요.
		// req.getRequestDispatcher("./admin/qnaAnswer_proc.jsp").forward(req,resp);
	}

	// Q&A 상세보기 수정 처리
	private void qnaManagerModifyFinish(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, ServletException,
			IOException {

		String QID = req.getParameter("QID"); // 식별자
		String title = req.getParameter("title"); // 제목
		String desc = req.getParameter("desc"); // 내용
		adminDAO.modifyQna(QID, title, desc);
		// 다시 리스트를 호출한다.
		qnaManagerList(req, resp);
	}

	// Q&A 삭제 처리
	private void qnaManagerDelete(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, ServletException,
			IOException {

		String QID = req.getParameter("QID"); // 식별자
		adminDAO.deleteQna(QID);
		// 다시 리스트를 호출한다.
		qnaManagerList(req, resp);
	}

	private void qnaManagerAnswerFinish(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, ServletException,
			IOException {
		String QID = req.getParameter("QID");
		String answer = req.getParameter("answer");

		adminDAO.answerQna(QID, answer);

		qnaManagerList(req, resp);
	}

	private void qnaManagerAnswer(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, ServletException,
			IOException {
		String QID = req.getParameter("QID");

		JSONObject json = adminDAO.getQnaDetail(QID);

		req.setAttribute("detail", json);

		req.getRequestDispatcher("./admin/qnaAnswer_proc.jsp").forward(req,
				resp);
	}

	private void qnaManagerList(HttpServletRequest req, HttpServletResponse resp)
			throws SQLException, ServletException, IOException {
		
		// 페이징 처리
		String pageNum = req.getParameter("curr_page");
		String _pageSize = req.getParameter("pageSize");
		if (pageNum == null) {
			pageNum = "1";
		}
		if (_pageSize == null) {
			_pageSize = "10";
		}

		int pageSize = Integer.parseInt(_pageSize);
		int currentPage = Integer.parseInt(pageNum);
		
		JSONArray json = adminDAO.getQna(pageSize, currentPage, null, "ALL");
		long plTotalCnt = adminDAO.getQnaCnt(null, "ALL");

		req.setAttribute("plTotalCnt", new Long(plTotalCnt));
		req.setAttribute("pageNum", new Long(currentPage));
		req.setAttribute("pageSize", new Long(pageSize));
		req.setAttribute("list", json);

		req.getRequestDispatcher("./admin/qnaList_proc.jsp").forward(req, resp);
	}

	private void couponManager(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, SQLException, IOException {
		String action = req.getParameter("action");
		// TODO 쿠폰작업 마무리
		if ("mgr".equals(action)) { // 쿠폰 상단 목록
			couponMgrView(req, resp);
		} else if ("register".equals(action)) { // 쿠폰 생성
			couponMgrRegister(req, resp); 
		} else if ("registerBulk".equals(action)) { // csv 쿠폰 업데이트
			couponMgrRegisterBulk(req, resp); 
		} else if ("get".equals(action)) { // 쿠폰 검색
			couponMgrGet(req, resp); 
		} else if ("del".equals(action)) { // 쿠폰 삭제
			couponMgrDel(req, resp); 
		} else if ("use".equals(action)) { // 쿠폰 사용
			couponMgrUse(req, resp); 
		} else if ("generate".equals(action)) { // 쿠폰 랜덤 생성
			couponMgrGen(req, resp); 
		} else if ("adminToUser".equals(action)) { // 쿠폰 이전
			couponMgrTransfer(req, resp);
		}
	}
	/**
	 * 쿠폰 이전
	 * @param req
	 * @param resp
	 * @throws SQLException
	 * @throws IOException
	 */
	private void couponMgrTransfer(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
		String coupon_id = req.getParameter("coupon_id"); // 쿠폰아이디
		String phone = req.getParameter("phone"); // 전화번호
		
		System.out.println("------couponMgrTransfer START-------");
		boolean result = couponDAO.couponMgrTransfer(coupon_id, phone);
		System.out.println("------couponMgrTransfer END-------");
		
		if(result == true){
			resp.setContentType("text/html;charset=utf-8");
			resp.getWriter().println("쿠폰이전이 정상 처리 되었습니다.");
		}else{
			resp.setContentType("text/html;charset=utf-8");
			resp.getWriter().println("관리자에게 문의 바랍니다.");
		}
	}
	/**
	 * 쿠폰 랜덤 생성
	 * @param req
	 * @param resp
	 * @throws SQLException 
	 * @throws IOException 
	 */
	private void couponMgrGen(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
		String cnt = req.getParameter("count"); // 쿠폰갯수
		String lot = req.getParameter("lot"); // 가격

		int count = 0;
		int price = 0;
		try {
			count = Integer.parseInt(cnt);
			price = Integer.parseInt(lot);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("------couponMgrGen START-------");
		boolean result = couponDAO.couponMgrGen(count, price);
		System.out.println("------couponMgrGen END-------");
		
		if(result == true){
			resp.setContentType("text/html;charset=utf-8");
			resp.getWriter().println("쿠폰생성이 정상 처리 되었습니다.");
		}else{
			resp.setContentType("text/html;charset=utf-8");
			resp.getWriter().println("관리자에게 문의 바랍니다.");
		}
		
		//Random rand = new Random(System.currentTimeMillis());
		// 기존의 랜덤 로직
		/*char ret[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
				'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
				'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

		StringBuffer couponString = new StringBuffer();
		int i = 0;
		for (i = 0; i < count; i++) {
			couponString.append(lot+",");
			couponString.append(ret[rand.nextInt(36)]);
			couponString.append(ret[rand.nextInt(36)]);
			couponString.append(ret[rand.nextInt(36)]);
			couponString.append(ret[rand.nextInt(36)]);
			couponString.append(ret[rand.nextInt(36)]);
			couponString.append(ret[rand.nextInt(36)]);
			couponString.append("\n");
		}*/
	}

	private void couponMgrGet(HttpServletRequest req, HttpServletResponse resp)
			throws SQLException, ServletException, IOException {

		String column = req.getParameter("column");
		String value = req.getParameter("value");

		// There may be many coupons, so create a JSONArray
		JSONArray jsonArr = new JSONArray();
		jsonArr = couponDAO.get_coupon(column, value); 

		// StringBuffer To Create a JSONObj;
		// JSONObject printJSON = new JSONObject();
		req.setAttribute("list", jsonArr);

		// forward to ListView
		// req.getRequestDispatcher("./admin/couponManagerList_proc.jsp").forward(req,
		// resp);
		req.getRequestDispatcher("./admin/couponManager_proc.jsp").forward(req,
				resp);
		/**
		 * JSONObject json = new JSONObject(); json.put("Column", column);
		 * json.put("Value", value);
		 * 
		 * resp.getWriter().print(json.toString());
		 **/

	}

	private void couponMgrDel(HttpServletRequest req, HttpServletResponse resp)
			throws SQLException, ServletException, IOException {
		String coupon_id = req.getParameter("coupon_id");
		//String msgTitle = "msg1";

		// 쿠폰 존재여부 체크
		System.out.println("------couponMgrDel START-------");
		
		System.out.println("coupon_id: " + coupon_id);
		
		int res = couponDAO.check_coupon_duplicate(coupon_id);
		System.out.println("쿠폰아이디 존재여부: " + res);
		
		if (res > 0) {
			System.out.println("쿠폰아이디가 존재합니다.");
			couponDAO.delete_coupon(coupon_id);
			couponDAO.update_coupon_track(coupon_id, CouponDAO.COUPON_DEL, "del_by_admin");
			System.out.println("쿠폰 삭제가 정상 처리되었습니다.");
			
			resp.setContentType("text/html;charset=utf-8");
			resp.getWriter().println("쿠폰 삭제가 정상 처리되었습니다.");
			//req.setAttribute(msgTitle, "성공적으로 삭제되었습니다.");
			// Coupon tracking system update
		} else {
			System.out.println("쿠폰아이디가 존재하지 않습니다.");
			resp.setContentType("text/html;charset=utf-8");
			resp.getWriter().println("관리자에게 문의 바랍니다.");
			//req.setAttribute(msgTitle, "존재하지 않는 쿠폰입니다.");
		}
		
		System.out.println("------couponMgrDel END-------");

		/*req.getRequestDispatcher("./admin/couponManager_proc.jsp").forward(req,
				resp);*/
	}

	private void couponMgrUse(HttpServletRequest req, HttpServletResponse resp)
			throws SQLException, ServletException, IOException {
		String coupon_id = req.getParameter("coupon_id");
		String driver_id = req.getParameter("driver_id");
		String customer_phone = req.getParameter("customer_phone");
		
		System.out.println("coupon_id: " + coupon_id);
		System.out.println("driver_id: " + driver_id);
		System.out.println("customer_phone: " + customer_phone);
		
		if(driver_id.equals("null") || driver_id.length() == 0)
			driver_id = "admin_driver";
		
		System.out.println("------couponMgrUse START-------");
		
		System.out.println("coupon_id: " + coupon_id);
		System.out.println("driver_id: " + driver_id);
		System.out.println("customer_phone: " + customer_phone);
		
		int chkUsed = couponDAO.check_coupon_used(coupon_id);
		int chkDuplicate = couponDAO.check_coupon_duplicate(coupon_id);
		System.out.println("쿠폰아이디가 이미 사용중인지 체크: " + chkUsed);
		System.out.println("쿠폰아이디 존재여부: " + chkDuplicate);

		if (chkUsed > 0 || chkDuplicate == 0) {
			// If the coupon is already used or there's no matching coupon,
			// return fail.
			if (chkUsed > 0){
				
				System.out.println("이미 사용된 쿠폰입니다.");
				
				resp.setContentType("text/html;charset=utf-8");
				resp.getWriter().println("이미 사용된 쿠폰입니다.");
				//req.setAttribute(msgTitle, "이미 사용된 쿠폰입니다.");
			}
			if (chkDuplicate == 0){
				
				System.out.println("유효하지 않은 쿠폰입니다.(존재하지 않습니다)");
				
				resp.setContentType("text/html;charset=utf-8");
				resp.getWriter().println("유효하지 않은 쿠폰입니다.(존재하지 않습니다)");
				//req.setAttribute(msgTitle, "유효하지 않은 쿠폰입니다.(존재하지 않습니다)");
			}
		} else {

			// 20111217 Settlement 테이블의 용도는 기사의 충전금액 결제 입니다.
			// 고객과 기사 간, 대리이력은 charge_history 입니다.
			// 20111217 CLX Coupon 가격을 기사의 충전금액으로 충전해주는 기능입니다.
			System.out.println("pre_settlement start: "+driver_id);
			int oid = settlementDAO.startSettlement(driver_id); // Create a new
																// OID with
																// given
																// DriverID
			System.out.println("pre_settlement complete: "+oid);

			System.out.println("settlement start");
			int res = couponDAO.settlement_coupon(coupon_id, oid); // Using this
																	// OID,
																	// process
																	// settlement
																	// by
																	// coupon.
			System.out.println("settlement complete: " + res);
			
			System.out.println("coupon_id: "+coupon_id);
			System.out.println("driver_id: "+driver_id);
			System.out.println("customer_phone: "+customer_phone);
			
			couponDAO.use_coupon(coupon_id, driver_id, customer_phone); // Set
																		// the
																		// coupon
																		// state=
																		// USED
			couponDAO.update_coupon_track(coupon_id, CouponDAO.COUPON_USE,
					driver_id); // Update coupon tracking system
			Driver driver = driverDAO.getDriverInfo(driver_id); // Get my driver
			int chargeSum = driver.getChargeSum()
					+ couponDAO.get_coupon_amount(coupon_id); // Add Coupon
																// Amount to
																// ChargeSum
			driverDAO.updateChargeSum(chargeSum, driver_id); // Update
																// ChargeSum.

			if (res > 0){
				System.out.println("성공적으로 사용 처리 되었습니다.");
				
				resp.setContentType("text/html;charset=utf-8");
				resp.getWriter().println("성공적으로 사용 처리 되었습니다.");
				//req.setAttribute(msgTitle, "성공적으로 사용 처리 되었습니다.");
			}
			else{
				System.out.println("실패하였습니다.");
				
				resp.setContentType("text/html;charset=utf-8");
				resp.getWriter().println("실패하였습니다.");
				//req.setAttribute(msgTitle, "실패하였습니다.");
			}
		}

		System.out.println("------couponMgrUse END-------");
		
		/*req.getRequestDispatcher("./admin/couponManager_proc.jsp").forward(req,
				resp);*/
	}

	private void couponMgrRegisterBulk(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, IOException,
			ServletException {
		String msgTitle = "msg1";
		BufferedReader bufRdr = new BufferedReader(new InputStreamReader(
				req.getInputStream(), "euc-kr"));
		String str = couponDAO.register_coupon_bulk(bufRdr);
		if (str == null) {
			req.setAttribute(msgTitle, "성공적으로 업로드 되었습니다.");
		} else {
			req.setAttribute(msgTitle, str);
		}
		req.getRequestDispatcher("./admin/couponManager_proc.jsp").forward(req,
				resp);
	}

	/**
	 * 쿠폰 화면 리스트
	 * 
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 * @throws SQLException
	 */
	private void couponMgrView(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException, SQLException {

		// 페이징 처리
		String pageNum = req.getParameter("curr_page");
		String _pageSize = req.getParameter("pageSize");
		if (pageNum == null) {
			pageNum = "1";
		}
		if (_pageSize == null) {
			_pageSize = "10";
		}

		int pageSize = Integer.parseInt(_pageSize);
		int currentPage = Integer.parseInt(pageNum);

		// 정보
		String gubun = req.getParameter("gubun"); // 전체, 사용, 미사용
		String column = req.getParameter("column"); // 선택항목 : 쿠폰번호, 고객전화, 고객이름
		String value = req.getParameter("value"); // 키워드

		if (gubun == null)
			gubun = "";
		if (column == null)
			column = "";
		if (value == null)
			value = "";

		// 한페이지에 출력되는 목록
		Coupon[] cp = couponDAO.getCouponList(pageSize, currentPage, gubun, column, value); // 리스트
		// 전체 레코드 수
		long plTotalCnt = couponDAO.getCouponCount(gubun, column, value); // 총갯수
		
		System.out.println("cp===" + cp.length);
		System.out.println("plTotalCnt===" + plTotalCnt);
		
		req.setAttribute("plTotalCnt", new Long(plTotalCnt));
		req.setAttribute("pageNum", new Long(currentPage));
		req.setAttribute("pageSize", new Long(pageSize));

		req.setAttribute("list", cp);
		req.setAttribute("gubun", gubun);
		req.setAttribute("column", column);
		req.setAttribute("value", value);

		req.getRequestDispatcher("./admin/couponManager_proc.jsp").forward(req,	resp);
	}

	private void couponMgrRegister(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, ServletException,
			IOException {
		// 2011.12.13 We only get <coupon_id, amount> and we will later update
		// the customer info.
		String coupon_id = req.getParameter("coupon_id");
		String amount = req.getParameter("amount");
		String msgTitle = "msg1";

		// if there is a duplicate COUPON_ID : resCount > 0
		int resCount = couponDAO.check_coupon_duplicate(coupon_id);

		if (resCount > 0) {
			// Notify that there is a duplicate coupon.
			req.setAttribute(msgTitle, "중복된 쿠폰입니다.");
		} else {
			// There is no duplicate coupon, try SQL
			// int result = coupon.register_coupon(coupon_id, cPhone, cName,
			// amount);
			int result = couponDAO.register_coupon(coupon_id, amount);
			if (result == 0) {
				req.setAttribute(msgTitle, "유효하지 않은 쿠폰 번호입니다.");
			} else {
				// returned value is 1
				// update coupon tracking system
				req.setAttribute(msgTitle, "성공적으로 등록되었습니다.");
				couponDAO.update_coupon_track(coupon_id, CouponDAO.COUPON_NEW,
						"reg_by_admin");
			}
		}
		req.getRequestDispatcher("./admin/couponManager_proc.jsp").forward(req,
				resp);
	}
	/**
	 * 대리운전 조회
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 * @throws SQLException
	 */
	private void driveDetail(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException, SQLException {
		String mode = req.getParameter("mode");
		if (mode.equals("today")) { // 일일조회
			driveDetailToday(req, resp);
		} else if (mode.equals("day")) { // 일별조회
			driveDetailDay(req, resp);
		} else if (mode.equals("period")) { // 기간조회
			driveDetailPeriod(req, resp);
		} else if (mode.equals("month")) { // 월별조회
			driveDetailMonth(req, resp);
		} else if (mode.equals("driverDetailView")) { // 일별상세조회
			driveDetailView(req, resp);
		}
	}
	/**
	 * 일별 대리운전 상세정보 페이지
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	private void driveDetailView(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		String driverId = req.getParameter("driverId");
		String BID = req.getParameter("BID");

		// 정보
		try {
			ChargeHistory ch = chargeHistoryDAO.getChargeHistoryDetail(driverId, BID);
			req.setAttribute("ch", ch);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		req.getRequestDispatcher("./admin/driveDetailView.jsp").forward(
				req, resp);
	}

	/**
	 * 일일 대리운전 정보
	 * 
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */

	private void driveDetailToday(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {

		// 페이징
		/*
		 * 조회 당일 현재 대리운전 연결현황을 보여줌 고객의 대리요청중, 대리기사의 대기중, 고객과 대리기사가 서비스중인 상태를
		 * 보여줌. 상태 : 서비스, 요청, 대기. 기본은 서비스
		 */
		String pageNum = req.getParameter("curr_page");
		String _pageSize = req.getParameter("pageSize");
		if (pageNum == null) {
			pageNum = "1";
		}
		if (_pageSize == null) {
			_pageSize = "10";
		}

		int pageSize = Integer.parseInt(_pageSize);
		int currentPage = Integer.parseInt(pageNum);

		HttpSession session = req.getSession();

		// 정보
		String dateA = req.getParameter("dateA");
		String dateB = req.getParameter("dateB");
		String local = req.getParameter("local");

		String result = req.getParameter("result");
		String name = req.getParameter("name");

		if (dateA == null || dateA.length() == 0) {
			dateA = Util.getCurrDate();
		}
		if (dateB == null || dateB.length() == 0) {
			dateB = Util.getCurrDate();
		}
		if (local == null || local.length() == 0) {
			// local = "%";
			local = (String) session.getAttribute("location");
		}
		if (result == null || result.length() == 0) {
			result = "%";
		}
		// session.setAttribute("location", local);

		String sido = req.getParameter("sido");
		String gugun = req.getParameter("gugun");
		String dong = req.getParameter("dong");
		String Lat = req.getParameter("Lat");
		String Lng = req.getParameter("Lng");
		String LID = req.getParameter("LID");
		try {
			// ChargeHistory[] ch = chargeHistoryDAO.getChargeHistory(date,
			// local, result, null, name, currentPage, pageSize);
			ChargeHistory[] ch = chargeHistoryDAO.getChargeHistoryDay(dateA,
					dateB, local, result, null, name, currentPage, pageSize);
			long plTotalCnt = chargeHistoryDAO.getCHNumPeriod(dateA, dateB,
					local, result, null, name);

			req.setAttribute("plTotalCnt", new Long(plTotalCnt));
			req.setAttribute("pageNum", new Long(currentPage));
			req.setAttribute("pageSize", new Long(pageSize));
			req.setAttribute("list", ch);
			req.setAttribute("dateA", dateA);
			req.setAttribute("dateB", dateB);
			req.setAttribute("local", local);

			JSONArray ar = chargeHistoryDAO.getLocationInfoByLocaName(sido,
					gugun, dong);
			req.setAttribute("sido", sido);
			req.setAttribute("gugun", gugun);
			req.setAttribute("dong", dong);
			
			req.setAttribute("Lat", Lat);
			req.setAttribute("Lng", Lng);
			req.setAttribute("LID", Lng);
		} catch (SQLException e) {
			e.printStackTrace();
			req.setAttribute("msg1", "조회에 실패했습니다.\\n잠시 후에  다시 시도해주세요");
		}

		req.getRequestDispatcher("./admin/driveDetail_today_proc.jsp").forward(
				req, resp);
	}

	/**
	 * 일별 대리운전 정보
	 * 
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	private void driveDetailDay(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// 페이징
		// TODO 기간으로 해달라고함
		String pageNum = req.getParameter("curr_page");
		String _pageSize = req.getParameter("pageSize");
		if (pageNum == null) {
			pageNum = "1";
		}
		if (_pageSize == null) {
			_pageSize = "10";
		}

		int pageSize = Integer.parseInt(_pageSize);
		int currentPage = Integer.parseInt(pageNum);

		HttpSession session = req.getSession();

		// 정보
		String dateA = req.getParameter("dateA");
		String dateB = req.getParameter("dateB");
		String local = req.getParameter("local");
		String result = req.getParameter("result");
		String name = req.getParameter("name");

		if (dateA == null || dateA.length() == 0) {
			dateA = Util.getCurrDate();
		}
		if (dateB == null || dateB.length() == 0) {
			dateB = Util.getCurrDate();
		}
		if (local == null || local.length() == 0) {
			// local = "%";
			local = (String) session.getAttribute("location");
		}
		if (result == null || result.length() == 0) {
			result = "%";
		}
		// session.setAttribute("location", local);

		try {
			// ChargeHistory[] ch = chargeHistoryDAO.getChargeHistory(date,
			// local, result, null, name, currentPage, pageSize);
			ChargeHistory[] ch = chargeHistoryDAO.getChargeHistoryDay(dateA,
					dateB, local, result, null, name, currentPage, pageSize);
			long plTotalCnt = chargeHistoryDAO.getCHNumPeriod(dateA, dateB,
					local, result, null, name);

			req.setAttribute("plTotalCnt", new Long(plTotalCnt));
			req.setAttribute("pageNum", new Long(currentPage));
			req.setAttribute("pageSize", new Long(pageSize));
			req.setAttribute("list", ch);
			req.setAttribute("dateA", dateA);
			req.setAttribute("dateB", dateB);
			req.setAttribute("local", local);
			req.setAttribute("name", name);

		} catch (SQLException e) {
			e.printStackTrace();
			req.setAttribute("msg1", "조회에 실패했습니다.\\n잠시 후에  다시 시도해주세요");
		}

		req.getRequestDispatcher("./admin/driveDetail_day_proc.jsp").forward(
				req, resp);
	}

	/**
	 * 기간별 대리운전 정보
	 * 
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 * @throws SQLException
	 */
	private void driveDetailPeriod(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException,
			SQLException {
		// 정보
		String dateA = req.getParameter("dateA");
		String dateB = req.getParameter("dateB");
		String local = req.getParameter("local");
		String result = req.getParameter("result");

		HttpSession session = req.getSession();

		// 날짜가 들어오지 않을 경우 오늘 날짜를 입력.
		// 날짜는 dateA가 dateB보다 과거이어야 한다. WEB단에서 날짜 비교 필터링 할것.
		if (dateA == null || dateA.length() == 0) {
			dateA = Util.getCurrDate();
			// dateA = "2012-01-05";
		}
		if (dateB == null || dateB.length() == 0) {
			dateB = Util.getCurrDate();
			// dateB = "2012-01-05";
		}
		if (result == null || result.length() == 0) {
			result = "%";
		}
		if (local == null || local.length() == 0) {
			// local = "%";
			local = (String) session.getAttribute("location");
		}

		try {
			ConcurrentHashMap<String, ChargeHistoryPeriod> ch = chargeHistoryDAO
					.getChargeHistoryPeriod(dateA, dateB, local, result);
			req.setAttribute("list", ch);
			req.setAttribute("dateA", dateA);
			req.setAttribute("dateB", dateB);
			req.setAttribute("local", local);

		} catch (SQLException e) {
			e.printStackTrace();
			req.setAttribute("msg1", "조회에 실패했습니다.\\n잠시 후에  다시 시도해주세요");
		} catch (Exception e) {
			e.printStackTrace();
		}

		req.getRequestDispatcher("./admin/driveDetail_period_proc.jsp")
				.forward(req, resp);

	}

	private void driveDetailMonth(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {

		// 정보
		String date = req.getParameter("date");
		String local = req.getParameter("local");
		String result = req.getParameter("result");

		HttpSession session = req.getSession();

		// 날짜가 들어오지 않을 경우 오늘 날짜를 입력.
		// 날짜는 dateA가 dateB보다 과거이어야 한다. WEB단에서 날짜 비교 필터링 할것.
		if (date == null || date.length() == 0) {
			date = Util.getCurrDate();
			// dateA = "2012-01-05";
		}
		if (result == null || result.length() == 0) {
			result = "%";
		}
		if (local == null || local.length() == 0) {
			// local = "%";
			local = (String) session.getAttribute("local");
		}

		try {
			ConcurrentHashMap<String, ChargeHistoryPeriod> ch = chargeHistoryDAO
					.getChargeHistoryMonth(date, local, result);

			req.setAttribute("list", ch);
			req.setAttribute("date", date);
			req.setAttribute("local", local);

		} catch (SQLException e) {
			e.printStackTrace();
			req.setAttribute("msg1", "조회에 실패했습니다.\\n잠시 후에  다시 시도해주세요");
		} catch (Exception e) {
			e.printStackTrace();
		}

		req.getRequestDispatcher("./admin/driveDetail_month_proc.jsp").forward(
				req, resp);

	}

	/**
	 * 대리기사 관리자
	 * 
	 * @param req
	 * @param resp
	 * @throws SQLException
	 * @throws ServletException
	 * @throws IOException
	 */
	private void driverManager(HttpServletRequest req, HttpServletResponse resp)
			throws SQLException, ServletException, IOException {
		// 모드를 가져온다
		String mode = req.getParameter("mode");
		if (mode.equals("driverList")) {
			// 대리기사 목록 모드
			driverList(req, resp);
		} else if (mode.equals("certify")) {
			// 신원관리
			driverCertify(req, resp);
		} else if (mode.equals("chargeList")) {
			driverChargeList(req, resp);
			// 충전금내역
		} else if (mode.equals("settlementList")) {
			// 대리기사가 입출금 관리 모드
			getSettlementList(req, resp);
		} else if (mode.equals("update")) {
			// 대리기사 수정
			driverUpdate(req, resp);
		} else if (mode.equals("updateDetail")) {
			// 대리기사 수정 상세보기
			driverUpdateDetail(req, resp);
		} else if (mode.equals("delete")) {
			// 대리기사 삭제
			driverDelete(req, resp);
		} else if (mode.equals("insert")) {
			// 대리기사 추가
			driverInsert(req, resp);
		} else if (mode.equals("levelUp")) { // 승인상태로 변환
			// 대리기사 승인/미승인 수정
			driverLevelup(req, resp);
		} else if (mode.equals("chargeSum")) { // 충전금
			// 대리기사 승인/미승인 수정
			driverChargeSum(req, resp);
		} else {
			// 알수 없는 모드
			try {
				resp.getWriter().print("Invalid Mode");
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}
	/**
	 * 충전금 누적 수정
	 * @param req
	 * @param resp
	 * @throws SQLException
	 * @throws ServletException
	 * @throws IOException
	 */
	private void driverChargeSum(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, ServletException,
			IOException {
		String driverId = req.getParameter("driverId"); // driverID
		String temp = req.getParameter("charge"); // 충전금
		
		// 로그
		System.out.println("driverId=="+driverId);
		System.out.println("charge=="+temp);
		
		Driver driver = new Driver();
		driver.setDriverId(driverId);
		
		// 충전금
		int charge=0;
		try {
			charge = Integer.parseInt(temp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		driver.setChargeSum(charge);
		
		System.out.println("------driverChargeSum START-------");
		boolean result = driverDAO.updateChargeSum(driver);
		System.out.println("------driverChargeSum END-------");
		if(result){
			// 한글을 보내기 위해서는 꼭 선언해야 함.
			resp.setContentType("text/html;charset=utf-8");
			resp.getWriter().println("대리기사 충전금이 정상 처리되었습니다.");
		}else{
			resp.setContentType("text/html;charset=utf-8");
			resp.getWriter().println("관리자에게 문의 바랍니다.");
		}
		
	}
	/**
	 * 대리기사 승인/미승인 수정
	 * @param req
	 * @param resp
	 * @throws SQLException
	 * @throws ServletException
	 * @throws IOException
	 */
	private void driverLevelup(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, ServletException,
			IOException {
		String driverId = req.getParameter("driverId"); // driverID
		String temp = req.getParameter("level"); // level상태값 0또는1이 와야함.
		
		// 로그
		System.out.println("driverId=="+driverId);
		System.out.println("level=="+temp);
		
		Driver driver = new Driver();
		driver.setDriverId(driverId);
		
		// 승인상태
		int level=0;
		try {
			level = Integer.parseInt(temp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		driver.setLevel(level);
		
		System.out.println("------driverLevelup START-------");
		boolean result = driverDAO.updateLevel(driver);
		System.out.println("------driverLevelup END-------");
		if(result){
			// 한글을 보내기 위해서는 꼭 선언해야 함.
			resp.setContentType("text/html;charset=utf-8");
			resp.getWriter().println("대리기사 승인상태가 정상 처리되었습니다.");
		}else{
			resp.setContentType("text/html;charset=utf-8");
			resp.getWriter().println("관리자에게 문의 바랍니다.");
		}
	}
	
	/**
	 * 대리기사 수정 상세보기
	 * @param req
	 * @param resp
	 * @throws SQLException
	 * @throws ServletException
	 * @throws IOException
	 */
	private void driverUpdateDetail(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, ServletException,
			IOException {
		String driverId = req.getParameter("driverId"); // driverID
		System.out.println("------driverUpdateDetail START-------");
		Driver driver = driverDAO.getDriverInfo(driverId);
		System.out.println("------driverUpdateDetail END-------");
		// 서버로그
		System.out.println("getDevId===" + driver.getDevId());
		System.out.println("getLicenseType===" + driver.getLicenseType());
		System.out.println("getPasswd===" + driver.getPasswd());
		System.out.println("getLiceseNo===" + driver.getLiceseNo());
		System.out.println("getAuthorizationNo===" + driver.getAuthorizationNo());
		System.out.println("getName===" + driver.getName());
		System.out.println("getResidentNo===" + driver.getResidentNo());
		System.out.println("getAssurance_complete_date===" + driver.getAssurance_complete_date());
		System.out.println("getPhone===" + driver.getPhone());
		System.out.println("getAmount===" + driver.getAmount());
		System.out.println("getAddress===" + driver.getAddress());
		System.out.println("getLicensePic===" + driver.getLicensePic());
		System.out.println("getEmail===" + driver.getEmail());
		// 웹으로 데이터 전송
		req.setAttribute("driverId", driver.getDevId()); // 아이디
		req.setAttribute("passwd", driver.getPasswd()); // 비밀번호
		req.setAttribute("name", driver.getName()); // 이름
		req.setAttribute("residentNo", driver.getResidentNo()); // 주민번호
		req.setAttribute("phone", driver.getPhone()); // 전화번호
		req.setAttribute("address", driver.getAddress()); // 주소
		req.setAttribute("email", driver.getEmail()); // 이메일
		req.setAttribute("licenseType", driver.getLicenseType()+""); // 면허종류 : 숫자로 되어 있기때문에 문자로 변환하여 전송한다.
		req.setAttribute("liceseNo", driver.getLiceseNo()); // 면허번호
		req.setAttribute("authorizationName", ""); // 보험이름
		req.setAttribute("authorizationNo", driver.getAuthorizationNo()); // 보험번호
		req.setAttribute("assurance_complete_date", driver.getAssurance_complete_date()); // 보험완료일
		req.setAttribute("amount", driver.getAmount());// 충전금
		req.setAttribute("licensePic", driver.getLicensePic()); // 면허증 사진 주소

		req.getRequestDispatcher("./admin/driverUpdate_view.jsp").forward(req,resp);
	}

	/**
	 * 대리기사 수정 처리
	 * @param req
	 * @param resp
	 * @throws SQLException
	 * @throws ServletException
	 * @throws IOException
	 */
	private void driverUpdate(HttpServletRequest req, HttpServletResponse resp)
			throws SQLException, ServletException, IOException {
		
		String driverId = null; // driverID
		String licenseType = null; // 면허종류
		String passwd = null; // 비밀번호
		String liceseNo = null; // 면허번호
		String authorizationNo = null; // 증권번호(보험번호)
		String name = null; // 이름
		String residentNo1 = null; // 주민번호
		String residentNo2 = null; // 주민번호
		String assurance_complete_date = null; // 보험만료일
		String phone1 = null; // 전화번호
		String phone2 = null; // 전화번호
		String phone3 = null; // 전화번호
		String amount = null; // 충전금(현금)
		String address = null; // 주소
		String email = null; // 이메일
		String picture = null; // 사진Path
		
		try {
			req.setCharacterEncoding("UTF-8");
			// Create a factory for disk-based file items
			DiskFileItemFactory factory = new DiskFileItemFactory();
			// Set factory constraints
			factory.setSizeThreshold(1024 * 1024 * 10);
			factory.setRepository(new File(getServletContext().getRealPath(
					"/WEB-INF/uploadData")));
			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);
			// Set overall request size constraint
			upload.setSizeMax(-1);
			@SuppressWarnings("rawtypes")
			List parameters = upload.parseRequest(req);
			for (int i = 0; i < parameters.size(); i++) {
				FileItem fileItem = (FileItem) parameters.get(i);
				// 첨부파일 체크
				if (fileItem.isFormField()) {
					String fieldName = fileItem.getFieldName();
					if ("driverId".equals(fieldName)) {
						driverId = fileItem.getString("utf-8");
					} else if("licenseType".equals(fieldName)) {
						licenseType = fileItem.getString("utf-8");
					} else if("passwd".equals(fieldName)) {
						passwd = fileItem.getString("utf-8");
					} else if("liceseNo".equals(fieldName)) {
						liceseNo = fileItem.getString("utf-8");
					} else if("authorizationNo".equals(fieldName)) {
						authorizationNo = fileItem.getString("utf-8");
					} else if("name".equals(fieldName)) {
						name = fileItem.getString("utf-8");
					} else if("residentNo1".equals(fieldName)) {
						residentNo1 = fileItem.getString("utf-8");
					} else if("residentNo2".equals(fieldName)) {
						residentNo2 = fileItem.getString("utf-8");
					} else if("assurance_complete_date".equals(fieldName)) {
						assurance_complete_date = fileItem.getString("utf-8");
					} else if("phone1".equals(fieldName)) {
						phone1 = fileItem.getString("utf-8");
					} else if("phone2".equals(fieldName)) {
						phone2 = fileItem.getString("utf-8");
					} else if("phone3".equals(fieldName)) {
						phone3 = fileItem.getString("utf-8");
					} else if("amount".equals(fieldName)) {
						amount = fileItem.getString("utf-8");
					} else if("address".equals(fieldName)) {
						address = fileItem.getString("utf-8");
					} else if("email".equals(fieldName)) {
						email = fileItem.getString("utf-8");
					}
					
				} else {
					// 로그
					System.out.println("driverId=="+driverId);
					System.out.println("licenseType=="+licenseType);
					System.out.println("passwd=="+passwd);
					System.out.println("liceseNo=="+liceseNo);
					System.out.println("authorizationNo=="+authorizationNo);
					System.out.println("name=="+name);
					System.out.println("residentNo1=="+residentNo1);
					System.out.println("residentNo2=="+residentNo2);
					System.out.println("assurance_complete_date=="+assurance_complete_date);
					System.out.println("phone1=="+phone1);
					System.out.println("phone2=="+phone2);
					System.out.println("phone3=="+phone3);
					System.out.println("amount=="+amount);
					System.out.println("address=="+address);
					System.out.println("email=="+email);

					if (fileItem.getSize() > 0 && driverId != null) {
						logger.debug("Image file found... fieldName:"
								+ fileItem.getName() + " /fileSize:"
								+ fileItem.getSize());
						String fileName = fileItem.getName(); // Image
						int idx = fileName.lastIndexOf('\\');
						if (idx != -1) {
							fileName = fileName.substring(idx + 1);
						}

						File folder = new File("/carcaro/picture");
						if (!folder.exists()) {
							folder.mkdirs();
						}

						int dotposition = fileName.lastIndexOf(".");
						String extension = fileName.substring(dotposition,
								fileName.length());
						picture = driverId + '_' + System.currentTimeMillis()
								+ extension;
						File file = new File(folder, picture); // driverId

						OutputStream out = new BufferedOutputStream(
								new FileOutputStream(file));
						InputStream in = new BufferedInputStream(
								fileItem.getInputStream());

						byte[] buf = new byte[8192];
						int len;
						while ((len = in.read(buf)) != -1) {
							out.write(buf, 0, len);
						}
						in.close();
						out.close();
						logger.debug("Image upload complete :" + picture);
					}
				}
			}
			logger.debug("--->> UpdateDriverPic: driverId:" + driverId
					+ " /Pic:" + picture);
			
			String residentNo = residentNo1 + residentNo2;
			String phone = phone1 + phone2 + phone3;
			
			Driver driver = new Driver();
			driver.setDriverId(driverId); // 드라이버 아이디
			driver.setLicenseType(1); // 면허 종류
			driver.setPasswd(passwd); // 비밀번호
			driver.setLiceseNo(liceseNo); // 면허증 번호
			driver.setAuthorizationNo(authorizationNo); // 보험 번호
			driver.setName(name); // 이름
			driver.setAssurance_complete_date(assurance_complete_date); // 보험 완료일
			driver.setResidentNo(residentNo); // 주민번호
			driver.setPhone(phone); // 전화번호
			driver.setAddress(address); // 주소
			driver.setEmail(email);
			
			System.out.println("------driverUpdate START-------");
			boolean result = driverDAO.updateDriver(driver);
			System.out.println("------driverUpdate START-------");
			if(result){
				// 한글을 보내기 위해서는 꼭 선언해야 함.
				resp.setContentType("text/html;charset=utf-8");
				resp.getWriter().println("대리기사 수정이 정상 처리되었습니다.");
			}else{
				resp.setContentType("text/html;charset=utf-8");
				resp.getWriter().println("관리자에게 문의 바랍니다.");
			}
			
		} catch (Exception e) {
			logger.error("driverUpdate error." + e.getMessage());
		}
		
/*		String driverId = req.getParameter("driverId"); // driverID
		String licenseType = req.getParameter("licenseType"); // 면허종류
		String passwd = req.getParameter("passwd"); // 비밀번호
		String liceseNo = req.getParameter("liceseNo"); // 면허번호
		String authorizationNo = req.getParameter("authorizationNo"); // 증권번호(보험번호)
		String name = req.getParameter("name"); // 이름
		String residentNo1 = req.getParameter("residentNo1"); // 주민번호
		String residentNo2 = req.getParameter("residentNo2"); // 주민번호
		String assurance_complete_date = req.getParameter("assurance_complete_date"); // 보험만료일
		String phone1 = req.getParameter("phone1"); // 전화번호
		String phone2 = req.getParameter("phone2"); // 전화번호
		String phone3 = req.getParameter("phone3"); // 전화번호
		String amount = req.getParameter("amount"); // 충전금(현금)
		String address = req.getParameter("address"); // 주소
		String email = req.getParameter("email"); // 이메일
		// String assurance = req.getParameter("assurance"); // 보험만료일 - 보험번호랑 겹침
		//String licensePic = req.getParameter("licensePic"); // 면허증사진
		
		// 로그
		System.out.println("driverId=="+driverId);
		System.out.println("licenseType=="+licenseType);
		System.out.println("passwd=="+passwd);
		System.out.println("liceseNo=="+liceseNo);
		System.out.println("authorizationNo=="+authorizationNo);
		System.out.println("name=="+name);
		System.out.println("residentNo1=="+residentNo1);
		System.out.println("residentNo2=="+residentNo2);
		System.out.println("assurance_complete_date=="+assurance_complete_date);
		System.out.println("phone1=="+phone1);
		System.out.println("phone2=="+phone2);
		System.out.println("phone3=="+phone3);
		System.out.println("amount=="+amount);
		System.out.println("address=="+address);
		System.out.println("email=="+email);
		
		String residentNo = residentNo1 + residentNo2;
		String phone = phone1 + phone2 + phone3;

		Driver driver = new Driver();
		driver.setDriverId(driverId); // 드라이버 아이디
		driver.setLicenseType(1); // 면허 종류
		driver.setPasswd(passwd); // 비밀번호
		driver.setLiceseNo(liceseNo); // 면허증 번호
		driver.setAuthorizationNo(authorizationNo); // 보험 번호
		driver.setName(name); // 이름
		driver.setAssurance_complete_date(assurance_complete_date); // 보험 완료일
		driver.setResidentNo(residentNo); // 주민번호
		driver.setPhone(phone); // 전화번호
		driver.setAddress(address); // 주소
		driver.setEmail(email);
		
		// 충전금 내역과 사진은 이곳에서 같이 등록을 해야 하는지 아니면 분리를 해야 하는지?
		//driver.setAmount(amount); // 충전금내역?
		// driver.setLicensePic(licensePic);

		System.out.println("------driverUpdate START-------");
		boolean result = driverDAO.updateDriver(driver);
		System.out.println("------driverUpdate START-------");
		if(result){
			// 한글을 보내기 위해서는 꼭 선언해야 함.
			resp.setContentType("text/html;charset=utf-8");
			resp.getWriter().println("대리기사 수정이 정상 처리되었습니다.");
		}else{
			resp.setContentType("text/html;charset=utf-8");
			resp.getWriter().println("관리자에게 문의 바랍니다.");
		}
		*/
	}

	/**
	 * 대리기사 삭제 처리
	 * @param req
	 * @param resp
	 * @throws SQLException
	 * @throws ServletException
	 * @throws IOException
	 */
	private void driverDelete(HttpServletRequest req, HttpServletResponse resp)
			throws SQLException, ServletException, IOException {

		String driverId = req.getParameter("driverId"); // driverID
		Driver driver = new Driver();
		driver.setDriverId(driverId);

		System.out.println("------driverDelete START-------");
		boolean result = driverDAO.deleteDriver(driver);
		System.out.println("------driverDelete START-------");
		if(result){
			// 한글을 보내기 위해서는 꼭 선언해야 함.
			resp.setContentType("text/html;charset=utf-8");
			resp.getWriter().println("대리기사 삭제가 정상 처리되었습니다.");
		}else{
			resp.setContentType("text/html;charset=utf-8");
			resp.getWriter().println("관리자에게 문의 바랍니다.");
		}
	}

	/**
	 * 대리기사 등록
	 * @param req
	 * @param resp
	 * @throws SQLException
	 * @throws ServletException
	 * @throws IOException
	 */
	private void driverInsert(HttpServletRequest req, HttpServletResponse resp)
			throws SQLException, ServletException, IOException {
		
		String driverId = null; // driverID
		String licenseType = null; // 면허종류
		String passwd = null; // 비밀번호
		String liceseNo = null; // 면허번호
		String authorizationNo = null; // 증권번호(보험번호)
		String name = null; // 이름
		String residentNo1 = null; // 주민번호
		String residentNo2 = null; // 주민번호
		String assurance_complete_date = null; // 보험만료일
		String phone1 = null; // 전화번호
		String phone2 = null; // 전화번호
		String phone3 = null; // 전화번호
		String amount = null; // 충전금(현금)
		String address = null; // 주소
		String email = null; // 이메일
		String picture = null; // 사진Path
		
		try {
			req.setCharacterEncoding("UTF-8");
			// Create a factory for disk-based file items
			DiskFileItemFactory factory = new DiskFileItemFactory();
			// Set factory constraints
			factory.setSizeThreshold(1024 * 1024 * 10);
			factory.setRepository(new File(getServletContext().getRealPath(
					"/WEB-INF/uploadData")));
			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);
			// Set overall request size constraint
			upload.setSizeMax(-1);
			@SuppressWarnings("rawtypes")
			List parameters = upload.parseRequest(req);
			for (int i = 0; i < parameters.size(); i++) {
				FileItem fileItem = (FileItem) parameters.get(i);
				// 첨부파일 체크
				if (fileItem.isFormField()) {
					String fieldName = fileItem.getFieldName();
					if ("driverId".equals(fieldName)) {
						driverId = fileItem.getString("utf-8");
					} else if("licenseType".equals(fieldName)) {
						licenseType = fileItem.getString("utf-8");
					} else if("passwd".equals(fieldName)) {
						passwd = fileItem.getString("utf-8");
					} else if("liceseNo".equals(fieldName)) {
						liceseNo = fileItem.getString("utf-8");
					} else if("authorizationNo".equals(fieldName)) {
						authorizationNo = fileItem.getString("utf-8");
					} else if("name".equals(fieldName)) {
						name = fileItem.getString("utf-8");
					} else if("residentNo1".equals(fieldName)) {
						residentNo1 = fileItem.getString("utf-8");
					} else if("residentNo2".equals(fieldName)) {
						residentNo2 = fileItem.getString("utf-8");
					} else if("assurance_complete_date".equals(fieldName)) {
						assurance_complete_date = fileItem.getString("utf-8");
					} else if("phone1".equals(fieldName)) {
						phone1 = fileItem.getString("utf-8");
					} else if("phone2".equals(fieldName)) {
						phone2 = fileItem.getString("utf-8");
					} else if("phone3".equals(fieldName)) {
						phone3 = fileItem.getString("utf-8");
					} else if("amount".equals(fieldName)) {
						amount = fileItem.getString("utf-8");
					} else if("address".equals(fieldName)) {
						address = fileItem.getString("utf-8");
					} else if("email".equals(fieldName)) {
						email = fileItem.getString("utf-8");
					}
					
				} else {
					// 로그
					System.out.println("driverId=="+driverId);
					System.out.println("licenseType=="+licenseType);
					System.out.println("passwd=="+passwd);
					System.out.println("liceseNo=="+liceseNo);
					System.out.println("authorizationNo=="+authorizationNo);
					System.out.println("name=="+name);
					System.out.println("residentNo1=="+residentNo1);
					System.out.println("residentNo2=="+residentNo2);
					System.out.println("assurance_complete_date=="+assurance_complete_date);
					System.out.println("phone1=="+phone1);
					System.out.println("phone2=="+phone2);
					System.out.println("phone3=="+phone3);
					System.out.println("amount=="+amount);
					System.out.println("address=="+address);
					System.out.println("email=="+email);

					if (fileItem.getSize() > 0 && driverId != null) {
						logger.debug("Image file found... fieldName:"
								+ fileItem.getName() + " /fileSize:"
								+ fileItem.getSize());
						String fileName = fileItem.getName(); // Image
						int idx = fileName.lastIndexOf('\\');
						if (idx != -1) {
							fileName = fileName.substring(idx + 1);
						}

						File folder = new File("/carcaro/picture");
						if (!folder.exists()) {
							folder.mkdirs();
						}

						int dotposition = fileName.lastIndexOf(".");
						String extension = fileName.substring(dotposition,
								fileName.length());
						picture = driverId + '_' + System.currentTimeMillis()
								+ extension;
						File file = new File(folder, picture); // driverId

						OutputStream out = new BufferedOutputStream(
								new FileOutputStream(file));
						InputStream in = new BufferedInputStream(
								fileItem.getInputStream());

						byte[] buf = new byte[8192];
						int len;
						while ((len = in.read(buf)) != -1) {
							out.write(buf, 0, len);
						}
						in.close();
						out.close();
						logger.debug("Image upload complete :" + picture);
					}
				}
			}
			logger.debug("--->> UpdateDriverPic: driverId:" + driverId
					+ " /Pic:" + picture);
			
			String residentNo = residentNo1 + residentNo2;
			String phone = phone1 + phone2 + phone3;
			
			Driver driver = new Driver();
			driver.setDriverId(driverId); // 드라이버 아이디
			driver.setLicenseType(1); // 면허 종류
			driver.setPasswd(passwd); // 비밀번호
			driver.setLiceseNo(liceseNo); // 면허증 번호
			driver.setAuthorizationNo(authorizationNo); // 보험 번호
			driver.setName(name); // 이름
			driver.setAssurance_complete_date(assurance_complete_date); // 보험 완료일
			driver.setResidentNo(residentNo); // 주민번호
			driver.setPhone(phone); // 전화번호
			driver.setAddress(address); // 주소
			driver.setEmail(email);
			
			System.out.println("------inserDriver START-------");
			boolean result = driverDAO.insertDriver(driver);
			System.out.println("------inserDriver End-------");
			
			if(result){
				// 한글을 보내기 위해서는 꼭 선언해야 함.
				resp.setContentType("text/html;charset=utf-8");
				resp.getWriter().println("대리기사 등록이 정상 처리되었습니다.");
			}else{
				resp.setContentType("text/html;charset=utf-8");
				resp.getWriter().println("관리자에게 문의 바랍니다.");
			}
			
		} catch (Exception e) {
			logger.error("driverInsert error." + e.getMessage());
		}

		/*String driverId = req.getParameter("driverId"); // driverID
		String licenseType = req.getParameter("licenseType"); // 면허종류
		String passwd = req.getParameter("passwd"); // 비밀번호
		String authorizationNo = req.getParameter("authorizationNo"); // 증권번호(보험번호)
		String authorizationName = req.getParameter("authorizationName"); // 증권번호(보험이름)
		String name = req.getParameter("name"); // 이름
		String residentNo1 = req.getParameter("residentNo1"); // 주민번호
		String residentNo2 = req.getParameter("residentNo2"); // 주민번호
		String residentNo = residentNo1 + residentNo2; // 주민번호
		String phone1 = req.getParameter("phone1"); // 전화번호
		String phone2 = req.getParameter("phone2"); // 전화번호
		String phone3 = req.getParameter("phone3"); // 전화번호
		String phone = phone1 + phone2 + phone3; // 전화번호
		String address = req.getParameter("address"); // 주소
		String email = req.getParameter("email"); // 이메일
		String liceseNo = req.getParameter("liceseNo"); // 면허번호
		String assurance_complete_date = req.getParameter("assurance_complete_date"); // 보험만료일
		
		System.out.println("driverId=="+driverId);
		System.out.println("licenseType=="+licenseType);
		System.out.println("authorizationNo=="+authorizationNo);
		System.out.println("authorizationName=="+authorizationName);
		System.out.println("name=="+name);
		System.out.println("residentNo1=="+residentNo1);
		System.out.println("residentNo2=="+residentNo2);
		System.out.println("residentNo=="+residentNo);
		System.out.println("phone1=="+phone1);
		System.out.println("phone2=="+phone2);
		System.out.println("phone3=="+phone3);
		System.out.println("address=="+address);
		System.out.println("email=="+email);
		System.out.println("liceseNo=="+liceseNo);
		System.out.println("assurance_complete_date=="+assurance_complete_date);

		Driver driver = new Driver();
		driver.setDriverId(driverId); // 드라이버 아이디
		driver.setPasswd(passwd); // 비밀번호
		driver.setName(name); // 이름
		driver.setPhone(phone); // 폰번호
		driver.setResidentNo(residentNo); // 주민번호
		driver.setAddress(address); // 주소
		driver.setEmail(email); // 이메일
		driver.setAuthorizationNo(authorizationNo); // 보험번호
		driver.setAuthorizationName(authorizationName); // 보험이름
		driver.setLicenseType(1); // 면허종류
		driver.setLiceseNo(liceseNo); // 면허번호
		driver.setAssurance_complete_date(assurance_complete_date); // 보험만료일
		
		System.out.println("------inserDriver START-------");
		boolean result = driverDAO.insertDriver(driver);
		System.out.println("------inserDriver End-------");
		
		if(result){
			// 한글을 보내기 위해서는 꼭 선언해야 함.
			resp.setContentType("text/html;charset=utf-8");
			resp.getWriter().println("대리기사 등록이 정상 처리되었습니다.");
		}else{
			resp.setContentType("text/html;charset=utf-8");
			resp.getWriter().println("관리자에게 문의 바랍니다.");
		}*/
	}

	// 충전내역
	private void driverChargeList(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, ServletException,
			IOException {
		// TODO Auto-generated method stub
		String pageNum = req.getParameter("curr_page");
		String _pageSize = req.getParameter("pageSize");
		if (pageNum == null) {
			pageNum = "1";
		}
		if (_pageSize == null) {
			_pageSize = "10";
		}
		int pageSize = Integer.parseInt(_pageSize);
		int currentPage = Integer.parseInt(pageNum);
		
		String gubun = req.getParameter("gubun"); // 0:이름, 1:ID, 2:전화번호
		if(gubun == null || gubun.length() == 0)
			gubun = "0";
		String keyword = req.getParameter("keyword"); // 검색어
		SettlementHist[] list = driverDAO.getSettlementList(pageSize, currentPage, gubun, keyword);
		long plTotalCnt = driverDAO.getSettlementCount(gubun, keyword);
		
		req.setAttribute("plTotalCnt", new Long(plTotalCnt)); // 전체페이지 수
		req.setAttribute("pageNum", new Long(currentPage)); // 현재 페이지 넘버
		req.setAttribute("pageSize", new Long(pageSize)); // 페이지당 레코드 수
		req.setAttribute("list", list);

		// System.out.print("list길이"+list.length);
		// req.getRequestDispatcher("./admin/driverList_Charge_view.jsp").forward(req,
		// resp);
		req.getRequestDispatcher("./admin/driverList_Charger_proc.jsp")
				.forward(req, resp);
		// 충전내역
	}

	private void driverCertify(HttpServletRequest req, HttpServletResponse resp)
			throws SQLException, ServletException, IOException {
		// TODO Auto-generated method stub
		String[] check = req.getParameterValues("check");
		String[] driverId = req.getParameterValues("driverId");

		if (check == null) {
			check = new String[0];
		}

		String[] dlist = new String[check.length];
		int index = -1;

		for (int i = 0; i < check.length; i++) {
			index = Integer.parseInt(check[i]);
			dlist[i] = driverId[index - 1];
		}

		try {
			MainServlet.main.updateDriverList(dlist);
			driverDAO.certifing(dlist);
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		driverList(req, resp);
	}

	private void certifingDriver(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, ServletException,
			IOException {
		String[] check = req.getParameterValues("check");
		String[] driverId = req.getParameterValues("driverId");

		if (check == null) {
			check = new String[0];
		}

		String[] dlist = new String[check.length];
		int index = -1;

		for (int i = 0; i < check.length; i++) {
			index = Integer.parseInt(check[i]);
			dlist[i] = driverId[index - 1];
		}

		try {
			MainServlet.main.updateDriverList(dlist);
			driverDAO.certifing(dlist);
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		driverList(req, resp);
	}

	private void registDriver(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, SQLException {
		req.setCharacterEncoding("utf-8");

		String driverId = req.getParameter("driverId");
		String passwd = req.getParameter("passwd");
		String name = req.getParameter("name");
		String phone = req.getParameter("phone");
		String residentNo = req.getParameter("residentNo1")
				+ req.getParameter("residentNo2");
		String authorizationNo = req.getParameter("authorizationNo");
		String email = req.getParameter("email1") + "@"
				+ req.getParameter("email2");
		int licenseType = Integer.parseInt(req.getParameter("licenseType"));
		boolean licenseAuto = Boolean.parseBoolean(req
				.getParameter("licenseAuto"));
		int career = Integer.parseInt(req.getParameter("career"));
		String company = req.getParameter("company");
		String address = req.getParameter("address");
		String os = req.getParameter("OS");
		if (os == null || os.length() == 0)
			os = "Android"; // iPhone Specific Method...
		int agreeReceive = 0;
		if (req.getParameter("agreeReceive") != null) {
			String ar[] = req.getParameterValues("agreeReceive");
			for (int i = 0; i < ar.length; i++)
				agreeReceive += Integer.parseInt(ar[i]);
		}

		Driver driver = new Driver(driverId, passwd, name, phone, residentNo,
				authorizationNo, email, licenseType, licenseAuto, career,
				company, address, agreeReceive, os);

		driverDAO.registDriver(driver);

		req.setAttribute("msg", "회원 가입이 완료되었습니다.");
		req.getRequestDispatcher("./index.jsp").forward(req, resp);
	}

	/**
	 * 정산금 관리자
	 * 
	 * @param req
	 * @param resp
	 */
	private void settleManager(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, SQLException, IOException {
		String mode = req.getParameter("mode");
		if (mode.equals("flow")) {
			// 입출금관리
			settleManagerFlow(req, resp);
		} else if (mode.equals("settlement")) {
			// 정산금관리
			settleManagerSettlement(req, resp);
		} else if(mode.equals("settlementInsert")){
			settlementInsert(req, resp);
		}
	}

	/**
	 * 입출금 관리
	 * 
	 * @param req
	 * @param resp
	 * @throws IOException
	 * @throws ServletException
	 */
	private void settleManagerFlow(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		String pageNum = req.getParameter("curr_page");
		String _pageSize = req.getParameter("pageSize");
		if (pageNum == null) {
			pageNum = "1";
		}
		if (_pageSize == null) {
			_pageSize = "10";
		}

		// 정보
		String date = req.getParameter("date");
		String local = req.getParameter("local");
		String result = req.getParameter("result");
		String name = req.getParameter("name");

		if (date == null || date.length() == 0) {
			date = Util.getCurrDate();
		}
		if (local == null || local.length() == 0) {
			local = "%";
		}
		if (result == null || result.length() == 0) {
			result = "%";
		}

		try {
			ConcurrentHashMap<String, ChargeHistoryPeriod> ch = chargeHistoryDAO
					.getChargeHistoryMonth2(date, local, result);

			req.setAttribute("list", ch);
			req.setAttribute("date", date);
			req.setAttribute("local", local);
			req.setAttribute("name", name);
		} catch (SQLException e) {
			e.printStackTrace();
			req.setAttribute("msg1", "조회에 실패했습니다.\\n잠시 후에  다시 시도해주세요");
		}
		req.getRequestDispatcher("./admin/settleManager_flow_proc.jsp")
				.forward(req, resp);
	}
	
	/**
	 * 정산금 입력
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 * @throws SQLException 
	 * @throws NumberFormatException 
	 */
	private void settlementInsert(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException, NumberFormatException, SQLException {
		// TODO
		String regDate = req.getParameter("regDate"); // 정산일자
		String Location = req.getParameter("Location"); // 지역
		String charge = req.getParameter("charge"); // 정산요금
		
		if(regDate == null || regDate.length() == 0){
			resp.setContentType("text/html;charset=utf-8");
			resp.getWriter().println("regDate: " + regDate);
		}
		if(Location == null || Location.length() == 0){
			resp.setContentType("text/html;charset=utf-8");
			resp.getWriter().println("Location: " + regDate);
		}
		if(charge == null || charge.length() == 0){
			resp.setContentType("text/html;charset=utf-8");
			resp.getWriter().println("charge: " + regDate);
		}
		
		System.out.println("------settlementInsert START-------");
		boolean re = settlementDAO.setAdjustment(regDate, Location, Integer.parseInt(charge));
		System.out.println("------settlementInsert START-------");
		if(re){
			// 한글을 보내기 위해서는 꼭 선언해야 함.
			resp.setContentType("text/html;charset=utf-8");
			resp.getWriter().println("정산금이 수정이 정상 처리되었습니다.");
		}else{
			resp.setContentType("text/html;charset=utf-8");
			resp.getWriter().println("관리자에게 문의 바랍니다.");
		}
	}
	/**
	 * 정산금 관리
	 * 
	 * @param req
	 * @param resp
	 */
	private void settleManagerSettlement(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		// TODO
		String pageNum = req.getParameter("curr_page");
		String _pageSize = req.getParameter("pageSize");
		if (pageNum == null) {
			pageNum = "1";
		}
		if (_pageSize == null) {
			_pageSize = "10";
		}

		// 정보
		String date = req.getParameter("date");
		String local = req.getParameter("local");
		String result = req.getParameter("result");
		// String name = req.getParameter("name");

		if (date == null || date.length() == 0) {
			date = Util.getCurrDate();
		}
		if (local == null || local.length() == 0) {
			local = "%";
		}
		if (result == null || result.length() == 0) {
			result = "%";
		}

		try {
			ConcurrentHashMap<String, ChargeHistoryPeriod> ch = chargeHistoryDAO
					.getChargeHistoryMonth2(date, local, result);

			req.setAttribute("list", ch);
			req.setAttribute("date", date);
			req.setAttribute("local", local);
			// req.setAttribute("name", name);
				
			
		} catch (SQLException e) {
			e.printStackTrace();
			req.setAttribute("msg1", "조회에 실패했습니다.\\n잠시 후에  다시 시도해주세요");
		}
		req.getRequestDispatcher("./admin/settleManager_settlement_proc.jsp")
				.forward(req, resp);
	}

	private void getSettlementList(HttpServletRequest req,
			HttpServletResponse resp) throws SQLException, ServletException,
			IOException {
		String pageNum = req.getParameter("curr_page");
		String _pageSize = req.getParameter("pageSize");
		if (pageNum == null) {
			pageNum = "1";
		}
		if (_pageSize == null) {
			_pageSize = "10";
		}

		int pageSize = Integer.parseInt(_pageSize);
		int currentPage = Integer.parseInt(pageNum);

		HttpSession session = req.getSession();
		String driverId = (String) session.getAttribute("id");

		Settlement[] list = settlementDAO.getSettlementList(driverId,
				currentPage, pageSize);
		long plTotalCnt = settlementDAO.getSettlementCount(driverId);

		req.setAttribute("plTotalCnt", new Long(plTotalCnt));
		req.setAttribute("pageNum", new Long(currentPage));
		req.setAttribute("pageSize", new Long(pageSize));
		req.setAttribute("list", list);

		req.getRequestDispatcher("./carcaro/settlementList.jsp").forward(req,
				resp);
	}

	/**
	 * 인증 안된 사용 자를 보여주는 화면
	 */
	private void driverList(HttpServletRequest req, HttpServletResponse resp)
			throws SQLException, ServletException, IOException {
		String _Check = req.getParameter("certifyCheck");
		String pageNum = req.getParameter("curr_page");
		String _pageSize = req.getParameter("pageSize");
		if (pageNum == null) {
			pageNum = "1";
		}
		if (_pageSize == null) {
			_pageSize = "10";
		}
		/*if (_Check == null) {
			_Check = "false";
		} else {
			_Check = "true";
		}*/
		
		String level = req.getParameter("level");
		String gubun = req.getParameter("gubun");
		String keyword = req.getParameter("keyword");
		// 승인, 미승인
		if(level == null || level.length() == 0)
			level = "0";
		// 이름, 아이디, 전화번호
		if(gubun == null || gubun.length() == 0)
			gubun = "0";
		// 키워드
		if(keyword == null)
			keyword = "";

		int pageSize = Integer.parseInt(_pageSize);
		int currentPage = Integer.parseInt(pageNum);
		//boolean certiCheck = Boolean.parseBoolean(_Check);

		Driver[] list = driverDAO.driverList(pageSize, currentPage, level, gubun, keyword);
		long plTotalCnt = driverDAO.getNoCertiCount(level, gubun, keyword);
		// debuging
		System.out.println("list=="+list.length);
		System.out.println("plTotalCnt=="+plTotalCnt);

		req.setAttribute("plTotalCnt", new Long(plTotalCnt));
		req.setAttribute("pageNum", new Long(currentPage));
		req.setAttribute("pageSize", new Long(pageSize));
		//req.setAttribute("certifyCheck", _Check + "");
		req.setAttribute("list", list);
		req.getRequestDispatcher("./admin/driverList_proc.jsp").forward(req,
				resp);

	}

	private void updateInfo(HttpServletRequest req, HttpServletResponse resp)
			throws SQLException, ServletException, IOException {
		req.setCharacterEncoding("utf-8");

		String driverId = req.getParameter("driverId");
		String passwd = req.getParameter("passwd");
		String phone = req.getParameter("phone");
		String authorizationNo = req.getParameter("authorizationNo");
		String email = req.getParameter("email1") + "@"
				+ req.getParameter("email2");
		int licenseType = Integer.parseInt(req.getParameter("licenseType"));
		boolean licenseAuto = Boolean.parseBoolean(req
				.getParameter("licenseAuto"));
		int career = Integer.parseInt(req.getParameter("career"));
		String company = req.getParameter("company");
		String address = req.getParameter("address");
		String os = req.getParameter("OS");

		if (os == null || os.length() == 0)
			os = "Android"; // iPhone Specific Method...

		int agreeReceive = 0;
		if (req.getParameter("agreeReceive") != null) {
			String ar[] = req.getParameterValues("agreeReceive");
			for (int i = 0; i < ar.length; i++)
				agreeReceive += Integer.parseInt(ar[i]);
		}

		Driver driver = new Driver(driverId, passwd, null, phone, null,
				authorizationNo, email, licenseType, licenseAuto, career,
				company, address, agreeReceive, os);

		driverDAO.updateDriver(driver);

		req.getRequestDispatcher("./carcaro/loginOk.jsp").forward(req, resp);
	}

	private void modifyInfo(HttpServletRequest req, HttpServletResponse resp)
			throws SQLException, ServletException, IOException {
		String driverId = req.getParameter("id");
		String checkPw = req.getParameter("checkPw");
		String pw = req.getParameter("pw");

		if (checkPw == null)
			checkPw = "false";
		boolean check = Boolean.parseBoolean(checkPw);

		if (pw != null) {
			check = driverDAO.checkPw(driverId, pw);
		}

		if (!check) {
			req.setAttribute("check", check + "");
		} else {
			Driver driver = driverDAO.getDriverInfo(driverId);
			req.setAttribute("check", check + "");
			req.setAttribute("driver", driver);
		}
		req.getRequestDispatcher("./carcaro/modifyInfo.jsp").forward(req, resp);
	}

	private void checkId(HttpServletRequest req, HttpServletResponse resp)
			throws SQLException, ServletException, IOException {
		String id = req.getParameter("id");
		long ret = driverDAO.checkId(id);

		req.setAttribute("ret", new Long(ret));
		req.setAttribute("id", id);
		req.getRequestDispatcher("./checkId.jsp").forward(req, resp);
	}

	private void loginConfirm(HttpServletRequest req, HttpServletResponse resp)
			throws SQLException {
		try {

			String input_id = req.getParameter("id");
			String input_pw = req.getParameter("pw");
			HttpSession session = req.getSession();
			String url = "./index.jsp";

			// int isConfirmed = driverDao.LoginDriver(input_id, input_pw);
			// int isConfirmed = adminDAO.LoginAdmin(input_id, input_pw);
			int isConfirmed = 1;
			if (isConfirmed == AdminDAO.LOGIN_OK) {// 아이디, 비밀번호 일치 시
				session.setAttribute("id", input_id);
				String aL = Integer.toString(adminDAO.accessLevel(input_id));
				String location = adminDAO.adminLocation(input_id);
				session.setAttribute("accessLevel", aL);
				session.setAttribute("location", location);
				url = "./admin/index_proc.jsp";

			} else if (isConfirmed == AdminDAO.LOGIN_FAIL) {
				req.setAttribute("msg", "아이디 또는 비밀번호가 틀렸습니다. 다시 로그인 하세요");
			}
			req.getRequestDispatcher(url).forward(req, resp);

		} catch (ServletException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * testFunction
	 * 
	 * @param req
	 * @param resp
	 * @throws SQLException
	 */
	@SuppressWarnings("unused")
	private void testFunction(HttpServletRequest req, HttpServletResponse resp)
			throws SQLException {
		String testParam = req.getParameter("testParam");
		try {
			int check = adminDAO.accessLevel("admin");
			resp.getWriter().print(check);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
