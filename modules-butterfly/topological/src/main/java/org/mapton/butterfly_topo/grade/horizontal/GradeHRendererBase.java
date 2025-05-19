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
package org.mapton.butterfly_topo.grade.horizontal;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import java.util.HashMap;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_format.types.topo.BTopoGrade;
import org.mapton.butterfly_topo.graphics.GraphicRendererBase;
import org.mapton.butterfly_topo.grade.GradeAttributeManager;

/**
 *
 * @author Patrik Karlström
 */
public abstract class GradeHRendererBase extends GraphicRendererBase {

    protected static IndexedCheckModel<GradeHRendererItem> sCheckModel;
    protected static HashMap<BTopoGrade, Position[]> sPointToPositionMap = new HashMap<>();
    protected final GradeAttributeManager mAttributeManager = GradeAttributeManager.getInstance();

    static {
        for (var renderItem : GradeHRendererItem.values()) {
            sPlotLimiter.setLimit(renderItem, renderItem.getPlotLimit());
        }
    }

    public GradeHRendererBase(RenderableLayer layer, RenderableLayer passiveLayer) {
        super(layer, passiveLayer);
    }

//    protected boolean isPlotLimitReached(BStructuralStrainGaugePoint p, Object key, Position position) {
//        return super.isPlotLimitReached(p, key, position, p.ext().getObservationsTimeFiltered().isEmpty());
//    }
}
