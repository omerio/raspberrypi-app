/**
 * The contents of this file may be used under the terms of the Apache License, Version 2.0
 * in which case, the provisions of the Apache License Version 2.0 are applicable instead of those above.
 *
 * Copyright 2014, Ecarf.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package uk.co.inetria.pi.app;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;


/**
 * Google API command line authentication provider. Useful for interactively obtaining
 * OAuth2 auothrization from users running applications on the commandline
 * 
 * @author Omer Dawelbeit (omerio)
 *
 */
public class CmdLineAuthenticationProvider {

	private static final Logger log = Logger.getLogger(CmdLineAuthenticationProvider.class.getName());

    /** Directory to store user credentials. */
    private static final java.io.File DATA_STORE_DIR =
            new java.io.File(System.getProperty("user.home"), ".store/pi_app");

    /**
     * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single
     * globally shared instance across your application.
     */
    private static FileDataStoreFactory dataStoreFactory;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport httpTransport;

    private Set<String> scopes;

    private Credential credential;

    private String clientSecretsFile;

    public Credential authorize() throws IOException {
        if(this.credential == null) {
            this.credential = getCredential();
        }

        return this.credential;
    }

    /**
     * Create an instance of Credential
     * @return
     * @throws IOException
     */
    protected Credential getCredential() throws IOException {
        
        if(this.clientSecretsFile == null) {
        	throw new IllegalArgumentException("client secrets file is required");
        }
        
        if(this.scopes == null) {
        	throw new IllegalArgumentException("you need to provide at least one scope");
        }
        
        // initialize the transport
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException e) {
            log.log(Level.SEVERE, "failed to create transport", e);
            throw new IOException(e);
        }

        // initialize the data store factory
        dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
        // load client secrets
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(CmdLineAuthenticationProvider.class.getResourceAsStream(this.clientSecretsFile)));

        // Set up authorization code flow.

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, scopes)
                .setDataStoreFactory(dataStoreFactory)
                .build();

        // authorize
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    /**
     * @param scopes the scopes to set
     */
    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    /**
     * @param clientSecretsFile the clientSecretsFile to set
     */
    public void setClientSecretsFile(String clientSecretsFile) {
        this.clientSecretsFile = clientSecretsFile;
    }
}
