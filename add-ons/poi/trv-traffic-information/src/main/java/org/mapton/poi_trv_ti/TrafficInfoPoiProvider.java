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
import java.util.ResourceBundle;
import javax.xml.bind.JAXBException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.mapton.api.MPoi;
import org.mapton.api.MPoiProvider;
import static org.mapton.poi_trv_ti.TrafficInformationManager.Service.*;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.trv_ti.TrafficInformation;
import se.trixon.trv_ti.road.trafficsafetycamera.v1.RESULT;
import se.trixon.trv_ti.road.trafficsafetycamera.v1.TrafficSafetyCamera;
import se.trixon.trv_ti.road.weatherstation.v1.WeatherStation;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MPoiProvider.class)
public class TrafficInfoPoiProvider implements MPoiProvider {

    private final ResourceBundle mBundle = NbBundle.getBundle(TrafficInfoPoiProvider.class);
    private final TrafficInformationManager mManager = TrafficInformationManager.getInstance();
    private final TrafficInformation mTrafficInformation = TrafficInformationManager.getInstance().getTrafficInformation();
    private final WKTReader mWktReader = new WKTReader();

    public TrafficInfoPoiProvider() {
    }

    @Override
    public String getName() {
        return mBundle.getString("name");
    }

    @Override
    public ArrayList<MPoi> getPois() {
        ArrayList<MPoi> pois = new ArrayList<>();
        addCameras(pois);
        addTrafficSafetyCameras(pois);
        addWeatherStations(pois);

        return pois;
    }

    private void addCameras(ArrayList<MPoi> pois) {
        File file = mManager.getFile(CAMERA);
        if (!file.exists()) {
            return;
        }

        try {
            for (se.trixon.trv_ti.road.camera.v1.RESULT result : mTrafficInformation.road().getCameraResults(file)) {
                result.getCamera().stream().map(camera -> {
//                    System.out.println(camera.getCameraGroup());
//                    System.out.println(camera.getContentType());
//                    System.out.println(camera.getDescription());
//                    System.out.println(camera.getIconId());
//                    System.out.println(camera.getId());
//                    System.out.println(camera.getLocation());
//                    System.out.println(camera.getStatus());
//                    System.out.println(camera.getType());
//                    System.out.println("----");
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
                    try {
                        Coordinate coordinate = mWktReader.read(camera.getGeometry().getWGS84()).getCoordinate();
                        poi.setLatitude(coordinate.y);
                        poi.setLongitude(coordinate.x);
                    } catch (ParseException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    return poi;
                }).forEachOrdered(poi -> {
                    pois.add(poi);
                });
            }
        } catch (IOException | InterruptedException | JAXBException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void addTrafficSafetyCameras(ArrayList<MPoi> pois) {
        File file = mManager.getFile(TRAFFIC_SAFETY_CAMERA);
        if (!file.exists()) {
            return;
        }

        try {
            for (RESULT result : mTrafficInformation.road().getTrafficSafetyCameraResults(file)) {
                for (TrafficSafetyCamera camera : result.getTrafficSafetyCamera()) {
                    MPoi poi = new MPoi();
                    poi.setDescription(camera.getBearing().toString());
                    poi.setCategory(String.format("%s", "Trafiksäkerhetskameror"));
                    poi.setColor("00ff00");
                    poi.setDisplayMarker(true);
                    poi.setName(camera.getName());
                    poi.setZoom(0.9);
                    poi.setPlacemarkImageUrl("https://www.transportstyrelsen.se/globalassets/global/vag/vagmarken/vagmarken-nedladdning/e24-1.png");
                    poi.setPlacemarkScale(0.1);
                    poi.setPlotLabel(false);
                    try {
                        Coordinate coordinate = mWktReader.read(camera.getGeometry().getWGS84()).getCoordinate();
                        poi.setLatitude(coordinate.y);
                        poi.setLongitude(coordinate.x);
                    } catch (ParseException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                    pois.add(poi);
                }
            }
        } catch (IOException | InterruptedException | JAXBException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void addWeatherStations(ArrayList<MPoi> pois) {
        File file = mManager.getFile(WEATHER_STATION);
        if (!file.exists()) {
            return;
        }

        try {
            for (se.trixon.trv_ti.road.weatherstation.v1.RESULT result : mTrafficInformation.road().getWeatherStationResults(file)) {
                for (WeatherStation weatherStation : result.getWeatherStation()) {
//                    System.out.println(weatherStation);
//                    System.out.println(weatherStation.getIconId());
//                    System.out.println(weatherStation.getId());
//                    System.out.println(weatherStation.getModifiedTime());
//                    System.out.println(weatherStation.getName());
//                    System.out.println(weatherStation.getRoadNumberNumeric());
//                    try {
//                        System.out.println(weatherStation.getMeasurement().getAir());
//                        System.out.println(weatherStation.getMeasurement().getAir().getTemp());
//                        System.out.println(weatherStation.getMeasurement().getAir().getTempIconId());
//
//                    } catch (Exception e) {
//                    }
//                    System.out.println(weatherStation.getMeasurement());
//                    System.out.println(weatherStation.getMeasurement());
//                    System.out.println(weatherStation.getMeasurement());
//                    System.out.println(weatherStation);
//                    System.out.println(weatherStation);
//                    System.out.println(weatherStation);
                    MPoi poi = new MPoi();
//                    weatherStation.getMeasurement().getRoad().
//                    poi.setDescription(weatherStation.getBearing().toString());
                    poi.setCategory(String.format("%s", "Väderstation"));
                    poi.setColor("ffff00");
                    poi.setDisplayMarker(true);
                    poi.setName(weatherStation.getName());
                    poi.setZoom(0.9);
//                    poi.setPlacemarkImageUrl("https://www.transportstyrelsen.se/globalassets/global/vag/vagmarken/vagmarken-nedladdning/e24-1.png");
//                    poi.setPlacemarkScale(0.1);
                    poi.setPlotLabel(false);
                    try {
                        Coordinate coordinate = mWktReader.read(weatherStation.getGeometry().getWGS84()).getCoordinate();
                        poi.setLatitude(coordinate.y);
                        poi.setLongitude(coordinate.x);
                    } catch (ParseException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                    pois.add(poi);
                }
            }
        } catch (IOException | InterruptedException | JAXBException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
