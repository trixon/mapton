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

/**
 *
 * @author Patrik Karlström
 */
public class TiltAttributeManager extends BaseAttributeManager {

    private BasicShapeAttributes mAxisAttributes;
    private BasicShapeAttributes mComponentEllipsoidAttributes;
    private BasicShapeAttributes mSurfaceAttributes;

    public static TiltAttributeManager getInstance() {
        return Holder.INSTANCE;
    }

    private TiltAttributeManager() {
    }

    public BasicShapeAttributes getAxisAttributes() {
        if (mAxisAttributes == null) {
            mAxisAttributes = new BasicShapeAttributes();
            mAxisAttributes.setDrawInterior(true);
            mAxisAttributes.setInteriorMaterial(Material.CYAN);
            mAxisAttributes.setDrawOutline(true);
            mAxisAttributes.setOutlineMaterial(Material.CYAN);
            mAxisAttributes.setOutlineWidth(1.0);
//            mAxisAttributes.setOutlineOpacity(1.0);
        }

        return mAxisAttributes;
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

        private static final TiltAttributeManager INSTANCE = new TiltAttributeManager();
    }
}
