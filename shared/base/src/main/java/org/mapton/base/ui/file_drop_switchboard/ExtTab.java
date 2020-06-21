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
import org.mapton.api.MCooTrans;
import org.mapton.api.MFileOpener;
import org.mapton.api.MFileOpenerInput;
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
    private final ComboBox<MFileOpener> mFileOpenerComboBox = new ComboBox<>();
    private final ArrayList<MFileOpener> mFileOpeners;
    private final ListView<MFileOpenerInput> mListView = new ListView<>();
    private final BorderPane mRoot = new BorderPane();

    ExtTab(String title, ArrayList<File> files, ArrayList<MFileOpener> fileOpeners) {
        mBundle = NbBundle.getBundle(FileDropSwitchboardView.class);
        mFileOpeners = fileOpeners;
        createUI();
        initListeners();
        setText(title);
        files.stream()
                .sorted(File::compareTo)
                .forEachOrdered(file -> {
                    mListView.getItems().add(new MFileOpenerInput(fileOpeners, file));
                });
    }

    ObservableList<MFileOpenerInput> getItems() {
        return mListView.getItems();
    }

    private void createUI() {
        setClosable(false);
        mFileOpenerComboBox.setPrefWidth(FxHelper.getUIScaled(100));
        mFileOpenerComboBox.getItems().setAll(mFileOpeners);
        mFileOpenerComboBox.setCellFactory(k -> new FileOpenerListCell());
        mFileOpenerComboBox.setButtonCell(new FileOpenerListCell());
        mFileOpenerComboBox.getSelectionModel().select(0);

        mCooTransComboBox.getItems().setAll(MCooTrans.getCooTrans());
        mCooTransComboBox.setItems(mCooTransComboBox.getItems().sorted());
        mCooTransComboBox.getSelectionModel().select(MOptions.getInstance().getMapCooTrans());

        HBox headerBox = new HBox(FxHelper.getUIScaled(8));
        headerBox.setPadding(FxHelper.getUIScaledInsets(8));
        headerBox.getChildren().addAll(
                new VBox(new Label(mBundle.getString("default_openers")), mFileOpenerComboBox),
                new VBox(new Label(mBundle.getString("default_coosys")), mCooTransComboBox)
        );

        mListView.setCellFactory(listview -> new FileOpenerItemListCell(mFileOpeners));
        mRoot.setTop(headerBox);
        mRoot.setCenter(mListView);

        setContent(mRoot);
    }

    private void initListeners() {
        mFileOpenerComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            mListView.getItems().forEach(item -> {
                item.setFileOpener(newValue);
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
