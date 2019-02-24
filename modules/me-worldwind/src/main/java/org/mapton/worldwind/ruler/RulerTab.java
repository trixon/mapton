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

import de.micromata.opengis.kml.v_2_2_0.Feature;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.measure.MeasureTool;
import gov.nasa.worldwind.util.measure.MeasureToolController;
import java.awt.Component;
import java.awt.Cursor;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import static org.mapton.api.Mapton.getIconSizeToolBar;
import org.mapton.worldwind.ModuleOptions;
import static org.mapton.worldwind.ModuleOptions.*;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxActionCheck;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class RulerTab extends Tab {

    static final int DEFAULT_PATH_TYPE_INDEX = 2;
    private static final int ICON_SIZE = (int) (getIconSizeToolBar() * 0.8);
    private BorderPane mBorderPane;
    private BorderPane mLowerBorderPane;
    private final MeasureTool mMeasureTool;
    private TextArea mMetricsTextArea;
    private final ModuleOptions mOptions = ModuleOptions.getInstance();
    private Action mOptionsAction;
    private ImageView mOptionsImageView;
    private PopOver mOptionsPopOver;
    private ImageView mPauseImageView;
    private TextArea mPointListTextArea;
    private ImageView mResumeImageView;
    private RunState mRunState;
    private Action mSaveAction;
    private ImageView mSaveImageView;
    private Action mShapeAction;
    private ImageView mShapeImageView;
    private ShapePopOver mShapePopOver;
    private FxActionCheck mStartAction;
    private ImageView mStartImageView;
    private FxActionCheck mStopAction;
    private ImageView mStopImageView;
    private ToolBar mToolBar;
    private VBox mTopBox;
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
        updatePointListVisibility();
    }

    public MeasureTool getMeasureTool() {
        return mMeasureTool;
    }

    Feature getFeature() {
        return new KmlFeatureGenerator(getText(), mMetricsTextArea.getText(), mMeasureTool).generate();
    }

    private void createUI() {
        mStartImageView = MaterialIcon._Av.PLAY_ARROW.getImageView(ICON_SIZE);
        mPauseImageView = MaterialIcon._Av.PAUSE_CIRCLE_OUTLINE.getImageView(ICON_SIZE);
        mResumeImageView = MaterialIcon._Av.PLAY_CIRCLE_OUTLINE.getImageView(ICON_SIZE);
        mStopImageView = MaterialIcon._Av.STOP.getImageView(ICON_SIZE);
        mSaveImageView = MaterialIcon._Content.SAVE.getImageView(ICON_SIZE);
        mOptionsImageView = MaterialIcon._Action.SETTINGS.getImageView(ICON_SIZE);
        mShapeImageView = MaterialIcon._Editor.FORMAT_SHAPES.getImageView(ICON_SIZE);

        mMetricsTextArea = new TextArea();
        mMetricsTextArea.setEditable(false);
        mMetricsTextArea.setPrefRowCount(7);
        mMetricsTextArea.setFont(Font.font("monospaced"));

        mPointListTextArea = new TextArea();
        mPointListTextArea.setEditable(false);

        mLowerBorderPane = new BorderPane(mPointListTextArea);
        mTopBox = new VBox(8
        );

        mTopBox.setAlignment(Pos.CENTER);
        mTopBox.setPadding(new Insets(8, 0, 8, 0));

        mLowerBorderPane.setTop(mTopBox);
        mBorderPane = new BorderPane(mLowerBorderPane);

        setContent(mBorderPane);

        initToolBar();

        mShapePopOver = new ShapePopOver(mMeasureTool);
        mOptionsPopOver = new OptionsPopOver(mMeasureTool, mWorldWindow);
    }

    private void initListeners() {
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

        mOptions.getPreferences().addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            Platform.runLater(() -> {
                switch (evt.getKey()) {
                    case KEY_RULER_POINT_LIST:
                        updatePointListVisibility();
                        break;
                }
            });
        });

        setOnClosed((event -> {
            mMeasureTool.setArmed(false);
            mMeasureTool.clear();
        }));
    }

    private void initToolBar() {
        mShapeAction = new Action(Dict.Geometry.GEOMETRY.toString(), (event) -> {
            if (mShapePopOver.isShowing()) {
                mShapePopOver.hide();
            } else {
                mShapePopOver.show(((ButtonBase) event.getSource()));
            }
        });
        mShapeAction.setGraphic(mShapeImageView);

        mStartAction = new FxActionCheck(Dict.START.toString(), (event) -> {
            setRunState(mRunState == RunState.STARTABLE ? RunState.STOPPABLE : RunState.RESUMABLE);
        });
        mStartAction.setGraphic(mStartImageView);

        mStopAction = new FxActionCheck(Dict.STOP.toString(), (event) -> {
            setRunState(RunState.STARTABLE);
        });
        mStopAction.setGraphic(mStopImageView);

        mSaveAction = new Action(Dict.SAVE.toString(), (event) -> {
            ((RulerTabPane) getTabPane()).save();
        });
        mSaveAction.setGraphic(mSaveImageView);

        mOptionsAction = new Action(Dict.OPTIONS.toString(), (event) -> {
            if (mOptionsPopOver.isShowing()) {
                mOptionsPopOver.hide();
            } else {
                mOptionsPopOver.show(((ButtonBase) event.getSource()));
            }
        });
        mOptionsAction.setGraphic(mOptionsImageView);

        ArrayList<Action> actions = new ArrayList<>();
        actions.addAll(Arrays.asList(
                mShapeAction,
                mStartAction,
                mStopAction,
                mSaveAction,
                mOptionsAction
        ));

        mToolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);
        mToolBar.setStyle("-fx-spacing: 0px;");
        mToolBar.setPadding(Insets.EMPTY);

        Platform.runLater(() -> {
            FxHelper.adjustButtonWidth(mToolBar.getItems().stream(), ICON_SIZE * 1.5);
            mToolBar.getItems().stream().filter((item) -> (item instanceof ButtonBase))
                    .map((item) -> (ButtonBase) item).forEachOrdered((buttonBase) -> {
                FxHelper.undecorateButton(buttonBase);
            });
        });
        mBorderPane.setTop(mToolBar);
    }

    private boolean isClosedShape() {
        return mOptions.getInt(KEY_RULER_SHAPE) > 1;
    }

    private void postInit() {
        mMeasureTool.setShowControlPoints(mOptions.is(KEY_RULER_CONTROL_POINTS));
        mMeasureTool.setFollowTerrain(mOptions.is(KEY_RULER_FOLLOW_TERRAIN));
        mMeasureTool.setShowAnnotation(mOptions.is(KEY_RULER_ANNOTATION));
        mMeasureTool.getController().setFreeHand(mOptions.is(KEY_RULER_FREE_HAND));
        mMeasureTool.getController().setUseRubberBand(mOptions.is(KEY_RULER_RUBBER_BAND));
    }

    private void setRunState(RunState runState) {
        switch (runState) {
            case STARTABLE://Stop measure
                mStartAction.setGraphic(mStartImageView);
                mStartAction.setSelected(false);
                mStopAction.setDisabled(true);
                mMeasureTool.setArmed(false);
                break;

            case RESUMABLE://Pause/Resume
                mMeasureTool.setArmed(!mMeasureTool.isArmed());
                mStartAction.setGraphic(!mMeasureTool.isArmed() ? mResumeImageView : mPauseImageView);
                mStartAction.setSelected(false);
                break;

            case STOPPABLE://Start measure
                mStartAction.setGraphic(mPauseImageView);
                mStartAction.setSelected(false);
                mStopAction.setDisabled(false);
                mMeasureTool.clear();
                mMeasureTool.setArmed(true);
                break;
        }

        mRunState = runState;
    }

    private void updateMetrics() {
        double unitLimit;
        double length = mMeasureTool.getLength();
        String lenghtString;
        unitLimit = 1E3;
        if (length <= 0) {
            lenghtString = "-";
        } else if (length < unitLimit) {
            lenghtString = String.format("%,7.1f m", length);
        } else {
            lenghtString = String.format("%,7.3f km", length / unitLimit);
        }

        double area = mMeasureTool.getArea();
        String areaString;
        unitLimit = 1E4;
        if (area < 0) {
            areaString = "-";
        } else if (area < unitLimit) {
            areaString = String.format("%,7.1f m²", area);
        } else {
            areaString = String.format("%,7.3f km²", area / unitLimit / 1E2);
        }

        double width = mMeasureTool.getWidth();
        String widthString;
        unitLimit = 1E3;
        if (width < 0) {
            widthString = "-";
        } else if (width < unitLimit) {
            widthString = String.format("%,7.1f m", width);
        } else {
            widthString = String.format("%,7.3f km", width / unitLimit);
        }

        double height = mMeasureTool.getHeight();
        String heightString;
        unitLimit = 1E3;
        if (height < 0) {
            heightString = "-";
        } else if (height < unitLimit) {
            heightString = String.format("%,7.1f m", height);
        } else {
            heightString = String.format("%,7.3f km", height / unitLimit);
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

        Platform.runLater(() -> {
            mMetricsTextArea.setText(builder.toString());
        });
    }

    private void updatePointListVisibility() {
        if (mOptions.is(ModuleOptions.KEY_RULER_POINT_LIST, false)) {
            mLowerBorderPane.setCenter(mPointListTextArea);
            mTopBox.getChildren().add(mMetricsTextArea);
        } else {
            mLowerBorderPane.setCenter(mMetricsTextArea);
        }
    }

    private void updatePoints() {
        mPointListTextArea.clear();
        if (mMeasureTool.getPositions() != null) {
            StringBuilder builder = new StringBuilder();

            mMeasureTool.getPositions().forEach((pos) -> {
                builder.append(String.format(Locale.ENGLISH, "%3.6f %2.6f\n", pos.getLongitude().getDegrees(), pos.getLatitude().getDegrees()));
            });

            Platform.runLater(() -> {
                mPointListTextArea.setText(builder.toString());
            });
        }
    }

    public enum RunState {
        STARTABLE, RESUMABLE, STOPPABLE;
    }
}
