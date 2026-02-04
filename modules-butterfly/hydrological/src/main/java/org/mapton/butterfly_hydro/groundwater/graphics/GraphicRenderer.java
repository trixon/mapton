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
package org.mapton.butterfly_hydro.groundwater.graphics;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.ObjectUtils;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.Mapton;
import org.mapton.butterfly_core.api.BCoordinatrix;
import org.mapton.butterfly_format.types.hydro.BHydroGroundwaterPoint;
import org.mapton.butterfly_format.types.hydro.BHydroGroundwaterPointObservation;
import org.mapton.butterfly_hydro.groundwater.GroundwaterAttributeManager;
import org.mapton.butterfly_hydro.groundwater.GroundwaterManager;
import org.mapton.worldwind.api.WWHelper;
import se.trixon.almond.util.DateHelper;

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
    private final GroundwaterManager mManager = GroundwaterManager.getInstance();
    private double mMidLevel;
    private double mMinLevel;

    public GraphicRenderer(RenderableLayer layer, RenderableLayer passiveLayer, IndexedCheckModel<GraphicItem> checkModel) {
        super(layer, passiveLayer);
        sCheckModel = checkModel;
    }

    public void init() {
        mMinLevel = mManager.getTimeFilteredItems().stream()
                .flatMap(p -> p.ext().getObservationsAllRaw().stream())
                .filter(o -> o.getGroundwaterLevel() != null)
                .mapToDouble(o -> o.getGroundwaterLevel())
                .min()
                .orElse(0);
    }

    @Override
    public void plot(BHydroGroundwaterPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        GraphicRendererBase.sMapObjects = mapObjects;

        plotTrace(p, position, GraphicItem.LEVEL_3, 3);
        plotTrace(p, position, GraphicItem.LEVEL_6, 6);
        plotTrace(p, position, GraphicItem.LEVEL_12, 12);
        plotTrace(p, position, GraphicItem.LEVEL_18, 18);
        plotTrace(p, position, GraphicItem.LEVEL_ALL, Integer.MAX_VALUE);

        if (sCheckModel.isChecked(GraphicItem.CHART)) {
            plotGroundwaterGraph(p, position);
        }
    }

    private Double getMedian(BHydroGroundwaterPoint p) {
        if (p.ext().getObservationsTimeFiltered().isEmpty()) {
            return 0.0;
        }
        var list = p.ext().getObservationsTimeFiltered();
        var waterLevels = list.stream()
                .filter(d -> d.getGroundwaterLevel() != null)
                .mapToDouble(BHydroGroundwaterPointObservation::getGroundwaterLevel)
                .sorted();
        var median = list.size() % 2 == 0
                ? waterLevels.skip(list.size() / 2 - 1).limit(2).average().getAsDouble()
                : waterLevels.skip(list.size() / 2).findFirst().getAsDouble();

        return median;
    }

    private void plotGroundwaterGraph(BHydroGroundwaterPoint p, Position position) {
        var offset = 20;
        var adjustment = Math.abs(mMinLevel);
        var totalDuration = Duration.between(p.ext().getDateFirst(), p.ext().getDateLatest());
        var daysPerMeter = 30d;
        var days = totalDuration.toDays();
        var midDate = p.ext().getDateFirst().plusDays((long) (days * .5));
        var totalDistance = days / daysPerMeter;
        var ll = BCoordinatrix.toLatLon(p);
        var ll1 = ll.getDestinationPoint(-90, .5 * totalDistance);
        var ll2 = ll.getDestinationPoint(+90, .5 * totalDistance);
        var p1 = WWHelper.positionFromLatLon(ll1);

        var bearing = ll1.getBearing(ll2);
        var scale = totalDistance / (totalDuration.toHours() / 24.0);
        var nodes = p.ext().getObservationsAllRaw().stream()
                .filter(o -> o.getGroundwaterLevel() != null)
                .map(o -> {
                    var duration = Duration.between(p.ext().getDateFirst(), o.getDate());
                    var distance = scale * (duration.toHours() / 24.0);
                    var level = offset + o.getGroundwaterLevel() + adjustment;
                    if (DateHelper.isBeforeOrEqual(o.getDate().toLocalDate(), midDate.toLocalDate())) {
                        mMidLevel = level;
                    }
                    var node = WWHelper.movePolar(p1, bearing, distance, level);
                    return node;
                })
                .toList();

        var path = new Path(nodes);
//        path.setShowPositions(true);

        path.setAttributes(mAttributeManager.getGroundwaterAttributes());
        addRenderable(path, true, null, null);
        var leftClickRunnable = (Runnable) () -> {
            Mapton.getGlobalState().put(BHydroGroundwaterPoint.class.getName() + "select", p);
        };

        path.setValue(WWHelper.KEY_RUNNABLE_LEFT_CLICK, leftClickRunnable);
        var groundPath = new Path(WWHelper.positionFromPosition(position, 0), WWHelper.positionFromPosition(position, mMidLevel));
        var attrs = new BasicShapeAttributes(mAttributeManager.getGroundwaterAttributes());
        groundPath.setAttributes(attrs);
        addRenderable(groundPath, false, null, null);
    }

    private void plotTrace(BHydroGroundwaterPoint p, Position position, List<BHydroGroundwaterPointObservation> list, GraphicItem rendererItem) {
        var prevDate = LocalDateTime.now();
        var altitude = 0.0;
        var prevHeight = 0.0;

        var reversedList = list.reversed();
        var minObservation = list.stream()
                .filter(o -> o.getGroundwaterLevel() != null)
                .min(Comparator.comparing(BHydroGroundwaterPointObservation::getGroundwaterLevel))
                .orElse(null);

        //var median = getMedian(p);
        if (minObservation == null) {
            return;
        }
        var referenceValue = minObservation.getGroundwaterLevel();

        for (int i = 0; i < reversedList.size(); i++) {
            var o = reversedList.get(i);
            if (ObjectUtils.anyNull(o.getDate(), o.getGroundwaterLevel())
                    || o.getDate().isBefore(LocalDate.parse("2017-01-01").atStartOfDay())) {
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
//            var dZ = o.getGroundwaterLevel() - median;
            var dZ = o.getGroundwaterLevel() - referenceValue;

//            dZ = dZ * dZ;
            var scale = 2.0;
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
            addRenderable(cylinder, true, rendererItem, sMapObjects);
        }

    }

    private void plotTrace(BHydroGroundwaterPoint p, Position position, GraphicItem rendererItem, int months) {
        if (!sCheckModel.isChecked(rendererItem)
                || isPlotLimitReached(p, rendererItem, position)) {
            return;
        }

        plotTrace(p, position,
                p.ext().getObservationsTimeFiltered().stream().filter(o -> DateHelper.isAfterOrEqual(o.getDate().toLocalDate(), LocalDate.now().minusMonths(months))).toList(),
                rendererItem);
    }
}
