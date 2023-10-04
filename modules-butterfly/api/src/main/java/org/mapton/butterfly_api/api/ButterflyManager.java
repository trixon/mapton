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
package org.mapton.butterfly_api.api;

import java.io.File;
import java.util.ArrayList;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.api.MCooTrans;
import org.mapton.api.MOptions;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.controlpoint.BBaseControlPoint;

/**
 *
 * @author Patrik Karlström
 */
public class ButterflyManager {

    private final ObjectProperty<Butterfly> mButterflyProperty = new SimpleObjectProperty<>();

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
        calculateLatLons(butterfly.getHydroControlPoints());
        calculateLatLons(butterfly.getTopoControlPoints());

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

    private MCooTrans getCooTrans() {
        return MCooTrans.getCooTrans(MOptions.getInstance().getMapCooTransName());

    }

    private static class Holder {

        private static final ButterflyManager INSTANCE = new ButterflyManager();
    }
}