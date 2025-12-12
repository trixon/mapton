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
package org.mapton.butterfly_remote.insar;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.mapton.api.MTemporalRange;
import org.mapton.api.ui.forms.MBaseFilterSection;
import org.mapton.butterfly_format.types.remote.BRemoteInsarPoint;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.SliderPane;
import se.trixon.almond.util.fx.session.SessionComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class FilterSectionMeas extends MBaseFilterSection {

    private final Direction mDefaultDirection = Direction.EITHER;
    private final MeasFilterUI mMeasFilterUI;
    private final SliderPane mVelocitySliderPane = new SliderPane("Hastighet (mm/år)", 100, true, true, 1d);
    private final ArgBox mVelocityArgBox = new ArgBox();

    public FilterSectionMeas() {
        super(SDict.MEASUREMENTS.toString());
        mMeasFilterUI = new MeasFilterUI();
        setContent(mMeasFilterUI.getRoot());
    }

    @Override
    public void clear() {
        super.clear();
        mVelocitySliderPane.clear();
        mVelocityArgBox.reset();
    }

    @Override
    public void createInfoContent(LinkedHashMap<String, String> map) {
        if (!isSelected()) {
            return;
        }

        map.put(SDict.MEASUREMENTS.toString(), ".");
//        map.put("Period " + Dict.FROM.toString(), levelPeriodDateLowProperty().get() != null ? levelPeriodDateLowProperty().get().toString() : "");
//        map.put("Period " + Dict.TO.toString(), levelPeriodDateHighProperty().get() != null ? levelPeriodDateHighProperty().get().toString() : "");
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        setSessionManager(sessionManager);
        sessionManager.register(getKeyFilter("section"), selectedProperty());
        mVelocitySliderPane.initSession(getKeyFilter("velocityValue"), sessionManager);
        sessionManager.register(getKeyFilter("velocityDirection"), mVelocityArgBox.mDirectionScb.selectedIndexProperty());
        sessionManager.register(getKeyFilter("velocityGtLt"), mVelocityArgBox.mGtLtScb.selectedIndexProperty());
    }

    @Override
    public void onShownFirstTime() {
    }

    @Override
    public void reset(PropertiesConfiguration filterConfig) {
        mVelocitySliderPane.setSelected(false);
    }

    boolean filter(BRemoteInsarPoint p) {
        if (isSelected()) {
            return true
                    && true;
        } else {
            return true;
        }
    }

    void initListeners(ChangeListener changeListenerObject, ListChangeListener<Object> listChangeListener) {
        List.of(
                selectedProperty(),
                mVelocitySliderPane.selectedProperty(),
                mVelocitySliderPane.valueProperty(),
                mVelocityArgBox.mDirectionScb.getSelectionModel().selectedItemProperty(),
                mVelocityArgBox.mGtLtScb.getSelectionModel().selectedItemProperty()
        //
        //                levelPeriodDateHighProperty(),
        //                levelPeriodDateLowProperty()
        //                mLevelPeriodCheckbox.selectedProperty(),
        //                mLevelPeriodAllSds.valueProperty(),
        //                mLevelPeriodDirectionScb.getSelectionModel().selectedItemProperty()
        ).forEach(propertyBase -> propertyBase.addListener(changeListenerObject));
    }

    void load(ArrayList<BRemoteInsarPoint> items, MTemporalRange temporalRange) {
//        mLevelPeriodAllSds.load();
//        mLevelPeriodAllSds.disableProperty().bind(mLevelPeriodCheckbox.selectedProperty().not());
//        if (temporalRange != null) {
//            mLeverPeriodDateRangePane.setMinMaxDate(temporalRange.getFromLocalDate(), temporalRange.getToLocalDate());
//        }
//        mLevelPeriodDirectionScb.load();
//
//        var sessionManager = getSessionManager();
//        sessionManager.register("filter.DateLevelPeriodLow", mLeverPeriodDateRangePane.lowStringProperty());
//        sessionManager.register("filter.DatePeriodHigh", mLeverPeriodDateRangePane.highStringProperty());
    }

//    private SimpleObjectProperty<LocalDate> levelPeriodDateHighProperty() {
//        return mLeverPeriodDateRangePane.highDateProperty();
//    }
//
//    private SimpleObjectProperty<LocalDate> levelPeriodDateLowProperty() {
//        return mLeverPeriodDateRangePane.lowDateProperty();
//    }
//
//    private boolean validatePeriodChanges(BRemoteInsarPoint p) {
//        if (!mLevelPeriodCheckbox.isSelected()) {
//            return true;
//        }
//        var direction = mLevelPeriodDirectionScb.getValue();
//        var lim = mLevelPeriodAllSds.getValue();
//        if (direction == Direction.EITHER) {
//            Double value = null;//p.ext().getGroundwaterLevelMinMaxSpan(levelPeriodDateLowProperty().get(), levelPeriodDateHighProperty().get());
//            if (value == null) {
//                return false;
//            }
//
//            if (lim == 0) {
//                return value == 0;
//            } else if (lim < 0) {//Up to
//                return value <= Math.abs(lim);
//            } else {//at least
//                return value >= lim;
//            }
//        } else {
//            Double value = null;//p.ext().getGroundwaterLevelDiff(levelPeriodDateLowProperty().get(), levelPeriodDateHighProperty().get());
//            if (value == null) {
//                return false;
//            }
//            var validDirection = value < 0 && direction == Direction.NEG
//                    || value > 0 && direction == Direction.POS
//                    || direction == Direction.EITHER;
//            value = Math.abs(value);
//
//            if (lim == 0) {
//                return value == 0;
//            } else if (lim < 0) {//Up to
//                return value <= Math.abs(lim) && validDirection;
//            } else {//at least
//                return value >= lim && validDirection;
//            }
//        }
//    }
    public class MeasFilterUI {

        private GridPane mRoot;

        public MeasFilterUI() {
            createUI();
        }

        public GridPane getRoot() {
            return mRoot;
        }

        private void createUI() {
            mRoot = new GridPane();
            double borderInnerPadding = FxHelper.getUIScaled(8.0);
            double topBorderInnerPadding = FxHelper.getUIScaled(0.0);
            int row = 1;
            mRoot.addRow(row++, mVelocitySliderPane, mVelocityArgBox);

//            var spinners = new Spinner[]{mLevelPeriodAllSds};
//            FxHelper.setEditable(true, spinners);
//            FxHelper.autoCommitSpinners(spinners);
//            FxHelper.autoSizeRegionHorizontal(mLevelPeriodDirectionScb);
            FxHelper.autoSizeColumn(mRoot, 2);

            mVelocityArgBox.disableProperty().bind(mVelocitySliderPane.selectedProperty().not());
            GridPane.setValignment(mVelocityArgBox, VPos.CENTER);
        }

    }

    private enum Direction {
        EITHER("±"),
        POS("+"),
        NEG("-");
        private final String mTitle;

        private Direction(String title) {
            mTitle = title;
        }

        @Override
        public String toString() {
            return mTitle;
        }

    }

    private class ArgBox extends HBox {

        private final SessionComboBox<Direction> mDirectionScb = new SessionComboBox<>();
        private final SessionComboBox<String> mGtLtScb = new SessionComboBox<>();

        public ArgBox() {
            super(FxHelper.getUIScaled(8d));
            createUI();
        }

        private void createUI() {
            setAlignment(Pos.CENTER_LEFT);
            mDirectionScb.getItems().setAll(Direction.values());
            mGtLtScb.getItems().setAll("<=", ">=");
            mGtLtScb.getSelectionModel().selectFirst();
            mDirectionScb.getSelectionModel().selectFirst();
            getChildren().setAll(mDirectionScb, mGtLtScb);
        }

        private void reset() {
            mGtLtScb.getSelectionModel().selectFirst();
            mDirectionScb.getSelectionModel().selectFirst();
        }

    }
}
