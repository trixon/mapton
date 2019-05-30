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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javax.swing.SwingUtilities;
import org.controlsfx.control.CheckListView;
import org.controlsfx.control.IndexedCheckModel;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
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

    private final CheckListView<MapoSource> mListView = new CheckListView<>();
    private final MapoSourceManager mManager = MapoSourceManager.getInstance();
    private final DateSelectionPane mDateSelectionPane = new DateSelectionPane();

    public TabSources(Mapo mapo) {
        setText(Dict.SOURCES.toString());
        mMapo = mapo;

//        createUI();
//        initValidation();
//        load();
        createUI();
        initStates();
        initListeners();
        load();
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

        Action refreshAction = new Action(Dict.REFRESH.toString(), (ActionEvent event) -> {
            new SourceScanner();
        });
        refreshAction.setGraphic(MaterialIcon._Navigation.REFRESH.getImageView(getIconSizeToolBarInt()));

        Collection<? extends Action> actions = Arrays.asList(
                new SourceFileImportAction().getAction(),
                new SourceFileExportAction().getAction(),
                addAction,
                remAction,
                editAction,
                ActionUtils.ACTION_SPAN,
                refreshAction
        );

        ToolBar toolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);

        FxHelper.adjustButtonWidth(toolBar.getItems().stream(), getIconSizeToolBarInt());
        toolBar.getItems().stream().filter((item) -> (item instanceof ButtonBase))
                .map((item) -> (ButtonBase) item).forEachOrdered((buttonBase) -> {
            FxHelper.undecorateButton(buttonBase);
        });

        toolBar.setStyle("-fx-spacing: 0px;");
        toolBar.setPadding(Insets.EMPTY);

        BorderPane innerBorderPane = new BorderPane(mListView);
        innerBorderPane.setTop(toolBar);
        BorderPane borderPane = new BorderPane(innerBorderPane);
        borderPane.setTop(mDateSelectionPane);
        setScrollPaneContent(borderPane);
        mListView.setItems(mManager.getItems());
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
    }

    private void initStates() {
    }

    private void load() {
        ArrayList<MapoSource> sources = mManager.loadItems();
        Platform.runLater(() -> {
            final IndexedCheckModel<MapoSource> checkModel = mListView.getCheckModel();
            final ObservableList<MapoSource> items = mListView.getItems();

            checkModel.getCheckedItems().addListener((ListChangeListener.Change<? extends MapoSource> c) -> {
                Platform.runLater(() -> {
                    items.forEach((source) -> {
                        source.setVisible(checkModel.isChecked(source));
                    });
                    try {
                        mManager.save();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                });
            });

            items.clear();
            if (sources != null) {
                items.addAll(sources);
                refreshCheckedStates();
            }
        });
    }

    private void refreshCheckedStates() {
        final IndexedCheckModel<MapoSource> checkModel = mListView.getCheckModel();
        final ObservableList<MapoSource> items = mListView.getItems();

        for (MapoSource source : items) {
            if (source.isVisible()) {
                checkModel.check(source);
            } else {
                checkModel.clearCheck(source);
            }
        }
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
}
