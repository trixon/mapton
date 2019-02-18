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
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.util.measure.MeasureTool;
import gov.nasa.worldwind.util.measure.MeasureToolController;
import java.awt.Component;
import java.awt.Cursor;
import java.beans.PropertyChangeEvent;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.SegmentedButton;
import static org.mapton.api.Mapton.getIconSizeToolBar;
import static org.mapton.worldwind.ModuleOptions.*;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class RulerTab extends Tab {

    private static final int DEFAULT_PATH_TYPE_INDEX = 2;
    private BorderPane mBorderPane;
    private final ResourceBundle mBundle = NbBundle.getBundle(RulerTab.class);
    private final MeasureTool mMeasureTool;
    private final ModuleOptions mOptions = ModuleOptions.getInstance();
    private PopOver mOptionsPopOver;
    private ToggleButton mOptionsToggleButton;
    private final String[] mPathTypes = {AVKey.LINEAR, AVKey.RHUMB_LINE, AVKey.GREAT_CIRCLE};
    private ImageView mPauseImageView;
    private ToggleButton mPauseToggleButton;
    private ToggleButton mPlayToggleButton;
    private ImageView mResumeImageView;
    private ComboBox<String> mShapeComboBox;
    private ToggleButton mStopToggleButton;
    private final WorldWindow mWorldWindow;

    public RulerTab(String title, WorldWindow worldWindow) {
        super(title);
        mWorldWindow = worldWindow;
        mMeasureTool = new MeasureTool(mWorldWindow);
        mMeasureTool.setController(new MeasureToolController());

        createUI();
        initListeners();
        postInit();
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

        int imageSize = getIconSizeToolBar();
        mPauseImageView = MaterialIcon._Av.PAUSE_CIRCLE_OUTLINE.getImageView(imageSize);
        mResumeImageView = MaterialIcon._Av.PLAY_CIRCLE_OUTLINE.getImageView(imageSize);

        mPlayToggleButton = new ToggleButton("", MaterialIcon._Av.PLAY_ARROW.getImageView(imageSize));
        mPauseToggleButton = new ToggleButton("", mPauseImageView);
        mStopToggleButton = new ToggleButton("", MaterialIcon._Av.STOP.getImageView(imageSize));
        mOptionsToggleButton = new ToggleButton("", MaterialIcon._Action.SETTINGS.getImageView(imageSize));
        mPauseToggleButton.setDisable(true);
        mStopToggleButton.setDisable(true);

        SegmentedButton segmentedButton = new SegmentedButton();
        segmentedButton.getButtons().addAll(mPlayToggleButton, mPauseToggleButton, mStopToggleButton, mOptionsToggleButton);

        VBox box = new VBox(8,
                new BorderPane(segmentedButton)
        );
        box.setPadding(insets8);

        mBorderPane = new BorderPane(box);
        VBox topBox = new VBox(mShapeComboBox);
        topBox.setPadding(insets8);

        mBorderPane.setTop(topBox);

        mShapeComboBox.prefWidthProperty().bind(mBorderPane.widthProperty());

        setContent(mBorderPane);

        mOptionsPopOver = new PopOver();
        mOptionsPopOver.setTitle(Dict.OPTIONS.toString());
        mOptionsPopOver.setContentNode(new OptionsPane());
        mOptionsPopOver.setArrowLocation(PopOver.ArrowLocation.TOP_LEFT);
        mOptionsPopOver.setHeaderAlwaysVisible(true);
        mOptionsPopOver.setCloseButtonEnabled(false);
        mOptionsPopOver.setDetachable(false);
        mOptionsPopOver.setAnimated(true);
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

        mOptionsToggleButton.setOnAction((event) -> {
            if (mOptionsPopOver.isShowing()) {
                mOptionsPopOver.hide();
            } else {
                mOptionsPopOver.show(mOptionsToggleButton);
            }

            mOptionsToggleButton.setSelected(false);
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

        setOnClosed((event -> {
            mMeasureTool.setArmed(false);
            mMeasureTool.clear();
        }));
    }

    private void postInit() {
        mShapeComboBox.getSelectionModel().select(0);

        mMeasureTool.setPathType(mPathTypes[DEFAULT_PATH_TYPE_INDEX]);
        mMeasureTool.setShowControlPoints(mOptions.is(KEY_RULER_CONTROL_POINTS));
        mMeasureTool.setFollowTerrain(mOptions.is(KEY_RULER_FOLLOW_TERRAIN));
        mMeasureTool.setShowAnnotation(mOptions.is(KEY_RULER_ANNOTATION));
        mMeasureTool.getController().setFreeHand(mOptions.is(KEY_RULER_FREE_HAND));
        mMeasureTool.getController().setUseRubberBand(mOptions.is(KEY_RULER_RUBBER_BAND));
    }

    private void updateMetrics() {
    }

    private void updatePoints() {
    }

    private class OptionsPane extends VBox {

        private CheckBox mAnnotationCheckBox;
        private CheckBox mControlPointsCheckBox;
        private CheckBox mFollowTerrainCheckBox;
        private CheckBox mFreeHandCheckBox;
        private final BiMap<String, CheckBox> mKeyCheckBoxes = HashBiMap.create();
        private ComboBox<String> mPathTypeComboBox;
        private CheckBox mRubberBandCheckBox;

        public OptionsPane() {
            createUI();
            initListeners();
            initStates();
        }

        private void createUI() {
            setPadding(new Insets(8, 16, 16, 16));
            setSpacing(8);

            Label pathTypeLabel = new Label(mBundle.getString("ruler.option.path_type"));
            String[] pathTypes = StringUtils.split(mBundle.getString("ruler.option.path_types"), "|");
            mPathTypeComboBox = new ComboBox<>(FXCollections.observableArrayList(pathTypes));
            mPathTypeComboBox.getSelectionModel().select(DEFAULT_PATH_TYPE_INDEX);
            mFollowTerrainCheckBox = new CheckBox(mBundle.getString("ruler.option.follow_terrain"));
            mRubberBandCheckBox = new CheckBox(mBundle.getString("ruler.option.rubber_band"));
            mFreeHandCheckBox = new CheckBox(mBundle.getString("ruler.option.free_hand"));
            mAnnotationCheckBox = new CheckBox(mBundle.getString("ruler.option.annotation"));
            mControlPointsCheckBox = new CheckBox(mBundle.getString("ruler.option.control_points"));

            mFreeHandCheckBox.disableProperty().bind(mRubberBandCheckBox.selectedProperty().not());
            getChildren().setAll(
                    pathTypeLabel,
                    mPathTypeComboBox,
                    mFollowTerrainCheckBox,
                    mRubberBandCheckBox,
                    mFreeHandCheckBox,
                    mAnnotationCheckBox,
                    mControlPointsCheckBox
            );

            mKeyCheckBoxes.put(KEY_RULER_FOLLOW_TERRAIN, mFollowTerrainCheckBox);
            mKeyCheckBoxes.put(KEY_RULER_RUBBER_BAND, mRubberBandCheckBox);
            mKeyCheckBoxes.put(KEY_RULER_FREE_HAND, mFreeHandCheckBox);
            mKeyCheckBoxes.put(KEY_RULER_ANNOTATION, mAnnotationCheckBox);
            mKeyCheckBoxes.put(KEY_RULER_CONTROL_POINTS, mControlPointsCheckBox);
        }

        private void initListeners() {
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

                    default:
                        throw new AssertionError();
                }

                mWorldWindow.redraw();
            };

            mKeyCheckBoxes.values().forEach((checkBox) -> {
                checkBox.setOnAction(eventHandler);
            });

            mPathTypeComboBox.setOnAction((event) -> {
                mMeasureTool.setPathType(mPathTypes[mPathTypeComboBox.getSelectionModel().getSelectedIndex()]);
            });
        }

        private void initStates() {
            mKeyCheckBoxes.values().forEach((checkBox) -> {
                checkBox.setSelected(mOptions.is(mKeyCheckBoxes.inverse().get(checkBox)));
            });
        }
    }
}
