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
package org.mapton.butterfly_roi.export;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Units;
import java.awt.Color;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import org.apache.commons.lang3.ObjectUtils;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.mapton.butterfly_core.api.BKey;
import org.mapton.butterfly_roi.RoiManager;
import org.mapton.core.api.ui.ExportConfiguration;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GraphicsHelper;

/**
 *
 * @author Patrik Karlström
 */
public class KmlGenerator {

    private final DateTimeFormatter mDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private final Document mDocument;
    private final Kml mKml = new Kml();
    private final RoiManager mManager = RoiManager.getInstance();
    private Folder mRootFolder;

    public KmlGenerator() {
        mDocument = mKml.createAndSetDocument().withOpen(true);
    }

    public Kml generate(ExportConfiguration ec) {
        mRootFolder = mDocument.createAndAddFolder().withName(Dict.Geometry.GEOMETRIES.toString()).withOpen(true);
        var iconFolder = mRootFolder.createAndAddFolder().withName("Ytor").withOpen(false);
        var styleIds = new HashSet<String>();
        for (var p : mManager.getFilteredItems()) {
            var styleNormalId = "s_%s".formatted(p.getName());
            var styleHighlightId = styleNormalId + "_h1";
            if (!styleIds.contains(styleNormalId)) {
                var normalStyle = mDocument
                        .createAndAddStyle()
                        .withId(styleNormalId);

                var color = ObjectUtils.getIfNull(p.getValue(BKey.PIN_COLOR), Color.RED);
                var normalIconStyle = normalStyle
                        .createAndSetIconStyle()
                        .withColor(GraphicsHelper.colorToAABBGGRR(color))
                        .withScale(1.0)
                        .withHotSpot(KmlFactory.createVec2()
                                .withXunits(Units.FRACTION)
                                .withYunits(Units.FRACTION)
                                .withX(0.5)
                                .withY(0)
                        );

                var normalLabelStyle = normalStyle
                        .createAndSetLabelStyle()
                        .withScale(0.7);

                var highlightStyle = mDocument
                        .createAndAddStyle()
                        .withId(styleHighlightId);

                String href = p.getValue(BKey.PIN_URL);
                var icon = KmlFactory.createIcon().withHref(href);
                normalIconStyle.setIcon(icon);
//                mDocument.createAndAddStyleMap().withId(styleNormalId)
//                        .addToPair(KmlFactory.createPair().withKey(StyleState.NORMAL).withStyleUrl("#" + styleNormalId))
//                        .addToPair(KmlFactory.createPair().withKey(StyleState.HIGHLIGHT).withStyleUrl("#" + styleHighlightId));

                styleIds.add(styleNormalId);
            }

            var placemark = KmlFactory.createPlacemark()
                    .withName(p.getValue(BKey.PIN_NAME))
                    //                    .withSnippet(mBlankSnippet)
                    .withStyleUrl("#" + styleNormalId);
            placemark.createAndSetPoint().addToCoordinates(p.getLon(), p.getLat());
            iconFolder.addToFeature(placemark);

            var polygon = placemark.createAndSetPolygon();
            var boundary = polygon.createAndSetOuterBoundaryIs();
            var linearRing = boundary.createAndSetLinearRing();
            var geometry = p.getGeometry();
            switch (geometry) {
                case LineString lineString -> {
                    //TODO
                }
                case org.locationtech.jts.geom.Polygon outPolygon -> {
                    addLinearRing(linearRing, outPolygon);
                }
                case org.locationtech.jts.geom.MultiPolygon multiPolygon -> {
                    for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                        var outPolygon = (org.locationtech.jts.geom.Polygon) multiPolygon.getGeometryN(i);
                        addLinearRing(linearRing, outPolygon);
                    }
                }
                default -> {
                }
            }
        }

        return mKml;
    }

    private void addLinearRing(LinearRing linearRing, Polygon polygon) {
        for (var coordinate : polygon.getCoordinates()) {
            linearRing.addToCoordinates(coordinate.x, coordinate.y);
        }
    }

}
