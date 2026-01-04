/*
 * Copyright 2026 Patrik Karlström.
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
package org.mapton.butterfly_rock_earthquake;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import org.mapton.api.Mapton;
import org.mapton.butterfly_core.api.BaseAttributeManager;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class QuakeAttributeManager extends BaseAttributeManager {

    private BasicShapeAttributes mComponentEllipsoidAttributes;
    private BasicShapeAttributes mComponentGroundPathAttributes;
    private PointPlacemarkAttributes mSinglePinAttributes;
    private BasicShapeAttributes mSurfaceAttributes;
    private AnnotationAttributes mAnnotationAttributes;

    public static QuakeAttributeManager getInstance() {
        return Holder.INSTANCE;
    }

    private QuakeAttributeManager() {
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
            mComponentGroundPathAttributes.setOutlineWidth(3);
        }

        return mComponentGroundPathAttributes;
    }

    public AnnotationAttributes getAnnotationAttributes() {
        if (mAnnotationAttributes == null) {
            var size = SwingHelper.getUIScaled(32);
            mAnnotationAttributes = new AnnotationAttributes();
            mAnnotationAttributes.setLeader(AVKey.SHAPE_NONE);
            mAnnotationAttributes.setDrawOffset(new Point(0, (int) (-size * .5)));
            mAnnotationAttributes.setSize(new Dimension(size, size));
            mAnnotationAttributes.setBorderWidth(0);
            mAnnotationAttributes.setCornerRadius(0);
            mAnnotationAttributes.setBackgroundColor(Color.BLACK);
        }

        return mAnnotationAttributes;
    }

    @Override
    public PointPlacemarkAttributes getPinAttributes(Color color) {
        if (mSinglePinAttributes == null) {
            mSinglePinAttributes = new PointPlacemarkAttributes(new PointPlacemark(Position.ZERO).getDefaultAttributes());
            mSinglePinAttributes.setImageAddress("https://maps.google.com/mapfiles/kml/shapes/caution.png");
            mSinglePinAttributes.setImageColor(color);
            mSinglePinAttributes.setScale(Mapton.getScalePinImage());
            mSinglePinAttributes.setLabelScale(Mapton.getScalePinLabel());
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

        private static final QuakeAttributeManager INSTANCE = new QuakeAttributeManager();
    }
}
