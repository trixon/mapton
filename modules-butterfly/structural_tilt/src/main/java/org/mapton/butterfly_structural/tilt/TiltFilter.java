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
package org.mapton.butterfly_structural.tilt;

import j2html.tags.ContainerTag;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.ui.forms.MFilterSectionDateProvider;
import org.mapton.api.ui.forms.MFilterSectionDisruptorProvider;
import org.mapton.api.ui.forms.MFilterSectionPointProvider;
import org.mapton.butterfly_core.api.ButterflyFormFilter;
import org.mapton.butterfly_core.api.FilterSectionMiscProvider;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class TiltFilter extends ButterflyFormFilter<TiltManager> implements
        FilterSectionMiscProvider,
        MFilterSectionPointProvider,
        MFilterSectionDateProvider,
        MFilterSectionDisruptorProvider {

    private IndexedCheckModel mAlarmNameCheckModel;
    private final ResourceBundle mBundle = NbBundle.getBundle(TiltFilter.class);
    private IndexedCheckModel mCategoryCheckModel;
    private final SimpleObjectProperty<LocalDate> mDateFirstHighProperty = new SimpleObjectProperty();
    private final SimpleObjectProperty<LocalDate> mDateFirstLowProperty = new SimpleObjectProperty();
    private final SimpleObjectProperty<LocalDate> mDateLastHighProperty = new SimpleObjectProperty();
    private final SimpleObjectProperty<LocalDate> mDateLastLowProperty = new SimpleObjectProperty();
    private IndexedCheckModel mGroupCheckModel;
    private final SimpleBooleanProperty mInvertProperty = new SimpleBooleanProperty();
    private final TiltManager mManager = TiltManager.getInstance();
    private IndexedCheckModel<String> mMeasNextCheckModel;
    private IndexedCheckModel mOperatorCheckModel;
    private IndexedCheckModel mOriginCheckModel;
    private final SimpleBooleanProperty mSectionDateProperty = new SimpleBooleanProperty();
    private final SimpleBooleanProperty mSectionDisruptorProperty = new SimpleBooleanProperty();
    private final SimpleBooleanProperty mSectionPointProperty = new SimpleBooleanProperty();
    private IndexedCheckModel mStatusCheckModel;

    public TiltFilter() {
        super(TiltManager.getInstance());

        initListeners();
    }

    @Override
    public SimpleObjectProperty<LocalDate> dateFirstHighProperty() {
        return mDateFirstHighProperty;
    }

    @Override
    public SimpleObjectProperty<LocalDate> dateFirstLowProperty() {
        return mDateFirstLowProperty;
    }

    @Override
    public SimpleObjectProperty<LocalDate> dateLastHighProperty() {
        return mDateLastHighProperty;
    }

    @Override
    public SimpleObjectProperty<LocalDate> dateLastLowProperty() {
        return mDateLastLowProperty;
    }

    @Override
    public IndexedCheckModel getAlarmLevelCheckModel() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public IndexedCheckModel getAlarmNameCheckModel() {
        return mAlarmNameCheckModel;
    }

    @Override
    public IndexedCheckModel getCategoryCheckModel() {
        return mCategoryCheckModel;
    }

    @Override
    public IndexedCheckModel getGroupCheckModel() {
        return mGroupCheckModel;
    }

    public IndexedCheckModel<String> getMeasNextCheckModel() {
        return mMeasNextCheckModel;
    }

    @Override
    public IndexedCheckModel getOperatorCheckModel() {
        return mOperatorCheckModel;
    }

    @Override
    public IndexedCheckModel getOriginCheckModel() {
        return mOriginCheckModel;
    }

    @Override
    public IndexedCheckModel getStatusCheckModel() {
        return mStatusCheckModel;
    }

    public void initCheckModelListeners() {
        List.of(
                mAlarmNameCheckModel,
                mCategoryCheckModel,
                getDateFromToCheckModel(),
                mFrequencyCheckModel,
                mMeasNextCheckModel,
                mGroupCheckModel,
                mOperatorCheckModel,
                mOriginCheckModel,
                mStatusCheckModel,
                getDisruptorCheckModel()
        ).forEach(cm -> cm.getCheckedItems().addListener(mListChangeListener));
    }

    @Override
    public SimpleBooleanProperty invertProperty() {
        return mInvertProperty;
    }

    @Override
    public SimpleBooleanProperty sectionDateProperty() {
        return mSectionDateProperty;
    }

    @Override
    public SimpleBooleanProperty sectionDisruptorProperty() {
        return mSectionDisruptorProperty;
    }

    @Override
    public SimpleBooleanProperty sectionPointProperty() {
        return mSectionPointProperty;
    }

    @Override
    public void setAlarmNameCheckModel(IndexedCheckModel alarmNameCheckModel) {
        mAlarmNameCheckModel = alarmNameCheckModel;
    }

    @Override
    public void setCategoryCheckModel(IndexedCheckModel categoryCheckModel) {
        mCategoryCheckModel = categoryCheckModel;
    }

    @Override
    public void setGroupCheckModel(IndexedCheckModel groupCheckModel) {
        mGroupCheckModel = groupCheckModel;
    }

    @Override
    public void setMeasNextCheckModel(IndexedCheckModel measNextCheckModel) {
        mMeasNextCheckModel = measNextCheckModel;
    }

    @Override
    public void setOperatorCheckModel(IndexedCheckModel operatorCheckModel) {
        mOperatorCheckModel = operatorCheckModel;
    }

    @Override
    public void setOriginCheckModel(IndexedCheckModel originCheckModel) {
        mOriginCheckModel = originCheckModel;
    }

    @Override
    public void setStatusCheckModel(IndexedCheckModel statusCheckModel) {
        mStatusCheckModel = statusCheckModel;
    }

    @Override
    public void update() {
        var filteredItems = mManager.getAllItems().stream()
                .filter(p -> validateFreeText(p.getName(), p.getGroup(), p.getComment()))
                .filter(p -> validateCoordinateArea(p.getLat(), p.getLon()))
                .filter(p -> validateCoordinateRuler(p.getLat(), p.getLon()))
                .filter(p -> {
                    if (mSectionPointProperty.get()) {
                        return validateCheck(mStatusCheckModel, p.getStatus())
                                && validateCheck(mGroupCheckModel, p.getGroup())
                                && validateCheck(mCategoryCheckModel, p.getCategory())
                                && validateAlarmName1(p, mAlarmNameCheckModel)
                                && validateFrequency(p.getFrequency())
                                && validateCheck(mOperatorCheckModel, p.getOperator())
                                && validateCheck(mOriginCheckModel, p.getOrigin())
                                && validateNextMeas(p, mMeasNextCheckModel, p.ext().getMeasurementUntilNext(ChronoUnit.DAYS))
                                && true;
                    } else {
                        return true;
                    }
                })
                .filter(p -> {
                    if (mSectionDateProperty.get()) {
                        return validateDateFromToHas(p.getDateValidFrom(), p.getDateValidTo())
                                && validateDateFromToWithout(p.getDateValidFrom(), p.getDateValidTo())
                                && validateDateFromToIs(p.getDateValidFrom(), p.getDateValidTo())
                                && validateAge(p.ext().getDateFirst(), mDateFirstLowProperty, mDateFirstHighProperty)
                                && validateAge(p.getDateLatest(), mDateLastLowProperty, mDateLastHighProperty)
                                && true;
                    } else {
                        return true;
                    }
                })
                .filter(p -> {
                    if (mSectionDisruptorProperty.get()) {
                        return validateDisruptor(p.getZeroX(), p.getZeroY())
                                && true;
                    } else {
                        return true;
                    }
                })
                //                .filter(p -> validateCheck(mAlarmNameCheckModel, p.getAlarm1Id()))
                .toList();

        if (mInvertProperty.get()) {
            var toBeExluded = new HashSet<>(filteredItems);
            filteredItems = mManager.getAllItems().stream()
                    .filter(p -> !toBeExluded.contains(p))
                    .toList();
        }

        mManager.getFilteredItems().setAll(filteredItems);

        getInfoPopOver().loadContent(createInfoContent().renderFormatted());
    }

    private ContainerTag createInfoContent() {
        //TODO Add measOperator+latest
        var map = new LinkedHashMap<String, String>();

        map.put(Dict.TEXT.toString(), getFreeText());
        map.put(Dict.GROUP.toString(), makeInfo(mGroupCheckModel.getCheckedItems()));
//        map.put(Dict.TYPE.toString(), makeInfo(mTypeCheckModel.getCheckedItems()));
//        map.put(mBundle.getString("soilMaterial"), makeInfo(mSoilCheckModel.getCheckedItems()));

        return createHtmlFilterInfo(map);

    }

    private void initListeners() {
        List.of(mSectionPointProperty,
                mSectionDateProperty,
                mSectionDisruptorProperty,
                mDateFirstLowProperty,
                mDateFirstHighProperty,
                mDateLastLowProperty,
                mDateLastHighProperty,
                mInvertProperty,
                //
                disruptorDistanceProperty(),
                mDisruptorManager.lastChangedProperty()
        ).forEach(propertyBase -> propertyBase.addListener(mChangeListenerObject));
    }
}
