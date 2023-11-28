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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.mapton.api.MArea;
import org.mapton.api.MAreaFilterManager;
import org.mapton.api.MCooTrans;
import org.mapton.api.MOptions;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.BBaseControlPoint;
import org.mapton.butterfly_format.types.tmo.BBasObjekt;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class ButterflyManager {

    private final ObjectProperty<Butterfly> mButterflyProperty = new SimpleObjectProperty<>();
    private final MAreaFilterManager mAreaFilterManager = MAreaFilterManager.getInstance();
    private final WKTReader mWktReader = new WKTReader();

    public static ButterflyManager getInstance() {
        return Holder.INSTANCE;
    }

    private ButterflyManager() {
    }

    public ObjectProperty<Butterfly> butterflyProperty() {
        return mButterflyProperty;
    }

    public Butterfly getButterfly() {
        return mButterflyProperty.get();
    }

    public void load() {
        var wrappedManager = org.mapton.butterfly_format.ButterflyManager.getInstance();

        wrappedManager.load(new File(FileUtils.getTempDirectory(), "butterfly"));
        var butterfly = wrappedManager.getButterfly();
        calculateLatLons(butterfly.hydro().getGroundwaterPoints());
        calculateLatLons(butterfly.topo().getControlPoints());

        calculateLatLonsTmo(butterfly.tmo().getGrundvatten());
        calculateLatLonsTmo(butterfly.tmo().getInfiltration());
        calculateLatLonsTmo(butterfly.tmo().getRörelse());
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
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }

        }

        setButterfly(butterfly);
    }

    public void setButterfly(Butterfly butterfly) {
        mButterflyProperty.set(butterfly);
    }

    private void calculateLatLons(ArrayList<? extends BBaseControlPoint> baseControlPoints) {
        for (var cp : baseControlPoints) {
            var x = cp.getZeroX();
            var y = cp.getZeroY();

            if (ObjectUtils.allNotNull(x, y)) {
                var wgs84 = getCooTrans().toWgs84(y, x);
                cp.setLat(wgs84.getY());
                cp.setLon(wgs84.getX());
            }
        }
    }

    private void calculateLatLonsTmo(ArrayList<? extends BBasObjekt> baseControlPoints) {
        for (var cp : baseControlPoints) {
            var x = cp.getX();
            var y = cp.getY();

            if (ObjectUtils.allNotNull(x, y)) {
                var wgs84 = getCooTrans().toWgs84(y, x);
                cp.setLat(wgs84.getY());
                cp.setLon(wgs84.getX());
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
