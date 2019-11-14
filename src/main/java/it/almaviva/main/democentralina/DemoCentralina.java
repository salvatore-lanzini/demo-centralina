package it.almaviva.main.democentralina;

import it.almaviva.main.propertiesloeader.PropertiesLoader;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

public class DemoCentralina {

    private static final String BROKER_HOST = "broker.host";
    private static final String BROKER_PORT = "broker.port";
    private static final String BROKER_USERNAME = "broker.username";
    private static final String BROKER_PASSWORD = "broker.password";
    private static final String BROKER_TOPIC_CODICE_STAZIONE = "broker.topic.codice.stazione";
    private static final String BROKER_TOPIC_GICS_GATEWAY_PHYSICAL_ID = "broker.topic.gics.gateway.physical.id";
    private static final String HIVE_NAME_TOPIC = "hive.name.topic";

    private static DemoCentralina demoCentralina = null;
    private MqttAsyncClient mqttClient;
    private Properties properties;
    private String topic;

    private DemoCentralina(Properties properties){
        this.properties = properties;
        try {
            mqttClient = getMqttAsyncClient();
        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static DemoCentralina getInstance() throws IOException {
        if(demoCentralina == null) {
            Properties properties = new PropertiesLoader().loadLocalProperties();
            demoCentralina = new DemoCentralina(properties);
        }
        return demoCentralina;
    }

    public void subscribe() throws MqttException {

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

            String codiceStazione = this.properties.getProperty(BROKER_TOPIC_CODICE_STAZIONE);
            String gicsGatewayPhysycalId = this.properties.getProperty(BROKER_TOPIC_GICS_GATEWAY_PHYSICAL_ID);
            String hiveNameTopic = this.properties.getProperty(HIVE_NAME_TOPIC);
            String topic = String.format("%s/%s/GICS/CMD/%s",hiveNameTopic,codiceStazione,gicsGatewayPhysycalId);
            this.mqttClient.subscribe(topic,0);
            System.out.println(String.format("Start subscribe topic: %s",topic));
    }


    private MqttAsyncClient getMqttAsyncClient() throws MqttException {
        String brokerHost = this.properties.getProperty(BROKER_HOST);
        int brokerPort = Integer.parseInt(this.properties.getProperty(BROKER_PORT));
        String brokerUsername = this.properties.getProperty(BROKER_USERNAME);
        String brokerPassword = this.properties.getProperty(BROKER_PASSWORD);
        String brokerUrl=String.format("tcp://%s:%d",brokerHost,brokerPort);
        MqttAsyncClient mqttAsyncClient = new MqttAsyncClient(brokerUrl,UUID.randomUUID().toString(), new MemoryPersistence());
        MqttConnectOptions connOptions = new MqttConnectOptions();
        connOptions.setUserName(brokerUsername);
        connOptions.setPassword(brokerPassword.toCharArray());
        connOptions.setAutomaticReconnect(true);
        mqttAsyncClient.connect(connOptions).waitForCompletion();
        return mqttAsyncClient;
    }

}
