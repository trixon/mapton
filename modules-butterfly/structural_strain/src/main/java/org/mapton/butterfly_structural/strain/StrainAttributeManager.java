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
package org.mapton.butterfly_structural.strain;

import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import org.mapton.butterfly_core.api.BaseAttributeManager;
import org.mapton.butterfly_format.types.structural.BStructuralStrainGaugePoint;

/**
 *
 * @author Patrik Karlström
 */
public class StrainAttributeManager extends BaseAttributeManager {

    private BasicShapeAttributes mComponentEllipsoidAttributes;
    private BasicShapeAttributes mStrainAttribute;
    private BasicShapeAttributes mSurfaceAttributes;

    public static StrainAttributeManager getInstance() {
        return Holder.INSTANCE;
    }

    private StrainAttributeManager() {
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

    public PointPlacemarkAttributes getPinAttributes(BStructuralStrainGaugePoint p) {
        var attrs = getPinAttributes(p, StrainHelper.getAlarmLevel(p));

//        if (mColorBy != null && mColorBy != ColorBy.ALARM) {
//            attrs = new PointPlacemarkAttributes(attrs);
//            attrs.setImageColor(getColor(p));
//        }
        return attrs;
    }

    public BasicShapeAttributes getStrainAttribute() {
        if (mStrainAttribute == null) {
            mStrainAttribute = new BasicShapeAttributes();
            mStrainAttribute.setDrawOutline(true);
            mStrainAttribute.setOutlineMaterial(Material.RED);
            mStrainAttribute.setOutlineWidth(4.0);
            mStrainAttribute.setOutlineOpacity(1.0);
        }

        return mStrainAttribute;
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

    public BasicShapeAttributes getSymbolAttributes(BStructuralStrainGaugePoint p) {
        var attrs = getSymbolAttributes(p, StrainHelper.getAlarmLevel(p));
//        if (mColorBy != null && mColorBy != ColorBy.ALARM) {
//            attrs = new BasicShapeAttributes(attrs);
//            attrs.setInteriorMaterial(new Material(getColor(p)));
//        }

        return attrs;
    }

    private static class Holder {

        private static final StrainAttributeManager INSTANCE = new StrainAttributeManager();
    }
}
