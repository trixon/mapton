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
package org.mapton.butterfly_topo.graphics;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import java.util.ArrayList;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer extends GraphicRendererBase {

    private final GraphicRendererAlarmLevel mAlarmRenderer;
    private final GraphicRendererCircle mCircleRenderer;
    private final GraphicRendererCount mCountRenderer;
    private final GraphicRendererGroundwater mGroundwaterRenderer;
    private final GraphicRendererGroup mGroupRenderer;
    private final GraphicRendererTrace mTraceRenderer;
    private final GraphicRendererTrend mTrendRenderer;
    private final GraphicRendererVector mVectorRenderer;

    public GraphicRenderer(RenderableLayer layer, RenderableLayer passiveLayer, IndexedCheckModel<GraphicItem> checkModel) {
        super(layer, passiveLayer);
        mVectorRenderer = new GraphicRendererVector(layer, passiveLayer);
        mTraceRenderer = new GraphicRendererTrace(layer, passiveLayer);
        mCountRenderer = new GraphicRendererCount(layer, passiveLayer);
        mCircleRenderer = new GraphicRendererCircle(layer, passiveLayer);
        mAlarmRenderer = new GraphicRendererAlarmLevel(layer, passiveLayer);
        mTrendRenderer = new GraphicRendererTrend(layer, passiveLayer);
        mGroupRenderer = new GraphicRendererGroup(layer, passiveLayer);
        mGroundwaterRenderer = new GraphicRendererGroundwater(layer, passiveLayer);

        sCheckModel = checkModel;
    }

    @Override
    public void plot(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        sMapObjects = mapObjects;

        if (sCheckModel.isChecked(GraphicItem.BEARING) && !sCheckModel.isChecked(GraphicItem.PIN)) {
            plotBearing(p, position, 0.0);
        }

        if (sCheckModel.isChecked(GraphicItem.MEASUREMENT_MODE)) {
            plotMeasMode(p, position);
        }

        if (p.ext().getNumOfObservationsFiltered() > 1) {
            mCircleRenderer.plot(p, position);
            mTraceRenderer.plot(p, position);
            mVectorRenderer.plot(p, position);
            mCountRenderer.plot(p, position);
            mAlarmRenderer.plot(p, position);
            mTrendRenderer.plot(p, position);
            mGroupRenderer.plot(p, position);
        }

        mGroundwaterRenderer.plot(p, position);
    }

    @Override
    public void postPlot() {
        mGroupRenderer.postPlot();
    }

    @Override
    public void reset() {
        super.reset();
        sPointToPositionMap.clear();
        mGroupRenderer.reset();
        clearLabeledPoints(BTopoControlPoint.class);
        mVectorRenderer.reset();
    }

}
