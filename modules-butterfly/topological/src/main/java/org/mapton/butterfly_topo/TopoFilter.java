/*
 * Copyright 2023 Patrik Karlström.
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
package org.mapton.butterfly_topo;

import j2html.tags.ContainerTag;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import static java.time.temporal.ChronoUnit.DAYS;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.MTemporalManager;
import org.mapton.api.ui.forms.FormFilter;
import org.mapton.api.ui.forms.FormHelper;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.api.TopoManager;
import org.mapton.butterfly_topo.shared.AlarmFilter;
import se.trixon.almond.util.BooleanHelper;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.StringHelper;

/**
 *
 * @author Patrik Karlström
 */
public class TopoFilter extends FormFilter<TopoManager> {

    IndexedCheckModel mAlarmNameCheckModel;
    IndexedCheckModel mCategoryCheckModel;
    IndexedCheckModel mDateFromToCheckModel;
    IndexedCheckModel mDimensionCheckModel;
    IndexedCheckModel<Integer> mFrequencyCheckModel;
    IndexedCheckModel mGroupCheckModel;
    IndexedCheckModel<String> mMeasCodeCheckModel;
    IndexedCheckModel<String> mMeasNextCheckModel;
    IndexedCheckModel mMeasOperatorsCheckModel;
    IndexedCheckModel mOperatorCheckModel;
    IndexedCheckModel mStatusCheckModel;
    IndexedCheckModel<AlarmFilter> mAlarmCheckModel;
    private final SimpleBooleanProperty mAlarmLevelChangeProperty = new SimpleBooleanProperty();
    private final SimpleBooleanProperty mInvertProperty = new SimpleBooleanProperty();
    private final TopoManager mManager = TopoManager.getInstance();
    private final SimpleStringProperty mMaxAgeProperty = new SimpleStringProperty();
    private final SimpleBooleanProperty mMeasDiffAllProperty = new SimpleBooleanProperty();
    private final SimpleDoubleProperty mMeasDiffAllValueProperty = new SimpleDoubleProperty();
    private final SimpleBooleanProperty mMeasDiffLatestProperty = new SimpleBooleanProperty();
    private final SimpleDoubleProperty mMeasDiffLatestValueProperty = new SimpleDoubleProperty();
    private final SimpleBooleanProperty mMeasIncludeWithout = new SimpleBooleanProperty();
    private final SimpleBooleanProperty mMeasLatestOperator = new SimpleBooleanProperty();
    private final SimpleBooleanProperty mMeasNumOfProperty = new SimpleBooleanProperty();
    private final SimpleIntegerProperty mMeasNumOfValueProperty = new SimpleIntegerProperty();
    private final SimpleBooleanProperty mSameAlarmProperty = new SimpleBooleanProperty();

    public TopoFilter() {
        super(TopoManager.getInstance());

        initListeners();
    }

    public SimpleBooleanProperty alarmLevelChangeProperty() {
        return mAlarmLevelChangeProperty;
    }

    public SimpleBooleanProperty invertProperty() {
        return mInvertProperty;
    }

    public SimpleStringProperty maxAgeProperty() {
        return mMaxAgeProperty;
    }

    public SimpleBooleanProperty measDiffAllProperty() {
        return mMeasDiffAllProperty;
    }

    public SimpleDoubleProperty measDiffAllValueProperty() {
        return mMeasDiffAllValueProperty;
    }

    public SimpleBooleanProperty measDiffLatestProperty() {
        return mMeasDiffLatestProperty;
    }

    public SimpleDoubleProperty measDiffLatestValueProperty() {
        return mMeasDiffLatestValueProperty;
    }

    public SimpleBooleanProperty measIncludeWithoutProperty() {
        return mMeasIncludeWithout;
    }

    public SimpleBooleanProperty measLatestOperatorProperty() {
        return mMeasLatestOperator;
    }

    public SimpleBooleanProperty measNumOfProperty() {
        return mMeasNumOfProperty;
    }

    public SimpleIntegerProperty measNumOfValueProperty() {
        return mMeasNumOfValueProperty;
    }

    public SimpleBooleanProperty sameAlarmProperty() {
        return mSameAlarmProperty;
    }

