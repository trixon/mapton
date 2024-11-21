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
package org.mapton.butterfly_hydro.groundwater;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Cylinder;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import org.apache.commons.lang3.ObjectUtils;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_format.types.hydro.BHydroGroundwaterPoint;
import org.mapton.butterfly_format.types.hydro.BHydroGroundwaterPointObservation;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer extends GraphicRendererBase {

    private static final int DAYS_TO_DIM_FIRST_OBSERVATION = 30;
    private static final int DAYS_TO_SKIP = 30;
    private static final int KEEP_END = 10;
    private static final int KEEP_START = 10;

    private final GroundwaterAttributeManager mAttributeManager = GroundwaterAttributeManager.getInstance();

    public GraphicRenderer(RenderableLayer layer, RenderableLayer passiveLayer, IndexedCheckModel<GraphicRendererItem> checkModel) {
        super(layer, passiveLayer);
        sCheckModel = checkModel;
    }

    public void plot(BHydroGroundwaterPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        GraphicRendererBase.sMapObjects = mapObjects;

        plotTrace(p, position);
    }

    private Double getMedian(BHydroGroundwaterPoint p) {
        if (p.ext().getObservationsTimeFiltered().isEmpty()) {
            return 0.0;
        }
        var list = p.ext().getObservationsTimeFiltered();
        var waterLevels = list.stream()
                .filter(d -> d.getGroundWaterLevel() != null)
                .mapToDouble(BHydroGroundwaterPointObservation::getGroundWaterLevel)
                .sorted();
        var median = list.size() % 2 == 0
                ? waterLevels.skip(list.size() / 2 - 1).limit(2).average().getAsDouble()
                : waterLevels.skip(list.size() / 2).findFirst().getAsDouble();

        return median;
    }

    private void plotTrace(BHydroGroundwaterPoint p, Position position) {
        if (!sCheckModel.isChecked(GraphicRendererItem.LEVEL)
                || isPlotLimitReached(p, GraphicRendererItem.LEVEL, position)) {
            return;
        }

        var prevDate = LocalDateTime.now();
        var altitude = 0.0;
        var prevHeight = 0.0;

        var reversedList = p.ext().getObservationsTimeFiltered().reversed();
        var median = getMedian(p);

        for (int i = 0; i < reversedList.size(); i++) {
            var o = reversedList.get(i);
            if (ObjectUtils.anyNull(o.getDate(), o.getGroundWaterLevel())) {
                continue;
            }

            var keepStart = i < KEEP_START;
            var keepEnd = i > reversedList.size() - KEEP_END;

            var daysSinceMeasurement = ChronoUnit.DAYS.between(o.getDate(), prevDate);
//            if (!keepStart && !keepEnd && !explicitlyAllowed && daysSinceMeasurement < DAYS_TO_SKIP) {
//                continue;
//            }

            var timeSpan = ChronoUnit.MINUTES.between(o.getDate(), prevDate);
            var height = timeSpan / 24000.0;
            if (height <= 0) {
                continue;
            }
            altitude = altitude + height * 0.5 + prevHeight * 0.5;
            prevDate = o.getDate();
            prevHeight = height;

            var pos = WWHelper.positionFromPosition(position, altitude);
            var maxRadius = 100.0;

//            var dZ = o.getNivå() - p.ext().getMaxObservation().getNivå();
            var dZ = o.getGroundWaterLevel() - median;
//            dZ = dZ * dZ;
            var scale = 1.0;
            var radius = Math.min(maxRadius, Math.abs(dZ) * scale + 0.1);
            var cylinder = new Cylinder(pos, height, radius);
            var attrs = mAttributeManager.getTimeSeriesAttributes(p);

            if (i == 0 && daysSinceMeasurement > DAYS_TO_DIM_FIRST_OBSERVATION) {
                attrs = new BasicShapeAttributes(attrs);
                attrs.setInteriorOpacity(0.25);
            }
            if (dZ < 0) {
                var lightColor = attrs.getInteriorMaterial().getSpecular().brighter();
//                attrs.setInteriorMaterial(new Material(lightColor));
            }

            cylinder.setAttributes(attrs);
            addRenderable(cylinder, true, GraphicRendererItem.LEVEL, sMapObjects);
        }

    }

}
