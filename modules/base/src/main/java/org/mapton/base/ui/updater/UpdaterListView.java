/*
 * Copyright 2022 Patrik Karlström.
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
package org.mapton.base.ui.updater;

import java.util.ArrayList;
import java.util.Comparator;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.mapton.api.MKey;
import org.mapton.api.MPrint;
import org.mapton.api.MUpdater;
import org.openide.util.Lookup;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class UpdaterListView extends ListView<MUpdater> {

    private final MPrint mPrint = new MPrint(MKey.UPDATER_LOGGER);
    private final BooleanProperty mSelectedProperty = new SimpleBooleanProperty(false);

    public UpdaterListView() {
        setMinWidth(FxHelper.getUIScaled(350));
        setCellFactory(listView -> new UpdaterListCell());

        Lookup.getDefault().lookupResult(MUpdater.class).addLookupListener(lookupEvent -> {
            refreshUpdaters();
        });
    }

    public synchronized void refreshUpdaters() {
        new Thread(() -> {
            var updaters = new ArrayList<>(Lookup.getDefault().lookupAll(MUpdater.class));
            for (var updater : updaters) {
                updater.setMarkedForUpdate(updater.isOutOfDate());
                if (updater.isAutoUpdate()) {
                    updater.setAutoUpdatePostRunnable(() -> {
                        refreshUpdaters();
                    });
                }
                String status;
                if (updater.isOutOfDate()) {
                    status = "is out of date";
                } else {
                    status = "OK";
                }

                mPrint.out(String.format("%s: %s %s", "Status check", updater.getName(), status));
            }

            Comparator<MUpdater> c1 = (o1, o2) -> Boolean.compare(o2.isOutOfDate(), o1.isMarkedForUpdate());
            Comparator<MUpdater> c2 = (o1, o2) -> o1.getCategory().compareTo(o2.getCategory());
            Comparator<MUpdater> c3 = (o1, o2) -> o1.getName().compareTo(o2.getName());

            updaters.sort(c1.thenComparing(c2).thenComparing(c3));

            Platform.runLater(() -> {
                getItems().setAll(updaters);
                refreshSelectedProperty();
            });
        }, getClass().getCanonicalName()).start();
    }

    public BooleanProperty selectedProperty() {
        return mSelectedProperty;
    }

    private void refreshSelectedProperty() {
        var markedForUpdate = false;
        for (var item : getItems()) {
            if (item.isMarkedForUpdate()) {
                markedForUpdate = true;
                break;
            }
        }

        mSelectedProperty.set(markedForUpdate);
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
                refreshSelectedProperty();
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
            var font = Font.font(FxHelper.getScaledFontSize() * 0.9);
            var italicFont = Font.font(font.getFamily(), FontPosture.ITALIC, font.getSize());
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
