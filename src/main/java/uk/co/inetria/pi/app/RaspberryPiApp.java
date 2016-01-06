/**
 * 
 */
package uk.co.inetria.pi.app;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.appspot.raspberrypi_dash.sensor.Sensor;
import com.appspot.raspberrypi_dash.sensor.model.SensorData;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.pi4j.wiringpi.Spi;

/**
 * Java application that talks to the RaspberryPi GPIO SPI pins connected to MCP3008 chip
 * MCP3008 - 8-Channel 10-Bit ADC With SPI Interface, https://www.adafruit.com/products/856
 * The MCP3008 chip has 3 devices connected to it:
 * 
 * 1- off the shelf photocell to measure illumination 
 * 2- TMP36 - Analog Temperature sensor - TMP36, https://www.adafruit.com/products/165
 * 3- Solar Cell 6.5 x 6.5 cm, 5.5 volt, http://www.amazon.co.uk/gp/product/B00KCP76CS?ref_=pe_385721_51767431_TE_dp_1
 * 
 * This app updates the sensor data on a Google App Engine backend using authenticated cloud endpoint client. OAuth2 is used
 * for authentication
 * 
 * @author omerio
 *
 */
public class RaspberryPiApp {
	
	private static final Logger log = Logger.getLogger(RaspberryPiApp.class.getName());
	
	private static final Set<String> SCOPES = Collections.singleton("https://www.googleapis.com/auth/userinfo.email");
	
	private static final String APP_NAME = "Raspberry Pi App";
	
	// set your endpoint url here
	private static final String DEFAULT_ROOT_URL = "https://raspberrypi-dash.appspot.com/_ah/api/"; //"http://localhost:8080/_ah/api/"; 
	
	// MCP3008 chip channels
	private static final int LIGHT = 0;
	private static final int TEMP = 1;
	private static final int VOLT = 2;
	
	private static final double [][] PHOTO_CELL = new double [][] {
		new double []{600_000, 0.1},
		new double []{70_000, 1},
		new double []{10_000, 10},
		new double []{1_500, 100},
		new double []{300, 1000}
	};
	
	private static double previousTemp = 0;
	private static double previousLux = 0;
	private static double previousVolt = 0;
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		
		log.info("Starting app");
		
		// get the sensor endpoint
		Sensor sensor = getSensorEndpoint();
		
		 // setup SPI for communication
        int fd = Spi.wiringPiSPISetup(0, 10000000);
        if (fd <= -1) {
            log.severe(" ==>> SPI SETUP FAILED");
            return;
        }
		
