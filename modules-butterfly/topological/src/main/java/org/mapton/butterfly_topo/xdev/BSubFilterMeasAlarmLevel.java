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
package org.mapton.butterfly_topo.xdev;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.lang3.ArrayUtils;
import static org.mapton.api.ui.forms.MBaseFilterSection.GAP_H;
import static org.mapton.api.ui.forms.MBaseFilterSection.GAP_V;
import org.mapton.api.ui.forms.NegPosStringConverterInteger;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.butterfly_topo.shared.AlarmLevelChangeMode;
import org.mapton.butterfly_topo.shared.AlarmLevelChangeUnit;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;
import se.trixon.almond.util.fx.session.SessionComboBox;
import se.trixon.almond.util.fx.session.SessionIntegerSpinner;

/**
 *
 * @author Patrik Karlström
 */
public class BSubFilterMeasAlarmLevel<T extends BXyzPoint> extends BSubFilterMeasBase {

    private static final int mDefaultAlarmLevelAgeValue = -7;
    private static final int mDefaultMeasAlarmLevelChangeLimit = 1;
    private static final int mDefaultMeasAlarmLevelChangeValue = 10;

    private Function<T, Integer> mAlarmAgeFunction;
    private final CheckBox mAlarmLevelChangeCheckbox = new CheckBox();
    private final SessionIntegerSpinner mAlarmLevelChangeLimitSis = new SessionIntegerSpinner(1, 100, mDefaultMeasAlarmLevelChangeLimit);
    private final SessionComboBox<AlarmLevelChangeMode> mAlarmLevelChangeModeScb = new SessionComboBox<>();
    private final SessionComboBox<AlarmLevelChangeUnit> mAlarmLevelChangeUnitScb = new SessionComboBox<>();
    private final SessionIntegerSpinner mAlarmLevelChangeValueSis = new SessionIntegerSpinner(2, 10000, mDefaultMeasAlarmLevelChangeValue);
    private Function<T, Integer> mAlarmLevelFunction;
    private final SessionCheckComboBox<BAlarmLevel> mAlarmLevelsSccb = new SessionCheckComboBox<>(true);
    private final CheckBox mMeasAlarmLevelAgeCheckbox = new CheckBox();
    private final SessionIntegerSpinner mMeasAlarmLevelAgeSis = new SessionIntegerSpinner(Integer.MIN_VALUE, Integer.MAX_VALUE, mDefaultAlarmLevelAgeValue);

    private final VBox mRoot = new VBox(GAP_V);
    private Node mRootBordered;

    public BSubFilterMeasAlarmLevel() {
        super(SDict.ALARM_LEVEL.toString());
        createUI();
    }

    @Override
    public void clear() {
        SessionCheckComboBox.clearChecks(
                mAlarmLevelsSccb
        );
        FxHelper.setSelected(false,
                mAlarmLevelChangeCheckbox,
                mMeasAlarmLevelAgeCheckbox
        );
        mMeasAlarmLevelAgeSis.getValueFactory().setValue(mDefaultAlarmLevelAgeValue);
        mAlarmLevelChangeLimitSis.getValueFactory().setValue(mDefaultMeasAlarmLevelChangeLimit);
        mAlarmLevelChangeValueSis.getValueFactory().setValue(mDefaultMeasAlarmLevelChangeValue);

    }

    @Override
    public void createInfoContent(LinkedHashMap map) {
        map.put(getTitle(), "TODO");
    }

    @Override
    public boolean filter(BXyzPoint p) {
        return validateAlarmLevel((T) p)
                && validateAlarmLevelAge((T) p)
                && validateAlarmLevelChange((T) p);
    }

    public Function<T, Integer> getAlarmAgeFunction() {
        return mAlarmAgeFunction;
    }

    public Function<T, Integer> getAlarmLevelFunction() {
        return mAlarmLevelFunction;
    }

    public VBox getRoot() {
        return mRoot;
    }

    public Node getRootBordered() {
        if (mRootBordered == null) {
            mRootBordered = wrapInTitleBorder(getTitle(), mRoot);
        }

        return mRootBordered;
    }

