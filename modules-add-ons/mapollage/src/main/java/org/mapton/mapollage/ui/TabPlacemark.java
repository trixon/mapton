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
package org.mapton.mapollage.ui;

import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import org.mapton.mapollage.api.MapollageState;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class TabPlacemark extends TabBase {

    private final ResourceBundle mBundle = SystemHelper.getBundle(TabPlacemark.class, "Bundle");

    private final ComboBox<String> mDatePatternComboBox = new ComboBox<>();
    private final RadioButton mNameByDateRadioButton = new RadioButton(Dict.DATE_PATTERN.toString());
    private final RadioButton mNameByFileRadioButton = new RadioButton(Dict.FILENAME.toString());
    private final RadioButton mNameByNoRadioButton = new RadioButton(mBundle.getString("TabPlacemark.nameByNoRadioButton"));
    private final ToggleGroup mNameByToggleGroup = new ToggleGroup();
    private final RadioButton mSymbolAsPhotoRadioButton = new RadioButton(Dict.PHOTO.toString());
    private final RadioButton mSymbolAsPinRadioButton = new RadioButton(Dict.PIN.toString());
    private final ToggleGroup mSymbolToggleGroup = new ToggleGroup();

    public TabPlacemark(MapollageState mapollageState) {
        setText(Dict.PLACEMARK.toString());
        mMapollageState = mapollageState;
        createUI();
//        load();
    }

    private void createUI() {
        VBox vBox = new VBox();
        VBox leftBox = new VBox();
        VBox rightBox = new VBox();
        vBox.getChildren().addAll(leftBox, rightBox);
        setScrollPaneContent(vBox);

        mNameByFileRadioButton.setToggleGroup(mNameByToggleGroup);
        mNameByDateRadioButton.setToggleGroup(mNameByToggleGroup);
        mNameByNoRadioButton.setToggleGroup(mNameByToggleGroup);

        mDatePatternComboBox.setMaxWidth(Double.MAX_VALUE);
        mDatePatternComboBox.setEditable(true);
        mDatePatternComboBox.setItems(FXCollections.observableList(Arrays.asList(mBundle.getString("dateFormats").split(";"))));

        Insets leftInsets = new Insets(0, 0, 0, 24);
        VBox.setMargin(mDatePatternComboBox, leftInsets);

        leftBox.getChildren().addAll(
                new Label(mBundle.getString("TabPlacemark.nameByLabel")),
                mNameByFileRadioButton,
                mNameByDateRadioButton,
                mDatePatternComboBox,
                mNameByNoRadioButton
        );

        mSymbolAsPhotoRadioButton.setToggleGroup(mSymbolToggleGroup);
        mSymbolAsPinRadioButton.setToggleGroup(mSymbolToggleGroup);

        rightBox.getChildren().addAll(
                new Label(Dict.SYMBOL.toString()),
                mSymbolAsPhotoRadioButton,
                mSymbolAsPinRadioButton
        );

        FxHelper.setPadding(
                new Insets(8, 0, 0, 0),
                mNameByFileRadioButton,
                mNameByDateRadioButton,
                mNameByNoRadioButton,
                rightBox,
                mSymbolAsPhotoRadioButton,
                mSymbolAsPinRadioButton
        );

        mDatePatternComboBox.disableProperty().bind(mNameByDateRadioButton.selectedProperty().not());
    }

}
