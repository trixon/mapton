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
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.worldwind.ModuleOptions;
import static org.mapton.worldwind.ModuleOptions.*;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxActionCheck;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class RulerTab extends Tab {

    static final int DEFAULT_PATH_TYPE_INDEX = 2;
    private BorderPane mBorderPane;
    private BorderPane mLowerBorderPane;
    private final MeasureTool mMeasureTool;
    private TextArea mMetricsTextArea;
    private final ModuleOptions mOptions = ModuleOptions.getInstance();
    private Action mOptionsAction;
    private Glyph mOptionsGlyph;
    private PopOver mOptionsPopOver;
    private Glyph mPauseGlyph;
    private TextArea mPointListTextArea;
    private Glyph mResumeImageGlyph;
    private RunState mRunState;
    private Action mSaveAction;
    private Glyph mSaveGlyph;
    private Action mShapeAction;
    private Glyph mShapeGlyph;
    private ShapePopOver mShapePopOver;
    private FxActionCheck mStartAction;
    private Glyph mStartImageGlyph;
    private FxActionCheck mStopAction;
    private Glyph mStopGlyph;
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
        mStartImageGlyph = Mapton.createGlyphToolbarForm(FontAwesome.Glyph.PLAY);
        mPauseGlyph = Mapton.createGlyphToolbarForm(FontAwesome.Glyph.PAUSE);
        mResumeImageGlyph = Mapton.createGlyphToolbarForm(FontAwesome.Glyph.PLAY_CIRCLE_ALT);
        mStopGlyph = Mapton.createGlyphToolbarForm(FontAwesome.Glyph.STOP);
        mSaveGlyph = Mapton.createGlyphToolbarForm(FontAwesome.Glyph.SAVE);
        mOptionsGlyph = Mapton.createGlyphToolbarForm(FontAwesome.Glyph.COG);
        mShapeGlyph = Mapton.createGlyphToolbarForm(FontAwesome.Glyph.SQUARE_ALT);

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

        mShapePopOver = new ShapePopOver(mMeasureTool);
        mOptionsPopOver = new OptionsPopOver(mMeasureTool, mWorldWindow);
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

        mOptions.getPreferences().addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            Platform.runLater(() -> {
                switch (evt.getKey()) {
                    case KEY_RULER_POINT_LIST:
                        updatePointListVisibility();
                        break;
                }
            });
        });

        mShapePopOver.shapeIndexProperty().addListener((ObservableValue<? extends Number> ov, Number t, Number t1) -> {
            setRunState(RunState.STOPPABLE);
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
        mShapeAction.setGraphic(mShapeGlyph);

        mStartAction = new FxActionCheck(Dict.START.toString(), (event) -> {
            setRunState(mRunState == RunState.STARTABLE ? RunState.STOPPABLE : RunState.RESUMABLE);
        });
        mStartAction.setGraphic(mStartImageGlyph);

        mStopAction = new FxActionCheck(Dict.STOP.toString(), (event) -> {
            setRunState(RunState.STARTABLE);
        });
        mStopAction.setGraphic(mStopGlyph);

        mSaveAction = new Action(Dict.SAVE.toString(), (event) -> {
            ((RulerTabPane) getTabPane()).save();
        });
        mSaveAction.setGraphic(mSaveGlyph);

        mOptionsAction = new Action(Dict.OPTIONS.toString(), (event) -> {
            if (mOptionsPopOver.isShowing()) {
                mOptionsPopOver.hide();
            } else {
                mOptionsPopOver.show(((ButtonBase) event.getSource()));
            }
        });
        mOptionsAction.setGraphic(mOptionsGlyph);

        ArrayList<Action> actions = new ArrayList<>();
        actions.addAll(Arrays.asList(
                mShapeAction,
                mStartAction,
                mStopAction,
                mSaveAction,
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
                    mStartAction.setGraphic(mStartImageGlyph);
                    mStartAction.setSelected(false);
                    mStopAction.setDisabled(true);
                    mMeasureTool.setArmed(false);
                    break;

                case RESUMABLE://Pause/Resume
                    mMeasureTool.setArmed(!mMeasureTool.isArmed());
                    mStartAction.setGraphic(!mMeasureTool.isArmed() ? mResumeImageGlyph : mPauseGlyph);
                    mStartAction.setSelected(false);
                    break;

                case STOPPABLE://Start measure
                    mStartAction.setGraphic(mPauseGlyph);
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
