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

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.lang3.ObjectUtils;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.MOptions;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.controlpoint.BTopoControlPoint;
import org.mapton.worldwind.api.WWHelper;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ComponentRenderer {

    private final HashMap<BTopoControlPoint, Position[]> mPointToPositionMap = new HashMap<>();
    private final RenderableLayer mLayer;
    private final IndexedCheckModel<RenderComponent> mCheckModel;
    private final TopoAttributeManager mAttributeManager = TopoAttributeManager.getInstance();

    public ComponentRenderer(RenderableLayer layer, IndexedCheckModel<RenderComponent> checkModel) {
        mLayer = layer;
        mCheckModel = checkModel;
    }

    public void reset() {
        mPointToPositionMap.clear();
    }

    public void plot(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        mapObjects.addAll(plotBearing(p, position));

        if (p.ext().getNumOfObservationsFiltered() > 1) {
            mapObjects.addAll(plotTrace(p, position));
            mapObjects.addAll(plotVector(p, position));
        }

    }

    private ArrayList<AVListImpl> plotBearing(BTopoControlPoint p, Position position) {
        var mapObjects = new ArrayList<AVListImpl>();
        int size = p.ext().getObservationsFiltered().size();
        if (!mCheckModel.isChecked(RenderComponent.BEARING)
                || p.getDimension() == BDimension._1d
                || p.ext().getNumOfObservationsFiltered() == 0) {
            return mapObjects;
        }

        int maxNumberOfItemsToPlot = Math.min(10, p.ext().getNumOfObservationsFiltered());

        boolean first = true;
        for (int i = size - 1; i >= size - maxNumberOfItemsToPlot + 1; i--) {
            var o = p.ext().getObservationsFiltered().get(i);

            try {
                var bearing = o.ext().getBearing();
                if (bearing == null || bearing.isNaN()) {
                    continue;
                }

                var length = 10.0;
                var p2 = WWHelper.movePolar(position, bearing, length);
                var z = first ? 0.2 : 0.1;
                position = WWHelper.positionFromPosition(position, z);
                p2 = WWHelper.positionFromPosition(p2, z);
                var path = new Path(position, p2);
                var sa = new BasicShapeAttributes();
                sa.setOutlineMaterial(Material.BLUE);
                path.setAttributes(sa);

                if (first) {
                    first = false;
                    sa.setOutlineWidth(2.0);
                } else {
                    sa.setOutlineWidth(4.0);
                    sa.setOutlineOpacity(0.05);
                }

                mLayer.addRenderable(path);
                mapObjects.add(path);
            } catch (Exception e) {
                System.err.println(e);
            }
        }

        return mapObjects;
    }

    private ArrayList<AVListImpl> plotTrace(BTopoControlPoint p, Position position) {
        var mapObjects = new ArrayList<AVListImpl>();

        if (mCheckModel.isChecked(RenderComponent.TRACE_1D) && p.getDimension() == BDimension._1d) {
            plotTrace1d(p, position, mapObjects);
        } else if (mCheckModel.isChecked(RenderComponent.TRACE_2D) && p.getDimension() == BDimension._2d) {
            plotTrace2d(p, position, mapObjects);
        } else if (mCheckModel.isChecked(RenderComponent.TRACE_3D) && p.getDimension() == BDimension._3d) {
            plotTrace3d(p, position, mapObjects);
        }

        return mapObjects;
    }

    private void plotTrace1d(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
    }

    private void plotTrace2d(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
    }

    private Position[] plot3dOffsetPole(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        return mPointToPositionMap.computeIfAbsent(p, k -> {
            var ZERO_SIZE = 0.1;
            var END_SIZE = 0.5;

            var startPosition = WWHelper.positionFromPosition(position, TopoLayerBundle.Z_OFFSET);
            var startEllipsoid = new Ellipsoid(startPosition, ZERO_SIZE, ZERO_SIZE, ZERO_SIZE);
            mapObjects.add(startEllipsoid);
            mLayer.addRenderable(startEllipsoid);

            var groundPath = new Path(position, startPosition);
            mapObjects.add(groundPath);
            mLayer.addRenderable(groundPath);
            var endPosition = startPosition;
//            var o1 = p.ext().getObservationsFiltered().getFirst();
            var o2 = p.ext().getObservationsFiltered().getLast();

            if (o2.ext().getDeltaZ() != null) {
                var x = p.getZeroX() + MathHelper.convertDoubleToDouble(o2.ext().getDeltaX()) * TopoLayerBundle.SCALE_FACTOR;
                var y = p.getZeroY() + MathHelper.convertDoubleToDouble(o2.ext().getDeltaY()) * TopoLayerBundle.SCALE_FACTOR;
                var z = p.getZeroZ()
                        + o2.getMeasuredZ()
                        + TopoLayerBundle.Z_OFFSET
                        + MathHelper.convertDoubleToDouble(o2.ext().getDeltaZ()) * TopoLayerBundle.SCALE_FACTOR;

                var wgs84 = MOptions.getInstance().getMapCooTrans().toWgs84(y, x);
                endPosition = Position.fromDegrees(wgs84.getY(), wgs84.getX(), z);
            }

            var endEllipsoid = new Ellipsoid(endPosition, END_SIZE, END_SIZE, END_SIZE);
            var sa = new BasicShapeAttributes();
            sa.setInteriorMaterial(TopoHelper.getAlarmMaterial(p));
            endEllipsoid.setAttributes(sa);
            mapObjects.add(endEllipsoid);
            mLayer.addRenderable(endEllipsoid);

            return new Position[]{startPosition, endPosition};
        });
    }

    private void plotTrace3d(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        if (!isValidForTraceVector3dPlot(p)) {
            return;
        }

        var positions = plot3dOffsetPole(p, position, mapObjects);
        if (ObjectUtils.anyNull(p.getZeroX(), p.getZeroY(), p.getZeroZ())) {
            return;
        }
        var o1 = p.ext().getObservationsFiltered().getFirst();

        var collectedNodes = p.ext().getObservationsFiltered().stream()
                .map(o -> {
                    var x = o1.getMeasuredX() + MathHelper.convertDoubleToDouble(o.ext().getDeltaX()) * TopoLayerBundle.SCALE_FACTOR;
                    var y = o1.getMeasuredY() + MathHelper.convertDoubleToDouble(o.ext().getDeltaY()) * TopoLayerBundle.SCALE_FACTOR;
                    var z = o1.getMeasuredZ()
                            + MathHelper.convertDoubleToDouble(o.ext().getDeltaZ()) * TopoLayerBundle.SCALE_FACTOR
                            + TopoLayerBundle.Z_OFFSET;
//                var z = o1.getMeasuredZ()
//                        + o2.getMeasuredZ()
//                        + TopoLayerBundle.Z_OFFSET
//                        + MathHelper.convertDoubleToDouble(o2.ext().getDeltaZ()) * TopoLayerBundle.SCALE_FACTOR;

                    var wgs84 = MOptions.getInstance().getMapCooTrans().toWgs84(y, x);
                    var p0 = Position.fromDegrees(wgs84.getY(), wgs84.getX(), z);

                    return p0;
                }).toList();

        var nodes = new ArrayList<Position>(collectedNodes);
//        nodes.add(0, positions[0]);
        var path = new Path(nodes);
        path.setShowPositions(true);
        mapObjects.add(path);
        mLayer.addRenderable(path);
        var END_SIZE = 0.25;

        var endEllipsoid = new Ellipsoid(nodes.getLast(), END_SIZE, END_SIZE, END_SIZE);
        mapObjects.add(endEllipsoid);
        mLayer.addRenderable(endEllipsoid);

        var startEllipsoid = new Ellipsoid(nodes.getFirst(), END_SIZE, END_SIZE, END_SIZE);
        mapObjects.add(startEllipsoid);
        mLayer.addRenderable(startEllipsoid);

    }

    private ArrayList<AVListImpl> plotVector(BTopoControlPoint p, Position position) {
        var mapObjects = new ArrayList<AVListImpl>();

        if (mCheckModel.isChecked(RenderComponent.VECTOR_1D) && p.getDimension() == BDimension._1d) {
            plotVector1d(p, position, mapObjects);
        } else if (mCheckModel.isChecked(RenderComponent.VECTOR_2D) && p.getDimension() == BDimension._2d) {
            plotVector2d(p, position, mapObjects);
        } else if (mCheckModel.isChecked(RenderComponent.VECTOR_3D) && p.getDimension() == BDimension._3d) {
            plotVector3d(p, position, mapObjects);
        }

        return mapObjects;
    }

    private void plotVector1d(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
    }

    private void plotVector2d(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
    }

    private boolean isValidForTraceVector3dPlot(BTopoControlPoint p) {
        var o1 = p.ext().getObservationsFiltered().getFirst();
        var o2 = p.ext().getObservationsFiltered().getLast();

        return ObjectUtils.allNotNull(o1.getMeasuredZ());
    }

    private void plotVector3d(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        if (!isValidForTraceVector3dPlot(p)) {
            return;
        }
        var positions = plot3dOffsetPole(p, position, mapObjects);
        var startPosition = positions[0];
        var endPosition = positions[1];

        var path = new Path(startPosition, endPosition);
        mapObjects.add(path);
        mLayer.addRenderable(path);

        //plot dZ
        var endDeltaZ = Position.fromDegrees(startPosition.latitude.degrees, startPosition.longitude.degrees, endPosition.getAltitude());
        var pathDeltaZ = new Path(startPosition, endDeltaZ);
        var sa = new BasicShapeAttributes();
        sa.setDrawOutline(true);
        sa.setOutlineWidth(4);
        pathDeltaZ.setAttributes(sa);
        mapObjects.add(pathDeltaZ);
        mLayer.addRenderable(pathDeltaZ);

        //plot dR
        var pathDeltaR = new Path(endDeltaZ, endPosition);
        var sar = new BasicShapeAttributes();
        sar.setDrawOutline(true);
        sar.setOutlineWidth(4);
        pathDeltaR.setAttributes(sar);
        mapObjects.add(pathDeltaR);
        mLayer.addRenderable(pathDeltaR);
    }

}
