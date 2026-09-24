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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.lang3.ObjectUtils;
import org.controlsfx.tools.Borders;
import org.mapton.api.MAvgPeriod;
import org.mapton.api.ui.forms.DateRangePane;
import org.mapton.api.ui.forms.MBaseFilterSection;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.ce_jfreechart.api.ChartHelper;
import org.openide.util.NbBundle;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.RangeSliderPane;
import se.trixon.almond.util.fx.session.SessionComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class BFilterSectionAvg extends MBaseFilterSection {

    private final ResourceBundle mBundle = NbBundle.getBundle(BFilterSectionAvg.class);
    private SessionComboBox<MAvgPeriod> mEwma1PeriodScb;
    private SessionComboBox<MAvgPeriod> mEwma2PeriodScb;
    private AvgComponent mHeightComponent;
    private AvgComponent mPlaneComponent;
    private final GridPane mRoot = new GridPane(columnGap, rowGap);
    private final TabPane mTabPane = new TabPane();
    public static final MAvgPeriod DEFAULT_PERIOD_1 = MAvgPeriod.SENSITIVE;
    public static final MAvgPeriod DEFAULT_PERIOD_2 = MAvgPeriod.CALM;

    public BFilterSectionAvg() {
        super("Medel");

        createUI();
        setContent(mRoot);
    }

    @Override
    public void clear() {
        super.clear();
        mHeightComponent.clear();
        mPlaneComponent.clear();
        mEwma1PeriodScb.getSelectionModel().select(DEFAULT_PERIOD_1);
        mEwma2PeriodScb.getSelectionModel().select(DEFAULT_PERIOD_2);
    }

    @Override
    public void createInfoContent(LinkedHashMap<String, String> map) {
        if (!isSelected()) {
            return;
        }
        map.put(Dict.MISCELLANEOUS.toUpper(), "TODO");
    }

    public boolean filter(BXyzPoint p) {
        var validEwma1_1d = true;
        var validEwma2_1d = true;
        var validEwma1_2d = true;
        var validEwma2_2d = true;
        boolean[] validActivityDelta1d = {true, true};
        var validActivity1d = true;
        var validActivity2d = true;

        if (isSelected()) {
            if (p.getDimension() != BDimension._2d) {
                String key = mHeightComponent.getKey();
                if (mHeightComponent.isActivatedEwma1()) {
                    validEwma1_1d = validateEwma(p, key, mHeightComponent.mEwma1RangeSliderPane, mEwma1PeriodScb);
                }
                if (mHeightComponent.isActivatedEwma2()) {
                    validEwma2_1d = validateEwma(p, key, mHeightComponent.mEwma2RangeSliderPane, mEwma2PeriodScb);
                }
                if (mHeightComponent.isActivatedActivity()) {
                    validActivity1d = validateActivity(p, mHeightComponent);
                }

                if (mHeightComponent.isActivatedActivityChange() || mHeightComponent.isActivatedActivityStrength()) {
                    validActivityDelta1d = validateActivityDelta(p, mHeightComponent);
                }
            }

            if (p.getDimension() != BDimension._1d) {
                String key = mPlaneComponent.getKey();
                if (mPlaneComponent.isActivatedEwma1()) {
                    validEwma1_2d = validateEwma(p, key, mPlaneComponent.mEwma1RangeSliderPane, mEwma1PeriodScb);
                }
                if (mPlaneComponent.isActivatedEwma2()) {
                    validEwma2_2d = validateEwma(p, key, mPlaneComponent.mEwma2RangeSliderPane, mEwma2PeriodScb);
                }
                if (mPlaneComponent.isActivatedActivity()) {
                    validActivity2d = validateActivity(p, mPlaneComponent);
                }
            }
        }

        var valid = true
                && validEwma1_1d
                && validEwma2_1d
                && validEwma1_2d
                && validEwma2_2d
                && validActivity1d
                && validActivityDelta1d[0]
                && validActivityDelta1d[1]
                && validActivity2d;

        return valid;
    }

    public void initListeners(ChangeListener changeListener, ListChangeListener<Object> listChangeListener) {
        List.of(
                selectedProperty(),
                mEwma1PeriodScb.getSelectionModel().selectedItemProperty(),
                mEwma2PeriodScb.getSelectionModel().selectedItemProperty()
        ).forEach(propertyBase -> propertyBase.addListener(changeListener));

        mHeightComponent.initListeners(changeListener, listChangeListener);
        mPlaneComponent.initListeners(changeListener, listChangeListener);
    }

    public void initListeners(BFilterSectionTrendProvider filter) {
        System.out.println("initListeners");
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        setSessionManager(sessionManager);
        sessionManager.register(getKeyFilter("section"), selectedProperty());
        sessionManager.register(getKeyFilter("ewma1"), mEwma1PeriodScb.selectedIndexProperty());
        sessionManager.register(getKeyFilter("ewma2"), mEwma2PeriodScb.selectedIndexProperty());
        mHeightComponent.initSession(sessionManager);
        mPlaneComponent.initSession(sessionManager);
    }

    public void load() {
        mEwma1PeriodScb.load();
        mEwma2PeriodScb.load();
        mHeightComponent.load();
        mPlaneComponent.load();
    }

    @Override
    public void onShownFirstTime() {
    }

    @Override
    public void reset(PropertiesConfiguration filterConfig) {
        mHeightComponent.reset();
        mPlaneComponent.reset();
        mEwma1PeriodScb.getSelectionModel().selectFirst();
        mEwma2PeriodScb.getSelectionModel().selectFirst();
    }

    private void createUI() {
        mEwma1PeriodScb = new SessionComboBox<>();
        mEwma1PeriodScb.getItems().setAll(MAvgPeriod.values());
        mEwma2PeriodScb = new SessionComboBox<>();
        mEwma2PeriodScb.getItems().setAll(MAvgPeriod.values());
        mHeightComponent = new AvgComponent(BComponent.HEIGHT);
        mPlaneComponent = new AvgComponent(BComponent.PLANE);
        mTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        mTabPane.getTabs().addAll(mHeightComponent, mPlaneComponent);
        int row = 0;
        mRoot.addRow(row++, new VBox(new Label("EWMA 1 (Kort)"), mEwma1PeriodScb), new VBox(new Label("EWMA 2 (Lång)"), mEwma2PeriodScb));
        mRoot.add(mTabPane, 0, row++, GridPane.REMAINING, 1);
        FxHelper.autoSizeColumn(mRoot, 2);
        FxHelper.autoSizeRegionHorizontal(mEwma1PeriodScb, mEwma2PeriodScb);
    }

    private boolean validateActivity(BXyzPoint p, AvgComponent avgComponent) {
        var slider = avgComponent.mActivityRangeSliderPane;
        var period1 = mEwma1PeriodScb.getValue();
        var period2 = mEwma2PeriodScb.getValue();

        HashMap<MAvgPeriod, EwmaHelper.Ewma> map = p.getValue(avgComponent.getKey());
        if (map == null) {
            return false;
        }

        var ewma1 = map.get(period1);
        var ewma2 = map.get(period2);
        if (ObjectUtils.anyNull(ewma1, ewma2)) {
            return false;
        }

        var value1 = ewma1.getLastValue();
        var value2 = ewma2.getLastValue();
        if (ObjectUtils.anyNull(value1, value2)) {
            return false;
        }

        var diff = value1 - value2;

        return slider.isValueValid(diff);
    }

    private boolean[] validateActivityDelta(BXyzPoint p, AvgComponent avgComponent) {
        var period1 = mEwma1PeriodScb.getValue();
        var period2 = mEwma2PeriodScb.getValue();
        var invalid = new boolean[]{false, false};

        HashMap<MAvgPeriod, EwmaHelper.Ewma> map = p.getValue(avgComponent.getKey());
        if (map == null) {
            return invalid;
        }

        var ewma1 = map.get(period1);
        var ewma2 = map.get(period2);
        if (ObjectUtils.anyNull(ewma1, ewma2)) {
            return invalid;
        }

        var timeSeries1 = ewma1.getTimeSeries();
        var timeSeries2 = ewma2.getTimeSeries();
        var timeSeries = XyzChartBuilder.createDifference(timeSeries1, timeSeries2, "");
        var fromDate = DateHelper.getMax(avgComponent.mActivityDateRangePane.lowDateProperty().get(), p.getDateZero());
        var toDate = DateHelper.getMax(avgComponent.mActivityDateRangePane.highDateProperty().get(), p.getDateZero());
        var activityFrom = XyzChartBuilder.getInterpolatedValue(timeSeries, ChartHelper.convertToMinute(fromDate.atStartOfDay()));
        var activityTo = XyzChartBuilder.getInterpolatedValue(timeSeries, ChartHelper.convertToMinute(toDate.atStartOfDay()));
        var activityChange = activityTo - activityFrom;
        var activityStrength = Math.abs(activityTo) - Math.abs(activityFrom);
        var validChange = avgComponent.mActivityChangeRangeSliderPane.isValueValid(activityChange);
        var validStrength = avgComponent.mActivityStrengthRangeSliderPane.isValueValid(activityStrength);

        return new boolean[]{validChange, validStrength};
    }

    private boolean validateEwma(BXyzPoint p, String key, RangeSliderPane slider, SessionComboBox<MAvgPeriod> comboBox) {
        HashMap<MAvgPeriod, EwmaHelper.Ewma> map = p.getValue(key);
        if (map == null) {
            return false;
        }

        var ewma = map.get(comboBox.getValue());
        if (ewma == null) {
            return false;
        }

        return slider.isValueValid(ewma.getLastValue());
    }

    class AvgComponent extends Tab {

        private final RangeSliderPane mActivityChangeRangeSliderPane;
        private final DateRangePane mActivityDateRangePane = new DateRangePane();
        private final RangeSliderPane mActivityRangeSliderPane;
        private final RangeSliderPane mActivityStrengthRangeSliderPane;
        private final BComponent mComponent;
        private final RangeSliderPane mEwma1RangeSliderPane;
        private final RangeSliderPane mEwma2RangeSliderPane;

        public AvgComponent(BComponent component) {
            var maxEwma = 50;
            var minEwma = component == BComponent.HEIGHT ? -maxEwma : 0;
            mEwma1RangeSliderPane = new RangeSliderPane("EWMA 1", minEwma, maxEwma, true, 1.0);
            mEwma2RangeSliderPane = new RangeSliderPane("EWMA 2", minEwma, maxEwma, true, 1.0);
            mActivityRangeSliderPane = new RangeSliderPane("Aktivitet (EWMA 1 - EWMA 2)", -maxEwma, maxEwma, true, 1.0);
            mActivityChangeRangeSliderPane = new RangeSliderPane("Aktivitetsförändring (Aktivitet tom - Aktivitet from)", -maxEwma, maxEwma, true, 1.0);
            mActivityStrengthRangeSliderPane = new RangeSliderPane("Aktivitetsstyrka (abs(Aktivitet tom) - abs(Aktivitet from))", -maxEwma, maxEwma, true, 1.0);
            mComponent = component;
            createUI();
            clear();
        }

        private void clear() {
            mEwma1RangeSliderPane.clear();
            mEwma2RangeSliderPane.clear();
            mActivityRangeSliderPane.clear();
            mActivityChangeRangeSliderPane.clear();
            mActivityStrengthRangeSliderPane.clear();
            mActivityDateRangePane.reset();

            var minDate = LocalDate.now().minusYears(4);
            var startDate = LocalDate.now().minusYears(1);
            mActivityDateRangePane.setMinMaxDate(minDate, LocalDate.now());
            mActivityDateRangePane.lowDateProperty().set(startDate);
        }

        private void createUI() {
            var rowGap = FxHelper.getUIScaled(8);
            var gp = new GridPane(rowGap, rowGap);
            var rangeSliders = List.of(
                    mEwma1RangeSliderPane,
                    mEwma2RangeSliderPane,
                    mActivityRangeSliderPane,
                    mActivityChangeRangeSliderPane,
                    mActivityStrengthRangeSliderPane
            );

            rangeSliders.forEach(rangeSlider -> {
                rangeSlider.setInvertIncluded(true);
                FxHelper.autoSizeRegionHorizontal(rangeSlider);
            });
            int row = 0;
            gp.add(mEwma1RangeSliderPane, 0, row++, GridPane.REMAINING, 1);
            gp.add(mEwma2RangeSliderPane, 0, row++, GridPane.REMAINING, 1);
            gp.add(mActivityRangeSliderPane, 0, row++, GridPane.REMAINING, 1);
            gp.add(mActivityChangeRangeSliderPane, 0, row++, GridPane.REMAINING, 1);
            gp.add(mActivityStrengthRangeSliderPane, 0, row++, GridPane.REMAINING, 1);
            gp.addRow(row++, mActivityDateRangePane.getRoot(), new Label());

            var leftRightPad = FxHelper.getUIScaled(4.0);
            var bottomLeftRightRadius = FxHelper.getUIScaled(12.0);
            var borderNode = Borders.wrap(gp)
                    .etchedBorder()
                    .radius(0, 0, bottomLeftRightRadius, bottomLeftRightRadius)
                    .innerPadding(mTopBorderInnerPadding, mBorderInnerPadding, mBorderInnerPadding, mBorderInnerPadding)
                    .outerPadding(0, leftRightPad, 0, leftRightPad)
                    .raised()
                    .build()
                    .build();
            mActivityDateRangePane.getRoot().disableProperty().bind(mActivityChangeRangeSliderPane.selectedProperty().or(mActivityStrengthRangeSliderPane.selectedProperty()).not());
            mActivityDateRangePane.getRoot().setBackground(FxHelper.createBackground(Color.DARKSALMON));

            FxHelper.autoSizeColumn(gp, 2);
            setContent(borderNode);
            setText(mComponent.getDimension().getName() + "d");
        }

        private String getKey() {
            return mComponent == BComponent.HEIGHT ? BKey.EWMA_H : BKey.EWMA_P;
        }

        private void initListeners(ChangeListener changeListener, ListChangeListener<Object> listChangeListener) {
            List.of(
                    mActivityDateRangePane.selectedFromStartProperty(),
                    mActivityDateRangePane.selectedToEndProperty(),
                    mActivityDateRangePane.lowStringProperty(),
                    mActivityDateRangePane.highStringProperty()
            ).forEach(propertyBase -> propertyBase.addListener(changeListener));

            List.of(
                    mEwma1RangeSliderPane,
                    mEwma2RangeSliderPane,
                    mActivityRangeSliderPane,
                    mActivityChangeRangeSliderPane,
                    mActivityStrengthRangeSliderPane
            ).forEach(rangeSlider -> {
                rangeSlider.selectedProperty().addListener(changeListener);
                rangeSlider.invertedProperty().addListener(changeListener);
                rangeSlider.maxProperty().addListener(changeListener);
                rangeSlider.minProperty().addListener(changeListener);
            });
        }

        private void initSession(SessionManager sessionManager) {
            String mode = mComponent.getDimension().getName() + "_";
            mEwma1RangeSliderPane.initSession(getKeyFilter(mode + "ewma1"), sessionManager);
            mEwma2RangeSliderPane.initSession(getKeyFilter(mode + "ewma2"), sessionManager);
            mActivityRangeSliderPane.initSession(getKeyFilter(mode + "activity"), sessionManager);
            mActivityChangeRangeSliderPane.initSession(getKeyFilter(mode + "activityChange"), sessionManager);
            mActivityStrengthRangeSliderPane.initSession(getKeyFilter(mode + "activityStrength"), sessionManager);
        }

        private boolean isActivatedActivity() {
            return mActivityRangeSliderPane.selectedProperty().get();
        }

        private boolean isActivatedActivityChange() {
            return mActivityChangeRangeSliderPane.selectedProperty().get();
        }

        private boolean isActivatedActivityStrength() {
            return mActivityStrengthRangeSliderPane.selectedProperty().get();
        }

        private boolean isActivatedEwma1() {
            return mEwma1RangeSliderPane.selectedProperty().get();
        }

        private boolean isActivatedEwma2() {
            return mEwma2RangeSliderPane.selectedProperty().get();
        }

        private void load() {
        }

        private void reset() {
        }
    }
}
