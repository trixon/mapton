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
package org.mapton.butterfly_structural.strain;

import j2html.tags.ContainerTag;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.ui.forms.FormFilter;
import org.mapton.api.ui.forms.MFilterSectionDateProvider;
import org.mapton.api.ui.forms.MFilterSectionDisruptorProvider;
import org.mapton.api.ui.forms.MFilterSectionPointProvider;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class StrainFilter extends FormFilter<StrainManager> implements
        MFilterSectionPointProvider,
        MFilterSectionDateProvider,
        MFilterSectionDisruptorProvider {

    private IndexedCheckModel mAlarmNameCheckModel;
    private final ResourceBundle mBundle = NbBundle.getBundle(StrainFilter.class);
    private IndexedCheckModel mCategoryCheckModel;
    private IndexedCheckModel mGroupCheckModel;
    private final StrainManager mManager = StrainManager.getInstance();
    private final SimpleObjectProperty<LocalDate> mMeasDateFirstHighProperty = new SimpleObjectProperty();
    private final SimpleObjectProperty<LocalDate> mMeasDateFirstLowProperty = new SimpleObjectProperty();
    private final SimpleObjectProperty<LocalDate> mMeasDateLastHighProperty = new SimpleObjectProperty();
    private final SimpleObjectProperty<LocalDate> mMeasDateLastLowProperty = new SimpleObjectProperty();
    private IndexedCheckModel mOperatorCheckModel;
    private IndexedCheckModel mOriginCheckModel;
    private final SimpleBooleanProperty mSectionBasicProperty = new SimpleBooleanProperty();
    private final SimpleBooleanProperty mSectionDateProperty = new SimpleBooleanProperty();
    private final SimpleBooleanProperty mSectionDisruptorProperty = new SimpleBooleanProperty();
    private IndexedCheckModel mStatusCheckModel;

    public StrainFilter() {
        super(StrainManager.getInstance());

        initListeners();
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
        mGroupCheckModel.getCheckedItems().addListener(mListChangeListener);
        List.of(
                //                mAlarmLevelCheckModel,
                mAlarmNameCheckModel,
                mCategoryCheckModel,
                getDateFromToCheckModel(),
                mFrequencyCheckModel,
                mGroupCheckModel,
                //                mMeasCodeCheckModel,
                //                mMeasOperatorsCheckModel,
                //                mMeasNextCheckModel,
                mOperatorCheckModel,
                mOriginCheckModel,
                mStatusCheckModel
        //                mDisruptorCheckModel
        ).forEach(cm -> cm.getCheckedItems().addListener(mListChangeListener));
    }

    @Override
    public SimpleObjectProperty<LocalDate> measDateFirstHighProperty() {
        return mMeasDateFirstHighProperty;
    }

    @Override
    public SimpleObjectProperty<LocalDate> measDateFirstLowProperty() {
        return mMeasDateFirstLowProperty;
    }

    @Override
    public SimpleObjectProperty<LocalDate> measDateLastHighProperty() {
        return mMeasDateLastHighProperty;
    }

    @Override
    public SimpleObjectProperty<LocalDate> measDateLastLowProperty() {
        return mMeasDateLastLowProperty;
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
        return mSectionBasicProperty;
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
    public void setMeasNextCheckModel(IndexedCheckModel checkModel) {
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
                .filter(p -> validateCheck(mStatusCheckModel, p.getStatus()))
                .filter(p -> validateCheck(mGroupCheckModel, p.getGroup()))
                .filter(p -> validateCheck(mCategoryCheckModel, p.getCategory()))
                .filter(p -> validateCheck(mOperatorCheckModel, p.getOperator()))
                .filter(p -> validateCheck(mOriginCheckModel, p.getOrigin()))
                .filter(p -> validateCheck(mAlarmNameCheckModel, p.getAlarm1Id()))
                .filter(p -> validateFrequency(p.getFrequency()))
                .filter(p -> validateDateFromToHas(p.getDateValidFrom(), p.getDateValidTo()))
                .filter(p -> validateDateFromToWithout(p.getDateValidFrom(), p.getDateValidTo()))
                .filter(p -> validateDateFromToIs(p.getDateValidFrom(), p.getDateValidTo()))
                .filter(p -> validateCoordinateArea(p.getLat(), p.getLon()))
                .filter(p -> validateCoordinateRuler(p.getLat(), p.getLon()))
                .toList();

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

    }
}
