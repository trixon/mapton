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
package org.mapton.addon.photos.ui;

import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.mapton.addon.photos.api.MapoSource;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.FileChooserPane;

/**
 *
 * @author Patrik Karlström
 */
public class SourceView extends VBox {

    private final ResourceBundle mBundle;
    private TextField mDescTextField;
    private final NotifyDescriptor mNotifyDescriptor;
    private TextField mExcludeTextField;
    private TextField mFilePatternField;
    private CheckBox mLinksCheckBox;
    private TextField mNameTextField;
    private CheckBox mRecursiveCheckBox;
    private FileChooserPane mSourceChooser;
    private Spinner<Integer> mThumbnailBorderSizeSpinner;
    private Spinner<Integer> mThumbnailSizeSpinner;
    private CheckBox mThumnailForceCreation;
    private HBox mhBox;
    private ColorPicker mColorPicker;

    public SourceView(NotifyDescriptor notifyDescriptor) {
        mNotifyDescriptor = notifyDescriptor;
        mBundle = NbBundle.getBundle(SourceView.class);
        createUI();
    }

    public void load(MapoSource source) {
        mNameTextField.setText(source.getName());
        mDescTextField.setText(source.getDescriptionString());

        mSourceChooser.setPath(source.getDir());
        mExcludeTextField.setText(source.getExcludePattern());
        mFilePatternField.setText(source.getFilePattern());

        mRecursiveCheckBox.setSelected(source.isRecursive());
        mLinksCheckBox.setSelected(source.isFollowLinks());
        mThumnailForceCreation.setSelected(source.isThumbnailForceCreation());

        mThumbnailSizeSpinner.getValueFactory().setValue(source.getThumbnailSize());
        mThumbnailBorderSizeSpinner.getValueFactory().setValue(source.getThumbnailBorderSize());

        Color color = Color.YELLOW;
        try {
            color = FxHelper.colorFromHexRGBA(source.getThumbnailBorderColor());
        } catch (Exception e) {
        }
        mColorPicker.setValue(color);
    }

    public void save(MapoSource source) {
        source.setName(mNameTextField.getText());
        source.setDescriptionString(mDescTextField.getText());

        source.setDir(mSourceChooser.getPath());
        source.setExcludePattern(mExcludeTextField.getText());
        source.setFilePattern(mFilePatternField.getText());

        source.setRecursive(mRecursiveCheckBox.isSelected());
        source.setFollowLinks(mLinksCheckBox.isSelected());
        source.setThumbnailForceCreation(mThumnailForceCreation.isSelected());

        source.setThumbnailSize(mThumbnailSizeSpinner.getValue());
        source.setThumbnailBorderSize(mThumbnailBorderSizeSpinner.getValue());
        source.setThumbnailBorderColor(FxHelper.colorToHexRGB(mColorPicker.getValue()));
    }

    private void createUI() {
        mhBox = new HBox(8);

        mNameTextField = new TextField();
        mDescTextField = new TextField();
        mExcludeTextField = new TextField();
        mFilePatternField = new TextField();
        mLinksCheckBox = new CheckBox(Dict.FOLLOW_LINKS.toString());
        mRecursiveCheckBox = new CheckBox(Dict.SUBDIRECTORIES.toString());
        mSourceChooser = new FileChooserPane(Dict.SELECT.toString(), Dict.IMAGE_DIRECTORY.toString(), FileChooserPane.ObjectMode.DIRECTORY, SelectionMode.SINGLE);
        mThumnailForceCreation = new CheckBox(mBundle.getString("TabSource.forceThumbnail"));

        Label nameLabel = new Label(Dict.NAME.toString());
        Label descLabel = new Label(Dict.DESCRIPTION.toString());
        Label filePatternLabel = new Label(Dict.FILE_PATTERN.toString());
        Label excludeLabel = new Label(mBundle.getString("TabSource.excludeLabel"));
        Label thumbnailLabel = new Label(Dict.IMAGE_SIZE.toString());
        Label borderSizeLabel = new Label(mBundle.getString("TabSource.borderSizeLabel"));
        Label colorLabel = new Label(Dict.COLOR.toString());

        mExcludeTextField.setTooltip(new Tooltip(mBundle.getString("TabSource.excludeTextField.toolTip")));

        mColorPicker = new ColorPicker();
        mThumbnailBorderSizeSpinner = new Spinner<>(0, 20, 2, 1);
        mThumbnailSizeSpinner = new Spinner<>(100, 1200, 250, 10);
        mThumbnailSizeSpinner.setEditable(true);
        mThumbnailBorderSizeSpinner.setEditable(true);

        FxHelper.autoCommitSpinners(
                mThumbnailBorderSizeSpinner,
                mThumbnailSizeSpinner
        );

        GridPane thumbGridPane = new GridPane();
        thumbGridPane.addColumn(0,
                thumbnailLabel,
                mThumbnailSizeSpinner
        );
        thumbGridPane.addColumn(1,
                borderSizeLabel,
                mThumbnailBorderSizeSpinner
        );
        thumbGridPane.addColumn(2,
                colorLabel,
                mColorPicker
        );

        mhBox.getChildren().addAll(
                mRecursiveCheckBox,
                mLinksCheckBox,
                mThumnailForceCreation
        );

        FxHelper.setPadding(
                FxHelper.getUIScaledInsets(8, 0, 0, 0),
                descLabel,
                mSourceChooser,
                filePatternLabel,
                excludeLabel,
                mhBox,
                thumbGridPane
        );

        FxHelper.setMargin(
                FxHelper.getUIScaledInsets(0, 0, 0, 16),
                borderSizeLabel,
                mThumbnailBorderSizeSpinner,
                colorLabel,
                mColorPicker
        );

        getChildren().addAll(
                nameLabel,
                mNameTextField,
                descLabel,
                mDescTextField,
                mSourceChooser,
                filePatternLabel,
                mFilePatternField,
                excludeLabel,
                mExcludeTextField,
                mhBox,
                thumbGridPane
        );

        setPadding(FxHelper.getUIScaledInsets(8, 16, 0, 16));
        initValidation();
    }

    private void initValidation() {
        final String text_is_required = "Text is required";
        boolean indicateRequired = false;

        ValidationSupport validationSupport = new ValidationSupport();
        Validator<Object> emptyValidator = Validator.createEmptyValidator(text_is_required);
        validationSupport.registerValidator(mNameTextField, indicateRequired, emptyValidator);

        validationSupport.validationResultProperty().addListener((ObservableValue<? extends ValidationResult> observable, ValidationResult oldValue, ValidationResult newValue) -> {
            mNotifyDescriptor.setValid(!validationSupport.isInvalid());
        });

        validationSupport.initInitialDecoration();
    }

}
