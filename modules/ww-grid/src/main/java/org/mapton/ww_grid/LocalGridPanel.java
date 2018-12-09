/*
 * Copyright 2018 Patrik Karlström.
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
package org.mapton.ww_grid;

import java.text.DecimalFormat;
import java.text.ParseException;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.mapton.api.MCooTrans;
import se.trixon.almond.nbp.fx.FxDialogPanel;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class LocalGridPanel extends FxDialogPanel {

    private final ComboBox<MCooTrans> mComboBox = new ComboBox();

    private Spinner<Integer> mLatCountSpinner;
    private Spinner<Double> mLatStartSpinner;
    private Spinner<Double> mLatStepSpinner;
    private Spinner<Double> mLineWidthSpinner;
    private Spinner<Integer> mLonCountSpinner;
    private Spinner<Double> mLonStartSpinner;
    private Spinner<Double> mLonStepSpinner;
    private TextField mNameTextField;

    public void load(LocalGrid grid) {
        mNameTextField.setText(grid.getName());

        mLatStartSpinner.getValueFactory().setValue(grid.getLatStart());
        mLatStepSpinner.getValueFactory().setValue(grid.getLatStep());
        mLatCountSpinner.getValueFactory().setValue(grid.getLatCount());

        mLonStartSpinner.getValueFactory().setValue(grid.getLonStart());
        mLonStepSpinner.getValueFactory().setValue(grid.getLonStep());
        mLonCountSpinner.getValueFactory().setValue(grid.getLonCount());

        mLineWidthSpinner.getValueFactory().setValue(grid.getLineWidth());

        if (!mComboBox.getItems().isEmpty()) {
            MCooTrans cooTrans = MCooTrans.getCooTrans(grid.getCooTrans());

            if (cooTrans == null) {
                cooTrans = mComboBox.getItems().get(0);
            }

            mComboBox.getSelectionModel().select(cooTrans);
        }
    }

    public void save(LocalGrid grid) {
        grid.setName(mNameTextField.getText());
        grid.setLatStart(mLatStartSpinner.getValue());
        grid.setLatStep(mLatStepSpinner.getValue());
        grid.setLatCount(mLatCountSpinner.getValue());

        grid.setLonStart(mLonStartSpinner.getValue());
        grid.setLonStep(mLonStepSpinner.getValue());
        grid.setLonCount(mLonCountSpinner.getValue());

        grid.setLineWidth(mLineWidthSpinner.getValue());

        grid.setCooTrans(mComboBox.getSelectionModel().getSelectedItem().getName());
    }

    @Override
    protected void fxConstructor() {
        setScene(createScene());
    }

    private Scene createScene() {
        mNameTextField = new TextField();

        mLatStartSpinner = new Spinner<>(Integer.MIN_VALUE, Double.MAX_VALUE, 0, 1);
        mLatStepSpinner = new Spinner<>(1, Double.MAX_VALUE, 1);
        mLatCountSpinner = new Spinner<>(1, Integer.MAX_VALUE, 1);

        mLonStartSpinner = new Spinner(Integer.MIN_VALUE, Double.MAX_VALUE, 0, 1);
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
        Label latStartLabel = new Label("START LAT");
        Label latStepLabel = new Label("STEP LAT");
        Label latCountLabel = new Label("COUNT LAT");

        Label lonStartLabel = new Label("START LON");
        Label lonStepLabel = new Label("STEP LON");
        Label lonCountLabel = new Label("COUNT LON");

        Label lineWidthLabel = new Label("LINE WIDTH");
        Label cooTransLabel = new Label("COO TRANS");

        VBox box = new VBox(
                nameLabel,
                mNameTextField,
                cooTransLabel,
                mComboBox,
                latStartLabel,
                mLatStartSpinner,
                lonStartLabel,
                mLonStartSpinner,
                latStepLabel,
                mLatStepSpinner,
                lonStepLabel,
                mLonStepSpinner,
                latCountLabel,
                mLatCountSpinner,
                lonCountLabel,
                mLonCountSpinner,
                lineWidthLabel,
                mLineWidthSpinner
        );

        initValidation();
        box.setPadding(new Insets(8, 16, 0, 16));

        final Insets topInsets = new Insets(8, 0, 8, 0);
        VBox.setMargin(latStartLabel, topInsets);
        VBox.setMargin(lonStartLabel, topInsets);

        mComboBox.getItems().setAll(MCooTrans.getCooTrans());
        mComboBox.setItems(mComboBox.getItems().sorted());

        return new Scene(box);
    }

    private void initValidation() {
        final String text_is_required = "Text is required";
        boolean indicateRequired = false;

        ValidationSupport validationSupport = new ValidationSupport();
        Validator<Object> emptyValidator = Validator.createEmptyValidator(text_is_required);
        validationSupport.registerValidator(mNameTextField, indicateRequired, emptyValidator);

        validationSupport.validationResultProperty().addListener((ObservableValue<? extends ValidationResult> observable, ValidationResult oldValue, ValidationResult newValue) -> {
            mDialogDescriptor.setValid(!validationSupport.isInvalid());
        });

        validationSupport.initInitialDecoration();
    }
}
