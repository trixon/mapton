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
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javax.swing.SortOrder;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.MTemporalManager;
import org.mapton.api.ui.forms.FormFilter;
import org.mapton.api.ui.forms.FormHelper;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_format.types.topo.BTopoControlPointObservation;
import org.mapton.butterfly_topo.api.TopoManager;
import org.mapton.butterfly_topo.shared.AlarmLevelChangeMode;
import org.mapton.butterfly_topo.shared.AlarmLevelChangeUnit;
import org.mapton.butterfly_topo.shared.AlarmLevelFilter;
import se.trixon.almond.util.BooleanHelper;
import se.trixon.almond.util.CollectionHelper;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
public class TopoFilter extends FormFilter<TopoManager> {

    IndexedCheckModel<AlarmLevelFilter> mAlarmLevelCheckModel;
    IndexedCheckModel mAlarmNameCheckModel;
    IndexedCheckModel mCategoryCheckModel;
    IndexedCheckModel mGroupCheckModel;
    IndexedCheckModel<String> mMeasCodeCheckModel;
    IndexedCheckModel<String> mMeasNextCheckModel;
    IndexedCheckModel<String> mMeasOperatorsCheckModel;
    IndexedCheckModel mOperatorCheckModel;
    IndexedCheckModel mOriginCheckModel;
    IndexedCheckModel mStatusCheckModel;
    private final SimpleBooleanProperty mDimens1Property = new SimpleBooleanProperty();
    private final SimpleBooleanProperty mDimens2Property = new SimpleBooleanProperty();
    private final SimpleBooleanProperty mDimens3Property = new SimpleBooleanProperty();
    private final SimpleBooleanProperty mInvertProperty = new SimpleBooleanProperty();
    private final TopoManager mManager = TopoManager.getInstance();
    private final SimpleBooleanProperty mMeasAlarmLevelAgeProperty = new SimpleBooleanProperty();
    private final SimpleIntegerProperty mMeasAlarmLevelAgeValueProperty = new SimpleIntegerProperty();
    private final SimpleIntegerProperty mMeasAlarmLevelChangeLimitProperty = new SimpleIntegerProperty();
    private final SimpleObjectProperty mMeasAlarmLevelChangeModeProperty = new SimpleObjectProperty();
    private final SimpleBooleanProperty mMeasAlarmLevelChangeProperty = new SimpleBooleanProperty();
    private final SimpleObjectProperty mMeasAlarmLevelChangeUnitProperty = new SimpleObjectProperty();
    private final SimpleIntegerProperty mMeasAlarmLevelChangeValueProperty = new SimpleIntegerProperty();
    private final SimpleObjectProperty<LocalDate> mMeasDateFirstHighProperty = new SimpleObjectProperty();
    private final SimpleObjectProperty<LocalDate> mMeasDateFirstLowProperty = new SimpleObjectProperty();
    private final SimpleObjectProperty<LocalDate> mMeasDateLastHighProperty = new SimpleObjectProperty();
    private final SimpleObjectProperty<LocalDate> mMeasDateLastLowProperty = new SimpleObjectProperty();
    private final SimpleBooleanProperty mMeasDiffAllProperty = new SimpleBooleanProperty();
    private final SimpleDoubleProperty mMeasDiffAllValueProperty = new SimpleDoubleProperty();
    private final SimpleBooleanProperty mMeasDiffLatestProperty = new SimpleBooleanProperty();
    private final SimpleDoubleProperty mMeasDiffLatestValueProperty = new SimpleDoubleProperty();
    private final SimpleBooleanProperty mMeasDiffPercentageHProperty = new SimpleBooleanProperty();
    private final SimpleIntegerProperty mMeasDiffPercentageHValueProperty = new SimpleIntegerProperty();
    private final SimpleBooleanProperty mMeasDiffPercentagePProperty = new SimpleBooleanProperty();
    private final SimpleIntegerProperty mMeasDiffPercentagePValueProperty = new SimpleIntegerProperty();
    private final SimpleBooleanProperty mMeasIncludeWithout = new SimpleBooleanProperty();
    private final SimpleBooleanProperty mMeasLatestOperator = new SimpleBooleanProperty();
    private final SimpleBooleanProperty mMeasNumOfProperty = new SimpleBooleanProperty();
    private final SimpleIntegerProperty mMeasNumOfValueProperty = new SimpleIntegerProperty();
    private final SimpleBooleanProperty mMeasSpeedProperty = new SimpleBooleanProperty();
    private final SimpleDoubleProperty mMeasSpeedValueProperty = new SimpleDoubleProperty();
    private final SimpleIntegerProperty mMeasTopListLimitProperty = new SimpleIntegerProperty();
    private final SimpleBooleanProperty mMeasTopListProperty = new SimpleBooleanProperty();
    private final SimpleIntegerProperty mMeasTopListSizeValueProperty = new SimpleIntegerProperty();
    private final SimpleObjectProperty<AlarmLevelChangeUnit> mMeasTopListUnitProperty = new SimpleObjectProperty();
    private final SimpleDoubleProperty mMeasYoyoCountValueProperty = new SimpleDoubleProperty();
    private final SimpleBooleanProperty mMeasYoyoProperty = new SimpleBooleanProperty();
    private final SimpleDoubleProperty mMeasYoyoSizeValueProperty = new SimpleDoubleProperty();
    private final SimpleBooleanProperty mSameAlarmProperty = new SimpleBooleanProperty();

