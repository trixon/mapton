/*
 * Copyright 2024 Patrik Karlström.
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
package org.mapton.butterfly_core.api;

import com.dlsc.gemsfx.util.SessionManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.api.MTemporalManager;
import org.mapton.api.ui.forms.MBaseFilterSection;
import static org.mapton.api.ui.forms.MBaseFilterSection.GAP_V;
import org.mapton.api.ui.forms.NegPosStringConverterInteger;
import static org.mapton.butterfly_core.api.BFilterSectionAlarm.AlarmElement.*;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import static org.mapton.butterfly_format.types.BDimension._1d;
import static org.mapton.butterfly_format.types.BDimension._2d;
import static org.mapton.butterfly_format.types.BDimension._3d;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.openide.util.NbBundle;
import se.trixon.almond.util.BooleanHelper;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.fx.BindingHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;
import se.trixon.almond.util.fx.session.SessionComboBox;
import se.trixon.almond.util.fx.session.SessionIntegerSpinner;

/**
 *
 * @author Patrik Karlström
 */
public class BFilterSectionAlarm extends MBaseFilterSection {

    public static final int DEFAULT_LEVEL_AGE_VALUE = -7;
    public static final int DEFAULT_LEVEL_CHANGE_LIMIT = 1;
    public static final int DEFAULT_LEVEL_CHANGE_VALUE = 10;
    public static final int DEFAULT_PERCENTAGE_VALUE = 80;

    private final AlarmLevelCalculator mAlarmLevelCalculator;
    private final ResourceBundle mBundle = NbBundle.getBundle(BFilterSectionAlarm.class);
    private final CheckBox mLevelAgeCheckBox = new CheckBox();
    private final SessionIntegerSpinner mLevelAgeSis = new SessionIntegerSpinner(Integer.MIN_VALUE, Integer.MAX_VALUE, DEFAULT_LEVEL_AGE_VALUE);
    private final CheckBox mLevelChangeCheckbox = new CheckBox();
    private final SessionIntegerSpinner mLevelChangeLimitSis = new SessionIntegerSpinner(1, 100, DEFAULT_LEVEL_CHANGE_LIMIT);
    private final SessionComboBox<AlarmLevelChangeMode> mLevelChangeModeScb = new SessionComboBox<>();
    private final SessionComboBox<AlarmLevelChangeUnit> mLevelChangeUnitScb = new SessionComboBox<>();
    private final SessionIntegerSpinner mLevelChangeValueSis = new SessionIntegerSpinner(2, 10000, DEFAULT_LEVEL_CHANGE_VALUE);
    private final SessionCheckComboBox<AlarmLevelFilter> mLevelSccb = new SessionCheckComboBox<>(true);
    private final CheckBox mPercentageHCheckbox = new CheckBox();
    private final SessionIntegerSpinner mPercentageHSis = new SessionIntegerSpinner(-1000, 1000, DEFAULT_PERCENTAGE_VALUE, 10);
    private final CheckBox mPercentagePCheckbox = new CheckBox();
    private final SessionIntegerSpinner mPercentagePSis = new SessionIntegerSpinner(-1000, 1000, DEFAULT_PERCENTAGE_VALUE, 10);
    private final GridPane mRoot = new GridPane(columnGap, rowGap);
    private final CheckBox mSameAlarmCheckBox = new CheckBox();

    public BFilterSectionAlarm(AlarmLevelCalculator alarmLevelCalculator) {
        super(SDict.ALARM.toString());
        mAlarmLevelCalculator = alarmLevelCalculator;
        createUI();
        setContent(mRoot);
    }

    @Override
    public void clear() {
        super.clear();
        FxHelper.setSelected(false,
                mSameAlarmCheckBox,
                mLevelChangeCheckbox,
                mLevelAgeCheckBox,
                mPercentageHCheckbox,
                mPercentagePCheckbox
        );

        mPercentageHSis.getValueFactory().setValue(DEFAULT_PERCENTAGE_VALUE);
        mPercentagePSis.getValueFactory().setValue(DEFAULT_PERCENTAGE_VALUE);
        mLevelAgeSis.getValueFactory().setValue(DEFAULT_LEVEL_AGE_VALUE);
        mLevelChangeLimitSis.getValueFactory().setValue(DEFAULT_LEVEL_CHANGE_LIMIT);
        mLevelChangeValueSis.getValueFactory().setValue(DEFAULT_LEVEL_CHANGE_VALUE);

        SessionCheckComboBox.clearChecks(
                mLevelSccb
        );
    }

