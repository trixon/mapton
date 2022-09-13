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
package org.mapton.base.ui.file_drop_switchboard;

import java.io.File;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MCooTrans;
import org.mapton.api.MCoordinateFileInput;
import org.mapton.api.MCoordinateFileOpener;
import org.mapton.api.MCrsManager;
import org.mapton.api.MOptions;
import org.openide.util.NbBundle;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ExtTab extends Tab {

    private final ResourceBundle mBundle;
    private final ComboBox<MCooTrans> mCooTransComboBox = new ComboBox<>();
    private final ComboBox<MCoordinateFileOpener> mCoordinateFileOpenerComboBox = new ComboBox<>();
    private final ArrayList<MCoordinateFileOpener> mCoordinateFileOpeners;
    private final ListView<MCoordinateFileInput> mListView = new ListView<>();
    private final BorderPane mRoot = new BorderPane();
    private final String mExt;

    ExtTab(String ext, ArrayList<File> files, ArrayList<MCoordinateFileOpener> coordinateFileOpeners) {
        mExt = ext;
        mBundle = NbBundle.getBundle(FileDropSwitchboardView.class);
        mCoordinateFileOpeners = coordinateFileOpeners;
        createUI();
        initListeners();
        setText(mExt);
        files.stream()
                .sorted(File::compareTo)
                .forEachOrdered(file -> {
                    mListView.getItems().add(new MCoordinateFileInput(coordinateFileOpeners, file));
                });
    }

    ObservableList<MCoordinateFileInput> getItems() {
        return mListView.getItems();
    }

    private void createUI() {
        setClosable(false);
        mCoordinateFileOpenerComboBox.getItems().setAll(mCoordinateFileOpeners);
        mCoordinateFileOpenerComboBox.setCellFactory(k -> new FileOpenerListCell());
        mCoordinateFileOpenerComboBox.setButtonCell(new FileOpenerListCell());
        mCoordinateFileOpenerComboBox.getSelectionModel().select(0);

        mCooTransComboBox.getItems().setAll(MCrsManager.getInstance().getItems());
        mCooTransComboBox.setItems(mCooTransComboBox.getItems().sorted());
        mCooTransComboBox.getSelectionModel().select(MOptions.getInstance().getMapCooTrans());

        if (StringUtils.equalsAnyIgnoreCase(mExt, "grid", "kml", "kmz")) {
            mCooTransComboBox.setValue(MCooTrans.getCooTrans("WGS 84"));
            mCooTransComboBox.setDisable(true);
        }

        HBox headerBox = new HBox(FxHelper.getUIScaled(8));
        headerBox.setPadding(FxHelper.getUIScaledInsets(8));
        headerBox.getChildren().addAll(
                new VBox(new Label(mBundle.getString("default_openers")), mCoordinateFileOpenerComboBox),
                new VBox(new Label(mBundle.getString("default_coosys")), mCooTransComboBox)
        );

        mListView.setCellFactory(listview -> new FileOpenerItemListCell(mExt, mCoordinateFileOpeners));
        mRoot.setTop(headerBox);
        mRoot.setCenter(mListView);

        setContent(mRoot);
    }

    private void initListeners() {
        mCoordinateFileOpenerComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            mListView.getItems().forEach(item -> {
                item.setCoordinateFileOpener(newValue);
            });
            mListView.refresh();
        });

        mCooTransComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            mListView.getItems().forEach(item -> {
                item.setCooTrans(newValue);
            });
            mListView.refresh();
        });
    }
}
