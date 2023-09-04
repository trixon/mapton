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
package org.mapton.api.ui.forms;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.MBaseDataManager;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.DelayedResetRunner;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 * @param <ManagerType>
 * @param <ItemType>
 */
public class ListForm<ManagerType extends MBaseDataManager, ItemType> {

    private final DelayedResetRunner mFilterDelayedResetRunner;
    private final TextArea mFilterTextArea = new TextArea();
    private StringProperty mFilterTextProperty = new SimpleStringProperty();
    private final Label mItemCountLabel = new Label();
    private ListFormConfiguration mListFormConfiguration;
    private final ListView<ItemType> mListView = new ListView<>();
    private final MBaseDataManager mManager;
    private final BorderPane mRoot = new BorderPane();
    private final String mTitle;
    private ToolBar mToolBar = new ToolBar();
    private final BorderPane mTopBorderPane = new BorderPane();

    public ListForm(MBaseDataManager manager, String title) {
        mManager = manager;
        mTitle = title;
        mFilterDelayedResetRunner = new DelayedResetRunner(200, () -> {
            mFilterTextProperty.set(mFilterTextArea.getText());
        });

        createUI();
        initListeners();
    }

    public void applyConfiguration(ListFormConfiguration lfc) {
        mListFormConfiguration = lfc;

        if (lfc.isUseTextFilter()) {
            mTopBorderPane.setCenter(mFilterTextArea);
        }

        if (lfc.getToolbarActions() != null) {
            initToolbar();
        }
    }

    public StringProperty filterTextProperty() {
        return mFilterTextProperty;
    }

    public ListView<ItemType> getListView() {
        return mListView;
    }

    public BorderPane getView() {
        return mRoot;
    }

    private void createUI() {
        var titleLabel = Mapton.createTitle(mTitle);

        mRoot.setTop(new VBox(titleLabel, mTopBorderPane));
        mRoot.setCenter(mListView);
        mRoot.setBottom(mItemCountLabel);

        mItemCountLabel.setAlignment(Pos.BASELINE_RIGHT);
        mFilterTextArea.setPromptText(Dict.FILTER.toString());
        mFilterTextArea.setPrefRowCount(4);

        mListView.itemsProperty().bind(mManager.timeFilteredItemsProperty());
        titleLabel.prefWidthProperty().bind(mRoot.widthProperty());
        mItemCountLabel.prefWidthProperty().bind(mRoot.widthProperty());
    }

    private void initListeners() {
        mManager.getTimeFilteredItems().addListener((ListChangeListener.Change c) -> {
            Platform.runLater(() -> {
                mItemCountLabel.setText("%d/%d".formatted(
                        mManager.getTimeFilteredItems().size(),
                        mManager.getAllItems().size()
                ));

                mManager.restoreSelection();
            });
        });

        mListView.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
            mManager.setSelectedItem(n);
        });

        mManager.selectedItemProperty().addListener((p, o, n) -> {
            if (mListView.getSelectionModel().getSelectedItem() != n) {
                mListView.getSelectionModel().select((ItemType) n);
                mListView.getFocusModel().focus(mListView.getItems().indexOf(n));
                FxHelper.scrollToItemIfNotVisible(mListView, n);
            }
        });

        mFilterTextArea.textProperty().addListener((p, o, n) -> {
            mFilterDelayedResetRunner.reset();
        });
    }

    private void initToolbar() {
        mToolBar = ActionUtils.createToolBar(mListFormConfiguration.getToolbarActions(), ActionUtils.ActionTextBehavior.HIDE);
        FxHelper.adjustButtonWidth(mToolBar.getItems().stream(), getIconSizeToolBarInt());
        FxHelper.undecorateButtons(mToolBar.getItems().stream());
        FxHelper.slimToolBar(mToolBar);
        mTopBorderPane.setTop(mToolBar);
    }
}
