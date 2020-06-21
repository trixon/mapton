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

import java.util.ArrayList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import org.mapton.api.MCooTrans;
import org.mapton.api.MFileOpener;
import org.mapton.api.MFileOpenerInput;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
class FileOpenerItemListCell extends ListCell<MFileOpenerInput> {

    private HBox mBox;
    private ComboBox<MCooTrans> mCooTransComboBox;
    private ComboBox<MFileOpener> mFileOpenerComboBox;
    private ArrayList<MFileOpener> mFileOpeners;
    private MFileOpenerInput mItem;

    public FileOpenerItemListCell(ArrayList<MFileOpener> fileOpeners) {
        mFileOpeners = fileOpeners;
        createUI();
    }

    @Override
    protected void updateItem(MFileOpenerInput item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
            clearContent();
        } else {
            addContent(item);
        }
    }

    private void addContent(MFileOpenerInput item) {
        mItem = item;
        mFileOpenerComboBox.setValue(item.getFileOpener());

        mCooTransComboBox.setValue(item.getCooTrans());

        setGraphic(mBox);
        setText(item.getFile().getName());
    }

    private void clearContent() {
        setText(null);
        setGraphic(null);
    }

    private void createUI() {
        mFileOpenerComboBox = new ComboBox<>();
        mFileOpenerComboBox.getItems().setAll(mFileOpeners);
        mFileOpenerComboBox.setPrefWidth(FxHelper.getUIScaled(100));
        mFileOpenerComboBox.setCellFactory(k -> new FileOpenerListCell());
        mFileOpenerComboBox.setButtonCell(new FileOpenerListCell());

        mFileOpenerComboBox.setOnAction(ae -> {
            var fileOpener = mFileOpenerComboBox.valueProperty().get();
            if (fileOpener != null) {
                mItem.setFileOpener(fileOpener);
            }
        });

        mCooTransComboBox = new ComboBox<>();
        mCooTransComboBox.getItems().setAll(MCooTrans.getCooTrans());
        mCooTransComboBox.setItems(mCooTransComboBox.getItems().sorted());

        mCooTransComboBox.setOnAction(ae -> {
            var cooTrans = mCooTransComboBox.valueProperty().get();
            if (cooTrans != null) {
                mItem.setCooTrans(cooTrans);
            }
        });

        mBox = new HBox(FxHelper.getUIScaled(8), mFileOpenerComboBox, mCooTransComboBox);
        mBox.setPadding(FxHelper.getUIScaledInsets(0, 12, 0, 0));
    }
}
