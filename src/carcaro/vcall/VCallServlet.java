package carcaro.vcall;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;

import org.json.JSONObject;

import org.apache.log4j.Logger;

import carcaro.ConnectionPool;
import carcaro.bean.VCall;
import carcaro.bean.VLocationInfoBase;
import carcaro.dao.APNSDAO;
import carcaro.dao.AdminDAO;
import carcaro.dao.CouponDAO;
import carcaro.dao.DriverDAO;
import carcaro.dao.LocationDAO;
import carcaro.dao.SettlementDAO;

/**
 * Servlet implementation class VCallServlet
 */
public class VCallServlet extends HttpServlet {

	private static final Logger logger = Logger.getRootLogger();

	private static final long serialVersionUID = 1L;
	
	private static final int WAIT_TIME = 40; //sec


	private List<String> vCallQueue;
	private ConcurrentHashMap<String, VCall> vCallList;
	private ConcurrentHashMap<String, VLocationInfoBase> vLocationInfo;
	private boolean active;

	private ConnectionPool connPool;

	private AdminDAO adminDAO;
	private LocationDAO locationDAO;

	private CallCheck callCheck;

	private LocationCheck locationCheck;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public VCallServlet() {
		super();
	}



	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);


		logger.debug("VCallSerlvet Init");


