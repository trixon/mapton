/*
 * Copyright 2019 Patrik Karlström.
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
package org.mapton.api;

import de.micromata.opengis.kml.v_2_2_0.AltitudeMode;
import de.micromata.opengis.kml.v_2_2_0.BalloonStyle;
import de.micromata.opengis.kml.v_2_2_0.Boundary;
import de.micromata.opengis.kml.v_2_2_0.ColorMode;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Icon;
import de.micromata.opengis.kml.v_2_2_0.IconStyle;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.LineStyle;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.PolyStyle;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import de.micromata.opengis.kml.v_2_2_0.Style;
import de.micromata.opengis.kml.v_2_2_0.Vec2;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import javafx.geometry.Point3D;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MLatLon;
import se.trixon.almond.util.ext.GrahamScan;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MKmlCreator {

    protected static final Logger LOGGER = Logger.getLogger(MKmlCreator.class.getName());
    protected final Document mDocument;
    protected final Kml mKml = new Kml();
    protected Folder mRootFolder;
    protected final SimpleDateFormat mTimeStampDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

    public static Comparator<Feature> getFeatureNameComparator() {
        return (Feature o1, Feature o2) -> o1.getName().compareTo(o2.getName());
    }

    public MKmlCreator() {
        mDocument = mKml.createAndSetDocument().withOpen(true);
    }

    public Placemark addCircle0(String name, ArrayList<Coordinate> coordinates, Folder folder) {
        try {
            Placemark placemark = folder
                    .createAndAddPlacemark()
                    .withName(name);

            Style style = placemark.createAndAddStyle();
            LineStyle lineStyle = style.createAndSetLineStyle()
                    .withColor("00000000")
                    .withWidth(0.0);

            PolyStyle polyStyle = style.createAndSetPolyStyle()
                    .withColor("ccffffff")
                    .withColorMode(ColorMode.RANDOM);

            Polygon polygon = placemark.createAndSetPolygon();
            Boundary boundary = polygon.createAndSetOuterBoundaryIs();
            LinearRing linearRing = boundary.createAndSetLinearRing();

            coordinates.forEach((node) -> {
                linearRing.addToCoordinates(node.getLongitude(), node.getLatitude());
            });
            return placemark;
        } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
            System.err.println(e);
            return null;
        }
    }

    public void addConvexHullPolygon(String name, ArrayList<Coordinate> coordinates, Folder polygonFolder, String color, ColorMode colorMode) {
        List<java.awt.geom.Point2D.Double> inputs = new ArrayList<>();
        coordinates.forEach((coordinate) -> {
            inputs.add(new java.awt.geom.Point2D.Double(coordinate.getLongitude(), coordinate.getLatitude()));
        });

        try {
            List<java.awt.geom.Point2D.Double> convexHull = GrahamScan.getConvexHullDouble(inputs);
            Placemark placemark = polygonFolder
                    .createAndAddPlacemark()
                    .withName(name);

            Style style = placemark.createAndAddStyle();
            LineStyle lineStyle = style.createAndSetLineStyle()
                    .withColor("00000000")
                    .withWidth(0.0);

            PolyStyle polyStyle = style.createAndSetPolyStyle();
            if (color != null) {
                polyStyle.setColor(color);
            }
            if (colorMode != null) {
                polyStyle.setColorMode(colorMode);
            }

            Polygon polygon = placemark.createAndSetPolygon();
            Boundary boundary = polygon.createAndSetOuterBoundaryIs();
            LinearRing linearRing = boundary.createAndSetLinearRing();

            convexHull.forEach((node) -> {
                linearRing.addToCoordinates(node.x, node.y);

            });
        } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
            System.err.println(e);
        }
    }

    public Placemark createCircle(String name, ArrayList<Point3D> coordinates, String color) {
        try {
            Placemark placemark = KmlFactory.createPlacemark().withName(name);
            Style style = placemark.createAndAddStyle();
            if (color != null) {

                LineStyle lineStyle = style.createAndSetLineStyle()
                        .withColor(color)
                        .withWidth(0.0);

                PolyStyle polyStyle = style.createAndSetPolyStyle()
                        .withColor(color)
                        .withFill(true)
                        .withColorMode(ColorMode.NORMAL);
            }

            Polygon polygon = placemark.createAndSetPolygon();
            Boundary boundary = polygon.createAndSetOuterBoundaryIs();
            LinearRing linearRing = boundary.createAndSetLinearRing();

            coordinates.forEach((node) -> {
                linearRing.addToCoordinates(node.getX(), node.getY());
            });
            return placemark;
        } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
            System.err.println(e);
            return null;
        }
    }

    @Deprecated
    public ArrayList<Point3D> createCircle(double lat, double lon, double radius, int quality) {
        if (quality < 3) {
            throw new IllegalArgumentException("Quality must be greater than 2");
        }

        ArrayList<Point3D> list = new ArrayList<>();
        for (double phi = 0; phi < 2 * Math.PI; phi += 2 * Math.PI / quality) {
            double lat2 = Math.sin(phi);
            double lon2 = Math.cos(phi);
            list.add(new Point3D(lon + lon2 * radius, lat + lat2 * radius, 0));
        }

        list.add(list.get(0));

        return list;
    }

    public Placemark createCircle(String name, double lat, double lon, double radius, int quality, double width, String lineColor, String fillColor, ColorMode colorMode, AltitudeMode altitudeMode) {
        if (quality < 3) {
            throw new IllegalArgumentException("Quality must be greater than 2");
        }

        MLatLon center = new MLatLon(lat, lon);
        ArrayList<Point3D> list = new ArrayList<>();

        for (double i = 0; i < 360.0; i = i + 360 / quality) {
            MLatLon latLon = center.getDestinationPoint(i, radius);
            list.add(new Point3D(latLon.getLongitude(), latLon.getLatitude(), 0));
        }

        list.add(list.get(0));

        return createPolygon(name, list, width, lineColor, fillColor, colorMode, altitudeMode);
    }

    public Placemark createLine(String name, ArrayList<Point3D> coordinates, double width, String color, AltitudeMode altitudeMode) {
        Placemark placemark = KmlFactory.createPlacemark().withName(name);
        Style style = placemark.createAndAddStyle();
        LineStyle lineStyle = style.createAndSetLineStyle()
                .withWidth(width);

        if (color != null) {
            lineStyle.setColor(color);
        }

        LineString line = placemark.createAndSetLineString();
        for (Point3D coordinate : coordinates) {
            line.addToCoordinates(coordinate.getX(), coordinate.getY(), coordinate.getZ());
        }

        line.setAltitudeMode(altitudeMode);

        return placemark;
    }

    public Placemark createLine(String name, Point3D p1, Point3D p2, double width, String color, AltitudeMode altitudeMode) {
        ArrayList<Point3D> coordinates = new ArrayList<>();
        coordinates.add(p1);
        coordinates.add(p2);

        return createLine(name, coordinates, width, color, altitudeMode);
    }

    public Placemark createPlacemark(String name, double lat, double lon, double scale, String color, String href, Vec2 hotSpot, BalloonStyle balloonStyle) {
        Placemark placemark = KmlFactory.createPlacemark().withName(name);

        if (href != null) {
            Style style = placemark.createAndAddStyle();
            IconStyle iconStyle = style
                    .createAndSetIconStyle()
                    .withScale(scale);

            if (color != null) {
                iconStyle.setColor(color);
            }

            if (balloonStyle != null) {
                style.setBalloonStyle(balloonStyle);
            }

            if (hotSpot != null) {
                iconStyle.setHotSpot(hotSpot);
            }

            Icon icon = KmlFactory.createIcon().withHref(href);
            iconStyle.setIcon(icon);
        }

        placemark.createAndSetPoint().addToCoordinates(lon, lat);

        return placemark;
    }

    public Placemark createPolygon(String name, ArrayList<Point3D> coordinates, double width, String lineColor, String fillColor, ColorMode colorMode, AltitudeMode altitudeMode) {
        Placemark placemark = KmlFactory.createPlacemark().withName(name);

        Style style = placemark.createAndAddStyle();
        LineStyle lineStyle = style.createAndSetLineStyle()
                .withColor(lineColor)
                .withWidth(width);

        PolyStyle polyStyle = style.createAndSetPolyStyle();
        if (fillColor != null) {
            polyStyle.setColor(fillColor);
        }
        if (colorMode != null) {
            polyStyle.setColorMode(colorMode);
        }

        Polygon polygon = placemark.createAndSetPolygon();
        Boundary boundary = polygon.createAndSetOuterBoundaryIs();
        LinearRing linearRing = boundary.createAndSetLinearRing();

        coordinates.forEach((node) -> {
            linearRing.addToCoordinates(node.getX(), node.getY());
        });

        polygon.setAltitudeMode(altitudeMode);

        return placemark;
    }

    public String save(File f) throws IOException {
        return save(f, false, false);
    }

    public String save(File f, boolean cleanNS2, boolean cleanSpace) throws IOException {
        StringWriter stringWriter = new StringWriter();
        mKml.marshal(stringWriter);
        String kmlString = stringWriter.toString();

        if (cleanNS2) {
            kmlString = cleanNS2(kmlString);
        }

        if (cleanSpace) {
            kmlString = cleanSpace(kmlString);
        }

        FileUtils.writeStringToFile(f, kmlString, "utf-8");

        return kmlString;
    }

    public void setVisible(Feature feature, boolean visible) {
        feature.setVisibility(visible);
        if (feature instanceof Folder) {
            ((Folder) feature).getFeature().forEach((f) -> {
                setVisible(f, visible);
            });
        }
    }

    public void sort(Comparator<Feature> c, List<Feature> features) {
        for (Feature feature : features) {
            sort(c, feature);
        }
    }

    public void sort(Comparator<Feature> c, Feature... features) {
        for (Feature feature : features) {
            if (feature instanceof Folder) {
                final List<Feature> subFeatures = ((Folder) feature).getFeature();
                subFeatures.sort(c);
                subFeatures.forEach((f) -> {
                    sort(c, f);
                });
            }
        }
    }

    protected String getSafeXmlString(String s) {
        if (StringUtils.containsAny(s, '<', '>', '&')) {
            s = new StringBuilder("<![CDATA[").append(s).append("]]>").toString();
        }

        return s;
    }

    private String cleanNS2(String kmlString) {
        kmlString = StringUtils.replace(kmlString, "xmlns:ns2=", "xmlns=");
        kmlString = StringUtils.replace(kmlString, "<ns2:", "<");
        kmlString = StringUtils.replace(kmlString, "</ns2:", "</");

        return kmlString;
    }

    private String cleanSpace(String kmlString) {
        kmlString = StringUtils.replace(kmlString, "        ", "\t");
        kmlString = StringUtils.replace(kmlString, "    ", "\t");

        return kmlString;
    }

}
