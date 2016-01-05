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

/**
 * @author omerio
 *
 */
public class RaspberryPiApp {
	
	private static final Logger log = Logger.getLogger(RaspberryPiApp.class.getName());
	
	private static final Set<String> SCOPES = Collections.singleton("https://www.googleapis.com/auth/userinfo.email");
	
	private static final String APP_NAME = "Raspberry Pi App";
	
	// set your endpoint url here
	private static final String DEFAULT_ROOT_URL = "https://raspberrypi-dash.appspot.com/_ah/api/"; //"http://localhost:8080/_ah/api/"; 

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		CmdLineAuthenticationProvider provider = new CmdLineAuthenticationProvider();
		provider.setClientSecretsFile("/client_secret.json");
		provider.setScopes(SCOPES);
		
		// get the oauth credentials
		Credential credential = provider.authorize();
		
		// get the sensor endpoint
		Sensor sensor = getSensorEndpoin(credential);
		
		// test creating some sample data
		SensorData data = new SensorData();
		data.setChannel("temperature");
		data.setDateTime(new DateTime(new Date()));
		data.setValue(10d);
		
		sensor.data().create(data).execute();

	}
	
	  /**
     * Create a Sensor API client instance
     * @return
     * @throws IOException 
     * @throws GeneralSecurityException 
     */
	private static Sensor getSensorEndpoin(Credential credential) throws IOException {
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
