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
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.api.MOptions;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.controlpoint.BTopoControlPoint;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ComponentRendererTrace extends ComponentRendererBase {

    public ArrayList<AVListImpl> plot(BTopoControlPoint p, Position position) {
        var mapObjects = new ArrayList<AVListImpl>();

        if (sCheckModel.isChecked(RenderComponent.TRACE_1D) && p.getDimension() == BDimension._1d) {
            plot1d(p, position, mapObjects);
        } else if (sCheckModel.isChecked(RenderComponent.TRACE_2D) && p.getDimension() == BDimension._2d) {
            plot2d(p, position, mapObjects);
        } else if (sCheckModel.isChecked(RenderComponent.TRACE_3D) && p.getDimension() == BDimension._3d) {
            plot3d(p, position, mapObjects);
        }

        return mapObjects;
    }

    private void plot1d(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
    }

    private void plot2d(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
    }

    private void plot3d(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
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
        addRenderable(path, true);
        var END_SIZE = 0.25;

        var endEllipsoid = new Ellipsoid(nodes.getLast(), END_SIZE, END_SIZE, END_SIZE);
        addRenderable(endEllipsoid, true);

        var startEllipsoid = new Ellipsoid(nodes.getFirst(), END_SIZE, END_SIZE, END_SIZE);
        addRenderable(startEllipsoid, true);
    }

}
