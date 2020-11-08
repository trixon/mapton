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
package org.mapton.core.ui.bookmark;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import javafx.scene.Node;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.action.Action;
import org.mapton.api.MBookmark;
import org.mapton.api.MBookmarkManager;
import org.mapton.api.MKmlCreator;
import org.mapton.api.MNotificationIcons;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.NotificationDisplayer.Priority;
import org.openide.util.Exceptions;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxActionSwing;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.almond.util.io.Geo;
import se.trixon.almond.util.io.GeoHeader;
import se.trixon.almond.util.io.GeoPoint;
import se.trixon.almond.util.swing.dialogs.SimpleDialog;

/**
 *
 * @author Patrik Karlström
 */
public class FileExportAction extends FileAction {

    private File mFile;

    public FileExportAction(PopOver popOver) {
        super(popOver);
    }

    @Override
    public Action getAction(Node owner) {
        FxActionSwing action = new FxActionSwing(Dict.EXPORT.toString(), () -> {
            SimpleDialog.clearFilters();
            SimpleDialog.addFilters("csv", "geo", "json", "kml", "csv");
            SimpleDialog.setFilter("csv");

            hidePopOver();

            final String dialogTitle = String.format("%s %s", Dict.EXPORT.toString(), mTitle.toLowerCase());
            SimpleDialog.setTitle(dialogTitle);

            if (mFile == null) {
                SimpleDialog.setPath(FileUtils.getUserDirectory());
            } else {
                SimpleDialog.setPath(mFile.getParentFile());
                SimpleDialog.setSelectedFile(new File(""));
            }

            if (SimpleDialog.saveFile(new String[]{"csv", "geo", "json", "kml"})) {
                new Thread(() -> {
                    mFile = SimpleDialog.getPath();

                    try {
                        switch (FilenameUtils.getExtension(mFile.getName())) {
                            case "csv":
                                new CsvExporter();
                                break;

                            case "json":
                                new JsonExporter();
                                break;

                            case "geo":
                                new GeoExporter();
                                break;

                            case "kml":
                                new KmlExporter();
                                break;

                            default:
                                throw new AssertionError();
                        }
                        NotificationDisplayer.getDefault().notify(
                                Dict.OPERATION_COMPLETED.toString(),
                                MNotificationIcons.getInformationIcon(),
                                dialogTitle,
                                null,
                                Priority.LOW
                        );
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }).start();
            }
        });

        action.setGraphic(MaterialIcon._Content.SAVE.getImageView(getIconSizeToolBarInt()));

        return action;
    }

    private class CsvExporter {

        public CsvExporter() throws IOException {
            final StringWriter stringWriter = new StringWriter();
            final CSVPrinter printer = CSVFormat.DEFAULT
                    .withCommentMarker('#')
                    .withDelimiter(';')
                    .withHeader(
                            MBookmarkManager.COL_CATEGORY,
                            MBookmarkManager.COL_NAME,
                            MBookmarkManager.COL_DESCRIPTION,
                            MBookmarkManager.COL_URL,
                            MBookmarkManager.COL_COLOR,
                            MBookmarkManager.COL_LATITUDE,
                            MBookmarkManager.COL_LONGITUDE,
                            MBookmarkManager.COL_ZOOM,
                            MBookmarkManager.COL_DISPLAY_MARKER)
                    .print(stringWriter);

            for (MBookmark bookmark : mManager.getItems()) {
                printer.printRecord(
                        bookmark.getCategory(),
                        bookmark.getName(),
                        bookmark.getDescription(),
                        bookmark.getUrl(),
                        bookmark.getColor(),
                        bookmark.getLatitude(),
                        bookmark.getLongitude(),
                        bookmark.getZoom(),
                        bookmark.isDisplayMarker() ? "1" : "0"
                );
            }

            FileUtils.writeStringToFile(mFile, stringWriter.toString(), "utf-8");
        }
    }

    private class GeoExporter {

        public GeoExporter() throws IOException {
            LinkedHashMap<String, String> map = new LinkedHashMap<>();
            map.put("Application", "Mapton");
            map.put("Author", SystemHelper.getUserName());
            map.put("Created", new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(new Date()));
            GeoHeader geoHeader = new GeoHeader("\"SBG Object Text v2.01\",\"Coordinate Document\"", map);

            Geo geo = new Geo();
            geo.setHeader(geoHeader);

            for (MBookmark bookmark : mManager.getItems()) {
                GeoPoint point = new GeoPoint();
                point.setPointId(bookmark.getName());
                point.setRemark(bookmark.getCategory());
                point.setX(bookmark.getLatitude());
                point.setY(bookmark.getLongitude());
                point.setZ(.0);

                geo.addPoint(point);
            }

            geo.write(mFile);
        }
    }

    private class JsonExporter {

        public JsonExporter() throws IOException {
            Gson gson = new GsonBuilder()
                    .setVersion(1.0)
                    .setPrettyPrinting()
                    .setDateFormat("yyyy-MM-dd HH:mm:ss")
                    .create();

            FileUtils.write(mFile, gson.toJson(mManager.getItems()), "utf-8");
        }
    }

    private class KmlExporter extends MKmlCreator {

        private final TreeMap<String, Folder> mCategories = new TreeMap<>();

        public KmlExporter() throws IOException {
            mDocument.setName(String.format("Mapton %s", Dict.BOOKMARKS.toString()));
            mManager.getItems().forEach((item) -> {
                Placemark placemark = KmlFactory.createPlacemark()
                        .withName(item.getName())
                        .withDescription(item.getDescription())
                        .withOpen(Boolean.TRUE);

                placemark.createAndSetPoint()
                        .addToCoordinates(item.getLongitude(), item.getLatitude());

                String key = StringUtils.defaultIfBlank(item.getCategory(), "---");
                mCategories.computeIfAbsent(key, k -> new Folder().withName(key)).addToFeature(placemark);
            });

            mCategories.values().forEach((folder) -> {
                mDocument.addToFeature(folder);
            });

            save(mFile);
        }
    }
}