    @Override
    public void createInfoContent(LinkedHashMap<String, String> map) {
        if (!isSelected()) {
            return;
        }
        map.put(SDict.ALARM.toUpper(), ".");
        if (mSameAlarmCheckBox.isSelected()) {
            map.put(mBundle.getString("sameAlarmCheckBoxText"), BooleanHelper.asYesNo(mSameAlarmCheckBox.isSelected()));
        }
    }

    public void disable(AlarmElement... elements) {
        var map = new HashMap<AlarmElement, Node>();
        map.put(SAME_ALARM, mSameAlarmCheckBox);

        for (var element : elements) {
            map.get(element).setDisable(true);
        }
    }

    public boolean filter(BXyzPoint p) {
        if (isSelected()) {
            var valid = true
                    & validateLevel(p)
                    && validateLevelAge(p)
                    && validateLevelChange(p)
                    && validatePercentageH(p)
                    && validatePercentageP(p);

            return valid;
        } else {
            return true;
        }
    }

    public CheckBox getSameAlarmCheckBox() {
        return mSameAlarmCheckBox;
    }

    public void initListeners(ChangeListener changeListenerObject, ListChangeListener<Object> listChangeListener) {
        List.of(
                selectedProperty(),
                mLevelAgeCheckBox.selectedProperty(),
                mLevelChangeCheckbox.selectedProperty(),
                mSameAlarmCheckBox.selectedProperty(),
                mPercentageHCheckbox.selectedProperty(),
                mPercentagePCheckbox.selectedProperty(),
                mPercentageHSis.sessionValueProperty(),
                mPercentagePSis.sessionValueProperty(),
                //
                mLevelAgeSis.valueProperty(),
                mLevelChangeValueSis.valueProperty(),
                mLevelChangeLimitSis.valueProperty(),
                //
                mLevelChangeModeScb.selectedIndexProperty(),
                mLevelChangeUnitScb.selectedIndexProperty()
        ).forEach(propertyBase -> propertyBase.addListener(changeListenerObject));

        List.of(
                mLevelSccb.getCheckModel()
        ).forEach(cm -> cm.getCheckedItems().addListener(listChangeListener));
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        setSessionManager(sessionManager);
        sessionManager.register(getKeyFilter("section"), selectedProperty());
        sessionManager.register(getKeyFilter("level"), mLevelSccb.checkedStringProperty());
        sessionManager.register(getKeyFilter("levelAge"), mLevelAgeCheckBox.selectedProperty());
        sessionManager.register(getKeyFilter("levelAgeValue"), mLevelAgeSis.sessionValueProperty());
        sessionManager.register(getKeyFilter("levelChange"), mLevelChangeCheckbox.selectedProperty());
        sessionManager.register(getKeyFilter("levelChangeMode"), mLevelChangeModeScb.selectedIndexProperty());
        sessionManager.register(getKeyFilter("levelChangeUnit"), mLevelChangeUnitScb.selectedIndexProperty());
        sessionManager.register(getKeyFilter("levelChangeValue"), mLevelChangeValueSis.sessionValueProperty());
        sessionManager.register(getKeyFilter("levelChangeLimit"), mLevelChangeLimitSis.sessionValueProperty());
        sessionManager.register(getKeyFilter("sameAlarm"), mSameAlarmCheckBox.selectedProperty());
        sessionManager.register(getKeyFilter("percentageH"), mPercentageHCheckbox.selectedProperty());
        sessionManager.register(getKeyFilter("percentageP"), mPercentagePCheckbox.selectedProperty());
        sessionManager.register(getKeyFilter("percentageHValue"), mPercentageHSis.sessionValueProperty());
        sessionManager.register(getKeyFilter("percentagePValue"), mPercentagePSis.sessionValueProperty());
    }

