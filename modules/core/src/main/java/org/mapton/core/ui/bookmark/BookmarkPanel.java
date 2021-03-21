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
package org.mapton.core.ui.bookmark;

import java.text.DecimalFormat;
import java.text.ParseException;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.mapton.api.MBookmark;
import org.mapton.api.MBookmarkManager;
import org.mapton.api.MDict;
import se.trixon.almond.nbp.fx.FxDialogPanel;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class BookmarkPanel extends FxDialogPanel {

    private BookmarkView mBookmarkView;

    public BookmarkPanel() {
    }

    public void load(MBookmark bookmark) {
        mBookmarkView.load(bookmark);
    }

    public void save(MBookmark bookmark) {
        mBookmarkView.save(bookmark);
    }

    @Override
    protected void fxConstructor() {
        mBookmarkView = new BookmarkView();
        setScene(new Scene(mBookmarkView));
    }

    class BookmarkView extends StackPane {

        private ComboBox<String> mCategoryComboBox;
        private ColorPicker mColorPicker;
        private TextArea mDescTextArea;
        private TextField mUrlTextField;
        private Spinner<Double> mLatitudeSpinner;
        private Spinner<Double> mLongitudeSpinner;
        private MBookmarkManager mManager = MBookmarkManager.getInstance();
        private TextField mNameTextField;
        private CheckBox mPlacemarkCheckBox;
        private Spinner<Double> mZoomSpinner;

        public BookmarkView() {
            createUI();
        }

        public void load(MBookmark bookmark) {
            mNameTextField.setText(bookmark.getName());
            mCategoryComboBox.getSelectionModel().select(bookmark.getCategory());
            mDescTextArea.setText(bookmark.getDescription());
            mUrlTextField.setText(bookmark.getUrl());
            mZoomSpinner.getValueFactory().setValue(bookmark.getZoom());
            mLatitudeSpinner.getValueFactory().setValue(bookmark.getLatitude());
            mLongitudeSpinner.getValueFactory().setValue(bookmark.getLongitude());
            mPlacemarkCheckBox.setSelected(bookmark.isDisplayMarker());
            Color color = Color.YELLOW;
            try {
                color = FxHelper.colorFromHexRGBA(bookmark.getColor());
            } catch (Exception e) {
            }
            mColorPicker.setValue(color);
        }

        public void save(MBookmark bookmark) {
            bookmark.setName(mNameTextField.getText());
            bookmark.setCategory(StringUtils.defaultString(mCategoryComboBox.getSelectionModel().getSelectedItem()));
            bookmark.setDescription(StringUtils.defaultString(mDescTextArea.getText()));
            bookmark.setUrl(StringUtils.defaultString(mUrlTextField.getText()));
            bookmark.setZoom(mZoomSpinner.getValue());
            bookmark.setLatitude(mLatitudeSpinner.getValue());
            bookmark.setLongitude(mLongitudeSpinner.getValue());
            bookmark.setDisplayMarker(mPlacemarkCheckBox.isSelected());
            bookmark.setColor(FxHelper.colorToHexRGB(mColorPicker.getValue()));
        }

        private void createUI() {
            mNameTextField = new TextField();
            mCategoryComboBox = new ComboBox<>();
            mDescTextArea = new TextArea();
            mUrlTextField = new TextField();
            mZoomSpinner = new Spinner<>(0.0, 1.0, 0.25, 0.1);
            mLatitudeSpinner = new Spinner<>(-90, 90, 0, 0.000001);
            mLongitudeSpinner = new Spinner<>(-180, 180, 0, 0.000001);
            mColorPicker = new ColorPicker();
            mCategoryComboBox.prefWidthProperty().bind(mNameTextField.widthProperty());
            mCategoryComboBox.setEditable(true);
            mCategoryComboBox.getItems().setAll(mManager.getCategories());

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

            mPlacemarkCheckBox = new CheckBox(MDict.DISPLAY_PLACEMARK.toString());

            mZoomSpinner.setEditable(true);
            mLatitudeSpinner.setEditable(true);
            mLongitudeSpinner.setEditable(true);
            FxHelper.autoCommitSpinners(mZoomSpinner, mLatitudeSpinner, mLongitudeSpinner);

            mDescTextArea.setPrefHeight(20);

            Label nameLabel = new Label(Dict.NAME.toString());
            Label descLabel = new Label(Dict.DESCRIPTION.toString());
            Label categoryLabel = new Label(Dict.CATEGORY.toString());
            Label colorLabel = new Label(Dict.COLOR.toString());
            Label zoomLabel = new Label(Dict.ZOOM.toString());
            Label latLabel = new Label(Dict.LATITUDE.toString());
            Label lonLabel = new Label(Dict.LONGITUDE.toString());
            Label urlLabel = new Label("URL");

            VBox box = new VBox(
                    nameLabel,
                    mNameTextField,
                    categoryLabel,
                    mCategoryComboBox,
                    descLabel,
                    mDescTextArea,
                    urlLabel,
                    mUrlTextField,
                    colorLabel,
                    mColorPicker,
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
            VBox.setMargin(categoryLabel, topInsets);
            VBox.setMargin(zoomLabel, topInsets);
            VBox.setMargin(latLabel, topInsets);
            VBox.setMargin(lonLabel, topInsets);
            VBox.setMargin(colorLabel, topInsets);
            VBox.setMargin(mPlacemarkCheckBox, topInsets);

            getChildren().setAll(box);
        }

        private void initValidation() {
            final String text_is_required = "Text is required";
            boolean indicateRequired = false;

            ValidationSupport validationSupport = new ValidationSupport();
            Validator<Object> emptyValidator = Validator.createEmptyValidator(text_is_required);
            validationSupport.registerValidator(mNameTextField, indicateRequired, emptyValidator);
            Validator<String> uniqueValidator = Validator.createPredicateValidator((String s) -> {
                return true;//Only used to trigger validation
            }, "The combination of name and category has to be unique");

            validationSupport.registerValidator(mCategoryComboBox, indicateRequired, uniqueValidator);
            validationSupport.validationResultProperty().addListener((ObservableValue<? extends ValidationResult> observable, ValidationResult oldValue, ValidationResult newValue) -> {
                //TODO
//            mDialogDescriptor.setValid(!validationSupport.isInvalid()
//                    && !mManager.exists(mBookmark.getId(), mNameTextField.getText().trim(), mCategoryComboBox.getSelectionModel().getSelectedItem().trim())
//            );
            });

            validationSupport.initInitialDecoration();
        }
    }
}
