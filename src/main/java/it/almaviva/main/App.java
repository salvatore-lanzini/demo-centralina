package it.almaviva.main;

import it.almaviva.main.democentralina.DemoCentralina;
import org.eclipse.paho.client.mqttv3.MqttException;
import java.io.IOException;

public class App {

        public static void main( String[] args ) throws MqttException, IOException {
            DemoCentralina.getInstance().subscribe();
        }
}
