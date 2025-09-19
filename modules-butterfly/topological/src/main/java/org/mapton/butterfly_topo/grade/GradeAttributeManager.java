/*
 * Copyright 2024 Patrik Karlström.
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
package org.mapton.butterfly_topo.grade;

import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import org.mapton.butterfly_core.api.BaseAttributeManager;
import org.mapton.butterfly_format.types.BAxis;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.topo.BTopoGrade;
import org.mapton.butterfly_topo.TopoAttributeManager;
import org.mapton.butterfly_topo.TopoHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GradeAttributeManager extends BaseAttributeManager {

    private BasicShapeAttributes mComponentDistancePathAttributes;
    private BasicShapeAttributes mGroundCylinderAttributes;
    private BasicShapeAttributes mGroundPathAttributes;
    private PointPlacemarkAttributes[] mPinAttributes;

    public static GradeAttributeManager getInstance() {
        return GradeAttributeManagerHolder.INSTANCE;
    }

    private GradeAttributeManager() {
    }

    public BasicShapeAttributes getComponentDistancePathAttributes(BTopoGrade p) {
        if (mComponentDistancePathAttributes == null) {
            mComponentDistancePathAttributes = new BasicShapeAttributes();
            mComponentDistancePathAttributes.setDrawOutline(true);
            mComponentDistancePathAttributes.setOutlineMaterial(Material.LIGHT_GRAY);
            mComponentDistancePathAttributes.setEnableLighting(false);
            mComponentDistancePathAttributes.setOutlineWidth(1.5);
        }

        var attrs = new BasicShapeAttributes(mComponentDistancePathAttributes);
        attrs.setOutlineMaterial(TopoHelper.getGradeDistanceMaterial(p));
        if (p.ext().getDiff().getPartialDiffDistance() >= 0) {
            attrs.setOutlineStippleFactor(3);
        }

        return attrs;
    }

    public BasicShapeAttributes getGradeVectorAttributes(BTopoGrade p) {
        return TopoAttributeManager.getInstance().getComponentVectorAttributes(TopoHelper.getAlarmLevel(p));
    }

    public BasicShapeAttributes getGradeVectorCurrentAttributes(BTopoGrade p) {
        return TopoAttributeManager.getInstance().getComponentVectorCurrentAttributes(TopoHelper.getAlarmLevel(p));
    }

    public BasicShapeAttributes getGroundCylinderAttributes() {
        if (mGroundCylinderAttributes == null) {
            mGroundCylinderAttributes = new BasicShapeAttributes();
            mGroundCylinderAttributes.setDrawOutline(false);
            mGroundCylinderAttributes.setDrawInterior(true);
            mGroundCylinderAttributes.setInteriorMaterial(Material.PINK);
            mGroundCylinderAttributes.setEnableLighting(true);
            mGroundCylinderAttributes.setOutlineWidth(1);
        }

        return mGroundCylinderAttributes;
    }

    public BasicShapeAttributes getGroundPathAttributes() {
        if (mGroundPathAttributes == null) {
            mGroundPathAttributes = new BasicShapeAttributes();
            mGroundPathAttributes.setDrawOutline(true);
            mGroundPathAttributes.setOutlineMaterial(Material.PINK);
            mGroundPathAttributes.setEnableLighting(false);
            mGroundPathAttributes.setOutlineWidth(1);
        }

        return mGroundPathAttributes;
    }

    public PointPlacemarkAttributes getPinAttributes(BTopoGrade p, BComponent component) {
        PointPlacemarkAttributes attrs;
        if (p.getAxis() == BAxis.HORIZONTAL) {
            attrs = getPinAttributes(TopoHelper.getAlarmLevelHorizontal(p));
        } else {
            attrs = getPinAttributes(TopoHelper.getAlarmLevelVertical(p));
        }

//        if (mColorBy != null && mColorBy != ColorBy.ALARM) {
//            attrs = new PointPlacemarkAttributes(attrs);
//            attrs.setImageColor(getColor(p));
//        }
        return attrs;
    }

    private static class GradeAttributeManagerHolder {

        private static final GradeAttributeManager INSTANCE = new GradeAttributeManager();
    }
}
