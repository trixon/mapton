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
package org.mapton.base.ui.grid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.controlsfx.control.CheckListView;
import org.controlsfx.control.IndexedCheckModel;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.glyphfont.FontAwesome;
import org.mapton.api.MLocalGrid;
import org.mapton.api.MLocalGridManager;
import org.mapton.api.MOptions;
import static org.mapton.api.MOptions.*;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.openide.util.Exceptions;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class LocalGridsView extends BorderPane {


    private final CheckListView<MLocalGrid> mListView = new CheckListView<>();
    private final MLocalGridManager mManager = MLocalGridManager.getInstance();
    private final MOptions mOptions = MOptions.getInstance();
    private CheckBox mPlotCheckBox;
private static LocalGridEditor sLocalGridEditor;

    public static void setLocalGridEditor(LocalGridEditor localGridEditor) {
        LocalGridsView.sLocalGridEditor = localGridEditor;
    }
    public LocalGridsView() {
        createUI();
        initStates();
        initListeners();
        load();
    }

    void activate() {
        //This is a workaround for a system deep NPE
        new Thread(() -> {
            try {
                Thread.sleep(2);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }

            Platform.runLater(() -> {
                mListView.requestFocus();
            });
        }).start();
    }

    private void createUI() {
        mPlotCheckBox = new CheckBox(Dict.LOCAL.toString());
        mPlotCheckBox.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, FxHelper.getScaledFontSize() * 1.2));
        mPlotCheckBox.setPadding(new Insets(0, 0, 0, 8));

        Action addAction = new Action(Dict.ADD.toString(), (ActionEvent event) -> {
            sLocalGridEditor.edit(null);
        });
        addAction.setGraphic(Mapton.createGlyphToolbarForm(FontAwesome.Glyph.PLUS));

        Action editAction = new Action(Dict.EDIT.toString(), (ActionEvent event) -> {
            if (getSelected() != null) {
                sLocalGridEditor.edit(getSelected());
            }
        });
        editAction.setGraphic(Mapton.createGlyphToolbarForm(FontAwesome.Glyph.PENCIL));

        Action remAction = new Action(Dict.REMOVE.toString(), (ActionEvent event) -> {
            if (getSelected() != null) {
                sLocalGridEditor.remove(getSelected());
            }
        });
        remAction.setGraphic(Mapton.createGlyphToolbarForm(FontAwesome.Glyph.MINUS));

        Collection<? extends Action> actions = Arrays.asList(
                new FileImportAction().getAction(this),
                new FileExportAction().getAction(this),
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

        FxHelper.slimToolBar(toolBar);
        setTop(new VBox(8, mPlotCheckBox, toolBar));
        setCenter(mListView);
        toolBar.disableProperty().bind(mPlotCheckBox.selectedProperty().not());
        mListView.disableProperty().bind(mPlotCheckBox.selectedProperty().not());

        mListView.setItems(mManager.getItems());
    }

    private MLocalGrid getSelected() {
        return mListView.getSelectionModel().getSelectedItem();
    }

    private void initListeners() {
        mPlotCheckBox.setOnAction((event) -> {
            mOptions.put(KEY_GRID_LOCAL_PLOT, mPlotCheckBox.isSelected());
        });

        mListView.setOnMouseClicked((mouseEvent) -> {
            if (getSelected() != null
                    && mouseEvent.getButton() == MouseButton.PRIMARY
                    && mouseEvent.getClickCount() == 2) {
                sLocalGridEditor.edit(getSelected());
            }
        });

        mListView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends MLocalGrid> c) -> {
            if (getSelected() != null) {
                getSelected().fitToBounds();
            }
        });

        mManager.getItems().addListener((ListChangeListener.Change<? extends MLocalGrid> c) -> {
            Platform.runLater(() -> {
                refreshCheckedStates();
                mManager.save();
            });

        });
    }

    private void initStates() {
        mPlotCheckBox.setSelected(mOptions.is(KEY_GRID_LOCAL_PLOT));
    }

    private void load() {
        ArrayList<MLocalGrid> grids = mManager.loadItems();
        Platform.runLater(() -> {
            final IndexedCheckModel<MLocalGrid> checkModel = mListView.getCheckModel();
            final ObservableList<MLocalGrid> items = mListView.getItems();

            checkModel.getCheckedItems().addListener((ListChangeListener.Change<? extends MLocalGrid> c) -> {
                Platform.runLater(() -> {
                    items.forEach((grid) -> {
                        grid.setVisible(checkModel.isChecked(grid));
                    });
                    mManager.save();
                });
            });

            items.clear();
            if (grids != null) {
                items.addAll(grids);
                refreshCheckedStates();
            }
        });
    }

    private void refreshCheckedStates() {
        final IndexedCheckModel<MLocalGrid> checkModel = mListView.getCheckModel();
        final ObservableList<MLocalGrid> items = mListView.getItems();

        for (MLocalGrid grid : items) {
            if (grid.isVisible()) {
                checkModel.check(grid);
            } else {
                checkModel.clearCheck(grid);
            }
        }
    }
}