    public void load(ArrayList<? extends BXyzPoint> items) {
        mLevelChangeModeScb.load();
        mLevelChangeUnitScb.load();
        mLevelChangeValueSis.load();
        mLevelChangeLimitSis.load();
        mLevelAgeSis.load();
        mLevelAgeSis.disableProperty().bind(mLevelAgeCheckBox.selectedProperty().not());
        mLevelChangeLimitSis.disableProperty().bind(mLevelChangeCheckbox.selectedProperty().not());
        mLevelChangeModeScb.disableProperty().bind(mLevelChangeCheckbox.selectedProperty().not());
        mLevelChangeUnitScb.disableProperty().bind(mLevelChangeCheckbox.selectedProperty().not());
        mLevelChangeValueSis.disableProperty().bind(mLevelChangeCheckbox.selectedProperty().not());
        mPercentageHSis.load();
        mPercentagePSis.load();
        mPercentageHSis.disableProperty().bind(mPercentageHCheckbox.selectedProperty().not());
        mPercentagePSis.disableProperty().bind(mPercentagePCheckbox.selectedProperty().not());

        mLevelSccb.loadAndRestoreCheckItems();

    }

    @Override
    public void onShownFirstTime() {
        FxHelper.setVisibleRowCount(25, mLevelSccb);
    }

    @Override
    public void reset(PropertiesConfiguration filterConfig) {
    }

    private void createUI() {
        mSameAlarmCheckBox.setText(mBundle.getString("sameAlarmCheckBoxText"));
        FxHelper.setShowCheckedCount(true,
                mLevelSccb
        );
        mLevelSccb.setTitle(SDict.ALARM_LEVEL.toString());
        mLevelSccb.getItems().setAll(AlarmLevelFilter.values());

        mLevelChangeModeScb.getItems().setAll(AlarmLevelChangeMode.values());
        mLevelChangeUnitScb.getItems().setAll(AlarmLevelChangeUnit.values());
        mLevelAgeSis.getValueFactory().setConverter(new NegPosStringConverterInteger());
        mLevelChangeCheckbox.setText(mBundle.getString("measAlarmLevelChangeCheckBoxText"));
        mLevelAgeCheckBox.setText("Ålder på larmnivå");
        mPercentageHCheckbox.setText(mBundle.getString("diffMeasPercentageHCheckboxText"));
        mPercentagePCheckbox.setText(mBundle.getString("diffMeasPercentagePCheckboxText"));

        mPercentageHSis.getValueFactory().setConverter(new NegPosStringConverterInteger());
        mPercentagePSis.getValueFactory().setConverter(new NegPosStringConverterInteger());
        FxHelper.setPadding(new Insets(GAP_V, 0, 0, 0), mLevelAgeCheckBox, mLevelChangeCheckbox);
        var alcGridPane = new GridPane(GAP_H, GAP_V);
        alcGridPane.add(mLevelChangeCheckbox, 0, 0, GridPane.REMAINING, 1);
        alcGridPane.addRow(1, mLevelChangeLimitSis, mLevelChangeModeScb);
        alcGridPane.addRow(2, mLevelChangeValueSis, mLevelChangeUnitScb);
        var diffPercentGridPane = new GridPane(GAP_H, GAP_V);
        diffPercentGridPane.addColumn(0, mPercentageHCheckbox, mPercentageHSis);
        diffPercentGridPane.addColumn(1, mPercentagePCheckbox, mPercentagePSis);

        mLevelChangeLimitSis.setPrefWidth(spinnerWidth);
        mLevelChangeValueSis.setPrefWidth(spinnerWidth);

        var spinners = new Spinner[]{
            mPercentageHSis,
            mPercentagePSis,
            mLevelChangeValueSis,
            mLevelChangeLimitSis,
            mLevelAgeSis
        };
        FxHelper.setEditable(true, spinners);
        FxHelper.autoCommitSpinners(spinners);

        var alarmBox = new VBox(GAP_V, mLevelSccb, new VBox(titleGap, mLevelAgeCheckBox, mLevelAgeSis), alcGridPane);
        var wrappedAlarmBox = wrapInTitleBorder("Larmnivå", alarmBox);
        var wrappedPercentageBox = wrapInTitleBorder("Larmförbrukning", diffPercentGridPane);
        var leftBox = new VBox(rowGap,
                wrappedAlarmBox
        );
        var rightBox = new VBox(rowGap,
                wrappedPercentageBox
        );

        int row = 0;
        mRoot.addRow(row++, leftBox, rightBox);
        FxHelper.autoSizeColumn(mRoot, 2);
        FxHelper.autoSizeRegionHorizontal(mLevelSccb, mLevelChangeModeScb, mLevelChangeUnitScb, mPercentageHSis, mPercentagePSis);
        BindingHelper.bindWidthForChildrens(leftBox, rightBox);
    }

