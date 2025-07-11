/*
 * Copyright 2025 Patrik Karlström.
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
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.Polygon;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.mapton.api.MOptions;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.TopoLayerBundle;
import static org.mapton.butterfly_topo.graphics.GraphicRendererBase.sCheckModel;
import static org.mapton.butterfly_topo.graphics.GraphicRendererBase.sMapObjects;
import org.mapton.worldwind.api.WWHelper;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.util.MathHelper;
import se.trixon.almond.util.ext.GrahamScan;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRendererGroup extends GraphicRendererBase {

    private final DistanceMeasure mDistanceMeasure;
    private final HashSet<BTopoControlPoint> mPoints = new HashSet<>();

    public GraphicRendererGroup(RenderableLayer layer, RenderableLayer passiveLayer) {
        super(layer, passiveLayer);
        mDistanceMeasure = (DistanceMeasure) (double[] a, double[] b) -> {
            var plane = Math.hypot(b[1] - a[1], b[0] - a[0]);
            var height = Math.abs(b[2] - a[2]);
            if (height < 1.0) {
                return Double.MAX_VALUE;
            } else {
                return plane;
            }
        };
    }

    public void plot(BTopoControlPoint p, Position position) {
        initScales();
        p.setValue("position", new Position(position.latitude, position.longitude, position.getElevation()));
        mPoints.add(p);
    }

    @Override
    public void postPlot() {
        if (sCheckModel.isChecked(GraphicItem.CLUSTER_DEFORMATION) && mPoints.size() > 2) {
            plotDeformation();
        }

        if (sCheckModel.isChecked(GraphicItem.CLUSTER_DEFORMATION_PLANE_ALTITUDES) && mPoints.size() > 1) {
            plotDeformationPlaneAltitudes();
        }
    }

    @Override
    public void reset() {
        mPoints.clear();
    }

    private void plotDeformation() {
        var coordinates = mPoints.stream()
                .map(p -> new Point2D.Double(p.getLon(), p.getLat()))
                .toList();

        List<Point.Double> convexHullCoordinates;

        try {
            convexHullCoordinates = GrahamScan.getConvexHullDouble(coordinates);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            return;
        }

        var points = convexHullCoordinates.stream().map(coordinate -> {
            for (var p : mPoints) {
                if (new Point2D.Double(p.getLon(), p.getLat()).distance(coordinate) < 0.000001) {
                    return p;
                }
            }
            throw new IllegalArgumentException("Should not reach this point");
        }).toList();

        var startPositions = new ArrayList<Position>();
        var endPositions = new ArrayList<Position>();

        for (var p : points) {
            var positions = plot3dOffsetPoleNoCache(p, p.getValue("position"), true, 1.0, true);
            startPositions.add(positions[0]);
            endPositions.add(positions[1]);
        }

        var polygon1 = new Polygon(startPositions);
        var polygon2 = new Polygon(endPositions);

        var attr1 = new BasicShapeAttributes();
        attr1.setDrawInterior(false);
        attr1.setDrawOutline(true);
        attr1.setOutlineWidth(6.0);
        attr1.setOutlineMaterial(Material.CYAN);

        var attr2 = new BasicShapeAttributes(attr1);
        attr2.setInteriorMaterial(Material.BLUE);
        attr2.setOutlineMaterial(Material.BLUE);

        polygon1.setAttributes(attr1);
        polygon2.setAttributes(attr2);

        addRenderable(polygon1, false, null, null);
        addRenderable(polygon2, false, null, null);
    }

    private void plotDeformationPlaneAltitudes() {
        var epsilon = 2.5;
        var minPoints = 1;
        var dbscan = new DBSCANClusterer<BTopoControlPoint>(epsilon, minPoints, mDistanceMeasure);
        var filteredPoints = mPoints.stream()
                .filter(p -> p.getDimension() == BDimension._3d)
                .filter(p -> ObjectUtils.allNotNull(p.getZeroX(), p.getZeroY(), p.getZeroZ()))
                .toList();

        if (filteredPoints.isEmpty()) {
            return;
        }

        //Calculate and subtract min on order to use dbscan.
        var minX = filteredPoints.stream().mapToDouble(p -> p.getZeroX()).min().getAsDouble();
        var minY = filteredPoints.stream().mapToDouble(p -> p.getZeroY()).min().getAsDouble();
        var minZ = filteredPoints.stream().mapToDouble(p -> p.getZeroZ()).min().getAsDouble();

        filteredPoints.forEach(p -> {
            p.setZeroXScaled(p.getZeroX() - minX);
            p.setZeroYScaled(p.getZeroY() - minY);
            p.setZeroZScaled(p.getZeroZ() - minZ);
        });

        var clusters = dbscan.cluster(filteredPoints);
        var labelAttributes = mAttributeManager.getLabelPlacemarkAttributes();
        labelAttributes.setLabelOffset(Offset.CENTER);

        for (var cluster : clusters) {
            var points = new ArrayList<>(cluster.getPoints());
            var anchorPositions = new ArrayList<Position>();

            //Calculate cirtual center point for each cluster
            var vcX = points.stream().mapToDouble(p -> p.getZeroX()).sum() / points.size();
            var vcY = points.stream().mapToDouble(p -> p.getZeroY()).sum() / points.size();
            var vcZ = points.stream().mapToDouble(p -> p.getZeroZ()).sum() / points.size();
            var vcWgs = MOptions.getInstance().getMapCooTrans().toWgs84(vcY, vcX);
            var virtualCenterPos = Position.fromDegrees(vcWgs.getY(), vcWgs.getX());

            Collections.sort(points, Comparator.comparing(BTopoControlPoint::getZeroZ));
            var pathPositions = new ArrayList<Position>();

            for (int i = 0; i < points.size(); i++) {
                var p = points.get(i);

                //Plot vertical ground path once from the virtual center
                if (i == 0) {
                    var topMostZ = points.getLast().getZeroZ();
                    var startPosition = WWHelper.positionFromPosition(virtualCenterPos, 0);
                    var endPosition = WWHelper.positionFromPosition(virtualCenterPos, topMostZ + TopoLayerBundle.getZOffset());
                    var groundPath = new Path(startPosition, endPosition);
                    groundPath.setAttributes(mAttributeManager.getComponentGroundPathAttributes());
                    addRenderable(groundPath, true, null, sMapObjects);
                }

                var altitude = p.getZeroZ() + TopoLayerBundle.getZOffset();
                var mapStartPosition = WWHelper.positionFromPosition(virtualCenterPos, altitude);
                var mapEndPosition = mapStartPosition;
                var o2 = p.ext().getObservationsTimeFiltered().getLast();
                anchorPositions.add(mapStartPosition);

                if (o2.ext().getDeltaZ() != null) {
                    var virtualDiffX = p.getZeroX() - vcX;
                    var virtualDiffY = p.getZeroY() - vcY;
//                    var virtualDiffZ = p.getZeroZ() - vcZ;
                    var x = -virtualDiffX + p.getZeroX() + MathHelper.convertDoubleToDouble(o2.ext().getDeltaX()) * mScale3dP;
                    var y = -virtualDiffY + p.getZeroY() + MathHelper.convertDoubleToDouble(o2.ext().getDeltaY()) * mScale3dP;

                    var wgs84 = MOptions.getInstance().getMapCooTrans().toWgs84(y, x);
                    mapEndPosition = Position.fromDegrees(wgs84.getY(), wgs84.getX(), altitude);
                }

                pathPositions.add(mapEndPosition);

                var path = new Path(mapStartPosition, mapEndPosition);
                path.setAttributes(mAttributeManager.getComponentVector2dAttributes(p));
                addRenderable(path, true, null, sMapObjects);
                var leftClickRunnable = (Runnable) () -> {
                    mManager.setSelectedItemAfterReset(p);
                };

                var leftDoubleClickRunnable = (Runnable) () -> {
                    Almond.openAndActivateTopComponent((String) getInteractiveLayer().getValue(WWHelper.KEY_FAST_OPEN));
                };

                path.setValue(WWHelper.KEY_RUNNABLE_LEFT_CLICK, leftClickRunnable);
                path.setValue(WWHelper.KEY_RUNNABLE_LEFT_DOUBLE_CLICK, leftDoubleClickRunnable);

                //Plot ruler and label
                var d2 = p.ext().deltaZero().getDelta2();
                if (d2 != null) {
                    var scaleStep = 0.001;
                    var r = 0.05;
                    var bearing = WWHelper.latLonFromPosition(mapStartPosition).getBearing(WWHelper.latLonFromPosition(mapEndPosition));
                    for (double j = scaleStep; j < d2; j += scaleStep) {
                        var rulerPosition = WWHelper.movePolar(mapStartPosition, bearing, j * mScale3dP, mapEndPosition.getAltitude());
                        var ellipsoid = new Ellipsoid(rulerPosition, r, r, r);
                        ellipsoid.setAttributes(mAttributeManager.getSymbolAttributes(p));
                        addRenderable(ellipsoid, false, null, null);
                    }

                    if (sCheckModel.isChecked(GraphicItem.CLUSTER_DEFORMATION_PLANE_ALTITUDES_LABEL)) {
                        var midPos = WWHelper.movePolar(mapStartPosition, bearing, d2 * .5);
                        plotLabel(midPos, "TODO");
                    }
                }
            }

            var path = new Path(pathPositions);
            path.setAttributes(mAttributeManager.getBearingAttribute(true));
            addRenderable(path, false, null, sMapObjects);

            var flagPositions = new ArrayList<Position>(pathPositions);
            flagPositions.addAll(anchorPositions.reversed());
            var flagPolygon = new Polygon(flagPositions);
            var attrs = new BasicShapeAttributes();
            attrs.setInteriorMaterial(Material.MAGENTA);
            attrs.setInteriorOpacity(0.5);
            attrs.setOutlineWidth(8);
            attrs.setOutlineMaterial(Material.BLACK);
//            attrs.setDrawOutline(false);
            flagPolygon.setAttributes(attrs);

            addRenderable(flagPolygon, false, null, sMapObjects);
        }
    }
}
