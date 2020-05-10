/*
 * Copyright 2020 Patrik Karlström.
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
package org.mapton.addon.files_nb.ui;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ToolBar;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.CheckListView;
import org.controlsfx.control.IndexedCheckModel;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.addon.files_nb.api.Document;
import org.mapton.addon.files_nb.api.DocumentManager;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.PopOverWatcher;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.almond.util.swing.dialogs.SimpleDialog;

/**
 *
 * @author Patrik Karlström
 */
public class DocumentsPane extends BorderPane {

    private final String[] SUPPORTED_EXTS = new String[]{"kml"};
    private List<Action> mActions;
    private File mFileDialogStartDir;
    private final CheckListView<Document> mListView = new CheckListView<>();
    private final DocumentManager mManager = DocumentManager.getInstance();
    private final OptionsPopOver mOptionsPopOver = new OptionsPopOver();
    private Action mRefreshAction;

    public DocumentsPane() {
        createUI();
        refreshCheckedStates();
        initListeners();
    }

    private void addFiles(List<File> files) {
        files.stream()
                .filter(file -> (file.isDirectory() || (file.isFile() && StringUtils.equalsAnyIgnoreCase(FilenameUtils.getExtension(file.getName()), SUPPORTED_EXTS))))
                .forEachOrdered(file -> {
                    mManager.addIfMissing(file);
                });

        mManager.sort();
    }

    private void createUI() {
        Action openAction = new Action(Dict.OPEN.toString(), event -> {
            requestAddFiles();
        });
        openAction.setGraphic(MaterialIcon._Content.ADD.getImageView(getIconSizeToolBarInt()));

        Action closeAction = new Action(Dict.CLOSE.toString(), event -> {
            if (getSelected() != null) {
                remove();
            }
        });
        closeAction.setGraphic(MaterialIcon._Content.REMOVE.getImageView(getIconSizeToolBarInt()));

        Action closeAllAction = new Action(Dict.CLOSE_ALL.toString(), event -> {
            if (!mListView.getItems().isEmpty()) {
                removeAll();
            }
        });
        closeAllAction.setGraphic(MaterialIcon._Content.CLEAR.getImageView(getIconSizeToolBarInt()));

        mRefreshAction = new Action(Dict.REFRESH.toString(), event -> {
            mManager.refresh();
        });
        mRefreshAction.setGraphic(MaterialIcon._Navigation.REFRESH.getImageView(getIconSizeToolBarInt()));

        Action optionsAction = new Action(Dict.OPTIONS.toString(), (event) -> {
            if (mOptionsPopOver.isShowing()) {
                mOptionsPopOver.hide();
            } else {
                Node node = (Node) event.getSource();
                mOptionsPopOver.show(node);
                PopOverWatcher.getInstance().registerPopOver(mOptionsPopOver, node);
            }
        });
        optionsAction.setGraphic(MaterialIcon._Action.SETTINGS.getImageView(getIconSizeToolBarInt()));

        mActions = Arrays.asList(
                openAction,
                closeAction,
                closeAllAction,
                ActionUtils.ACTION_SPAN,
                mRefreshAction,
                optionsAction
        );

        ToolBar toolBar = ActionUtils.createToolBar(mActions, ActionUtils.ActionTextBehavior.HIDE);

        FxHelper.adjustButtonWidth(toolBar.getItems().stream(), getIconSizeToolBarInt());
        toolBar.getItems().stream().filter((item) -> (item instanceof ButtonBase))
                .map((item) -> (ButtonBase) item).forEachOrdered((buttonBase) -> {
            FxHelper.undecorateButton(buttonBase);
        });

        FxHelper.slimToolBar(toolBar);
        setTop(toolBar);
        setCenter(mListView);

        mListView.itemsProperty().bind(mManager.itemsProperty());
    }

    private Document getSelected() {
        return mListView.getSelectionModel().getSelectedItem();
    }

    private void initListeners() {
        mListView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends Document> c) -> {
            if (getSelected() != null) {
                getSelected().fitToBounds();
            }
        });

        mManager.getItems().addListener((ListChangeListener.Change<? extends Document> c) -> {
            Platform.runLater(() -> {
                refreshCheckedStates();
                try {
                    mManager.save();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
        });

        final IndexedCheckModel<Document> checkModel = mListView.getCheckModel();

        checkModel.getCheckedItems().addListener((ListChangeListener.Change<? extends Document> c) -> {
            Platform.runLater(() -> {
                mManager.getItems().forEach(source -> {
                    source.setVisible(checkModel.isChecked(source));
                });

                try {
                    mManager.save();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
        });

        mListView.setOnDragOver((DragEvent event) -> {
            Dragboard board = event.getDragboard();
            if (board.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
        });

        mListView.setOnDragDropped((DragEvent event) -> {
            addFiles(event.getDragboard().getFiles());
        });
    }

    private void refreshCheckedStates() {
        final IndexedCheckModel<Document> checkModel = mListView.getCheckModel();

        for (Document source : mManager.getItems()) {
            if (source.isVisible()) {
                checkModel.check(source);
            } else {
                checkModel.clearCheck(source);
            }
        }
    }

    private void remove() {
        final Document source = getSelected();

        SwingUtilities.invokeLater(() -> {
            String[] buttons = new String[]{Dict.CANCEL.toString(), Dict.CLOSE.toString()};
            NotifyDescriptor d = new NotifyDescriptor(
                    String.format(Dict.Dialog.MESSAGE_FILE_CLOSE.toString(), source.getFile().getName()),
                    String.format(Dict.Dialog.TITLE_CLOSE_S.toString(), Dict.FILE.toString().toLowerCase()) + "?",
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE,
                    buttons,
                    Dict.CLOSE.toString());

            if (Dict.CLOSE.toString() == DialogDisplayer.getDefault().notify(d)) {
                Platform.runLater(() -> {
                    mManager.removeAll(source);
                });
            }
        });
    }

    private void removeAll() {
        SwingUtilities.invokeLater(() -> {
            String[] buttons = new String[]{Dict.CANCEL.toString(), Dict.CLOSE_ALL.toString()};
            NotifyDescriptor d = new NotifyDescriptor(
                    Dict.Dialog.MESSAGE_FILE_CLOSE_ALL.toString(),
                    Dict.Dialog.TITLE_FILE_CLOSE_ALL.toString() + "?",
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE,
                    buttons,
                    Dict.CLOSE_ALL.toString());

            if (Dict.CLOSE_ALL.toString() == DialogDisplayer.getDefault().notify(d)) {
                Platform.runLater(() -> {
                    mManager.removeAll();
                });
            }
        });
    }

    private void requestAddFiles() {
        SimpleDialog.clearFilters();
        for (String ext : SUPPORTED_EXTS) {
            SimpleDialog.addFilters(ext);
        }
        SimpleDialog.setFilter("kml");
        SimpleDialog.setTitle(Dict.OPEN.toString());

        if (mFileDialogStartDir == null) {
            SimpleDialog.setPath(FileUtils.getUserDirectory());
        } else {
            SimpleDialog.setPath(mFileDialogStartDir);
            SimpleDialog.setSelectedFile(new File(""));
        }

        if (SimpleDialog.openFileAndDirectoy(true)) {
            File[] paths = SimpleDialog.getPaths();
            mFileDialogStartDir = paths[0].getParentFile();
            addFiles(Arrays.asList(paths));
        }
    }
}
