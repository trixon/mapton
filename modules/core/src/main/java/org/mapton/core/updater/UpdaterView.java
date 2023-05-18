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
package org.mapton.core.updater;

import java.util.Arrays;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.MKey;
import org.mapton.api.MUpdater;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.LogListener;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.LogPanel;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class UpdaterView extends BorderPane implements LogListener {

    private ListView<MUpdater> mListView;
    private LogPanel mLogPanel;
    private final UpdaterManager mUpdaterManager = UpdaterManager.getInstance();
    private UpdaterMaskerPane mUpdaterMaskerPane;

    public UpdaterView() {
        createUI();

        Mapton.getGlobalState().addListener(gsce -> {
            mLogPanel.println((String) gsce.getObject());
        }, MKey.UPDATER_LOGGER);
    }

    @Override
    public void println(String s) {
        mLogPanel.println(s);
    }

    private void createUI() {
        mListView = new ListView<>();
        mListView.setMinWidth(FxHelper.getUIScaled(350));
        mListView.setCellFactory(listView -> new UpdaterListCell());
        mListView.itemsProperty().bind(mUpdaterManager.itemsProperty());

        mUpdaterMaskerPane = new UpdaterMaskerPane();
        mUpdaterMaskerPane.setContent(mListView);

        mLogPanel = new LogPanel();
        mLogPanel.setMonospaced();

        var updateAction = new Action(Dict.UPDATE.toString(), event -> {
            update();
        });
        updateAction.setGraphic(MaterialIcon._Action.SYSTEM_UPDATE_ALT.getImageView(getIconSizeToolBarInt()));

        var refreshAction = new Action(Dict.REFRESH.toString(), event -> {
            mUpdaterManager.populate();
        });
        refreshAction.setGraphic(MaterialIcon._Navigation.REFRESH.getImageView(getIconSizeToolBarInt()));

        var clearAction = new Action(Dict.CLEAR.toString(), event -> {
            mLogPanel.clear();
        });
        clearAction.setGraphic(MaterialIcon._Content.CLEAR.getImageView(getIconSizeToolBarInt()));

        var actions = Arrays.asList(
                refreshAction,
                updateAction,
                clearAction
        );

        var toolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.SHOW);
        FxHelper.undecorateButtons(toolBar.getItems().stream());
        FxHelper.slimToolBar(toolBar);

        updateAction.disabledProperty().bind(mUpdaterMaskerPane.runningProperty().or(mUpdaterManager.selectedProperty().not()));
        refreshAction.disabledProperty().bind(mUpdaterMaskerPane.runningProperty());

        setLeft(mUpdaterMaskerPane.getNode());
        setCenter(mLogPanel);
        setTop(toolBar);

        SystemHelper.runLaterDelayed(1000, () -> {
            mUpdaterManager.populate();
        });

    }

    private void update() {
        for (var updater : mListView.getItems()) {
            if (updater.isMarkedForUpdate()) {
                mUpdaterMaskerPane.update(mListView.getItems(), () -> {
                    mUpdaterManager.populate();
                });
                break;
            }
        }
    }

    class UpdaterListCell extends ListCell<MUpdater> {

        private final Label mCategoryLabel = new Label();
        private final Label mCommentLabel = new Label();
        private final Font mHeaderFont;
        private final Label mLastUpdatedLabel = new Label();
        private final CheckBox mNameCheckBox = new CheckBox();
        private VBox mVBox;

        public UpdaterListCell() {
            mHeaderFont = Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, FxHelper.getScaledFontSize() * 1.1);
            createUI();
        }

        @Override
        protected void updateItem(MUpdater updater, boolean empty) {
            super.updateItem(updater, empty);
            if (updater == null || empty) {
                clearContent();
            } else {
                addContent(updater);
            }
        }

        private void addContent(MUpdater updater) {
            setText(null);

            mNameCheckBox.setText(updater.getName());
            mNameCheckBox.setSelected(updater.isOutOfDate());
            mNameCheckBox.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
                updater.setMarkedForUpdate(t1);
                mUpdaterManager.refreshSelectedProperty();
            });

            mCategoryLabel.setText(updater.getCategory());
            mCommentLabel.setText(updater.getComment());
            mLastUpdatedLabel.setText(updater.getLastUpdated());

            setGraphic(mVBox);
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
        }

        private void createUI() {
            var font = Font.font(FxHelper.getScaledFontSize() * 0.9);
            var italicFont = Font.font(font.getFamily(), FontPosture.ITALIC, font.getSize());

            mNameCheckBox.setFont(mHeaderFont);
            mCategoryLabel.setFont(font);
            mCommentLabel.setFont(italicFont);
            mLastUpdatedLabel.setFont(font);

            mVBox = new VBox(
                    FxHelper.getUIScaled(2),
                    mCategoryLabel,
                    mNameCheckBox,
                    mCommentLabel,
                    mLastUpdatedLabel
            );

            mVBox.setPadding(FxHelper.getUIScaledInsets(4));
        }
    }
}
