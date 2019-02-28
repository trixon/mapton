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
package org.mapton.worldwind.ruler;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.util.measure.MeasureTool;
import java.util.ResourceBundle;
import java.util.prefs.PreferenceChangeEvent;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MDict;
import org.mapton.worldwind.ModuleOptions;
import static org.mapton.worldwind.ModuleOptions.*;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class OptionsPopOver extends BasePopOver {

    private CheckBox mAnnotationCheckBox;
    private ColorPicker mAnnotationColorPicker;
    private ColorPicker mBackgroundColorPicker;
    private final ResourceBundle mBundle = NbBundle.getBundle(RulerTab.class);
    private CheckBox mControlPointsCheckBox;
    private CheckBox mFollowTerrainCheckBox;
    private CheckBox mFreeHandCheckBox;
    private final BiMap<String, CheckBox> mKeyCheckBoxes = HashBiMap.create();
    private ColorPicker mLineColorPicker;
    private Spinner<Double> mLineWidthSpinner;
    private final MeasureTool mMeasureTool;
    private final ModuleOptions mOptions = ModuleOptions.getInstance();
    private ComboBox<String> mPathTypeComboBox;
    private final String[] mPathTypes = {AVKey.LINEAR, AVKey.RHUMB_LINE, AVKey.GREAT_CIRCLE};
    private ColorPicker mPointColorPicker;
    private CheckBox mPointListCheckBox;
    private CheckBox mRubberBandCheckBox;
    private final SimpleIntegerProperty mShapeIntegerProperty;
    private final WorldWindow mWorldWindow;

    public OptionsPopOver(MeasureTool measureTool, WorldWindow worldWindow) {
        mMeasureTool = measureTool;
        mWorldWindow = worldWindow;
        mShapeIntegerProperty = new SimpleIntegerProperty();

        setTitle(Dict.OPTIONS.toString());
        setContentNode(createUI());
        initListeners();
        initStates();
    }

    private Node createUI() {
        VBox vbox = new VBox(8);

        vbox.setPadding(new Insets(8, 16, 16, 16));
        Label pathTypeLabel = new Label(mBundle.getString("ruler.option.path_type"));
        String[] pathTypes = StringUtils.split(mBundle.getString("ruler.option.path_types"), "|");
        mPathTypeComboBox = new ComboBox<>(FXCollections.observableArrayList(pathTypes));
        mPathTypeComboBox.getSelectionModel().select(RulerTab.DEFAULT_PATH_TYPE_INDEX);

        mLineColorPicker = new ColorPicker();
        mBackgroundColorPicker = new ColorPicker();
        mPointColorPicker = new ColorPicker();
        mAnnotationColorPicker = new ColorPicker();

        mLineWidthSpinner = new Spinner<>(1.0, 10.0, 2.0);
        mLineWidthSpinner.prefWidthProperty().bind(mLineColorPicker.widthProperty());
        FxHelper.setEditable(true, mLineWidthSpinner);
        FxHelper.autoCommitSpinners(mLineWidthSpinner);

        mFollowTerrainCheckBox = new CheckBox(mBundle.getString("ruler.option.follow_terrain"));
        mRubberBandCheckBox = new CheckBox(mBundle.getString("ruler.option.rubber_band"));
        mFreeHandCheckBox = new CheckBox(mBundle.getString("ruler.option.free_hand"));
        mAnnotationCheckBox = new CheckBox(mBundle.getString("ruler.option.annotation"));
        mControlPointsCheckBox = new CheckBox(mBundle.getString("ruler.option.control_points"));
        mPointListCheckBox = new CheckBox(mBundle.getString("ruler.option.point_list"));

        mFollowTerrainCheckBox.disableProperty().bind(mShapeIntegerProperty.greaterThan(1));
        mFreeHandCheckBox.disableProperty().bind(mRubberBandCheckBox.selectedProperty().not().or(mShapeIntegerProperty.greaterThan(0).and(mShapeIntegerProperty.lessThan(3)).not()));
        mAnnotationCheckBox.disableProperty().bind(mControlPointsCheckBox.selectedProperty().not());

        vbox.getChildren().setAll(
                new VBox(new Label(Dict.Geometry.LINE.toString()), mLineColorPicker),
                new VBox(new Label(MDict.LINE_WIDTH.toString()), mLineWidthSpinner),
                new VBox(new Label(Dict.BACKGROUND.toString()), mBackgroundColorPicker),
                new VBox(new Label(Dict.Geometry.POINT.toString()), mPointColorPicker),
                new VBox(new Label(mBundle.getString("ruler.option.annotation")), mAnnotationColorPicker),
                new VBox(pathTypeLabel, mPathTypeComboBox),
                mFollowTerrainCheckBox,
                mRubberBandCheckBox,
                mFreeHandCheckBox, mControlPointsCheckBox, mAnnotationCheckBox, mPointListCheckBox
        );

        mKeyCheckBoxes.put(ModuleOptions.KEY_RULER_FOLLOW_TERRAIN, mFollowTerrainCheckBox);
        mKeyCheckBoxes.put(ModuleOptions.KEY_RULER_RUBBER_BAND, mRubberBandCheckBox);
        mKeyCheckBoxes.put(ModuleOptions.KEY_RULER_FREE_HAND, mFreeHandCheckBox);
        mKeyCheckBoxes.put(ModuleOptions.KEY_RULER_ANNOTATION, mAnnotationCheckBox);
        mKeyCheckBoxes.put(ModuleOptions.KEY_RULER_CONTROL_POINTS, mControlPointsCheckBox);
        mKeyCheckBoxes.put(ModuleOptions.KEY_RULER_POINT_LIST, mPointListCheckBox);

        return vbox;
    }

    private java.awt.Color initColors(ColorPicker colorPicker, String key, Color defaultColor) {
        Color color = FxHelper.colorFromHexRGBA(mOptions.get(key, FxHelper.colorToHexRGBA(defaultColor)));
        colorPicker.setValue(color);

        return FxHelper.colorToColor(color);
    }

    private void initListeners() {
        mOptions.getPreferences().addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            Platform.runLater(() -> {
                switch (evt.getKey()) {
                    case KEY_RULER_SHAPE:
                        mShapeIntegerProperty.set(mOptions.getInt(KEY_RULER_SHAPE));
                        break;
                }
            });
        });

        EventHandler<ActionEvent> eventHandler = (ActionEvent event) -> {
            CheckBox checkBox = (CheckBox) event.getSource();
            String key = mKeyCheckBoxes.inverse().get(checkBox);
            boolean selected = checkBox.isSelected();
            mOptions.put(key, selected);
            switch (key) {
                case KEY_RULER_CONTROL_POINTS:
                    mMeasureTool.setShowControlPoints(selected);
                    break;

                case KEY_RULER_FOLLOW_TERRAIN:
                    mMeasureTool.setFollowTerrain(selected);
                    break;

                case KEY_RULER_FREE_HAND:
                    mMeasureTool.getController().setFreeHand(selected);
                    break;

                case KEY_RULER_RUBBER_BAND:
                    mMeasureTool.getController().setUseRubberBand(selected);
                    break;

                case KEY_RULER_ANNOTATION:
                    mMeasureTool.setShowAnnotation(selected);
                    break;
            }
            mWorldWindow.redraw();
        };

        mKeyCheckBoxes.values().forEach((checkBox) -> {
            checkBox.setOnAction(eventHandler);
        });

        mPathTypeComboBox.setOnAction((event) -> {
            int index = mPathTypeComboBox.getSelectionModel().getSelectedIndex();
            mMeasureTool.setPathType(mPathTypes[index]);
            mOptions.put(KEY_RULER_PATH_TYPE, index);
        });

        EventHandler<ActionEvent> colorActionEvent = (event) -> {
            ColorPicker source = (ColorPicker) event.getSource();
            Color color = source.getValue();
            java.awt.Color awtColor = FxHelper.colorToColor(color);
            String key = null;

            if (source == mLineColorPicker) {
                key = KEY_RULER_COLOR_LINE;
                mMeasureTool.setLineColor(awtColor);
            } else if (source == mBackgroundColorPicker) {
                key = KEY_RULER_COLOR_BACKGROUND;
                mMeasureTool.setFillColor(awtColor);
            } else if (source == mPointColorPicker) {
                key = KEY_RULER_COLOR_POINT;
                mMeasureTool.getControlPointsAttributes().setBackgroundColor(awtColor);
            } else if (source == mAnnotationColorPicker) {
                key = KEY_RULER_COLOR_ANNOTATION;
                mMeasureTool.getAnnotationAttributes().setTextColor(awtColor);
            }

            mOptions.put(key, FxHelper.colorToHexRGBA(color));
            mWorldWindow.redraw();
        };

        mLineColorPicker.setOnAction(colorActionEvent);
        mBackgroundColorPicker.setOnAction(colorActionEvent);
        mPointColorPicker.setOnAction(colorActionEvent);
        mAnnotationColorPicker.setOnAction(colorActionEvent);

        mLineWidthSpinner.valueProperty().addListener((ObservableValue<? extends Double> observable, Double oldValue, Double newValue) -> {
            mMeasureTool.setLineWidth(newValue);
            mOptions.put(KEY_RULER_LINE_WIDTH, newValue);
        });
    }

    private void initStates() {
        mKeyCheckBoxes.values().forEach((checkBox) -> {
            checkBox.setSelected(mOptions.is(mKeyCheckBoxes.inverse().get(checkBox)));
        });

        mMeasureTool.setLineColor(initColors(mLineColorPicker, KEY_RULER_COLOR_LINE, Color.YELLOW));
        mMeasureTool.setFillColor(initColors(mBackgroundColorPicker, KEY_RULER_COLOR_BACKGROUND, Color.web("FFFF0030")));
        mMeasureTool.getControlPointsAttributes().setBackgroundColor(initColors(mPointColorPicker, KEY_RULER_COLOR_POINT, Color.BLUE));
        mMeasureTool.getAnnotationAttributes().setTextColor(initColors(mAnnotationColorPicker, KEY_RULER_COLOR_ANNOTATION, Color.WHITESMOKE));

        mPointListCheckBox.setSelected(mOptions.is(ModuleOptions.KEY_RULER_POINT_LIST, false));
        mShapeIntegerProperty.set(mOptions.getInt(KEY_RULER_SHAPE));
        int index = mOptions.getInt(KEY_RULER_PATH_TYPE, 1);
        mPathTypeComboBox.getSelectionModel().select(index);
        mMeasureTool.setPathType(mPathTypes[index]);

        double lineWidth = mOptions.getDouble(KEY_RULER_LINE_WIDTH, 3.0);
        mLineWidthSpinner.getValueFactory().setValue(lineWidth);
        mMeasureTool.setLineWidth(lineWidth);
    }
}
