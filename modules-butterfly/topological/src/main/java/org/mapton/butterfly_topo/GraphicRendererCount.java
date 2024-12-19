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
package org.mapton.butterfly_topo;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Material;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.TreeMap;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import static org.mapton.butterfly_topo.GraphicRendererBase.sMapObjects;
import org.mapton.worldwind.api.WWHelper;
import se.trixon.almond.util.CollectionHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRendererCount extends GraphicRendererBase {

    private final DateTimeFormatter mPerMonthDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM'-01'");

    public GraphicRendererCount(RenderableLayer layer, RenderableLayer passiveLayer) {
        super(layer, passiveLayer);
    }

    public void plot(BTopoControlPoint p, Position position) {
        if (sCheckModel.isChecked(GraphicRendererItem.MEASUREMENTS)) {
            plotCount(p, position);
        }
        if (sCheckModel.isChecked(GraphicRendererItem.MEASUREMENTS_PER_MONTH)) {
            plotCountPerMonth(p, position);
        }
    }

    private void plotCount(BTopoControlPoint p, Position position) {
        if (isPlotLimitReached(p, GraphicRendererItem.MEASUREMENTS, position)) {
            return;
        }
        var count = p.ext().getObservationsTimeFiltered().size();
        var cylinder = new Cylinder(position, count * .25, 0.5);
        cylinder.setAttributes(mAttributeManager.getComponentMeasurementsAttributes(p));
        addRenderable(cylinder, true, null, sMapObjects);
    }

    private void plotCountPerMonth(BTopoControlPoint p, Position position) {
        if (isPlotLimitReached(p, GraphicRendererItem.MEASUREMENTS_PER_MONTH, position)) {
            return;
        }

        var monthToCountMap = new TreeMap<String, Integer>();

        p.ext().getObservationsTimeFiltered().forEach(o -> {
            CollectionHelper.incInteger(monthToCountMap, o.getDate().format(mPerMonthDateTimeFormatter));
        });

        var items = new ArrayList<Counter>();
        for (int y = p.ext().getObservationFilteredFirstDate().getYear(); y < LocalDate.now().getYear() + 1; y++) {
            for (int m = 1; m < 13; m++) {
                var key = "%d-%02d-01".formatted(y, m);
                var count = monthToCountMap.getOrDefault(key, 0);
                items.add(new Counter(LocalDate.parse(key).atStartOfDay(), count));
            }
        }

        var reversedList = items.reversed();
        var altitude = 0.0;
        var prevHeight = 0.0;
        var prevDate = LocalDateTime.now();

        for (int i = 0; i < reversedList.size(); i++) {
            var o = reversedList.get(i);
            var timeSpan = ChronoUnit.MINUTES.between(o.date(), prevDate);
            var height = timeSpan / 24000.0;
            altitude = altitude + height * 0.5 + prevHeight * 0.5;
            prevDate = o.date();
            prevHeight = height;

            var pos = WWHelper.positionFromPosition(position, altitude);
            var maxRadius = 5.0;
            var scale = 0.5;
            var dZ = o.count();
            var radius = Math.min(maxRadius, Math.abs(dZ) * scale + 0.05);
            var maximus = radius == maxRadius;

            var cylinder = new Cylinder(pos, height, radius);
            var attrs = mAttributeManager.getComponentMeasurementsAttributes(p);
            if (maximus) {
                attrs = new BasicShapeAttributes(attrs);
                attrs.setInteriorMaterial(new Material(attrs.getInteriorMaterial().getDiffuse().darker()));
            }
            cylinder.setAttributes(attrs);

            addRenderable(cylinder, true, GraphicRendererItem.MEASUREMENTS_PER_MONTH, sMapObjects);
        }

    }

    public record Counter(LocalDateTime date, int count) {

    }
}
