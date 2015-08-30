package carcaro;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import net.sf.json.JSONObject;
import net.sf.json.util.JSONTokener;

import org.apache.log4j.Logger;

import carcaro.service.MqttConnectionListner;

import com.ibm.mqtt.IMqttClient;
import com.ibm.mqtt.MqttClient;
import com.ibm.mqtt.MqttException;
import com.ibm.mqtt.MqttNotConnectedException;
import com.ibm.mqtt.MqttPersistence;
import com.ibm.mqtt.MqttPersistenceException;
import com.ibm.mqtt.MqttSimpleCallback;

/*
 * An example of how to implement an MQTT client in Android, able to receive
 *  push notifications from an MQTT message broker server.
 *
 *  Dale Lane (dale.lane@gmail.com)
 *    28 Jan 2011
 */
public class MQTTService implements MqttSimpleCallback {
	/************************************************************************/
	/* CONSTANTS */
	/************************************************************************/

	// something unique to identify your app - used for stuff like accessing
	// application preferences
	public static final String APP_ID = "dku.hj.mqtttest";

	public static int QUALITY_OF_SERVICE = 2;
	public static boolean RETAINED_PUBLISH = true;
	
	private static final Logger logger = Logger.getRootLogger();

	// constants used to define MQTT connection status
	public enum MQTTConnectionStatus {
		INITIAL, // initial status
		CONNECTING, // attempting to connect
		CONNECTED, // connected
		NOTCONNECTED_WAITINGFORINTERNET, // can't connect because the phone
		// does not have Internet access
		NOTCONNECTED_USERDISCONNECT, // user has explicitly requested
		// disconnection
		NOTCONNECTED_DATADISABLED, // can't connect because the user
		// has disabled data access
		NOTCONNECTED_UNKNOWNREASON
		// failed to connect for some reason
	}

	// MQTT constants
	public static final int MAX_MQTT_CLIENTID_LENGTH = 22;

	/************************************************************************/
	/* VARIABLES used to maintain state */
	/************************************************************************/

	// status of MQTT client connection
	private MQTTConnectionStatus connectionStatus = MQTTConnectionStatus.INITIAL;

	/************************************************************************/
	/* VARIABLES used to configure MQTT connection */
	/************************************************************************/

	// taken from preferences
	// host name of the server we're receiving push notifications from
	private String brokerHostName;
	// taken from preferences
	// topic we want to receive messages about
	// can include wildcards - e.g. '#' matches anything
	private static String topicName;
	
	// defaults - this sample uses very basic defaults for it's interactions
	// with message brokers
	private static int brokerPortNumber = 1883;
	private MqttPersistence usePersistence = null;
	private boolean cleanStart = false;
	private int[] qualitiesOfService = { 2 };

	// how often should the app ping the server to keep the connection alive?
	//
	// too frequently - and you waste battery life
	// too infrequently - and you wont notice if you lose your connection
	// until the next unsuccessfull attempt to ping
	//
	// it's a trade-off between how time-sensitive the data is that your
	// app is handling, vs the acceptable impact on battery life
	//
	// it is perhaps also worth bearing in mind the network's support for
	// long running, idle connections. Ideally, to keep a connection open
	// you want to use a keep alive value that is less than the period of
	// time after which a network operator will kill an idle connection
	private short keepAliveSeconds = 20 * 60;

	// This is how the Android client app will identify itself to the
	// message broker.
	// It has to be unique to the broker - two clients are not permitted to
	// connect to the same broker using the same client ID.
	private String mqttClientId;

	/************************************************************************/
	/* VARIABLES - other local variables */
	/************************************************************************/
	// connection to the message broker
	private static IMqttClient mqttClient = null;

	// receiver that wakes the Service up when it's time to ping the server
	private PingSender pingSender;
	
	/************************************************************************/
	/* METHODS - core Service lifecycle methods */
	/************************************************************************/
	
	private Timer ping;
	
	private MqttConnectionListner connectionListner;

	public MQTTService(){
		
		// reset status variable to initial state
		connectionStatus = MQTTConnectionStatus.INITIAL;
		
//		mqttClientId = "CarcaroServerhj";
		mqttClientId = "CarcaroServer" + (int) (Math.random() * 10000);
//		mqttClientId = "CarcaroServerHj";
		
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Properties prop = new Properties();
		try {
			prop.load(classLoader.getResourceAsStream("../conf.properties"));
		} catch (IOException e) {
			logger.error("can't find conf.propertes. broker hostname set localhost.");
			e.printStackTrace();
		}
		
		brokerHostName = prop.getProperty("push.hostName", "localhost");
		// "sylark.cafe24.com";
//		topicName = "/somegroup/" + mqttClientId;
//		topicName = "/test/" + mqttClientId;
		topicName = "/checker/";

	}
	
