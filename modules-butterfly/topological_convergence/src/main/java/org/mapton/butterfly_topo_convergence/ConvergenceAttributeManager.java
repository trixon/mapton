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
package org.mapton.butterfly_topo_convergence;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.awt.Color;
import org.mapton.api.Mapton;
import org.mapton.butterfly_core.api.BaseAttributeManager;

/**
 *
 * @author Patrik Karlström
 */
public class ConvergenceAttributeManager extends BaseAttributeManager {

    private BasicShapeAttributes mGroundPathAttributes;
    private BasicShapeAttributes mIndicatorAttributes;
    private BasicShapeAttributes mNodeAttributes;
    private BasicShapeAttributes mPairPathAttributes;
    private PointPlacemarkAttributes mSinglePinAttributes;
    private BasicShapeAttributes mSurfaceAttributes;

    public static ConvergenceAttributeManager getInstance() {
        return Holder.INSTANCE;
    }

    private ConvergenceAttributeManager() {
    }

    public BasicShapeAttributes getGroundPathAttributes() {
        if (mGroundPathAttributes == null) {
            mGroundPathAttributes = new BasicShapeAttributes();
            mGroundPathAttributes.setDrawOutline(true);
            mGroundPathAttributes.setOutlineMaterial(Material.LIGHT_GRAY);
            mGroundPathAttributes.setEnableLighting(false);
            mGroundPathAttributes.setOutlineWidth(1);
        }

        return mGroundPathAttributes;
    }

    public BasicShapeAttributes getIndicatorAttributes() {
        if (mIndicatorAttributes == null) {
            mIndicatorAttributes = new BasicShapeAttributes();
            mIndicatorAttributes.setDrawOutline(false);
            mIndicatorAttributes.setInteriorMaterial(Material.RED);
            mIndicatorAttributes.setEnableLighting(true);
            mIndicatorAttributes.setInteriorOpacity(0.4);
        }

        return mIndicatorAttributes;
    }

    public BasicShapeAttributes getNodeAttributes() {
        if (mNodeAttributes == null) {
            mNodeAttributes = new BasicShapeAttributes();
            mNodeAttributes.setDrawOutline(false);
            mNodeAttributes.setInteriorMaterial(Material.ORANGE);
            mNodeAttributes.setEnableLighting(true);
        }

        return mNodeAttributes;
    }

    public BasicShapeAttributes getPairPathAttributes() {
        if (mPairPathAttributes == null) {
            mPairPathAttributes = new BasicShapeAttributes();
            mPairPathAttributes.setDrawOutline(true);
            mPairPathAttributes.setOutlineMaterial(Material.YELLOW);
            mPairPathAttributes.setEnableLighting(false);
            mPairPathAttributes.setOutlineWidth(2);
        }

        return mPairPathAttributes;
    }

    @Override
    public PointPlacemarkAttributes getPinAttributes(Color color) {
        if (mSinglePinAttributes == null) {
            mSinglePinAttributes = new PointPlacemarkAttributes(new PointPlacemark(Position.ZERO).getDefaultAttributes());
            mSinglePinAttributes.setImageAddress("https://maps.google.com/mapfiles/kml/shapes/polygon.png");
            mSinglePinAttributes.setImageColor(color);
            mSinglePinAttributes.setScale(Mapton.SCALE_PIN_IMAGE);
            mSinglePinAttributes.setLabelScale(Mapton.SCALE_PIN_LABEL);
            mSinglePinAttributes.setImageOffset(Offset.BOTTOM_CENTER);
        }

        return mSinglePinAttributes;
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

    private static class Holder {

        private static final ConvergenceAttributeManager INSTANCE = new ConvergenceAttributeManager();
    }
}
