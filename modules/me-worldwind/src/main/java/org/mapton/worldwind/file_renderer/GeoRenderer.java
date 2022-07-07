/*
 * Copyright 2022 Patrik Karlström.
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
package org.mapton.worldwind.file_renderer;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.AbstractShape;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.Polygon;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.CappedCylinder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MCoordinateFile;
import org.mapton.api.MLatLon;
import org.mapton.api.file_opener.GeoCoordinateFileOpener;
import org.mapton.worldwind.api.CoordinateFileRendererWW;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.MathHelper;
import se.trixon.almond.util.io.Geo;
import se.trixon.almond.util.io.GeoLine;
import se.trixon.almond.util.io.GeoPoint;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = CoordinateFileRendererWW.class)
public class GeoRenderer extends CoordinateFileRendererWW {

    private BasicAirspaceAttributes mCircleAttributes;
    private BasicShapeAttributes mLineBasicShapeAttributes;

    public GeoRenderer() {
        addSupportedFileOpeners(GeoCoordinateFileOpener.class);
        initAttributes();
    }

    @Override
    public void init(LayerBundle layerBundle) {
        setLayerBundle(layerBundle);
    }

    @Override
    protected void load(MCoordinateFile coordinateFile) {
        mCooTrans = coordinateFile.getCooTrans();
        new Thread(() -> {
            try {
                var geo = new Geo();
                geo.read(coordinateFile.getFile());
                var layer = new RenderableLayer();
                layer.setPickEnabled(false);

                renderPoints(layer, geo.getPoints(), mCircleAttributes);

                double elevation = 0.1;

                var linePoints = renderLines(layer, geo.getLines(), elevation, mLineBasicShapeAttributes);
                renderPoints(layer, linePoints, mCircleAttributes);

                addLayer(coordinateFile, layer);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }, getClass().getName() + " Load").start();
    }

    @Override
    protected void render() {
        for (var coordinateFile : mCoordinateFileManager.getSublistBySupportedOpeners(getSupportedFileOpeners())) {
            render(coordinateFile);
        }
    }

    private void addCurvePoint(ArrayList<Position> positions, Point2D sourcePoint, double angle, double radius, double elevation) {
        double x = Math.cos(angle / 180 * Math.PI) * radius;
        double y = Math.sin(angle / 180 * Math.PI) * radius;
        var destPoint = sourcePoint.add(y, x);
        var destWgs84Point = mCooTrans.toWgs84(destPoint.getY(), destPoint.getX());

        positions.add(Position.fromDegrees(destWgs84Point.getY(), destWgs84Point.getX(), elevation));
    }

    private void initAttributes() {
        mLineBasicShapeAttributes = new BasicShapeAttributes();
        mLineBasicShapeAttributes.setDrawInterior(false);
        mLineBasicShapeAttributes.setOutlineMaterial(Material.RED);
        mLineBasicShapeAttributes.setOutlineWidth(1.0D);

        mCircleAttributes = new BasicAirspaceAttributes(mLineBasicShapeAttributes);
    }

    private void renderCurvedLine(RenderableLayer layer, GeoLine geoLine, double elevation, BasicShapeAttributes attributes) {
        var straightPositions = new ArrayList<Position>();
        var curvePositions = new ArrayList<Position>();

        for (int i = 0; i < geoLine.getPoints().size(); i++) {
            var geoPoint = geoLine.getPoints().get(i);
            var p = mCooTrans.toWgs84(geoPoint.getX(), geoPoint.getY());
            if (!StringUtils.equalsIgnoreCase(geoPoint.getSpecialCode(), "R")) {
                straightPositions.add(Position.fromDegrees(p.getY(), p.getX(), elevation));
            } else {
                if (!straightPositions.isEmpty()) {
                    straightPositions.add(Position.fromDegrees(p.getY(), p.getX(), elevation));
                    renderLine(layer, new ArrayList<>(straightPositions), false, attributes);
                    straightPositions.clear();
                }

                if (i == geoLine.getPoints().size() - 1) {
                    break;
                }

                var nextGeoPoint = geoLine.getPoints().get(i + 1);
                var sourcePoint2d = new Point2D(geoPoint.getY(), geoPoint.getX());
                var destPoint2d = new Point2D(nextGeoPoint.getY(), nextGeoPoint.getX());
                double radius;

                try {
                    radius = Double.parseDouble(geoPoint.getRemark());
                } catch (NumberFormatException e) {
                    radius = sourcePoint2d.distance(destPoint2d);
                }

                var centerPoint2d = MathHelper.calculateCircleCenter(sourcePoint2d, destPoint2d, radius);
                var sourcePoint2dW84 = mCooTrans.toWgs84(sourcePoint2d.getY(), sourcePoint2d.getX());
                var destPoint2dW84 = mCooTrans.toWgs84(destPoint2d.getY(), destPoint2d.getX());
                var centerPoint2dW84 = mCooTrans.toWgs84(centerPoint2d.getY(), centerPoint2d.getX());

                var sourceLatLon = new MLatLon(sourcePoint2dW84.getY(), sourcePoint2dW84.getX());
                var destLatLon = new MLatLon(destPoint2dW84.getY(), destPoint2dW84.getX());
                var centerLatLon = new MLatLon(centerPoint2dW84.getY(), centerPoint2dW84.getX());

                double absR = Math.abs(radius);
                var sourceBearing = centerLatLon.getBearing(sourceLatLon);
                var destBearing = centerLatLon.getBearing(destLatLon);
                var angleStep = (sourceBearing < destBearing ? 1.0 : -1.0) * 2;
                var lastLatLon = destLatLon;

                if (Math.signum(destBearing - sourceBearing) != Math.signum(radius)) {
                    lastLatLon = sourceLatLon;
                    var x = destBearing;
                    destBearing = sourceBearing;
                    if (destBearing < 0) {
                        destBearing += 360;
                    }
                    sourceBearing = x;
                    if (sourceBearing < 0) {
                        sourceBearing += 360;
                    }
                }

                if (sourceBearing < destBearing) {
                    for (double angle = sourceBearing; angle < destBearing; angle += angleStep) {
                        addCurvePoint(curvePositions, centerPoint2d, angle, absR, elevation);
                    }
                } else {
                    for (double angle = sourceBearing; angle > destBearing; angle += angleStep) {
                        addCurvePoint(curvePositions, centerPoint2d, angle, absR, elevation);
                    }
                }

                curvePositions.add(WWHelper.positionFromLatLon(lastLatLon, elevation));
                renderLine(layer, curvePositions, false, attributes);
            }
        }

        if (!straightPositions.isEmpty()) {
            renderLine(layer, straightPositions, false, attributes);
        }
    }

    private void renderLine(RenderableLayer layer, List<Position> positions, boolean closed, BasicShapeAttributes attributes) {
        try {
            if (positions.size() > 1) {
                AbstractShape path;
                if (closed) {
                    path = new Polygon(positions);
                } else {
                    path = new Path(positions);
                }

                path.setAttributes(attributes);
                path.setAltitudeMode(WorldWind.ABSOLUTE);
                layer.addRenderable(path);
            }
        } catch (IllegalArgumentException e) {
            System.err.format("%s: %s, %s\n",
                    getClass().getSimpleName(),
                    "Invalid path",
                    e.getMessage()
            );
        }
    }

    private List<GeoPoint> renderLines(RenderableLayer layer, List<GeoLine> geoLines, double elevation, BasicShapeAttributes attributes) {
        var geoPoints = new ArrayList<GeoPoint>();

        for (var geoLine : geoLines) {
            var positions = new ArrayList<Position>();
            var ordinaryLine = true;

            for (var geoPoint : geoLine.getPoints()) {
                if (StringUtils.equalsIgnoreCase(geoPoint.getSpecialCode(), "C")) {
                    geoPoints.add(geoPoint);
                }

                if (mCooTrans.isWithinProjectedBounds(geoPoint.getX(), geoPoint.getY())) {
                    var p = mCooTrans.toWgs84(geoPoint.getX(), geoPoint.getY());
                    elevation = MathHelper.convertDoubleToDouble(geoPoint.getZ());
                    positions.add(Position.fromDegrees(p.getY(), p.getX(), elevation));
                }

                if (StringUtils.equalsIgnoreCase(geoPoint.getSpecialCode(), "R")) {
                    ordinaryLine = false;
                }
            }

            if (ordinaryLine) {
                renderLine(layer, positions, geoLine.isClosedPolygon(), attributes);
            } else {
                try {
                    renderCurvedLine(layer, geoLine, elevation, attributes);
                } catch (Exception e) {
                    System.err.println("xxxxx");
                    System.err.println(e);
                }
            }
        }

        return geoPoints;
    }

    private void renderPoints(RenderableLayer layer, List<GeoPoint> geoPoints, BasicAirspaceAttributes circleAttributes) {
        for (var geoPoint : geoPoints) {
            if (mCooTrans.isWithinProjectedBounds(geoPoint.getX(), geoPoint.getY())) {
                var p = mCooTrans.toWgs84(geoPoint.getX(), geoPoint.getY());
                var position = Position.fromDegrees(p.getY(), p.getX());

                if (StringUtils.equalsIgnoreCase(geoPoint.getSpecialCode(), "C")) {
                    String remark = StringUtils.replace(geoPoint.getRemark(), "_", " ");
                    var raw = StringUtils.split(remark, " ");
                    var raw2 = StringUtils.replace(raw[0], ",", ".");
                    double r = Double.valueOf(raw2);

                    var cappedCylinder = new CappedCylinder(position, r);
                    cappedCylinder.setAltitudes(0.0, 0.1);
                    cappedCylinder.setCenter(position);
                    cappedCylinder.setRadii(r, r + 0.05);
                    cappedCylinder.setAttributes(circleAttributes);

                    layer.addRenderable(cappedCylinder);
                } else {
                    var pointPlacemark = new PointPlacemark(position);
                    pointPlacemark.setLabelText(geoPoint.getPointId());
                    pointPlacemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                    pointPlacemark.setEnableLabelPicking(true);
                    layer.addRenderable(pointPlacemark);
                }
            }
        }
    }
}
