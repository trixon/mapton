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
package org.mapton.butterfly_topo.export;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.Units;
import java.awt.Color;
import java.util.HashSet;
import java.util.TreeMap;
import org.mapton.butterfly_core.api.BKey;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_topo.api.TopoManager;
import org.mapton.core.api.ui.ExportConfiguration;
import se.trixon.almond.util.GraphicsHelper;

/**
 *
 * @author Patrik Karlström
 */
public class KmlGenerator {

    private final Document mDocument;
    private final Kml mKml = new Kml();
    private final TopoManager mManager = TopoManager.getInstance();
    private Folder mRootFolder;

    public KmlGenerator() {
        mDocument = mKml.createAndSetDocument().withOpen(true);
    }

    public Kml generate(ExportConfiguration ec) {
        mRootFolder = mDocument.createAndAddFolder().withName("Frekvenser").withOpen(true);
        var freqToFolder1dMap = new TreeMap<Integer, Folder>();
        var freqToFolder3dMap = new TreeMap<Integer, Folder>();
        var styleIds = new HashSet<String>();
        for (var p : mManager.getFilteredItems()) {
            var freqMap = p.getDimension() == BDimension._1d ? freqToFolder1dMap : freqToFolder3dMap;
            var folder = freqMap.computeIfAbsent(p.getFrequency(), k -> new Folder().withName(String.valueOf(p.getFrequency())));
            var styleNormalId = "s_%s".formatted(p.getName());
            var styleHighlightId = styleNormalId + "_h1";
            if (!styleIds.contains(styleNormalId)) {
                var normalStyle = mDocument
                        .createAndAddStyle()
                        .withId(styleNormalId);

                Color color = p.getValue(BKey.PIN_COLOR);

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
            folder.addToFeature(placemark);
        }

        var folder1d = mRootFolder.createAndAddFolder().withName("1d");
        var folder3d = mRootFolder.createAndAddFolder().withName("3d");
        freqToFolder1dMap.values().forEach(folder -> folder1d.addToFeature(folder));
        freqToFolder3dMap.values().forEach(folder -> folder3d.addToFeature(folder));

        return mKml;
    }
}
