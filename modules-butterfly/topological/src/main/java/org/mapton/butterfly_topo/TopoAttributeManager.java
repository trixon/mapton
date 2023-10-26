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
import org.mapton.butterfly_api.api.ButterflyHelper;
import org.mapton.butterfly_format.types.controlpoint.BTopoControlPoint;
import org.mapton.butterfly_topo.shared.ColorBy;

/**
 *
 * @author Patrik Karlström
 */
public class TopoAttributeManager {

    private BasicShapeAttributes[] mBearingAttributes;
    private ColorBy mColorBy;
    private BasicShapeAttributes mComponentGroundPathAttributes;
    private BasicShapeAttributes[] mComponentVector12dAttributes;
    private BasicShapeAttributes[] mComponentVector3dAttributes;
    private BasicShapeAttributes[] mComponentVectorCurrentAttributes;
    private BasicShapeAttributes mComponentZeroAttributes;
    private BasicShapeAttributes mIndicatorConnectorAttributes;
    private BasicShapeAttributes[] mIndicatorNeedAttributes;
    private PointPlacemarkAttributes mLabelPlacemarkAttributes;
    private PointPlacemarkAttributes[] mPinAttributes;
    private BasicShapeAttributes[] mSymbolAttributes;

    public static TopoAttributeManager getInstance() {
        return Holder.INSTANCE;
    }

    private TopoAttributeManager() {
        initAttributes();
    }

    public BasicShapeAttributes getBearingAttribute(boolean first) {
        if (mBearingAttributes == null) {
            mBearingAttributes = new BasicShapeAttributes[2];
            for (int i = 0; i < 2; i++) {
                var attrs = new BasicShapeAttributes();
                attrs.setDrawOutline(true);
                attrs.setOutlineWidth(4);
                attrs.setOutlineMaterial(Material.MAGENTA);
                attrs.setOutlineWidth(i == 0 ? 2.0 : 4.0);
                attrs.setOutlineOpacity(i == 0 ? 1.0 : 0.05);
                mBearingAttributes[i] = attrs;
            }
        }

        return mBearingAttributes[first ? 0 : 1];
    }

    public ColorBy getColorBy() {
        return mColorBy;
    }

    public BasicShapeAttributes getComponentGroundPathAttributes() {
        if (mComponentGroundPathAttributes == null) {
            mComponentGroundPathAttributes = new BasicShapeAttributes();
            mComponentGroundPathAttributes.setDrawOutline(true);
            mComponentGroundPathAttributes.setOutlineMaterial(Material.LIGHT_GRAY);
            mComponentGroundPathAttributes.setEnableLighting(false);
            mComponentGroundPathAttributes.setOutlineWidth(1);
        }

        return mComponentGroundPathAttributes;
    }

    public BasicShapeAttributes getComponentVector1dAttributes(BTopoControlPoint p) {
        return mComponentVector12dAttributes[TopoHelper.getAlarmLevelHeight(p) + 1];
    }

    public BasicShapeAttributes getComponentVector2dAttributes(BTopoControlPoint p) {
        return mComponentVector12dAttributes[TopoHelper.getAlarmLevelPlane(p) + 1];
    }

    public BasicShapeAttributes getComponentVector3dAttributes(BTopoControlPoint p) {
        if (mComponentVector3dAttributes == null) {
            mComponentVector3dAttributes = new BasicShapeAttributes[4];
            for (int i = 0; i < 4; i++) {
                var attrs = new BasicShapeAttributes();
                attrs.setDrawOutline(true);
                attrs.setOutlineMaterial(ButterflyHelper.getAlarmMaterial(i - 1));
                attrs.setEnableLighting(false);
                attrs.setOutlineWidth(4);
                mComponentVector3dAttributes[i] = attrs;
            }
        }

        return mComponentVector3dAttributes[TopoHelper.getAlarmLevel(p) + 1];
    }

    public BasicShapeAttributes getComponentVectorCurrentAttributes(BTopoControlPoint p) {
        if (mComponentVectorCurrentAttributes == null) {
            mComponentVectorCurrentAttributes = new BasicShapeAttributes[4];
            for (int i = 0; i < 4; i++) {
                var attrs = new BasicShapeAttributes();
                attrs.setDrawOutline(false);
                attrs.setInteriorMaterial(ButterflyHelper.getAlarmMaterial(i - 1));
                attrs.setEnableLighting(true);
                mComponentVectorCurrentAttributes[i] = attrs;
            }
        }

        return mComponentVectorCurrentAttributes[TopoHelper.getAlarmLevel(p) + 1];
    }

    public BasicShapeAttributes getComponentZeroAttributes() {
        if (mComponentZeroAttributes == null) {
            mComponentZeroAttributes = new BasicShapeAttributes();
            mComponentZeroAttributes.setDrawOutline(false);
            mComponentZeroAttributes.setInteriorMaterial(Material.LIGHT_GRAY);
            mComponentZeroAttributes.setEnableLighting(true);
        }

        return mComponentZeroAttributes;
    }

    public BasicShapeAttributes getIndicatorConnectorAttribute() {
        if (mIndicatorConnectorAttributes == null) {
            mIndicatorConnectorAttributes = new BasicShapeAttributes();
            mIndicatorConnectorAttributes.setOutlineMaterial(Material.DARK_GRAY);
            mIndicatorConnectorAttributes.setOutlineWidth(2.0);
        }

        return mIndicatorConnectorAttributes;
    }

    public BasicShapeAttributes[] getIndicatorNeedAttributes() {
        if (mIndicatorNeedAttributes == null) {
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

        }
        return mIndicatorNeedAttributes;
    }

    public PointPlacemarkAttributes getLabelPlacemarkAttributes() {
        if (mLabelPlacemarkAttributes == null) {
            mLabelPlacemarkAttributes = new PointPlacemarkAttributes();
            mLabelPlacemarkAttributes.setLabelScale(1.6);
            mLabelPlacemarkAttributes.setDrawImage(false);
        }

        return mLabelPlacemarkAttributes;
    }

    public PointPlacemarkAttributes getPinAttributes(BTopoControlPoint p) {
        if (mPinAttributes == null) {
            mPinAttributes = new PointPlacemarkAttributes[4];
            for (int i = 0; i < 4; i++) {
                var attrs = new PointPlacemarkAttributes(new PointPlacemark(Position.ZERO).getDefaultAttributes());
                attrs.setScale(0.75);
                attrs.setImageAddress("images/pushpins/plain-white.png");
                attrs.setImageColor(ButterflyHelper.getAlarmColorAwt(i - 1));

                mPinAttributes[i] = attrs;
            }
        }

        return mPinAttributes[TopoHelper.getAlarmLevelHeight(p) + 1];
    }

    public BasicShapeAttributes getSymbolAttributes(BTopoControlPoint p) {
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

        return mSymbolAttributes[TopoHelper.getAlarmLevelHeight(p) + 1];
    }

    public void setColorBy(ColorBy colorBy) {
        mColorBy = colorBy;
    }

    private void initAttributes() {
        mComponentVector12dAttributes = new BasicShapeAttributes[4];
        for (int i = 0; i < 4; i++) {
            var attrs = new BasicShapeAttributes();
            attrs.setDrawOutline(true);
            attrs.setOutlineWidth(4);
            attrs.setOutlineMaterial(ButterflyHelper.getAlarmMaterial(i - 1));
            attrs.setOutlineOpacity(0.4);
            mComponentVector12dAttributes[i] = attrs;
        }
    }

    private static class Holder {

        private static final TopoAttributeManager INSTANCE = new TopoAttributeManager();
    }
}
