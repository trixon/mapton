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
package org.mapton.butterfly_geo_extensometer;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.Pyramid;
import gov.nasa.worldwind.render.airspaces.PartialCappedCylinder;
import java.util.ArrayList;
import java.util.Random;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_format.types.geo.BGeoExtensometer;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer {

    private final ExtensoAttributeManager mAttributeManager = ExtensoAttributeManager.getInstance();
    private final IndexedCheckModel<GraphicRendererItem> mCheckModel;
    private final RenderableLayer mLayer;
    private final Random mRandom = new Random();

    public GraphicRenderer(RenderableLayer layer, IndexedCheckModel<GraphicRendererItem> checkModel) {
        mLayer = layer;
        mCheckModel = checkModel;
    }

    public void plot(BGeoExtensometer extenso, Position position, ArrayList<AVListImpl> mapObjects) {
        if (mCheckModel.isChecked(GraphicRendererItem.POLE)) {
            mapObjects.add(plotPole(extenso, position));
        }

        if (mCheckModel.isChecked(GraphicRendererItem.SLICE)) {
            plotSlice(extenso, position);
        }
    }

    private AVListImpl plotPole(BGeoExtensometer extenso, Position position) {
        var p0 = WWHelper.positionFromPosition(position, 0.0);
        var p1 = WWHelper.positionFromPosition(position, 8.0 * (extenso.getPoints().size() + 1));
        var path = new Path(p0, p1);
        path.setAttributes(mAttributeManager.getGroundConnectorAttributes());
        mLayer.addRenderable(path);

        for (int i = 0; i < extenso.getPoints().size(); i++) {
            var point = extenso.getPoints().get(i);
            var p = WWHelper.positionFromPosition(position, 8.0 * (i + 1));
            var size = 2.0;//TODO bas on delta
            size = mRandom.nextDouble(2, 4);
            var pyramid = new Pyramid(p, size * 1.5, size);
            pyramid.setAttributes(mAttributeManager.getStatusAttributes(mRandom.nextDouble()));
            if (mRandom.nextBoolean()) {
                pyramid.setRoll(Angle.POS180);
            }
            mLayer.addRenderable(pyramid);
        }

        return path;
    }

    private void plotSlice(BGeoExtensometer extenso, Position position) {
        int numOfSlices = extenso.getPoints().size();

        for (int i = 0; i < extenso.getPoints().size(); i++) {
            var point = extenso.getPoints().get(i);
            var angle = 360.0 / numOfSlices;

            for (int j = 0; j < point.ext().getObservationsTimeFiltered().size(); j++) {
                var o = point.ext().getObservationsTimeFiltered().get(j);
                var radius = mRandom.nextDouble(2, 5);
                var cappedCylinder = new PartialCappedCylinder(position, radius,
                        Angle.fromDegrees(i * angle),
                        Angle.fromDegrees(angle * (i + 1))
                );
                cappedCylinder.setAttributes(mAttributeManager.getSliceAttributes(mRandom.nextDouble()));
                cappedCylinder.setAltitudes(j, j + 1);

                mLayer.addRenderable(cappedCylinder);
            }
        }
    }
}
