package carcaro;

//import javapns.back.SSLConnectionHelper;

import javapns.notification.PushNotificationManager;
import javapns.notification.PushNotificationPayload;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.devices.Device;
import javapns.notification.Payload;
import javapns.Push;



import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import sun.org.mozilla.javascript.internal.Context;


public class APNService {
	private PushNotificationManager pushManager;
	
	private String rootPath;
	
	private static final Logger logger = Logger.getRootLogger();
	
	public APNService(String rootPath){
		this.rootPath = rootPath;

		
	}
	
	public void closeConnection(){

	}
	
	public void sendPushNotification(int mid,String title, String body, String deviceToken) {
		
		if ( deviceToken.length() > 0){
			PushNotificationPayload payLoad = new PushNotificationPayload();
			try {
				payLoad.addAlert(title);						// ex) 고객이 대리요청을 하였습니다.
				payLoad.addBadge(1);
				payLoad.addSound("default");
				payLoad.addCustomDictionary("body", body);		// 상세내용  ex) 출발지 : 서울 강남구 논현동   목적지 : 서울 서대문구 신촌동
				payLoad.addCustomDictionary("mid", mid);		// 내용 전문을 지닌 mid.  
			} catch (Exception e1) {
				e1.printStackTrace();
				logger.debug(e1.getMessage()); 
			}
			

			String certificatePath = rootPath + "/cert/apns_pro.p12";     // 복사해놓은 인증서의 경로명+파일명
			String certificatePassword = "31049a";
			try {
				Push.payload (payLoad, certificatePath, certificatePassword, true, deviceToken);
			} catch (CommunicationException e) {
				e.printStackTrace();
			} catch (KeystoreException e) {
				e.printStackTrace();
			}
		}
		
		
	}
	
public void sendPushNotificationWithoutAlert(int mid,String title, String body, String deviceToken) {
		
		if ( deviceToken.length() > 0){
			PushNotificationPayload payLoad = new PushNotificationPayload();
			try {
//				payLoad.addAlert(title);						// ex) 고객이 대리요청을 하였습니다.
//				payLoad.addBadge(1);
//				payLoad.addSound("default");
				payLoad.addCustomDictionary("body", body);		// 상세내용  ex) 출발지 : 서울 강남구 논현동   목적지 : 서울 서대문구 신촌동
				payLoad.addCustomDictionary("mid", mid);		// 내용 전문을 지닌 mid.  
			} catch (Exception e1) {
				e1.printStackTrace();
				logger.debug(e1.getMessage()); 
			}
			

			String certificatePath = rootPath + "/cert/apns_pro.p12";     // 복사해놓은 인증서의 경로명+파일명
			String certificatePassword = "31049a";
			try {
				Push.payload (payLoad, certificatePath, certificatePassword, true, deviceToken);
			} catch (CommunicationException e) {
				e.printStackTrace();
			} catch (KeystoreException e) {
				e.printStackTrace();
			}
		}
		
		
	}
}

