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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import org.controlsfx.glyphfont.FontAwesome;
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
    private final RadioButton mNameByNoRadioButton = new RadioButton(mBundle.getString("Placemark.nameByNoRadioButton"));
    private final ToggleGroup mNameByToggleGroup = new ToggleGroup();
    private final Spinner<Double> mScaleSpinner = new Spinner(0.5, 10.0, 1.0, 0.1);
    private final RadioButton mSymbolAsPhotoRadioButton = new RadioButton(Dict.PHOTO.toString());
    private final RadioButton mSymbolAsPinRadioButton = new RadioButton(Dict.PIN.toString());
    private final ToggleGroup mSymbolToggleGroup = new ToggleGroup();
    private final CheckBox mTimestampCheckBox = new CheckBox(mBundle.getString("Placemark.timestampCheckBox"));
    private final Spinner<Double> mZoomSpinner = new Spinner(1.0, 10.0, 1.0, 0.1);

    public TabPlacemark(MapollageState mapollageState) {
        setText(Dict.PLACEMARK.toString());
        setGraphic(FontAwesome.Glyph.MAP_MARKER.getChar());
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

        mScaleSpinner.setEditable(true);
        mZoomSpinner.setEditable(true);
        FxHelper.autoCommitSpinners(mScaleSpinner, mZoomSpinner);

        mNameByFileRadioButton.setToggleGroup(mNameByToggleGroup);
        mNameByDateRadioButton.setToggleGroup(mNameByToggleGroup);
        mNameByNoRadioButton.setToggleGroup(mNameByToggleGroup);

        mDatePatternComboBox.setMaxWidth(Double.MAX_VALUE);
        mDatePatternComboBox.setEditable(true);
        mDatePatternComboBox.setItems(FXCollections.observableList(Arrays.asList(mBundle.getString("dateFormats").split(";"))));

        Insets leftInsets = new Insets(0, 0, 0, 24);
        VBox.setMargin(mDatePatternComboBox, leftInsets);

        leftBox.getChildren().addAll(
                new Label(mBundle.getString("Placemark.nameByLabel")),
                mNameByFileRadioButton,
                mNameByDateRadioButton,
                mDatePatternComboBox,
                mNameByNoRadioButton
        );

        mSymbolAsPhotoRadioButton.setToggleGroup(mSymbolToggleGroup);
        mSymbolAsPinRadioButton.setToggleGroup(mSymbolToggleGroup);
        Label scaleLabel = new Label(Dict.SCALE.toString());
        Label zoomLabel = new Label(Dict.ZOOM.toString());

        Insets topInsets = new Insets(8, 0, 0, 0);
        VBox.setMargin(mTimestampCheckBox, topInsets);

        rightBox.getChildren().addAll(
                new Label(Dict.SYMBOL.toString()),
                mSymbolAsPhotoRadioButton,
                mSymbolAsPinRadioButton,
                scaleLabel,
                mScaleSpinner,
                zoomLabel,
                mZoomSpinner,
                mTimestampCheckBox
        );

        FxHelper.setPadding(
                new Insets(8, 0, 0, 0),
                mNameByFileRadioButton,
                mNameByDateRadioButton,
                mNameByNoRadioButton,
                rightBox,
                mSymbolAsPhotoRadioButton,
                mSymbolAsPinRadioButton,
                scaleLabel,
                zoomLabel
        );

        mDatePatternComboBox.disableProperty().bind(mNameByDateRadioButton.selectedProperty().not());
    }

}
