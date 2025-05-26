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
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.TopoLayerBundle;
import static org.mapton.butterfly_topo.graphics.GraphicRendererBase.sCheckModel;
import static org.mapton.butterfly_topo.graphics.GraphicRendererBase.sMapObjects;
import org.mapton.worldwind.api.WWHelper;
import se.trixon.almond.nbp.Almond;
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
        p.setValue("position", position);
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
            var positions = plot3dOffsetPole(p, p.getValue("position"), 1.0, true);
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
            var points = cluster.getPoints();
            Collections.sort(points, Comparator.comparing(BTopoControlPoint::getZeroZ));
            var pathPositions = new ArrayList<Position>();
            var lastP = points.getLast();
            Position firstPosition = points.getLast().getValue("position");

            for (int i = 0; i < points.size(); i++) {
                var p = points.get(i);
                if (i == 0) {
                    var startPosition = WWHelper.positionFromPosition(firstPosition, 0);
                    var endPosition = WWHelper.positionFromPosition(firstPosition, lastP.getZeroZ() + TopoLayerBundle.getZOffset());
                    var groundPath = new Path(startPosition, endPosition);
                    groundPath.setAttributes(mAttributeManager.getComponentGroundPathAttributes());
                    addRenderable(groundPath, true, null, sMapObjects);
                }

                var positions = plot3dOffsetPole(p, p.getValue("position"), false, 1.0, true);
                var altitude = p.getZeroZ() + TopoLayerBundle.getZOffset();
                var startPosition = WWHelper.positionFromPosition(firstPosition, altitude);
                var endPosition = WWHelper.positionFromPosition(positions[1], altitude);
                pathPositions.add(endPosition);

                var path = new Path(startPosition, endPosition);
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

                var d2 = p.ext().deltaZero().getDelta2();
                if (d2 != null) {
                    var scaleStep = 0.005;
                    var r = 0.05;
                    var bearing = WWHelper.latLonFromPosition(startPosition).getBearing(WWHelper.latLonFromPosition(endPosition));
                    for (double j = scaleStep; j < d2; j += scaleStep) {
                        var rulerPosition = WWHelper.movePolar(startPosition, bearing, j * 500, endPosition.getAltitude());
                        var ellipsoid = new Ellipsoid(rulerPosition, r, r, r);
                        ellipsoid.setAttributes(mAttributeManager.getSymbolAttributes(p));
                        addRenderable(ellipsoid, false, null, null);
                    }
                }

                plotLabel(p, positions[0]);
            }

            var path = new Path(pathPositions);
            path.setAttributes(mAttributeManager.getBearingAttribute(true));
            addRenderable(path, false, null, sMapObjects);
        }
    }
}
