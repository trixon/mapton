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
package org.mapton.worldwind;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import gov.nasa.worldwind.WorldWindowGLDrawable;
import gov.nasa.worldwind.util.measure.MeasureTool;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import static org.mapton.worldwind.ModuleOptions.*;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class RulerTab extends Tab {

    private BorderPane mBorderPane;
    private CheckBox mControlPointsCheckBox;
    private CheckBox mFollowTerrainCheckBox;
    private CheckBox mFreeHandCheckBox;
    private final BiMap<String, CheckBox> mKeyCheckBoxes = HashBiMap.create();
    private final MeasureTool mMeasureTool;
    private final ModuleOptions mOptions = ModuleOptions.getInstance();
    private CheckBox mRubberBandCheckBox;
    private ComboBox<String> mShapeComboBox;
    private CheckBox mToolTipCheckBox;
    private final WorldWindowGLDrawable mWwd;

    public RulerTab(String title, WorldWindowGLDrawable wwd, MeasureTool measureTool) {
        super(title);
        mWwd = wwd;
        mMeasureTool = measureTool;

        createUI();
        initListeners();
        postInit();
    }

    private void initStates() {
        mKeyCheckBoxes.values().forEach((checkBox) -> {
            checkBox.setSelected(mOptions.is(mKeyCheckBoxes.inverse().get(checkBox)));
        });
    }

    public MeasureTool getMeasureTool() {
        return mMeasureTool;
    }

    private void createUI() {
        mShapeComboBox = new ComboBox<>();
        mShapeComboBox.getItems().setAll(
                Dict.Shape.LINE.toString(),
                Dict.Shape.PATH.toString(),
                Dict.Shape.POLYGON.toString(),
                Dict.Shape.CIRCLE.toString(),
                Dict.Shape.ELLIPSE.toString(),
                Dict.Shape.SQUARE.toString(),
                Dict.Shape.RECTANGLE.toString()
        );
        Button armButton = new Button("arm");
        armButton.setOnAction((event) -> {
            mMeasureTool.setArmed(!mMeasureTool.isArmed());
        });

        mFollowTerrainCheckBox = new CheckBox("FOLLOW TERRAIN");
        mRubberBandCheckBox = new CheckBox("RUBBER BAND");
        mFreeHandCheckBox = new CheckBox("FREE HAND");
        mToolTipCheckBox = new CheckBox("TOOL TIP");
        mControlPointsCheckBox = new CheckBox("CONTROL POINTS");

        mFreeHandCheckBox.disableProperty().bind(mRubberBandCheckBox.selectedProperty().not());

        VBox box = new VBox(8,
                armButton,
                mFollowTerrainCheckBox,
                mRubberBandCheckBox,
                mFreeHandCheckBox,
                mToolTipCheckBox,
                mControlPointsCheckBox
        );

        mBorderPane = new BorderPane(box);
        mBorderPane.setTop(mShapeComboBox);
        mShapeComboBox.prefWidthProperty().bind(mBorderPane.widthProperty());

        setContent(mBorderPane);

        mKeyCheckBoxes.put(KEY_RULER_FOLLOW_TERRAIN, mFollowTerrainCheckBox);
        mKeyCheckBoxes.put(KEY_RULER_RUBBER_BAND, mRubberBandCheckBox);
        mKeyCheckBoxes.put(KEY_RULER_FREE_HAND, mFreeHandCheckBox);
        mKeyCheckBoxes.put(KEY_RULER_TOOL_TIP, mToolTipCheckBox);
        mKeyCheckBoxes.put(KEY_RULER_CONTROL_POINTS, mControlPointsCheckBox);
    }

    private void initListeners() {
        String shapes[] = {
            MeasureTool.SHAPE_LINE,
            MeasureTool.SHAPE_PATH,
            MeasureTool.SHAPE_POLYGON,
            MeasureTool.SHAPE_CIRCLE,
            MeasureTool.SHAPE_ELLIPSE,
            MeasureTool.SHAPE_SQUARE,
            MeasureTool.SHAPE_QUAD
        };

        mShapeComboBox.setOnAction((event) -> {
            mMeasureTool.setMeasureShapeType(shapes[mShapeComboBox.getSelectionModel().getSelectedIndex()]);
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
                case KEY_RULER_TOOL_TIP:
                    mMeasureTool.setShowAnnotation(selected);
                    break;

                default:
                    throw new AssertionError();
            }
            mWwd.redraw();
        };

        mKeyCheckBoxes.values().forEach((checkBox) -> {
            checkBox.setOnAction(eventHandler);
        });

    }

    private void postInit() {
        mShapeComboBox.getSelectionModel().select(0);
    }
}
