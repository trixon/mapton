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
package org.mapton.butterfly_topo_convergence.group.graphics;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.Pyramid;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.Mapton;
import org.mapton.butterfly_core.api.ButterflyHelper;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_format.types.topo.BTopoConvergenceGroup;
import org.mapton.butterfly_format.types.topo.BTopoConvergenceObservation;
import org.mapton.butterfly_format.types.topo.BTopoConvergencePair;
import org.mapton.butterfly_topo_convergence.api.ConvergenceGroupManager;
import org.mapton.butterfly_topo_convergence.group.AnchorManager;
import org.mapton.butterfly_topo_convergence.pair.PairHelper;
import org.mapton.butterfly_topo_convergence.pair.chart.ConvergencePairChartBuilder;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer extends GraphicRendererBase {

    private final AnchorManager mAnchorManager = AnchorManager.getInstance();
//    private final Material[] mMaterials;
    private double mOffset;
    private final HashSet<String> mPlottedLabels = new HashSet<>();
    private final HashSet<String> mPlottedNodes = new HashSet<>();

    public GraphicRenderer(RenderableLayer layer, RenderableLayer passiveLayer, IndexedCheckModel<GraphicItem> checkModel) {
        super(layer, passiveLayer);
        sCheckModel = checkModel;
//        mMaterials = new Material[]{
//            //https://colordesigner.io/gradient-generator/?mode=hsl#00FF00-FF0000
//            new Material(Color.decode("#00ff00")),
//            new Material(Color.decode("#22ff00")),
//            new Material(Color.decode("#44ff00")),
//            new Material(Color.decode("#66ff00")),
//            new Material(Color.decode("#88ff00")),
//            new Material(Color.decode("#aaff00")),
//            new Material(Color.decode("#ccff00")),
//            new Material(Color.decode("#eeff00")),
//            new Material(Color.decode("#ffee00")),
//            new Material(Color.decode("#ffcc00")),
//            new Material(Color.decode("#ffaa00")),
//            new Material(Color.decode("#ff8800")),
//            new Material(Color.decode("#ff6600")),
//            new Material(Color.decode("#ff4400")),
//            new Material(Color.decode("#ff2200")),
//            new Material(Color.decode("#ff0000"))
//        };
    }

    @Override
    public void plot(BTopoConvergenceGroup convergence, Position position, ArrayList<AVListImpl> mapObjects) {
        sMapObjects = mapObjects;
        sMapObjects = mapObjects;
        mOffset = ConvergenceGroupManager.getInstance().getOffset();

        if (sCheckModel.isChecked(GraphicItem.GEOMETRY)) {
            plotGeometry(convergence);
        }

        if (sCheckModel.isChecked(GraphicItem.GEOMETRY) && sCheckModel.isChecked(GraphicItem.GEOMETRY_NAME)) {
            plotGeometryName(convergence);
        }

        if (sCheckModel.isChecked(GraphicItem.ANCHOR_DISPLACEMENT)) {
            plotAnchorDisplacement(convergence);
        }

        mOffset = ConvergenceGroupManager.getInstance().getOffset();
        if (sCheckModel.isChecked(GraphicItem.LINES)) {
            plotLine(convergence);
        }

        if (sCheckModel.isChecked(GraphicItem.NODE)) {
            plotNodes(convergence);
        }

        if (sCheckModel.isChecked(GraphicItem.LABELS)) {
            plotLabels(convergence);
        }

    }

    @Override
    public void reset() {
        super.reset();
        mPlottedNodes.clear();
        mPlottedLabels.clear();
    }

    private BTopoConvergencePair getPairForPoints(BTopoConvergenceGroup group, BTopoControlPoint anchorPoint, BTopoControlPoint p2) {
        return group.ext().getPairs().stream()
                .filter(pair -> {
                    var set = Set.of(pair.getP1(), pair.getP2());
                    return set.containsAll(List.of(anchorPoint, p2));
                })
                .findFirst()
                .orElse(null);
    }

    private void plotAnchorDisplacement(BTopoConvergenceGroup group) {
        if (!group.ext().hasAnchorPoint()) {
            return;
        }

        var anchorPoint = group.ext().getAnchorPoint();
        plotGroundPath(anchorPoint);
        var anchorPosition = PairHelper.getPosition(anchorPoint, mOffset);
        group.ext().getControlPointsWithoutAnchor().stream()
                .sorted(Comparator.comparingDouble(BTopoControlPoint::getZeroZ))
                .limit(2)
                .forEach(p -> {
                    var pair = getPairForPoints(group, anchorPoint, p);
                    if (pair != null) {
//                        var delta = pair.getObservations().getLast().getDeltaDeltaZComparedToFirst();
//                        var pos2 = PairHelper.getPosition(p, mOffset);
//                        var path = new Path(anchorPosition, pos2);
//                        var attrs = new BasicShapeAttributes(mAttributeManager.getPairPathAttributes());
//                        var material = Material.GREEN;
//                        var absDelta = FastMath.abs(delta) / 1000.0;
//
//                        if (absDelta > 6) {
//                            material = Material.RED;
//                        } else if (absDelta > 4) {
//                            material = Material.ORANGE;
//                        } else if (absDelta > 2) {
//                            material = Material.YELLOW;
//                        }
//                        attrs.setOutlineMaterial(material);
//
//                        if (delta >= 0) {
//                            attrs.setOutlineStippleFactor(3);
//                        }
//                        path.setAttributes(attrs);
//                        addRenderable(path, true, GraphicItem.GEOMETRY, null);
//                        var leftClickRunnable = (Runnable) () -> {
//                            mAnchorManager.setSelectedItem(pair);
//                        };
//                        path.setValue(WWHelper.KEY_RUNNABLE_LEFT_CLICK, leftClickRunnable);
                    }
                });
    }

    private void plotGeometry(BTopoConvergenceGroup group) {
        group.ext().getPairs().stream()
                .filter(pair -> pair.getP1() != group.ext().getAnchorPoint() && pair.getP2() != group.ext().getAnchorPoint())
                .forEachOrdered(pair -> {
                    var pos1 = PairHelper.getPosition(pair.getP1(), mOffset);
                    var pos2 = PairHelper.getPosition(pair.getP2(), mOffset);

                    var path = new Path(pos1, pos2);
                    var attrs = new BasicShapeAttributes(mAttributeManager.getPairPathAttributes());
                    attrs.setOutlineMaterial(new Material(Color.decode("#add8e6")));
                    attrs.setOutlineWidth(1);
                    path.setAttributes(attrs);
                    addRenderable(path, true, GraphicItem.GEOMETRY, sMapObjects);
                });

        group.ext().getControlPointsWithoutAnchor().forEach(p -> {
            plotGroundPath(p);
        });
    }

    private void plotGeometryName(BTopoConvergenceGroup group) {
        group.ext().getPairs().forEach(pair -> {
            plotLabel(pair, pair.getP1());
            plotLabel(pair, pair.getP2());
        });
    }

    private void plotGroundPath(BTopoControlPoint p) {
        var name = p.getName();
        if (!mPlottedNodes.contains(name)) {
            var position = PairHelper.getPosition(p, mOffset);

            var groundPath = new Path(position, WWHelper.positionFromPosition(position, 0.0));
            groundPath.setAttributes(mAttributeManager.getGroundPathAttributes());
            addRenderable(groundPath, false, GraphicItem.GEOMETRY, sMapObjects);

            mPlottedNodes.add(name);
        }
    }

    private void plotLabel(BTopoConvergencePair pair, BTopoControlPoint controlPoint) {
        var name = controlPoint.getName();

        if (!mPlottedLabels.contains(name)) {
            var position = PairHelper.getPosition(controlPoint, mOffset);
            var placemark = new PointPlacemark(position);
            placemark.setAltitudeMode(WorldWind.ABSOLUTE);
            placemark.setAttributes(mAttributeManager.getLabelPlacemarkAttributes());
            placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(mAttributeManager.getLabelPlacemarkAttributes(), 1.5));
            placemark.setLabelText(StringUtils.remove(controlPoint.getName(), pair.getConvergenceGroup().getName()));
            addRenderable(placemark, true, null, sMapObjects);
            mPlottedLabels.add(name);
        }
    }

    private void plotLabelq(BTopoConvergencePair pair, BTopoControlPoint point) {
        var name = point.getName();

        if (!mPlottedLabels.contains(name)) {
            var position = PairHelper.getPosition(point, mOffset);
            var placemark = new PointPlacemark(position);
            placemark.setAltitudeMode(WorldWind.ABSOLUTE);
            placemark.setAttributes(mAttributeManager.getLabelPlacemarkAttributes());
            placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(mAttributeManager.getLabelPlacemarkAttributes(), 1.5));
            placemark.setLabelText(StringUtils.remove(point.getName(), pair.getConvergenceGroup().getName()));
            addRenderable(placemark, true, null, sMapObjects);
            mPlottedLabels.add(name);
        }
    }

    private void plotLabels(BTopoConvergenceGroup convergence) {
        for (var pair : convergence.ext().getPairs()) {

            plotLabel(pair, pair.getP1());
            plotLabel(pair, pair.getP2());
        }
    }

    private void plotLine(BTopoConvergenceGroup convergence) {
        for (var pair : convergence.ext().getPairs()) {
            if (pair.ext().getObservationsTimeFiltered().isEmpty()) {
                continue;
            }

            var pos1 = PairHelper.getPosition(pair.getP1(), mOffset);
            var pos2 = PairHelper.getPosition(pair.getP2(), mOffset);

            var path = new Path(pos1, pos2);
            var attrs = new BasicShapeAttributes(mAttributeManager.getPairPathAttributes());
            Function<BTopoConvergenceObservation, Double> function = BTopoConvergenceObservation.FUNCTION_3D;
//        var delta = pair.getObservations().getLast().getDeltaDeltaDistanceComparedToFirst();
//            int level = pair.getLevel(mMaterials.length);
//            pair.getObservations().getLast();
//            TopoHelper.getAlarmLevel(convergence);
//            int alarmLevel = TopoHelper.getAlarmLevel(pair);
            int alarmLevel = pair.ext().getAlarmLevel(function);

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

    private void plotNode(BTopoControlPoint controlPoint, double offset) {
        var name = controlPoint.getName();
        if (!mPlottedNodes.contains(name)) {
            var radius = PairHelper.NODE_SIZE;
            var position = PairHelper.getPosition(controlPoint, offset);
            var pyramid = new Pyramid(position, radius, radius);
            pyramid.setAttributes(mAttributeManager.getNodeAttributes());
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

    private void plotNodes(BTopoConvergenceGroup convergence) {
        for (var pair : convergence.ext().getPairs()) {

            plotNode(pair.getP1(), mOffset);
            plotNode(pair.getP2(), mOffset);
        }
    }

    private void plotPoints(BTopoConvergenceGroup convergenceGroup, Position position, ArrayList<AVListImpl> mapObjects) {
        var offset = convergenceGroup.ext().getControlPointsWithoutAnchor().stream()
                .map(p -> p.getZeroZ())
                .mapToDouble(Double::doubleValue).min().orElse(0);
        if (offset < 0) {
            offset = offset * -1.0;
        }
        offset += 2;
        var random = new Random();
        for (var controlPoint : convergenceGroup.ext().getControlPointsWithoutAnchor()) {
            var altitude = controlPoint.getZeroZ() + offset;
            var p = Position.fromDegrees(controlPoint.getLat(), controlPoint.getLon(), altitude);
            var radius = 0.6;
            var pyramid = new Pyramid(p, radius * 1.0, radius * 1.0);

            pyramid.setAttributes(mAttributeManager.getNodeAttributes());
//            addRenderable(pyramid, true, GraphicItem.NONE, sMapObjects);

            for (var cp2 : convergenceGroup.ext().getControlPointsWithoutAnchor()) {
                if (cp2 == controlPoint) {
                    continue;
                }
                var altitude2 = cp2.getZeroZ() + offset;
                var p2 = Position.fromDegrees(cp2.getLat(), cp2.getLon(), altitude2);
                var pairPath = new Path(p, p2);
                var attrs = new BasicShapeAttributes(mAttributeManager.getPairPathAttributes());
                int colorIndex = random.nextInt(0, 3);
                switch (colorIndex) {
                    case 0 ->
                        attrs.setOutlineMaterial(Material.YELLOW);
                    case 1 ->
                        attrs.setOutlineMaterial(Material.RED);
                    case 2 ->
                        attrs.setOutlineMaterial(Material.GREEN);
                    case 3 ->
                        attrs.setOutlineMaterial(Material.BLUE);
                    default ->
                        attrs.setOutlineMaterial(Material.MAGENTA);
                }

                if (random.nextBoolean()) {
                    attrs.setOutlineStippleFactor(3);
                }

                pairPath.setAttributes(attrs);
                addRenderable(pairPath, true, null, null);
            }
        }
    }

}
