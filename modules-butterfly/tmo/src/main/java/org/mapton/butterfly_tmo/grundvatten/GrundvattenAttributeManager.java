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
package org.mapton.butterfly_tmo.grundvatten;

import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.awt.Color;
import org.mapton.butterfly_format.types.tmo.BGrundvatten;
import se.trixon.almond.util.GraphicsHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GrundvattenAttributeManager {

    private BasicShapeAttributes mComponentGroundPathAttributes;
    private PointPlacemarkAttributes mLabelPlacemarkAttributes;
    private PointPlacemarkAttributes mPinAttributes;
    private BasicShapeAttributes mSurfaceAttributes;
    private BasicShapeAttributes mTimeSeriesAttributes;

    public static GrundvattenAttributeManager getInstance() {
        return Holder.INSTANCE;
    }

    private GrundvattenAttributeManager() {
    }

    public BasicShapeAttributes getComponentGroundPathAttributes() {
        if (mComponentGroundPathAttributes == null) {
            mComponentGroundPathAttributes = new BasicShapeAttributes();
            mComponentGroundPathAttributes.setDrawOutline(true);
            mComponentGroundPathAttributes.setOutlineMaterial(Material.YELLOW);
            mComponentGroundPathAttributes.setEnableLighting(false);
            mComponentGroundPathAttributes.setOutlineWidth(1);
        }

        return mComponentGroundPathAttributes;
    }

    public PointPlacemarkAttributes getLabelPlacemarkAttributes() {
        if (mLabelPlacemarkAttributes == null) {
            mLabelPlacemarkAttributes = new PointPlacemarkAttributes();
            mLabelPlacemarkAttributes.setLabelScale(1.6);
            mLabelPlacemarkAttributes.setImageColor(GraphicsHelper.colorAddAlpha(Color.RED, 0));
            mLabelPlacemarkAttributes.setScale(0.75);
            mLabelPlacemarkAttributes.setImageAddress("images/pushpins/plain-white.png");
        }

        return mLabelPlacemarkAttributes;
    }

    public PointPlacemarkAttributes getPinAttributes(BGrundvatten grundvatten) {
        if (mPinAttributes == null) {
            mPinAttributes = new PointPlacemarkAttributes();
            mPinAttributes.setScale(0.75);
            mPinAttributes.setImageAddress("images/pushpins/plain-white.png");
            mPinAttributes.setImageColor(Color.BLUE.brighter());
        }

        var attrs = new PointPlacemarkAttributes(mPinAttributes);
        attrs.setImageColor(getColor(grundvatten));

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

    private Color getColor(BGrundvatten grundvatten) {
        switch (grundvatten.getGrundvattenmagasin()) {
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
                return Color.decode("#e7feff");
            }

            default -> {
                return Color.GRAY;
            }
        }

    }

    public BasicShapeAttributes getTimeSeriesAttributes(BGrundvatten grundvatten) {
        if (mTimeSeriesAttributes == null) {
            mTimeSeriesAttributes = new BasicShapeAttributes();
            mTimeSeriesAttributes.setDrawOutline(false);
            mTimeSeriesAttributes.setInteriorMaterial(Material.BLUE);
            mTimeSeriesAttributes.setEnableLighting(true);
        }

        var attrs = new BasicShapeAttributes(mTimeSeriesAttributes);
        attrs.setInteriorMaterial(new Material(getColor(grundvatten)));

        return attrs;
    }

    private static class Holder {

        private static final GrundvattenAttributeManager INSTANCE = new GrundvattenAttributeManager();
    }
}