    @Override
    public void initListeners(ChangeListener changeListener, ListChangeListener listChangeListener) {
        List.of(
                mMeasAlarmLevelAgeCheckbox.selectedProperty(),
                mMeasAlarmLevelAgeSis.valueProperty(),
                mAlarmLevelChangeCheckbox.selectedProperty(),
                mAlarmLevelChangeLimitSis.valueProperty(),
                mAlarmLevelChangeValueSis.valueProperty(),
                mAlarmLevelChangeLimitSis.valueProperty(),
                mAlarmLevelChangeValueSis.valueProperty()
        ).forEach(o -> o.addListener(changeListener));

        List.of(
                mAlarmLevelsSccb.getCheckModel().getCheckedItems()
        ).forEach(o -> o.addListener(listChangeListener));
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        sessionManager.register("filter.checkedNextAlarm", mAlarmLevelsSccb.checkedStringProperty());
        sessionManager.register("filter.measAlarmLevelAge", mMeasAlarmLevelAgeCheckbox.selectedProperty());
        sessionManager.register("filter.measAlarmLevelAgeValue", mMeasAlarmLevelAgeSis.sessionValueProperty());
        sessionManager.register("filter.measAlarmLevelChange", mAlarmLevelChangeCheckbox.selectedProperty());
        sessionManager.register("filter.measAlarmLevelChangeLimit", mAlarmLevelChangeLimitSis.sessionValueProperty());
        sessionManager.register("filter.measAlarmLevelChangeValue", mAlarmLevelChangeValueSis.sessionValueProperty());
        sessionManager.register("filter.measAlarmLevelChangeMode", mAlarmLevelChangeModeScb.selectedIndexProperty());
        sessionManager.register("filter.measAlarmLevelChangeUnit", mAlarmLevelChangeUnitScb.selectedIndexProperty());
    }

    @Override
    public void load(ArrayList items) {
        mAlarmLevelsSccb.loadAndRestoreCheckItems();
        mMeasAlarmLevelAgeSis.load();
        mAlarmLevelChangeLimitSis.load();
        mAlarmLevelChangeModeScb.load();
        mAlarmLevelChangeUnitScb.load();

        mMeasAlarmLevelAgeSis.disableProperty().bind(mMeasAlarmLevelAgeCheckbox.selectedProperty().not());
        mAlarmLevelChangeLimitSis.disableProperty().bind(mAlarmLevelChangeCheckbox.selectedProperty().not());
        mAlarmLevelChangeModeScb.disableProperty().bind(mAlarmLevelChangeCheckbox.selectedProperty().not());
        mAlarmLevelChangeUnitScb.disableProperty().bind(mAlarmLevelChangeCheckbox.selectedProperty().not());
        mAlarmLevelChangeValueSis.disableProperty().bind(mAlarmLevelChangeCheckbox.selectedProperty().not());
        mAlarmLevelChangeValueSis.load();
    }

    @Override
    public void onShownFirstTime() {
    }

    @Override
    public void reset(PropertiesConfiguration filterConfig) {
    }

    public void setAlarmAgeFunction(Function<T, Integer> alarmAgeFunction) {
        this.mAlarmAgeFunction = alarmAgeFunction;
    }

    public void setAlarmLevelFunction(Function<T, Integer> alarmLevelFunction) {
        this.mAlarmLevelFunction = alarmLevelFunction;
    }

    private void createUI() {
        FxHelper.setShowCheckedCount(true,
                mAlarmLevelsSccb
        );
        mAlarmLevelsSccb.setTitle(getTitle());
        mAlarmLevelsSccb.getItems().setAll(BAlarmLevel.values());
        mMeasAlarmLevelAgeCheckbox.setText("Ålder på larmnivå");
        mMeasAlarmLevelAgeSis.getValueFactory().setConverter(new NegPosStringConverterInteger());
        mAlarmLevelChangeCheckbox.setText("Larmnivåförändringar");
        mAlarmLevelChangeModeScb.getItems().setAll(AlarmLevelChangeMode.values());
        mAlarmLevelChangeUnitScb.getItems().setAll(AlarmLevelChangeUnit.values());
        mAlarmLevelChangeCheckbox.setDisable(true);
        var alcGridPane = new GridPane(GAP_H, GAP_V);
        alcGridPane.add(mAlarmLevelChangeCheckbox, 0, 0, GridPane.REMAINING, 1);
        alcGridPane.addRow(1, mAlarmLevelChangeLimitSis, mAlarmLevelChangeModeScb);
        alcGridPane.addRow(2, mAlarmLevelChangeValueSis, mAlarmLevelChangeUnitScb);
        mAlarmLevelChangeLimitSis.setPrefWidth(spinnerWidth);
        mAlarmLevelChangeValueSis.setPrefWidth(spinnerWidth);

        mRoot.getChildren().addAll(
                mAlarmLevelsSccb,
                new VBox(titleGap, mMeasAlarmLevelAgeCheckbox, mMeasAlarmLevelAgeSis),
                alcGridPane
        );

        var spinners = new Spinner[]{
            mMeasAlarmLevelAgeSis,
            mAlarmLevelChangeLimitSis,
            mAlarmLevelChangeValueSis
        };

        FxHelper.autoSizeRegionHorizontal(mAlarmLevelsSccb, mAlarmLevelChangeModeScb, mAlarmLevelChangeUnitScb);
        FxHelper.setEditable(true, spinners);
        FxHelper.autoCommitSpinners(spinners);
    }

