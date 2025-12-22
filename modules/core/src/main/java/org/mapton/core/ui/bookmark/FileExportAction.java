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

import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import javafx.scene.Node;
import javax.swing.JFileChooser;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.action.Action;
import org.mapton.api.MKmlCreator;
import org.mapton.api.MNotificationIcons;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.NotificationDisplayer.Priority;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.Exceptions;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.nbp.FileChooserHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxActionSwing;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.almond.util.io.Geo;
import se.trixon.almond.util.io.GeoHeader;
import se.trixon.almond.util.io.GeoPoint;
import se.trixon.almond.util.swing.FileHelper;

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
            hidePopOver();

            var dialogTitle = "%s %s".formatted(Dict.EXPORT.toString(), mTitle.toLowerCase());
            var extensionFilters = FileChooserHelper.getExtensionFilters();
            var fileChooser = new FileChooserBuilder(FileExportAction.class)
                    .addFileFilter(extensionFilters.get("csv"))
                    .addFileFilter(extensionFilters.get("geo"))
                    .addFileFilter(extensionFilters.get("json"))
                    .addFileFilter(extensionFilters.get("kml"))
                    .setAcceptAllFileFilterUsed(false)
                    .setDefaultWorkingDirectory(FileHelper.getDefaultDirectory())
                    .setFileFilter(extensionFilters.get("csv"))
                    .setFilesOnly(true)
                    .setSelectionApprover(FileChooserHelper.getFileExistSelectionApprover(Almond.getFrame()))
                    .setTitle(dialogTitle)
                    .createFileChooser();

            if (fileChooser.showSaveDialog(Almond.getFrame()) == JFileChooser.APPROVE_OPTION) {
                new Thread(() -> {
                    mFile = FileChooserHelper.getFileWithProperExt(fileChooser);
                    try {
                        switch (FilenameUtils.getExtension(mFile.getName())) {
                            case "csv" ->
                                new CsvExporter();

                            case "json" ->
                                new JsonExporter();

                            case "geo" ->
                                new GeoExporter();

                            case "kml" ->
                                new KmlExporter();

                            default ->
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
                }, getClass().getCanonicalName()).start();
            }
        });

        action.setGraphic(MaterialIcon._Content.SAVE.getImageView(getIconSizeToolBarInt()));

        return action;
    }

    private class CsvExporter {

        public CsvExporter() throws IOException {
            mManager.save(mManager.getFilteredItems(), mFile);
        }
    }

    private class GeoExporter {

        public GeoExporter() throws IOException {
            var map = new LinkedHashMap<String, String>();
            map.put("Application", "Mapton");
            map.put("Author", SystemHelper.getUserName());
            map.put("Created", FastDateFormat.getInstance("yyyy-MM-dd HH.mm.ss").format(new Date()));
            var geo = new Geo(new GeoHeader(map));

            mManager.getFilteredItems().stream()
                    .map(bookmark -> {
                        var point = new GeoPoint();
                        point.setPointId(bookmark.getName());
                        point.setRemark(bookmark.getCategory());
                        point.setX(bookmark.getLatitude());
                        point.setY(bookmark.getLongitude());
                        point.setZ(.0);
                        return point;
                    })
                    .forEachOrdered(p -> geo.addPoint(p));

            geo.write(mFile);
        }
    }

    private class JsonExporter {

        public JsonExporter() throws IOException {
            mJsonObjectMapper.writeValue(mFile, mManager.getFilteredItems());
        }
    }

    private class KmlExporter extends MKmlCreator {

        private final TreeMap<String, Folder> mCategories = new TreeMap<>();

        public KmlExporter() throws IOException {
            mDocument.setName("Mapton %s".formatted(Dict.BOOKMARKS.toString()));
            mManager.getFilteredItems().forEach(bookmark -> {
                var placemark = KmlFactory.createPlacemark()
                        .withName(bookmark.getName())
                        .withDescription(bookmark.getDescription())
                        .withOpen(Boolean.TRUE);

                placemark.createAndSetPoint()
                        .addToCoordinates(bookmark.getLongitude(), bookmark.getLatitude());

                var key = StringUtils.defaultIfBlank(bookmark.getCategory(), "---");
                mCategories.computeIfAbsent(key, k -> new Folder().withName(key)).addToFeature(placemark);
            });

            mCategories.values().forEach(folder -> {
                mDocument.addToFeature(folder);
            });

            save(mFile);
        }
    }
}
