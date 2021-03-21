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
package org.mapton.poi_trv_ti;

import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.application.Platform;
import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.mapton.api.MPoi;
import org.mapton.api.MPoiProvider;
import org.mapton.api.MPoiStyle;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;

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
        var pois = new ArrayList<MPoi>();
        addCameras(pois);
        addParking(pois);
        addTrafficSafetyCameras(pois);
        addWeatherStations(pois);

        return pois;
    }

    private void addCameras(ArrayList<MPoi> pois) {
        mManager.getCameraGroupToPhotoUrl().clear();
        mManager.getResultsCamera().forEach(result -> {
            for (var camera : result.getCamera()) {
                try {
                    if (!camera.isActive()) {
                        continue;
                    }
                    try {
                        mManager.getCameraGroupToPhotoUrl().put(camera.getCameraGroup(), camera.getPhotoUrl());
                    } catch (NullPointerException e) {
                        //System.out.println(ToStringBuilder.reflectionToString(camera, ToStringStyle.MULTI_LINE_STYLE));
                    }
                    var poi = new MPoi();
                    poi.setDescription(camera.getDescription());
                    poi.setCategory(String.format("%s", "Kameror"));
                    poi.setCategory(camera.getType());
                    poi.setColor("ff0000");
                    poi.setDisplayMarker(true);
                    poi.setName(camera.getName());
                    poi.setZoom(0.9);
                    poi.setExternalImageUrl(camera.getPhotoUrl() + "?type=fullsize");
                    setLatLonFromGeometry(poi, camera.getGeometry().getWGS84());

                    var style = new MPoiStyle();
                    poi.setStyle(style);
                    style.setImageUrl(getPlacemarkUrl(camera.getIconId()));
                    style.setLabelVisible(false);

                    pois.add(poi);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        });
    }

    private void addParking(ArrayList<MPoi> pois) {
        mManager.getResultsParking().forEach(result -> {
            for (var parking : result.getParking()) {
                try {
                    var poi = new MPoi();
                    poi.setDescription(parking.getDescription());
                    poi.setCategory(String.format("%s", "Parkering"));
                    poi.setColor("00ff00");
                    poi.setDisplayMarker(true);
                    poi.setName(parking.getName());
                    poi.setZoom(0.9);
                    setLatLonFromGeometry(poi, parking.getGeometry().getWGS84());

                    var style = new MPoiStyle();
                    poi.setStyle(style);
                    style.setImageUrl(getPlacemarkUrl(parking.getIconId()));
                    style.setLabelVisible(false);

                    pois.add(poi);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        });
    }

    private void addTrafficSafetyCameras(ArrayList<MPoi> pois) {
        mManager.getResultsTrafficSafetyCamera().forEach(result -> {
            for (var camera : result.getTrafficSafetyCamera()) {
                try {
                    var poi = new MPoi();
                    poi.setDescription(camera.getBearing().toString());
                    poi.setCategory(String.format("%s", "Trafiksäkerhetskameror"));
                    poi.setColor("00ff00");
                    poi.setDisplayMarker(true);
                    poi.setName(camera.getName());
                    poi.setZoom(0.9);
                    setLatLonFromGeometry(poi, camera.getGeometry().getWGS84());

                    var style = new MPoiStyle();
                    poi.setStyle(style);
                    style.setImageUrl(getPlacemarkUrl(camera.getIconId()));
                    style.setLabelVisible(false);

                    pois.add(poi);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        });
    }

    private void addWeatherStations(ArrayList<MPoi> pois) {
        mManager.getResultsWeatherStation().forEach(result -> {
            for (var weatherStation : result.getWeatherStation()) {
                try {
                    if ( //                        (weatherStation.getRoadNumberNumeric() != null && weatherStation.getRoadNumberNumeric() > 0)
                            //                        (weatherStation.isActive() != null && !weatherStation.isActive())
                            (weatherStation.isDeleted() != null && weatherStation.isDeleted())
                            || StringUtils.endsWith(weatherStation.getName(), " Fjärryta")) {
                        continue;
                    }

                    var poi = new MPoi();
                    poi.setCategory(String.format("%s", "Väderstation"));
                    poi.setDisplayMarker(true);
                    poi.setName(weatherStation.getName());
                    poi.setZoom(0.9);
                    poi.setPropertyNode(mWeatherView);
                    poi.setPropertySource(weatherStation);
                    setLatLonFromGeometry(poi, weatherStation.getGeometry().getWGS84());

                    var style = new MPoiStyle();
                    poi.setStyle(style);
                    final var measurement = weatherStation.getMeasurement();
                    if (weatherStation.isActive()) {
                        style.setLabelText(String.format("%.0f°", measurement.getAir().getTemp()));
                        style.setImageUrl(mManager.getIconUrl(measurement.getPrecipitation()));
                        try {
                            poi.setDescription(String.format("%s %s",
                                    measurement.getPrecipitation().getAmount(),
                                    measurement.getPrecipitation().getAmountName()
                            ));
                        } catch (Exception e) {
                        }
                    } else {
                        style.setLabelText("NODATA");
                        style.setImageUrl(String.format("%s%s.png", SystemHelper.getPackageAsPath(TrafficInfoPoiProvider.class), "precipitationNoData"));
                    }

                    style.setLabelScale(1.2);
                    style.setImageScale(FxHelper.getUIScaled(0.1));
                    style.setLabelVisible(true);
                    style.setImageLocation(MPoiStyle.ImageLocation.MIDDLE_CENTER);

                    pois.add(poi);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        });
    }

    private String getPlacemarkUrl(String iconId) {
        return String.format("https://api.trafikinfo.trafikverket.se/v2/icons/%s", iconId);
    }

    private void setLatLonFromGeometry(MPoi poi, String wkt) {
        try {
            var coordinate = mWktReader.read(wkt).getCoordinate();
            poi.setLatitude(coordinate.y);
            poi.setLongitude(coordinate.x);
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