    private boolean validateAlarmLevel(T p) {
        var alarmLevelCheckModel = mAlarmLevelsSccb.getCheckModel();
        if (alarmLevelCheckModel.isEmpty()) {
            return true;
        }

        var level = mAlarmLevelFunction.apply(p);
        var selectedLevels = alarmLevelCheckModel.getCheckedItems().stream().mapToInt(a -> a.getLevel()).toArray();

        return ArrayUtils.contains(selectedLevels, level);
    }

    private boolean validateAlarmLevelAge(T p) {
        if (!mMeasAlarmLevelAgeCheckbox.isSelected()) {
            return true;
        }

        var value = mAlarmAgeFunction.apply(p);
        if (value == null) {
            return true;
        }

        var lim = mMeasAlarmLevelAgeSis.getValue();

        value = Math.abs(value);

        if (lim == 0) {
            return value == 0;
        } else if (lim < 0) {
            return value <= Math.abs(lim) && value != 0;
        } else if (lim > 0) {
            return value >= lim;
        }

        return true;
    }

    private boolean validateAlarmLevelChange(T p) {
        if (!mAlarmLevelChangeCheckbox.isSelected()) {
            return true;
        }
        return true;
//        var observations = p.extOrNull().getObservationsTimeFiltered();
//        if (observations.size() < 2) {
//            return false;
//        }
//
//        var unit = mAlarmLevelChangeUnitScb.getSelectionModel().getSelectedItem();
//        int value = mAlarmLevelChangeValueSis.getValue();
//
//        Stream<? extends BXyzPointObservation> source;
//        if (unit == AlarmLevelChangeUnit.DAYS) {
//            source = observations.stream()
//                    .filter(o -> MTemporalManager.getInstance().isValid(o.getDate()))
//                    .filter(o -> DateHelper.isAfterOrEqual(o.getDate().toLocalDate(), LocalDate.now().minusDays(value)));
//        } else {
//            source = observations.stream()
//                    .filter(o -> MTemporalManager.getInstance().isValid(o.getDate()))
//                    .skip(Math.max(0, observations.size() - value));
//        }
//
//        var filteredObservations = source.toList();
//
//        if (filteredObservations.isEmpty()) {
//            return false;
//        }
//
//        int countBetter = 0;
//        int countWorse = 0;
//
//        for (int i = 1; i < filteredObservations.size(); i++) {
//            var prev = filteredObservations.get(i - 1);
//            var current = filteredObservations.get(i);
//            int prevLevel = p.ext().getAlarmLevel(prev);
//            int currentLevel = p.ext().getAlarmLevel(current);
//
//            if (prevLevel > currentLevel) {
//                countBetter++;
//            }
//
//            if (prevLevel < currentLevel) {
//                countWorse++;
//            }
//        }
//
//        var mode = mAlarmLevelChangeModeScb.getSelectionModel().getSelectedItem();
//        int limit = mAlarmLevelChangeLimitSis.getValue();
//
//        switch (mode) {
//            case AlarmLevelChangeMode.BETTER -> {
//                return countBetter >= limit;
//            }
//            case AlarmLevelChangeMode.WORSE -> {
//                return countWorse >= limit;
//            }
//            case AlarmLevelChangeMode.EITHER -> {
//                return countBetter + countWorse >= limit;
//            }
//            default ->
//                throw new AssertionError();
//        }
    }
}
