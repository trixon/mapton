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
package org.mapton.workbench.modules;

import com.dlsc.workbenchfx.view.controls.ToolbarItem;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import java.util.ArrayList;
import java.util.Comparator;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.mapton.api.MKey;
import org.mapton.api.MMaskerPaneBase;
import org.mapton.api.MPrint;
import org.mapton.api.MUpdater;
import org.mapton.workbench.api.MWorkbenchModule;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GlobalStateChangeEvent;
import se.trixon.almond.util.LogListener;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.LogPanel;

/**
 *
 * @author Patrik Karlström
 */
public class UpdaterModule extends MWorkbenchModule implements LogListener {

    private BorderPane mBorderPane;
    private ListView<MUpdater> mListView;
    private LogPanel mLogPanel;
    private final BooleanProperty mRunningProperty = new SimpleBooleanProperty(false);
    private UpdaterMaskerPane mUpdaterMaskerPane;
    private MPrint mPrint = new MPrint(MKey.UPDATER_LOGGER);

    public UpdaterModule() {
        super(Dict.UPDATER.toString(), MaterialDesignIcon.DOWNLOAD);

        createUI();

        Lookup.getDefault().lookupResult(MUpdater.class).addLookupListener((LookupEvent ev) -> {
            refresh();
        });

        mGlobalState.addListener((GlobalStateChangeEvent evt) -> {
            mLogPanel.println((String) evt.getObject());
        }, MKey.UPDATER_LOGGER);

        refresh();
    }

    @Override
    public Node activate() {
        return mBorderPane;
    }

    @Override
    public void println(String s) {
        mLogPanel.println(s);
    }

    private void createUI() {
        mUpdaterMaskerPane = new UpdaterMaskerPane();
        var refreshToolbarItem = new ToolbarItem(Dict.REFRESH.toString(), new MaterialDesignIconView(MaterialDesignIcon.SYNC), event -> {
            refresh();
        });

        var updateToolbarItem = new ToolbarItem(Dict.UPDATE.toString(), new MaterialDesignIconView(MaterialDesignIcon.DOWNLOAD), event -> {
            for (MUpdater updater : mListView.getItems()) {
                if (updater.isMarkedForUpdate()) {
                    mUpdaterMaskerPane.update();
                    break;
                }
            }
        });

        refreshToolbarItem.disableProperty().bind(mRunningProperty);
        updateToolbarItem.disableProperty().bind(mRunningProperty);

        getToolbarControlsLeft().addAll(refreshToolbarItem, updateToolbarItem);

        mLogPanel = new LogPanel();
        mLogPanel.setMonospaced();

        mListView = new ListView<>();
        mListView.setCellFactory((ListView<MUpdater> param) -> new UpdaterListCell());
        mListView.setMinWidth(350);
        mUpdaterMaskerPane.setContent(mListView);

        mBorderPane = new BorderPane(mLogPanel);
        mBorderPane.setLeft(mUpdaterMaskerPane.getNode());
    }

    private void refresh() {
        new Thread(() -> {
            ArrayList<MUpdater> updaters = new ArrayList<>(Lookup.getDefault().lookupAll(MUpdater.class));
            for (MUpdater updater : updaters) {
                updater.setMarkedForUpdate(updater.isOutOfDate());
                String status;
                if (updater.isOutOfDate()) {
                    status = "is out of date";
                } else {
                    status = "OK";
                }

                mPrint.out(String.format("%s: %s %s", Dict.UPDATER.toString(), updater.getName(), status));
            }

            Comparator<MUpdater> c1 = (MUpdater o1, MUpdater o2) -> Boolean.compare(o2.isOutOfDate(), o1.isMarkedForUpdate());
            Comparator<MUpdater> c2 = (MUpdater o1, MUpdater o2) -> o1.getCategory().compareTo(o2.getCategory());
            Comparator<MUpdater> c3 = (MUpdater o1, MUpdater o2) -> o1.getName().compareTo(o2.getName());

            updaters.sort(c1.thenComparing(c2).thenComparing(c3));

            Platform.runLater(() -> {
                mListView.getItems().setAll(updaters);
            });
        }).start();
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
            mNameCheckBox.setFont(mHeaderFont);
            Font font = Font.font(FxHelper.getScaledFontSize() * 0.9);
            Font italicFont = Font.font(font.getFamily(), FontPosture.ITALIC, font.getSize());
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

    class UpdaterMaskerPane extends MMaskerPaneBase {

        void update() {
            mMaskerPane.setVisible(true);
            mRunningProperty.set(true);

            new Thread(() -> {
                for (MUpdater updater : mListView.getItems()) {
                    if (updater.isMarkedForUpdate()) {
                        mPrint.out(String.format("%s: %s", updater.getName(), "UPDATE BEGIN"));
                        updater.run();
                        mPrint.out(String.format("%s: %s", updater.getName(), "UPDATE END"));
                    }
                }

                Platform.runLater(() -> {
                    refresh();
                    mMaskerPane.setVisible(false);
                    notify(Dict.OPERATION_COMPLETED.toString());
                    mRunningProperty.set(false);
                });
            }).start();
        }
    }
}