    public TopoFilter() {
        super(TopoManager.getInstance());

        initListeners();
    }

    public SimpleBooleanProperty dimens1Property() {
        return mDimens1Property;
    }

    public SimpleBooleanProperty dimens2Property() {
        return mDimens2Property;
    }

    public SimpleBooleanProperty dimens3Property() {
        return mDimens3Property;
    }

    public SimpleBooleanProperty invertProperty() {
        return mInvertProperty;
    }

    public SimpleBooleanProperty measAlarmLevelAgeProperty() {
        return mMeasAlarmLevelAgeProperty;
    }

    public SimpleIntegerProperty measAlarmLevelAgeValueProperty() {
        return mMeasAlarmLevelAgeValueProperty;
    }

    public SimpleIntegerProperty measAlarmLevelChangeLimitProperty() {
        return mMeasAlarmLevelChangeLimitProperty;
    }

    public SimpleObjectProperty measAlarmLevelChangeModeProperty() {
        return mMeasAlarmLevelChangeModeProperty;
    }

    public SimpleBooleanProperty measAlarmLevelChangeProperty() {
        return mMeasAlarmLevelChangeProperty;
    }

    public SimpleObjectProperty measAlarmLevelChangeUnitProperty() {
        return mMeasAlarmLevelChangeUnitProperty;
    }

    public SimpleIntegerProperty measAlarmLevelChangeValueProperty() {
        return mMeasAlarmLevelChangeValueProperty;
    }

    public SimpleObjectProperty<LocalDate> measDateFirstHighProperty() {
        return mMeasDateFirstHighProperty;
    }

    public SimpleObjectProperty<LocalDate> measDateFirstLowProperty() {
        return mMeasDateFirstLowProperty;
    }

    public SimpleObjectProperty<LocalDate> measDateLastHighProperty() {
        return mMeasDateLastHighProperty;
    }

