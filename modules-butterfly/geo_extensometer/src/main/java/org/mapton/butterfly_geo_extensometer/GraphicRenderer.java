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
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.Pyramid;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.airspaces.PartialCappedCylinder;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_format.types.geo.BGeoExtensometer;
import org.mapton.worldwind.api.WWHelper;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer {

    private final ExtensoAttributeManager mAttributeManager = ExtensoAttributeManager.getInstance();
    private final IndexedCheckModel<GraphicRendererItem> mCheckModel;
    private final RenderableLayer mLayer;
    private ArrayList<AVListImpl> mMapObjects;

    public GraphicRenderer(RenderableLayer layer, IndexedCheckModel<GraphicRendererItem> checkModel) {
        mLayer = layer;
        mCheckModel = checkModel;
    }

    public void plot(BGeoExtensometer extenso, Position position, ArrayList<AVListImpl> mapObjects) {
        mMapObjects = mapObjects;

        if (mCheckModel.isChecked(GraphicRendererItem.POLE)) {
            plotPole(extenso, position);
        }

        if (mCheckModel.isChecked(GraphicRendererItem.SLICE)) {
            plotSlice(extenso, position);
        }
    }

    private void addRenderable(Renderable renderable, boolean interactiveLayer) {
        if (interactiveLayer) {
            mLayer.addRenderable(renderable);
            if (renderable instanceof AVListImpl avlist) {
                mMapObjects.add(avlist);
            }
        } else {
            //mLayerXYZ.addRenderable(renderable); //TODO Add to a non responsive layer
        }
    }

    private void plotPole(BGeoExtensometer extenso, Position position) {
        var p0 = WWHelper.positionFromPosition(position, 0.0);
        var p1 = WWHelper.positionFromPosition(position, 8.0 * (extenso.getPoints().size() + 1));
        var path = new Path(p0, p1);
        path.setAttributes(mAttributeManager.getGroundConnectorAttributes());
        addRenderable(path, true);

        int i = 0;
        for (var point : extenso.getPoints().reversed()) {
            if (point.ext().getObservationsTimeFiltered().isEmpty()) {
                continue;
            }

            var lastObservation = point.ext().getObservationFilteredLast();
            var p = WWHelper.positionFromPosition(position, 8.0 * (i + 1));
            var size = 3.0 + lastObservation.ext().getDelta() / 100;
            size = MathHelper.limit(size, 2, 4);
            var pyramid = new Pyramid(p, size * 1.5, size);
            pyramid.setAttributes(mAttributeManager.getComponentAlarmAttributes(point.ext().getAlarmLevel()));

            if (lastObservation.ext().getDelta() < 0) {
                pyramid.setRoll(Angle.POS180);
            }

            addRenderable(pyramid, true);

            i++;
        }
    }

    private void plotSlice(BGeoExtensometer extenso, Position position) {
        int numOfSlices = extenso.getPoints().size();

        for (int i = 0; i < extenso.getPoints().size(); i++) {
            var point = extenso.getPoints().get(i);
            var angle = 360.0 / numOfSlices;

            var reversedList = point.ext().getObservationsTimeFiltered().reversed();
            var prevDate = LocalDateTime.now();
            var altitude = 0.0;
            var prevHeight = 0.0;

            for (int j = 0; j < reversedList.size(); j++) {
                var o = reversedList.get(j);

                var timeSpan = ChronoUnit.MINUTES.between(o.getDate(), prevDate);
                var height = timeSpan / 24000.0;
                altitude = altitude + height * 0.5 + prevHeight * 0.5;
                prevDate = o.getDate();
                prevHeight = height;

                var delta = o.ext().getDelta();
                if (delta == null) {
                    continue;
                }

                var maxRadius = 50.0;
                var radius = Math.min(maxRadius, Math.abs(delta) / 20 + 0.05);
                var maximus = radius == maxRadius;

                var cappedCylinder = new PartialCappedCylinder(position, radius,
                        Angle.fromDegrees(i * angle),
                        Angle.fromDegrees(angle * (i + 1))
                );
                var halfHeight = height / 2.0;
                cappedCylinder.setAltitudes(altitude - halfHeight, altitude + halfHeight);
                var alarmLevel = point.ext().getAlarmLevel(o);
                var rise = Math.signum(delta) > 0;
                var attrs = mAttributeManager.getComponentTraceAttributes(alarmLevel, rise, maximus);

                if (j == 0 && ChronoUnit.DAYS.between(o.getDate(), LocalDateTime.now()) > 180) {
                    attrs = new BasicShapeAttributes(attrs);
                    attrs.setInteriorOpacity(0.25);
                    attrs.setOutlineOpacity(0.20);
                }

                cappedCylinder.setAttributes(attrs);
                addRenderable(cappedCylinder, true);
            }
        }
    }
}
