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
package org.mapton.butterfly_topo.grade.horizontal;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Renderable;
import java.util.ArrayList;
import java.util.HashMap;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_core.api.PlotLimiter;
import org.mapton.butterfly_format.types.topo.BTopoGrade;
import org.mapton.butterfly_topo.grade.GradeAttributeManager;

/**
 *
 * @author Patrik Karlström
 */
public abstract class GradeHRendererBase {

    protected static IndexedCheckModel<GradeHRendererItem> sCheckModel;
    protected static RenderableLayer sInteractiveLayer;
    protected static RenderableLayer sMuteLayer;
    protected static ArrayList<AVListImpl> sMapObjects;
    protected static PlotLimiter sPlotLimiter = new PlotLimiter();
    protected static HashMap<BTopoGrade, Position[]> sPointToPositionMap = new HashMap<>();
    protected final GradeAttributeManager mAttributeManager = GradeAttributeManager.getInstance();

    public GradeHRendererBase() {
        for (var renderItem : GradeHRendererItem.values()) {
            sPlotLimiter.setLimit(renderItem, renderItem.getPlotLimit());
        }
    }

    public void addRenderable(Renderable renderable, boolean interactiveLayer) {
        if (interactiveLayer) {
            sInteractiveLayer.addRenderable(renderable);
            if (renderable instanceof AVListImpl avlist) {
                sMapObjects.add(avlist);
            }
        } else {
            sMuteLayer.addRenderable(renderable);
        }
    }

    /*
    public boolean isValidFor3dPlot(BTopoGrade p) {
        var o1 = p.ext().getObservationsTimeFiltered().getFirst();
        var o2 = p.ext().getObservationsTimeFiltered().getLast();

        return ObjectUtils.allNotNull(p.getZeroX(), p.getZeroY(), p.getZeroZ(), o1.getMeasuredZ(), o2.getMeasuredZ());
    }

    public Position[] plot3dOffsetPole(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        return sPointToPositionMap.computeIfAbsent(p, k -> {
            var ZERO_SIZE = 1.2;
            var CURRENT_SIZE = 1.0;
            var zeroZ = p.getZeroZ();

            var startPosition = WWHelper.positionFromPosition(position, zeroZ + TopoLayerBundle.Z_OFFSET);
            var startEllipsoid = new Ellipsoid(startPosition, ZERO_SIZE, ZERO_SIZE, ZERO_SIZE);
            startEllipsoid.setAttributes(mAttributeManager.getComponentZeroAttributes());

            addRenderable(startEllipsoid, true);

            var groundPath = new Path(position, startPosition);
            groundPath.setAttributes(mAttributeManager.getGroundPathAttributes());
            addRenderable(groundPath, true);

            var currentPosition = startPosition;
//            var o1 = p.ext().getObservationsTimeFiltered().getFirst();
            var o2 = p.ext().getObservationsTimeFiltered().getLast();

            if (o2.ext().getDeltaZ() != null) {
                var x = p.getZeroX() + MathHelper.convertDoubleToDouble(o2.ext().getDeltaX()) * TopoLayerBundle.SCALE_FACTOR;
                var y = p.getZeroY() + MathHelper.convertDoubleToDouble(o2.ext().getDeltaY()) * TopoLayerBundle.SCALE_FACTOR;
                var z = +o2.getMeasuredZ()
                        + TopoLayerBundle.Z_OFFSET
                        + MathHelper.convertDoubleToDouble(o2.ext().getDeltaZ()) * TopoLayerBundle.SCALE_FACTOR;

                var wgs84 = MOptions.getInstance().getMapCooTrans().toWgs84(y, x);
                currentPosition = Position.fromDegrees(wgs84.getY(), wgs84.getX(), z);
            }

            var currentEllipsoid = new Ellipsoid(currentPosition, CURRENT_SIZE, CURRENT_SIZE, CURRENT_SIZE);
            currentEllipsoid.setAttributes(mAttributeManager.getComponentVectorCurrentAttributes(p));
            addRenderable(currentEllipsoid, true);

            return new Position[]{startPosition, currentPosition};
        });
    }
     */
    protected boolean isPlotLimitReached(BTopoGrade p, Object key, Position position) {
        if (sPlotLimiter.isLimitReached(key, p.getName())) {
            //TODO
//            addRenderable(sPlotLimiter.getPlotLimitIndicator(position, p.ext().getObservationsTimeFiltered().isEmpty()), true);
            return true;
        } else {
            return false;
        }
    }
}
