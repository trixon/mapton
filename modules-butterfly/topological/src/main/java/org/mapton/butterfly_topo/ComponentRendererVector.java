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
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.Path;
import java.util.ArrayList;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.worldwind.api.WWHelper;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ComponentRendererVector extends ComponentRendererBase {

    public ArrayList<AVListImpl> plot(BTopoControlPoint p, Position position) {
        var mapObjects = new ArrayList<AVListImpl>();

        if (sCheckModel.isChecked(ComponentRendererItem.VECTOR_1D) && p.getDimension() == BDimension._1d) {
            plot1d(p, position, mapObjects);
        }

        if (sCheckModel.isChecked(ComponentRendererItem.VECTOR_3D) && p.getDimension() == BDimension._3d) {
            plot3d(p, position, mapObjects);
        }

        return mapObjects;
    }

    private void plot1d(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        plot1dVector(p, position, mapObjects);
    }

    private void plot1dVector(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        var ZERO_SIZE = 0.4;
        var CURRENT_SIZE = 1.0;
        var zeroZ = p.getZeroZ();
        if (zeroZ == null) {
            return;
        }
        var zeroPosition = WWHelper.positionFromPosition(position, zeroZ + TopoLayerBundle.Z_OFFSET);
        var zeroEllipsoid = new Ellipsoid(zeroPosition, ZERO_SIZE, ZERO_SIZE, ZERO_SIZE);
        zeroEllipsoid.setAttributes(mAttributeManager.getComponentZeroAttributes());
        addRenderable(zeroEllipsoid, true);

        var currentPosition = zeroPosition;
        var o = p.ext().getObservationsTimeFiltered().getLast();
        var direction = o.ext().getDeltaZ() != null && o.ext().getDeltaZ() < 0 ? -1 : 1;

        if (o.ext().getDeltaZ() != null) {
            var z = +o.getMeasuredZ()
                    + TopoLayerBundle.Z_OFFSET
                    + MathHelper.convertDoubleToDouble(o.ext().getDeltaZ()) * TopoLayerBundle.SCALE_FACTOR_Z;

            currentPosition = WWHelper.positionFromPosition(currentPosition, z);
        }

        var currentEllipsoid = new Ellipsoid(currentPosition, CURRENT_SIZE, CURRENT_SIZE, CURRENT_SIZE);
        currentEllipsoid.setAttributes(mAttributeManager.getComponentCurrentAttributes(p));
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
