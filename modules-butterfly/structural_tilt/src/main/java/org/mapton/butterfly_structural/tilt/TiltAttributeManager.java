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
package org.mapton.butterfly_structural.tilt;

import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import org.mapton.butterfly_core.api.BaseAttributeManager;
import org.mapton.butterfly_core.api.ButterflyHelper;
import org.mapton.butterfly_format.types.structural.BStructuralTiltPoint;

/**
 *
 * @author Patrik Karlström
 */
public class TiltAttributeManager extends BaseAttributeManager {

    private BasicShapeAttributes mBearingAttribute;

    private BasicShapeAttributes mComponentEllipsoidAttributes;
    private BasicShapeAttributes mComponentGroundPathAttributes;
    private BasicShapeAttributes mSurfaceAttributes;
    private BasicShapeAttributes[] mSymbolAttributes;
    private BasicShapeAttributes mTiltAttribute;

    public static TiltAttributeManager getInstance() {
        return Holder.INSTANCE;
    }

    private TiltAttributeManager() {
    }

    public BasicShapeAttributes getBearingAttribute() {
        if (mBearingAttribute == null) {
            mBearingAttribute = new BasicShapeAttributes();
            mBearingAttribute.setDrawOutline(true);
            mBearingAttribute.setOutlineMaterial(Material.CYAN);
            mBearingAttribute.setOutlineWidth(4.0);
            mBearingAttribute.setOutlineOpacity(1.0);
        }

        return mBearingAttribute;
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

    public BasicShapeAttributes getSymbolAttributes(BStructuralTiltPoint p) {
        if (mSymbolAttributes == null) {
            mSymbolAttributes = new BasicShapeAttributes[4];
            for (int i = 0; i < 4; i++) {
                var attrs = new BasicShapeAttributes();
                attrs.setInteriorMaterial(ButterflyHelper.getAlarmMaterial(i - 1));
                attrs.setEnableLighting(true);
                attrs.setDrawOutline(false);
                mSymbolAttributes[i] = attrs;
            }
        }

        var attrs = mSymbolAttributes[1];
//        var attrs = mSymbolAttributes[TopoHelper.getAlarmLevel(p) + 1];
//        if (mColorBy != null && mColorBy != ColorBy.ALARM) {
//            attrs = new BasicShapeAttributes(attrs);
//            attrs.setInteriorMaterial(new Material(getColor(p)));
//        }

        return attrs;
    }

    public BasicShapeAttributes getTiltAttribute() {
        if (mTiltAttribute == null) {
            mTiltAttribute = new BasicShapeAttributes();
            mTiltAttribute.setDrawOutline(true);
            mTiltAttribute.setOutlineMaterial(Material.RED);
            mTiltAttribute.setOutlineWidth(4.0);
            mTiltAttribute.setOutlineOpacity(1.0);
        }

        return mTiltAttribute;
    }

    private static class Holder {

        private static final TiltAttributeManager INSTANCE = new TiltAttributeManager();
    }
}
