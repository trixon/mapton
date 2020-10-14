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
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.worldwind.ModuleOptions;
import static org.mapton.worldwind.ModuleOptions.*;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelperFx;
import se.trixon.almond.util.fx.FxActionCheck;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class RulerTab extends Tab {

    static final int DEFAULT_PATH_TYPE_INDEX = 2;
    private BorderPane mBorderPane;
    private Action mCopyAction;
    private ImageView mCopyImageView;
    private BorderPane mLowerBorderPane;
    private final MeasureTool mMeasureTool;
    private TextArea mMetricsTextArea;
    private final ModuleOptions mOptions = ModuleOptions.getInstance();
    private Action mOptionsAction;
    private OptionsContextMenu mOptionsContextMenu;
    private ImageView mOptionsImageView;
    private ImageView mPauseImageView;
    private TextArea mPointListTextArea;
    private ImageView mResumeImageView;
    private RunState mRunState;
    private Action mSaveAction;
    private ImageView mSaveImageView;
    private Action mShapeAction;
    private ShapeContextMenu mShapeContextMenu;
    private ImageView mShapeImageView;
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

    private void copyPointList() {
        SystemHelperFx.copyToClipboard(String.join(", ", mPointListTextArea.getText().split("\n")));
    }

    private void createUI() {
        mStartImageView = MaterialIcon._Av.PLAY_ARROW.getImageView(getIconSizeToolBarInt());
        mPauseImageView = MaterialIcon._Av.PAUSE_CIRCLE_OUTLINE.getImageView(getIconSizeToolBarInt());
        mResumeImageView = MaterialIcon._Av.PLAY_CIRCLE_OUTLINE.getImageView(getIconSizeToolBarInt());
        mStopImageView = MaterialIcon._Av.STOP.getImageView(getIconSizeToolBarInt());
        mSaveImageView = MaterialIcon._Content.SAVE.getImageView(getIconSizeToolBarInt());
        mOptionsImageView = MaterialIcon._Action.SETTINGS.getImageView(getIconSizeToolBarInt());
        mShapeImageView = MaterialIcon._Editor.FORMAT_SHAPES.getImageView(getIconSizeToolBarInt());
        mCopyImageView = MaterialIcon._Content.CONTENT_COPY.getImageView(getIconSizeToolBarInt());

        mMetricsTextArea = new TextArea();
        mMetricsTextArea.setEditable(false);
        mMetricsTextArea.setPrefRowCount(7);
        mMetricsTextArea.setFont(Font.font("monospaced", FxHelper.getScaledFontSize()));

        mPointListTextArea = new TextArea();
        mPointListTextArea.setEditable(false);

        mLowerBorderPane = new BorderPane(mPointListTextArea);
        mTopBox = new VBox(8
        );

        mTopBox.setAlignment(Pos.CENTER);

        mLowerBorderPane.setTop(mTopBox);
        mBorderPane = new BorderPane(mLowerBorderPane);

        setContent(mBorderPane);

        initToolBar();

        mShapeContextMenu = new ShapeContextMenu(mMeasureTool);
        mOptionsContextMenu = new OptionsContextMenu(mMeasureTool, mWorldWindow);
    }

    private void initListeners() {
        mMeasureTool.addPropertyChangeListener((PropertyChangeEvent propertyChangeEvent) -> {
            final String propertyName = propertyChangeEvent.getPropertyName();

            if (StringUtils.equalsAny(propertyName, MeasureTool.EVENT_POSITION_ADD, MeasureTool.EVENT_POSITION_REMOVE, MeasureTool.EVENT_POSITION_REPLACE)) {
                updatePoints();
            } else if (propertyName.equals(MeasureTool.EVENT_ARMED)) {
                Cursor cursor;

                if (mMeasureTool.isArmed()) {
                    cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
                } else {
                    cursor = Cursor.getDefaultCursor();
                    int shapeIndex = mOptions.getInt(KEY_RULER_SHAPE);
                    if (mRunState == RunState.STOPPABLE && shapeIndex != 1 && shapeIndex != 2) {
                        setRunState(RunState.STARTABLE);
                    }
                }

                ((Component) mWorldWindow).setCursor(cursor);
            } else if (propertyName.equals(MeasureTool.EVENT_METRIC_CHANGED)) {
                updateMetrics();
            }
        });

        mOptions.getPreferences().addPreferenceChangeListener(evt -> {
            Platform.runLater(() -> {
                switch (evt.getKey()) {
                    case KEY_RULER_POINT_LIST:
                        updatePointListVisibility();
                        break;
                }
            });
        });

        mShapeContextMenu.shapeIndexProperty().addListener((ov, oldValue, newValue) -> {
            setRunState(RunState.STOPPABLE);
        });

        setOnClosed(event -> {
            mMeasureTool.setArmed(false);
            mMeasureTool.clear();
        });
    }

    private void initToolBar() {
        mShapeAction = new Action(Dict.Geometry.GEOMETRY.toString(), event -> {
            if (mShapeContextMenu.isShowing()) {
                mShapeContextMenu.hide();
            } else {
                Node node = FxHelper.getButtonForAction(mShapeAction, mToolBar.getItems());
                Bounds bounds = node.getBoundsInLocal();
                Bounds screenBounds = node.localToScreen(bounds);
                mShapeContextMenu.show(node, screenBounds.getMinX(), screenBounds.getMaxY());
            }
        });
        mShapeAction.setGraphic(mShapeImageView);

        mStartAction = new FxActionCheck(Dict.START.toString(), event -> {
            setRunState(mRunState == RunState.STARTABLE ? RunState.STOPPABLE : RunState.RESUMABLE);
        });
        mStartAction.setGraphic(mStartImageView);

        mStopAction = new FxActionCheck(Dict.STOP.toString(), event -> {
            setRunState(RunState.STARTABLE);
        });
        mStopAction.setGraphic(mStopImageView);

        mSaveAction = new Action(Dict.SAVE.toString(), event -> {
            ((RulerTabPane) getTabPane()).save();
        });
        mSaveAction.setGraphic(mSaveImageView);

        mCopyAction = new Action(Dict.COPY.toString(), event -> {
            copyPointList();
        });
        mCopyAction.setGraphic(mCopyImageView);

        mOptionsAction = new Action(Dict.OPTIONS.toString(), event -> {
            if (mOptionsContextMenu.isShowing()) {
                mOptionsContextMenu.hide();
            } else {
                Node node = FxHelper.getButtonForAction(mOptionsAction, mToolBar.getItems());
                Bounds bounds = node.getBoundsInLocal();
                Bounds screenBounds = node.localToScreen(bounds);
                mOptionsContextMenu.show(node, screenBounds.getMinX(), screenBounds.getMaxY());
            }
        });
        mOptionsAction.setGraphic(mOptionsImageView);

        ArrayList<Action> actions = new ArrayList<>();
        actions.addAll(Arrays.asList(
                mShapeAction,
                mStartAction,
                mStopAction,
                mSaveAction,
                mCopyAction,
                ActionUtils.ACTION_SPAN,
                mOptionsAction
        ));

        mToolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);
        FxHelper.slimToolBar(mToolBar);

        Platform.runLater(() -> {
            FxHelper.adjustButtonWidth(mToolBar.getItems().stream(), getIconSizeToolBarInt() * 1.5);
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
        Platform.runLater(() -> {
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
        });
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
