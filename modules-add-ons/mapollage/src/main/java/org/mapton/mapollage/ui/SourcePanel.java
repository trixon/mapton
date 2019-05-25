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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.mapton.mapollage.api.MapollageSource;
import org.openide.util.NbBundle;
import se.trixon.almond.nbp.fx.FxDialogPanel;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class SourcePanel extends FxDialogPanel {

    private final ResourceBundle mBundle = NbBundle.getBundle(SourcePanel.class);
    private TextField mNameTextField;

    public void load(MapollageSource source) {
        mNameTextField.setText(source.getName());
    }

    public void save(MapollageSource source) {
        source.setName(mNameTextField.getText());
    }

    @Override
    protected void fxConstructor() {
        setScene(createScene());
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

    private Scene createScene() {
        mNameTextField = new TextField();

        Label nameLabel = new Label(Dict.NAME.toString());

        Font defaultFont = Font.getDefault();
        final Font font = new Font(defaultFont.getSize() * 1.4);

        GridPane gp = new GridPane();
        int col = 0;
        int row = 0;

        GridPane headerPane = new GridPane();
        headerPane.addRow(0, nameLabel);
        headerPane.addRow(1, mNameTextField);
        headerPane.setHgap(8);
        autoSizeRegion(mNameTextField);
        autoSizeColumn(headerPane, 2);

        gp.addRow(row, headerPane);

        autoSizeRegion(headerPane);

        final Insets rowInsets = new Insets(0, 0, 8, 0);
        GridPane.setMargin(headerPane, rowInsets);
        GridPane.setMargin(mNameTextField, rowInsets);

        initValidation();
        gp.setPadding(new Insets(8, 16, 0, 16));

        final Insets topInsets = new Insets(8, 0, 8, 0);

        return new Scene(gp);
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
