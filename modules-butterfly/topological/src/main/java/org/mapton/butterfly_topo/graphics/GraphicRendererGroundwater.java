/*
 * Copyright 2026 Patrik Karlström.
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

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Path;
import java.time.Duration;
import java.util.List;
import org.mapton.api.Mapton;
import org.mapton.butterfly_core.api.BCoordinatrix;
import org.mapton.butterfly_core.api.ButterflyHelper;
import org.mapton.butterfly_format.types.hydro.BHydroGroundwaterPoint;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRendererGroundwater extends GraphicRendererBase {

    public GraphicRendererGroundwater(RenderableLayer layer, RenderableLayer passiveLayer) {
        super(layer, passiveLayer);
    }

    public void plot(BTopoControlPoint p, Position position) {
        if (sCheckModel.isChecked(GraphicItem.CLUSTER_GROUNDWATER_CONNECTOR)) {
            plotGroundwaterConnector(p, position);
        }

        if (sCheckModel.isChecked(GraphicItem.CLUSTER_GROUNDWATER_GRAPH)) {
            plotGroundwaterGraph(p, position);
        }

    }

    private List<BHydroGroundwaterPoint> getGroundwaterPoints(BTopoControlPoint p) {
        return ButterflyHelper.getGroundwaterPoints(p, 100, 5, p.getDateZero());
    }

    private void plotGroundwaterConnector(BTopoControlPoint p, Position position) {
        for (var gw : getGroundwaterPoints(p)) {
            var p1 = WWHelper.positionFromPosition(position, 0.1);
            var p2 = WWHelper.positionFromPosition(BCoordinatrix.toPositionWW2d(gw), 0.1);
            var path = new Path(p1, p2);
            path.setAttributes(mAttributeManager.getGroundwaterAttributes());
            addRenderable(path, true, null, null);
            var leftClickRunnable = (Runnable) () -> {
                Mapton.getGlobalState().put(BHydroGroundwaterPoint.class.getName() + "select", gw);
            };

            path.setValue(WWHelper.KEY_RUNNABLE_LEFT_CLICK, leftClickRunnable);
        }
    }

    private void plotIndicatorGroundPath(Position position, BTopoControlPoint point) {
//        var groundPath = new Path(position, WWHelper.positionFromPosition(position, MAX + .3));
//        groundPath.setAttributes(mAttributeManager.getGroundPathAttributes());
//        addRenderable(groundPath, false, null, null);

    }

    private void plotGroundwaterGraph(BTopoControlPoint p, Position position) {
        var groundwaterPoints = getGroundwaterPoints(p);
        var maxStart = groundwaterPoints.stream().mapToDouble(gw -> gw.ext().getObservationRawFirst().getGroundwaterLevel()).max().orElse(0);
        var maxEnd = groundwaterPoints.stream().mapToDouble(gw -> gw.ext().getObservationRawLast().getGroundwaterLevel()).max().orElse(0);
        var offset = 20;
//        double adjustment;
        var minLevel = groundwaterPoints.stream()
                .flatMap(gw -> gw.ext().getObservationsAllRaw().stream())
                .mapToDouble(o -> o.getGroundwaterLevel())
                .min()
                .orElse(0);
        var adjustment = minLevel * Math.signum(minLevel);
        for (var gw : groundwaterPoints) {
            var p1 = WWHelper.positionFromPosition(BCoordinatrix.toPositionWW2d(gw), 0.1);
            var p2 = WWHelper.positionFromPosition(position, 0.1);

            var ll1 = BCoordinatrix.toLatLon(gw);
            var ll2 = BCoordinatrix.toLatLon(p);

            var totalDistance = ll1.distance(ll2);
            var bearing = ll1.getBearing(ll2);
//            var minLevel = gw.ext().getObservationsAllRaw().stream().mapToDouble(o -> o.getGroundwaterLevel()).min().orElse(0);
            var maxLevel = gw.ext().getObservationsAllRaw().stream().mapToDouble(o -> o.getGroundwaterLevel()).max().orElse(0);
            var totalDuration = Duration.between(gw.ext().getDateFirst(), gw.ext().getDateLatest());
            var scale = totalDistance / (totalDuration.toHours() / 24.0);

            var nodes = gw.ext().getObservationsAllRaw().stream()
                    .filter(o -> o.getGroundwaterLevel() != null)
                    .map(o -> {
                        var duration = Duration.between(gw.ext().getDateFirst(), o.getDate());
                        var distance = scale * (duration.toHours() / 24.0);
                        var level = offset + o.getGroundwaterLevel() - adjustment;
                        var node = WWHelper.movePolar(p1, bearing, distance, level);
                        return node;
                    })
                    .toList();

            var path = new Path(nodes);
//            path.setShowPositions(true);

            path.setAttributes(mAttributeManager.getGroundwaterAttributes());
            addRenderable(path, true, null, null);
            var leftClickRunnable = (Runnable) () -> {
                Mapton.getGlobalState().put(BHydroGroundwaterPoint.class.getName() + "select", gw);
            };

            path.setValue(WWHelper.KEY_RUNNABLE_LEFT_CLICK, leftClickRunnable);

            var groundPath = new Path(p1, WWHelper.positionFromPosition(p1, maxStart + offset - adjustment));
            groundPath.setAttributes(mAttributeManager.getGroundwaterAttributes());
            addRenderable(groundPath, false, null, null);
        }

        var groundPath0 = new Path(position, WWHelper.positionFromPosition(position, maxStart + offset - adjustment));
        groundPath0.setAttributes(mAttributeManager.getGroundwaterAttributes());
        addRenderable(groundPath0, false, null, null);
    }

}
