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

import static j2html.TagCreator.b;
import static j2html.TagCreator.body;
import static j2html.TagCreator.each;
import static j2html.TagCreator.filter;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.head;
import static j2html.TagCreator.hr;
import static j2html.TagCreator.html;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.title;
import static j2html.TagCreator.tr;
import j2html.tags.ContainerTag;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import static java.time.temporal.ChronoUnit.DAYS;
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
import org.mapton.api.ui.forms.FormFilter;
import org.mapton.api.ui.forms.FormHelper;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.controlpoint.BTopoControlPoint;
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

    private IndexedCheckModel mCategoryCheckModel;
    private IndexedCheckModel<AlarmFilter> mAlarmCheckModel;
    private IndexedCheckModel mDateFromToCheckModel;
    private final SimpleBooleanProperty mDiffMeasAllProperty = new SimpleBooleanProperty();
    private final SimpleDoubleProperty mDiffMeasAllValueProperty = new SimpleDoubleProperty();
    private final SimpleBooleanProperty mDiffMeasLatestProperty = new SimpleBooleanProperty();
    private final SimpleDoubleProperty mDiffMeasLatestValueProperty = new SimpleDoubleProperty();
    private IndexedCheckModel mDimensionCheckModel;
    private IndexedCheckModel<Integer> mFrequencyCheckModel;
    private IndexedCheckModel mGroupCheckModel;
    private final TopoManager mManager = TopoManager.getInstance();
    private final SimpleStringProperty mMaxAgeProperty = new SimpleStringProperty();
    private IndexedCheckModel<String> mMeasCodeCheckModel;
    private final SimpleBooleanProperty mMeasIncludeWithout = new SimpleBooleanProperty();
    private final SimpleBooleanProperty mMeasLatestOperator = new SimpleBooleanProperty();
    private IndexedCheckModel mMeasOperatorsCheckModel;
    private IndexedCheckModel<String> mNextMeasCheckModel;
    private final SimpleBooleanProperty mNumOfMeasProperty = new SimpleBooleanProperty();
    private final SimpleIntegerProperty mNumOfMeasValueProperty = new SimpleIntegerProperty();
    private IndexedCheckModel mOperatorCheckModel;
    private final SimpleBooleanProperty mSameAlarmProperty = new SimpleBooleanProperty();
    private IndexedCheckModel mStatusCheckModel;

    public TopoFilter() {
        super(TopoManager.getInstance());

        initListeners();
    }

    public SimpleBooleanProperty diffMeasAllProperty() {
        return mDiffMeasAllProperty;
    }

    public SimpleDoubleProperty diffMeasAllValueProperty() {
        return mDiffMeasAllValueProperty;
    }

    public SimpleBooleanProperty diffMeasLatestProperty() {
        return mDiffMeasLatestProperty;
    }

    public SimpleDoubleProperty diffMeasLatestValueProperty() {
        return mDiffMeasLatestValueProperty;
    }

    public SimpleStringProperty maxAgeProperty() {
        return mMaxAgeProperty;
    }

    public SimpleBooleanProperty measIncludeWithoutProperty() {
        return mMeasIncludeWithout;
    }

    public SimpleBooleanProperty measLatestOperatorProperty() {
        return mMeasLatestOperator;
    }

    public SimpleBooleanProperty numOfMeasProperty() {
        return mNumOfMeasProperty;
    }

    public SimpleIntegerProperty numOfMeasValueProperty() {
        return mNumOfMeasValueProperty;
    }

    public SimpleBooleanProperty sameAlarmProperty() {
        return mSameAlarmProperty;
    }

    public void setCheckModelAlarm(IndexedCheckModel<AlarmFilter> checkModel) {
        mAlarmCheckModel = checkModel;
        checkModel.getCheckedItems().addListener(mListChangeListener);
    }

    public void setCheckModelCategory(IndexedCheckModel checkModel) {
        mCategoryCheckModel = checkModel;
        checkModel.getCheckedItems().addListener(mListChangeListener);
    }

    public void setCheckModelDateFromTo(IndexedCheckModel<String> checkModel) {
        mDateFromToCheckModel = checkModel;
        checkModel.getCheckedItems().addListener(mListChangeListener);
    }

    public void setCheckModelDimension(IndexedCheckModel checkModel) {
        mDimensionCheckModel = checkModel;
        checkModel.getCheckedItems().addListener(mListChangeListener);
    }

    public void setCheckModelFrequency(IndexedCheckModel checkModel) {
        mFrequencyCheckModel = checkModel;
        checkModel.getCheckedItems().addListener(mListChangeListener);
    }

    public void setCheckModelGroup(IndexedCheckModel checkModel) {
        mGroupCheckModel = checkModel;
        checkModel.getCheckedItems().addListener(mListChangeListener);
    }

    public void setCheckModelMeasCode(IndexedCheckModel<String> checkModel) {
        mMeasCodeCheckModel = checkModel;
        checkModel.getCheckedItems().addListener(mListChangeListener);
    }

    public void setCheckModelMeasOperators(IndexedCheckModel checkModel) {
        mMeasOperatorsCheckModel = checkModel;
        checkModel.getCheckedItems().addListener(mListChangeListener);
    }

    public void setCheckModelNextMeas(IndexedCheckModel checkModel) {
        mNextMeasCheckModel = checkModel;
        checkModel.getCheckedItems().addListener(mListChangeListener);
    }

    public void setCheckModelOperator(IndexedCheckModel checkModel) {
        mOperatorCheckModel = checkModel;
        checkModel.getCheckedItems().addListener(mListChangeListener);
    }

    public void setCheckModelStatus(IndexedCheckModel checkModel) {
        mStatusCheckModel = checkModel;
        checkModel.getCheckedItems().addListener(mListChangeListener);
    }

    @Override
    public void update() {
        var filteredItems = mManager.getAllItems().stream()
                .filter(p -> StringUtils.isBlank(getFreeText()) || validateFreeText(p))
                .filter(p -> validateDimension(p.getDimension()))
                .filter(p -> validateStatus(p.getStatus()))
                .filter(p -> validateGroup(p.getGroup()))
                .filter(p -> validateCategory(p.getCategory()))
                .filter(p -> validateAlarm(p))
                .filter(p -> validateMeasDisplacementAll(p))
                .filter(p -> validateMeasDisplacementLatest(p))
                .filter(p -> validateMeasCount(p))
                .filter(p -> validateOperator(p.getOperator()))
                .filter(p -> validateFrequency(p.getFrequency()))
                .filter(p -> validateMaxAge(p.getDateLatest()))
                .filter(p -> validateNextMeas(p))
                .filter(p -> validateMeasWithout(p))
                .filter(p -> validateMeasCode(p))
                .filter(p -> validateMeasOperators(p))
                .filter(p -> validateDateFromToHas(p.getDateValidFrom(), p.getDateValidTo()))
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

        mManager.getFilteredItems().setAll(filteredItems);

        getInfoPopOver().loadContent(createInfoContent().renderFormatted());
    }

    private ContainerTag createInfoContent() {
        //TODO Add measOperator+latest
        var map = new LinkedHashMap<String, String>();

        map.put(Dict.TEXT.toString(), getFreeText());
        map.put(SDict.DIMENSION.toString(), makeInfoDimension(mDimensionCheckModel.getCheckedItems()));
        map.put(Dict.STATUS.toString(), makeInfo(mStatusCheckModel.getCheckedItems()));
        map.put(Dict.GROUP.toString(), makeInfo(mGroupCheckModel.getCheckedItems()));
        map.put(Dict.CATEGORY.toString(), makeInfo(mCategoryCheckModel.getCheckedItems()));
        map.put(SDict.OPERATOR.toString(), makeInfo(mOperatorCheckModel.getCheckedItems()));
        map.put(SDict.FREQUENCY.toString(), makeInfoInteger(mFrequencyCheckModel.getCheckedItems()));
        map.put(SDict.VALID_FROM_TO.toString(), makeInfo(mDateFromToCheckModel.getCheckedItems()));
        map.put(getBundle().getString("nextMeasCheckComboBoxTitle"), makeInfo(mNextMeasCheckModel.getCheckedItems()));
        map.put(getBundle().getString("measCodeCheckComboBoxTitle"), makeInfo(mMeasCodeCheckModel.getCheckedItems()));
        map.put(Dict.Time.MAX_AGE.toString(), makeInfo(mMaxAgeProperty.get(), "*"));

        if (mNumOfMeasProperty.get()) {
            var value = mNumOfMeasValueProperty.get();
            map.put(getBundle().getString("numOfMeasCheckBoxText"), FormHelper.negPosToLtGt(value));
        }

        if (mDiffMeasAllProperty.get()) {
            map.put(getBundle().getString("diffMeasAllCheckBoxText"), FormHelper.negPosToLtGt(mDiffMeasAllValueProperty.get()));
        }

        if (mDiffMeasLatestProperty.get()) {
            map.put(getBundle().getString("diffMeasLatestCheckBoxText"), FormHelper.negPosToLtGt(mDiffMeasLatestValueProperty.get()));
        }

        if (mSameAlarmProperty.get()) {
            map.put(getBundle().getString("sameAlarmCheckBoxText"), BooleanHelper.asYesNo(mSameAlarmProperty.get()));
        }

        var html = html(
                head(
                        title(Dict.FILTER.toString())
                ),
                body(
                        h1(Dict.FILTER.toString()),
                        hr(),
                        table(
                                tbody(
                                        each(filter(map.entrySet(), entry -> StringUtils.isNotBlank(entry.getValue())), entry
                                                -> tr(
                                                td(entry.getKey()),
                                                td(b(entry.getValue()))
                                        )
                                        )
                                )
                        ),
                        hr()//Temp last line
                ));

        return html;
    }

    private void initListeners() {
        mMeasIncludeWithout.addListener(mChangeListenerObject);
        mMeasLatestOperator.addListener(mChangeListenerObject);
        mDiffMeasLatestProperty.addListener(mChangeListenerObject);
        mDiffMeasLatestValueProperty.addListener(mChangeListenerObject);
        mDiffMeasAllProperty.addListener(mChangeListenerObject);
        mDiffMeasAllValueProperty.addListener(mChangeListenerObject);
        mNumOfMeasValueProperty.addListener(mChangeListenerObject);
        mNumOfMeasProperty.addListener(mChangeListenerObject);
        mSameAlarmProperty.addListener(mChangeListenerObject);
        mMaxAgeProperty.addListener(mChangeListenerObject);
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

    private boolean validateCategory(String s) {
        return validateCheck(mCategoryCheckModel, s);
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

    private boolean validateGroup(String s) {
        return validateCheck(mGroupCheckModel, s);
    }

    private boolean validateMaxAge(LocalDateTime dateTime) {
        var ageFilter = mMaxAgeProperty.get();

        if (ageFilter.equalsIgnoreCase("*")) {
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
        var valid = mMeasCodeCheckModel.isEmpty()
                || mMeasCodeCheckModel.isChecked(getBundle().getString("measZeroCount")) && p.ext().getObservationsRaw().stream().filter(oo -> oo.isZeroMeasurement()).count() > 1
                || mMeasCodeCheckModel.isChecked(getBundle().getString("measReplacementCount")) && p.ext().getObservationsRaw().stream().filter(oo -> oo.isReplacementMeasurement()).count() > 0;

        return valid;
    }

    private boolean validateMeasCount(BTopoControlPoint p) {
        if (!mNumOfMeasProperty.get()) {
            return true;
        }

        var lim = mNumOfMeasValueProperty.get();
        var value = p.ext().getObservationsRaw().size();

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
        if (mDiffMeasAllProperty.get() && p.ext().deltaZero().getDelta() != null) {
            double lim = mDiffMeasAllValueProperty.get();
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
        if (!mDiffMeasLatestProperty.get()) {
            return true;
        }

        var observations = p.ext().getObservationsFiltered();
        if (observations.size() > 1) {
            var first = observations.get(observations.size() - 2);
            var last = observations.get(observations.size() - 1);
            double lim = mDiffMeasLatestValueProperty.get();
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
            return mMeasOperatorsCheckModel.getCheckedItems().contains(p.ext().getObservationsRaw().getLast().getOperator());
        } else {
            var pointOperators = p.ext().getObservationsRaw().stream().map(o -> o.getOperator()).collect(Collectors.toSet());

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

        if (mNextMeasCheckModel.isEmpty()) {
            return true;
        } else if (mNextMeasCheckModel.isChecked("∞") && frequency == 0) {
            return true;
        } else if (frequency > 0 && mNextMeasCheckModel.isChecked("<0") && nextMeas.isBefore(today)) {
            return true;
        } else {
            return mNextMeasCheckModel.getCheckedItems().stream()
                    .filter(s -> StringUtils.countMatches(s, "-") == 1)
                    .anyMatch(s -> {
                        int start = Integer.parseInt(StringUtils.substringBefore(s, "-"));
                        int end = Integer.parseInt(StringUtils.substringAfter(s, "-"));
                        return remainingDays >= start && remainingDays <= end;
                    });
        }
    }

    private boolean validateOperator(String s) {
        return validateCheck(mOperatorCheckModel, s);
    }

    private boolean validateStatus(String s) {
        return validateCheck(mStatusCheckModel, s);
    }

}