    @Override
    public void update() {
        var filteredItems = mManager.getAllItems().stream()
                .filter(p -> StringUtils.isBlank(getFreeText()) || validateFreeText(p))
                .filter(p -> validateDimension(p.getDimension()))
                .filter(p -> validateCheck(mStatusCheckModel, p.getStatus()))
                .filter(p -> validateCheck(mGroupCheckModel, p.getGroup()))
                .filter(p -> validateCheck(mCategoryCheckModel, p.getCategory()))
                .filter(p -> validateAlarmName(p))
                .filter(p -> validateAlarm(p))
                .filter(p -> validateAlarmLevelchange(p))
                .filter(p -> validateMeasDisplacementAll(p))
                .filter(p -> validateMeasDisplacementLatest(p))
                .filter(p -> validateMeasCount(p))
                .filter(p -> validateCheck(mOperatorCheckModel, p.getOperator()))
                .filter(p -> validateFrequency(p.getFrequency()))
                .filter(p -> validateMaxAge(p.getDateLatest()))
                .filter(p -> validateNextMeas(p))
                .filter(p -> validateMeasWithout(p))
                .filter(p -> validateMeasCode(p))
                .filter(p -> validateMeasOperators(p))
                .filter(p -> validateDateFromToHas(p.getDateValidFrom(), p.getDateValidTo()))
                .filter(p -> validateDateFromToWithout(p.getDateValidFrom(), p.getDateValidTo()))
                .filter(p -> validateDateFromToIs(p.getDateValidFrom(), p.getDateValidTo()))
                .filter(p -> validateCoordinateArea(p.getLat(), p.getLon()))
                .filter(p -> validateCoordinateRuler(p.getLat(), p.getLon()))
                .toList();

        if (mSameAlarmProperty.get()) {
            var hAlarms = filteredItems.stream().map(o -> o.getNameOfAlarmHeight()).collect(Collectors.toSet());
            var pAlarms = filteredItems.stream().map(o -> o.getNameOfAlarmPlane()).collect(Collectors.toSet());

            filteredItems = mManager.getAllItems().stream()
                    .filter(o -> {
                        String hAlarm = o.getNameOfAlarmHeight();
                        String pAlarm = o.getNameOfAlarmPlane();

                        var validH = StringUtils.isNotBlank(hAlarm) && hAlarms.contains(hAlarm);
                        var validP = StringUtils.isNotBlank(pAlarm) && pAlarms.contains(pAlarm);

                        return validH || validP;
                    })
                    .toList();
        }

        if (mInvertProperty.get()) {
            var toBeExluded = new HashSet<>(filteredItems);
            filteredItems = mManager.getAllItems().stream()
                    .filter(p -> !toBeExluded.contains(p))
                    .toList();
        }

        mManager.getFilteredItems().setAll(filteredItems);

        getInfoPopOver().loadContent(createInfoContent().renderFormatted());
    }

    void initCheckModelListeners() {
        mStatusCheckModel.getCheckedItems().addListener(mListChangeListener);
        mAlarmCheckModel.getCheckedItems().addListener(mListChangeListener);
        mAlarmNameCheckModel.getCheckedItems().addListener(mListChangeListener);
        mCategoryCheckModel.getCheckedItems().addListener(mListChangeListener);
        mDateFromToCheckModel.getCheckedItems().addListener(mListChangeListener);
        mDimensionCheckModel.getCheckedItems().addListener(mListChangeListener);
        mFrequencyCheckModel.getCheckedItems().addListener(mListChangeListener);
        mGroupCheckModel.getCheckedItems().addListener(mListChangeListener);
        mMeasCodeCheckModel.getCheckedItems().addListener(mListChangeListener);
        mMeasOperatorsCheckModel.getCheckedItems().addListener(mListChangeListener);
        mMeasNextCheckModel.getCheckedItems().addListener(mListChangeListener);
        mOperatorCheckModel.getCheckedItems().addListener(mListChangeListener);
        mStatusCheckModel.getCheckedItems().addListener(mListChangeListener);
    }

