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

import java.io.File;
import java.io.IOException;
import java.util.List;
import javafx.scene.Node;
import javax.swing.JFileChooser;
import org.apache.commons.io.FilenameUtils;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.action.Action;
import org.mapton.api.MBookmark;
import org.mapton.api.MNotificationIcons;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.NotificationDisplayer.Priority;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.Exceptions;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.nbp.FileChooserHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxActionSwing;
import se.trixon.almond.util.fx.FxHelper;
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
        var action = new FxActionSwing(Dict.IMPORT.toString(), () -> {
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

    private void importCsv() throws IOException {
        FxHelper.runLater(() -> mManager.add(mFile));

    }

    private void importGeo() throws IOException {
        var geo = new Geo();
        geo.read(mFile);
        var bookmarks = geo.getPoints().stream()
                .map(geoPoint -> {
                    var bookmark = new MBookmark();
                    bookmark.setName(geoPoint.getPointId());
                    bookmark.setCategory(geoPoint.getRemark());
                    bookmark.setLatitude(geoPoint.getX());
                    bookmark.setLongitude(geoPoint.getY());
                    bookmark.setZoom(0.999);
                    bookmark.setColor("#FF0000");
                    return bookmark;
                })
                .toList();

        FxHelper.runLater(() -> mManager.add(bookmarks));
    }

    private void importJson() throws IOException {
        List<MBookmark> bookmarks = mJsonObjectMapper.readValue(mFile, mJsonObjectMapper.getTypeFactory().constructCollectionType(List.class, MBookmark.class));

        FxHelper.runLater(() -> mManager.add(bookmarks));
    }
}