	public void setConnectionListner(MqttConnectionListner connectionListner) {
		this.connectionListner = connectionListner;
	}

	public void start(){
		
		// define the connection to the broker
		defineConnectionToBroker(brokerHostName);
		
		ping = new Timer(true);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				handleStart();
			}
		}, "MQTTService").start();
	}
	
	public void disconnect() {
		disconnectFromBroker();

		// set status
		connectionStatus = MQTTConnectionStatus.NOTCONNECTED_USERDISCONNECT;

		// inform the app that the app has successfully disconnected
		broadcastServiceStatus("Disconnected");
		
		if (mqttClient != null) {
			mqttClient.terminate();
			mqttClient = null;
		}
	}
	
	private boolean isOnline(){
		// TODO implementation
		return true;
	}

	synchronized void handleStart() {
		// before we start - check for a couple of reasons why we should stop

		if (mqttClient == null) {
			// we were unable to define the MQTT client connection, so we stop
			// immediately - there is nothing that we can do
			disconnect();
			return;
		}

		// the Activity UI has started the MQTT service - this may be starting
		// the Service new for the first time, or after the Service has been
		// running for some time (multiple calls to startService don't start
		// multiple Services, but it does call this method multiple times)
		// if we have been running already, we re-send any stored data
		rebroadcastStatus();
		rebroadcastReceivedMessages();

		// if the Service was already running and we're already connected - we
		// don't need to do anything
		if (!isAlreadyConnected()) {
			// set the status to show we're trying to connect
			connectionStatus = MQTTConnectionStatus.CONNECTING;

			// we are creating a background service that will run forever until
			// the user explicity stops it. so - in case they start needing
			// to save battery life - we should ensure that they don't forget
			// we're running, by leaving an ongoing notification in the status
			// bar while we are running
			org.apache.log4j.Logger.getRootLogger().info("MQTT Service is running");

			// before we attempt to connect - we check if the phone has a
			// working data connection
			if (isOnline()) {
				// we think we have an Internet connection, so try to connect
				// to the message broker
				if (connectToBroker()) {
					// we subscribe to a topic - registering to receive push
					// notifications with a particular key
					// in a 'real' app, you might want to subscribe to multiple
					// topics - I'm just subscribing to one as an example
					// note that this topicName could include a wildcard, so
					// even just with one subscription, we could receive
					// messages for multiple topics
					subscribeToTopic(topicName);
				} else {
					// 30초 뒤에 다시 시도한다.
					try {
						Thread.sleep(30 * 1000);
					} catch (InterruptedException e) {
						logger.error(e.getMessage());
					} 
					handleStart();
				}
				
			} else {
				// we can't do anything now because we don't have a working
				// data connection
				connectionStatus = MQTTConnectionStatus.NOTCONNECTED_WAITINGFORINTERNET;

				// inform the app that we are not connected
				broadcastServiceStatus("Waiting for network connection");
			}
		}

	}

	/************************************************************************/
	/* METHODS - broadcasts and notifications */
	/************************************************************************/

	// methods used to notify the Activity UI of something that has happened
	// so that it can be updated to reflect status and the data received
	// from the server

	private void broadcastServiceStatus(String statusDescription) {
		// inform the app (for times when the Activity UI is running /
		// active) of the current MQTT connection status so that it
		// can update the UI accordingly
		
		//logger.info("status : " + statusDescription);
		
	}

	private void broadcastReceivedMessage(String topic, Message message) {
		// pass a message received from the MQTT server on to the Activity UI
		// (for times when it is running / active) so that it can be displayed
		// in the app GUI
		
		//logger.info("message : sender="+ message.sender + ", message ="+message.message + ", topic=" + topic   );
	}

	// methods used to notify the user of what has happened for times when
	// the app Activity UI isn't running

	private void notifyUser(String alert, String title, String body) {
		// TODO 경우에 따라 관리자에게 알려야 한다.
		
		logger.info("alert="+ alert + ", title=" + title + ", body="+ body);
		
	}

	/************************************************************************/
	/* METHODS - binding that allows access from the Actitivy */
	/************************************************************************/

	//
	// public methods that can be used by Activities that bind to the Service
	//

	public MQTTConnectionStatus getConnectionStatus() {
		return connectionStatus;
	}

	public void rebroadcastStatus() {
		String status = "";

		switch (connectionStatus) {
		case INITIAL:
			status = "Please wait";
			break;
		case CONNECTING:
			status = "Connecting...";
			break;
		case CONNECTED:
			status = "Connected";
			break;
		case NOTCONNECTED_UNKNOWNREASON:
			status = "Not connected - waiting for network connection";
			break;
		case NOTCONNECTED_USERDISCONNECT:
			status = "Disconnected";
			break;
		case NOTCONNECTED_DATADISABLED:
			status = "Not connected - background data disabled";
			break;
		case NOTCONNECTED_WAITINGFORINTERNET:
			status = "Unable to connect";
			break;
		}

		//
		// inform the app that the Service has successfully connected
		broadcastServiceStatus(status);
	}

	/************************************************************************/
	/* METHODS - MQTT methods inherited from MQTT classes */
	/************************************************************************/

	/*
	 * callback - method called when we no longer have a connection to the
	 * message broker server
	 */
	public void connectionLost() throws Exception {

		//
		// have we lost our data connection?
		//
		if (isOnline() == false) {
			connectionStatus = MQTTConnectionStatus.NOTCONNECTED_WAITINGFORINTERNET;

			// inform the app that we are not connected any more
			broadcastServiceStatus("Connection lost - no network connection");

			//
			// inform the user (for times when the Activity UI isn't running)
			// that we are no longer able to receive messages
			notifyUser("Connection lost - no network connection", "MQTT",
					"Connection lost - no network connection");

			//
			// wait until the phone has a network connection again, when we
			// the network connection receiver will fire, and attempt another
			// connection to the broker
		} else {
			//
			// we are still online
			// the most likely reason for this connectionLost is that we've
			// switched from wifi to cell, or vice versa
			// so we try to reconnect immediately
			// 

			connectionStatus = MQTTConnectionStatus.NOTCONNECTED_UNKNOWNREASON;

			// inform the app that we are not connected any more, and are
			// attempting to reconnect
			broadcastServiceStatus("Connection lost - reconnecting...");

			// try to reconnect
			if (connectToBroker()) {
				subscribeToTopic(topicName);
			}
		}

	}

	/*
	 * callback - called when we receive a message from the server
	 */
	public void publishArrived(String topic, byte[] payloadbytes, int qos,
			boolean retained) {
		//
		// I'm assuming that all messages I receive are being sent as strings
		// this is not an MQTT thing - just me making as assumption about what
		// data I will be receiving - your app doesn't have to send/receive
		// strings - anything that can be sent as bytes is valid
		String body;
		try {
			body = new String(payloadbytes, "UTF-8");
			
			logger.debug("publishArrived. body is " + body);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
			body = new String(payloadbytes);
		}
		String[] msg = body.split("&");
		
		Message message = new Message();
		message.sender = msg[0];
		message.receiver = topic;
		message.message = msg[1];
		
		if(connectionListner != null){
			
			if(message.message.equals("connected")){
				connectionListner.mqttConnected(message.sender);
			} else if(message.message.equals("disconnected")) {
				connectionListner.mqttDisconnected(message.sender);
			}
		}
		
		
		//
		// for times when the app's Activity UI is not running, the Service
		// will need to safely store the data that it receives
		if (addReceivedMessageToStore(topic, message)) {
			// this is a new message - a value we haven't seen before

			//
			// inform the app (for times when the Activity UI is running) of the
			// received message so the app UI can be updated with the new data
			broadcastReceivedMessage(topic, message);

			//
			// inform the user (for times when the Activity UI isn't running)
			// that there is new data available
			notifyUser("New data received", topic, body);
		}

		// receiving this message will have kept the connection alive for us, so
		// we take advantage of this to postpone the next scheduled ping
		scheduleNextPing();

	}

	/************************************************************************/
	/* METHODS - wrappers for some of the MQTT methods that we use */
	/************************************************************************/

	/*
	 * Create a client connection object that defines our connection to a
	 * message broker server
	 */
	private void defineConnectionToBroker(String brokerHostName) {
		String mqttConnSpec = "tcp://" + brokerHostName + "@"
				+ brokerPortNumber;

		try {
			// define the connection to the broker
			mqttClient = MqttClient.createMqttClient(mqttConnSpec,
					usePersistence);

			// register this client app has being able to receive messages
			mqttClient.registerSimpleHandler(this);
		} catch (MqttException e) {
			// something went wrong!
			mqttClient = null;
			connectionStatus = MQTTConnectionStatus.NOTCONNECTED_UNKNOWNREASON;

			//
			// inform the app that we failed to connect so that it can update
			// the UI accordingly
			broadcastServiceStatus("Invalid connection parameters");

			//
			// inform the user (for times when the Activity UI isn't running)
			// that we failed to connect
			notifyUser("Unable to connect", "MQTT", "Unable to connect");
		}
	}

	/*
	 * (Re-)connect to the message broker
	 */
	private boolean connectToBroker() {
		try {
			// try to connect
			mqttClient
					.connect(mqttClientId, cleanStart, keepAliveSeconds);

			//
			// inform the app that the app has successfully connected
			broadcastServiceStatus("Connected");

			// we are connected
			connectionStatus = MQTTConnectionStatus.CONNECTED;

			// we need to wake up the phone's CPU frequently enough so that the
			// keep alive messages can be sent
			// we schedule the first one of these now
			scheduleNextPing();

			return true;
		} catch (MqttException e) {
			// something went wrong!

			connectionStatus = MQTTConnectionStatus.NOTCONNECTED_UNKNOWNREASON;

			//
			// inform the app that we failed to connect so that it can update
			// the UI accordingly
			broadcastServiceStatus("Unable to connect");

			//
			// inform the user (for times when the Activity UI isn't running)
			// that we failed to connect
			notifyUser("Unable to connect", "MQTT",
					"Unable to connect - will retry later");

			// if something has failed, we wait for one keep-alive period before
			// trying again
			// in a real implementation, you would probably want to keep count
			// of how many times you attempt this, and stop trying after a
			// certain number, or length of time - rather than keep trying
			// forever.
			// a failure is often an intermittent network issue, however, so
			// some limited retry is a good idea
			scheduleNextPing();

			return false;
		}
	}

	/*
	 * 메시지를 성공적으로 보내거나 받으면, ping 스케쥴을 다시 한다.
	 * Schedule the next time that you want the phone to wake up and ping the
	 * message broker server
	 */
	private void scheduleNextPing() {
		
		// 다음 핑할 시간.
		Calendar wakeUpTime = Calendar.getInstance();
//		wakeUpTime.add(Calendar.SECOND, keepAliveSeconds);
		wakeUpTime.add(Calendar.SECOND, 20);
		
		logger.info("ping scheduled. " + wakeUpTime.getTime());
		
		if(pingSender != null){
			PingSender old = pingSender;
			old.cancelTrigger();
			ping.purge();
		}
		pingSender = new PingSender();
		ping.schedule(pingSender, wakeUpTime.getTime());
	}
	
	/*
	 * Send a request to the message broker to be sent messages published with
	 * the specified topic name. Wildcards are allowed.
	 */
	private void subscribeToTopic(String topicName) {
		boolean subscribed = false;

		if (isAlreadyConnected() == false) {
			// quick sanity check - don't try and subscribe if we
			// don't have a connection
			
			logger.error("Unable to subscribe as we are not connected");
		} else {
			try {
				String[] topics = { topicName };
				mqttClient.subscribe(topics, qualitiesOfService);

				subscribed = true;
			} catch (MqttNotConnectedException e) {
				logger.error("subscribe failed - MQTT not connected", e);
			} catch (IllegalArgumentException e) {
				logger.error("subscribe failed - illegal argument", e);
			} catch (MqttException e) {
				logger.error("subscribe failed - MQTT exception", e);
			}
		}

		if (subscribed == false) {
			//
			// inform the app of the failure to subscribe so that the UI can
			// display an error
			broadcastServiceStatus("Unable to subscribe");

			//
			// inform the user (for times when the Activity UI isn't running)
			notifyUser("Unable to subscribe", "MQTT", "Unable to subscribe");
		}
	}

	/**
	 * 다른 사람에게 메시지를 보낼 때 사용한다.
	 * @param topic
	 * @param msg
	 */
	public void publishToTopic(String topic, String msg){
		try {
			JSONObject json =(JSONObject)new JSONTokener(msg).nextValue();
			json.put("sender", mqttClientId);
			String message = json.toString(); // mqttClientId+"&"+msg;
			mqttClient.publish(topic,  message.getBytes("UTF-8"),  QUALITY_OF_SERVICE,  RETAINED_PUBLISH);
			
			scheduleNextPing();
			
		} catch (MqttNotConnectedException e) {
			e.printStackTrace();
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
							
	}

	/*
	 * Terminates a connection to the message broker.
	 */
	private void disconnectFromBroker() {
		// if we've been waiting for an Internet connection, this can be
		// cancelled - we don't need to be told when we're connected now
		try {
			ping.cancel();
			ping.purge();
		} catch (Exception eee) {
			// probably because we hadn't registered it
			logger.error("unregister failed", eee);
		}

		try {
			if (mqttClient != null) {
				mqttClient.disconnect();
			}
		} catch (MqttPersistenceException e) {
			logger.error("disconnect failed - persistence exception", e);
		}

	}

	/*
	 * Checks if the MQTT client thinks it has an active connection
	 */
	private boolean isAlreadyConnected() {
		return ((mqttClient != null) && (mqttClient.isConnected()));
	}

	/*
	 * Used to implement a keep-alive protocol at this Service level - it sends
	 * a PING message to the server, then schedules another ping after an
	 * interval defined by keepAliveSeconds
	 */
	public class PingSender extends TimerTask {
		
		private boolean trigger = true;
		private boolean runned = false;
		
		/**
		 * 실행되지 않았으면, 작업을 취소한다.
		 * 이미 실행 되었으면 아무일을 하지 않는다. 
		 */
		public void cancelTrigger(){
			
			if(!runned){
				cancel();
				logger.debug("ping cancled.");
				trigger = false;
			}
		}
		
		public boolean runned(){
			return runned;
		}
		
		@Override
		public void run() {
			
			if(!trigger){
				logger.debug("ping is not run.");
				return;
			}
			
			// Note that we don't need a wake lock for this method (even though
			// it's important that the phone doesn't switch off while we're
			// doing this).
			// According to the docs, "Alarm Manager holds a CPU wake lock as
			// long as the alarm receiver's onReceive() method is executing.
			// This guarantees that the phone will not sleep until you have
			// finished handling the broadcast."
			// This is good enough for our needs.

			try {
				mqttClient.ping();
				logger.debug("ping." + new Date());
			} catch (MqttException e) {
				// if something goes wrong, it should result in connectionLost
				// being called, so we will handle it there
				logger.error("ping failed - MQTT exception", e);

				// assume the client connection is broken - trash it
				try {
					mqttClient.disconnect();
				} catch (MqttPersistenceException e1) {
					logger.error("disconnect failed - persistence exception",e1);
				}

				// reconnect
				if (connectToBroker()) {
					subscribeToTopic(topicName);
				}
			}
			
			runned = true;

			// start the next keep alive period
			scheduleNextPing();
		}
	}

	/************************************************************************/
	/* APP SPECIFIC - stuff that would vary for different uses of MQTT */
	/************************************************************************/

	// apps that handle very small amounts of data - e.g. updates and
	// notifications that don't need to be persisted if the app / phone
	// is restarted etc. may find it acceptable to store this data in a
	// variable in the Service
	// that's what I'm doing in this sample: storing it in a local hashtable
	// if you are handling larger amounts of data, and/or need the data to
	// be persisted even if the app and/or phone is restarted, then
	// you need to store the data somewhere safely
	// see http://developer.android.com/guide/topics/data/data-storage.html
	// for your storage options - the best choice depends on your needs

	// stored internally

	private Hashtable<String, Message> dataCache = new Hashtable<String, Message>();

	private boolean addReceivedMessageToStore(String key, Message message) {
		Message previousValue = null;

		previousValue = dataCache.put(key, message);

		// is this a new value? or am I receiving something I already knew?
		// we return true if this is something new
		return ((previousValue == null) || (previousValue.equals(message) == false));
	}
	
	private class Message {
		
		String sender;
		String receiver;
		String message;
		
	}

	// provide a public interface, so Activities that bind to the Service can
	// request access to previously received messages

	public void rebroadcastReceivedMessages() {
		Enumeration<String> e = dataCache.keys();
		while (e.hasMoreElements()) {
			String nextKey = e.nextElement();
			Message nextValue = dataCache.get(nextKey);

			broadcastReceivedMessage(nextKey, nextValue);
		}
	}

}