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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.mapton.api.MKmlCreator;
import org.mapton.api.MOptions;
import org.mapton.core.api.MaptonNb;
import org.openide.util.Exceptions;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.io.Geo;
import se.trixon.almond.util.io.GeoHeader;
import se.trixon.almond.util.io.GeoLine;
import se.trixon.almond.util.io.GeoPoint;
import se.trixon.almond.util.swing.SwingHelper;
import se.trixon.almond.util.swing.dialogs.SimpleDialog;

/**
 *
 * @author Patrik Karlström
 */
public class RulerExporter {

    private final RulerTabPane mRulerTabPane;
    private final FastDateFormat mDateFormat = FastDateFormat.getInstance("yyyyMMdd_HHmmss");
    private File mDestination;

    public RulerExporter(RulerTabPane rulerTabPane) {
        mRulerTabPane = rulerTabPane;

        SwingHelper.runLater(() -> {
            exportFile();
        });
    }

    private void exportFile() {
        SimpleDialog.clearFilters();
        SimpleDialog.addFilters("kml", "geo");
        SimpleDialog.setFilter("kml");
        SimpleDialog.setTitle(String.format("%s %s", Dict.SAVE.toString(), Dict.COORDINATE_FILE.toString().toLowerCase()));
        SimpleDialog.setParent(MaptonNb.getFrame());

        String epoch = mDateFormat.format(new Date());

        SimpleDialog.setSelectedFile(new File(Dict.Geometry.GEOMETRIES.toString() + "_" + epoch));
        if (mDestination == null) {
            SimpleDialog.setPath(FileUtils.getUserDirectory());
        } else {
            SimpleDialog.setPath(mDestination.getParentFile());
        }

        if (SimpleDialog.saveFile(new String[]{"geo", "kml"})) {
            mDestination = SimpleDialog.getPath();
            new Thread(() -> {
                try {
                    switch (FilenameUtils.getExtension(mDestination.getName())) {
                        case "geo":
                            new ExporterGeo(epoch);
                            break;

                        case "kml":
                            new ExporterKml(epoch);
                            break;

                        default:
                            throw new AssertionError();
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }, getClass().getCanonicalName()).start();
        }
    }

    private class ExporterGeo {

        public ExporterGeo(String epoch) throws IOException {
            var map = new LinkedHashMap<String, String>();
            map.put("Application", "Mapton");
            map.put("Author", SystemHelper.getUserName());
            map.put("Created", epoch);
            var geo = new Geo(new GeoHeader(map));

            mRulerTabPane.getTabs().stream()
                    .filter(tab -> (tab instanceof RulerTab))
                    .forEachOrdered(tab -> {
                        geo.getLines().add(constructLine(((RulerTab) tab).getMeasureTool()));
                    });

            geo.write(mDestination);
        }

        private GeoLine constructLine(MeasureTool measureTool) {
            switch (measureTool.getMeasureShapeType()) {
                case MeasureTool.SHAPE_LINE:
                case MeasureTool.SHAPE_PATH:
                    return generateLine(measureTool.getPositions());
                case MeasureTool.SHAPE_ELLIPSE:
                case MeasureTool.SHAPE_POLYGON:
                case MeasureTool.SHAPE_SQUARE:
                case MeasureTool.SHAPE_QUAD:
                case MeasureTool.SHAPE_CIRCLE:
                    return generatePolygon(measureTool.getPositions());
                default:
                    return null;
            }
        }

        private GeoLine generateLine(ArrayList<? extends Position> positions) {
            var line = new GeoLine();
            var cooTrans = MOptions.getInstance().getMapCooTrans();

            for (Position position : positions) {
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

        ExporterKml(String epoch) throws IOException {
            mDocument.setName(String.format("%s_%s", Dict.Geometry.GEOMETRIES.toString(), epoch));

            mRulerTabPane.getTabs().stream()
                    .filter(tab -> (tab instanceof RulerTab))
                    .forEachOrdered(tab -> {
                        mDocument.addToFeature(((RulerTab) tab).getFeature());
                    });

            save(mDestination, true, true);
            SystemHelper.desktopOpen(mDestination);
        }
    }
}
