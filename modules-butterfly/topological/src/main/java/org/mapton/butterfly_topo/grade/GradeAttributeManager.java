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

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.awt.Color;
import org.mapton.butterfly_core.api.BaseAttributeManager;
import org.mapton.butterfly_core.api.ButterflyHelper;
import org.mapton.butterfly_format.types.topo.BTopoGrade;

/**
 *
 * @author Patrik Karlström
 */
public class GradeAttributeManager extends BaseAttributeManager {

    private BasicShapeAttributes mGroundCylinderAttributes;
    private BasicShapeAttributes mGroundPathAttributes;
    private BasicShapeAttributes mGradeHAttributes;
    private PointPlacemarkAttributes[] mPinAttributes;

    public static GradeAttributeManager getInstance() {
        return GradeAttributeManagerHolder.INSTANCE;
    }

    private GradeAttributeManager() {
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

    public BasicShapeAttributes getGradeHAttributes(BTopoGrade p) {
//        if (mGradeHAttributes == null) {
        mGradeHAttributes = new BasicShapeAttributes();
        mGradeHAttributes.setDrawOutline(true);
        mGradeHAttributes.setOutlineMaterial(Material.BLUE);
        mGradeHAttributes.setEnableLighting(false);
        mGradeHAttributes.setOutlineWidth(2);
//        }
        var material = Material.GREEN;
        var grade = Math.abs(p.ext().getDiff().getZPerMille());
        if (grade >= 1.0) {
            material = Material.RED;
        } else if (grade >= 0.5) {
            material = Material.YELLOW;
        }
        mGradeHAttributes.setOutlineMaterial(material);

        return mGradeHAttributes;
    }

    public PointPlacemarkAttributes getPinAttributes(BTopoGrade p) {
        if (mPinAttributes == null) {
            mPinAttributes = new PointPlacemarkAttributes[4];
            for (int i = 0; i < 4; i++) {
                var attrs = new PointPlacemarkAttributes(new PointPlacemark(Position.ZERO).getDefaultAttributes());
                attrs.setScale(0.75);
                attrs.setImageAddress("images/pushpins/plain-white.png");
                attrs.setImageColor(ButterflyHelper.getAlarmColorAwt(i - 1));
                attrs.setImageColor(Color.ORANGE);

                mPinAttributes[i] = attrs;
            }
        }

        var attrs = mPinAttributes[1];

//        if (mColorBy != null && mColorBy != ColorBy.ALARM) {
//            attrs = new PointPlacemarkAttributes(attrs);
//            attrs.setImageColor(getColor(mColorBy, p));
//        }
        return attrs;
    }

    private static class GradeAttributeManagerHolder {

        private static final GradeAttributeManager INSTANCE = new GradeAttributeManager();
    }
}
