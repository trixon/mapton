/*
 * Copyright 2020 Patrik Karlström.
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
package org.mapton.poi_trv_ti;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.apache.commons.io.FileUtils;
import org.mapton.api.MServiceKeyManager;
import org.mapton.api.Mapton;
import org.openide.util.Exceptions;
import se.trixon.trv_ti.TrafficInformation;

/**
 *
 * @author Patrik Karlström
 */
public class TrafficInformationManager {

    private final File mCacheDir;
    private final TrafficInformationManager mManager = getInstance();
    private HashMap<Service, File> mServiceToFile = new HashMap<>();
    private final TrafficInformation mTrafficInformation;

    public static TrafficInformationManager getInstance() {
        return Holder.INSTANCE;
    }

    private TrafficInformationManager() {
        mTrafficInformation = new TrafficInformation(MServiceKeyManager.getInstance().getKey("001"));
        mCacheDir = new File(Mapton.getCacheDir(), "poi/trv-ti");
        try {
            FileUtils.forceMkdir(mCacheDir);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public File getFile(Service service) {
        return mServiceToFile.computeIfAbsent(service, k -> new File(mCacheDir, service.getFilename()));
    }

    public TrafficInformation getTrafficInformation() {
        return mTrafficInformation;
    }

    private static class Holder {

        private static final TrafficInformationManager INSTANCE = new TrafficInformationManager();
    }

    public enum Service {
        CAMERA("camera.xml"),
        TRAFFIC_SAFETY_CAMERA("traffic_safety_camera.xml"),
        WEATHER_STATION("weather_station.xml");
        private final String mFilename;

        private Service(String filename) {
            mFilename = filename;
        }

        public String getFilename() {
            return mFilename;
        }
    }
}