//		vCallQueue = new ConcurrentLinkedQueue<HashMap<String, String>>();
		vCallQueue = new ArrayList<String>();
		vCallList = new ConcurrentHashMap<String, VCall>();
		vLocationInfo = new ConcurrentHashMap<String, VLocationInfoBase>();


		try {
			connPool = ConnectionPool.getInstance();
			adminDAO = new AdminDAO(connPool);
			locationDAO = new LocationDAO(connPool);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		loadCallList();

		loadLocationInfo();




//		vcallExecuter = new VCallExecuter(vCallQueue);
//		vcallExecuter.setDaemon(true);
//		vcallExecuter.start();


		callCheck = new CallCheck();
		callCheck.setDaemon(true);
		callCheck.start();

		
		// 좌표 생성기.. 사용 안하면 주석 처리 할 것
		
//		locationCheck = new LocationCheck();
//		locationCheck.setDaemon(true);
//		locationCheck.start();


	}

	@Override
	public void destroy(){

		// Stop Threads
		//vcallExecuter.stopThread();
		//locationCheck.stopThread();
		callCheck.stopThread();
		this.active = false;

	}


	/**
	 * 위치정보 로드 (DB에 6000개 모두 저장완료 ... 사용안함)
	 */
	private void loadLocationInfo() {
		List<VLocationInfoBase> _vLIBList = new ArrayList<VLocationInfoBase>();
		try {
			_vLIBList = locationDAO.loadLocationInfo();
			for ( VLocationInfoBase vLIB : _vLIBList){
				vLocationInfo.put(Integer.toString(vLIB.getLID()), vLIB);
			}
			logger.debug("VLocationInfo Loaded... Cnt: "+ _vLIBList.size());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}



	/**
	 * 가상콜 목록 불러오기
	 */
	private void loadCallList() {
		List<VCall> _vCallList;

		if ( !vCallList.isEmpty() )	vCallList.clear();
		try {
			_vCallList = locationDAO.loadCallList();
			for (VCall vcall : _vCallList ){
				vCallList.put(vcall.getcCallTime(), vcall);
			}
			logger.debug("VCallList Loaded... Cnt: "+ _vCallList.size());
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 가상콜 실행
	 * @param vcall
	 */
	private void performVCall(VCall vcall){

		URL u;
		StringBuffer url = new StringBuffer();

		String cDest = vcall.getcDest();
		String cSrc = vcall.getcSrc();
		String cLat = vcall.getcLat();
		String cLng = vcall.getcLng();
		String cOS = vcall.getcOS();
		
		
		
		// 랜덤한 폰번호
		Random rand = new Random();
		rand.setSeed(System.currentTimeMillis());
		int cty = rand.nextInt(8000) + 1111;
		int num = rand.nextInt(8000) + 1111;
		String cPhoneNum = "010"+Integer.toString(cty)+Integer.toString(num);
		
		// 랜덤한 디바이스아이디(11111111~99999999)
		String randDevId = Integer.toString(rand.nextInt(80000000) + 11111111);
		
		// 가상으로 연결시키기 위해 List에 삽입
		vCallQueue.add(randDevId);
		
		
		try {
			url.append("http://vcarcaro.cafe24.com:8080/carcaro/appccr?cmd=requestDriver");
			url.append("&cDevId="+randDevId);
			url.append("&cPhone="+cPhoneNum);
			url.append("&cLat="+cLat);
			url.append("&cLng="+cLng);
			url.append("&OS="+cOS);
			url.append("&cName=");
			url.append("&cDst=");
			url.append(URLEncoder.encode(cDest, "UTF-8"));
			url.append("&cSrc=");
			url.append(URLEncoder.encode(cSrc, "UTF-8"));
			url.append("&safeOn=false&safePhone=&coupon_id=");
			
		}catch (UnsupportedEncodingException e){
			e.printStackTrace();
		}
		logger.debug("------>>>>>>>>>>Perform VCall : "+url.toString());
		
		//Open the URL for reading
		try {

			u = new URL(url.toString());

			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(u.openStream()));
				String str;
				StringBuffer sb = new StringBuffer();
				while ((str = in.readLine()) != null) {
					// str is one line of text; readLine() strips the newline character(s)
					sb.append(str + "\n");
				}
				in.close();

				logger.debug(sb.toString());
				
			
			} 
			catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	}


	/**
	 * 콜 시간 체크
	 * 매초마다 가상콜 리스트에서 시간을 체크해 가상콜 시행
	 */
	private void checkCallList() {

		DateTime checkTime = DateTime.now();
		DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm:ss");
		String hourMin = checkTime.toString(fmt);
		
		VCall vCall = vCallList.get(hourMin);
		
//		logger.debug("Call Check... : " + hourMin );
		if ( vCall != null ){
//			logger.debug("vCall: "+ vCall.getcNum() + " /" + vCall.getcDest() + " /" + vCall.getsRepeat());
			int day = checkTime.getDayOfWeek(); // 1= Mon, 7 = Sun
			String sRepeat = vCall.getsRepeat();
			
			// sRepeat 컬럼에 오늘 날짜가 있으면 실행
			if ( sRepeat.indexOf(Integer.toString(day)) > -1 ){
				logger.debug("vCall Found call to perform: " + vCall.getcNum() + " /" + vCall.getcSrc() + " /" + vCall.getcDest() + " /" + vCall.getsRepeat());
				// 서버상에 올릴때는 주석을 해제 해야 한다.
				// 테스트상에서는 주석으로 처리해야 한다.
				// 이유는 아래 VCall은 실서버 URL로 VCall을 날리기 때문.
				performVCall(vCall);
			}else {
			}
		}
	}

	
	/**
	 *  다른 대리기사와 연결되었음을 가상화 하기 위한 작업.
	 *  직접적으로 appccr과 http 통신
	 */
	private void startEndDriving(){
		//
		URL u;
		StringBuffer url = new StringBuffer();
		
		
		// Dummy 대리기사
		if ( vCallQueue.size() == 0 ) return;
			// 큐사이즈가 제로가 아닐때만 실행
		String driverId = "test0";
		String cDevId = vCallQueue.get(0);
		vCallQueue.remove(0);

		
		
		url.append("http://vcarcaro.cafe24.com:8080/carcaro/appccr?cmd=startDriving");
		url.append("&cDevId="+cDevId);
		url.append("&driverId="+driverId);
		logger.debug("------>>>>>>>>>>Connect(StartDriving) Vcall : "+url.toString());
		
		//Open the URL for reading
		try {
			u = new URL(url.toString());
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(u.openStream()));
				String str;
				StringBuffer sb = new StringBuffer();
				while ((str = in.readLine()) != null) {
					// str is one line of text; readLine() strips the newline character(s)
					sb.append(str + "\n");
				}
				in.close();
				logger.debug(sb.toString());
			} 
			catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		
		// url 클리어
		url = null;
		url = new StringBuffer();
		url.append("http://vcarcaro.cafe24.com:8080/carcaro/appccr?cmd=endDriving");
		url.append("&cDevId="+cDevId);
		url.append("&driverId="+driverId);
		logger.debug("------>>>>>>>>>>End Vcall : "+url.toString());
		
		//Open the URL for reading
		try {
			u = new URL(url.toString());
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(u.openStream()));
				String str;
				StringBuffer sb = new StringBuffer();
				while ((str = in.readLine()) != null) {
					// str is one line of text; readLine() strips the newline character(s)
					sb.append(str + "\n");
				}
				in.close();
				logger.debug(sb.toString());
			} 
			catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * 콜 체크하는 쓰레드
	 * @author CheeseLemonX
	 *
	 */
	private class CallCheck extends Thread {
		private boolean running = true;

		public CallCheck(){
			setName("Call Check Thread");
		}

		public void run() {
			int counter = 1;
			int uSleep = 1000;
			while (running) {
				try {
					// perform check call List
					checkCallList();
					
					// 최대한 티가 나지 않도록 ..어떤 것은 빨리 가상연결, 어떤것은 느리게 가상연결, 어떤것은 가상연결 되지 않음.. 모든 가능성 가능.
					if ( counter % 29 == 0 ){
						startEndDriving();
					}

					if ( counter % 60 == 0){
						// VCall List reload per minute
						loadCallList();
					}
					
					// Sleep for 1 sec
					counter ++;
					Thread.sleep(uSleep);
					} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		/**
		 * 쓰레드 멈추기
		 * 요거 이상함...
		 */
		public void stopThread(){
			// TODO thread.stop()이 deprecate된 관계로 이렇게 사용하라는데.. 이상함
			running = false;
		}
	}

	
	/**
	 * 위치기반 DB에 입력. 이제 사용  안함.
	 * @author CheeseLemonX
	 *
	 */
	private class LocationCheck extends Thread {
		private boolean checker = true;
		int index = 0;

		public LocationCheck(){
			setName("Location xy Check Thread");
		}

		public void run(){
			while (checker){
				try{
					index ++;
					if ( index <= vLocationInfo.size()) {

						VLocationInfoBase vLocBase = vLocationInfo.get(Integer.toString(index));
						int Status = vLocBase.getStatus();
						if ( Status == VLocationInfoBase.GPS_NULL ){
							logger.debug(">>Inserting Location Info LIB: "+index);
							String Sido = vLocBase.getSido();
							String Gugun = vLocBase.getGugun();
							String Dong = vLocBase.getDong();
							JSONObject latLng = getLatLngFromGoogle(Sido, Gugun, Dong);
							String Lat = latLng.getString("lat");
							String Lng = latLng.getString("lng");
							vLocBase.setLat(Lat);
							vLocBase.setLng(Lng);
							vLocBase.setStatus(VLocationInfoBase.GPS_NOT_NULL);
							
							// 해시맵 업데이트
							vLocationInfo.put(Integer.toString(index), vLocBase);
							
							// DB 업데이트
							locationDAO.updateLatLng(index, Lat, Lng);
							Thread.sleep(200);
						}else {

						}
					}else {
						
						// stop thread when finished checking location
						checker = false;
						
					}
					

				}catch (InterruptedException e){
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		public void stopThread(){
			checker = false;
		}

		
		/**
		 * 구글의 GEOCoding을 이용함.
		 * @param Sido
		 * @param Gugun
		 * @param Dong
		 * @return
		 */
		public JSONObject getLatLngFromGoogle(String Sido,String Gugun,String Dong) {
			JSONObject ret  = new JSONObject();

			URL u;
			StringBuffer url = new StringBuffer();

			try {
				url.append("http://maps.googleapis.com/maps/api/geocode/json?address=");
				url.append(URLEncoder.encode(Sido, "UTF-8"));
				url.append("+");
				url.append(URLEncoder.encode(Gugun, "UTF-8"));
				url.append("+");
				url.append(URLEncoder.encode(Dong, "UTF-8"));
				url.append("&sensor=false");
			}catch (UnsupportedEncodingException e){
				e.printStackTrace();
			}
			logger.debug(url.toString());
			//Open the URL for reading
			try {

				u = new URL(url.toString());

				try {
					BufferedReader in = new BufferedReader(new InputStreamReader(u.openStream()));
					String str;
					StringBuffer sb = new StringBuffer();
					while ((str = in.readLine()) != null) {
						// str is one line of text; readLine() strips the newline character(s)
						sb.append(str + "\n");
					}
					in.close();

//					logger.debug(sb.toString());
					
					// ========= Parse Geocode =========
					JSONObject recvJSON = new JSONObject(sb.toString());
					String status = recvJSON.getString("status");
					if ( "OK".equals(status)){
						JSONArray results = recvJSON.getJSONArray("results");
						JSONObject resultsObj = results.getJSONObject(0);
						JSONObject geometry = resultsObj.getJSONObject("geometry");
						ret = geometry.getJSONObject("location");
					}
					
				} 
				catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e){
					e.printStackTrace();
				}
			}
			catch (MalformedURLException e) {
				e.printStackTrace();
			}

			return ret;
		}
	}

}





