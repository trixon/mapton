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
package org.mapton.addon.seasonal;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.CappedCylinder;
import gov.nasa.worldwind.render.airspaces.Orbit;
import gov.nasa.worldwind.render.airspaces.Polygon;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.swing.Timer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.action.Action;
import org.mapton.api.MKey;
import org.mapton.api.MLatLon;
import org.mapton.api.MOptions;
import org.mapton.api.MSimpleObjectStorageManager;
import org.mapton.api.Mapton;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.LayerBundleManager;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class SeasonalLayerBundle extends LayerBundle {

    private final int STARTUP_DELAY = 5;
    private final RenderableLayer mCandleLayer = new RenderableLayer();
    private final RenderableLayer mLayer = new RenderableLayer();

    public SeasonalLayerBundle() {
        mLayer.setPickEnabled(false);
        setVisibleInLayerManager(mLayer, false);

        mCandleLayer.setPickEnabled(false);
        setVisibleInLayerManager(mCandleLayer, false);

        init();
        initListeners();
    }

    @Override
    public void populate() {
        getLayers().addAll(mLayer, mCandleLayer);
        setPopulated(true);
        mLayer.setEnabled(true);
    }

    private void checkForRefresh() {
        String[] fettisdagar = {"20200225", "20210216", "20220201", "20230221", "20240224", "20250304"};
        var halloween = "1031";

        final String today = new SimpleDateFormat("yyyyMMdd").format(new Date(System.currentTimeMillis()));
        if (ArrayUtils.contains(fettisdagar, today)) {
            refresh(false, getMardiGrasRunnable());
        } else if (true || StringUtils.endsWith(today, halloween)) {
            refresh(true, getHalloweenRunnable());
        }
    }

    private Runnable getHalloweenRunnable() {
        return () -> {
            mLayer.addRenderable(new SurfaceImage("pumpkin.png", Sector.FULL_SPHERE));
            Mapton.notification(MKey.NOTIFICATION_FX_WARNING, "Trick or treat!", "Give me some Jazz", (Action) null);
        };
    }

    private Runnable getMardiGrasRunnable() {
        return () -> {
            final Material topBreadMaterial = new Material(Color.decode("#712616").darker());
            final Material bottomBreadMaterial = new Material(Color.decode("#712616"));
            final Material creamMaterial = new Material(Color.decode("#fffdd0"));
            final Material sugarMaterial = new Material(Color.decode("#f5f5f5"));

            AirspaceAttributes topBreadAttrs = new BasicAirspaceAttributes(topBreadMaterial, 0.95);
            AirspaceAttributes botttomBreadAttrs = new BasicAirspaceAttributes(bottomBreadMaterial, 0.95);
            AirspaceAttributes creamAttrs = new BasicAirspaceAttributes(creamMaterial, 0.95);
            AirspaceAttributes sugarAttrs = new BasicAirspaceAttributes(sugarMaterial, 1.0);

            CappedCylinder topCylinder = new CappedCylinder(topBreadAttrs);
            topCylinder.setCenter(LatLon.fromDegrees(90.0, 0.0));
            topCylinder.setRadii(0.0, 700 * 10000.0);
            final double lowAlt = 10 * 10000.0;
            final double highAlt = 50 * 10000.0;
            topCylinder.setAltitudes(lowAlt, highAlt);
            topCylinder.setTerrainConforming(false, false);
            topCylinder.setDragEnabled(false);

            CappedCylinder bottomCylinder = new CappedCylinder(botttomBreadAttrs);
            bottomCylinder.setCenter(LatLon.fromDegrees(-90.0, 0.0));
            bottomCylinder.setRadii(0.0, 11000 * 1000.0);
            bottomCylinder.setAltitudes(lowAlt, highAlt);
            bottomCylinder.setTerrainConforming(false, false);
            bottomCylinder.setDragEnabled(false);

            Orbit creamOrbit = new Orbit(creamAttrs);
            creamOrbit.setLocations(LatLon.fromDegrees(18, 180), LatLon.fromDegrees(18, 90));
            creamOrbit.setAltitudes(lowAlt, highAlt * 1.5);
            creamOrbit.setWidth(450 * 10000.0);
            creamOrbit.setOrbitType(Orbit.OrbitType.CENTER);
            creamOrbit.setTerrainConforming(false, false);
            creamOrbit.setDragEnabled(false);

            mLayer.addRenderable(topCylinder);
            mLayer.addRenderable(bottomCylinder);

            double baseLat = 18.0;
            double width = 12.0;

            int step = 60;
            for (int i = -180; i < 180; i = i + step) {
                Polygon creamPolygon = new Polygon(creamAttrs);
                double startLon = i;
                double stopLon = i + step;
                creamPolygon.setLocations(Arrays.asList(
                        LatLon.fromDegrees(baseLat - width, startLon),
                        LatLon.fromDegrees(baseLat - width, stopLon),
                        LatLon.fromDegrees(baseLat + width, stopLon),
                        LatLon.fromDegrees(baseLat + width, startLon)
                ));
                creamPolygon.setAltitudes(lowAlt, highAlt * 1.5);
                creamPolygon.setTerrainConforming(true, true);
                mLayer.addRenderable(creamPolygon);
            }

            for (int i = 0; i < 2000; i++) {
                double lat = RandomUtils.nextDouble(50, 90);
                double lon = RandomUtils.nextDouble(0, 360) - 180;

                CappedCylinder sugarCylinder = new CappedCylinder(sugarAttrs);
                sugarCylinder.setCenter(LatLon.fromDegrees(lat, lon));
                sugarCylinder.setRadii(0.0, 7 * 10000.0 * RandomUtils.nextDouble(0.3, 1.2));
                sugarCylinder.setAltitudes(highAlt, highAlt * RandomUtils.nextDouble(1, 1.01));
                sugarCylinder.setTerrainConforming(false, false);
                sugarCylinder.setDragEnabled(false);
                mLayer.addRenderable(sugarCylinder);
            }
        };
    }

    private void init() {
        LocalDateTime[] startTimes = {
            LocalDateTime.parse("2020-10-01T00:00:00"),
            //LocalDateTime.parse("2020-11-29T00:00:00"),
            LocalDateTime.parse("2020-12-06T00:00:00"),
            LocalDateTime.parse("2020-12-13T00:00:00"),
            LocalDateTime.parse("2020-12-20T00:00:00")
        };

        double[] lats;
        double[] lons;
        double[][] ll = Mapton.getGlobalState().get("org.mapton.addon.seasonal.candle");
        if (ll != null) {
            lats = ll[0];
            lons = ll[1];
        } else {
            var mapHome = MOptions.getInstance().getMapHome();
            var lat = mapHome.getLatitude();
            var lon = mapHome.getLongitude();
            var dist = 0.000675;
            lats = new double[]{lat, lat, lat, lat};
            lons = new double[]{lon - 1.5 * dist, lon - 0.5 * dist, lon + 0.5 * dist, lon + 1.5 * dist};
        }

        var candles = new Candle[4];
        for (int i = 0; i < candles.length; i++) {
            candles[i] = new Candle(
                    new MLatLon(lats[i], lons[i]),
                    startTimes[i],
                    120.0,
                    5.0
            );
        }

        var timer = new Timer(700, event -> {
            mCandleLayer.removeAllRenderables();
            for (var candle : candles) {
                for (var renderable : candle.getRenderables()) {
                    mCandleLayer.addRenderable(renderable);
                }
            }
            LayerBundleManager.getInstance().redraw();
        });

        timer.start();
    }

    private void initListeners() {
        Mapton.getExecutionFlow().executeWhenReady(MKey.EXECUTION_FLOW_MAP_WW_INITIALIZED, () -> {
            checkForRefresh();
        });

        MSimpleObjectStorageManager.getInstance().addListener(pcl -> {
            checkForRefresh();
        }, SeasonalSOSB.class);
    }

    private void refresh(boolean hollow, Runnable r) {
        mLayer.removeAllRenderables();
        if (MSimpleObjectStorageManager.getInstance().getBoolean(SeasonalSOSB.class, true)) {
            new Thread(() -> {
                try {
                    TimeUnit.SECONDS.sleep(STARTUP_DELAY);
                    r.run();
                    if (hollow) {
                        var node = NbPreferences.root().node("org/mapton/me/worldwind");
                        node.put("map_style_prev", node.get("map_style", "se.trixon.hollow"));
                        node.put("map_style", "se.trixon.hollow");
                    }
                    Mapton.getEngine().panTo(new MLatLon(0, 0), 0.05);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }).start();
        }
    }
}
