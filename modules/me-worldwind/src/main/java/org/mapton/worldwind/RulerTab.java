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
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.measure.MeasureTool;
import gov.nasa.worldwind.util.measure.MeasureToolController;
import java.awt.Component;
import java.awt.Cursor;
import java.beans.PropertyChangeEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
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
    private TextArea mMetricsTextArea;
    private final ModuleOptions mOptions = ModuleOptions.getInstance();
    private PopOver mOptionsPopOver;
    private ToggleButton mOptionsToggleButton;
    private final String[] mPathTypes = {AVKey.LINEAR, AVKey.RHUMB_LINE, AVKey.GREAT_CIRCLE};
    private ImageView mPauseImageView;
    private TextArea mPointsTextArea;
    private ImageView mResumeImageView;
    private RunState mRunState;
    private ComboBox<String> mShapeComboBox;
    private ImageView mStartImageView;
    private ToggleButton mStartToggleButton;
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

        setRunState(RunState.STARTABLE);
    }

    public MeasureTool getMeasureTool() {
        return mMeasureTool;
    }

    private void createUI() {
        mShapeComboBox = new ComboBox<>();
        mShapeComboBox.getItems().setAll(
                Dict.Geometry.LINE.toString(),
                Dict.Geometry.PATH.toString(),
                Dict.Geometry.POLYGON.toString(),
                Dict.Geometry.CIRCLE.toString(),
                Dict.Geometry.ELLIPSE.toString(),
                Dict.Geometry.SQUARE.toString(),
                Dict.Geometry.RECTANGLE.toString()
        );

        int imageSize = (int) (getIconSizeToolBar() * 0.8);
        mStartImageView = MaterialIcon._Av.PLAY_ARROW.getImageView(imageSize);
        mPauseImageView = MaterialIcon._Av.PAUSE_CIRCLE_OUTLINE.getImageView(imageSize);
        mResumeImageView = MaterialIcon._Av.PLAY_CIRCLE_OUTLINE.getImageView(imageSize);

        mStartToggleButton = new ToggleButton();
        mStopToggleButton = new ToggleButton("", MaterialIcon._Av.STOP.getImageView(imageSize));
        mOptionsToggleButton = new ToggleButton("", MaterialIcon._Action.SETTINGS.getImageView(imageSize));

        SegmentedButton segmentedButton = new SegmentedButton();
        segmentedButton.getButtons().addAll(mStartToggleButton, mStopToggleButton, mOptionsToggleButton);

        mMetricsTextArea = new TextArea();
        mMetricsTextArea.setEditable(false);
        mMetricsTextArea.setPrefRowCount(7);
        mMetricsTextArea.setFont(Font.font("monospaced"));

        mPointsTextArea = new TextArea();
        mPointsTextArea.setEditable(false);

        mBorderPane = new BorderPane(mPointsTextArea);
        VBox topBox = new VBox(8,
                mShapeComboBox,
                segmentedButton,
                mMetricsTextArea
        );
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(8, 0, 0, 0));

        mBorderPane.setTop(topBox);
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
//            setRunState(RunState.STARTABLE);
//            setRunState(RunState.STOPPABLE);
        });

        mStartToggleButton.setOnAction((event) -> {
            setRunState(mRunState == RunState.STARTABLE ? RunState.STOPPABLE : RunState.RESUMABLE);
        });

        mStopToggleButton.setOnAction((event) -> {
            setRunState(RunState.STARTABLE);
        });

        mOptionsToggleButton.setOnAction((event) -> {
            if (mOptionsPopOver.isShowing()) {
                mOptionsPopOver.hide();
            } else {
                mOptionsPopOver.show(mOptionsToggleButton);
            }

            mOptionsToggleButton.setSelected(false);
        });

        mMeasureTool.addPropertyChangeListener((PropertyChangeEvent propertyChangeEvent) -> {
            final String propertyName = propertyChangeEvent.getPropertyName();

            if (StringUtils.equalsAny(propertyName, MeasureTool.EVENT_POSITION_ADD, MeasureTool.EVENT_POSITION_REMOVE, MeasureTool.EVENT_POSITION_REPLACE)) {
                updatePoints();
            } else if (propertyName.equals(MeasureTool.EVENT_ARMED)) {
                ((Component) mWorldWindow).setCursor(mMeasureTool.isArmed() ? Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR) : Cursor.getDefaultCursor());
//                int shape = mShapeComboBox.getSelectionModel().getSelectedIndex();
//                if ((shape == 1 || shape == 2) && !mMeasureTool.isArmed()) {
//                    setRunState(RunState.STARTABLE);
//                }
            } else if (propertyName.equals(MeasureTool.EVENT_METRIC_CHANGED)) {
                updateMetrics();
            }
        });

        setOnClosed((event -> {
            mMeasureTool.setArmed(false);
            mMeasureTool.clear();
        }));
    }

    private boolean isClosedShape() {
        return mShapeComboBox.getSelectionModel().getSelectedIndex() > 1;
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

    private void setRunState(RunState runState) {
        switch (runState) {
            case STARTABLE://Stop measure
                mStartToggleButton.setGraphic(mStartImageView);
                mStartToggleButton.setSelected(false);
                mStopToggleButton.setDisable(true);
                mMeasureTool.setArmed(false);
                break;

            case RESUMABLE://Pause/Resume
                mMeasureTool.setArmed(!mMeasureTool.isArmed());
                mStartToggleButton.setGraphic(!mMeasureTool.isArmed() ? mResumeImageView : mPauseImageView);
                mStartToggleButton.setSelected(false);
                break;

            case STOPPABLE://Start measure
                mStartToggleButton.setGraphic(mPauseImageView);
                mStartToggleButton.setSelected(false);
                mStopToggleButton.setDisable(false);
                mMeasureTool.clear();
                mMeasureTool.setArmed(true);
                break;
        }

        mRunState = runState;
    }

    private void updateMetrics() {
        double length = mMeasureTool.getLength();
        String lenghtString;
        if (length <= 0) {
            lenghtString = "-";
        } else if (length < 1000) {
            lenghtString = String.format("%,7.1f m", length);
        } else {
            lenghtString = String.format("%,7.3f km", length / 1000);
        }

        double area = mMeasureTool.getArea();
        String areaString;
        if (area < 0) {
            areaString = "-";
        } else if (area < 1e6) {
            areaString = String.format("%,7.1f m2", area);
        } else {
            areaString = String.format("%,7.3f km2", area / 1e6);
        }

        double width = mMeasureTool.getWidth();
        String widthString;
        if (width < 0) {
            widthString = "-";
        } else if (width < 1000) {
            widthString = String.format("%,7.1f m", width);
        } else {
            widthString = String.format("%,7.3f km", width / 1000);
        }

        double height = mMeasureTool.getHeight();
        String heightString;
        if (height < 0) {
            heightString = "-";
        } else if (height < 1000) {
            heightString = String.format("%,7.1f m", height);
        } else {
            heightString = String.format("%,7.3f km", height / 1000);
        }

        Angle angle = mMeasureTool.getOrientation();
        String angleString;
        if (angle == null) {
            angleString = "-";
        } else {
            angleString = String.format("%,6.2f\u00B0", angle.degrees);
        }

        Position center = mMeasureTool.getCenterPosition();
        String centerString;
        if (center == null) {
            centerString = "-";
        } else {
            centerString = String.format("%,7.4f\u00B0 %,7.4f\u00B0", center.getLatitude().degrees, center.getLongitude().degrees);
        }

        int maxKeyLength = Integer.MIN_VALUE;
        int maxValLength = Integer.MIN_VALUE;
        LinkedHashMap<String, String> values = new LinkedHashMap<>();

        values.put(isClosedShape() ? Dict.Geometry.PERIMETER.toString() : Dict.Geometry.LENGTH.toString(), lenghtString);
        values.put(Dict.Geometry.AREA.toString(), areaString);
        values.put(Dict.Geometry.BEARING.toString(), angleString);
        values.put(Dict.Geometry.WIDTH.toString(), widthString);
        values.put(Dict.Geometry.HEIGHT.toString(), heightString);
        values.put(Dict.Geometry.CENTER.toString(), centerString);

        for (Map.Entry<String, String> entry : values.entrySet()) {
            maxKeyLength = Math.max(maxKeyLength, entry.getKey().length());
            maxValLength = Math.max(maxValLength, entry.getValue().length());
        }

        String separator = " : ";
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, String> entry : values.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            builder.append(StringUtils.leftPad(key, maxKeyLength)).append(separator).append(value).append("\n");
        }

        mMetricsTextArea.setText(builder.toString());
    }

    private void updatePoints() {
    }

    public enum RunState {
        STARTABLE, RESUMABLE, STOPPABLE;
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
            ReadOnlyIntegerProperty selectedIndexProperty = mShapeComboBox.getSelectionModel().selectedIndexProperty();
            mFollowTerrainCheckBox.disableProperty().bind(selectedIndexProperty.greaterThan(1));
            mFreeHandCheckBox.disableProperty().bind(
                    mRubberBandCheckBox.selectedProperty().not()
                            .or(selectedIndexProperty.greaterThan(0)
                                    .and(selectedIndexProperty.lessThan(3)).not())
            );
            mAnnotationCheckBox.disableProperty().bind(mControlPointsCheckBox.selectedProperty().not());

            getChildren().setAll(
                    pathTypeLabel,
                    mPathTypeComboBox,
                    mFollowTerrainCheckBox,
                    mRubberBandCheckBox,
                    mFreeHandCheckBox,
                    mControlPointsCheckBox,
                    mAnnotationCheckBox
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
