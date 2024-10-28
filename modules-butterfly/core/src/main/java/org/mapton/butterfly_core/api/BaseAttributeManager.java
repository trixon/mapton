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
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.awt.Color;
import org.mapton.butterfly_format.types.BXyzPoint;
import se.trixon.almond.util.GraphicsHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BaseAttributeManager {

    private PointPlacemarkAttributes mLabelPlacemarkAttributes;
    private PointPlacemarkAttributes[] mPinAttributes;
    private PointPlacemarkAttributes mSinglePinAttributes;
    private BasicShapeAttributes[] mSymbolAttributes;

    public BaseAttributeManager() {
    }

    public PointPlacemarkAttributes getLabelPlacemarkAttributes() {
        if (mLabelPlacemarkAttributes == null) {
            mLabelPlacemarkAttributes = new PointPlacemarkAttributes(new PointPlacemark(Position.ZERO).getDefaultAttributes());
            mLabelPlacemarkAttributes.setLabelScale(1.6);
            mLabelPlacemarkAttributes.setImageColor(GraphicsHelper.colorAddAlpha(Color.RED, 0));
            mLabelPlacemarkAttributes.setScale(0.75);
            mLabelPlacemarkAttributes.setImageAddress("images/pushpins/plain-white.png");
        }

        return mLabelPlacemarkAttributes;
    }

    public PointPlacemarkAttributes getPinAttributes(Color color) {
        if (mSinglePinAttributes == null) {
            mSinglePinAttributes = new PointPlacemarkAttributes(new PointPlacemark(Position.ZERO).getDefaultAttributes());
            mSinglePinAttributes.setScale(0.75);
            mSinglePinAttributes.setImageAddress("images/pushpins/plain-white.png");
            mSinglePinAttributes.setImageColor(color);
        }

        return mSinglePinAttributes;
    }

    public PointPlacemarkAttributes getPinAttributes(BXyzPoint p, int alarmLevel) {
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

        return mPinAttributes[alarmLevel + 1];
    }

    public BasicShapeAttributes getSymbolAttributes(BXyzPoint p, int alarmLevel) {
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

        var attrs = mSymbolAttributes[alarmLevel + 1];

        return attrs;
    }

}
