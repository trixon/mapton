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
package org.mapton.butterfly_structural.strain;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Box;
import java.util.ArrayList;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.structural.BStructuralStrainGaugePoint;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer extends GraphicRendererBase {

    private final StrainAttributeManager mAttributeManager = StrainAttributeManager.getInstance();

    public GraphicRenderer(RenderableLayer layer, IndexedCheckModel<GraphicRendererItem> checkModel) {
        super(layer);
        sCheckModel = checkModel;
    }

    public void plot(BStructuralStrainGaugePoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        GraphicRendererBase.sMapObjects = mapObjects;
        if (sCheckModel.isChecked(GraphicRendererItem.ALARM_CONSUMPTION)) {
            plotAlarmConsumption(p, position);
        }
    }

    public void reset() {
    }

    private void plotAlarmConsumption(BStructuralStrainGaugePoint p, Position position) {
        if (isPlotLimitReached(p, GraphicRendererItem.ALARM_CONSUMPTION, position) || p.ext().getObservationFilteredLast() == null) {
            return;
        }

        var o = p.ext().getObservationFilteredLast();

        if (p.getDimension() != BDimension._2d) {
            Integer percentH = p.ext().getAlarmPercent(BComponent.HEIGHT);
            if (percentH == null) {
                percentH = 0;
            }

            int alarmLevel = p.ext().getAlarmLevelHeight(o);
            var dZ = o.ext().getDeltaZ();
            var rise = false;
            if (dZ != null) {
                rise = Math.signum(o.ext().getDeltaZ()) > 0;
            }
            var attrs = mAttributeManager.getComponentTrace1dAttributes(alarmLevel, rise, false);
            var pos = WWHelper.positionFromPosition(position, PERCENTAGE_ALTITUDE * percentH / 100.0);
            var box = new Box(pos, PERCENTAGE_SIZE, PERCENTAGE_SIZE, PERCENTAGE_SIZE);
            box.setAttributes(attrs);
            addRenderable(box, true);
        }

        plotPercentageRod(position, p.ext().getAlarmPercent());
    }
}
