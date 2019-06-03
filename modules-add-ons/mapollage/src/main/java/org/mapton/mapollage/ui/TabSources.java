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
package org.mapton.mapollage.ui;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javax.swing.SwingUtilities;
import org.controlsfx.control.CheckListView;
import org.controlsfx.control.IndexedCheckModel;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.mapollage.SourceScanner;
import org.mapton.mapollage.api.Mapo;
import org.mapton.mapollage.api.MapoSource;
import org.mapton.mapollage.api.MapoSourceManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class TabSources extends TabBase {

    private List<Action> mActions;
    private BorderPane mBorderPane;
    private final DateSelectionPane mDateSelectionPane = new DateSelectionPane();
    private final CheckListView<MapoSource> mListView = new CheckListView<>();
    private final MapoSourceManager mManager = MapoSourceManager.getInstance();
    private Action mRefreshAction;
    private Button mRefreshButton;
    private Thread mRefreshThread;
    private RunState mRunState;

    public TabSources(Mapo mapo) {
        setText(Dict.SOURCES.toString());
        mMapo = mapo;

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
        Action addAction = new Action(Dict.ADD.toString(), (ActionEvent event) -> {
            mManager.edit(null);
        });
        addAction.setGraphic(MaterialIcon._Content.ADD.getImageView(getIconSizeToolBarInt()));

        Action editAction = new Action(Dict.EDIT.toString(), (ActionEvent event) -> {
            if (getSelected() != null) {
                mManager.edit(getSelected());
            }
        });
        editAction.setGraphic(MaterialIcon._Editor.MODE_EDIT.getImageView(getIconSizeToolBarInt()));

        Action remAction = new Action(Dict.REMOVE.toString(), (ActionEvent event) -> {
            if (getSelected() != null) {
                remove();
            }
        });
        remAction.setGraphic(MaterialIcon._Content.REMOVE.getImageView(getIconSizeToolBarInt()));

        mRefreshAction = new Action((ActionEvent event) -> {
            if (mRunState == RunState.STARTABLE) {
                setRunningState(RunState.CANCELABLE);
                mRefreshThread = new Thread(() -> {
                    new SourceScanner();
                    setRunningState(RunState.STARTABLE);
                });
                mRefreshThread.start();
            } else {
                mRefreshThread.interrupt();
                setRunningState(RunState.STARTABLE);
            }
        });
        setRunningState(RunState.STARTABLE);

        mActions = Arrays.asList(
                new SourceFileImportAction().getAction(),
                new SourceFileExportAction().getAction(),
                addAction,
                remAction,
                editAction,
                ActionUtils.ACTION_SPAN,
                mRefreshAction
        );

        ToolBar toolBar = ActionUtils.createToolBar(mActions, ActionUtils.ActionTextBehavior.HIDE);

        FxHelper.adjustButtonWidth(toolBar.getItems().stream(), getIconSizeToolBarInt());
        toolBar.getItems().stream().filter((item) -> (item instanceof ButtonBase))
                .map((item) -> (ButtonBase) item).forEachOrdered((buttonBase) -> {
            FxHelper.undecorateButton(buttonBase);
        });

        toolBar.setStyle("-fx-spacing: 0px;");
        toolBar.setPadding(Insets.EMPTY);
        mRefreshButton = (Button) toolBar.getItems().get(toolBar.getItems().size() - 1);
        BorderPane innerBorderPane = new BorderPane(mListView);
        innerBorderPane.setTop(toolBar);
        mBorderPane = new BorderPane(innerBorderPane);
        mBorderPane.setTop(mDateSelectionPane);
        setScrollPaneContent(mBorderPane);

        mListView.itemsProperty().bind(mManager.itemsProperty());
    }

    private MapoSource getSelected() {
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

        mListView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends MapoSource> c) -> {
            if (getSelected() != null) {
                getSelected().fitToBounds();
            }
        });

        mManager.getItems().addListener((ListChangeListener.Change<? extends MapoSource> c) -> {
            Platform.runLater(() -> {
                refreshCheckedStates();
                try {
                    mManager.save();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
        });

        final IndexedCheckModel<MapoSource> checkModel = mListView.getCheckModel();

        checkModel.getCheckedItems().addListener((ListChangeListener.Change<? extends MapoSource> c) -> {
//            Platform.runLater(() -> {
            mDateSelectionPane.setDisable(true);
            mManager.getItems().forEach((source) -> {
                source.setVisible(checkModel.isChecked(source));
                if (source.isVisible()) {
                    mDateSelectionPane.setDisable(false);
                }
            });

            try {
                mManager.save();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            Mapton.getGlobalState().put(Mapo.KEY_SOURCE_UPDATED, mManager);
            Mapton.getGlobalState().put(Mapo.KEY_SETTINGS_UPDATED, mMapo.getSettings());
//            });
        });

    }

    private void refreshCheckedStates() {
        final IndexedCheckModel<MapoSource> checkModel = mListView.getCheckModel();
        boolean disableDateSelection = true;

        for (MapoSource source : mManager.getItems()) {
            if (source.isVisible()) {
                checkModel.check(source);
                disableDateSelection = false;
            } else {
                checkModel.clearCheck(source);
            }
        }

        mDateSelectionPane.setDisable(disableDateSelection);
    }

    private void remove() {
        final MapoSource source = getSelected();

        SwingUtilities.invokeLater(() -> {
            String[] buttons = new String[]{Dict.CANCEL.toString(), Dict.REMOVE.toString()};
            NotifyDescriptor d = new NotifyDescriptor(
                    String.format(Dict.Dialog.MESSAGE_PROFILE_REMOVE.toString(), source.getName()),
                    String.format(Dict.Dialog.TITLE_REMOVE_S.toString(), Dict.SOURCE.toString().toLowerCase()) + "?",
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE,
                    buttons,
                    Dict.REMOVE.toString());

            if (Dict.REMOVE.toString() == DialogDisplayer.getDefault().notify(d)) {
                Platform.runLater(() -> {
                    mManager.removeAll(source);
                });
            }
        });
    }

    private void setRunningState(RunState runState) {
        Platform.runLater(() -> {
            mRunState = runState;

            switch (runState) {
                case CANCELABLE:
                    FxHelper.disableControls(mBorderPane.getChildrenUnmodifiable(), true, mRefreshButton);
                    mRefreshAction.setText(Dict.CANCEL.toString());
                    mRefreshAction.setGraphic(MaterialIcon._Navigation.CANCEL.getImageView(getIconSizeToolBarInt()));
                    mActions.forEach((action) -> {
                        action.setDisabled(true);
                    });
                    mRefreshAction.setDisabled(false);
                    break;

                case STARTABLE:
                    mRefreshAction.setText(Dict.REFRESH.toString());
                    mRefreshAction.setGraphic(MaterialIcon._Navigation.REFRESH.getImageView(getIconSizeToolBarInt()));
                    try {
                        mActions.forEach((action) -> {
                            action.setDisabled(false);
                        });
                        FxHelper.disableControls(mBorderPane.getChildrenUnmodifiable(), false, mRefreshButton);
                    } catch (Exception e) {
                    }
                    break;
            }
        });
    }

    public enum RunState {
        STARTABLE, CANCELABLE;
    }
}
