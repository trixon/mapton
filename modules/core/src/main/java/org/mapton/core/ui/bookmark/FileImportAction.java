/*
 * Copyright 2023 Patrik Karlström.
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

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import javafx.scene.Node;
import javax.swing.JFileChooser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.action.Action;
import org.mapton.api.FileChooserHelper;
import org.mapton.api.MBookmark;
import org.mapton.api.MBookmarkManager;
import org.mapton.api.MNotificationIcons;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.NotificationDisplayer.Priority;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.Exceptions;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.MathHelper;
import se.trixon.almond.util.fx.FxActionSwing;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.almond.util.io.Geo;
import se.trixon.almond.util.swing.FileHelper;

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
            hidePopOver();

            var dialogTitle = "%s %s".formatted(Dict.IMPORT.toString(), mTitle.toLowerCase());
            var extensionFilters = FileChooserHelper.getExtensionFilters();
            var fileChooser = new FileChooserBuilder(FileExportAction.class)
                    .addFileFilter(extensionFilters.get("csv"))
                    .addFileFilter(extensionFilters.get("geo"))
                    .addFileFilter(extensionFilters.get("json"))
                    .setDefaultWorkingDirectory(FileHelper.getDefaultDirectory())
                    .setFileFilter(extensionFilters.get("csv"))
                    .setFilesOnly(true)
                    .setTitle(dialogTitle)
                    .setSelectionApprover(FileChooserHelper.getFileExistOpenSelectionApprover(Almond.getFrame()))
                    .createFileChooser();

            if (fileChooser.showOpenDialog(Almond.getFrame()) == JFileChooser.APPROVE_OPTION) {
                new Thread(() -> {
                    mFile = fileChooser.getSelectedFile();
                    mImports = 0;
                    mErrors = 0;

                    try {
                        switch (FilenameUtils.getExtension(mFile.getName())) {
                            case "csv" ->
                                importCsv();

                            case "geo" ->
                                importGeo();

                            case "json" ->
                                importJson();

                            default ->
                                throw new AssertionError();
                        }

                        if (mImports + mErrors > 0) {
                            NotificationDisplayer.getDefault().notify(
                                    Dict.OPERATION_COMPLETED.toString(),
                                    MNotificationIcons.getInformationIcon(),
                                    dialogTitle,
                                    null,
                                    Priority.LOW
                            );
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }, getClass().getCanonicalName()).start();
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
        var requiredColumns = new String[]{
            MBookmarkManager.COL_NAME,
            MBookmarkManager.COL_LATITUDE,
            MBookmarkManager.COL_LONGITUDE};

        try ( var csvRecords = CSVParser.parse(
                mFile,
                Charset.forName("utf-8"),
                CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setDelimiter(';').build()
        )) {
            String default_zoom = "0.85";
            if (isValidCsv(csvRecords, requiredColumns)) {
                var bookmarks = new ArrayList<MBookmark>();

                for (var csvRecord : csvRecords) {
                    String category = getOrDefault(csvRecord, MBookmarkManager.COL_CATEGORY, Dict.DEFAULT.toString());
                    String description = getOrDefault(csvRecord, MBookmarkManager.COL_DESCRIPTION, "");
                    String url = getOrDefault(csvRecord, MBookmarkManager.COL_URL, "");
                    String color = getOrDefault(csvRecord, MBookmarkManager.COL_COLOR, "FFFF00");
                    String displayMarker = getOrDefault(csvRecord, MBookmarkManager.COL_DISPLAY_MARKER, "1");
                    String zoomString = getOrDefault(csvRecord, MBookmarkManager.COL_ZOOM, default_zoom);
                    if (!NumberUtils.isCreatable(zoomString)) {
                        zoomString = default_zoom;
                    }

                    Double lat = MathHelper.convertStringToDouble(csvRecord.get(MBookmarkManager.COL_LATITUDE));
                    Double lon = MathHelper.convertStringToDouble(csvRecord.get(MBookmarkManager.COL_LONGITUDE));
                    Double zoom = MathHelper.convertStringToDouble(zoomString);

                    var bookmark = new MBookmark();

                    bookmark.setCategory(category);
                    bookmark.setName(csvRecord.get(MBookmarkManager.COL_NAME));
                    bookmark.setDescription(description);
                    bookmark.setUrl(url);
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
                String message = mBundle.getString("bookmark_import_error_csv_message").formatted(String.join("\n ▶ ", requiredColumns));
                NotificationDisplayer.getDefault().notify(
                        mBundle.getString("bookmark_import_error_csv_title"),
                        MNotificationIcons.getErrorIcon(),
                        message,
                        null,
                        Priority.HIGH
                );
            }
        }
    }

    private void importGeo() throws IOException {
        var geo = new Geo();
        geo.read(mFile);
        var bookmarks = new ArrayList<MBookmark>();

        for (var geoPoint : geo.getPoints()) {
            var bookmark = new MBookmark();
            bookmark.setName(geoPoint.getPointId());
            bookmark.setCategory(geoPoint.getRemark());
            bookmark.setLatitude(geoPoint.getX());
            bookmark.setLongitude(geoPoint.getY());
            bookmark.setZoom(0.999);

            bookmarks.add(bookmark);
        }

        Point result = mManager.dbInsert(bookmarks);
        mImports = result.x;
        mErrors = result.y;
    }

    private void importJson() throws IOException {
        var gson = new GsonBuilder()
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
        for (var column : columns) {
            if (records.getHeaderMap().containsKey(column)) {
                continue;
            } else {
                return false;
            }
        }

        return true;
    }

}
