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
package org.mapton.api;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class MServiceKeyManager {

    private Properties mProperties;

    public static MServiceKeyManager getInstance() {
        return Holder.INSTANCE;
    }

    private MServiceKeyManager() {
    }

    public String getKey(String key) {
        if (mProperties == null) {
            mProperties = new Properties();
            try {
                var urlConnection = new URL("https://mapton.org/files/services.properties").openConnection();
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);
                urlConnection.connect();
                mProperties.load(urlConnection.getInputStream());
                urlConnection.getInputStream().close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return mProperties.getProperty(key, "");
    }

    private static class Holder {

        private static final MServiceKeyManager INSTANCE = new MServiceKeyManager();
    }
}
