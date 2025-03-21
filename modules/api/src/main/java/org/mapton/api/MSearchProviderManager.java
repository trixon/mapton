/*
 * Copyright 2025 Patrik Karlström.
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

import java.util.HashMap;

/**
 *
 * @author Patrik Karlström
 */
public class MSearchProviderManager {

    private final HashMap<String, String> mMap = new HashMap<>();

    public static MSearchProviderManager getInstance() {
        return Holder.INSTANCE;
    }

    private MSearchProviderManager() {
    }

    public HashMap<String, String> getMap() {
        return mMap;
    }

    public String getUrl(String externalSysId, String value) {
        var baseUrl = mMap.get(externalSysId);
        if (baseUrl != null) {
            return baseUrl.formatted(value);
        } else {
            return null;
        }
    }

    private static class Holder {

        private static final MSearchProviderManager INSTANCE = new MSearchProviderManager();
    }
}