    private boolean validateLevel(BXyzPoint p) {
        var levelCheckModel = mLevelSccb.getCheckModel();
        if (levelCheckModel.isEmpty()) {
            return true;
        }

        var level = mAlarmLevelCalculator.getLevel(p);
        var levelH = mAlarmLevelCalculator.getLevel1(p);
        var levelP = mAlarmLevelCalculator.getLevel2(p);

        var anyAlarmLevelFilterValues = EnumSet.of(AlarmLevelFilter.ANY_0, AlarmLevelFilter.ANY_1, AlarmLevelFilter.ANY_2, AlarmLevelFilter.ANY_3, AlarmLevelFilter.ANY_E);

        for (var alarmFilter : AlarmLevelFilter.values()) {
            var itemChecked = levelCheckModel.isChecked(alarmFilter);
            if (anyAlarmLevelFilterValues.contains(alarmFilter) && itemChecked) {
                if (alarmFilter == AlarmLevelFilter.ANY_0 && level == 0) {
                    return true;
                } else if (alarmFilter == AlarmLevelFilter.ANY_1 && level == 1) {
                    return true;
                } else if (alarmFilter == AlarmLevelFilter.ANY_2 && level == 2) {
                    return true;
                } else if (alarmFilter == AlarmLevelFilter.ANY_3 && level == 3) {
                    return true;
                } else if (alarmFilter == AlarmLevelFilter.ANY_E && level == -1) {
                    return true;
                }
            }
            var validH = itemChecked && alarmFilter.getComponent() == BComponent.HEIGHT && alarmFilter.getLevel() == levelH;
            var validP = itemChecked && alarmFilter.getComponent() == BComponent.PLANE && alarmFilter.getLevel() == levelP;
            var valid = false;
            switch (p.getDimension()) {
                case _1d ->
                    valid = validH;
                case _2d ->
                    valid = validP;
                case _3d -> {
                    var hSelected = levelCheckModel.isChecked(AlarmLevelFilter.HEIGHT_0)
                            || levelCheckModel.isChecked(AlarmLevelFilter.HEIGHT_1)
                            || levelCheckModel.isChecked(AlarmLevelFilter.HEIGHT_2)
                            || levelCheckModel.isChecked(AlarmLevelFilter.HEIGHT_3)
                            || levelCheckModel.isChecked(AlarmLevelFilter.HEIGHT_E);

                    var pSelected = levelCheckModel.isChecked(AlarmLevelFilter.PLANE_0)
                            || levelCheckModel.isChecked(AlarmLevelFilter.PLANE_1)
                            || levelCheckModel.isChecked(AlarmLevelFilter.PLANE_2)
                            || levelCheckModel.isChecked(AlarmLevelFilter.PLANE_3)
                            || levelCheckModel.isChecked(AlarmLevelFilter.PLANE_E);
                    if (hSelected && pSelected) {
                        valid = validH && validP;
                    } else if (hSelected) {
                        valid = validH;
                    } else if (pSelected) {
                        valid = validP;
                    }
                }
                default ->
                    throw new AssertionError();
            }

            if (valid) {
                return true;
            }
        }

        return false;
    }

