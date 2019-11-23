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
package org.mapton.core_wb.grid;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.StringConverter;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.mapton.api.MCooTrans;
import org.mapton.api.MDict;
import org.mapton.api.MLocalGrid;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class LocalGridPanel extends GridPane {

    private final ResourceBundle mBundle = NbBundle.getBundle(LocalGridPanel.class);
    private ColorPicker mColorPicker;
    private final ComboBox<MCooTrans> mCooTransComboBox = new ComboBox<>();
    private Spinner<Integer> mLatCountSpinner;
    private Spinner<Double> mLatStartSpinner;
    private Spinner<Double> mLatStepSpinner;
    private Spinner<Double> mLineWidthSpinner;
    private Spinner<Integer> mLonCountSpinner;
    private Spinner<Double> mLonStartSpinner;
    private Spinner<Double> mLonStepSpinner;
    private TextField mNameTextField;

    public LocalGridPanel() {
        createUI();
    }

    public void load(MLocalGrid grid) {
        mNameTextField.setText(grid.getName());
        mColorPicker.setValue(FxHelper.colorFromHexRGBA(grid.getColor()));

        mLatStartSpinner.getValueFactory().setValue(grid.getLatStart());
        mLatStepSpinner.getValueFactory().setValue(grid.getLatStep());
        mLatCountSpinner.getValueFactory().setValue(grid.getLatCount());

        mLonStartSpinner.getValueFactory().setValue(grid.getLonStart());
        mLonStepSpinner.getValueFactory().setValue(grid.getLonStep());
        mLonCountSpinner.getValueFactory().setValue(grid.getLonCount());

        mLineWidthSpinner.getValueFactory().setValue(grid.getLineWidth());

        if (!mCooTransComboBox.getItems().isEmpty()) {
            MCooTrans cooTrans = MCooTrans.getCooTrans(grid.getCooTrans());

            if (cooTrans == null) {
                cooTrans = mCooTransComboBox.getItems().get(0);
            }

            mCooTransComboBox.getSelectionModel().select(cooTrans);
        }
    }

    public void save(MLocalGrid grid) {
        grid.setName(mNameTextField.getText());
        grid.setCooTrans(mCooTransComboBox.getSelectionModel().getSelectedItem().getName());
        grid.setColor(FxHelper.colorToHexRGB(mColorPicker.getValue()));
        grid.setLineWidth(mLineWidthSpinner.getValue());

        grid.setLatStart(mLatStartSpinner.getValue());
        grid.setLatStep(mLatStepSpinner.getValue());
        grid.setLatCount(mLatCountSpinner.getValue());

        grid.setLonStart(mLonStartSpinner.getValue());
        grid.setLonStep(mLonStepSpinner.getValue());
        grid.setLonCount(mLonCountSpinner.getValue());
    }

    private void autoSizeColumn(GridPane gridPane, int columnCount) {
        gridPane.getColumnConstraints().clear();

        for (int i = 0; i < columnCount; i++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth(100.0 / columnCount);
            gridPane.getColumnConstraints().add(columnConstraints);
        }
    }

    private void autoSizeRegion(Region... regions) {
        for (Region region : regions) {
            GridPane.setHgrow(region, Priority.ALWAYS);
            GridPane.setFillWidth(region, true);
            region.setMaxWidth(Double.MAX_VALUE);
        }
    }

    private void createUI() {
        mNameTextField = new TextField();
        mColorPicker = new ColorPicker();

        mLatStartSpinner = new Spinner<>(Integer.MIN_VALUE, Double.MAX_VALUE, 0, 1);
        mLatStepSpinner = new Spinner<>(1, Double.MAX_VALUE, 1);
        mLatCountSpinner = new Spinner<>(1, Integer.MAX_VALUE, 1);

        mLonStartSpinner = new Spinner<>(Integer.MIN_VALUE, Double.MAX_VALUE, 0, 1);
        mLonStepSpinner = new Spinner<>(1, Double.MAX_VALUE, 1);
        mLonCountSpinner = new Spinner<>(1, Integer.MAX_VALUE, 1);

        mLineWidthSpinner = new Spinner<>(0.1, 20, 0.1);

        StringConverter<Double> converter = new StringConverter<Double>() {
            private final DecimalFormat mDecimalFormat = new DecimalFormat("#.######");

            @Override
            public Double fromString(String value) {
                try {
                    if (value == null) {
                        return null;
                    }

                    value = value.trim();

                    if (value.length() < 1) {
                        return null;
                    }

                    return mDecimalFormat.parse(value).doubleValue();
                } catch (ParseException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public String toString(Double value) {
                if (value == null) {
                    return "";
                }

                return mDecimalFormat.format(value);
            }
        };

        mLatStartSpinner.getValueFactory().setConverter(converter);
        mLonStartSpinner.getValueFactory().setConverter(converter);

        FxHelper.setEditable(true, mLatCountSpinner, mLatStartSpinner, mLatStepSpinner, mLonCountSpinner, mLonStartSpinner, mLonStepSpinner, mLineWidthSpinner);
        FxHelper.autoCommitSpinners(mLatCountSpinner, mLatStartSpinner, mLatStepSpinner, mLonCountSpinner, mLonStartSpinner, mLonStepSpinner, mLineWidthSpinner);

        Label nameLabel = new Label(Dict.NAME.toString());
        Label latStartLabel = new Label(MDict.ORIGIN.toString());
        Label latStepLabel = new Label(mBundle.getString("step"));
        Label latCountLabel = new Label(mBundle.getString("count"));

        Label lonStartLabel = new Label(MDict.ORIGIN.toString());
        Label lonStepLabel = new Label(mBundle.getString("step"));
        Label lonCountLabel = new Label(mBundle.getString("count"));

        Label latLabel = new Label(mBundle.getString("lat"));
        Label lonLabel = new Label(mBundle.getString("lon"));
        final Font font = new Font(FxHelper.getScaledFontSize() * 1.4);
        latLabel.setFont(font);
        lonLabel.setFont(font);

        Label lineWidthLabel = new Label(MDict.LINE_WIDTH.toString());
        Label cooTransLabel = new Label(MDict.COORDINATE_SYSTEM.toString());
        Label colorLabel = new Label(Dict.COLOR.toString());

        int col = 0;
        int row = 0;

        GridPane headerPane = new GridPane();
        headerPane.addRow(0, nameLabel, cooTransLabel);
        headerPane.addRow(1, mNameTextField, mCooTransComboBox);
        headerPane.addRow(2, lineWidthLabel, colorLabel);
        headerPane.addRow(3, mLineWidthSpinner, mColorPicker);
        headerPane.setHgap(8);
        autoSizeRegion(mNameTextField, mCooTransComboBox, mColorPicker, mLineWidthSpinner);
        autoSizeColumn(headerPane, 2);

        addRow(row, headerPane);

        GridPane latPane = new GridPane();
        latPane.add(latLabel, 0, 0);
        latPane.addRow(1, latStartLabel, latStepLabel, latCountLabel);
        latPane.addRow(2, mLatStartSpinner, mLatStepSpinner, mLatCountSpinner);
        latPane.setHgap(8);
        autoSizeRegion(mLatStartSpinner, mLatStepSpinner, mLatCountSpinner);
        autoSizeColumn(latPane, 3);

        addRow(++row, latPane);

        GridPane lonPane = new GridPane();
        lonPane.add(lonLabel, 0, 0);
        lonPane.addRow(1, lonStartLabel, lonStepLabel, lonCountLabel);
        lonPane.addRow(2, mLonStartSpinner, mLonStepSpinner, mLonCountSpinner);
        lonPane.setHgap(8);
        autoSizeRegion(mLonStartSpinner, mLonStepSpinner, mLonCountSpinner);
        autoSizeColumn(lonPane, 3);

        addRow(++row, lonPane);

        autoSizeRegion(headerPane, latPane, lonPane);

        final Insets rowInsets = new Insets(0, 0, 8, 0);
        GridPane.setMargin(headerPane, rowInsets);
        GridPane.setMargin(latPane, rowInsets);
        GridPane.setMargin(lonPane, rowInsets);
        GridPane.setMargin(mNameTextField, rowInsets);
        GridPane.setMargin(mCooTransComboBox, rowInsets);

        initValidation();
        setPadding(new Insets(8, 16, 0, 16));

        final Insets topInsets = new Insets(8, 0, 8, 0);
        VBox.setMargin(latStartLabel, topInsets);
        VBox.setMargin(lonStartLabel, topInsets);

        mCooTransComboBox.getItems().setAll(MCooTrans.getCooTrans());
        mCooTransComboBox.setItems(mCooTransComboBox.getItems().sorted());
    }

    private void initValidation() {
        final String text_is_required = "Text is required";
        boolean indicateRequired = false;

        ValidationSupport validationSupport = new ValidationSupport();
        Validator<Object> emptyValidator = Validator.createEmptyValidator(text_is_required);
        validationSupport.registerValidator(mNameTextField, indicateRequired, emptyValidator);

        validationSupport.validationResultProperty().addListener((ObservableValue<? extends ValidationResult> observable, ValidationResult oldValue, ValidationResult newValue) -> {
//aaa            mDialogDescriptor.setValid(!validationSupport.isInvalid());
        });

        validationSupport.initInitialDecoration();
    }
}
