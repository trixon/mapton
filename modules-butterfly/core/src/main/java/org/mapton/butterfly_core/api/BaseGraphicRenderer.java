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

    protected static ArrayList<AVListImpl> sMapObjects;
    protected static PlotLimiter sPlotLimiter = new PlotLimiter();
    private final BaseAttributeManager mAttributeManager = new BaseAttributeManager() {
    };
    private final RenderableLayer mInteractiveLayer;
    private final RenderableLayer mPassiveLayer;

    public BaseGraphicRenderer(RenderableLayer interactiveLayer, RenderableLayer passiveLayer) {
        mInteractiveLayer = interactiveLayer;
        mPassiveLayer = passiveLayer;
    }

    public void addRenderable(Renderable renderable, boolean interactiveLayer) {
        if (interactiveLayer) {
            mInteractiveLayer.addRenderable(renderable);
            if (renderable instanceof AVListImpl avlist) {
                sMapObjects.add(avlist);
            }
        } else if (mPassiveLayer != null) {
            mPassiveLayer.addRenderable(renderable);
        }
    }

    public void plotPercentageRod(Position position, Integer percent) {
        if (percent == null) {
            percent = 0;
        }

        var pos = WWHelper.positionFromPosition(position, PERCENTAGE_ALTITUDE * Math.max(percent, 100) / 100.0);
        var groundPath = new Path(WWHelper.positionFromPosition(position, 0.0), pos);
        groundPath.setAttributes(mAttributeManager.getComponentGroundPathAttributes());
        addRenderable(groundPath, true);

        var pos100 = WWHelper.positionFromPosition(position, PERCENTAGE_ALTITUDE);
        var cylinder = new Cylinder(pos100, 0.25, PERCENTAGE_SIZE * 2);
        cylinder.setAttributes(mAttributeManager.getComponentZeroAttributes());
        addRenderable(cylinder, true);
    }

    protected boolean isPlotLimitReached(U p, Object key, Position position, boolean emptyList) {
        if (sPlotLimiter.isLimitReached(key, p.getName())) {
            addRenderable(sPlotLimiter.getPlotLimitIndicator(position, emptyList), true);
            return true;
        } else {
            return false;
        }
    }
}