        // infinite loop
        while(true) {
        	
        	int data = readChannel(TEMP);
        	double temp = calculateTemperature(data);
            
        	//log.info("Temperature raw reading is: " + data);
        	//log.info("Temperature Degrees reading is: " + temp);
        	
        	data = readChannel(LIGHT);
        	double lux = calculateIllumination(data);
        	
        	//log.info("Light raw reading is: " + data);
        	//log.info("Light Lux reading is: " + lux);
        	
        	
        	data = readChannel(VOLT);
        	double volt = calculateVoltage(data);
        	
        	//log.info("Voltage raw reading is: " + data);
        	//log.info("Voltage reading is: " + volt);
        	
        	// ditch the first 20 readings, values seem to oscillate a lot at startup
        	int i = 0;
        	boolean changed = false;
        	
        	if(i > 19) {
        		try {

        			if(valueChanged(temp, previousTemp, 1)) {
        				log.info("Temperature changed: " + temp);
        				sendSensorData(temp, "temperature", sensor);
        				changed = true;
        			}

        			if(valueChanged(lux, previousLux, 1)) {
        				log.info("Lux changed: " + lux);
        				sendSensorData(lux, "illuminance", sensor);
        				changed = true;
        			}

        			if(valueChanged(volt, previousVolt, 0.05)) {
        				log.info("Volt changed: " + volt);
        				sendSensorData(volt, "voltage", sensor);
        				changed = true;
        			}

        		} catch(IOException e) {
        			log.log(Level.SEVERE, "Failed to update backend", e);
        			Thread.sleep(10000);
        		}

        		previousTemp = temp;
        		previousLux = lux;
        		previousVolt = volt;
        		
        	} else {
        		i++;
        	}

        	if(!changed) {
        		// only sleep we have not changed anything
        		Thread.sleep(1000);
        	}

        }
	}
	
	/**
	 * Has any of the values changed
	 * @param temp
	 * @param volt
	 * @param lux
	 * @return
	 */
	private static boolean valueChanged(double newValue, double oldValue, double difference) {
		return Math.abs(newValue - oldValue) >= difference;
	}
	
	/**
	 * http://emant.com/733010.page
	 * http://raspi.tv/2013/controlled-shutdown-duration-test-of-pi-model-a-with-2-cell-lipo
	 * @param data
	 * @return
	 */
	private static double calculateVoltage(int data) {
		double vo = data * 3.3 / 1023.0;
        double vin = vo * 1.67;
        // round to two decimal places
        vin = Math.round(vin * 100.0) / 100.0;
        return vin;
	}
	
	/**
	 * Calculate the illumination in Lux from a photocell resistance.
	 * This is just an approximation
	 * https://learn.adafruit.com/photocells/using-a-photocell
	 * https://learn.adafruit.com/photocells/measuring-light
	 * @return
	 */
	private static double calculateIllumination(int data) {
		 
         double voltage = data * 3.3 / 1023.0;
         double resistance = (33.0/voltage - 10) * 1000;
         
         double [] values = null;
         
         for(double [] pair: PHOTO_CELL) {
        	 if(resistance >= pair[0]) {
        		 values = pair;
        		 break;
        	 }
         }
         
         if(values == null) {
        	 values = PHOTO_CELL[4];
         }
         
         return values[1];
         
	}
	
	/**
	 * convert the adc value to millivolts: millivolts = data * ( 3300.0 / 1023.0 )
	 * convert the millivolts value to a celsius temperature: temp_C = ((millivolts - 100.0) / 10.0) - 40.0
	 * https://www.adafruit.com/products/165
	 * https://learn.adafruit.com/send-raspberry-pi-data-to-cosm/python-script
	 * @param data
	 * @return
	 */
	private static double calculateTemperature(int data) {
         double millivolts = data * ( 3300.0 / 1023.0 );
         double temp = ((millivolts - 100.0) / 10.0) - 40.0;
         
         return Math.round(temp);
	}
	
	/**
	 * Function to read SPI data from MCP3008 chip
	 * Channel must be an integer 0-7
	 * Based on this Python code 
	 * (see http://www.raspberrypi-spy.co.uk/2013/10/analogue-sensors-on-the-raspberry-pi-using-an-mcp3008/)
	 * adc = spi.xfer2([1,(8+channel)<<4,0])
	 * data = ((adc[1]&3) << 8) + adc[2]
	 * 
	 * We also make heavy use of bitwise operator, see here for more info 
	 * https://docs.oracle.com/javase/tutorial/java/nutsandbolts/op3.html
	 */
	private static int readChannel(int channel) {
		
		byte packet[] = new byte[3]; // we send 3 bytes to communicate with the MCP3008 chip
		packet[0] = 0b00000001;  					 // start bit
		packet[1] = (byte) ((8 + channel) << 4);     // data,explained below
		// 8 = 0b1000, add channel, shift left 4 places. chip expects 0b10010000 to read channel 1
		packet[2] = 0b00000000;  					 // chip doesn't care!

		Spi.wiringPiSPIDataRW(0, packet, 3);   
		
		// extract a 10 bits number from the 3 bytes returned (explained below)
		int data = ((packet[1] & 0b11) << 8) | (packet[2] & 0xff); // & 0xff to loose the sign, java bytes & ints are signed!
		// chip returns 3 bytes at indexes, 0, 1 & 2
		// ?_?_?_?_?_?_?_?, ?_?_?_?_?_0_b9_b8, b7_b6_b5_b4_b3_b2_b1_b0 
		// ?_?_?_?_?_0_b9_b8 & 0b11 = b9_b8
		// 0_0_0_0_0_0_b9_b8 << 8 = b9_b8_0_0_0_0_0_0_0_0
		// b9_b8_0_0_0_0_0_0_0_0 + packet[2] = b9_b8_0_0_0_0_0_0_0_0 + b7_b6_b5_b4_b3_b2_b1_b0  = b9_b8_b7_b6_b5_b4_b3_b2_b1_b0
		
		return data;
	}
	
	/**
	 * 
	 * @param value
	 * @param channel
	 * @param sensor
	 * @throws IOException
	 */
	private static void sendSensorData(double value, String channel, Sensor sensor) throws IOException {
		// test creating some sample data
		SensorData data = new SensorData();
		data.setChannel(channel);
		data.setDateTime(new DateTime(new Date()));
		data.setValue(value);

		sensor.data().create(data).execute();
	}
	
	  /**
     * Create a Sensor API client instance
     * @return
     * @throws IOException 
     * @throws GeneralSecurityException 
     */
	private static Sensor getSensorEndpoint() throws IOException {
		
		CmdLineAuthenticationProvider provider = new CmdLineAuthenticationProvider();
		// see https://developers.google.com/api-client-library/java/
		provider.setClientSecretsFile("client_secret.json");
		provider.setScopes(SCOPES);
		
		// get the oauth credentials
		Credential credential = provider.authorize();
		
		// initialize the transport
		HttpTransport httpTransport;
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException e) {
            log.log(Level.SEVERE, "failed to create transport", e);
            throw new IOException(e);
        }

		return new Sensor.Builder(httpTransport, JacksonFactory.getDefaultInstance(), credential)
				.setApplicationName(APP_NAME).setRootUrl(DEFAULT_ROOT_URL).build();

	}

}
