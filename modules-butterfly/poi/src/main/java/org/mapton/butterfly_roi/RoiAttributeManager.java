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
package org.mapton.butterfly_roi;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.awt.Color;
import org.mapton.api.Mapton;
import org.mapton.butterfly_core.api.BaseAttributeManager;
import org.mapton.butterfly_format.types.BRoi;

/**
 *
 * @author Patrik Karlström
 */
public class RoiAttributeManager extends BaseAttributeManager {

    private BasicShapeAttributes mComponentEllipsoidAttributes;
    private BasicShapeAttributes mComponentGroundPathAttributes;
    private BasicShapeAttributes mShapeAttributes;
    private BasicShapeAttributes mShapeHighlightAttributes;
    private PointPlacemarkAttributes mSinglePinAttributes;
    private BasicShapeAttributes mSurfaceAttributes;

    public static RoiAttributeManager getInstance() {
        return Holder.INSTANCE;
    }

    private RoiAttributeManager() {
    }

    public BasicShapeAttributes getComponentEllipsoidAttributes() {
        if (mComponentEllipsoidAttributes == null) {
            mComponentEllipsoidAttributes = new BasicShapeAttributes();
            mComponentEllipsoidAttributes.setDrawOutline(false);
            mComponentEllipsoidAttributes.setInteriorMaterial(Material.ORANGE);
            mComponentEllipsoidAttributes.setEnableLighting(true);
        }

        return mComponentEllipsoidAttributes;
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

    @Override
    public PointPlacemarkAttributes getPinAttributes(Color color) {
        if (mSinglePinAttributes == null) {
            mSinglePinAttributes = new PointPlacemarkAttributes(new PointPlacemark(Position.ZERO).getDefaultAttributes());
            mSinglePinAttributes.setImageAddress("https://maps.google.com/mapfiles/kml/pal2/icon17.png");
            mSinglePinAttributes.setImageColor(color);
            mSinglePinAttributes.setScale(Mapton.getScalePinImage() * 2.5);
            mSinglePinAttributes.setLabelScale(Mapton.getScalePinLabel());
            mSinglePinAttributes.setImageOffset(Offset.BOTTOM_CENTER);
        }

        return mSinglePinAttributes;
    }

    public BasicShapeAttributes getSurfaceAttributes(BRoi roi) {
        if (mShapeAttributes == null) {
            mShapeAttributes = new BasicShapeAttributes();
            mShapeAttributes.setOutlineWidth(3.0);
            mShapeAttributes.setDrawInterior(true);
            mShapeAttributes.setDrawOutline(true);
            mShapeAttributes.setInteriorOpacity(0.1);
            mShapeAttributes.setInteriorMaterial(Material.ORANGE);
            mShapeAttributes.setOutlineMaterial(Material.YELLOW);
        }

        return mShapeAttributes;
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

    public BasicShapeAttributes getSurfaceHighlightAttributes(BRoi a) {
        if (mShapeHighlightAttributes == null) {
            mShapeHighlightAttributes = new BasicShapeAttributes(getSurfaceAttributes(a));
            mShapeHighlightAttributes.setInteriorOpacity(0.20);
            mShapeHighlightAttributes.setOutlineOpacity(0.20);

        }

        return mShapeHighlightAttributes;
    }

    private static class Holder {

        private static final RoiAttributeManager INSTANCE = new RoiAttributeManager();
    }
}
