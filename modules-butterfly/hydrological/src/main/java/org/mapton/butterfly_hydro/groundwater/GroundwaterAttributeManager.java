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
package org.mapton.butterfly_hydro.groundwater;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.awt.Color;
import java.util.HashMap;
import org.mapton.api.Mapton;
import org.mapton.butterfly_core.api.BaseAttributeManager;
import org.mapton.butterfly_format.types.hydro.BHydroGroundwaterPoint;

/**
 *
 * @author Patrik Karlström
 */
public class GroundwaterAttributeManager extends BaseAttributeManager {

    private BasicShapeAttributes mComponentGroundPathAttributes;
    private final HashMap<String, BasicShapeAttributes> mMagasinToAttributes = new HashMap<>();
    private PointPlacemarkAttributes mPinAttributes;
    private BasicShapeAttributes mSurfaceAttributes;
    private BasicShapeAttributes mTimeSeriesAttributes;

    public static GroundwaterAttributeManager getInstance() {
        return Holder.INSTANCE;
    }

    private GroundwaterAttributeManager() {
    }

//    public BasicShapeAttributes getComponentGroundPathAttributes() {
//        if (mComponentGroundPathAttributes == null) {
//            mComponentGroundPathAttributes = new BasicShapeAttributes();
//            mComponentGroundPathAttributes.setDrawOutline(true);
//            mComponentGroundPathAttributes.setOutlineMaterial(Material.YELLOW);
//            mComponentGroundPathAttributes.setEnableLighting(false);
//            mComponentGroundPathAttributes.setOutlineWidth(1);
//        }
//
//        return mComponentGroundPathAttributes;
//    }
    public PointPlacemarkAttributes getPinAttributes(BHydroGroundwaterPoint p) {
        if (mPinAttributes == null) {
            mPinAttributes = new PointPlacemarkAttributes(new PointPlacemark(Position.ZERO).getDefaultAttributes());
            mPinAttributes.setImageAddress("images/pushpins/plain-white.png");
            mPinAttributes.setImageColor(Color.BLUE.brighter());
            mPinAttributes.setScale(Mapton.SCALE_PIN_IMAGE);
            mPinAttributes.setLabelScale(Mapton.SCALE_PIN_LABEL);
        }

        var attrs = new PointPlacemarkAttributes(mPinAttributes);
        attrs.setImageColor(getColor(p));

        return attrs;
    }

    public BasicShapeAttributes getSurfaceAttributes() {
        if (mSurfaceAttributes == null) {
            mSurfaceAttributes = new BasicShapeAttributes();
            mSurfaceAttributes.setDrawOutline(false);
            mSurfaceAttributes.setDrawInterior(true);
            mSurfaceAttributes.setInteriorMaterial(Material.RED);
            mSurfaceAttributes.setEnableLighting(false);
        }

        return mSurfaceAttributes;
    }

    public BasicShapeAttributes getTimeSeriesAttributes(BHydroGroundwaterPoint p) {
        if (mTimeSeriesAttributes == null) {
            mTimeSeriesAttributes = new BasicShapeAttributes();
            mTimeSeriesAttributes.setDrawOutline(false);
            mTimeSeriesAttributes.setInteriorMaterial(new Material(Color.BLUE.brighter()));
            mTimeSeriesAttributes.setEnableLighting(true);
        }

//        var attrs = mMagasinToAttributes.computeIfAbsent(p.getCategory(), k -> {
//            var a = new BasicShapeAttributes(mTimeSeriesAttributes);
//            a.setInteriorMaterial(new Material(getColor(p)));
//
//            return a;
//        });
        return mTimeSeriesAttributes;
    }

    private Color getColor(BHydroGroundwaterPoint grundvatten) {
        switch (grundvatten.getCategory()) {
            case "Övre" -> {
                return Color.BLUE;
            }
            case "Undre" -> {
                return Color.BLUE.brighter();
            }
            case "Mellan" -> {
                return Color.decode("#87cefa");
            }
            case "Sjönivå" -> {
                return Color.decode("#000080");
            }
            case "Portryck" -> {
                return Color.decode("#126180");
            }
            case "Osäkert" -> {
                return Color.decode("#ff0000");
            }
            case "Energibrunn" -> {
                return Color.decode("#0fc0fc");
            }
            case "Berg" -> {
                return Color.decode("#e7feff").darker();
            }

            default -> {
                return Color.GRAY;
            }
        }

    }

    private static class Holder {

        private static final GroundwaterAttributeManager INSTANCE = new GroundwaterAttributeManager();
    }
}
