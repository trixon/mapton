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

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.Renderable;
import java.util.ArrayList;
import org.mapton.butterfly_format.types.BBase;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BaseGraphicRenderer<T extends Enum<T>, U extends BBase> {

    public static final double PERCENTAGE_ALTITUDE = 50.0;
    public static final double PERCENTAGE_SIZE = 1.5;

    private final BaseAttributeManager mAttributeManager = new BaseAttributeManager() {
    };
    private final RenderableLayer mInteractiveLayer;
    private final RenderableLayer mPassiveLayer;
    private final PlotLimiter mPlotLimiter;

    public BaseGraphicRenderer(RenderableLayer interactiveLayer, RenderableLayer passiveLayer, PlotLimiter plotLimiter) {
        mInteractiveLayer = interactiveLayer;
        mPassiveLayer = passiveLayer;
        mPlotLimiter = plotLimiter;
    }

    public void addRenderable(Renderable renderable, boolean interactiveLayer, Object plotLimiterKey, ArrayList<AVListImpl> mapObjects) {
        if (interactiveLayer) {
            mInteractiveLayer.addRenderable(renderable);
            if (renderable instanceof AVListImpl avlist && mapObjects != null) {
                mapObjects.add(avlist);
            }
        } else if (mPassiveLayer != null) {
            mPassiveLayer.addRenderable(renderable);
        }

        mPlotLimiter.incPlotCounter(plotLimiterKey);
    }

    public void addToAllowList(Object item) {
        mPlotLimiter.addToAllowList(item);
    }

    public PlotLimiter getPlotLimiter() {
        return mPlotLimiter;
    }

    public void plotPercentageRod(Position position, Integer percent) {
        if (percent == null) {
            percent = 0;
        }

        var pos = WWHelper.positionFromPosition(position, PERCENTAGE_ALTITUDE * Math.max(percent, 100) / 100.0);
        var groundPath = new Path(WWHelper.positionFromPosition(position, 0.0), pos);
        groundPath.setAttributes(mAttributeManager.getComponentGroundPathAttributes());
        addRenderable(groundPath, true, null, null);

        var pos100 = WWHelper.positionFromPosition(position, PERCENTAGE_ALTITUDE);
        var cylinder = new Cylinder(pos100, 0.25, PERCENTAGE_SIZE * 2);
        cylinder.setAttributes(mAttributeManager.getComponentZeroAttributes());
        addRenderable(cylinder, true, null, null);
    }

    public void resetPlotLimiter() {
        mPlotLimiter.reset();
    }

    protected boolean isPlotLimitReached(U p, Object key, Position position, boolean emptyList, ArrayList<AVListImpl> mapObjects) {
        if (mPlotLimiter.isLimitReached(key, p)) {
            addRenderable(mPlotLimiter.createPlotLimitIndicator(position, emptyList), true, null, mapObjects);
            return true;
        } else {
            return false;
        }
    }
}
