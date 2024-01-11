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
package org.mapton.butterfly_core.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.geotools.api.geometry.MismatchedDimensionException;
import org.geotools.api.referencing.operation.TransformException;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.mapton.api.MArea;
import org.mapton.api.MAreaFilterManager;
import org.mapton.api.MCooTrans;
import org.mapton.api.MOptions;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.ButterflyLoader;
import org.mapton.butterfly_format.types.BBaseControlPoint;
import org.mapton.butterfly_format.types.tmo.BBasObjekt;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ButterflyManager {

    private final MAreaFilterManager mAreaFilterManager = MAreaFilterManager.getInstance();
    private final ObjectProperty<Butterfly> mButterflyProperty = new SimpleObjectProperty<>();
    private Date mFileDate;
    private final WKTReader mWktReader = new WKTReader();

    public static ButterflyManager getInstance() {
        return Holder.INSTANCE;
    }

    private ButterflyManager() {
    }

    public ObjectProperty<Butterfly> butterflyProperty() {
        return mButterflyProperty;
    }

    public void calculateLatLons(ArrayList<? extends BBaseControlPoint> baseControlPoints) {
        for (var cp : baseControlPoints) {
            var x = cp.getZeroX();
            var y = cp.getZeroY();

            if (ObjectUtils.allNotNull(x, y)) {
                var wgs84 = getCooTrans().toWgs84(y, x);
                cp.setLat(MathHelper.round(wgs84.getY(), 6));
                cp.setLon(MathHelper.round(wgs84.getX(), 6));
            }
        }
    }

    public Butterfly getButterfly() {
        return mButterflyProperty.get();
    }

    public Date getFileDate() {
        return mFileDate;
    }

    public void load() {
        var butterflyLoader = ButterflyLoader.getInstance();
        var sourceDir = new File(FileUtils.getTempDirectory(), "butterfly");
        ButterflyLoader.setSourceDir(sourceDir);

        if (!sourceDir.isDirectory()) {
            System.err.println("Not a dir: " + sourceDir);
            //TODO Infobox
            return;
        }

        var coosysPlane = ButterflyConfig.getInstance().getConfig().getString("COOSYS.PLANE");

        if (coosysPlane != null) {
            var preferences = NbPreferences.forModule(MCooTrans.class);
            preferences.put("map.coo_trans", coosysPlane);
            //TODO request restart id changed
        }

        if (!butterflyLoader.load()) {
            return;
        }

        mFileDate = butterflyLoader.getDate(sourceDir);
        var butterfly = butterflyLoader.getButterfly();

        calculateLatLons(butterfly.hydro().getGroundwaterPoints());
        calculateLatLons(butterfly.topo().getControlPoints());

        calculateLatLonsTmo(butterfly.tmo().getGrundvatten());
        calculateLatLonsTmo(butterfly.tmo().getInfiltration());
        calculateLatLonsTmo(butterfly.tmo().getRorelse());
        calculateLatLonsTmo(butterfly.tmo().getTunnelvatten());
        calculateLatLonsTmo(butterfly.tmo().getVaderstation());
        calculateLatLonsTmo(butterfly.tmo().getVattenkemi());

        var areas = new ArrayList<MArea>();
        var prefix = "Haga/";
        butterfly.getAreaFilters().stream().forEachOrdered(areaFilter -> {
            var area = new MArea(areaFilter.getName());
            area.setName(areaFilter.getName());
            area.setWktGeometry(areaFilter.getWkt());
            areas.add(area);
        });

        mAreaFilterManager.clearByPrefix(prefix);
        mAreaFilterManager.addAll(areas);

        for (var area : butterfly.getAreaActivities()) {
            try {
                var geometry = mWktReader.read(area.getWkt());
                area.setGeometry(geometry);

                try {
                    var targetGeometry = MOptions.getInstance().getMapCooTrans().transform(geometry);
                    area.setTargetGeometry(targetGeometry);
                } catch (MismatchedDimensionException | TransformException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        setButterfly(butterfly);
        ButterflyHelper.refreshTitle();
    }

    public void setButterfly(Butterfly butterfly) {
        mButterflyProperty.set(butterfly);
    }

    private void calculateLatLonsTmo(ArrayList<? extends BBasObjekt> baseControlPoints) {
        for (var cp : baseControlPoints) {
            var x = cp.getX();
            var y = cp.getY();

            if (ObjectUtils.allNotNull(x, y)) {
                var wgs84 = getCooTrans().toWgs84(y, x);
                cp.setLat(MathHelper.round(wgs84.getY(), 6));
                cp.setLon(MathHelper.round(wgs84.getX(), 6));
            }
        }
    }

    private MCooTrans getCooTrans() {
        return MCooTrans.getCooTrans(MOptions.getInstance().getMapCooTransName());

    }

    private static class Holder {

        private static final ButterflyManager INSTANCE = new ButterflyManager();
    }
}
