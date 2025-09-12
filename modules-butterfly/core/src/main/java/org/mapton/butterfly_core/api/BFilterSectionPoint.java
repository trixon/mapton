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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.IndexedCheckModel;
import org.controlsfx.tools.Borders;
import org.mapton.api.ui.forms.MBaseFilterSection;
import static org.mapton.butterfly_core.api.BFilterSectionPoint.PointElement.*;
import org.mapton.butterfly_format.types.BComponent;
import static org.mapton.butterfly_format.types.BDimension._1d;
import static org.mapton.butterfly_format.types.BDimension._2d;
import static org.mapton.butterfly_format.types.BDimension._3d;
import org.mapton.butterfly_format.types.BMeasurementMode;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.RangeSliderPane;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class BFilterSectionPoint extends MBaseFilterSection {

    private final SessionCheckComboBox<String> mAlarmNameSccb;
    private final SessionCheckComboBox<AlarmFlags> mAlarmStatSccb;
    private final RangeSliderPane mAltitudeRangeSlider = new RangeSliderPane("Z", -100.0, 100.0, false);
    private final ResourceBundle mBundle = NbBundle.getBundle(getClass());
    private final SessionCheckComboBox<String> mCategorySccb;
    private final SessionCheckComboBox<Integer> mDefaultFrequencySccb;
    private final SessionCheckComboBox<DefaultFreqFlags> mDefaultFrequencyStatSccb;
    private final SessionCheckComboBox<Integer> mFrequencySccb;
    private final SessionCheckComboBox<String> mGroupSccb;
    private final SessionCheckComboBox<Integer> mIntenseFrequencySccb;
    private final SessionCheckComboBox<IntenseFreqFlags> mIntenseFrequencyStatSccb;
    private final SessionCheckComboBox<String> mMeasNextSccb;
    private final SessionCheckComboBox<String> mMeasurementModeSccb;
    private final SessionCheckComboBox<String> mOperatorSccb;
    private final SessionCheckComboBox<String> mOriginSccb;
    private final PointFilterUI mPointFilterUI;
    private final SessionCheckComboBox<String> mRollingSccb;
    private final SessionCheckComboBox<String> mSparseSccb;
    private final SessionCheckComboBox<String> mStatusSccb;
    private final SessionCheckComboBox<String> mUnitDiffSccb;
    private final SessionCheckComboBox<String> mUnitSccb;

    public BFilterSectionPoint() {
        super("Grunddata");
        mAlarmNameSccb = new SessionCheckComboBox<>();
        mStatusSccb = new SessionCheckComboBox<>();
        mOriginSccb = new SessionCheckComboBox<>();
        mOperatorSccb = new SessionCheckComboBox<>();
        mUnitSccb = new SessionCheckComboBox<>();
        mUnitDiffSccb = new SessionCheckComboBox<>();
        mMeasNextSccb = new SessionCheckComboBox<>(true);
        mGroupSccb = new SessionCheckComboBox<>();
        mRollingSccb = new SessionCheckComboBox<>();
        mSparseSccb = new SessionCheckComboBox<>();
        mFrequencySccb = new SessionCheckComboBox<>();
        mDefaultFrequencySccb = new SessionCheckComboBox<>();
        mDefaultFrequencyStatSccb = new SessionCheckComboBox<>();
        mAlarmStatSccb = new SessionCheckComboBox<>();
        mIntenseFrequencySccb = new SessionCheckComboBox<>();
        mIntenseFrequencyStatSccb = new SessionCheckComboBox<>();
        mCategorySccb = new SessionCheckComboBox<>();
        mMeasurementModeSccb = new SessionCheckComboBox<>();
        mPointFilterUI = new PointFilterUI();
        init();
        setContent(mPointFilterUI.getBaseBox());
    }

    @Override
    public void clear() {
        super.clear();
        mPointFilterUI.clear();
    }

    @Override
    public void createInfoContent(LinkedHashMap<String, String> map) {
        if (!isSelected()) {
            return;
        }
        map.put(Dict.Geometry.POINT.toUpper(), ".");
        map.put(Dict.STATUS.toString(), makeInfo(mStatusSccb.getCheckModel().getCheckedItems()));
        map.put(SDict.FREQUENCY.toString(), makeInfoInteger(mFrequencySccb.getCheckModel().getCheckedItems()));
        map.put("%s (%s)".formatted(SDict.FREQUENCY.toString(), Dict.DEFAULT.toLower()), makeInfoInteger(mDefaultFrequencySccb.getCheckModel().getCheckedItems()));
        map.put("%s (%s)".formatted(SDict.FREQUENCY.toString(), Dict.HIGH.toLower()), makeInfoInteger(mIntenseFrequencySccb.getCheckModel().getCheckedItems()));
        map.put(mBundle.getString("nextMeasCheckComboBoxTitle"), makeInfo(mMeasNextSccb.getCheckModel().getCheckedItems()));
        map.put(Dict.GROUP.toString(), makeInfo(mGroupSccb.getCheckModel().getCheckedItems()));
        map.put("Rolling formula", makeInfo(mRollingSccb.getCheckModel().getCheckedItems()));
        map.put("Sparse", makeInfo(mSparseSccb.getCheckModel().getCheckedItems()));
        map.put(Dict.CATEGORY.toString(), makeInfo(mCategorySccb.getCheckModel().getCheckedItems()));
        map.put(SDict.ALARMS.toString(), makeInfo(mAlarmNameSccb.getCheckModel().getCheckedItems()));
        map.put(SDict.OPERATOR.toString(), makeInfo(mOperatorSccb.getCheckModel().getCheckedItems()));
        map.put(Dict.ORIGIN.toString(), makeInfo(mOriginSccb.getCheckModel().getCheckedItems()));
        map.put("Mätläge", makeInfo(mMeasurementModeSccb.getCheckModel().getCheckedItems()));
    }

    public void disable(PointElement... elements) {
        var map = new HashMap<PointElement, Node>();
        map.put(ALARM, mAlarmNameSccb);
        map.put(ALARM_STAT, mAlarmStatSccb);
        map.put(ALTITUDE, mAltitudeRangeSlider);
        map.put(CATEGORY, mCategorySccb);
        map.put(FREQUENCY_DEFAULT, mDefaultFrequencySccb);
        map.put(FREQUENCY_DEFAULT_STAT, mDefaultFrequencyStatSccb);
        map.put(FREQUENCY_INTENSE, mIntenseFrequencySccb);
        map.put(FREQUENCY_INTENSE_STAT, mIntenseFrequencyStatSccb);
        map.put(FREQUENCY, mFrequencySccb);
        map.put(GROUP, mGroupSccb);
        map.put(FORMULA_ROLLING, mRollingSccb);
        map.put(FORMULA_SPARSE, mSparseSccb);
        map.put(MEAS_NEXT, mMeasNextSccb);
        map.put(MEAS_MODE, mMeasurementModeSccb);
        map.put(OPERATOR, mOperatorSccb);
        map.put(ORIGIN, mOriginSccb);
        map.put(STATUS, mStatusSccb);
        map.put(UNIT, mUnitSccb);
        map.put(UNIT_DIFF, mUnitDiffSccb);

        for (var element : elements) {
            map.get(element).setDisable(true);
        }
    }

    public boolean filter(BXyzPoint p, Long remainingDays) {
        if (isSelected()) {
            return validateCheck(mStatusSccb.getCheckModel(), p.getStatus())
                    && validateCheck(mGroupSccb.getCheckModel(), p.getGroup())
                    && validateCheck(mCategorySccb.getCheckModel(), p.getCategory())
                    && validateAlarmName(p, mAlarmNameSccb.getCheckModel())
                    && validateAlarmFlags(p, mAlarmStatSccb.getCheckModel())
                    && validateCheck(mFrequencySccb.getCheckModel(), p.getFrequency())
                    && validateCheck(mDefaultFrequencySccb.getCheckModel(), p.getFrequencyDefault())
                    && validateDefaultFregFlags(p, mDefaultFrequencyStatSccb.getCheckModel())
                    && validateCheck(mIntenseFrequencySccb.getCheckModel(), p.getFrequencyHigh())
                    && validateIntenseFregFlags(p, mIntenseFrequencyStatSccb.getCheckModel())
                    && validateCheck(mOperatorSccb.getCheckModel(), p.getOperator())
                    && validateCheck(mUnitSccb.getCheckModel(), p.getUnit())
                    && validateCheck(mUnitDiffSccb.getCheckModel(), p.getUnitDiff())
                    && validateCheck(mRollingSccb.getCheckModel(), p.getRollingFormula())
                    && validateCheck(mSparseSccb.getCheckModel(), p.getSparse())
                    && validateCheck(mOriginSccb.getCheckModel(), p.getOrigin())
                    && validateCheckMeasurementMode(mMeasurementModeSccb.getCheckModel(), p.getMeasurementMode())
                    && validateNextMeas(p, mMeasNextSccb.getCheckModel(), remainingDays)
                    && validateAltitude(p)
                    && true;
        } else {
            return true;
        }
    }

    public GridPane getRoot() {
        return mPointFilterUI.getBaseBox();
    }

    public void initListeners(ChangeListener changeListenerObject, ListChangeListener<Object> listChangeListener) {
        List.of(
                selectedProperty(),
                mAltitudeRangeSlider.selectedProperty(),
                mAltitudeRangeSlider.minProperty(),
                mAltitudeRangeSlider.maxProperty()
        ).forEach(propertyBase -> propertyBase.addListener(changeListenerObject));

        List.of(
                mMeasNextSccb.getCheckModel(),
                mStatusSccb.getCheckModel(),
                mGroupSccb.getCheckModel(),
                mCategorySccb.getCheckModel(),
                mAlarmNameSccb.getCheckModel(),
                mAlarmStatSccb.getCheckModel(),
                mFrequencySccb.getCheckModel(),
                mDefaultFrequencySccb.getCheckModel(),
                mDefaultFrequencyStatSccb.getCheckModel(),
                mIntenseFrequencySccb.getCheckModel(),
                mIntenseFrequencyStatSccb.getCheckModel(),
                mMeasurementModeSccb.getCheckModel(),
                mOperatorSccb.getCheckModel(),
                mOriginSccb.getCheckModel(),
                mUnitSccb.getCheckModel(),
                mUnitDiffSccb.getCheckModel(),
                mRollingSccb.getCheckModel(),
                mSparseSccb.getCheckModel()
        ).forEach(cm -> cm.getCheckedItems().addListener(listChangeListener));
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        sessionManager.register("filter.section.point", selectedProperty());
        mPointFilterUI.initSession(sessionManager);
    }

    public void load(ArrayList<? extends BXyzPoint> items) {
        var allAlarmNames = items.stream().map(o -> o.getAlarm1Id()).collect(Collectors.toCollection(HashSet::new));
        allAlarmNames.addAll(items.stream().map(o -> o.getAlarm2Id()).collect(Collectors.toSet()));
        mAlarmNameSccb.loadAndRestoreCheckItems(allAlarmNames.stream());
        mRollingSccb.loadAndRestoreCheckItems(items.stream().map(o -> o.getRollingFormula()));
        mSparseSccb.loadAndRestoreCheckItems(items.stream().map(o -> o.getSparse()));
        mGroupSccb.loadAndRestoreCheckItems(items.stream().map(o -> o.getGroup()));
        mCategorySccb.loadAndRestoreCheckItems(items.stream().map(o -> o.getCategory()));
        mOperatorSccb.loadAndRestoreCheckItems(items.stream().map(o -> o.getOperator()));
        mOriginSccb.loadAndRestoreCheckItems(items.stream().map(o -> o.getOrigin()));
        mUnitSccb.loadAndRestoreCheckItems(items.stream().map(o -> o.getUnit()));
        mUnitDiffSccb.loadAndRestoreCheckItems(items.stream().map(o -> o.getUnitDiff()));
        mStatusSccb.loadAndRestoreCheckItems(items.stream().map(o -> o.getStatus()));
        mFrequencySccb.loadAndRestoreCheckItems(items.stream().filter(o -> o.getFrequency() != null).map(o -> o.getFrequency()));
        mDefaultFrequencySccb.loadAndRestoreCheckItems(items.stream().filter(o -> o.getFrequencyDefault() != null).map(o -> o.getFrequencyDefault()));
        mIntenseFrequencySccb.loadAndRestoreCheckItems(items.stream().filter(o -> o.getFrequencyHigh() != null).map(o -> o.getFrequencyHigh()));
        mMeasNextSccb.loadAndRestoreCheckItems();
        mMeasurementModeSccb.loadAndRestoreCheckItems(Stream.of("Automatisk", "Manuell", "Odefinierad"));

        var minZ = items.stream().filter(p -> p.getZeroZ() != null).mapToDouble(p -> p.getZeroZ()).min().orElse(-100d);
        var maxZ = items.stream().filter(p -> p.getZeroZ() != null).mapToDouble(p -> p.getZeroZ()).max().orElse(100d);
        mAltitudeRangeSlider.setMinMaxValue(minZ - 1, maxZ + 1);
    }

    @Override
    public void onShownFirstTime() {
        mPointFilterUI.onShownFirstTime();
    }

    @Override
    public void reset(PropertiesConfiguration filterConfig) {
        if (filterConfig != null) {
            mPointFilterUI.reset(filterConfig);
        }
    }

    public boolean validateAlarmName(BXyzPoint p, IndexedCheckModel checkModel) {
        var ah = p.getAlarm1Id();
        var ap = p.getAlarm2Id();

        switch (p.getDimension()) {
            case _1d -> {
                return validateCheck(checkModel, ah);
            }
            case _2d -> {
                return validateCheck(checkModel, ap);
            }
            case _3d -> {
                return validateCheck(checkModel, ah) && validateCheck(checkModel, ap);
            }
        }

        return true;
    }

    public boolean validateAlarmName1(BXyzPoint p, IndexedCheckModel checkModel) {
        return validateCheck(checkModel, p.getAlarm1Id());
    }

    public boolean validateAlarmName2(BXyzPoint p, IndexedCheckModel checkModel) {
        return validateCheck(checkModel, p.getAlarm2Id());
    }

    public boolean validateCheckMeasurementMode(IndexedCheckModel checkModel, BMeasurementMode m) {
        if (checkModel.isEmpty()) {
            return true;
        }

        return m == BMeasurementMode.AUTOMATIC && checkModel.isChecked("Automatisk")
                || m == BMeasurementMode.MANUAL && checkModel.isChecked("Manuell")
                || m == null && checkModel.isChecked("Odefinierad");
    }

    public boolean validateDefaultFregFlags(BXyzPoint p, IndexedCheckModel checkModel) {
        if (checkModel.isEmpty()) {
            return true;
        }

        var eq = true;
        var neq = true;
        var set = true;
        var nset = true;

        if (checkModel.isChecked(DefaultFreqFlags.EQUALS)) {
            eq = ObjectUtils.compare(p.getFrequency(), p.getFrequencyDefault()) == 0;
        }

        if (checkModel.isChecked(DefaultFreqFlags.NOT_EQUALS)) {
            neq = ObjectUtils.compare(p.getFrequency(), p.getFrequencyDefault()) != 0;
        }

        if (checkModel.isChecked(DefaultFreqFlags.SET)) {
            set = ObjectUtils.compare(-1, p.getFrequencyDefault()) != 0;
        }

        if (checkModel.isChecked(DefaultFreqFlags.NOT_SET)) {
            nset = ObjectUtils.compare(-1, p.getFrequencyDefault()) == 0;
        }

        return eq
                && neq
                && set
                && nset;
    }

    public boolean validateIntenseFregFlags(BXyzPoint p, IndexedCheckModel checkModel) {
        if (checkModel.isEmpty()) {
            return true;
        }

        var eq = true;
        var neq = true;
        var set = true;
        var nset = true;
        var paramset = true;
        var paramnset = true;

        if (checkModel.isChecked(IntenseFreqFlags.EQUALS)) {
            eq = ObjectUtils.compare(p.getFrequency(), p.getFrequencyHigh()) == 0;
        }

        if (checkModel.isChecked(IntenseFreqFlags.NOT_EQUALS)) {
            neq = ObjectUtils.compare(p.getFrequency(), p.getFrequencyHigh()) != 0;
        }

        if (checkModel.isChecked(IntenseFreqFlags.SET)) {
            set = ObjectUtils.compare(-1, p.getFrequencyHigh()) != 0;
        }

        if (checkModel.isChecked(IntenseFreqFlags.NOT_SET)) {
            nset = ObjectUtils.compare(-1, p.getFrequencyHigh()) == 0;
        }

        if (checkModel.isChecked(IntenseFreqFlags.PARAM_SET)) {
            paramset = StringUtils.isNotBlank(p.getFrequencyHighParam());
        }

        if (checkModel.isChecked(IntenseFreqFlags.PARAM_NOT_SET)) {
            paramnset = StringUtils.isBlank(p.getFrequencyHighParam());
        }

        return eq
                && neq
                && set
                && nset
                && paramset
                && paramnset;
    }

    public boolean validateNextMeas(BXyzPoint p, IndexedCheckModel<String> checkModel, long remainingDays) {
        var frequency = p.getFrequency();
        var latest = p.getDateLatest() != null ? p.getDateLatest().toLocalDate() : LocalDate.MIN;
        var today = LocalDate.now();
        var nextMeas = latest.plusDays(frequency);
//        var remainingDays = ;

        if (checkModel.isEmpty()) {
            return true;
        } else if (checkModel.isChecked("∞") && frequency == 0) {
            return true;
        } else if (frequency > 0 && checkModel.isChecked("<0") && nextMeas.isBefore(today)) {
            return true;
        } else if (frequency > 0 && checkModel.isChecked("0") && remainingDays == 0) {
            return true;
        } else {
            return checkModel.getCheckedItems().stream()
                    .filter(s -> StringUtils.countMatches(s, "-") == 1)
                    .anyMatch(s -> {
                        int start = Integer.parseInt(StringUtils.substringBefore(s, "-"));
                        int end = Integer.parseInt(StringUtils.substringAfter(s, "-"));
                        return remainingDays >= start && remainingDays <= end;
                    });
        }
    }

    private void init() {
    }

    private boolean validateAlarmFlags(BXyzPoint p, IndexedCheckModel<AlarmFlags> checkModel) {
        if (checkModel.isEmpty()) {
            return true;
        }

        var set1 = true;
        var nset1 = true;
        var set2 = true;
        var nset2 = true;
        var diffset = true;
        var diffnset = true;

        var a1 = p.extOrNull().getAlarm(BComponent.HEIGHT);
        var a2 = p.extOrNull().getAlarm(BComponent.PLANE);

        if (checkModel.isChecked(AlarmFlags.SET_1)) {
            set1 = a1 != null;
        }

        if (checkModel.isChecked(AlarmFlags.NOT_SET_1)) {
            nset1 = a1 == null;
        }

        if (checkModel.isChecked(AlarmFlags.SET_2)) {
            set2 = a2 != null;
        }

        if (checkModel.isChecked(AlarmFlags.NOT_SET_2)) {
            nset2 = a2 == null;
        }

        if (checkModel.isChecked(AlarmFlags.SET_DIFF)) {
            diffset = a1 != null && a1.getRatio1() != null;
        }

        if (checkModel.isChecked(AlarmFlags.NOT_SET_DIFF)) {
            diffnset = a1 == null || a1.getRatio1() == null;
        }

        return set1
                && nset1
                && set2
                && nset2
                && diffset
                && diffnset;
    }

    private boolean validateAltitude(BXyzPoint p) {
        try {
            var z = p.getZeroZ();
            if (mAltitudeRangeSlider.selectedProperty().get()) {
                return inRange(z, mAltitudeRangeSlider.minProperty(), mAltitudeRangeSlider.maxProperty());
            } else {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public enum DefaultFreqFlags {
        EQUALS("= %s".formatted(SDict.FREQUENCY.toString())),
        NOT_EQUALS("≠ %s".formatted(SDict.FREQUENCY.toString())),
        SET("Har standardfrekvens"),
        NOT_SET("Saknar standardfrekvens");
        private final String mTitle;

        private DefaultFreqFlags(String title) {
            mTitle = title;
        }

        @Override
        public String toString() {
            return mTitle;
        }

    }

    public enum AlarmFlags {
        SET_1("Har larm 1"),
        SET_2("Har larm 2"),
        NOT_SET_1("Saknar larm 1"),
        NOT_SET_2("Saknar larm 2"),
        SET_DIFF("Har diff"),
        NOT_SET_DIFF("Saknar diff");
        private final String mTitle;

        private AlarmFlags(String title) {
            mTitle = title;
        }

        @Override
        public String toString() {
            return mTitle;
        }

    }

    public enum IntenseFreqFlags {
        EQUALS("= %s".formatted(SDict.FREQUENCY.toString())),
        NOT_EQUALS("≠ %s".formatted(SDict.FREQUENCY.toString())),
        SET("Har hög frekvens"),
        NOT_SET("Saknar hög frekvens"),
        PARAM_SET("Har hög frekvensparameter"),
        PARAM_NOT_SET("Saknar hög frekvensparameter");
        private final String mTitle;

        private IntenseFreqFlags(String title) {
            mTitle = title;
        }

        @Override
        public String toString() {
            return mTitle;
        }

    }

    public enum PointElement {
        ALARM,
        ALARM_STAT,
        ALTITUDE,
        CATEGORY,
        FORMULA_ROLLING,
        FORMULA_SPARSE,
        FREQUENCY_DEFAULT,
        FREQUENCY_DEFAULT_STAT,
        FREQUENCY_INTENSE,
        FREQUENCY_INTENSE_STAT,
        FREQUENCY,
        GROUP,
        MEAS_NEXT,
        MEAS_MODE,
        OPERATOR,
        ORIGIN,
        STATUS,
        UNIT,
        UNIT_DIFF;
    }

    public class PointFilterUI {

        private Node mBaseBorderBox;
        private GridPane mBaseBox;
        private final double mBorderInnerPadding = FxHelper.getUIScaled(8.0);
        private final double mTopBorderInnerPadding = FxHelper.getUIScaled(16.0);

        public PointFilterUI() {
            createUI();
        }

        public void clear() {
            mAltitudeRangeSlider.clear();
            SessionCheckComboBox.clearChecks(
                    mStatusSccb,
                    mGroupSccb,
                    mCategorySccb,
                    mAlarmNameSccb,
                    mAlarmStatSccb,
                    mOperatorSccb,
                    mOriginSccb,
                    mUnitSccb,
                    mUnitDiffSccb,
                    mMeasNextSccb,
                    mMeasurementModeSccb,
                    mRollingSccb,
                    mSparseSccb,
                    mFrequencySccb,
                    mDefaultFrequencyStatSccb,
                    mDefaultFrequencySccb,
                    mIntenseFrequencyStatSccb,
                    mIntenseFrequencySccb
            );
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

        public void initSession(SessionManager sessionManager) {
            sessionManager.register("filter.checkedAlarmName", mAlarmNameSccb.checkedStringProperty());
            sessionManager.register("filter.checkedAlarmStat", mAlarmStatSccb.checkedStringProperty());
            sessionManager.register("filter.checkedCategory", mCategorySccb.checkedStringProperty());
            sessionManager.register("filter.checkedFrequency", mFrequencySccb.checkedStringProperty());
            sessionManager.register("filter.checkedDefaultFrequency", mDefaultFrequencySccb.checkedStringProperty());
            sessionManager.register("filter.checkedDefaultFrequencyStat", mDefaultFrequencyStatSccb.checkedStringProperty());
            sessionManager.register("filter.checkedIntenseFrequency", mIntenseFrequencySccb.checkedStringProperty());
            sessionManager.register("filter.checkedIntenseFrequencyStat", mIntenseFrequencyStatSccb.checkedStringProperty());
            sessionManager.register("filter.checkedGroup", mGroupSccb.checkedStringProperty());
            sessionManager.register("filter.checkedRollingFormula", mRollingSccb.checkedStringProperty());
            sessionManager.register("filter.checkedSparseFormula", mSparseSccb.checkedStringProperty());
            sessionManager.register("filter.checkedOperators", mOperatorSccb.checkedStringProperty());
            sessionManager.register("filter.checkedOrigin", mOriginSccb.checkedStringProperty());
            sessionManager.register("filter.checkedStatus", mStatusSccb.checkedStringProperty());
            sessionManager.register("filter.checkedUnit", mUnitSccb.checkedStringProperty());
            sessionManager.register("filter.checkedUnitDiff", mUnitDiffSccb.checkedStringProperty());
            sessionManager.register("filter.measCheckedNextMeas", mMeasNextSccb.checkedStringProperty());
            sessionManager.register("filter.measCheckedMeasMode", mMeasurementModeSccb.checkedStringProperty());
            mAltitudeRangeSlider.initSession("altitude", sessionManager);
        }

        public void onShownFirstTime() {
            FxHelper.setVisibleRowCount(25,
                    mGroupSccb,
                    mCategorySccb,
                    mAlarmNameSccb,
                    mRollingSccb,
                    mSparseSccb
            );
        }

        public void reset(PropertiesConfiguration filterConfig) {
            BaseFilterPopOver.splitAndCheck(filterConfig.getString("STATUS"), mStatusSccb.getCheckModel());
            BaseFilterPopOver.splitAndCheck(filterConfig.getString("GROUP"), mGroupSccb.getCheckModel());
            BaseFilterPopOver.splitAndCheck(filterConfig.getString("CATEGORY"), mCategorySccb.getCheckModel());
            BaseFilterPopOver.splitAndCheck(filterConfig.getString("OPERATOR"), mOperatorSccb.getCheckModel());
//            BaseFilterPopOver.splitAndCheck(filterConfig.getString("UNIT"), mOperatorSccb.getCheckModel());
        }

        private void createUI() {
            FxHelper.setShowCheckedCount(true,
                    mMeasNextSccb,
                    mStatusSccb,
                    mGroupSccb,
                    mCategorySccb,
                    mAlarmNameSccb,
                    mAlarmStatSccb,
                    mOperatorSccb,
                    mOriginSccb,
                    mUnitSccb,
                    mUnitDiffSccb,
                    mMeasurementModeSccb,
                    mFrequencySccb,
                    mDefaultFrequencySccb,
                    mDefaultFrequencyStatSccb,
                    mIntenseFrequencySccb,
                    mIntenseFrequencyStatSccb,
                    mRollingSccb,
                    mSparseSccb
            );

            mMeasNextSccb.setTitle(mBundle.getString("nextMeasCheckComboBoxTitle"));
            mStatusSccb.setTitle(Dict.STATUS.toString());
            mGroupSccb.setTitle(Dict.GROUP.toString());
            mCategorySccb.setTitle(Dict.CATEGORY.toString());
            mAlarmNameSccb.setTitle(SDict.ALARMS.toString());
            mOperatorSccb.setTitle(SDict.OPERATOR.toString());
            mOriginSccb.setTitle(Dict.ORIGIN.toString());
            mUnitSccb.setTitle("Enhet");
            mUnitDiffSccb.setTitle("Enhet, diff");
            mFrequencySccb.setTitle(SDict.FREQUENCY.toString());
            mRollingSccb.setTitle("Formel, rullande");
            mSparseSccb.setTitle("Formel, utglesning");

            mDefaultFrequencySccb.setTitle("Frekvens, standard");
            mAlarmStatSccb.setTitle("Larm, status");
            mAlarmStatSccb.getItems().setAll(AlarmFlags.values());
            mDefaultFrequencyStatSccb.setTitle("Status, standard");
            mDefaultFrequencyStatSccb.getItems().setAll(DefaultFreqFlags.values());

            mIntenseFrequencySccb.setTitle("%s, %s".formatted(SDict.FREQUENCY.toString(), Dict.HIGH.toLower()));
            mIntenseFrequencyStatSccb.setTitle("%s, %s".formatted(Dict.STATUS.toString(), Dict.HIGH.toLower()));
            mIntenseFrequencyStatSccb.getItems().setAll(IntenseFreqFlags.values());
            mMeasurementModeSccb.setTitle("Mätläge");
            mMeasNextSccb.getItems().setAll(List.of(
                    "<0",
                    "0",
                    "1-6",
                    "7-14",
                    "15-28",
                    "29-182",
                    "∞"
            ));

            int rowGap = FxHelper.getUIScaled(12);
            mBaseBox = new GridPane(rowGap, rowGap);
            var leftBox = new VBox(rowGap,
                    mStatusSccb,
                    mFrequencySccb,
                    mDefaultFrequencySccb,
                    mIntenseFrequencySccb,
                    mAlarmNameSccb,
                    mGroupSccb,
                    mOriginSccb,
                    mUnitSccb,
                    mRollingSccb,
                    mAltitudeRangeSlider
            );

            var dummyLabel = new Label();
            dummyLabel.prefHeightProperty().bind(mMeasurementModeSccb.heightProperty());

            var rightBox = new VBox(rowGap,
                    mMeasurementModeSccb,
                    mMeasNextSccb,
                    mDefaultFrequencyStatSccb,
                    mIntenseFrequencyStatSccb,
                    mAlarmStatSccb,
                    mCategorySccb,
                    mOperatorSccb,
                    mUnitDiffSccb,
                    mSparseSccb
            );

            int row = 1;
            mBaseBox.addRow(row++, leftBox, rightBox);

            FxHelper.autoSizeColumn(mBaseBox, 2);
            FxHelper.bindWidthForChildrens(leftBox, rightBox);
        }
    }
}
