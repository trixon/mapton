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
package org.mapton.butterfly_acoustic.blast.export;

import de.micromata.opengis.kml.v_2_2_0.AltitudeMode;
import de.micromata.opengis.kml.v_2_2_0.ColorMode;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.TimeStamp;
import de.micromata.opengis.kml.v_2_2_0.Units;
import java.awt.Color;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.api.MKmlCreator;
import org.mapton.butterfly_acoustic.blast.BlastManager;
import org.mapton.butterfly_core.api.BKey;
import org.mapton.butterfly_format.types.acoustic.BAcousticBlast;
import org.mapton.core.api.ui.ExportConfiguration;
import se.trixon.almond.util.GraphicsHelper;

/**
 *
 * @author Patrik Karlström
 */
public class KmlGenerator {

    private final DateTimeFormatter mDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private final Document mDocument;
    private final Kml mKml = new Kml();
    private final BlastManager mManager = BlastManager.getInstance();
    private Folder mRootFolder;

    public KmlGenerator() {
        mDocument = mKml.createAndSetDocument().withOpen(true);
    }

    public Kml generate(ExportConfiguration ec) {
        mRootFolder = mDocument.createAndAddFolder().withName("Sprängsalvor").withOpen(true);
        var iconFolder = mRootFolder.createAndAddFolder().withName("Ikoner").withOpen(false);
        var influenceFolder = mRootFolder.createAndAddFolder().withName("Influens 40-60-80").withOpen(false);
        var styleIds = new HashSet<String>();
        for (var p : mManager.getFilteredItems()) {
            var styleNormalId = "s_%s".formatted(p.getName());
            var styleHighlightId = styleNormalId + "_h1";
            if (!styleIds.contains(styleNormalId)) {
                var normalStyle = mDocument
                        .createAndAddStyle()
                        .withId(styleNormalId);

                Color color = ObjectUtils.defaultIfNull(p.getValue(BKey.PIN_COLOR), Color.RED);
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

            var timePrimitive = KmlFactory.createTimeStamp().withWhen(p.getDateLatest().format(mDateTimeFormatter));
            var placemark = KmlFactory.createPlacemark()
                    .withName(p.getValue(BKey.PIN_NAME))
                    //                    .withSnippet(mBlankSnippet)
                    .withTimePrimitive(timePrimitive)
                    .withStyleUrl("#" + styleNormalId);
            placemark.createAndSetPoint().addToCoordinates(p.getLon(), p.getLat());
            iconFolder.addToFeature(placemark);

            var influenceCircles = List.of(
                    new InfluenceCircle(40.0, Color.RED, new Color(255, 0, 0, 0)),
                    new InfluenceCircle(60.0, Color.YELLOW, new Color(0, 0, 0, 0)),
                    new InfluenceCircle(80.0, Color.GREEN, new Color(0, 0, 0, 0))
            );

            if (p.ext().getMeasurementAge(ChronoUnit.DAYS) < 15) {
                plot(new InfluenceCircle(40.0, Color.RED, new Color(255, 0, 0, 90)),
                        influenceFolder, p, timePrimitive);
            }

            for (var influenceCircle : influenceCircles) {
                plot(influenceCircle, influenceFolder, p, timePrimitive);
            }
        }

        return mKml;
    }

    private void plot(InfluenceCircle influenceCircle, Folder folder, BAcousticBlast p, TimeStamp timeStamp) {
        var circle = MKmlCreator.createCircle("",
                p.getLat(), p.getLon(),
                influenceCircle.radius(),
                80,
                1,
                GraphicsHelper.colorToAABBGGRR(influenceCircle.lineColor()),
                GraphicsHelper.colorToAABBGGRR(influenceCircle.fillColor()),
                ColorMode.NORMAL,
                AltitudeMode.CLAMP_TO_GROUND);

        circle.setTimePrimitive(timeStamp);
        folder.addToFeature(circle);
    }

    public record InfluenceCircle(double radius, Color lineColor, Color fillColor) {

    }
}
