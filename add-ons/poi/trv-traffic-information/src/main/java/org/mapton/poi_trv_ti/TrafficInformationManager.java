/*
 * Copyright 2023 Patrik Karlström.
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

import jakarta.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MServiceKeyManager;
import org.mapton.api.MSimpleObjectStorageManager;
import org.mapton.api.Mapton;
import org.openide.util.Exceptions;
import se.trixon.almond.util.Direction;
import se.trixon.almond.util.SystemHelper;
import se.trixon.trv_traffic_information.TrafficInformation;
import se.trixon.trv_traffic_information.road.weathermeasurepoint.v2_1.PrecipitationConditionAggregated;
import se.trixon.trv_traffic_information.road.weathermeasurepoint.v2_1.WindCondition;

/**
 *
 * @author Patrik Karlström
 */
public class TrafficInformationManager {

    private final File mCacheDir;
    private final ConcurrentHashMap<String, String> mCameraGroupToPhotoUrl = new ConcurrentHashMap<>();
    private HashMap<File, Long> mFileToTimestamp = new HashMap<>();
    private List<se.trixon.trv_traffic_information.road.camera.v1_1.RESULT> mResultsCamera;
    private List<se.trixon.trv_traffic_information.road.parking.v1_4.RESULT> mResultsParking;
    private List<se.trixon.trv_traffic_information.road.trafficsafetycamera.v1.RESULT> mResultsTrafficSafetyCamera;
    private List<se.trixon.trv_traffic_information.road.weathermeasurepoint.v2_1.RESULT> mResultsWeatherMeasurepoint;
    private List<se.trixon.trv_traffic_information.road.weatherobservation.v2_1.RESULT> mResultsWeatherObservation;
    private HashMap<Service, File> mServiceToFile = new HashMap<>();
    private final TrafficInformation mTrafficInformation;

    public static TrafficInformationManager getInstance() {
        return Holder.INSTANCE;
    }

