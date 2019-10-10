package it.almaviva.main.democentralina;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import java.util.UUID;

public class DemoCentralina {

    private static final String BROKER_URL = "tcp://10.207.232.210:1883";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "SeMmqtt1!";

    private MqttAsyncClient mqttClient;

    public DemoCentralina(){
        try {
            mqttClient = getMqttAsyncClient();
        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void subscribe(String topic, int qos) throws MqttException {
            this.mqttClient.setCallback(new MqttCallback() {

                @Override
                public void connectionLost(Throwable throwable) {
                    System.out.println(throwable.getMessage());
                    System.exit(0);
                }

                @Override
                public void messageArrived(String topicSubscribe, MqttMessage mqttMessage) throws Exception {
                    System.out.println(String.format("Json from topic:%s%s",System.lineSeparator(),mqttMessage.toString()));
                    JSONObject jsonCommand = new JSONObject(mqttMessage.toString());
                    JSONObject jsonCommandFeedback = createCmdFeedbackPayload(jsonCommand);
                    String topicPublish = topicSubscribe.replaceAll("/GICS/CMD","/GICS/CMD/FEEDBACK");
                    publish(topicPublish,jsonCommandFeedback.toString(2),0);

                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

                }

                public void publish(String topic, String json, int qos) throws MqttException {
                    MqttMessage mqttMessage = new MqttMessage(json.getBytes());
                    mqttMessage.setQos(qos);
                    System.out.println(String.format("Publish su: %s%s%s",topic,System.lineSeparator(),new JSONObject(json).toString(2)));
                    mqttClient.publish(topic,mqttMessage);
                }

                private JSONObject createCmdFeedbackPayload(JSONObject jsonCommand){
                    JSONObject cmdFeedbackPayload;
                    try{
                        JSONObject commandSubJson = jsonCommand.getJSONObject("Command");
                        cmdFeedbackPayload = new JSONObject(jsonCommand.toString());
                        cmdFeedbackPayload.remove("Command");
                        cmdFeedbackPayload.put("MsgId",UUID.randomUUID().toString());
                        cmdFeedbackPayload.put("Timestamp",System.currentTimeMillis());
                        commandSubJson.put("Success",true);
                        cmdFeedbackPayload.put("Response",commandSubJson);
                    }catch(Exception e){
                        e.printStackTrace();
                        cmdFeedbackPayload = null;
                    }
                    return cmdFeedbackPayload;
                }
            });
            this.mqttClient.subscribe(topic,qos);
            System.out.println(String.format("Start subscribe topic: %s",topic));
    }


    public static MqttAsyncClient getMqttAsyncClient() throws MqttException {
        MqttAsyncClient mqttAsyncClient = new MqttAsyncClient(BROKER_URL,UUID.randomUUID().toString(), new MemoryPersistence());
        MqttConnectOptions connOptions = new MqttConnectOptions();
        connOptions.setUserName(USERNAME);
        connOptions.setPassword(PASSWORD.toCharArray());
        connOptions.setAutomaticReconnect(true);
        mqttAsyncClient.connect(connOptions).waitForCompletion();
        return mqttAsyncClient;
    }

}
