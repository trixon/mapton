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
package org.mapton.butterfly_core.indicators;

import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;

/**
 *
 * @author Patrik Karlström
 */
public class IndicatorAttributeManager {

    private BasicShapeAttributes mScaleRulerAttributes;

    public static IndicatorAttributeManager getInstance() {
        return Holder.INSTANCE;
    }

    private IndicatorAttributeManager() {
    }

    public BasicShapeAttributes getScaleRulerAttribute(int i) {
        if (mScaleRulerAttributes == null) {
            mScaleRulerAttributes = new BasicShapeAttributes();
            mScaleRulerAttributes.setDrawInterior(true);
            mScaleRulerAttributes.setDrawOutline(false);
            mScaleRulerAttributes.setEnableLighting(true);
        }

        var attrs = new BasicShapeAttributes(mScaleRulerAttributes);
        attrs.setInteriorMaterial((i & 1) == 0 ? Material.WHITE : Material.RED);
        return attrs;
    }

    private static class Holder {

        private static final IndicatorAttributeManager INSTANCE = new IndicatorAttributeManager();
    }
}
