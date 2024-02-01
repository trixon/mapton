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
package org.mapton.butterfly_topo.pair;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.awt.Color;
import org.mapton.butterfly_core.api.ButterflyHelper;
import org.mapton.butterfly_format.types.topo.BTopoPointPair;
import se.trixon.almond.util.GraphicsHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GradeAttributeManager {

    private PointPlacemarkAttributes mLabelPlacemarkAttributes;
    private PointPlacemarkAttributes[] mPinAttributes;

    private GradeAttributeManager() {
    }

    public static GradeAttributeManager getInstance() {
        return GradeAttributeManagerHolder.INSTANCE;
    }

    public PointPlacemarkAttributes getPinAttributes(BTopoPointPair p) {
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

    private static class GradeAttributeManagerHolder {

        private static final GradeAttributeManager INSTANCE = new GradeAttributeManager();
    }
}