    private ContainerTag createInfoContent() {
        //TODO Add measOperator+latest
        var map = new LinkedHashMap<String, String>();

        map.put(Dict.TEXT.toString(), getFreeText());
        map.put(SDict.DIMENSION.toString(), makeInfoDimension(mDimensionCheckModel.getCheckedItems()));
        map.put(Dict.STATUS.toString(), makeInfo(mStatusCheckModel.getCheckedItems()));
        map.put(Dict.GROUP.toString(), makeInfo(mGroupCheckModel.getCheckedItems()));
        map.put(Dict.CATEGORY.toString(), makeInfo(mCategoryCheckModel.getCheckedItems()));
        map.put(SDict.ALARMS.toString(), makeInfo(mAlarmNameCheckModel.getCheckedItems()));
        map.put(SDict.OPERATOR.toString(), makeInfo(mOperatorCheckModel.getCheckedItems()));
        map.put(SDict.FREQUENCY.toString(), makeInfoInteger(mFrequencyCheckModel.getCheckedItems()));
        map.put(SDict.VALID_FROM_TO.toString(), makeInfo(mDateFromToCheckModel.getCheckedItems()));
        map.put(getBundle().getString("nextMeasCheckComboBoxTitle"), makeInfo(mMeasNextCheckModel.getCheckedItems()));
        map.put(getBundle().getString("measCodeCheckComboBoxTitle"), makeInfo(mMeasCodeCheckModel.getCheckedItems()));
        map.put(Dict.Time.MAX_AGE.toString(), makeInfo(mMaxAgeProperty.get(), "*"));

        if (mMeasNumOfProperty.get()) {
            var value = mMeasNumOfValueProperty.get();
            map.put(getBundle().getString("numOfMeasCheckBoxText"), FormHelper.negPosToLtGt(value));
        }

        if (mMeasDiffAllProperty.get()) {
            map.put(getBundle().getString("diffMeasAllCheckBoxText"), FormHelper.negPosToLtGt(mMeasDiffAllValueProperty.get()));
        }

        if (mMeasDiffLatestProperty.get()) {
            map.put(getBundle().getString("diffMeasLatestCheckBoxText"), FormHelper.negPosToLtGt(mMeasDiffLatestValueProperty.get()));
        }

        if (mSameAlarmProperty.get()) {
            map.put(getBundle().getString("sameAlarmCheckBoxText"), BooleanHelper.asYesNo(mSameAlarmProperty.get()));
        }

        return createHtmlFilterInfo(map);
    }

    private void initListeners() {
        mAlarmLevelChangeProperty.addListener(mChangeListenerObject);
        mInvertProperty.addListener(mChangeListenerObject);
        mMaxAgeProperty.addListener(mChangeListenerObject);
        mMeasDiffAllProperty.addListener(mChangeListenerObject);
        mMeasDiffAllValueProperty.addListener(mChangeListenerObject);
        mMeasDiffLatestProperty.addListener(mChangeListenerObject);
        mMeasDiffLatestValueProperty.addListener(mChangeListenerObject);
        mMeasIncludeWithout.addListener(mChangeListenerObject);
        mMeasLatestOperator.addListener(mChangeListenerObject);
        mMeasNumOfProperty.addListener(mChangeListenerObject);
        mMeasNumOfValueProperty.addListener(mChangeListenerObject);
        mSameAlarmProperty.addListener(mChangeListenerObject);
    }

    private String makeInfoDimension(ObservableList<BDimension> checkedItems) {
        return String.join(", ", checkedItems.stream().map(d -> d.getName()).toList());
    }

    private boolean validateAlarm(BTopoControlPoint p) {
        if (mAlarmCheckModel.isEmpty()) {
            return true;
        }
        var levelH = TopoHelper.getAlarmLevelHeight(p);
        var levelP = TopoHelper.getAlarmLevelPlane(p);

        for (var alarmFilter : AlarmFilter.values()) {
            var validH = mAlarmCheckModel.isChecked(alarmFilter) && alarmFilter.getComponent() == BComponent.HEIGHT && alarmFilter.getLevel() == levelH;
            var validP = mAlarmCheckModel.isChecked(alarmFilter) && alarmFilter.getComponent() == BComponent.PLANE && alarmFilter.getLevel() == levelP;
            if (validH || validP) {
                return true;
            }
        }

        return false;
    }

    private boolean validateAlarmLevelchange(BTopoControlPoint p) {
        if (!mAlarmLevelChangeProperty.get()) {
            return true;
        }

        var hLevels = new HashSet<Integer>();
        var pLevels = new HashSet<Integer>();

        p.ext().getObservationsAllCalculated().stream()
                .filter(o -> MTemporalManager.getInstance().isValid(o.getDate()))
                .forEachOrdered(o -> {
                    hLevels.add(p.ext().getAlarmLevelHeight(o));
                    pLevels.add(p.ext().getAlarmLevelPlane(o));
                });

        return hLevels.size() > 1 || pLevels.size() > 1;
    }