    private boolean validateLevelAge(BXyzPoint p) {
        if (!mLevelAgeCheckBox.isSelected()) {
            return true;
        }

        var hset = new HashSet<Integer>();
        var pset = new HashSet<Integer>();

        for (var o : p.extOrNull().getObservationsTimeFiltered()) {
            hset.add(p.extOrNull().getAlarmLevel(BComponent.HEIGHT, o));
            pset.add(p.extOrNull().getAlarmLevel(BComponent.PLANE, o));
            if (hset.size() > 1 || pset.size() > 1) {
                break;
            }
        }

        var noChangeH = hset.size() < 2;
        var noChangeP = pset.size() < 2;

        switch (p.getDimension()) {
            case _1d -> {
                if (noChangeH) {
                    return false;
                }
            }
            case _2d -> {
                if (noChangeP) {
                    return false;
                }
            }
            case _3d -> {
                if (noChangeH || noChangeP) {
                    return false;
                }
            }
            default ->
                throw new AssertionError();
        }

        var lim = mLevelAgeSis.getValue();
        Long value = null;

        var ageH = p.extOrNull().getAlarmLevelAge(BComponent.HEIGHT);
        var ageP = p.extOrNull().getAlarmLevelAge(BComponent.PLANE);

        if (ObjectUtils.allNull(ageH, ageP)) {
            return true;
        }

        switch (p.getDimension()) {
            case BDimension._1d -> {
                value = p.extOrNull().getAlarmLevelAge(BComponent.HEIGHT);
            }
            case BDimension._2d -> {
                value = p.extOrNull().getAlarmLevelAge(BComponent.PLANE);
            }
            case BDimension._3d -> {
                var valueH = p.extOrNull().getAlarmLevelAge(BComponent.HEIGHT);
                var valueP = p.extOrNull().getAlarmLevelAge(BComponent.PLANE);

                if (ObjectUtils.allNotNull(valueH, valueP)) {
                    if (lim < 0) {
                        value = Math.max(valueH, valueP);
                    } else {

                        value = Math.min(valueH, valueP);
                    }
                } else if (valueH == null) {
                    value = valueP;
                } else if (valueP == null) {
                    value = valueH;
                }
            }

            default ->
                throw new AssertionError();
        }

        if (ObjectUtils.allNull(value)) {
            return true;
        }

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

    private boolean validateLevelChange(BXyzPoint p) {
        if (!mLevelChangeCheckbox.isSelected()) {
            return true;
        }

        var observations = p.extOrNull().getObservationsTimeFiltered().stream()
                .filter(o -> MTemporalManager.getInstance().isValid(o.getDate()))
                .filter(o -> DateHelper.isAfterOrEqual(o.getDate().toLocalDate(), p.getDateZero()))
                .toList();

        if (observations.size() < 2) {
            return false;
        }

        var mode = mLevelChangeModeScb.getValue();
        var unit = mLevelChangeUnitScb.getValue();
        int value = mLevelChangeValueSis.getValue();
        int limit = mLevelChangeLimitSis.getValue();

        if (unit == AlarmLevelChangeUnit.DAYS) {
            observations = observations.stream()
                    .filter(o -> DateHelper.isAfterOrEqual(o.getDate().toLocalDate(), LocalDate.now().minusDays(value)))
                    .toList();
        } else {
            observations = observations.stream()
                    .skip(Math.max(0, observations.size() - value))
                    .toList();
        }

        if (observations.size() < 2) {
            return false;
        }

        int countBetter = 0;
        int countWorse = 0;

        for (int i = 1; i < observations.size(); i++) {
            var prev = observations.get(i - 1);
            var current = observations.get(i);
            int prevLevel = p.extOrNull().getAlarmLevel(prev);
            int currentLevel = p.extOrNull().getAlarmLevel(current);

            if (prevLevel > currentLevel) {
                countBetter++;
            }

            if (prevLevel < currentLevel) {
                countWorse++;
            }
        }

        switch (mode) {
            case AlarmLevelChangeMode.BETTER -> {
                return countBetter >= limit;
            }
            case AlarmLevelChangeMode.WORSE -> {
                return countWorse >= limit;
            }
            case AlarmLevelChangeMode.EITHER -> {
                return countBetter + countWorse >= limit;
            }
            default ->
                throw new AssertionError();
        }
    }

    private boolean validatePercentageH(BXyzPoint p) {
        var ext = p.extOrNull();
        if (!mPercentageHCheckbox.isSelected() || p.getDimension() == BDimension._2d) {
            return true;
        } else if (ext.getAlarmPercent(BComponent.HEIGHT) == null) {
            return false;
        }

        double lim = mPercentageHSis.getValue();
        double value = ext.getAlarmPercent(BComponent.HEIGHT);

        if (lim == 0) {
            return value == 0;
        } else if (lim < 0) {
            return value <= Math.abs(lim);
        } else {
            return value >= lim;
        }
    }

    private boolean validatePercentageP(BXyzPoint p) {
        var ext = p.extOrNull();
        if (!mPercentagePCheckbox.isSelected()) {
            return true;
        } else if (p.getDimension() == BDimension._1d) {
            return false;
        } else if (ext.getAlarmPercent(BComponent.PLANE) == null) {
            return false;
        }
        double lim = mPercentagePSis.getValue();
        double value = ext.getAlarmPercent(BComponent.PLANE);

        if (lim == 0) {
            return value == 0;
        } else if (lim < 0) {
            return value <= Math.abs(lim);
        } else {
            return value >= lim;
        }
    }

    public enum AlarmElement {
        SAME_ALARM;
    }
}
