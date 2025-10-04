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

import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.application.Platform;
import org.apache.commons.lang3.Strings;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.mapton.api.MPoi;
import org.mapton.api.MPoiProvider;
import org.mapton.api.MPoiStyle;
import org.mapton.api.MSimpleObjectStorageManager;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.swing.SwingHelper;

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
        addWeatherMeasurepoints(pois);

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
                    var urlExtraArg = "";
                    if (MSimpleObjectStorageManager.getInstance().getBoolean(ImageSizeSOSB.class, ImageSizeSOSB.DEFAULT_VALUE)) {
                        urlExtraArg = "?type=fullsize";
                    }

                    var poi = new MPoi();
                    poi.setDescription(camera.getDescription());
                    poi.setCategory("%s".formatted("Kameror"));
                    poi.setCategory(camera.getType());
                    poi.setColor("ff0000");
                    poi.setDisplayMarker(true);
                    poi.setName(camera.getName());
                    poi.setZoom(0.9);
                    poi.setExternalImageUrl(camera.getPhotoUrl() + urlExtraArg);
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
                    poi.setCategory("%s".formatted("Parkering"));
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
                    poi.setCategory("%s".formatted("Trafiksäkerhetskameror"));
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

    private void addWeatherMeasurepoints(ArrayList<MPoi> pois) {
        mManager.getResultsWeatherMeasurepoint().forEach(result -> {
            for (var weatherStation : result.getWeatherMeasurepoint()) {
                if (weatherStation.getGeometry() == null) {
                    continue;
                }
                try {
                    if ( //                        (weatherStation.getRoadNumberNumeric() != null && weatherStation.getRoadNumberNumeric() > 0)
                            //                        (weatherStation.isActive() != null && !weatherStation.isActive())
                            (weatherStation.isDeleted() != null && weatherStation.isDeleted())
                            || Strings.CS.endsWith(weatherStation.getName(), " Fjärryta")) {
                        continue;
                    }

                    var poi = new MPoi();
                    poi.setCategory("%s".formatted("Väderstation"));
                    poi.setDisplayMarker(true);
                    poi.setName(weatherStation.getName());
                    poi.setZoom(0.9);
                    poi.setPropertyNode(mWeatherView);
                    poi.setPropertySource(weatherStation);
                    setLatLonFromGeometry(poi, weatherStation.getGeometry().getWGS84());

                    var style = new MPoiStyle();
                    poi.setStyle(style);
                    var observation = weatherStation.getObservation();
                    if (!weatherStation.isDeleted()) {
                        var air = observation.getAir();
                        if (air != null) {

                            var temperature = air.getTemperature().getValue().getValue();
                            style.setLabelText("%.0f°".formatted(temperature));
                            var precipitation = observation.getAggregated30Minutes().getPrecipitation();
                            style.setImageUrl(mManager.getIconUrl(precipitation));
                            try {
                                poi.setDescription("%.1f %s".formatted(
                                        2 * precipitation.getTotalWaterEquivalent().getValue().getValue().doubleValue(),
                                        WeatherHelper.getPrecipType(precipitation)
                                ));
                            } catch (Exception e) {
                            }
                        }
                    } else {
                        style.setLabelText("NODATA");
                        style.setImageUrl("%s%s.png".formatted(SystemHelper.getPackageAsPath(TrafficInfoPoiProvider.class), "precipitationNoData"));
                    }

                    style.setLabelScale(SwingHelper.getUIScaled(1.2));
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
        return "https://api.trafikinfo.trafikverket.se/v2/icons/%s".formatted(iconId);
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
