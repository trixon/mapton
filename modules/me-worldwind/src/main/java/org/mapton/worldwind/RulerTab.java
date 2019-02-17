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
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.util.measure.MeasureTool;
import gov.nasa.worldwind.util.measure.MeasureToolController;
import java.awt.Component;
import java.awt.Cursor;
import java.beans.PropertyChangeEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.SegmentedButton;
import static org.mapton.api.Mapton.getIconSizeToolBar;
import static org.mapton.worldwind.ModuleOptions.*;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.icons.material.MaterialIcon;

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
    private ImageView mPauseImageView;
    private ToggleButton mPauseToggleButton;
    private ToggleButton mPlayToggleButton;
    private ImageView mResumeImageView;
    private CheckBox mRubberBandCheckBox;
    private ToggleButton mSettingsToggleButton;
    private ComboBox<String> mShapeComboBox;
    private ToggleButton mStopToggleButton;
    private CheckBox mToolTipCheckBox;
    private final WorldWindow mWorldWindow;

    public RulerTab(String title, WorldWindow worldWindow) {
        super(title);
        mWorldWindow = worldWindow;
        mMeasureTool = new MeasureTool(mWorldWindow);
        mMeasureTool.setController(new MeasureToolController());

        createUI();
        initListeners();
        postInit();
        initStates();
    }

    public MeasureTool getMeasureTool() {
        return mMeasureTool;
    }

    private void createUI() {
        final Insets insets8 = new Insets(8);

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

        mFollowTerrainCheckBox = new CheckBox("FOLLOW TERRAIN");
        mRubberBandCheckBox = new CheckBox("RUBBER BAND");
        mFreeHandCheckBox = new CheckBox("FREE HAND");
        mToolTipCheckBox = new CheckBox("TOOL TIP");
        mControlPointsCheckBox = new CheckBox("CONTROL POINTS");

        mFreeHandCheckBox.disableProperty().bind(mRubberBandCheckBox.selectedProperty().not());

        int imageSize = getIconSizeToolBar();
        mPauseImageView = MaterialIcon._Av.PAUSE_CIRCLE_OUTLINE.getImageView(imageSize);
        mResumeImageView = MaterialIcon._Av.PLAY_CIRCLE_OUTLINE.getImageView(imageSize);

        mPlayToggleButton = new ToggleButton("", MaterialIcon._Av.PLAY_ARROW.getImageView(imageSize));
        mPauseToggleButton = new ToggleButton("", mPauseImageView);
        mStopToggleButton = new ToggleButton("", MaterialIcon._Av.STOP.getImageView(imageSize));
        mSettingsToggleButton = new ToggleButton("", MaterialIcon._Action.SETTINGS.getImageView(imageSize));
        mPauseToggleButton.setDisable(true);
        mStopToggleButton.setDisable(true);

        SegmentedButton segmentedButton = new SegmentedButton();
        segmentedButton.getButtons().addAll(mPlayToggleButton, mPauseToggleButton, mStopToggleButton, mSettingsToggleButton);

        VBox box = new VBox(8,
                new BorderPane(segmentedButton),
                mFollowTerrainCheckBox,
                mRubberBandCheckBox,
                mFreeHandCheckBox,
                mToolTipCheckBox,
                mControlPointsCheckBox
        );

        box.setPadding(insets8);

        mBorderPane = new BorderPane(box);
        VBox topBox = new VBox(mShapeComboBox);
        topBox.setPadding(insets8);

        mBorderPane.setTop(topBox);

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
            mWorldWindow.redraw();
        };

        mKeyCheckBoxes.values().forEach((checkBox) -> {
            checkBox.setOnAction(eventHandler);
        });

        mPlayToggleButton.setOnAction((event) -> {
            mMeasureTool.clear();
            mMeasureTool.setArmed(true);
        });

        mPauseToggleButton.setOnAction((event) -> {
            mMeasureTool.setArmed(!mMeasureTool.isArmed());
            mPauseToggleButton.setGraphic(!mMeasureTool.isArmed() ? mResumeImageView : mPauseImageView);
            mPauseToggleButton.setDisable(false);
            ((Component) mWorldWindow).setCursor(!mMeasureTool.isArmed() ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        });

        mStopToggleButton.setOnAction((event) -> {
            mMeasureTool.setArmed(false);
            mStopToggleButton.setSelected(false);
        });

        mSettingsToggleButton.setOnAction((event) -> {
            mSettingsToggleButton.setSelected(false);
        });

        mMeasureTool.addPropertyChangeListener((PropertyChangeEvent event) -> {
            final String propertyName = event.getPropertyName();
            // Add, remove or change positions
            if (propertyName.equals(MeasureTool.EVENT_POSITION_ADD)
                    || propertyName.equals(MeasureTool.EVENT_POSITION_REMOVE)
                    || propertyName.equals(MeasureTool.EVENT_POSITION_REPLACE)) {
                updatePoints();    // Update position list when changed
            } // The tool was armed / disarmed
            else if (propertyName.equals(MeasureTool.EVENT_ARMED)) {
                if (mMeasureTool.isArmed()) {
                    mPlayToggleButton.setDisable(true);
                    mPauseToggleButton.setGraphic(mPauseImageView);
                    mPauseToggleButton.setDisable(false);
                    mStopToggleButton.setDisable(false);
                    ((Component) mWorldWindow).setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                } else {
                    mPlayToggleButton.setDisable(false);
                    mPauseToggleButton.setGraphic(mPauseImageView);
                    mPauseToggleButton.setDisable(true);
                    mStopToggleButton.setDisable(true);
                    ((Component) mWorldWindow).setCursor(Cursor.getDefaultCursor());
                }

            } // Metric changed - sent after each render frame
            else if (propertyName.equals(MeasureTool.EVENT_METRIC_CHANGED)) {
                updateMetrics();
            }
        });
    }

    private void initStates() {
        mKeyCheckBoxes.values().forEach((checkBox) -> {
            checkBox.setSelected(mOptions.is(mKeyCheckBoxes.inverse().get(checkBox)));
        });
    }

    private void postInit() {
        mShapeComboBox.getSelectionModel().select(0);
    }

    private void updateMetrics() {
    }

    private void updatePoints() {
    }
}
