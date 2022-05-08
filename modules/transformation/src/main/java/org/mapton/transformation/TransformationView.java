/*
 * Copyright 2022 Patrik Karlström.
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
package org.mapton.transformation;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.mapton.api.MCrsManager;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.LogPanel;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.almond.util.io.CoordinateFormat;
import se.trixon.almond.util.io.Geo;
import se.trixon.almond.util.io.GeoPoint;
import se.trixon.almond.util.swing.dialogs.SimpleDialog;

/**
 *
 * @author Patrik Karlström
 */
public class TransformationView extends BorderPane {

    private final ResourceBundle mBundle = NbBundle.getBundle(TransformationView.class);
    private final ComboBox<CoordinateReferenceSystem> mDestComboBox = new ComboBox<>();
    private Geo mDestGeo;
    private final LogPanel mDestLogPanel = new LogPanel();
    private File mFile;
    private final MCrsManager mManager = MCrsManager.getInstance();
    private final ComboBox<CoordinateReferenceSystem> mSourceComboBox = new ComboBox<>();
    private Geo mSourceGeo;
    private final LogPanel mSourceLogPanel = new LogPanel();
    private final Spinner<Integer> mXYSpinner = new Spinner<>(0, 9, 3);
    private final Spinner<Integer> mZSpinner = new Spinner<>(0, 9, 3);
    private File mDestination;
    private final Label mSourceLabel = new Label();

    public TransformationView() {
        createUI();
        initListeners();

        mSourceComboBox.setItems(mManager.getSelectedSystems());
        mDestComboBox.setItems(mManager.getSelectedSystems());
    }

    private void clear() {
        mSourceLogPanel.clear();
        mDestLogPanel.clear();
        mFile = null;
        mSourceGeo = null;
        mDestGeo = null;
        mSourceLabel.setText("");
    }

    private void createUI() {
        var transformAction = new Action(mBundle.getString("transform"), event -> {
            if (ObjectUtils.allNotNull(mFile, mSourceComboBox.getValue(), mDestComboBox.getValue())) {
                transform();
            }
        });
        transformAction.setGraphic(MaterialIcon._Image.LEAK_REMOVE.getImageView(getIconSizeToolBarInt()));

        var saveAction = new Action(Dict.SAVE.toString(), event -> {
            save();
        });
        saveAction.setGraphic(MaterialIcon._Content.SAVE.getImageView(getIconSizeToolBarInt()));

        var clearAction = new Action(Dict.CLEAR.toString(), event -> {
            clear();
        });
        clearAction.setGraphic(MaterialIcon._Content.CLEAR.getImageView(getIconSizeToolBarInt()));

        var actions = Arrays.asList(
                transformAction,
                saveAction,
                ActionUtils.ACTION_SPAN,
                clearAction
        );

        var toolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.SHOW);
        FxHelper.slimToolBar(toolBar);
        FxHelper.undecorateButtons(toolBar.getItems().stream());

        var gridPane = new GridPane();
        var fromLabel = new Label(Dict.FROM.toString());

        var toLabel = new Label(Dict.TO.toString());
        var xyLabel = new Label("XY:");
        var zLabel = new Label("Z:");
        var sourceBox = new HBox(
                fromLabel,
                mSourceComboBox,
                mSourceLabel
        );
        var destBox = new HBox(
                toLabel,
                mDestComboBox,
                xyLabel,
                mXYSpinner,
                zLabel,
                mZSpinner
        );

        sourceBox.setAlignment(Pos.CENTER_LEFT);
        destBox.setAlignment(Pos.CENTER_LEFT);

        var insets = FxHelper.getUIScaledInsets(0, 0, 0, 8);
        FxHelper.setPadding(insets, mSourceLabel, xyLabel, zLabel);

        gridPane.addRow(0, sourceBox, destBox);
        gridPane.addRow(1, mSourceLogPanel, mDestLogPanel);
        FxHelper.autoSizeColumn(gridPane, 2);
        gridPane.prefHeightProperty().bind(heightProperty());

