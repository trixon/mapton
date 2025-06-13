/*
 * Copyright 2025 Patrik Karlström.
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
package org.mapton.butterfly_hydro.groundwater;

import com.dlsc.gemsfx.util.SessionManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.controlsfx.tools.Borders;
import org.mapton.api.MTemporalRange;
import org.mapton.api.ui.forms.DateRangePane;
import org.mapton.api.ui.forms.MBaseFilterSection;
import org.mapton.api.ui.forms.NegPosStringConverterDouble;
import org.mapton.butterfly_format.types.hydro.BHydroGroundwaterPoint;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionComboBox;
import se.trixon.almond.util.fx.session.SessionDoubleSpinner;

/**
 *
 * @author Patrik Karlström
 */
public class FilterSectionMeas extends MBaseFilterSection {

    private final Direction mDefaultDirection = Direction.DOWN;

    private final double mDefaultLevelPeriodValue = 0.5;
    private final SessionDoubleSpinner mLevelPeriodAllSds = new SessionDoubleSpinner(-20.0, 20.0, mDefaultLevelPeriodValue, 0.1);
    private final CheckBox mLevelPeriodCheckbox = new CheckBox();
    private final SessionComboBox<Direction> mLevelPeriodDirectionScb = new SessionComboBox<>();
    private final DateRangePane mLeverPeriodDateRangePane = new DateRangePane();
    private final MeasFilterUI mMeasFilterUI;

    public FilterSectionMeas() {
        super("Mätningar");
        mMeasFilterUI = new MeasFilterUI();
        init();
        setContent(mMeasFilterUI.getBaseBox());
    }

    @Override
    public void clear() {
        super.clear();

        mLevelPeriodDirectionScb.getSelectionModel().select(mDefaultDirection);
        mLevelPeriodAllSds.getValueFactory().setValue(mDefaultLevelPeriodValue);
        mLeverPeriodDateRangePane.reset();
        FxHelper.setSelected(false, mLevelPeriodCheckbox);
    }

    @Override
    public void createInfoContent(LinkedHashMap<String, String> map) {
        if (!isSelected()) {
            return;
        }

        map.put("MÄTNINGAR", ".");
        map.put("Period " + Dict.FROM.toString(), levelPeriodDateLowProperty().get() != null ? levelPeriodDateLowProperty().get().toString() : "");
        map.put("Period " + Dict.TO.toString(), levelPeriodDateHighProperty().get() != null ? levelPeriodDateHighProperty().get().toString() : "");
    }

    public GridPane getRoot() {
        return mMeasFilterUI.getBaseBox();
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        setSessionManager(sessionManager);
        sessionManager.register("filter.section.meas", selectedProperty());
        sessionManager.register("filter.levelPeriod", mLevelPeriodCheckbox.selectedProperty());
        sessionManager.register("filter.levelPeriod.Value", mLevelPeriodAllSds.sessionValueProperty());
        sessionManager.register("filter.levelPeriod.DateLow", mLeverPeriodDateRangePane.lowStringProperty());
        sessionManager.register("filter.levelPeriod.DateHigh", mLeverPeriodDateRangePane.highStringProperty());
        sessionManager.register("filter.levelPeriod.Direction", mLevelPeriodDirectionScb.selectedIndexProperty());
    }

    @Override
    public void onShownFirstTime() {
        mMeasFilterUI.onShownFirstTime();
    }

    @Override
    public void reset(PropertiesConfiguration filterConfig) {
        if (filterConfig != null) {
            mMeasFilterUI.reset(filterConfig);
        }
    }

    boolean filter(BHydroGroundwaterPoint p) {
        if (isSelected()) {
            return validatePeriodChanges(p)
                    && true;
        } else {
            return true;
        }
    }

    void initListeners(ChangeListener changeListenerObject, ListChangeListener<Object> listChangeListener) {
        List.of(
                selectedProperty(),
                //
                levelPeriodDateHighProperty(),
                levelPeriodDateLowProperty(),
                mLevelPeriodCheckbox.selectedProperty(),
                mLevelPeriodAllSds.valueProperty(),
                mLevelPeriodDirectionScb.getSelectionModel().selectedItemProperty()
        ).forEach(propertyBase -> propertyBase.addListener(changeListenerObject));
    }

    void load(ArrayList<BHydroGroundwaterPoint> items, MTemporalRange temporalRange) {
        mLevelPeriodAllSds.load();
        mLevelPeriodAllSds.disableProperty().bind(mLevelPeriodCheckbox.selectedProperty().not());
        if (temporalRange != null) {
            mLeverPeriodDateRangePane.setMinMaxDate(temporalRange.getFromLocalDate(), temporalRange.getToLocalDate());
        }
        mLevelPeriodDirectionScb.load();

        var sessionManager = getSessionManager();
        sessionManager.register("filter.DateLevelPeriodLow", mLeverPeriodDateRangePane.lowStringProperty());
        sessionManager.register("filter.DatePeriodHigh", mLeverPeriodDateRangePane.highStringProperty());
    }

    private void init() {
    }

    private SimpleObjectProperty<LocalDate> levelPeriodDateHighProperty() {
        return mLeverPeriodDateRangePane.highDateProperty();
    }