    private TrafficInformationManager() {
        var key = MSimpleObjectStorageManager.getInstance().getString(ApiKeyProvider.class, null);
        if (StringUtils.isBlank(key)) {
            key = MServiceKeyManager.getInstance().getKey("001");
        }
        mTrafficInformation = new TrafficInformation(key);
        mCacheDir = new File(Mapton.getCacheDir(), "poi/trv-ti");

        try {
            FileUtils.forceMkdir(mCacheDir);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public ConcurrentHashMap<String, String> getCameraGroupToPhotoUrl() {
        return mCameraGroupToPhotoUrl;
    }

    public File getFile(Service service) {
        return mServiceToFile.computeIfAbsent(service, k -> new File(mCacheDir, service.getFilename()));
    }

    public String getIconUrl(WindCondition windCondition) {
        var azimuth = windCondition.getDirection().getValue().getValue();
        var direction = Direction.fromAzimuth(azimuth);

        return "%swind%s.png".formatted(SystemHelper.getPackageAsPath(TrafficInfoPoiProvider.class), direction.getShortName());
    }

    public String getIconUrl(PrecipitationConditionAggregated precipitation) {
        var basename = "NoData";
        basename = "NoPrecipitation";
        var total = 2 * precipitation.getTotalWaterEquivalent().getValue().getValue().doubleValue();

        if (total > 0) {
            var rain = precipitation.getRain().isValue();
            var snow = precipitation.getSnow().isValue();
            String type = "";
            if (rain && snow) {
                type = "Sleet";
            } else if (rain) {
                type = "Rain";
            } else if (snow) {
                type = "Snow";
            }

            String amount = null;
            if (total > 4) {
                amount = "Heavy";
            } else if (total > 0.5) {
                amount = "Moderate";
            } else {
                amount = "Light";
            }
            basename = amount + type;
        }

        return "%sprecipitation%s.png".formatted(SystemHelper.getPackageAsPath(TrafficInfoPoiProvider.class), basename);

//        precipitation.
//        switch (precipitation.toString()) {
//            case "Givare saknas/Fel på givare":
//                break;
//            case "Lätt regn":
//                basename = "LightRain";
//                break;
//            case "Måttligt regn":
//                basename = "ModerateRain";
//                break;
//            case "Kraftigt regn":
//                basename = "HeavyRain";
//                break;
//            case "Lätt snöblandat regn":
//                basename = "LightSleet";
//                break;
//            case "Måttligt snöblandat regn":
//                basename = "ModerateSleet";
//                break;
//            case "Kraftigt snöblandat regn":
//                basename = "HeavySleet";
//                break;
//            case "Lätt snöfall":
//                basename = "LightSnow";
//                break;
//            case "Måttligt snöfall":
//                basename = "ModerateSnow";
//                break;
//            case "Kraftigt snöfall":
//                basename = "HeavySnow";
//                break;
//            case "Annan nederbördstyp":
//                basename = "NoData";
//                break;
//
//            case "Ingen nederbörd":
//                basename = "NoPrecipitation";
//                break;
//
//            case "Okänd nederbördstyp":
//                basename = "NoData";
//                break;
//
//            default:
//                throw new AssertionError();
//        }
//
    }

    public List<se.trixon.trv_traffic_information.road.camera.v1_1.RESULT> getResultsCamera() {
        File file = getFile(Service.CAMERA);
        if (file.exists()) {
            if (isOutOfDate(mResultsCamera, file)) {
                try {
                    mResultsCamera = mTrafficInformation.road().getCameraResults(file);
                    mFileToTimestamp.put(file, file.lastModified());
                } catch (IOException | InterruptedException | JAXBException ex) {
                    mResultsCamera = new ArrayList<>();
                    Exceptions.printStackTrace(ex);
                    if (ex instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
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
                    if (ex instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
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
                    if (ex instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        } else {
            mResultsTrafficSafetyCamera = new ArrayList<>();

        }
        return mResultsTrafficSafetyCamera;
    }

    public List<se.trixon.trv_traffic_information.road.weathermeasurepoint.v2_1.RESULT> getResultsWeatherMeasurepoint() {
        var file = getFile(Service.WEATHER_MEASUREPOINT);
        if (file.exists()) {
            if (isOutOfDate(mResultsWeatherMeasurepoint, file)) {
                try {
                    mResultsWeatherMeasurepoint = mTrafficInformation.road().getWeatherMeasurepointResults(file);
                    mFileToTimestamp.put(file, file.lastModified());
                } catch (IOException | InterruptedException | JAXBException ex) {
                    mResultsWeatherMeasurepoint = new ArrayList<>();
                    Exceptions.printStackTrace(ex);
                    if (ex instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        } else {
            mResultsWeatherMeasurepoint = new ArrayList<>();
        }

        return mResultsWeatherMeasurepoint;
    }

    public List<se.trixon.trv_traffic_information.road.weatherobservation.v2_1.RESULT> getResultsWeatherObservation() {
        var file = getFile(Service.WEATHER_OBSERVATION);
        if (file.exists()) {
            if (isOutOfDate(mResultsWeatherObservation, file)) {
                try {
                    mResultsWeatherObservation = mTrafficInformation.road().getWeatherObservationResults(file);
                    mFileToTimestamp.put(file, file.lastModified());
                } catch (IOException | InterruptedException | JAXBException ex) {
                    mResultsWeatherObservation = new ArrayList<>();
                    Exceptions.printStackTrace(ex);
                    if (ex instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        } else {
            mResultsWeatherObservation = new ArrayList<>();
        }

        return mResultsWeatherObservation;
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
        WEATHER_MEASUREPOINT("weather_measurepoint.xml"),
        WEATHER_OBSERVATION("weather_observation.xml");
        private final String mFilename;

        private Service(String filename) {
            mFilename = filename;
        }

        public String getFilename() {
            return mFilename;
        }
    }
}
