/*
 * Copyright 2018 Patrik Karlström.
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
package org.mapton.ww_grid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javax.swing.SwingUtilities;
import org.controlsfx.control.CheckListView;
import org.controlsfx.control.IndexedCheckModel;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.MDict;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import static org.mapton.ww_grid.Options.*;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class LocalGridView extends BorderPane {

    private final CheckListView<LocalGrid> mListView = new CheckListView<>();
    private final LocalGridManager mManager = LocalGridManager.getInstance();
    private final Options mOptions = Options.getInstance();
    private CheckBox mPlotCheckBox;

    public LocalGridView() {
        createUI();
        initStates();
        initListeners();
        load();
    }

    private void createUI() {
        Font defaultFont = Font.getDefault();
        ResourceBundle bundle = NbBundle.getBundle(GridTopComponent.class);
        mPlotCheckBox = new CheckBox(bundle.getString("local"));
        mPlotCheckBox.setFont(new Font(defaultFont.getSize() * 1.2));

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

        Collection<? extends Action> actions = Arrays.asList(
                addAction,
                remAction,
                editAction
        );

        ToolBar toolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);

        FxHelper.adjustButtonWidth(toolBar.getItems().stream(), getIconSizeToolBarInt());
        toolBar.getItems().stream().filter((item) -> (item instanceof ButtonBase))
                .map((item) -> (ButtonBase) item).forEachOrdered((buttonBase) -> {
            FxHelper.undecorateButton(buttonBase);
        });

        toolBar.setStyle("-fx-spacing: 0px;");
        setTop(new VBox(8, mPlotCheckBox, toolBar));
        setCenter(mListView);
        toolBar.disableProperty().bind(mPlotCheckBox.selectedProperty().not());
        mListView.disableProperty().bind(mPlotCheckBox.selectedProperty().not());

        mListView.setItems(mManager.getItems());
    }

    private LocalGrid getSelected() {
        return mListView.getSelectionModel().getSelectedItem();
    }

    private void initListeners() {
        mPlotCheckBox.setOnAction((event) -> {
            mOptions.set(KEY_LOCAL_PLOT, mPlotCheckBox.isSelected());
        });

        mListView.setOnMouseClicked((mouseEvent) -> {
            if (getSelected() != null
                    && mouseEvent.getButton() == MouseButton.PRIMARY
                    && mouseEvent.getClickCount() == 2) {
                mManager.edit(getSelected());
            }
        });

        mManager.getItems().addListener((ListChangeListener.Change<? extends LocalGrid> c) -> {
            Platform.runLater(() -> {
                refreshCheckedStates();
                mManager.save();
            });

        });
    }

    private void initStates() {
        mPlotCheckBox.setSelected(mOptions.is(KEY_LOCAL_PLOT));
    }

    private void load() {
        ArrayList<LocalGrid> grids = mManager.loadItems();
        Platform.runLater(() -> {
            mListView.getItems().clear();
            if (grids != null) {
                mListView.getItems().addAll(grids);
                refreshCheckedStates();

                final IndexedCheckModel<LocalGrid> checkModel = mListView.getCheckModel();
                final ObservableList<LocalGrid> items = mListView.getItems();

                checkModel.getCheckedItems().addListener((ListChangeListener.Change<? extends LocalGrid> c) -> {
                    Platform.runLater(() -> {
                        items.forEach((grid) -> {
                            grid.setVisible(checkModel.isChecked(grid));
                        });
                        mManager.save();
                    });
                });
            }
        });
    }

    private void refreshCheckedStates() {
        final IndexedCheckModel<LocalGrid> checkModel = mListView.getCheckModel();
        final ObservableList<LocalGrid> items = mListView.getItems();

        for (LocalGrid grid : items) {
            if (grid.isVisible()) {
                checkModel.check(grid);
            } else {
                checkModel.clearCheck(grid);
            }
        }
    }

    private void remove() {
        final LocalGrid localGrid = getSelected();

        SwingUtilities.invokeLater(() -> {
            String[] buttons = new String[]{Dict.CANCEL.toString(), Dict.REMOVE.toString()};
            NotifyDescriptor d = new NotifyDescriptor(
                    String.format(Dict.Dialog.MESSAGE_PROFILE_REMOVE.toString(), localGrid.getName()),
                    String.format(Dict.Dialog.TITLE_REMOVE_S.toString(), MDict.GRID.toString().toLowerCase()) + "?",
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE,
                    buttons,
                    Dict.REMOVE.toString());

            if (Dict.REMOVE.toString() == DialogDisplayer.getDefault().notify(d)) {
                Platform.runLater(() -> {
                    mManager.removeAll(localGrid);
                });
            }
        });
    }
}