        mSourceLogPanel.setPromptText(mBundle.getString("prompt_source"));
        mSourceLogPanel.setFont(Font.font("monospaced"));
        mSourceLogPanel.prefHeightProperty().bind(gridPane.heightProperty());

        mDestLogPanel.setFont(Font.font("monospaced"));

        mSourceComboBox.setCellFactory(list -> new CrsListCell());
        mSourceComboBox.setButtonCell(new CrsListCell());

        mDestComboBox.setCellFactory(list -> new CrsListCell());
        mDestComboBox.setButtonCell(new CrsListCell());

        setTop(toolBar);
        setCenter(gridPane);
    }

    private void initListeners() {
        mSourceLogPanel.setOnDragOver(dragEvent -> {
            var dragboard = dragEvent.getDragboard();
            if (dragboard.hasFiles()) {
                dragEvent.acceptTransferModes(TransferMode.COPY);
            }
        });

        mSourceLogPanel.setOnDragDropped(dragEvent -> {
            open(dragEvent.getDragboard().getFiles().get(0));
        });
    }

    private void open(File file) {
        mFile = file;
        mSourceLabel.setText(file.getAbsolutePath());
        try {
            mSourceGeo = new Geo();
            mSourceGeo.read(file);
            mSourceLogPanel.setText(mSourceGeo.toString());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void save() {
        SimpleDialog.clearFilters();
        SimpleDialog.addFilters("geo");
        SimpleDialog.setFilter("geo");
        SimpleDialog.setTitle(String.format("%s %s", Dict.SAVE.toString(), Dict.COORDINATE_FILE.toString().toLowerCase()));
        var toSystem = StringUtils.substringAfter(mDestComboBox.getValue().getName().toString(), ":");
        var name = String.format("%s_%s.geo", FilenameUtils.getBaseName(mFile.getName()), toSystem);
        var file = new File(mFile.getParentFile(), name);
        SimpleDialog.setSelectedFile(file);
        if (mDestination == null) {
            SimpleDialog.setPath(FileUtils.getUserDirectory());
        } else {
            SimpleDialog.setPath(mDestination.getParentFile());
        }

        if (SimpleDialog.saveFile()) {
            mDestination = SimpleDialog.getPath();
            new Thread(() -> {
                try {
                    switch (FilenameUtils.getExtension(mDestination.getName())) {
                        case "geo":
                            mDestGeo.write(mDestination);
                            break;

                        default:
                            throw new AssertionError();
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }, getClass().getCanonicalName()).start();
        }
    }

    private void transform() {
        var fromCrs = mSourceComboBox.getValue();
        var toCrs = mDestComboBox.getValue();

        try {
            mDestGeo = new Geo();
            mDestGeo.read(mFile);
            mDestGeo.setCoordinateFormat(CoordinateFormat.FORMATTED);
            var xyValue = mXYSpinner.getValue();
            var zValue = mZSpinner.getValue();
            GeoPoint.setDecimalsX(xyValue);
            GeoPoint.setDecimalsY(xyValue);
            GeoPoint.setDecimalsZ(zValue);

            var mathTransform = CRS.findMathTransform(fromCrs, toCrs, false);
            for (var line : mDestGeo.getLines()) {
                for (var point : line.getPoints()) {
                    transform(mathTransform, point);
                }
            }

            for (var point : mDestGeo.getPoints()) {
                transform(mathTransform, point);
            }

            mDestLogPanel.setText(mDestGeo.toString());
        } catch (FactoryException ex) {
            Exceptions.printStackTrace(ex);
        } catch (MismatchedDimensionException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void transform(MathTransform mathTransform, GeoPoint point) {
        try {
            var dp = mathTransform.transform(new DirectPosition2D(point.getX(), point.getY()), null);
            point.setX(dp.getCoordinate()[0]);
            point.setY(dp.getCoordinate()[1]);
        } catch (MismatchedDimensionException ex) {
            Exceptions.printStackTrace(ex);
        } catch (TransformException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

}
