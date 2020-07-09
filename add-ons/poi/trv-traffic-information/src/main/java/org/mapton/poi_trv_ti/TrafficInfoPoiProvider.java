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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;
import javafx.application.Platform;
import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.mapton.api.MPoi;
import org.mapton.api.MPoiProvider;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;
import se.trixon.trv_traffic_information.road.camera.v1.Camera;
import se.trixon.trv_traffic_information.road.parking.v1_4.Parking;
import se.trixon.trv_traffic_information.road.trafficsafetycamera.v1.TrafficSafetyCamera;
import se.trixon.trv_traffic_information.road.weatherstation.v1.Measurement;
import se.trixon.trv_traffic_information.road.weatherstation.v1.WeatherStation;
import se.trixon.trv_traffic_information.road.weatherstation.v1.Wind;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MPoiProvider.class)
public class TrafficInfoPoiProvider implements MPoiProvider {

    private final ResourceBundle mBundle = NbBundle.getBundle(TrafficInfoPoiProvider.class);
    private final TrafficInformationManager mManager = TrafficInformationManager.getInstance();
    private WeatherView mWeatherView;
    private final WKTReader mWktReader = new WKTReader();

    public TrafficInfoPoiProvider() {
        Platform.runLater(() -> {
            mWeatherView = new WeatherView();
        });
    }

    @Override
    public String getName() {
        return mBundle.getString("name");
    }

    @Override
    public ArrayList<MPoi> getPois() {
        ArrayList<MPoi> pois = new ArrayList<>();
        addCameras(pois);
        addParking(pois);
        addTrafficSafetyCameras(pois);
        addWeatherStations(pois);

        return pois;
    }

    private void addCameras(ArrayList<MPoi> pois) {
        mManager.getResultsCamera().forEach(result -> {
            for (Camera camera : result.getCamera()) {
                MPoi poi = new MPoi();
                poi.setDescription(camera.getDescription());
                poi.setCategory(String.format("%s", "Kameror"));
                poi.setCategory(camera.getType());
                poi.setColor("ff0000");
                poi.setDisplayMarker(true);
                poi.setName(camera.getName());
                poi.setZoom(0.9);
                poi.setExternalImageUrl(camera.getPhotoUrl() + "?type=fullsize");
                poi.setPlotLabel(false);
                poi.setPlacemarkImageUrl(getPlacemarkUrl(camera.getIconId()));
                setLatLonFromGeometry(poi, camera.getGeometry().getWGS84());

                pois.add(poi);
            }
        });
    }

    private void addParking(ArrayList<MPoi> pois) {
        mManager.getResultsParking().forEach(result -> {
            for (Parking parking : result.getParking()) {
                MPoi poi = new MPoi();
                poi.setDescription(parking.getDescription());
                poi.setCategory(String.format("%s", "Parkering"));
                poi.setColor("00ff00");
                poi.setDisplayMarker(true);
                poi.setName(parking.getName());
                poi.setZoom(0.9);
                poi.setPlacemarkImageUrl(getPlacemarkUrl(parking.getIconId()));
                poi.setPlotLabel(false);
                setLatLonFromGeometry(poi, parking.getGeometry().getWGS84());

                pois.add(poi);
            }
        });
    }

    private void addTrafficSafetyCameras(ArrayList<MPoi> pois) {
        mManager.getResultsTrafficSafetyCamera().forEach(result -> {
            for (TrafficSafetyCamera camera : result.getTrafficSafetyCamera()) {
                MPoi poi = new MPoi();
                poi.setDescription(camera.getBearing().toString());
                poi.setCategory(String.format("%s", "Trafiksäkerhetskameror"));
                poi.setColor("00ff00");
                poi.setDisplayMarker(true);
                poi.setName(camera.getName());
                poi.setZoom(0.9);
                poi.setPlacemarkImageUrl(getPlacemarkUrl(camera.getIconId()));
                poi.setPlotLabel(false);
                setLatLonFromGeometry(poi, camera.getGeometry().getWGS84());

                pois.add(poi);
            }
        });
    }

    private void addWeatherStations(ArrayList<MPoi> pois) {
        mManager.getResultsWeatherStation().forEach(result -> {
            for (WeatherStation weatherStation : result.getWeatherStation()) {
                if ( //                        (weatherStation.getRoadNumberNumeric() != null && weatherStation.getRoadNumberNumeric() > 0)
                        (weatherStation.isActive() != null && !weatherStation.isActive())
                        || (weatherStation.isDeleted() != null && weatherStation.isDeleted())
                        || StringUtils.endsWith(weatherStation.getName(), " Fjärryta")) {
                    continue;
                }

                MPoi poi = new MPoi();
                poi.setCategory(String.format("%s", "Väderstation"));
                poi.setDisplayMarker(true);
                poi.setName(weatherStation.getName());
                poi.setZoom(0.9);
                poi.setPlacemarkImageUrl(getPlacemarkUrl(weatherStation.getIconId()));
                poi.setPlotLabel(false);
                poi.setPropertyNode(mWeatherView);
                poi.setPropertySource(weatherStation);
                setLatLonFromGeometry(poi, weatherStation.getGeometry().getWGS84());

                LinkedHashMap<String, Object> propertyMap = new LinkedHashMap<>();
                propertyMap.put(Dict.NAME.toString(), weatherStation.getName());
                propertyMap.put("Road", weatherStation.getRoadNumberNumeric());
                propertyMap.put("Modified", weatherStation.getModifiedTime());
                propertyMap.put("Active", weatherStation.isActive());
                propertyMap.put("Deleted", weatherStation.isDeleted());
                final Measurement measurement = weatherStation.getMeasurement();
                if (measurement != null) {
                    propertyMap.put("Meas", measurement.getMeasureTime());
                    propertyMap.put("Road", measurement.getRoad().getTemp());
                    propertyMap.put("Air", measurement.getAir().getTemp());
                    propertyMap.put("Air Humid", measurement.getAir().getRelativeHumidity());
                    propertyMap.put("Precept type", measurement.getPrecipitation().getType());
                    propertyMap.put("Precept amount", measurement.getPrecipitation().getAmount());
                    propertyMap.put("Precept amount name", measurement.getPrecipitation().getAmountName());
                    propertyMap.put("Precept type", measurement.getPrecipitation().getType());
                    final Wind wind = measurement.getWind();
                    if (wind != null) {

                        propertyMap.put("Wind direction", wind.getDirection());
                        propertyMap.put("Wind direction icon", wind.getDirectionIconId());
                        propertyMap.put("Wind direction text", wind.getDirectionText());
                        propertyMap.put("Wind direction force", wind.getForce());
                        propertyMap.put("Wind direction force max", wind.getForceMax());
                    }
                }
                poi.setPropertyMap(propertyMap);

                pois.add(poi);
            }
        });
    }

    private String getPlacemarkUrl(String iconId) {
        return String.format("https://api.trafikinfo.trafikverket.se/v2/icons/%s", iconId);
    }

    private void setLatLonFromGeometry(MPoi poi, String wkt) {
        try {
            Coordinate coordinate = mWktReader.read(wkt).getCoordinate();
            poi.setLatitude(coordinate.y);
            poi.setLongitude(coordinate.x);
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
