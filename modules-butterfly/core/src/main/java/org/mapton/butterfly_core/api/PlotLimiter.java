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
package org.mapton.butterfly_core.api;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Box;
import gov.nasa.worldwind.render.Material;
import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class PlotLimiter {

    private final Set<Object> mAllowList = new HashSet();
    private final Map<Object, Integer> mObjectCounter = new HashMap<>();
    private final Map<Object, Integer> mObjectLimits = new HashMap<>();
    private BasicShapeAttributes mSkipPlotAttribute;

    public void addToAllowList(String name) {
        mAllowList.add(name);
    }

    public int getItemCount(Object key) {
        return mObjectCounter.getOrDefault(key, 0);
    }

    public Box getPlotLimitIndicator(Position position) {
        var radii = 1.0;
        var altitude = radii * 2;
        var p = WWHelper.positionFromPosition(position, altitude);
        var angle = Angle.fromDegrees(45);

        var box = new Box(p, radii, radii, radii, angle, angle, angle);
        box.setAttributes(getSkipPlotAttribute());

        return box;
    }

    public BasicShapeAttributes getSkipPlotAttribute() {
        if (mSkipPlotAttribute == null) {
            mSkipPlotAttribute = new BasicShapeAttributes();
            mSkipPlotAttribute.setInteriorMaterial(new Material(Color.decode("#800080")));
            mSkipPlotAttribute.setEnableLighting(true);
            mSkipPlotAttribute.setDrawOutline(false);
        }

        return mSkipPlotAttribute;
    }

    public void incPlotCounter(Object key) {
        mObjectCounter.put(key, mObjectCounter.getOrDefault(key, 0) + 1);
    }

    public boolean isLimitReached(Object key, String allowListKey) {
        var count = getItemCount(key);
        boolean limitReached = count > mObjectLimits.getOrDefault(key, 0);
        if (limitReached && mAllowList.contains(allowListKey)) {
            limitReached = false;
        }

        return limitReached;
    }

    public void reset() {
        mObjectCounter.clear();
    }

    public void setLimit(Object key, int limit) {
        mObjectLimits.put(key, limit);
    }

    public void setSkipPlotAttribute(BasicShapeAttributes skipPlotAttribute) {
        this.mSkipPlotAttribute = skipPlotAttribute;
    }
}
