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
package org.mapton.butterfly_tmo.grundvatten;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Renderable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import org.mapton.butterfly_core.api.PlotLimiter;
import org.mapton.butterfly_format.types.tmo.BGrundvatten;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ComponentRenderer {

    private static final int DAYS_TO_DIM_FIRST_OBSERVATION = 30;
    private static final int DAYS_TO_SKIP = 30;
    private static final int KEEP_END = 10;
    private static final int KEEP_START = 10;
    private static final String KEY_TIME_SERIES = "timeSeries";
    private static final int PLOT_LIMIT = 1000;

    private final GrundvattenAttributeManager mAttributeManager = GrundvattenAttributeManager.getInstance();
    private final RenderableLayer mLevelLayer;
    private ArrayList<AVListImpl> mMapObjects;
    private final PlotLimiter mPlotLimiter = new PlotLimiter();

    public ComponentRenderer(RenderableLayer levelLayer) {
        mLevelLayer = levelLayer;
        mPlotLimiter.setLimit(KEY_TIME_SERIES, PLOT_LIMIT);
    }

    public PlotLimiter getPlotLimiter() {
        return mPlotLimiter;
    }

    public void plot(BGrundvatten p, Position position, ArrayList<AVListImpl> mapObjects) {
        mMapObjects = mapObjects;

        if (isPlotLimitReached(p, KEY_TIME_SERIES, position)) {
            return;
        }

        var prevDate = LocalDateTime.now();
        var altitude = 0.0;
        var prevHeight = 0.0;

        var explicitlyAllowed = mPlotLimiter.isAllowed(p.getName());
        var reversedList = p.ext().getObservationsTimeFiltered().reversed();

        for (int i = 0; i < reversedList.size(); i++) {
            var o = reversedList.get(i);
            if (o.getDate() == null) {
                continue;
            }

            var keepStart = i < KEEP_START;
            var keepEnd = i > reversedList.size() - KEEP_END;

            var daysSinceMeasurement = ChronoUnit.DAYS.between(o.getDate(), prevDate);
            if (!keepStart && !keepEnd && !explicitlyAllowed && daysSinceMeasurement < DAYS_TO_SKIP) {
                continue;
            }

            var timeSpan = ChronoUnit.MINUTES.between(o.getDate(), prevDate);
            var height = timeSpan / 24000.0;
            altitude = altitude + height * 0.5 + prevHeight * 0.5;
            prevDate = o.getDate();
            prevHeight = height;

            var pos = WWHelper.positionFromPosition(position, altitude);
            var maxRadius = 100.0;

            var dZ = o.getNivå() - p.ext().getMinObservation().getNivå();
            var radius = Math.min(maxRadius, Math.abs(dZ) * 1 + 0.01);

            var cylinder = new Cylinder(pos, height, radius);
            var attrs = mAttributeManager.getTimeSeriesAttributes(p);

            if (i == 0 && daysSinceMeasurement > DAYS_TO_DIM_FIRST_OBSERVATION) {
                attrs = new BasicShapeAttributes(attrs);
                attrs.setInteriorOpacity(0.25);
            }

            cylinder.setAttributes(attrs);
            addRenderable(mLevelLayer, cylinder);
            mPlotLimiter.incPlotCounter(KEY_TIME_SERIES);
        }
    }

    public void reset() {
        mPlotLimiter.reset();
    }

    private void addRenderable(RenderableLayer layer, Renderable renderable) {
        layer.addRenderable(renderable);
        if (layer == mLevelLayer) {
            if (renderable instanceof AVListImpl avlist) {
                mMapObjects.add(avlist);
            }
        } else {
            //mLayerXYZ.addRenderable(renderable); //TODO Add to a non responsive layer
        }
    }

    private boolean isPlotLimitReached(BGrundvatten p, Object key, Position position) {
        if (mPlotLimiter.isLimitReached(key, p.getName())) {
            addRenderable(mLevelLayer, mPlotLimiter.getPlotLimitIndicator(position, p.ext().getObservationsTimeFiltered().isEmpty()));
            return true;
        } else {
            if (p.ext().getObservationsTimeFiltered().isEmpty()) {
                addRenderable(mLevelLayer, mPlotLimiter.getPlotLimitIndicator(position, true));
            }

            return false;
        }
    }

}