    private boolean validateAlarmName(BTopoControlPoint p) {
        var ah = p.getNameOfAlarmHeight();
        var ap = p.getNameOfAlarmPlane();

        switch (p.getDimension()) {
            case _1d -> {
                return validateCheck(mAlarmNameCheckModel, ah);
            }
            case _2d -> {
                return validateCheck(mAlarmNameCheckModel, ap);
            }
            case _3d -> {
                return validateCheck(mAlarmNameCheckModel, ah) && validateCheck(mAlarmNameCheckModel, ap);
            }
        }

        return true;
    }

    private boolean validateDateFromToHas(LocalDate fromDate, LocalDate toDate) {
        var validFromChecked = mDateFromToCheckModel.isChecked(SDict.HAS_VALID_FROM.toString());
        var validToChecked = mDateFromToCheckModel.isChecked(SDict.HAS_VALID_TO.toString());
        var valid = (!validFromChecked && !validToChecked)
                || (fromDate != null && validFromChecked)
                || (toDate != null && validToChecked);

        return valid;
    }

    private boolean validateDateFromToIs(LocalDate fromDate, LocalDate toDate) {
        var now = LocalDate.now();
        var validChecked = mDateFromToCheckModel.isChecked(SDict.IS_VALID.toString());
        var invalidChecked = mDateFromToCheckModel.isChecked(SDict.IS_INVALID.toString());

        if (validChecked && invalidChecked) {
            return false;
        } else if (!validChecked && !invalidChecked) {
            return true;
        }

        if (validChecked) {
            var validFromDate = fromDate == null ? false : DateHelper.isAfterOrEqual(now, fromDate);
            var validToDate = toDate == null ? false : DateHelper.isBeforeOrEqual(now, toDate);
            return validFromDate || validToDate;
        } else {//invalidChecked
            var invalidFromDate = fromDate == null ? true : DateHelper.isAfterOrEqual(now, fromDate);
            var invalidToDate = toDate == null ? true : DateHelper.isBeforeOrEqual(now, toDate);
            return !invalidFromDate || !invalidToDate;
        }
    }

    private boolean validateDateFromToWithout(LocalDate fromDate, LocalDate toDate) {
        var validFromChecked = mDateFromToCheckModel.isChecked(SDict.WITHOUT_VALID_FROM.toString());
        var validToChecked = mDateFromToCheckModel.isChecked(SDict.WITHOUT_VALID_TO.toString());
        var valid = (!validFromChecked && !validToChecked)
                || (fromDate == null && validFromChecked)
                || (toDate == null && validToChecked);

        return valid;
    }

    private boolean validateDimension(BDimension dimension) {
        return validateCheck(mDimensionCheckModel, dimension);
    }

    private boolean validateFreeText(BTopoControlPoint o) {
        return StringHelper.matchesSimpleGlobByWord(getFreeText(), true, false,
                o.getName(),
                o.getCategory(),
                o.getGroup(),
                o.getNameOfAlarmHeight(),
                o.getNameOfAlarmPlane()
        );
    }

    private boolean validateFrequency(Integer frequency) {
        return validateCheck(mFrequencyCheckModel, frequency);
    }

    private boolean validateMaxAge(LocalDateTime dateTime) {
        var ageFilter = mMaxAgeProperty.get();

        if (ageFilter == null || ageFilter.equalsIgnoreCase("*")) {
            return true;
        }

        if (dateTime == null) {
            return ageFilter.equalsIgnoreCase("NODATA");
        } else {
            if (ageFilter.equalsIgnoreCase("∞")) {
                return true;
            } else if (ageFilter.equalsIgnoreCase("NODATA")) {
                return false;
            }

            long daysBetween = DAYS.between(dateTime, LocalDateTime.now());
            boolean valid = daysBetween < Integer.parseInt(ageFilter);
            return valid;
        }
    }

