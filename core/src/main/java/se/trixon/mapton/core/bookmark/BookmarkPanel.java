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
package se.trixon.mapton.core.bookmark;

import java.text.DecimalFormat;
import java.text.ParseException;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import se.trixon.almond.nbp.fx.FxDialogPanel;
import se.trixon.almond.util.Dict;
import se.trixon.mapton.core.api.DictMT;

/**
 *
 * @author Patrik Karlström
 */
public class BookmarkPanel extends FxDialogPanel {

    private TextField mCatTextField;
    private TextArea mDescTextArea;
    private Spinner<Double> mLatitudeSpinner;
    private Spinner<Double> mLongitudeSpinner;
    private TextField mNameTextField;
    private CheckBox mPlacemarkCheckBox;
    private Spinner<Double> mZoomSpinner;

    @Override
    protected void fxConstructor() {
        setScene(createScene());
    }

    void load(Bookmark bookmark) {
        mNameTextField.setText(bookmark.getName());
        mCatTextField.setText(bookmark.getCategory());
        mDescTextArea.setText(bookmark.getDescription());
        mZoomSpinner.getValueFactory().setValue(bookmark.getZoom());
        mLatitudeSpinner.getValueFactory().setValue(bookmark.getLatitude());
        mLongitudeSpinner.getValueFactory().setValue(bookmark.getLongitude());
        mPlacemarkCheckBox.setSelected(bookmark.isDisplayMarker());
    }

    void save(Bookmark bookmark) {
        Platform.runLater(() -> {
            bookmark.setName(mNameTextField.getText());
            bookmark.setCategory(StringUtils.defaultString(mCatTextField.getText()));
            bookmark.setDescription(StringUtils.defaultString(mDescTextArea.getText()));
            bookmark.setZoom(mZoomSpinner.getValue());
            bookmark.setLatitude(mLatitudeSpinner.getValue());
            bookmark.setLongitude(mLongitudeSpinner.getValue());
            bookmark.setDisplayMarker(mPlacemarkCheckBox.isSelected());
        });
    }

    private Scene createScene() {
        mNameTextField = new TextField();
        mCatTextField = new TextField();
        mDescTextArea = new TextArea();
        mZoomSpinner = new Spinner(0.0, 1.0, 0.25, 0.1);
        mLatitudeSpinner = new Spinner(-90, 90, 0, 0.000001);
        mLongitudeSpinner = new Spinner(-180, 180, 0, 0.000001);

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

        mZoomSpinner.getValueFactory().setConverter(converter);
        mLatitudeSpinner.getValueFactory().setConverter(converter);
        mLongitudeSpinner.getValueFactory().setConverter(converter);

        mPlacemarkCheckBox = new CheckBox(DictMT.DISPLAY_PLACEMARK.toString());

        mZoomSpinner.setEditable(true);
        mLatitudeSpinner.setEditable(true);
        mLongitudeSpinner.setEditable(true);

        Label nameLabel = new Label(Dict.NAME.toString());
        Label descLabel = new Label(Dict.DESCRIPTION.toString());
        Label catLabel = new Label(Dict.CATEGORY.toString());
        Label zoomLabel = new Label(Dict.ZOOM.toString());
        Label latLabel = new Label(Dict.LATITUDE.toString());
        Label lonLabel = new Label(Dict.LONGITUDE.toString());

        VBox box = new VBox(
                nameLabel,
                mNameTextField,
                catLabel,
                mCatTextField,
                descLabel,
                mDescTextArea,
                zoomLabel,
                mZoomSpinner,
                latLabel,
                mLatitudeSpinner,
                lonLabel,
                mLongitudeSpinner,
                mPlacemarkCheckBox
        );

        initValidation();
        box.setPadding(new Insets(8, 16, 0, 16));
        VBox.setVgrow(mDescTextArea, Priority.ALWAYS);

        final Insets topInsets = new Insets(8, 0, 8, 0);
        VBox.setMargin(descLabel, topInsets);
        VBox.setMargin(catLabel, topInsets);
        VBox.setMargin(zoomLabel, topInsets);
        VBox.setMargin(latLabel, topInsets);
        VBox.setMargin(lonLabel, topInsets);
        VBox.setMargin(mPlacemarkCheckBox, topInsets);

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
