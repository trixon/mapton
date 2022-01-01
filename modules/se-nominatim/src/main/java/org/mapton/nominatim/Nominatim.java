/*
 * Copyright 2022 Patrik Karlström.
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Logger;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
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
        System.out.println(ToStringBuilder.reflectionToString(nominatim.getAddress(new MLatLon(57.685575, 11.959081)), ToStringStyle.JSON_STYLE));
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
        try {
            int timeout = 5000;

            var urlConnection = new URL("https://mapton.org/files/nominatim-client.properties").openConnection();
            urlConnection.setConnectTimeout(timeout);
            urlConnection.setReadTimeout(timeout);
            urlConnection.connect();

            var properties = new Properties();
            properties.load(urlConnection.getInputStream());
            urlConnection.getInputStream().close();

            String baseUrl = properties.getProperty("nominatim.server.url");
            String email = properties.getProperty("nominatim.headerEmail");

            var nominatimOptions = new NominatimOptions();
            nominatimOptions.setAcceptLanguage(Locale.getDefault());

            var requestConfig = RequestConfig.custom()
                    .setConnectTimeout(timeout)
                    .setConnectionRequestTimeout(timeout)
                    .setSocketTimeout(timeout)
                    .build();

            var httpClient = HttpClientBuilder.create()
                    .setDefaultRequestConfig(requestConfig)
                    .setSSLSocketFactory(SSLConnectionSocketFactory.getSocketFactory())
                    .build();

            return new JsonNominatimClient(baseUrl, httpClient, email, nominatimOptions);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    private static class Holder {

        private static final Nominatim INSTANCE = new Nominatim();
    }
}