    private boolean validateMeasCode(BTopoControlPoint p) {
        var firstNotZero = !p.ext().getObservationRawFirstDate().equals(p.getDateZero());
        var valid = mMeasCodeCheckModel.isEmpty()
                || mMeasCodeCheckModel.isChecked(getBundle().getString("measCodeZero")) && firstNotZero
                || mMeasCodeCheckModel.isChecked(getBundle().getString("measCodeReplacement")) && p.ext().getObservationsAllRaw().stream().filter(oo -> oo.isReplacementMeasurement()).count() > 0;

        return valid;
    }

    private boolean validateMeasCount(BTopoControlPoint p) {
        if (!mMeasNumOfProperty.get()) {
            return true;
        }

        var lim = mMeasNumOfValueProperty.get();
        var value = p.ext().getObservationsAllRaw().size();

        if (lim == 0) {
            return value == 0;
        } else if (lim < 0) {
            return value <= Math.abs(lim) && value != 0;
        } else if (lim > 0) {
            return value >= lim;
        }

        return true;
    }

    private boolean validateMeasDisplacementAll(BTopoControlPoint p) {
        if (mMeasDiffAllProperty.get() && p.ext().deltaZero().getDelta() != null) {
            double lim = mMeasDiffAllValueProperty.get();
            double value = Math.abs(p.ext().deltaZero().getDelta());

            if (lim == 0) {
                return value == 0;
            } else if (lim < 0) {
                return value <= Math.abs(lim);
            } else {
                return value >= lim;
            }
        } else {
            return true;
        }
    }

    private boolean validateMeasDisplacementLatest(BTopoControlPoint p) {
        if (!mMeasDiffLatestProperty.get()) {
            return true;
        }

        var observations = p.ext().getObservationsTimeFiltered();
        if (observations.size() > 1) {
            var first = observations.get(observations.size() - 2);
            var last = observations.get(observations.size() - 1);
            double lim = mMeasDiffLatestValueProperty.get();
            Double lastDelta = last.ext().getDelta();
            Double firstDelta = first.ext().getDelta();
            if (ObjectUtils.anyNull(firstDelta, lastDelta)) {
                return false;
            }
            double value = Math.abs(lastDelta - firstDelta);

            if (lim == 0) {
                return value == 0;
            } else if (lim < 0) {
                return value <= Math.abs(lim);
            } else {
                return value >= lim;
            }
        } else {
            return false;
        }
    }

    private boolean validateMeasOperators(BTopoControlPoint p) {
        if (mMeasOperatorsCheckModel.isEmpty()) {
            return true;
        }

        if (mMeasLatestOperator.get()) {
            return mMeasOperatorsCheckModel.getCheckedItems().contains(p.ext().getObservationsAllRaw().getLast().getOperator());
        } else {
            var pointOperators = p.ext().getObservationsAllRaw().stream().map(o -> o.getOperator()).collect(Collectors.toSet());

            for (var operator : mMeasOperatorsCheckModel.getCheckedItems()) {
                if (pointOperators.contains(operator)) {
                    return true;
                }
            }

            return false;
        }
    }

    private boolean validateMeasWithout(BTopoControlPoint p) {
        var valid = mMeasIncludeWithout.get()
                || p.ext().getNumOfObservations() > 0;

        return valid;
    }

    private boolean validateNextMeas(BTopoControlPoint p) {
        //TODO Verify exact days...

        var frequency = p.getFrequency();
        var latest = p.getDateLatest() != null ? p.getDateLatest().toLocalDate() : LocalDate.MIN;
        var today = LocalDate.now();
        var nextMeas = latest.plusDays(frequency);
        var remainingDays = p.ext().getMeasurementUntilNext(ChronoUnit.DAYS);

        if (mMeasNextCheckModel.isEmpty()) {
            return true;
        } else if (mMeasNextCheckModel.isChecked("∞") && frequency == 0) {
            return true;
        } else if (frequency > 0 && mMeasNextCheckModel.isChecked("<0") && nextMeas.isBefore(today)) {
            return true;
        } else {
            return mMeasNextCheckModel.getCheckedItems().stream()
                    .filter(s -> StringUtils.countMatches(s, "-") == 1)
                    .anyMatch(s -> {
                        int start = Integer.parseInt(StringUtils.substringBefore(s, "-"));
                        int end = Integer.parseInt(StringUtils.substringAfter(s, "-"));
                        return remainingDays >= start && remainingDays <= end;
                    });
        }
    }
}
