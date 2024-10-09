/*
 * Copyright 2024 Patrik Karlström.
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
package org.mapton.butterfly_monmon;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.Path;
import java.util.ArrayList;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.MLatLon;
import org.mapton.butterfly_format.types.monmon.BMonmon;
import org.mapton.butterfly_topo.api.TopoManager;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer {

    private final MonAttributeManager mAttributeManager = MonAttributeManager.getInstance();
    private final IndexedCheckModel<GraphicRendererItem> mCheckModel;
    private final RenderableLayer mLayer;

    public GraphicRenderer(RenderableLayer layer, IndexedCheckModel<GraphicRendererItem> checkModel) {
        mLayer = layer;
        mCheckModel = checkModel;
    }

    public void plot(BMonmon mon, Position position, int stationIndex, ArrayList<AVListImpl> mapObjects) {
        mapObjects.add(plotGroundConnector(mon, 0.0));
        if (mon.isChild()) {
            if (null != TopoManager.getInstance().getAllItemsMap().get(mon.getStationName())) {
                mapObjects.add(plotStationConnector(mon, stationIndex));
            }

            boolean checked1 = mCheckModel.isChecked(GraphicRendererItem.LATEST_1);
            boolean checked7 = mCheckModel.isChecked(GraphicRendererItem.LATEST_7);
            boolean checked14 = mCheckModel.isChecked(GraphicRendererItem.LATEST_14);

            if (checked1) {
                mapObjects.add(plotStatus(mon, 1, -1));
            }
            if (checked7) {
                mapObjects.add(plotStatus(mon, 7, 0));
            }
            if (checked14) {
                mapObjects.add(plotStatus(mon, 14, 1));
            }
        }
    }

    private AVListImpl plotGroundConnector(BMonmon mon, double groundZ) {
        var p0 = WWHelper.positionFromLatLon(new MLatLon(mon.getLat(), mon.getLon()), groundZ);
        var p1 = WWHelper.positionFromLatLon(new MLatLon(mon.getLat(), mon.getLon()), mon.getControlPoint().getZeroZ());
        var path = new Path(p0, p1);
        path.setAttributes(mAttributeManager.getGroundConnectorAttributes());
        mLayer.addRenderable(path);

        return path;
    }

    private AVListImpl plotStationConnector(BMonmon mon, int stationIndex) {
        var stationName = mon.getStationName();
        var p = mon.getControlPoint();
        var s = TopoManager.getInstance().getAllItemsMap().get(stationName);
        var p0 = WWHelper.positionFromLatLon(new MLatLon(s.getLat(), s.getLon()), s.getZeroZ());
        var p1 = WWHelper.positionFromLatLon(new MLatLon(mon.getLat(), mon.getLon()), p.getZeroZ());
        var path = new Path(p0, p1);
        path.setAttributes(mAttributeManager.getStationConnectorAttribute(stationIndex));
        mLayer.addRenderable(path);

        double size = 0.2;
        var ellipsoid = new Ellipsoid(p1, size, size, size);
        ellipsoid.setAttributes(mAttributeManager.getStationConnectorEllipsoidAttributes());

        mLayer.addRenderable(ellipsoid);

        return path;
    }

    private AVListImpl plotStatus(BMonmon mon, int index, int order) {
        var size = 1.0;
        var z = mon.getControlPoint().getZeroZ() + size * 2 * order;
        var latLon = new MLatLon(mon.getLat(), mon.getLon());
        var p = WWHelper.positionFromLatLon(latLon, z);
        var ellipsoid = new Ellipsoid(p, size, size, size);
        ellipsoid.setAttributes(mAttributeManager.getStatusAttributes(mon.getQuota(index)));
        mLayer.addRenderable(ellipsoid);

        if (order == 1) {
            plotGroundConnector(mon, z);
        }

        return ellipsoid;
    }

}
