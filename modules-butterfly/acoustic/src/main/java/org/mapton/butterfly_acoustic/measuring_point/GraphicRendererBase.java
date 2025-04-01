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
package org.mapton.butterfly_acoustic.measuring_point;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.lang3.ObjectUtils;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_core.api.BaseGraphicRenderer;
import org.mapton.butterfly_core.api.PlotLimiter;
import org.mapton.butterfly_format.types.acoustic.BAcousticVibrationPoint;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;

/**
 *
 * @author Patrik Karlström
 */
public abstract class GraphicRendererBase extends BaseGraphicRenderer<GraphicRendererItem, BAcousticVibrationPoint> {

    protected static IndexedCheckModel<GraphicRendererItem> sCheckModel;
    protected static ArrayList<AVListImpl> sMapObjects;
    protected static final PlotLimiter sPlotLimiter = new PlotLimiter();
    protected static HashMap<BTopoControlPoint, Position[]> sPointToPositionMap = new HashMap<>();
    protected final MeasPointAttributeManager mAttributeManager = MeasPointAttributeManager.getInstance();
    protected final MeasPointManager mManager = MeasPointManager.getInstance();

    static {
        for (var renderItem : GraphicRendererItem.values()) {
            sPlotLimiter.setLimit(renderItem, renderItem.getPlotLimit());
        }
    }

    public GraphicRendererBase(RenderableLayer layer, RenderableLayer passiveLayer) {
        super(layer, passiveLayer, sPlotLimiter);
    }

    public boolean isValidFor3dPlot(BAcousticVibrationPoint p) {
        var o1 = p.ext().getObservationsTimeFiltered().getFirst();
        var o2 = p.ext().getObservationsTimeFiltered().getLast();

        return ObjectUtils.allNotNull(p.getZeroX(), p.getZeroY(), p.getZeroZ(), o1.getMeasuredZ(), o2.getMeasuredZ());
    }

    protected boolean isPlotLimitReached(BAcousticVibrationPoint p, Object key, Position position) {
        return super.isPlotLimitReached(p, key, position, p.ext().getObservationsTimeFiltered().isEmpty(), sMapObjects);
    }
}
