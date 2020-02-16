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
package org.mapton.base.ui.bookmark;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import javafx.scene.Node;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.action.Action;
import org.mapton.api.MBookmark;
import org.mapton.api.MBookmarkManager;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.openide.util.Exceptions;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.MathHelper;
import se.trixon.almond.util.fx.FxActionSwing;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.almond.util.io.Geo;
import se.trixon.almond.util.io.GeoPoint;
import se.trixon.almond.util.swing.dialogs.SimpleDialog;

/**
 *
 * @author Patrik Karlström
 */
public class FileImportAction extends FileAction {

    private File mFile;
    private int mErrors;
    private int mImports;

    public FileImportAction(PopOver popOver) {
        super(popOver);
    }

    @Override
    public Action getAction(Node owner) {
        FxActionSwing action = new FxActionSwing(Dict.IMPORT.toString(), () -> {
            SimpleDialog.clearFilters();
            SimpleDialog.addFilters("csv", "geo", "json", "csv");
            SimpleDialog.setFilter("csv");

            if (mPopOver != null) {
                mPopOver.hide();
            }

            final String dialogTitle = String.format("%s %s", Dict.IMPORT.toString(), mTitle.toLowerCase());
            SimpleDialog.setTitle(dialogTitle);

            if (mFile == null) {
                SimpleDialog.setPath(FileUtils.getUserDirectory());
            } else {
                SimpleDialog.setPath(mFile.getParentFile());
                SimpleDialog.setSelectedFile(new File(""));
            }

            if (SimpleDialog.openFile()) {
                new Thread(() -> {
                    mFile = SimpleDialog.getPath();
                    mImports = 0;
                    mErrors = 0;

                    try {
                        switch (FilenameUtils.getExtension(mFile.getName())) {
                            case "csv": {
                                importCsv();
                            }
                            break;

                            case "geo": {
                                importGeo();
                            }
                            break;

                            case "json":
                                importJson();
                                break;

                            default:
                                throw new AssertionError();
                        }

                        if (mImports + mErrors > 0) {
                            Mapton.notification(MKey.NOTIFICATION_INFORMATION, dialogTitle, Dict.OPERATION_COMPLETED.toString());
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                }).start();
            }
        });

        action.setGraphic(MaterialIcon._File.FOLDER_OPEN.getImageView(getIconSizeToolBarInt()));

        return action;
    }

    private String getOrDefault(CSVRecord record, String key, String defaultValue) {
        if (record.isSet(key)) {
            return record.get(key);
        } else {
            return defaultValue;
        }
    }

    private void importCsv() throws IOException {
        String[] requiredColumns = new String[]{
            MBookmarkManager.COL_NAME,
            MBookmarkManager.COL_LATITUDE,
            MBookmarkManager.COL_LONGITUDE};

        try (CSVParser records = CSVParser.parse(
                mFile,
                Charset.forName("utf-8"),
                CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(';')
        )) {
            String default_zoom = "0.85";
            if (isValidCsv(records, requiredColumns)) {
                ArrayList<MBookmark> bookmarks = new ArrayList<>();

                for (CSVRecord record : records) {
                    String category = getOrDefault(record, MBookmarkManager.COL_CATEGORY, Dict.DEFAULT.toString());
                    String description = getOrDefault(record, MBookmarkManager.COL_DESCRIPTION, "");
                    String color = getOrDefault(record, MBookmarkManager.COL_COLOR, "FFFF00");
                    String displayMarker = getOrDefault(record, MBookmarkManager.COL_DISPLAY_MARKER, "1");
                    String zoomString = getOrDefault(record, MBookmarkManager.COL_ZOOM, default_zoom);
                    if (!NumberUtils.isCreatable(zoomString)) {
                        zoomString = default_zoom;
                    }

                    Double lat = MathHelper.convertStringToDouble(record.get(MBookmarkManager.COL_LATITUDE));
                    Double lon = MathHelper.convertStringToDouble(record.get(MBookmarkManager.COL_LONGITUDE));
                    Double zoom = MathHelper.convertStringToDouble(zoomString);

                    MBookmark bookmark = new MBookmark();

                    bookmark.setCategory(category);
                    bookmark.setName(record.get(MBookmarkManager.COL_NAME));
                    bookmark.setDescription(description);
                    bookmark.setColor(color);
                    bookmark.setDisplayMarker(displayMarker.equalsIgnoreCase("1"));

                    bookmark.setLatitude(lat);
                    bookmark.setLongitude(lon);
                    bookmark.setZoom(zoom);

                    bookmarks.add(bookmark);
                }

                Point result = mManager.dbInsert(bookmarks);
                mImports = result.x;
                mErrors = result.y;
            } else {
                String message = String.format(mBundle.getString("bookmark_import_error_csv_message"), String.join("\n ▶ ", requiredColumns));
                Mapton.notification(
                        MKey.NOTIFICATION_ERROR,
                        mBundle.getString("bookmark_import_error_csv_title"),
                        message
                );
            }
        }
    }

    private void importGeo() throws IOException {
        Geo geo = new Geo();
        geo.read(mFile);
        ArrayList<MBookmark> bookmarks = new ArrayList<>();

        for (GeoPoint point : geo.getPoints()) {
            MBookmark bookmark = new MBookmark();
            bookmark.setName(point.getPointId());
            bookmark.setCategory(point.getRemark());
            bookmark.setLatitude(point.getX());
            bookmark.setLongitude(point.getY());
            bookmark.setZoom(0.999);

            bookmarks.add(bookmark);
        }

        Point result = mManager.dbInsert(bookmarks);
        mImports = result.x;
        mErrors = result.y;
    }

    private void importJson() throws IOException {
        Gson gson = new GsonBuilder()
                .setVersion(1.0)
                .setPrettyPrinting()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();

        String json = FileUtils.readFileToString(mFile, "UTF-8");

        ArrayList<MBookmark> bookmarks = gson.fromJson(json, new TypeToken<ArrayList<MBookmark>>() {
        }.getType());

        Point result = mManager.dbInsert(bookmarks);
        mImports = result.x;
        mErrors = result.y;
    }

    private boolean isValidCsv(CSVParser records, String[] columns) {

        for (String column : columns) {
            if (records.getHeaderMap().containsKey(column)) {
                continue;
            } else {
                return false;
            }
        }

        return true;
    }

}
