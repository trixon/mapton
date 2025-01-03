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
package org.mapton.butterfly_topo;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Polygon;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import static org.mapton.butterfly_topo.GraphicRendererBase.sCheckModel;
import se.trixon.almond.util.ext.GrahamScan;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRendererGroup extends GraphicRendererBase {

    private final HashSet<BTopoControlPoint> mPoints = new HashSet<>();

    public GraphicRendererGroup(RenderableLayer layer, RenderableLayer passiveLayer) {
        super(layer, passiveLayer);
    }

    public void plot(BTopoControlPoint p, Position position) {
        p.setValue("position", position);
        mPoints.add(p);
    }

    @Override
    public void postPlot() {
        if (sCheckModel.isChecked(GraphicRendererItem.GROUP_DEFORMATION) && mPoints.size() > 2) {
            plotDeformation();
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
            var positions = plot3dOffsetPole(p, p.getValue("position"));
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

}
