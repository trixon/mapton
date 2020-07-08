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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.apache.commons.io.FileUtils;
import org.mapton.api.MServiceKeyManager;
import org.mapton.api.Mapton;
import org.openide.util.Exceptions;
import se.trixon.trv_traffic_information.TrafficInformation;

/**
 *
 * @author Patrik Karlström
 */
public class TrafficInformationManager {

    private final File mCacheDir;
    private HashMap<File, Long> mFileToTimestamp = new HashMap<>();
    private final TrafficInformationManager mManager = getInstance();
    private List<se.trixon.trv_traffic_information.road.camera.v1.RESULT> mResultsCamera;
    private List<se.trixon.trv_traffic_information.road.parking.v1_4.RESULT> mResultsParking;
    private List<se.trixon.trv_traffic_information.road.trafficsafetycamera.v1.RESULT> mResultsTrafficSafetyCamera;
    private List<se.trixon.trv_traffic_information.road.weatherstation.v1.RESULT> mResultsWeatherStation;
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

    public List<se.trixon.trv_traffic_information.road.camera.v1.RESULT> getResultsCamera() {
        File file = getFile(Service.CAMERA);
        if (file.exists()) {
            if (isOutOfDate(mResultsCamera, file)) {
                try {
                    mResultsCamera = mTrafficInformation.road().getCameraResults(file);
                    mFileToTimestamp.put(file, file.lastModified());
                } catch (IOException | InterruptedException | JAXBException ex) {
                    mResultsCamera = new ArrayList<>();
                    Exceptions.printStackTrace(ex);
                }
            }
        } else {
            mResultsCamera = new ArrayList<>();
        }

        return mResultsCamera;
    }

    public List<se.trixon.trv_traffic_information.road.parking.v1_4.RESULT> getResultsParking() {
        File file = getFile(Service.PARKING);
        if (file.exists()) {
            if (isOutOfDate(mResultsParking, file)) {
                try {
                    mResultsParking = mTrafficInformation.road().getParkingResults(file);
                    mFileToTimestamp.put(file, file.lastModified());
                } catch (IOException | InterruptedException | JAXBException ex) {
                    mResultsParking = new ArrayList<>();
                    Exceptions.printStackTrace(ex);
                }
            }
        } else {
            mResultsParking = new ArrayList<>();
        }

        return mResultsParking;
    }

    public List<se.trixon.trv_traffic_information.road.trafficsafetycamera.v1.RESULT> getResultsTrafficSafetyCamera() {
        File file = getFile(Service.TRAFFIC_SAFETY_CAMERA);
        if (file.exists()) {
            if (isOutOfDate(mResultsTrafficSafetyCamera, file)) {
                try {
                    mResultsTrafficSafetyCamera = mTrafficInformation.road().getTrafficSafetyCameraResults(file);
                    mFileToTimestamp.put(file, file.lastModified());
                } catch (IOException | InterruptedException | JAXBException ex) {
                    mResultsTrafficSafetyCamera = new ArrayList<>();
                    Exceptions.printStackTrace(ex);
                }
            }
        } else {
            mResultsTrafficSafetyCamera = new ArrayList<>();

        }
        return mResultsTrafficSafetyCamera;
    }

    public List<se.trixon.trv_traffic_information.road.weatherstation.v1.RESULT> getResultsWeatherStation() {
        File file = getFile(Service.WEATHER_STATION);
        if (file.exists()) {
            if (isOutOfDate(mResultsWeatherStation, file)) {
                try {
                    mResultsWeatherStation = mTrafficInformation.road().getWeatherStationResults(file);
                    mFileToTimestamp.put(file, file.lastModified());
                } catch (IOException | InterruptedException | JAXBException ex) {
                    mResultsWeatherStation = new ArrayList<>();
                    Exceptions.printStackTrace(ex);
                }
            }
        } else {
            mResultsWeatherStation = new ArrayList<>();
        }

        return mResultsWeatherStation;
    }

    public TrafficInformation getTrafficInformation() {
        return mTrafficInformation;
    }

    private boolean isOutOfDate(Object result, File file) {
        long lastModified = mFileToTimestamp.computeIfAbsent(file, k -> 0L);
        return result == null || file.lastModified() > lastModified;
    }

    private static class Holder {

        private static final TrafficInformationManager INSTANCE = new TrafficInformationManager();
    }

    public enum Service {
        CAMERA("camera.xml"),
        PARKING("parking.xml"),
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
