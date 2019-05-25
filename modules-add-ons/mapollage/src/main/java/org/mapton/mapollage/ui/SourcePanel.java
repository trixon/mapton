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

import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.mapton.mapollage.api.MapollageSource;
import org.openide.util.NbBundle;
import se.trixon.almond.nbp.fx.FxDialogPanel;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.FileChooserPane;

/**
 *
 * @author Patrik Karlström
 */
public class SourcePanel extends FxDialogPanel {

    private ResourceBundle mBundle;
    private TextField mDescTextField;
    private TextField mExcludeTextField;
    private TextField mFilePatternField;
    private CheckBox mLinksCheckBox;
    private TextField mNameTextField;
    private CheckBox mRecursiveCheckBox;
    private FileChooserPane mSourceChooser;
    private HBox mhBox;
    private VBox mvBox;

    public SourcePanel() {
    }

    public void load(MapollageSource source) {
        mNameTextField.setText(source.getName());
        mDescTextField.setText(source.getDescriptionString());

        mSourceChooser.setPath(source.getDir());
        mExcludeTextField.setText(source.getExcludePattern());
        mFilePatternField.setText(source.getFilePattern());

        mRecursiveCheckBox.setSelected(source.isRecursive());
        mLinksCheckBox.setSelected(source.isFollowLinks());
    }

    public void save(MapollageSource source) {
        source.setName(mNameTextField.getText());
        source.setDescriptionString(mDescTextField.getText());

        source.setDir(mSourceChooser.getPath());
        source.setExcludePattern(mExcludeTextField.getText());
        source.setFilePattern(mFilePatternField.getText());

        source.setRecursive(mRecursiveCheckBox.isSelected());
        source.setFollowLinks(mLinksCheckBox.isSelected());
    }

    @Override
    protected void fxConstructor() {
        mBundle = NbBundle.getBundle(SourcePanel.class);
        setScene(createScene());
    }

    private Scene createScene() {
        mhBox = new HBox(8);
        mvBox = new VBox();

        mNameTextField = new TextField();
        mDescTextField = new TextField();
        mExcludeTextField = new TextField();
        mFilePatternField = new TextField();
        mLinksCheckBox = new CheckBox(Dict.FOLLOW_LINKS.toString());
        mRecursiveCheckBox = new CheckBox(Dict.SUBDIRECTORIES.toString());
        mSourceChooser = new FileChooserPane(Dict.SELECT.toString(), Dict.IMAGE_DIRECTORY.toString(), FileChooserPane.ObjectMode.DIRECTORY, SelectionMode.SINGLE);

        Label nameLabel = new Label(Dict.NAME.toString());
        Label descLabel = new Label(Dict.DESCRIPTION.toString());
        Label filePatternLabel = new Label(Dict.FILE_PATTERN.toString());
        Label excludeLabel = new Label(mBundle.getString("Source.excludeLabel"));

        mExcludeTextField.setTooltip(new Tooltip(mBundle.getString("Source.excludeTextField.toolTip")));

        mhBox.getChildren().addAll(mRecursiveCheckBox, mLinksCheckBox);

        FxHelper.addTopPadding(
                new Insets(8, 0, 0, 0),
                descLabel,
                mSourceChooser,
                filePatternLabel,
                excludeLabel,
                mhBox
        );

        mvBox.getChildren().addAll(
                nameLabel,
                mNameTextField,
                descLabel,
                mDescTextField,
                mSourceChooser,
                filePatternLabel,
                mFilePatternField,
                excludeLabel,
                mExcludeTextField,
                mhBox
        );
        mvBox.setPadding(new Insets(8, 16, 0, 16));

        initValidation();

        return new Scene(mvBox);
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
