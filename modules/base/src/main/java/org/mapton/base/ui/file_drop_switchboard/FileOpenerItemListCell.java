/*
 * Copyright 2021 Patrik Karlström.
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
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MCooTrans;
import org.mapton.api.MCoordinateFileInput;
import org.mapton.api.MCoordinateFileOpener;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
class FileOpenerItemListCell extends ListCell<MCoordinateFileInput> {

    private HBox mBox;
    private ComboBox<MCooTrans> mCooTransComboBox;
    private final String mExt;
    private ComboBox<MCoordinateFileOpener> mCoordinateFileOpenerComboBox;
    private final ArrayList<MCoordinateFileOpener> mCoordinateFileOpeners;
    private MCoordinateFileInput mItem;

    public FileOpenerItemListCell(String ext, ArrayList<MCoordinateFileOpener> coordinateFileOpeners) {
        mExt = ext;
        mCoordinateFileOpeners = coordinateFileOpeners;
        createUI();
    }

    @Override
    protected void updateItem(MCoordinateFileInput item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
            clearContent();
        } else {
            addContent(item);
        }
    }

    private void addContent(MCoordinateFileInput item) {
        mItem = item;
        mCoordinateFileOpenerComboBox.setValue(item.getCoordinateFileOpener());

        if (StringUtils.equalsAnyIgnoreCase(mExt, "grid", "kml", "kmz")) {
            mCooTransComboBox.setValue(MCooTrans.getCooTrans("WGS 84"));
        } else {
            mCooTransComboBox.setValue(item.getCooTrans());
        }

        setGraphic(mBox);
        setText(item.getFile().getName());
    }

    private void clearContent() {
        setText(null);
        setGraphic(null);
    }

    private void createUI() {
        mCoordinateFileOpenerComboBox = new ComboBox<>();
        mCoordinateFileOpenerComboBox.getItems().setAll(mCoordinateFileOpeners);
        mCoordinateFileOpenerComboBox.setCellFactory(k -> new FileOpenerListCell());
        mCoordinateFileOpenerComboBox.setButtonCell(new FileOpenerListCell());

        mCoordinateFileOpenerComboBox.setOnAction(ae -> {
            var fileOpener = mCoordinateFileOpenerComboBox.valueProperty().get();
            if (fileOpener != null) {
                mItem.setCoordinateFileOpener(fileOpener);
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

        if (StringUtils.equalsAnyIgnoreCase(mExt, "grid", "kml", "kmz")) {
            mCooTransComboBox.setDisable(true);
        }

        mBox = new HBox(FxHelper.getUIScaled(8), mCoordinateFileOpenerComboBox, mCooTransComboBox);
        mBox.setPadding(FxHelper.getUIScaledInsets(0, 12, 0, 0));
    }
}
