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
package org.mapton.butterfly_geo.inclinometer;

import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import org.mapton.butterfly_core.api.BaseAttributeManager;
import org.mapton.butterfly_format.types.geo.BGeoInclinometerPoint;

/**
 *
 * @author Patrik Karlström
 */
public class InclinoAttributeManager extends BaseAttributeManager {

    private BasicShapeAttributes mComponentEllipsoidAttributes;
    private BasicShapeAttributes mInclinoAttribute;
    private BasicShapeAttributes mSurfaceAttributes;

    public static InclinoAttributeManager getInstance() {
        return Holder.INSTANCE;
    }

    private InclinoAttributeManager() {
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

    public BasicShapeAttributes getInclinoAttribute() {
        if (mInclinoAttribute == null) {
            mInclinoAttribute = new BasicShapeAttributes();
            mInclinoAttribute.setDrawOutline(true);
            mInclinoAttribute.setOutlineMaterial(Material.RED);
            mInclinoAttribute.setOutlineWidth(4.0);
            mInclinoAttribute.setOutlineOpacity(1.0);
        }

        return mInclinoAttribute;
    }

    public PointPlacemarkAttributes getPinAttributes(BGeoInclinometerPoint p) {
        var attrs = getPinAttributes(InclinoHelper.getAlarmLevel(p));
//        attrs.setImageAddress("https://maps.google.com/mapfiles/kml/paddle/wht-circle.png");
//        PinPaddle.SQUARE.apply(attrs);
//        if (mColorBy != null && mColorBy != ColorBy.ALARM) {
//            attrs = new PointPlacemarkAttributes(attrs);
//            attrs.setImageColor(getColor(p));
//        }
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

    private static class Holder {

        private static final InclinoAttributeManager INSTANCE = new InclinoAttributeManager();
    }
}