    private SimpleObjectProperty<LocalDate> levelPeriodDateLowProperty() {
        return mLeverPeriodDateRangePane.lowDateProperty();
    }

    private boolean validatePeriodChanges(BHydroGroundwaterPoint p) {
        if (!mLevelPeriodCheckbox.isSelected()) {
            return true;
        }
        var direction = mLevelPeriodDirectionScb.getValue();
        var lim = mLevelPeriodAllSds.getValue();
        if (direction == Direction.MIN_MAX_SPAN) {
            var value = p.ext().getGroundwaterLevelMinMaxSpan(levelPeriodDateLowProperty().get(), levelPeriodDateHighProperty().get());
            if (value == null) {
                return false;
            }

            if (lim == 0) {
                return value == 0;
            } else if (lim < 0) {//Up to
                return value <= Math.abs(lim);
            } else {//at least
                return value >= lim;
            }
        } else {
            var value = p.ext().getGroundwaterLevelDiff(levelPeriodDateLowProperty().get(), levelPeriodDateHighProperty().get());
            if (value == null) {
                return false;
            }
            var validDirection = value < 0 && direction == Direction.DOWN
                    || value > 0 && direction == Direction.UP
                    || direction == Direction.EITHER;
            value = Math.abs(value);

            if (lim == 0) {
                return value == 0;
            } else if (lim < 0) {//Up to
                return value <= Math.abs(lim) && validDirection;
            } else {//at least
                return value >= lim && validDirection;
            }
        }
    }

    public class MeasFilterUI {

        private Node mBaseBorderBox;
        private GridPane mBaseBox;

        public MeasFilterUI() {
            createUI();
        }

        public Node getBaseBorderBox() {
            if (mBaseBorderBox == null) {
                mBaseBorderBox = Borders.wrap(mBaseBox)
                        .etchedBorder()
                        .title("Grunddata")
                        .innerPadding(mTopBorderInnerPadding, mBorderInnerPadding, mBorderInnerPadding, mBorderInnerPadding)
                        .outerPadding(0)
                        .raised()
                        .build()
                        .build();
            }
            return mBaseBorderBox;
        }

        public GridPane getBaseBox() {
            return mBaseBox;
        }

        public void onShownFirstTime() {
        }

        private void createUI() {
            mLevelPeriodCheckbox.setText("Nivåförändring");
            mLevelPeriodAllSds.getValueFactory().setConverter(new NegPosStringConverterDouble());

            mLevelPeriodDirectionScb.getItems().setAll(Direction.values());
            mLevelPeriodDirectionScb.getSelectionModel().select(mDefaultDirection);
            var levelPeriodGridPane = new GridPane(GAP_H, GAP_V);
            levelPeriodGridPane.add(mLevelPeriodCheckbox, 0, 0, 2, 1);
            levelPeriodGridPane.addRow(1, mLevelPeriodAllSds, mLevelPeriodDirectionScb);
            FxHelper.autoSizeColumn(levelPeriodGridPane, 2);

//            int rowGap = FxHelper.getUIScaled(12);
//            mBaseBox = new GridPane(rowGap, rowGap);
            mBaseBox = new GridPane();
            double borderInnerPadding = FxHelper.getUIScaled(8.0);
            double topBorderInnerPadding = FxHelper.getUIScaled(0.0);

            var wrappedDateBox = Borders.wrap(mLeverPeriodDateRangePane.getRoot())
                    .etchedBorder()
                    .title("Nivåförändringsperiod")
                    .innerPadding(topBorderInnerPadding, borderInnerPadding, borderInnerPadding, borderInnerPadding)
                    .outerPadding(0)
                    .raised()
                    .build()
                    .build();

            var leftBox = new VBox(rowGap,
                    levelPeriodGridPane,
                    wrappedDateBox
            );

            var rightBox = new VBox(rowGap,
                    new Label("")
            );

            int row = 1;
            mBaseBox.addRow(row++, leftBox, rightBox);

            var spinners = new Spinner[]{mLevelPeriodAllSds};
            FxHelper.setEditable(true, spinners);
            FxHelper.autoCommitSpinners(spinners);
            FxHelper.autoSizeRegionHorizontal(mLevelPeriodDirectionScb);

            FxHelper.autoSizeColumn(mBaseBox, 2);
            FxHelper.bindWidthForChildrens(leftBox, rightBox);

            mLevelPeriodDirectionScb.disableProperty().bind(mLevelPeriodCheckbox.selectedProperty().not().or(mLevelPeriodAllSds.valueProperty().isEqualTo(0.0)));
            wrappedDateBox.disableProperty().bind(mLevelPeriodCheckbox.selectedProperty().not());
        }

        private void reset(PropertiesConfiguration filterConfig) {
        }
    }

    private enum Direction {
        UP("Höjning"),
        DOWN("Sänkning"),
        EITHER("±"),
        MIN_MAX_SPAN("Min-Max spann");
        private final String mTitle;

        private Direction(String title) {
            mTitle = title;
        }

        @Override
        public String toString() {
            return mTitle;
        }

    }
}
