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
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;
import org.mapton.worldwind.ModuleOptions;
import static org.mapton.worldwind.ModuleOptions.*;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
class OptionsPane extends VBox {

    private CheckBox mAnnotationCheckBox;
    private ColorPicker mAnnotationColorPicker;
    private final ResourceBundle mBundle = NbBundle.getBundle(RulerTab.class);
    private CheckBox mControlPointsCheckBox;
    private CheckBox mFollowTerrainCheckBox;
    private CheckBox mFreeHandCheckBox;
    private final BiMap<String, CheckBox> mKeyCheckBoxes = HashBiMap.create();
    private ColorPicker mLineColorPicker;
    private MeasureTool mMeasureTool;
    private final ModuleOptions mOptions = ModuleOptions.getInstance();
    private ComboBox<String> mPathTypeComboBox;
    private final String[] mPathTypes = {AVKey.LINEAR, AVKey.RHUMB_LINE, AVKey.GREAT_CIRCLE};
    private ColorPicker mPointColorPicker;
    private CheckBox mPointListCheckBox;
    private CheckBox mRubberBandCheckBox;
    private SimpleIntegerProperty mShapeIntegerProperty = new SimpleIntegerProperty();
    private WorldWindow mWorldWindow;

    public OptionsPane(MeasureTool measureTool, WorldWindow worldWindow) {
        mMeasureTool = measureTool;
        mWorldWindow = worldWindow;

        createUI();
        initListeners();
        initStates();

        mMeasureTool.setPathType(mPathTypes[RulerTab.DEFAULT_PATH_TYPE_INDEX]);
    }

    private void createUI() {
        setPadding(new Insets(8, 16, 16, 16));
        setSpacing(8);
        Label pathTypeLabel = new Label(mBundle.getString("ruler.option.path_type"));
        String[] pathTypes = StringUtils.split(mBundle.getString("ruler.option.path_types"), "|");
        mPathTypeComboBox = new ComboBox<>(FXCollections.observableArrayList(pathTypes));
        mPathTypeComboBox.getSelectionModel().select(RulerTab.DEFAULT_PATH_TYPE_INDEX);

        mLineColorPicker = new ColorPicker(Color.YELLOW);
        mPointColorPicker = new ColorPicker(Color.BLUE);
        mAnnotationColorPicker = new ColorPicker(Color.WHITE);

        mFollowTerrainCheckBox = new CheckBox(mBundle.getString("ruler.option.follow_terrain"));
        mRubberBandCheckBox = new CheckBox(mBundle.getString("ruler.option.rubber_band"));
        mFreeHandCheckBox = new CheckBox(mBundle.getString("ruler.option.free_hand"));
        mAnnotationCheckBox = new CheckBox(mBundle.getString("ruler.option.annotation"));
        mControlPointsCheckBox = new CheckBox(mBundle.getString("ruler.option.control_points"));
        mPointListCheckBox = new CheckBox(mBundle.getString("ruler.option.point_list"));

        mFollowTerrainCheckBox.disableProperty().bind(mShapeIntegerProperty.greaterThan(1));
        mFreeHandCheckBox.disableProperty().bind(mRubberBandCheckBox.selectedProperty().not().or(mShapeIntegerProperty.greaterThan(0).and(mShapeIntegerProperty.lessThan(3)).not()));
        mAnnotationCheckBox.disableProperty().bind(mControlPointsCheckBox.selectedProperty().not());

        getChildren().setAll(new VBox(pathTypeLabel, mPathTypeComboBox), new VBox(new Label(Dict.Geometry.LINE.toString()), mLineColorPicker), new VBox(new Label(Dict.Geometry.POINT.toString()), mPointColorPicker), new VBox(new Label(mBundle.getString("ruler.option.annotation")), mAnnotationColorPicker), mFollowTerrainCheckBox, mRubberBandCheckBox, mFreeHandCheckBox, mControlPointsCheckBox, mAnnotationCheckBox, mPointListCheckBox);

        mKeyCheckBoxes.put(ModuleOptions.KEY_RULER_FOLLOW_TERRAIN, mFollowTerrainCheckBox);
        mKeyCheckBoxes.put(ModuleOptions.KEY_RULER_RUBBER_BAND, mRubberBandCheckBox);
        mKeyCheckBoxes.put(ModuleOptions.KEY_RULER_FREE_HAND, mFreeHandCheckBox);
        mKeyCheckBoxes.put(ModuleOptions.KEY_RULER_ANNOTATION, mAnnotationCheckBox);
        mKeyCheckBoxes.put(ModuleOptions.KEY_RULER_CONTROL_POINTS, mControlPointsCheckBox);
        mKeyCheckBoxes.put(ModuleOptions.KEY_RULER_POINT_LIST, mPointListCheckBox);
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
            mMeasureTool.setPathType(mPathTypes[mPathTypeComboBox.getSelectionModel().getSelectedIndex()]);
        });

        mLineColorPicker.setOnAction((event) -> {
            mMeasureTool.setLineColor(FxHelper.colorToColor(mLineColorPicker.getValue()));
            mWorldWindow.redraw();
        });

        mPointColorPicker.setOnAction((event) -> {
            mMeasureTool.getControlPointsAttributes().setBackgroundColor(FxHelper.colorToColor(mPointColorPicker.getValue()));
            mWorldWindow.redraw();
        });

        mAnnotationColorPicker.setOnAction((event) -> {
            mMeasureTool.getAnnotationAttributes().setTextColor(FxHelper.colorToColor(mAnnotationColorPicker.getValue()));
            mWorldWindow.redraw();
        });
    }

    private void initStates() {
        mKeyCheckBoxes.values().forEach((checkBox) -> {
            checkBox.setSelected(mOptions.is(mKeyCheckBoxes.inverse().get(checkBox)));
        });
        mPointListCheckBox.setSelected(mOptions.is(ModuleOptions.KEY_RULER_POINT_LIST, false));
    }
}
