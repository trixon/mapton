/* 
 * Copyright 2021 Patrik Karlström.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mapton.nominatim;

import fr.dudie.nominatim.client.JsonNominatimClient;
import fr.dudie.nominatim.client.NominatimOptions;
import fr.dudie.nominatim.model.Address;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Logger;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.mapton.api.MLatLon;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class Nominatim {

    private static final Logger LOGGER = Logger.getLogger(Nominatim.class.getName());

    public static Nominatim getInstance() {
        return Holder.INSTANCE;
    }

    public static void main(String[] args) throws IOException {
        Nominatim nominatim = getInstance();
    }

    private Nominatim() {
    }

    public Address getAddress(MLatLon latLon) throws IOException {
        return getClient().getAddress(latLon.getLongitude(), latLon.getLatitude());
    }

    public Address getAddress(MLatLon latLon, int zoom) throws IOException {
        return getClient().getAddress(latLon.getLongitude(), latLon.getLatitude(), zoom);
    }

    public List<Address> search(String string) throws IOException {
        return getClient().search(string);
    }

    private JsonNominatimClient getClient() {
        InputStream inputStream = null;

        try {
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
            ClientConnectionManager connexionManager = new SingleClientConnManager(null, registry);
            HttpClient httpClient = new DefaultHttpClient(connexionManager, null);

            inputStream = new URL("https://mapton.org/files/nominatim-client.properties").openStream();
            Properties properties = new Properties();
            properties.load(inputStream);
            String baseUrl = properties.getProperty("nominatim.server.url");
            String email = properties.getProperty("nominatim.headerEmail");
            NominatimOptions options = new NominatimOptions();
            options.setAcceptLanguage(Locale.getDefault());
            return new JsonNominatimClient(baseUrl, httpClient, email, options);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return null;
    }

    private static class Holder {

        private static final Nominatim INSTANCE = new Nominatim();
    }
}
