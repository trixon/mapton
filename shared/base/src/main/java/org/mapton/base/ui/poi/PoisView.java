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
package org.mapton.base.ui.poi;

import java.util.ArrayList;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.textfield.TextFields;
import org.mapton.api.MDict;
import org.mapton.api.MPoi;
import org.mapton.api.MPoiManager;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class PoisView extends BorderPane {

    private PoiCategoryCheckTreeView mCategoryCheckTreeView;
    private TextField mFilterTextField;
    private Label mItemCountLabel;
    private ListView<MPoi> mListView;
    private final MPoiManager mManager = MPoiManager.getInstance();

    public PoisView() {
        createUI();
        initListeners();

        mManager.refresh("");
        mCategoryCheckTreeView.populate();
    }

    private void createUI() {
        mFilterTextField = TextFields.createClearableTextField();
        mFilterTextField.setPromptText(String.format("%s %s", Dict.SEARCH.toString(), MDict.POI.toString()));
        mFilterTextField.setMinWidth(20);

        mListView = new ListView<>();
        mListView.itemsProperty().bind(mManager.filteredItemsProperty());
        mListView.setCellFactory(param -> new PoiListCell());

        Action refreshAction = new Action(Dict.REFRESH.toString(), event -> {
            mManager.refresh("");
            mCategoryCheckTreeView.populate();
        });
        refreshAction.setGraphic(MaterialIcon._Navigation.REFRESH.getImageView(getIconSizeToolBarInt()));

        Action filterAction = new Action(Dict.FILTER.toString(), event -> {
        });
        filterAction.setGraphic(MaterialIcon._Content.FILTER_LIST.getImageView(getIconSizeToolBarInt()));
        filterAction.setDisabled(true);

        Action optionsAction = new Action(Dict.OPTIONS.toString(), event -> {
        });
        optionsAction.setGraphic(MaterialIcon._Action.SETTINGS.getImageView(getIconSizeToolBarInt()));
        optionsAction.setDisabled(true);

        ArrayList<Action> actions = new ArrayList<>();
        actions.add(refreshAction);
        actions.add(filterAction);
        actions.add(optionsAction);

        ToolBar toolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);
        FxHelper.adjustButtonWidth(toolBar.getItems().stream(), getIconSizeToolBarInt());
        FxHelper.undecorateButtons(toolBar.getItems().stream());
        BorderPane topBorderPane = new BorderPane(mFilterTextField);
        topBorderPane.setRight(toolBar);
        toolBar.setMinWidth(getIconSizeToolBarInt() * 4.5);

        FxHelper.slimToolBar(toolBar);

        Label titleLabel = Mapton.createTitle(MDict.POI.toString());
        VBox topBox = new VBox(
                titleLabel,
                topBorderPane
        );
        mItemCountLabel = new Label();
        mItemCountLabel.setAlignment(Pos.BASELINE_RIGHT);
        setTop(topBox);
        setCenter(mListView);
        BorderPane bottomBorderPane = new BorderPane(mCategoryCheckTreeView = new PoiCategoryCheckTreeView());
        bottomBorderPane.setBottom(mItemCountLabel);
        setBottom(bottomBorderPane);

        titleLabel.prefWidthProperty().bind(widthProperty());
        mItemCountLabel.prefWidthProperty().bind(widthProperty());
    }

    private void initListeners() {
        mFilterTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            mManager.refresh(newValue);
        });

        mManager.getFilteredItems().addListener((ListChangeListener.Change<? extends MPoi> c) -> {
            mItemCountLabel.setText(String.format("%d/%d", mManager.getFilteredItems().size(), mManager.getAllItems().size()));
            mCategoryCheckTreeView.populate();
        });
    }

    class PoiListCell extends ListCell<MPoi> {

        private final Label mDesc1Label = new Label();
        private final Label mNameLabel = new Label();
        private final String mStyleBold = "-fx-font-weight: bold;";
        private VBox mVBox;

        public PoiListCell() {
            createUI();
        }

        @Override
        protected void updateItem(MPoi poi, boolean empty) {
            super.updateItem(poi, empty);
            if (poi == null || empty) {
                clearContent();
            } else {
                addContent(poi);
            }
        }

        private void addContent(MPoi poi) {
            setText(null);

            mNameLabel.setText(poi.getName());
            mDesc1Label.setText(String.format("%s: %s", poi.getProvider(), poi.getCategory()));
            setTooltip(new Tooltip(ToStringBuilder.reflectionToString(poi, ToStringStyle.MULTI_LINE_STYLE)));

            setGraphic(mVBox);
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
        }

        private void createUI() {
            mNameLabel.setStyle(mStyleBold);

            mVBox = new VBox(
                    mNameLabel,
                    mDesc1Label
            );
        }
    }
}
