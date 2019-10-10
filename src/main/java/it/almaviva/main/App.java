package it.almaviva.main;

import it.almaviva.main.democentralina.DemoCentralina;
import org.eclipse.paho.client.mqttv3.MqttException;


public class App {
    private static final String TOPIC = "SVIL/LO4000/GICS/CMD/GWY_Galleria_Orte_LFM";
    private static final int QoS = 0;

    public static void main( String[] args ) throws MqttException {
        DemoCentralina demoCentralina = new DemoCentralina();
        demoCentralina.subscribe(TOPIC,QoS);
    }
}
