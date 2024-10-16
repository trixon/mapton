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
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.Renderable;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.lang3.ObjectUtils;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.MOptions;
import org.mapton.api.MSimpleObjectStorageManager;
import org.mapton.butterfly_core.api.PlotLimiter;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.api.TopoManager;
import org.mapton.butterfly_topo.sos.ScalePlot3dHSosd;
import org.mapton.butterfly_topo.sos.ScalePlot3dPSosd;
import org.mapton.worldwind.api.WWHelper;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class GraphicRendererBase {

    protected static IndexedCheckModel<GraphicRendererItem> sCheckModel;
    protected static RenderableLayer sInteractiveLayer;
    protected static ArrayList<AVListImpl> sMapObjects;
    protected static PlotLimiter sPlotLimiter = new PlotLimiter();
    protected static HashMap<BTopoControlPoint, Position[]> sPointToPositionMap = new HashMap<>();
    protected final TopoAttributeManager mAttributeManager = TopoAttributeManager.getInstance();
    protected final TopoManager mManager = TopoManager.getInstance();

    public GraphicRendererBase() {
        for (var renderItem : GraphicRendererItem.values()) {
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
            //mLayerXYZ.addRenderable(renderable); //TODO Add to a non responsive layer
        }
    }

    public boolean isValidFor3dPlot(BTopoControlPoint p) {
        var o1 = p.ext().getObservationsTimeFiltered().getFirst();
        var o2 = p.ext().getObservationsTimeFiltered().getLast();

        return ObjectUtils.allNotNull(p.getZeroX(), p.getZeroY(), p.getZeroZ(), o1.getMeasuredZ(), o2.getMeasuredZ());
    }

    public Position[] plot3dOffsetPole(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        var scale3dH = MSimpleObjectStorageManager.getInstance().getInteger(ScalePlot3dHSosd.class, 500);
        var scale3dP = MSimpleObjectStorageManager.getInstance().getInteger(ScalePlot3dPSosd.class, 500);

        return sPointToPositionMap.computeIfAbsent(p, k -> {
            var ZERO_SIZE = 1.2;
            var CURRENT_SIZE = 1.0;
            var zeroZ = p.getZeroZ();

            var startPosition = WWHelper.positionFromPosition(position, zeroZ + TopoLayerBundle.getZOffset());
            var startEllipsoid = new Ellipsoid(startPosition, ZERO_SIZE, ZERO_SIZE, ZERO_SIZE);
            startEllipsoid.setAttributes(mAttributeManager.getComponentZeroAttributes());

            addRenderable(startEllipsoid, true);

            var groundPath = new Path(position, startPosition);
            groundPath.setAttributes(mAttributeManager.getComponentGroundPathAttributes());
            addRenderable(groundPath, true);

            var currentPosition = startPosition;
//            var o1 = p.ext().getObservationsTimeFiltered().getFirst();
            var o2 = p.ext().getObservationsTimeFiltered().getLast();

            if (o2.ext().getDeltaZ() != null) {
                var x = p.getZeroX() + MathHelper.convertDoubleToDouble(o2.ext().getDeltaX()) * scale3dP;
                var y = p.getZeroY() + MathHelper.convertDoubleToDouble(o2.ext().getDeltaY()) * scale3dP;
                var z = +o2.getMeasuredZ()
                        + TopoLayerBundle.getZOffset()
                        + MathHelper.convertDoubleToDouble(o2.ext().getDeltaZ()) * scale3dH;

                var wgs84 = MOptions.getInstance().getMapCooTrans().toWgs84(y, x);
                currentPosition = Position.fromDegrees(wgs84.getY(), wgs84.getX(), z);
            }

            var currentEllipsoid = new Ellipsoid(currentPosition, CURRENT_SIZE, CURRENT_SIZE, CURRENT_SIZE);
            currentEllipsoid.setAttributes(mAttributeManager.getComponentVectorCurrentAttributes(p));
            addRenderable(currentEllipsoid, true);

            return new Position[]{startPosition, currentPosition};
        });
    }

    protected boolean isPlotLimitReached(BTopoControlPoint p, Object key, Position position) {
        if (sPlotLimiter.isLimitReached(key, p.getName())) {
            addRenderable(sPlotLimiter.getPlotLimitIndicator(position, p.ext().getObservationsTimeFiltered().isEmpty()), true);
            return true;
        } else {
            return false;
        }
    }
}