    public SimpleObjectProperty<LocalDate> measDateLastLowProperty() {
        return mMeasDateLastLowProperty;
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

    public SimpleBooleanProperty measDiffPercentageHProperty() {
        return mMeasDiffPercentageHProperty;
    }

    public SimpleIntegerProperty measDiffPercentageHValueProperty() {
        return mMeasDiffPercentageHValueProperty;
    }

    public SimpleBooleanProperty measDiffPercentagePProperty() {
        return mMeasDiffPercentagePProperty;
    }

    public SimpleIntegerProperty measDiffPercentagePValueProperty() {
        return mMeasDiffPercentagePValueProperty;
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

    public SimpleBooleanProperty measSpeedProperty() {
        return mMeasSpeedProperty;
    }

    public SimpleDoubleProperty measSpeedValueProperty() {
        return mMeasSpeedValueProperty;
    }

    public SimpleIntegerProperty measTopListLimitProperty() {
        return mMeasTopListLimitProperty;
    }

    public SimpleBooleanProperty measTopListProperty() {
        return mMeasTopListProperty;
    }

    public SimpleIntegerProperty measTopListSizeValueProperty() {
        return mMeasTopListSizeValueProperty;
    }

    public SimpleObjectProperty measTopListUnitProperty() {
        return mMeasTopListUnitProperty;
    }

    public SimpleDoubleProperty measYoyoCountValueProperty() {
        return mMeasYoyoCountValueProperty;
    }

    public SimpleBooleanProperty measYoyoProperty() {
        return mMeasYoyoProperty;
    }

    public SimpleDoubleProperty measYoyoSizeValueProperty() {
        return mMeasYoyoSizeValueProperty;
    }

    public SimpleBooleanProperty sameAlarmProperty() {
        return mSameAlarmProperty;
    }

    @Override
    public void update() {
        var filteredItems = mManager.getAllItems().stream()
                .filter(p -> validateDimension(p.getDimension()))
                .filter(p -> validateCheck(mStatusCheckModel, p.getStatus()))
                .filter(p -> validateCheck(mGroupCheckModel, p.getGroup()))
                .filter(p -> validateCheck(mCategoryCheckModel, p.getCategory()))
                .filter(p -> validateAlarmName(p))
                .filter(p -> validateFrequency(p.getFrequency()))
                .filter(p -> validateCheck(mOperatorCheckModel, p.getOperator()))
                .filter(p -> validateCheck(mOriginCheckModel, p.getOrigin()))
                .filter(p -> {
                    var alarmH = p.ext().getAlarm(BComponent.HEIGHT);
                    var alarmP = p.ext().getAlarm(BComponent.PLANE);

                    var nameH = alarmH == null ? "" : alarmH.getName();
                    var nameP = alarmP == null ? "" : alarmP.getName();

                    return validateFreeText(p.getName(), p.getCategory(), p.getGroup(), p.getNameOfAlarmHeight(), p.getNameOfAlarmPlane(), nameH, nameP);
                })
                .filter(p -> validateCoordinateArea(p.getLat(), p.getLon()))
                .filter(p -> validateCoordinateRuler(p.getLat(), p.getLon()))
                .filter(p -> validateAlarm(p))
                .filter(p -> validateMeasAlarmLevelAge(p))
                .filter(p -> validateMeasAlarmLevelChange(p))
                .filter(p -> validateMeasDisplacementAll(p))
                .filter(p -> validateMeasDisplacementLatest(p))
                .filter(p -> validateMeasDisplacementPercentH(p))
                .filter(p -> validateMeasDisplacementPercentP(p))
                .filter(p -> validateMeasSpeed(p))
                .filter(p -> validateMeasCount(p))
                .filter(p -> validateAge(p.ext().getDateFirst(), mMeasDateFirstLowProperty, mMeasDateFirstHighProperty))
                .filter(p -> validateAge(p.getDateLatest(), mMeasDateLastLowProperty, mMeasDateLastHighProperty))
                .filter(p -> validateNextMeas(p))
                .filter(p -> validateMeasWithout(p))
                .filter(p -> validateMeasCode(p))
                .filter(p -> validateMeasOperators(p))
                .filter(p -> validateDateFromToHas(p.getDateValidFrom(), p.getDateValidTo()))
                .filter(p -> validateDateFromToWithout(p.getDateValidFrom(), p.getDateValidTo()))
                .filter(p -> validateDateFromToIs(p.getDateValidFrom(), p.getDateValidTo()))
                .filter(p -> validateMeasYoyo(p))
                .filter(p -> validateDisruptor(p.getZeroX(), p.getZeroY()))
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

        if (mMeasTopListProperty.get()) {
            filteredItems = createTopList(filteredItems);
        }

        mManager.getFilteredItems().setAll(filteredItems);

        getInfoPopOver().loadContent(createInfoContent().renderFormatted());
    }

    void initCheckModelListeners() {
        List.of(
                mAlarmLevelCheckModel,
                mAlarmNameCheckModel,
                mCategoryCheckModel,
                mDateFromToCheckModel,
                mFrequencyCheckModel,
                mGroupCheckModel,
                mMeasCodeCheckModel,
                mMeasOperatorsCheckModel,
                mMeasNextCheckModel,
                mOperatorCheckModel,
                mOriginCheckModel,
                mStatusCheckModel,
                mDisruptorCheckModel
        ).forEach(cm -> cm.getCheckedItems().addListener(mListChangeListener));
    }

    private ContainerTag createInfoContent() {
        var map = new LinkedHashMap<String, String>();

        map.put(Dict.TEXT.toString(), getFreeText());
        map.put(SDict.DIMENSION.toString(), makeInfoDimension());
        map.put(Dict.STATUS.toString(), makeInfo(mStatusCheckModel.getCheckedItems()));
        map.put(Dict.GROUP.toString(), makeInfo(mGroupCheckModel.getCheckedItems()));
        map.put(Dict.CATEGORY.toString(), makeInfo(mCategoryCheckModel.getCheckedItems()));
        map.put(SDict.ALARMS.toString(), makeInfo(mAlarmNameCheckModel.getCheckedItems()));
        map.put(SDict.OPERATOR.toString(), makeInfo(mOperatorCheckModel.getCheckedItems()));
        map.put(Dict.ORIGIN.toString(), makeInfo(mOriginCheckModel.getCheckedItems()));
        map.put(SDict.FREQUENCY.toString(), makeInfoInteger(mFrequencyCheckModel.getCheckedItems()));
        map.put(SDict.VALID_FROM_TO.toString(), makeInfo(mDateFromToCheckModel.getCheckedItems()));
        map.put(getBundle().getString("nextMeasCheckComboBoxTitle"), makeInfo(mMeasNextCheckModel.getCheckedItems()));
        map.put(getBundle().getString("measCodeCheckComboBoxTitle"), makeInfo(mMeasCodeCheckModel.getCheckedItems()));
        map.put("Första " + Dict.FROM.toString(), mMeasDateFirstLowProperty.get() != null ? mMeasDateFirstLowProperty.get().toString() : "");
        map.put("Första " + Dict.TO.toString(), mMeasDateFirstHighProperty.get() != null ? mMeasDateFirstHighProperty.get().toString() : "");
        map.put("Senaste " + Dict.FROM.toString(), mMeasDateLastLowProperty.get() != null ? mMeasDateLastLowProperty.get().toString() : "");
        map.put("Senaste " + Dict.TO.toString(), mMeasDateLastHighProperty.get() != null ? mMeasDateLastHighProperty.get().toString() : "");

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

    private List<BTopoControlPoint> createTopList(List<BTopoControlPoint> filteredItems) {
        var topListMaxSize = mMeasTopListSizeValueProperty.get();
        var limit = mMeasTopListLimitProperty.get();
        var unit = mMeasTopListUnitProperty.get();
        var pointToDiffMap = new LinkedHashMap<BTopoControlPoint, Double>();

        filteredItems.forEach(p -> {
            var reversedObservations = p.ext().getObservationsTimeFiltered().reversed();
            List<BTopoControlPointObservation> limitedObservations;
            if (limit == 0) {
                limitedObservations = reversedObservations;
            } else {
                if (unit == AlarmLevelChangeUnit.DAYS) {
                    var arrayList = new ArrayList<BTopoControlPointObservation>();
                    for (var o : reversedObservations) {
                        if (o.getDate().isAfter(LocalDateTime.now().minusDays(limit))) {
                            arrayList.add(o);
                        } else {
                            break;
                        }
                    }
                    limitedObservations = arrayList;
                } else {
                    limitedObservations = reversedObservations.subList(0, Math.min(limit, reversedObservations.size()));
                }
            }

            if (limitedObservations.size() >= 2) {
                var delta = limitedObservations.getFirst().ext().getDelta() - limitedObservations.getLast().ext().getDelta();
                pointToDiffMap.put(p, Math.abs(delta));
            }
        });

        return CollectionHelper.sortByValue(pointToDiffMap, SortOrder.DESCENDING)
                .entrySet()
                .stream()
                .limit(topListMaxSize)
                .map(entry -> entry.getKey())
                .toList();
    }

    private void initListeners() {
        List.of(
                mInvertProperty,
                mDimens1Property,
                mDimens2Property,
                mDimens3Property,
                mMeasAlarmLevelChangeProperty,
                mMeasAlarmLevelChangeLimitProperty,
                mMeasAlarmLevelChangeModeProperty,
                mMeasAlarmLevelChangeUnitProperty,
                mMeasAlarmLevelChangeValueProperty,
                mMeasTopListProperty,
                mMeasTopListLimitProperty,
                mMeasTopListUnitProperty,
                mMeasTopListSizeValueProperty,
                mMeasAlarmLevelAgeProperty,
                mMeasAlarmLevelAgeValueProperty,
                mMeasDiffAllProperty,
                mMeasDiffAllValueProperty,
                mMeasDiffPercentageHProperty,
                mMeasDiffPercentageHValueProperty,
                mMeasDiffPercentagePProperty,
                mMeasDiffPercentagePValueProperty,
                mMeasDiffLatestProperty,
                mMeasDiffLatestValueProperty,
                mMeasSpeedProperty,
                mMeasSpeedValueProperty,
                mMeasIncludeWithout,
                mMeasLatestOperator,
                mMeasNumOfProperty,
                mMeasNumOfValueProperty,
                mSameAlarmProperty,
                mMeasYoyoCountValueProperty,
                mMeasYoyoSizeValueProperty,
                mMeasYoyoProperty,
                mMeasDateFirstLowProperty,
                mMeasDateFirstHighProperty,
                mMeasDateLastLowProperty,
                mMeasDateLastHighProperty,
                //
                disruptorDistanceProperty(),
                mDisruptorManager.lastChangedProperty()
        ).forEach(propertyBase -> propertyBase.addListener(mChangeListenerObject));
    }

    private String makeInfoDimension() {
        var d1 = mDimens1Property.get();
        var d2 = mDimens2Property.get();
        var d3 = mDimens3Property.get();

        var sb = new StringBuilder();

        sb.append(BooleanHelper.asCheckBox(d1, "1")).append(", ")
                .append(BooleanHelper.asCheckBox(d2, "2")).append(", ")
                .append(BooleanHelper.asCheckBox(d3, "3"));

        return sb.toString();
    }

    private boolean validateAge(LocalDateTime lastMeasurementDateTime, SimpleObjectProperty<LocalDate> low, SimpleObjectProperty<LocalDate> high) {
        if (null != lastMeasurementDateTime) {
            var lowDate = low.get();
            var highDate = high.get();
            var valid = DateHelper.isBetween(lowDate, highDate, lastMeasurementDateTime.toLocalDate());

            return valid;
        } else {
            return false;
        }
    }

    private boolean validateAlarm(BTopoControlPoint p) {
        if (mAlarmLevelCheckModel.isEmpty()) {
            return true;
        }

        var level = TopoHelper.getAlarmLevel(p);
        var levelH = TopoHelper.getAlarmLevelHeight(p);
        var levelP = TopoHelper.getAlarmLevelPlane(p);

        var anyAlarmLevelFilterValues = EnumSet.of(AlarmLevelFilter.ANY_0, AlarmLevelFilter.ANY_1, AlarmLevelFilter.ANY_2, AlarmLevelFilter.ANY_E);

        for (var alarmFilter : AlarmLevelFilter.values()) {
            var itemChecked = mAlarmLevelCheckModel.isChecked(alarmFilter);
            if (anyAlarmLevelFilterValues.contains(alarmFilter) && itemChecked) {
                if (alarmFilter == AlarmLevelFilter.ANY_0 && level == 0) {
                    return true;
                } else if (alarmFilter == AlarmLevelFilter.ANY_1 && level == 1) {
                    return true;
                } else if (alarmFilter == AlarmLevelFilter.ANY_2 && level == 2) {
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
                    var hSelected = mAlarmLevelCheckModel.isChecked(AlarmLevelFilter.HEIGHT_0)
                            || mAlarmLevelCheckModel.isChecked(AlarmLevelFilter.HEIGHT_1)
                            || mAlarmLevelCheckModel.isChecked(AlarmLevelFilter.HEIGHT_2)
                            || mAlarmLevelCheckModel.isChecked(AlarmLevelFilter.HEIGHT_E);

                    var pSelected = mAlarmLevelCheckModel.isChecked(AlarmLevelFilter.PLANE_0)
                            || mAlarmLevelCheckModel.isChecked(AlarmLevelFilter.PLANE_1)
                            || mAlarmLevelCheckModel.isChecked(AlarmLevelFilter.PLANE_2)
                            || mAlarmLevelCheckModel.isChecked(AlarmLevelFilter.PLANE_E);
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

    private boolean validateDimension(BDimension dimension) {
        var d1 = mDimens1Property.get();
        var d2 = mDimens2Property.get();
        var d3 = mDimens3Property.get();

        if ((d1 || d2 || d3) == false) {
            return true;
        }

        switch (dimension) {
            case _1d -> {
                return d1;
            }

            case _2d -> {
                return d2;
            }

            case _3d -> {
                return d3;
            }
        }

        return false;
    }

    private boolean validateMeasAlarmLevelAge(BTopoControlPoint p) {
        if (!mMeasAlarmLevelAgeProperty.get()) {
            return true;
        }

        var hset = new HashSet<Integer>();
        var pset = new HashSet<Integer>();

        for (var o : p.ext().getObservationsTimeFiltered()) {
            hset.add(p.ext().getAlarmLevel(BComponent.HEIGHT, o));
            pset.add(p.ext().getAlarmLevel(BComponent.PLANE, o));
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

        var lim = mMeasAlarmLevelAgeValueProperty.get();
        Long value = null;

        var ageH = p.ext().getAlarmLevelAge(BComponent.HEIGHT);
        var ageP = p.ext().getAlarmLevelAge(BComponent.PLANE);

        if (ObjectUtils.allNull(ageH, ageP)) {
            return true;
        }

        switch (p.getDimension()) {
            case BDimension._1d -> {
                value = p.ext().getAlarmLevelAge(BComponent.HEIGHT);
            }
            case BDimension._2d -> {
                value = p.ext().getAlarmLevelAge(BComponent.PLANE);
            }
            case BDimension._3d -> {
                var valueH = p.ext().getAlarmLevelAge(BComponent.HEIGHT);
                var valueP = p.ext().getAlarmLevelAge(BComponent.PLANE);

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

    private boolean validateMeasAlarmLevelChange(BTopoControlPoint p) {
        if (!mMeasAlarmLevelChangeProperty.get()) {
            return true;
        }

        var observations = p.ext().getObservationsTimeFiltered();
        if (observations.size() < 2) {
            return false;
        }

        var mode = measAlarmLevelChangeModeProperty().get();
        var unit = measAlarmLevelChangeUnitProperty().get();
        int value = mMeasAlarmLevelChangeValueProperty.get();
        int limit = mMeasAlarmLevelChangeLimitProperty.get();

        Stream<BTopoControlPointObservation> source;
        if (unit == AlarmLevelChangeUnit.DAYS) {
            source = observations.stream()
                    .filter(o -> MTemporalManager.getInstance().isValid(o.getDate()))
                    .filter(o -> DateHelper.isAfterOrEqual(o.getDate().toLocalDate(), LocalDate.now().minusDays(value)));
        } else {
            source = observations.stream()
                    .filter(o -> MTemporalManager.getInstance().isValid(o.getDate()))
                    .skip(Math.max(0, observations.size() - value));
        }

        var filteredObservations = source.toList();

        if (filteredObservations.isEmpty()) {
            return false;
        }

        int countBetter = 0;
        int countWorse = 0;

        for (int i = 1; i < filteredObservations.size(); i++) {
            var prev = filteredObservations.get(i - 1);
            var current = filteredObservations.get(i);
            int prevLevel = p.ext().getAlarmLevel(prev);
            int currentLevel = p.ext().getAlarmLevel(current);

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

    private boolean validateMeasCode(BTopoControlPoint p) {
        if (p.ext().getObservationsAllRaw().isEmpty()) {
            return true;
        }
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

    private boolean validateMeasDisplacementPercentH(BTopoControlPoint p) {
        if (!mMeasDiffPercentageHProperty.get() || p.getDimension() == BDimension._2d || p.ext().getAlarmPercent(BComponent.HEIGHT) == null) {
            return true;
        }

        double lim = mMeasDiffPercentageHValueProperty.get();
        double value = p.ext().getAlarmPercent(BComponent.HEIGHT);

        if (lim == 0) {
            return value == 0;
        } else if (lim < 0) {
            return value <= Math.abs(lim);
        } else {
            return value >= lim;
        }
    }

    private boolean validateMeasDisplacementPercentP(BTopoControlPoint p) {
        if (!mMeasDiffPercentagePProperty.get()
                || p.getDimension() == BDimension._1d
                || p.ext().getAlarmPercent(BComponent.PLANE) == null
                || p.ext().deltaZero().getDelta2() == null) {
            return true;
        }

        double lim = mMeasDiffPercentagePValueProperty.get();
        double value = p.ext().getAlarmPercent(BComponent.PLANE);

        if (lim == 0) {
            return value == 0;
        } else if (lim < 0) {
            return value <= Math.abs(lim);
        } else {
            return value >= lim;
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

    private boolean validateMeasSpeed(BTopoControlPoint p) {
        if (mMeasSpeedProperty.get()) {
//        if (mMeasSpeedProperty.get() && p.ext().deltaZero().getDelta() != null && p.ext().deltaZero().getDelta1() != null) {
            double lim = mMeasSpeedValueProperty.get();
            double value = Math.abs(p.ext().getSpeed()[0]);

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

    private boolean validateMeasWithout(BTopoControlPoint p) {
        var valid = mMeasIncludeWithout.get() || p.ext().getNumOfObservations() > 0;

        return valid;
    }

    private boolean validateMeasYoyo(BTopoControlPoint p) {
        if (!mMeasYoyoProperty.get()) {
            return true;
        } else if (p.getDimension() != BDimension._1d || p.ext().getObservationsTimeFiltered().size() < 2) {
            return false;
        }

        int matches = 0;
        double prevSignum = 0.0;

        for (int i = 1; i < p.ext().getObservationsTimeFiltered().size(); i++) {
            var prevMeas = p.ext().getObservationsTimeFiltered().get(i - 1);
            var currenMeas = p.ext().getObservationsTimeFiltered().get(i);
            var currentDeltaZ = currenMeas.ext().getDeltaZ();
            var prevDeltaZ = prevMeas.ext().getDeltaZ();

            if (ObjectUtils.anyNull(currentDeltaZ, prevDeltaZ)) {
                continue;
            }

            var signum = Math.signum(currentDeltaZ - prevDeltaZ);
            boolean directionChange = prevSignum != 0 && prevSignum != signum;
            prevSignum = signum;

            if (directionChange && Math.abs(currentDeltaZ - prevDeltaZ) >= mMeasYoyoSizeValueProperty.get()) {
                matches++;
            }
        }

        return matches >= mMeasYoyoCountValueProperty.get();
    }

    private boolean validateNextMeas(BTopoControlPoint p) {
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
        } else if (frequency > 0 && mMeasNextCheckModel.isChecked("0") && remainingDays == 0) {
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
