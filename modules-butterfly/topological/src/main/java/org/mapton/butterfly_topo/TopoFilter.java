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
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleBooleanProperty;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_core.api.BCoordinatrix;
import org.mapton.butterfly_core.api.BFilterSectionAlarm;
import org.mapton.butterfly_core.api.BFilterSectionAlarmProvider;
import org.mapton.butterfly_core.api.BFilterSectionDate;
import org.mapton.butterfly_core.api.BFilterSectionDateProvider;
import org.mapton.butterfly_core.api.BFilterSectionDisruptor;
import org.mapton.butterfly_core.api.BFilterSectionDisruptorProvider;
import org.mapton.butterfly_core.api.BFilterSectionMeas;
import org.mapton.butterfly_core.api.BFilterSectionMeasProvider;
import org.mapton.butterfly_core.api.BFilterSectionMisc;
import org.mapton.butterfly_core.api.BFilterSectionMiscProvider;
import org.mapton.butterfly_core.api.BFilterSectionPoint;
import org.mapton.butterfly_core.api.BFilterSectionPointProvider;
import org.mapton.butterfly_core.api.BFilterSectionTrend;
import org.mapton.butterfly_core.api.BFilterSectionTrendProvider;
import org.mapton.butterfly_core.api.ButterflyFormFilter;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.BMeasurementMode;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.api.TopoManager;
import se.trixon.almond.util.BooleanHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
public class TopoFilter extends ButterflyFormFilter<TopoManager> implements
        BFilterSectionMiscProvider,
        BFilterSectionPointProvider,
        BFilterSectionDateProvider,
        BFilterSectionTrendProvider,
        BFilterSectionAlarmProvider,
        BFilterSectionMeasProvider,
        BFilterSectionDisruptorProvider {

    private final SimpleBooleanProperty m1dCloseToAutoProperty = new SimpleBooleanProperty();
    private final SimpleBooleanProperty mDimens1Property = new SimpleBooleanProperty();
    private final SimpleBooleanProperty mDimens2Property = new SimpleBooleanProperty();
    private final SimpleBooleanProperty mDimens3Property = new SimpleBooleanProperty();
    private FilterSectionMeas mFilterSectionMeasSpecific;
    private FilterSectionMon mFilterSectionMon;
    private final TopoManager mManager = TopoManager.getInstance();
    private final SimpleBooleanProperty mMeasIncludeWithout = new SimpleBooleanProperty();

    public TopoFilter() {
        super(TopoManager.getInstance());
        mContentOptions = TopoContentOptions.getInstance();
        initListeners();
    }

    public SimpleBooleanProperty closeToAutoProperty() {
        return m1dCloseToAutoProperty;
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

    public void initCheckModelListeners() {
//        List.of(
//        ).forEach(cm -> cm.getCheckedItems().addListener(mListChangeListener));
    }

    public SimpleBooleanProperty measIncludeWithoutProperty() {
        return mMeasIncludeWithout;
    }

    @Override
    public void setFilterSection(BFilterSectionDate filterSection) {
        mFilterSectionDate = filterSection;
        mFilterSectionDate.initListeners(mChangeListenerObject, mListChangeListener);
    }

    @Override
    public void setFilterSection(BFilterSectionPoint filterSection) {
        mFilterSectionPoint = filterSection;
        mFilterSectionPoint.initListeners(mChangeListenerObject, mListChangeListener);
    }

    @Override
    public void setFilterSection(BFilterSectionDisruptor filterSection) {
        mFilterSectionDisruptor = filterSection;
        mFilterSectionDisruptor.initListeners(mChangeListenerObject, mListChangeListener);
    }

    @Override
    public void setFilterSection(BFilterSectionMisc filterSection) {
        mFilterSectionMisc = filterSection;
        mFilterSectionMisc.initListeners(mChangeListenerObject, mListChangeListener);
    }

    @Override
    public void setFilterSection(BFilterSectionTrend filterSection) {
        mFilterSectionTrend = filterSection;
        mFilterSectionTrend.initListeners(mChangeListenerObject, mListChangeListener);
    }

    @Override
    public void setFilterSection(BFilterSectionAlarm filterSection) {
        mFilterSectionAlarm = filterSection;
        mFilterSectionAlarm.initListeners(mChangeListenerObject, mListChangeListener);
    }

    @Override
    public void setFilterSection(BFilterSectionMeas filterSection) {
        mFilterSectionMeas = filterSection;
        mFilterSectionMeas.initListeners(mChangeListenerObject, mListChangeListener);
    }

    public void setFilterSection(FilterSectionMeas filterSection) {
        mFilterSectionMeasSpecific = filterSection;
        mFilterSectionMeasSpecific.initListeners(mChangeListenerObject, mListChangeListener);
    }

    public void setFilterSection(FilterSectionMon filterSection) {
        mFilterSectionMon = filterSection;
        mFilterSectionMon.initListeners(mChangeListenerObject, mListChangeListener);
    }

    @Override
    public void update() {
        var filteredItems = mManager.getAllItems().stream()
                .filter(p -> p.isVisible() != mInvisibleProperty.get())
                .filter(p -> {
                    var alarmH = p.ext().getAlarm(BComponent.HEIGHT);
                    var alarmP = p.ext().getAlarm(BComponent.PLANE);

                    var nameH = alarmH == null ? "" : alarmH.getName();
                    var nameP = alarmP == null ? "" : alarmP.getName();

                    return validateFreeText(p.getName(), p.getCategory(), p.getGroup(), p.getAlarm1Id(), p.getAlarm2Id(), nameH, nameP, p.getTag());
                })
                .filter(p -> validateCoordinateCircle(p.getLat(), p.getLon()))
                .filter(p -> validateCoordinateArea(p.getLat(), p.getLon()))
                .filter(p -> validateCoordinateRuler(p.getLat(), p.getLon()))
                .filter(p -> mFilterSectionPoint.isSelected() && validateDimension(p.getDimension()))
                .filter(p -> validate1dCloseToAuto(p))
                .filter(p -> mFilterSectionPoint.filter(p, p.ext().getMeasurementUntilNext(ChronoUnit.DAYS)))
                .filter(p -> mFilterSectionDate.filter(p, p.ext().getDateFirst()))
                .filter(p -> mFilterSectionAlarm.filter(p))
                .filter(p -> mFilterSectionDisruptor.filter(p))
                .filter(p -> mFilterSectionTrend.filter(p))
                .filter(p -> mFilterSectionMon.filter(p))
                .filter(p -> mFilterSectionMisc.filter(p))
                .filter(p -> mFilterSectionMeas.filter(p) && mFilterSectionMeasSpecific.filter(p))
                .filter(p -> validateMeasWithout(p))
                .toList();

        filteredItems = mFilterSectionMisc.filterCluster(filteredItems);

        if (mFilterSectionAlarm.getSameAlarmCheckBox().isSelected()) {
            var hAlarms = filteredItems.stream().map(o -> o.getAlarm1Id()).collect(Collectors.toSet());
            var pAlarms = filteredItems.stream().map(o -> o.getAlarm2Id()).collect(Collectors.toSet());

            filteredItems = mManager.getAllItems().stream()
                    .filter(o -> {
                        String hAlarm = o.getAlarm1Id();
                        String pAlarm = o.getAlarm2Id();

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

        if (mFilterSectionMeas.isSelected() && mFilterSectionMeasSpecific.shouldCreateTopList()) {
            filteredItems = mFilterSectionMeasSpecific.createTopList(filteredItems);
        }

        filteredItems = sortAndLimit(filteredItems);

        mManager.setItemsFiltered(filteredItems);
        getInfoPopOver().loadContent(createInfoContent().renderFormatted());
    }

    private ContainerTag createInfoContent() {
        var map = new LinkedHashMap<String, String>();
        map.put(Dict.TEXT.toString(), getFreeText());
        mFilterSectionPoint.createInfoContent(map);
        map.put(SDict.DIMENSION.toString(), makeInfoDimension());
        mFilterSectionMeas.createInfoContent(map);
        mFilterSectionMeasSpecific.createInfoContent(map);
        mFilterSectionAlarm.createInfoContent(map);
        mFilterSectionDate.createInfoContent(map);
        mFilterSectionDisruptor.createInfoContent(map);
        mFilterSectionTrend.createInfoContent(map);

        return createHtmlFilterInfo(map);
    }

    private void initListeners() {
        List.of(
                mInvertProperty,
                mInvisibleProperty,
                mDimens1Property,
                mDimens2Property,
                mDimens3Property,
                m1dCloseToAutoProperty,
                mMeasIncludeWithout,
                mContentOptions.listSortOrderProperty(),
                mContentOptions.listLimitProperty()
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

    private boolean validate1dCloseToAuto(BTopoControlPoint point) {
        if (point.getDimension() != BDimension._1d || !m1dCloseToAutoProperty.get()) {
            return true;
        } else {
            return mManager.getAllItems().stream()
                    .filter(p -> p.getMeasurementMode() == BMeasurementMode.AUTOMATIC)
                    .filter(p -> p.ext().getMeasurementAge(ChronoUnit.DAYS) < 7)
                    .filter(p -> {
                        var distance = BCoordinatrix.toLatLon(p).distance(BCoordinatrix.toLatLon(point));
                        return distance <= 10.0;
                    })
                    .findFirst()
                    .isPresent();
        }
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

    private boolean validateMeasWithout(BTopoControlPoint p) {
        var valid = mMeasIncludeWithout.get() || p.ext().getNumOfObservations() > 0;

        return valid;
    }

}
