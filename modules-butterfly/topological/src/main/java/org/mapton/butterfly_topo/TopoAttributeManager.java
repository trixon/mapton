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
package org.mapton.butterfly_topo;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;

/**
 *
 * @author Patrik Karlström
 */
public class TopoAttributeManager {

    private BasicShapeAttributes[] mIndicatorNeedAttributes;
    private final BasicShapeAttributes mSymbolAttributes = new BasicShapeAttributes();
    private PointPlacemarkAttributes mLabelPlacemarkAttributes;
    private PointPlacemarkAttributes mPinAttributes;

    private TopoAttributeManager() {
        initAttributes();
    }

    public static TopoAttributeManager getInstance() {
        return Holder.INSTANCE;
    }

    public BasicShapeAttributes[] getIndicatorNeedAttributes() {
        return mIndicatorNeedAttributes;
    }

    public PointPlacemarkAttributes getLabelPlacemarkAttributes() {
        return mLabelPlacemarkAttributes;
    }

    public PointPlacemarkAttributes getPinAttributes() {
        return mPinAttributes;
    }

    public BasicShapeAttributes getSymbolAttributes() {
        return mSymbolAttributes;
    }

    private void initAttributes() {
        //***
        var indicatorNeed = new BasicShapeAttributes();
        var indicatorNeed0 = new BasicShapeAttributes(indicatorNeed);
        var indicatorNeed1 = new BasicShapeAttributes(indicatorNeed);
        var indicatorNeed2 = new BasicShapeAttributes(indicatorNeed);
        indicatorNeed0.setInteriorMaterial(Material.GREEN);
        indicatorNeed1.setInteriorMaterial(Material.ORANGE);
        indicatorNeed2.setInteriorMaterial(Material.RED);

        mIndicatorNeedAttributes = new BasicShapeAttributes[]{
            indicatorNeed0,
            indicatorNeed1,
            indicatorNeed2
        };

        //***
        mLabelPlacemarkAttributes = new PointPlacemarkAttributes();
        mLabelPlacemarkAttributes.setLabelScale(1.6);
        mLabelPlacemarkAttributes.setDrawImage(false);

        //***
        mSymbolAttributes.setEnableLighting(true);
        mSymbolAttributes.setDrawOutline(false);

        //***
        mPinAttributes = new PointPlacemarkAttributes(new PointPlacemark(Position.ZERO).getDefaultAttributes());
        mPinAttributes.setScale(0.75);
        mPinAttributes.setImageAddress("images/pushpins/plain-white.png");

    }

    private static class Holder {

        private static final TopoAttributeManager INSTANCE = new TopoAttributeManager();
    }
}
