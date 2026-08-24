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
package org.mapton.butterfly_composite;

import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import org.mapton.butterfly_core.api.BaseAttributeManager;
import org.mapton.butterfly_format.types.composite.BCompositePoint;

/**
 *
 * @author Patrik Karlström
 */
public class CompositeAttributeManager extends BaseAttributeManager {

    private BasicShapeAttributes mComponentEllipsoidAttributes;
    private BasicShapeAttributes mLoadAttribute;
    private BasicShapeAttributes mSurfaceAttributes;

    public static CompositeAttributeManager getInstance() {
        return Holder.INSTANCE;
    }

    private CompositeAttributeManager() {
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

    public PointPlacemarkAttributes getPinAttributes(BCompositePoint p) {
        var attrs = getPinAttributes(CompositeHelper.getAlarmLevel(p));

//        if (mColorBy != null && mColorBy != ColorBy.ALARM) {
//            attrs = new PointPlacemarkAttributes(attrs);
//            attrs.setImageColor(getColor(p));
//        }
        return attrs;
    }

    public BasicShapeAttributes getLoadAttribute() {
        if (mLoadAttribute == null) {
            mLoadAttribute = new BasicShapeAttributes();
            mLoadAttribute.setDrawOutline(true);
            mLoadAttribute.setOutlineMaterial(Material.RED);
            mLoadAttribute.setOutlineWidth(4.0);
            mLoadAttribute.setOutlineOpacity(1.0);
        }

        return mLoadAttribute;
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

        private static final CompositeAttributeManager INSTANCE = new CompositeAttributeManager();
    }
}
