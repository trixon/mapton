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
package org.mapton.butterfly_rock_convergence.graphic;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import gov.nasa.worldwind.render.Pyramid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.SortOrder;
import org.apache.commons.lang3.Strings;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.Mapton;
import org.mapton.butterfly_core.api.BCoordinatrix;
import org.mapton.butterfly_core.api.ButterflyHelper;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_format.types.rock.BRockConvergence;
import org.mapton.butterfly_format.types.rock.BRockConvergenceObservation;
import org.mapton.butterfly_format.types.rock.BRockConvergencePair;
import org.mapton.butterfly_rock_convergence.api.ConvergenceManager;
import org.mapton.butterfly_rock_convergence.AnchorManager;
import org.mapton.butterfly_rock_convergence.pair.PairHelper;
import org.mapton.butterfly_rock_convergence.pair.chart.ConvergencePairChartBuilder;
import org.mapton.worldwind.api.WWHelper;
import se.trixon.almond.util.CollectionHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer extends GraphicRendererBase {

    private final AnchorManager mAnchorManager = AnchorManager.getInstance();
    private double mOffset;
    private final HashSet<String> mPlottedLabels = new HashSet<>();
    private final HashSet<String> mPlottedNodes = new HashSet<>();

    public GraphicRenderer(RenderableLayer layer, RenderableLayer passiveLayer, IndexedCheckModel<GraphicItem> checkModel) {
        super(layer, passiveLayer);
        sCheckModel = checkModel;
    }

    @Override
    public void plot(BRockConvergence convergence, Position position, ArrayList<AVListImpl> mapObjects) {
        sMapObjects = mapObjects;
        sMapObjects = mapObjects;
        mOffset = ConvergenceManager.getInstance().getOffset();

        if (sCheckModel.isChecked(GraphicItem.LINES)) {
            plotLine(convergence);
        }

        if (sCheckModel.isChecked(GraphicItem.NODE)) {
            plotNodes(convergence);
        }

        if (sCheckModel.isChecked(GraphicItem.LABELS)) {
            plotLabels(convergence);
        }

        if (sCheckModel.isChecked(GraphicItem.VALUE_1D)) {
            plotValues(convergence, GraphicItem.VALUE_1D);
        }

        if (sCheckModel.isChecked(GraphicItem.VALUE_2D)) {
            plotValues(convergence, GraphicItem.VALUE_2D);
        }

        if (sCheckModel.isChecked(GraphicItem.VALUE_3D)) {
            plotValues(convergence, GraphicItem.VALUE_3D);
        }
    }

    @Override
    public void reset() {
        super.reset();
        mPlottedNodes.clear();
        mPlottedLabels.clear();
    }

    private void plotLabel(BRockConvergencePair pair, BTopoControlPoint controlPoint) {
        var name = controlPoint.getName();

        if (!mPlottedLabels.contains(name)) {
            var position = PairHelper.getPosition(controlPoint, mOffset);
            var placemark = new PointPlacemark(position);
            placemark.setAltitudeMode(WorldWind.ABSOLUTE);
            placemark.setAttributes(mAttributeManager.getLabelPlacemarkAttributes());
            placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(mAttributeManager.getLabelPlacemarkAttributes(), 1.5));
            placemark.setLabelText(Strings.CS.remove(controlPoint.getName(), pair.getConvergence().getName()));
            addRenderable(placemark, true, null, sMapObjects);
            mPlottedLabels.add(name);
        }
    }

    private void plotLabels(BRockConvergence convergence) {
        for (var pair : convergence.ext().getPairs()) {

            plotLabel(pair, pair.getP1());
            plotLabel(pair, pair.getP2());
        }
    }

    private void plotLine(BRockConvergence convergence) {
        for (var pair : convergence.ext().getPairs()) {
            if (pair.ext().getObservationsTimeFiltered().isEmpty()) {
                continue;
            }

            var pos1 = PairHelper.getPosition(pair.getP1(), mOffset);
            var pos2 = PairHelper.getPosition(pair.getP2(), mOffset);

            var path = new Path(pos1, pos2);
            var attrs = new BasicShapeAttributes(mAttributeManager.getPairPathAttributes());
            var function = BRockConvergenceObservation.FUNCTION_3D;
            var alarmLevel = pair.ext().getAlarmLevel(function);

            attrs.setOutlineMaterial(ButterflyHelper.getAlarmMaterial(alarmLevel));
            var delta = pair.ext().getDelta(function);
            if (delta >= 0) {
                attrs.setOutlineStippleFactor(3);
            }
            if (Math.abs(delta) < 0.5) {
                attrs.setOutlineMaterial(Material.LIGHT_GRAY);
            }

            path.setAttributes(attrs);
            addRenderable(path, true, GraphicItem.LINES, sMapObjects);
        }
    }

    private void plotNode(BTopoControlPoint controlPoint, double offset, boolean oneOfTheWorsts) {
        var name = controlPoint.getName();
        if (!mPlottedNodes.contains(name)) {
            var radius = PairHelper.NODE_SIZE;
            var position = PairHelper.getPosition(controlPoint, offset);
            var pyramid = new Pyramid(position, radius, radius);
            var attrs = new BasicShapeAttributes(mAttributeManager.getNodeAttributes());
            if (oneOfTheWorsts) {
                attrs.setInteriorMaterial(Material.RED);
            }
            pyramid.setAttributes(attrs);
            addRenderable(pyramid, true, GraphicItem.NODE, null);

            var groundPath = new Path(position, WWHelper.positionFromPosition(position, 0.0));
            groundPath.setAttributes(mAttributeManager.getGroundPathAttributes());
            addRenderable(groundPath, false, GraphicItem.NODE, sMapObjects);

            mPlottedNodes.add(name);

            var leftClickRunnable = (Runnable) () -> {
                Mapton.getGlobalState().put(ConvergencePairChartBuilder.class.getName() + "node", name);
            };
            pyramid.setValue(WWHelper.KEY_RUNNABLE_LEFT_CLICK, leftClickRunnable);
        }
    }

    private void plotNodes(BRockConvergence convergence) {
        var map = new HashMap<String, Integer>();
        convergence.ext().getPairsOrderedByDeltaDesc(BRockConvergenceObservation.FUNCTION_3D, 5)
                .forEachOrdered(pair -> {
                    CollectionHelper.incInteger(map, pair.getP1().getName());
                    CollectionHelper.incInteger(map, pair.getP2().getName());
                });

        var sortedMap = CollectionHelper.sortByValue(map, SortOrder.DESCENDING);
        var worstPointNames = sortedMap.keySet().stream()
                .limit(2)
                .collect(Collectors.toSet());

        for (var pair : convergence.ext().getPairs()) {
            for (var p : List.of(pair.getP1(), pair.getP2())) {
                plotNode(p, mOffset, worstPointNames.contains(p.getName()));
            }
        }
    }

    private void plotValues(BRockConvergence convergence, GraphicItem graphicItem) {
        Function<BRockConvergenceObservation, Double> function = null;
        var verticalOffset = 0.0;
        switch (graphicItem) {
            case VALUE_1D:
                function = BRockConvergenceObservation.FUNCTION_1D;
                verticalOffset = -0.5;
                break;
            case VALUE_2D:
                function = BRockConvergenceObservation.FUNCTION_2D;
                break;
            case VALUE_3D:
                function = BRockConvergenceObservation.FUNCTION_3D;
                verticalOffset = +0.5;
                break;
        }

        for (var pair : convergence.ext().getPairs()) {
            var pos1 = BCoordinatrix.toPositionWW3d(pair.getP1());
            var pos2 = BCoordinatrix.toPositionWW3d(pair.getP2());
            var latLon = Position.getCenter(List.of(pos1, pos2));
            var elevation = -0.5 + mOffset + pos1.elevation + (pos2.elevation - pos1.elevation) / 2 + verticalOffset;
            var position = new Position(latLon, elevation);
            var placemark = new PointPlacemark(position);
            placemark.setAltitudeMode(WorldWind.ABSOLUTE);
            var attrs = new PointPlacemarkAttributes(mAttributeManager.getLabelPlacemarkAttributes());
            attrs.setLabelOffset(Offset.CENTER);
            placemark.setAttributes(attrs);
            placemark.setLabelText("%+.1f".formatted(pair.ext().getDelta(function)));
            addRenderable(placemark, false, null, null);
        }

    }

}
