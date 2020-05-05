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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javax.swing.SwingUtilities;
import org.controlsfx.control.CheckListView;
import org.controlsfx.control.IndexedCheckModel;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.addon.files_nb.api.FileSource;
import org.mapton.addon.files_nb.api.FileSourceManager;
import org.mapton.addon.files_nb.api.Mapo;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.PopOverWatcher;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class SourcesPane extends BorderPane {

    private List<Action> mActions;
    private final CheckListView<FileSource> mListView = new CheckListView<>();
    private final FileSourceManager mManager = FileSourceManager.getInstance();
    private final Mapo mMapo = Mapo.getInstance();
    private final OptionsPopOver mOptionsPopOver = new OptionsPopOver();
    private Action mRefreshAction;

    public SourcesPane() {
        createUI();
        refreshCheckedStates();
        initListeners();
        Mapton.getGlobalState().put(Mapo.KEY_SOURCE_UPDATED, mManager);

        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            Mapton.getGlobalState().put(Mapo.KEY_SETTINGS_UPDATED, mMapo.getSettings());
        }).start();
    }

    private void createUI() {
        Action addAction = new Action(Dict.ADD.toString(), event -> {
            mManager.edit(null);
        });
        addAction.setGraphic(MaterialIcon._Content.ADD.getImageView(getIconSizeToolBarInt()));

        Action editAction = new Action(Dict.EDIT.toString(), event -> {
            if (getSelected() != null) {
                mManager.edit(getSelected());
            }
        });
        editAction.setGraphic(MaterialIcon._Editor.MODE_EDIT.getImageView(getIconSizeToolBarInt()));

        Action removeAction = new Action(Dict.REMOVE.toString(), event -> {
            if (getSelected() != null) {
                remove();
            }
        });
        removeAction.setGraphic(MaterialIcon._Content.REMOVE.getImageView(getIconSizeToolBarInt()));

        Action removeAllAction = new Action(Dict.REMOVE_ALL.toString(), event -> {
            if (!mListView.getItems().isEmpty()) {
                removeAll();
            }
        });
        removeAllAction.setGraphic(MaterialIcon._Content.CLEAR.getImageView(getIconSizeToolBarInt()));

        mRefreshAction = new Action(event -> {
        });
        mRefreshAction.setText(Dict.REFRESH.toString());
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
                new SourceFileImportAction().getAction(),
                addAction,
                removeAction,
                removeAllAction,
                editAction,
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

    private FileSource getSelected() {
        return mListView.getSelectionModel().getSelectedItem();
    }

    private void initListeners() {
        mListView.setOnMouseClicked((mouseEvent) -> {
            if (getSelected() != null
                    && mouseEvent.getButton() == MouseButton.PRIMARY
                    && mouseEvent.getClickCount() == 2) {
                mManager.edit(getSelected());
            }
        });

        mListView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends FileSource> c) -> {
            if (getSelected() != null) {
                getSelected().fitToBounds();
            }
        });

        mManager.getItems().addListener((ListChangeListener.Change<? extends FileSource> c) -> {
            Platform.runLater(() -> {
                refreshCheckedStates();
                try {
                    mManager.save();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
        });

        final IndexedCheckModel<FileSource> checkModel = mListView.getCheckModel();

        checkModel.getCheckedItems().addListener((ListChangeListener.Change<? extends FileSource> c) -> {
            Platform.runLater(() -> {
                mManager.getItems().forEach((source) -> {
                    source.setVisible(checkModel.isChecked(source));
                });

                try {
                    mManager.save();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }

                Mapton.getGlobalState().put(Mapo.KEY_SOURCE_UPDATED, mManager);
                Mapton.getGlobalState().put(Mapo.KEY_SETTINGS_UPDATED, mMapo.getSettings());
            });
        });
    }

    private void refreshCheckedStates() {
        final IndexedCheckModel<FileSource> checkModel = mListView.getCheckModel();

        for (FileSource source : mManager.getItems()) {
            if (source.isVisible()) {
                checkModel.check(source);
            } else {
                checkModel.clearCheck(source);
            }
        }
    }

    private void remove() {
        final FileSource source = getSelected();

        SwingUtilities.invokeLater(() -> {
            String[] buttons = new String[]{Dict.CANCEL.toString(), Dict.CLOSE.toString()};
            NotifyDescriptor d = new NotifyDescriptor(
                    String.format(Dict.Dialog.MESSAGE_FILE_CLOSE.toString(), source.getName()),
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
}
