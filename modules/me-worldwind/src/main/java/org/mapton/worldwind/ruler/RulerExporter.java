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
package org.mapton.worldwind.ruler;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.measure.MeasureTool;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import javax.swing.JFileChooser;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.mapton.api.FileChooserHelper;
import org.mapton.api.MKmlCreator;
import org.mapton.api.MOptions;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.Exceptions;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.io.Geo;
import se.trixon.almond.util.io.GeoHeader;
import se.trixon.almond.util.io.GeoLine;
import se.trixon.almond.util.io.GeoPoint;
import se.trixon.almond.util.swing.FileHelper;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class RulerExporter {

    private final FastDateFormat mDateFormat = FastDateFormat.getInstance("yyyyMMdd_HHmmss");
    private final RulerTabPane mRulerTabPane;

    public RulerExporter(RulerTabPane rulerTabPane) {
        mRulerTabPane = rulerTabPane;

        SwingHelper.runLater(() -> {
            exportFile();
        });
    }

    private void exportFile() {
        var dialogTitle = "%s %s".formatted(Dict.SAVE.toString(), Dict.COORDINATE_FILE.toString().toLowerCase());
        var extensionFilters = FileChooserHelper.getExtensionFilters();
        var fileChooser = new FileChooserBuilder(RulerExporter.class)
                .addFileFilter(extensionFilters.get("kml"))
                .addFileFilter(extensionFilters.get("geo"))
                .setAcceptAllFileFilterUsed(false)
                .setDefaultWorkingDirectory(FileHelper.getDefaultDirectory())
                .setFileFilter(extensionFilters.get("kml"))
                .setFilesOnly(true)
                .setSelectionApprover(FileChooserHelper.getFileExistSelectionApprover(Almond.getFrame()))
                .setTitle(dialogTitle)
                .createFileChooser();

        String epoch = mDateFormat.format(new Date());
        var templateFile = new File(Dict.Geometry.GEOMETRIES.toString() + "_" + epoch);
        fileChooser.setSelectedFile(templateFile);

        if (fileChooser.showSaveDialog(Almond.getFrame()) == JFileChooser.APPROVE_OPTION) {
            var file = FileChooserHelper.getFileWithProperExt(fileChooser);
            new Thread(() -> {
                try {
                    switch (FilenameUtils.getExtension(file.getName())) {
                        case "geo" ->
                            new ExporterGeo(file, epoch);

                        case "kml" ->
                            new ExporterKml(file, epoch);

                        default ->
                            throw new AssertionError();
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }, getClass().getCanonicalName()).start();
        }
    }

    private class ExporterGeo {

        public ExporterGeo(File file, String epoch) throws IOException {
            var map = new LinkedHashMap<String, String>();
            map.put("Application", "Mapton");
            map.put("Author", SystemHelper.getUserName());
            map.put("Created", epoch);
            var geo = new Geo(new GeoHeader(map));

            mRulerTabPane.getTabs().stream()
                    .filter(RulerTab.class::isInstance)
                    .map(tab -> (RulerTab) tab)
                    .forEachOrdered(rulerTab -> {
                        geo.getLines().add(constructLine(rulerTab.getMeasureTool()));
                    });

            geo.write(file);
        }

        private GeoLine constructLine(MeasureTool measureTool) {
            return switch (measureTool.getMeasureShapeType()) {
                case MeasureTool.SHAPE_LINE, MeasureTool.SHAPE_PATH ->
                    generateLine(measureTool.getPositions());
                case MeasureTool.SHAPE_ELLIPSE, MeasureTool.SHAPE_POLYGON, MeasureTool.SHAPE_SQUARE, MeasureTool.SHAPE_QUAD, MeasureTool.SHAPE_CIRCLE ->
                    generatePolygon(measureTool.getPositions());
                default ->
                    null;
            };
        }

        private GeoLine generateLine(ArrayList<? extends Position> positions) {
            var line = new GeoLine();
            var cooTrans = MOptions.getInstance().getMapCooTrans();

            for (var position : positions) {
                var point = new GeoPoint();
                var p = cooTrans.fromWgs84(position.getLatitude().degrees, position.getLongitude().degrees);
                point.setX(p.getY());
                point.setY(p.getX());
                point.setZ(.0);

                line.getPoints().add(point);
            }

            return line;
        }

        private GeoLine generatePolygon(ArrayList<? extends Position> positions) {
            var line = generateLine(positions);
            line.setClosedPolygon(true);

            return line;
        }
    }

    private class ExporterKml extends MKmlCreator {

        ExporterKml(File file, String epoch) throws IOException {
            mDocument.setName("%s_%s".formatted(Dict.Geometry.GEOMETRIES.toString(), epoch));

            mRulerTabPane.getTabs().stream()
                    .filter(tab -> tab instanceof RulerTab)
                    .forEachOrdered(tab -> {
                        mDocument.addToFeature(((RulerTab) tab).getFeature());
                    });

            save(file, true, true);
        }
    }
}
