/*
 * Copyright 2021 Patrik Karlström.
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
package org.mapton.worldwind.temp;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.Renderable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;
import static org.mapton.worldwind.temp.Renderer.DIGEST_RENDERABLE_MAP;
import org.mapton.api.MCoordinateFile;
import org.openide.util.Exceptions;
import se.trixon.almond.util.io.Geo;
import se.trixon.almond.util.io.GeoPoint;

/**
 *
 * @author Patrik Karlström
 */
public class GeoRenderer extends Renderer {

    private BasicShapeAttributes mLineBasicShapeAttributes;

    public static void render(MCoordinateFile coordinateFile, RenderableLayer layer) {
        GeoRenderer renderer = new GeoRenderer(coordinateFile, layer);
        renderer.render(layer);
    }

    public GeoRenderer(MCoordinateFile coordinateFile, RenderableLayer layer) {
        mCoordinateFile = coordinateFile;
        mCooTrans = coordinateFile.getCooTrans();
        mLayer = layer;
        initAttributes();
    }

    @Override
    protected void render(RenderableLayer layer) {
        render(() -> {
            String digest = getDigest();
            ArrayList<Renderable> renderables = DIGEST_RENDERABLE_MAP.computeIfAbsent(digest, k -> {
                ArrayList<Renderable> newRenderables = new ArrayList<>();
                Geo geo = new Geo();
                try {
                    geo.read(mCoordinateFile.getFile());
                    renderPoints(newRenderables, geo.getPoints());
                    System.out.println(geo.getLines().size());
                    //Dynamic transform
                    //Create Points
                    //Create Lines
                    System.out.println(geo.toString());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                return newRenderables;
            });

            for (Renderable renderable : renderables) {
                layer.addRenderable(renderable);
            }
        });
    }

    private void initAttributes() {
        mLineBasicShapeAttributes = new BasicShapeAttributes();
    }

    private void renderPoints(ArrayList<Renderable> renderables, List<GeoPoint> points) {
        for (GeoPoint point : points) {
            if (mCooTrans.isWithinProjectedBounds(point.getX(), point.getY())) {
                Point2D p = mCooTrans.toWgs84(point.getX(), point.getY());
                PointPlacemark placemark = new PointPlacemark(Position.fromDegrees(p.getY(), p.getX()));
                placemark.setLabelText(point.getPointId());
                placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                placemark.setEnableLabelPicking(true);
                renderables.add(placemark);
            }
        }
    }
}
