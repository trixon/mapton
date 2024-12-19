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
package org.mapton.butterfly_core.api;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.awt.Color;
import org.mapton.api.Mapton;
import se.trixon.almond.util.GraphicsHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BaseAttributeManager {

    private BasicShapeAttributes[] mAlarmInteriorAttributes;
    private BasicShapeAttributes mAlarmLimitAttributes;
    private BasicShapeAttributes[] mAlarmOutlineAttributes;
    private BasicShapeAttributes mComponentGroundPathAttributes;
    private BasicShapeAttributes mComponentGroundPathEvenAttributes;
    private BasicShapeAttributes mComponentGroundPathOddAttributes;
    private BasicShapeAttributes[][] mComponentTrace1dAttributes;
    private BasicShapeAttributes mComponentZeroAttributes;
    private PointPlacemarkAttributes mLabelPlacemarkAttributes;
    private PointPlacemarkAttributes[] mPinAttributes;
    private PointPlacemarkAttributes mSinglePinAttributes;

    public BaseAttributeManager() {
    }

    public BasicShapeAttributes getAlarmInteriorAttributes(int alarmLevel) {
        if (mAlarmInteriorAttributes == null) {
            mAlarmInteriorAttributes = new BasicShapeAttributes[4];
            for (int i = 0; i < 4; i++) {
                var attrs = new BasicShapeAttributes();
                attrs.setInteriorMaterial(ButterflyHelper.getAlarmMaterial(i - 1));
                attrs.setEnableLighting(true);
                attrs.setDrawOutline(false);
                mAlarmInteriorAttributes[i] = attrs;
            }
        }

        return mAlarmInteriorAttributes[alarmLevel + 1];
    }

    public BasicShapeAttributes getAlarmLimit() {
        if (mAlarmLimitAttributes == null) {
            mAlarmLimitAttributes = new BasicShapeAttributes();
            mAlarmLimitAttributes.setDrawOutline(false);
            mAlarmLimitAttributes.setInteriorMaterial(Material.LIGHT_GRAY);
            mAlarmLimitAttributes.setEnableLighting(true);
            mAlarmLimitAttributes.setInteriorOpacity(0.5);
        }

        return mAlarmLimitAttributes;
    }

    public BasicShapeAttributes getAlarmOutlineAttributes(int alarmLevel) {
        if (mAlarmOutlineAttributes == null) {
            mAlarmOutlineAttributes = new BasicShapeAttributes[4];
            for (int i = 0; i < 4; i++) {
                var attrs = new BasicShapeAttributes();
                attrs.setOutlineMaterial(ButterflyHelper.getAlarmMaterial(i - 1));
                attrs.setDrawInterior(false);
                attrs.setOutlineWidth(2.0);
                attrs.setOutlineOpacity(1.0);
                mAlarmOutlineAttributes[i] = attrs;
            }
        }

        return mAlarmOutlineAttributes[alarmLevel + 1];
    }

    public BasicShapeAttributes getComponentGroundPathAttributes() {
        if (mComponentGroundPathAttributes == null) {
            mComponentGroundPathAttributes = new BasicShapeAttributes();
            mComponentGroundPathAttributes.setDrawOutline(true);
            mComponentGroundPathAttributes.setOutlineMaterial(Material.LIGHT_GRAY);
            mComponentGroundPathAttributes.setEnableLighting(false);
            mComponentGroundPathAttributes.setOutlineWidth(1.5);
        }

        return mComponentGroundPathAttributes;
    }

    public BasicShapeAttributes getComponentGroundPathEvenAttributes() {
        if (mComponentGroundPathEvenAttributes == null) {
            mComponentGroundPathEvenAttributes = new BasicShapeAttributes(getComponentGroundPathAttributes());
            mComponentGroundPathEvenAttributes.setOutlineMaterial(Material.RED);
            mComponentGroundPathEvenAttributes.setOutlineOpacity(0.5);
        }

        return mComponentGroundPathEvenAttributes;
    }

    public BasicShapeAttributes getComponentGroundPathOddAttributes() {
        if (mComponentGroundPathOddAttributes == null) {
            mComponentGroundPathOddAttributes = new BasicShapeAttributes(getComponentGroundPathAttributes());
            mComponentGroundPathOddAttributes.setOutlineMaterial(Material.WHITE);
            mComponentGroundPathOddAttributes.setOutlineOpacity(0.5);
        }

        return mComponentGroundPathOddAttributes;
    }

    public BasicShapeAttributes getComponentTrace1dAttributes(int alarmLevel, boolean rise, boolean maximus) {
        if (mComponentTrace1dAttributes == null) {
            mComponentTrace1dAttributes = new BasicShapeAttributes[5][2];

            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 2; j++) {
                    var attrs = new BasicShapeAttributes();
                    attrs.setDrawOutline(false);
                    Material material;
                    if (i < 4) {
                        material = ButterflyHelper.getAlarmMaterial(i - 1);
                    } else {
                        material = new Material(Color.decode("#800080"));
                    }
                    attrs.setInteriorMaterial(material);
                    attrs.setEnableLighting(true);

                    if (j == 1) {
                        attrs.setDrawOutline(true);
                        if (i < 4) {
                            attrs.setOutlineMaterial(Material.LIGHT_GRAY);
                        } else {
                            attrs.setOutlineMaterial(Material.YELLOW);
                        }
                    }

                    mComponentTrace1dAttributes[i][j] = attrs;
                }
            }
        }

        int offset = 1;
        if (maximus) {
            offset++;
        }
        var i = alarmLevel + offset;
        var j = rise ? 1 : 0;

        return mComponentTrace1dAttributes[i][j];
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

    public PointPlacemarkAttributes getLabelPlacemarkAttributes() {
        if (mLabelPlacemarkAttributes == null) {
            mLabelPlacemarkAttributes = new PointPlacemarkAttributes(new PointPlacemark(Position.ZERO).getDefaultAttributes());
            mLabelPlacemarkAttributes.setImageAddress("images/pushpins/plain-white.png");
            mLabelPlacemarkAttributes.setImageColor(GraphicsHelper.colorAddAlpha(Color.RED, 0));
            mLabelPlacemarkAttributes.setScale(Mapton.SCALE_PIN_IMAGE);
            mLabelPlacemarkAttributes.setLabelScale(Mapton.SCALE_PIN_LABEL);
        }

        return mLabelPlacemarkAttributes;
    }

    public PointPlacemarkAttributes getPinAttributes(Color color) {
        if (mSinglePinAttributes == null) {
            mSinglePinAttributes = new PointPlacemarkAttributes(new PointPlacemark(Position.ZERO).getDefaultAttributes());
            mSinglePinAttributes.setImageAddress("images/pushpins/plain-white.png");
            mSinglePinAttributes.setImageColor(color);
            mSinglePinAttributes.setScale(Mapton.SCALE_PIN_IMAGE);
            mSinglePinAttributes.setLabelScale(Mapton.SCALE_PIN_LABEL);
        }

        return mSinglePinAttributes;
    }

    public PointPlacemarkAttributes getPinAttributes(int alarmLevel) {
        if (mPinAttributes == null) {
            mPinAttributes = new PointPlacemarkAttributes[4];
            for (int i = 0; i < 4; i++) {
                var attrs = new PointPlacemarkAttributes(new PointPlacemark(Position.ZERO).getDefaultAttributes());
                attrs.setScale(Mapton.SCALE_PIN_IMAGE);
                attrs.setImageAddress("images/pushpins/plain-white.png");
                attrs.setImageColor(ButterflyHelper.getAlarmColorAwt(i - 1));
                attrs.setLabelScale(Mapton.SCALE_PIN_LABEL);

                mPinAttributes[i] = attrs;
            }
        }

        return mPinAttributes[alarmLevel + 1];
    }

}
