package carcaro.service;

public interface MqttConnectionListner {

	void mqttConnected(String msg);

	void mqttDisconnected(String msg);

}
