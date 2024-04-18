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
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.Path;
import java.util.ArrayList;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.worldwind.api.WWHelper;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRendererVector extends GraphicRendererBase {

    public ArrayList<AVListImpl> plot(BTopoControlPoint p, Position position) {
        var mapObjects = new ArrayList<AVListImpl>();
        var dimension = p.getDimension();

        if (sCheckModel.isChecked(GraphicRendererItem.VECTOR_1D) && (dimension == BDimension._1d || dimension == BDimension._3d)) {
            plot1d(p, position, mapObjects);
        }

        if (sCheckModel.isChecked(GraphicRendererItem.VECTOR_1D_ALARM) && (dimension == BDimension._1d || dimension == BDimension._3d)) {
            plot1dVectorAlarm(p, position, mapObjects);
        }

        if (sCheckModel.isChecked(GraphicRendererItem.VECTOR_3D) && dimension == BDimension._3d) {
            plot3d(p, position, mapObjects);
        }

        return mapObjects;
    }

    private void plot1d(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        plot1dVector(p, position, mapObjects);
    }

    private void plot1dVector(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        var zeroZ = p.getZeroZ();
        if (zeroZ == null) {
            return;
        }
        var ZERO_SIZE = 0.4;
        var CURRENT_SIZE = 1.0;
        var zeroPosition = WWHelper.positionFromPosition(position, zeroZ + TopoLayerBundle.getZOffset());
        var zeroEllipsoid = new Ellipsoid(zeroPosition, ZERO_SIZE, ZERO_SIZE, ZERO_SIZE);
        zeroEllipsoid.setAttributes(mAttributeManager.getComponentZeroAttributes());
        addRenderable(zeroEllipsoid, true);

        var currentPosition = zeroPosition;
        var o = p.ext().getObservationsTimeFiltered().getLast();
        var direction = o.ext().getDeltaZ() != null && o.ext().getDeltaZ() < 0 ? -1 : 1;

        if (o.ext().getDeltaZ() != null) {
            var z = zeroZ
                    + TopoLayerBundle.getZOffset()
                    + MathHelper.convertDoubleToDouble(o.ext().getDeltaZ()) * TopoLayerBundle.SCALE_FACTOR_Z;

            currentPosition = WWHelper.positionFromPosition(currentPosition, z);
        }

        var currentEllipsoid = new Ellipsoid(currentPosition, CURRENT_SIZE, CURRENT_SIZE, CURRENT_SIZE);
        currentEllipsoid.setAttributes(mAttributeManager.getComponentVectorCurrentAttributes(p));
        addRenderable(currentEllipsoid, true);

        var currentPositionTop = WWHelper.positionFromPosition(currentPosition, currentPosition.getAltitude() - CURRENT_SIZE * direction);
        var deltaPath = new Path(zeroPosition, currentPositionTop);
        deltaPath.setAttributes(mAttributeManager.getComponentGroundPathAttributes());
        addRenderable(deltaPath, true);

        var currentPositionBottom = WWHelper.positionFromPosition(currentPosition, currentPosition.getAltitude() + CURRENT_SIZE * direction);
        var groundPath = new Path(position, currentPositionBottom);
        groundPath.setAttributes(mAttributeManager.getComponentGroundPathAttributes());
        addRenderable(groundPath, true);
    }

    private void plot1dVectorAlarm(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        var zeroZ = p.getZeroZ();
        var alarm = p.ext().getAlarm(BComponent.HEIGHT);

        if (ObjectUtils.anyNull(alarm, zeroZ) || TopoHelper.getAlarmLevelHeight(p) < 0) {
            return;
        }

        var min0 = alarm.ext().getRange0().getMinimum();
        var max0 = alarm.ext().getRange0().getMaximum();
        var span0 = max0 - min0;
        var scaledSpan0 = span0 * TopoLayerBundle.SCALE_FACTOR_Z;
        var nonSymmetricAdjustment = span0 / 2 + min0;

        var z0 = zeroZ + TopoLayerBundle.getZOffset() + nonSymmetricAdjustment * TopoLayerBundle.SCALE_FACTOR_Z;
        var zeroPosition = WWHelper.positionFromPosition(position, z0);
        var radius = 0.5;
        var cylinder0 = new Cylinder(zeroPosition, radius, scaledSpan0 * 0.5, radius);
        cylinder0.setAttributes(mAttributeManager.getComponentVectorAlarmAttributes(0));
        addRenderable(cylinder0, true);

        if (TopoHelper.getAlarmLevelHeight(p) > 0) {
            var attrs = mAttributeManager.getComponentVectorAlarmAttributes(1);

            //Plot bottom cylinder
            var bottomLower = alarm.ext().getRange1().getMinimum();
            var bottomUpper = min0;
            var spanBottom = bottomUpper - bottomLower;
            var scaledSpanBottom = spanBottom * TopoLayerBundle.SCALE_FACTOR_Z;
            var nonSymmetricAdjustmentBottom = spanBottom / 2 + bottomLower;
            var zBottom = zeroZ + TopoLayerBundle.getZOffset() + nonSymmetricAdjustmentBottom * TopoLayerBundle.SCALE_FACTOR_Z;
            var bottomPosition = WWHelper.positionFromPosition(position, zBottom);
            var cylinderBottom = new Cylinder(bottomPosition, radius, scaledSpanBottom * 0.5, radius);
            cylinderBottom.setAttributes(attrs);
            addRenderable(cylinderBottom, true);

            //Plot top cylinder
            var topLower = max0;
            var topUpper = alarm.ext().getRange1().getMaximum();
            var spanTop = topUpper - topLower;
            var scaledSpanTop = spanTop * TopoLayerBundle.SCALE_FACTOR_Z;
            var nonSymmetricAdjustmentTop = spanTop / 2 + topLower;
            var zTop = zeroZ + TopoLayerBundle.getZOffset() + nonSymmetricAdjustmentTop * TopoLayerBundle.SCALE_FACTOR_Z;
            var topPosition = WWHelper.positionFromPosition(position, zTop);
            var cylinderTop = new Cylinder(topPosition, radius, scaledSpanTop * 0.5, radius);
            cylinderTop.setAttributes(attrs);
            addRenderable(cylinderTop, true);
        }
    }

    private void plot2d(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
    }

    private void plot3d(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        if (!isValidFor3dPlot(p)) {
            return;
        }

        var positions = plot3dOffsetPole(p, position, mapObjects);
        var startPosition = positions[0];
        var endPosition = positions[1];

        var path = new Path(startPosition, endPosition);
        path.setAttributes(mAttributeManager.getComponentVector3dAttributes(p));
        addRenderable(path, true);

        //plot dZ
        var endDeltaZ = Position.fromDegrees(startPosition.latitude.degrees, startPosition.longitude.degrees, endPosition.getAltitude());
        var pathDeltaZ = new Path(startPosition, endDeltaZ);
        pathDeltaZ.setAttributes(mAttributeManager.getComponentVector1dAttributes(p));
        addRenderable(pathDeltaZ, true);

        //plot dR
        var pathDeltaR = new Path(endDeltaZ, endPosition);
        pathDeltaR.setAttributes(mAttributeManager.getComponentVector2dAttributes(p));
        addRenderable(pathDeltaR, true);
    }

}
