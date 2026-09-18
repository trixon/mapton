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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.lang3.ObjectUtils;
import org.controlsfx.tools.Borders;
import org.mapton.api.MAvgPeriod;
import org.mapton.api.ui.forms.MBaseFilterSection;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.BTrendDirection;
import static org.mapton.butterfly_format.types.BTrendDirection.PARALLEL;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.RangeSliderPane;
import se.trixon.almond.util.fx.session.SessionComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class BFilterSectionAvg<T extends BXyzPoint> extends MBaseFilterSection {

    private final ResourceBundle mBundle = NbBundle.getBundle(BFilterSectionAvg.class);
    private final SessionComboBox<BTrendDirection> mDirectionScb = new SessionComboBox<>();
    private SessionComboBox<MAvgPeriod> mEwma1PeriodScb;
    private SessionComboBox<MAvgPeriod> mEwma2PeriodScb;
    private AvgComponent mHeightComponent;
    private AvgComponent mPlaneComponent;
    private final GridPane mRoot = new GridPane(columnGap, rowGap);

    public BFilterSectionAvg() {
        super("Medel");

        createUI();
        setContent(mRoot);
        mEwma1PeriodScb.getSelectionModel().selectFirst();
        mEwma2PeriodScb.getSelectionModel().selectLast();
    }

    @Override
    public void clear() {
        super.clear();
        mHeightComponent.clear();
        mPlaneComponent.clear();
        mEwma1PeriodScb.getSelectionModel().selectFirst();
        mEwma2PeriodScb.getSelectionModel().selectLast();
        mDirectionScb.getSelectionModel().selectFirst();
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
                    validActivity1d = validateDiffReference(p, mHeightComponent);
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
                    validActivity2d = validateDiffReference(p, mPlaneComponent);
                }
            }
        }

        var valid = true
                && validEwma1_1d
                && validEwma2_1d
                && validEwma1_2d
                && validEwma2_2d
                && validActivity1d
                && validActivity2d;

        return valid;
    }

    public void initListeners(ChangeListener changeListener, ListChangeListener<Object> listChangeListener) {
        List.of(
                selectedProperty(),
                mDirectionScb.valueProperty(),
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
        sessionManager.register(getKeyFilter("ewmaDirection"), mDirectionScb.selectedIndexProperty());
        mHeightComponent.initSession(sessionManager);
        mPlaneComponent.initSession(sessionManager);
    }

    public void load() {
        mEwma1PeriodScb.load();
        mEwma2PeriodScb.load();
        mHeightComponent.load();
        mPlaneComponent.load();
        mDirectionScb.load();
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
        mDirectionScb.getItems().setAll(BTrendDirection.values());

        int row = 0;
        mRoot.addRow(row++, new VBox(new Label("EWMA 1 (Kort)"), mEwma1PeriodScb), new VBox(new Label("EWMA 2 (Lång)"), mEwma2PeriodScb));
        mRoot.add(mHeightComponent, 0, row++, GridPane.REMAINING, 1);
        mRoot.add(mPlaneComponent, 0, row++, GridPane.REMAINING, 1);
        mRoot.addRow(row++, new VBox(new Label("Riktning"), mDirectionScb), new VBox(new Label("")));
        FxHelper.autoSizeColumn(mRoot, 2);
        FxHelper.autoSizeRegionHorizontal(mEwma1PeriodScb, mEwma2PeriodScb);

        mDirectionScb.setDisable(true);
    }

    private boolean validateDiffReference(BXyzPoint p, AvgComponent avgComponent) {
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

    private boolean validateVerticalDirection(BXyzPoint p) {
        if (mDirectionScb.getValue() == BTrendDirection.EITHER) {
            return true;
        }
        if (p.getDimension() == BDimension._2d) {
            return false;
        }
        var dZ = p.extOrNull().deltaZero().getDelta1();
        if (dZ == null) {
            return false;
        }
        HashMap<MAvgPeriod, TrendHelper.Trend> map = p.getValue(BKey.TRENDS_H);
        if (map == null) {
            return false;
        }

        var trend = map.get(mEwma1PeriodScb.getValue());
        if (trend == null) {
            return false;
        }

        var value = TrendHelper.getVelocity(trend);
        if (value == null) {
            return false;
        }

        var posTrend = value >= 0d;
        var posDelta = dZ >= 0d;
        var closeToZero = Math.abs(value) < 0.1;

        switch (mDirectionScb.getValue()) {
            case CONVERGENT:
                return posTrend != posDelta;
            case DIVERGENT:
                return posTrend == posDelta;
            case TRIVIAL:
                return Math.abs(value) < 2 && !closeToZero;
            case PARALLEL:
                return closeToZero;
            default:
                throw new AssertionError();
        }
    }

    class AvgComponent extends BorderPane {

        private final RangeSliderPane mActivityRangeSliderPane;
        private final BComponent mComponent;
        private final RangeSliderPane mEwma1RangeSliderPane;
        private final RangeSliderPane mEwma2RangeSliderPane;

        public AvgComponent(BComponent component) {
            var maxEwma = 50;
            var minEwma = component == BComponent.HEIGHT ? -maxEwma : 0;
            mEwma1RangeSliderPane = new RangeSliderPane("EWMA 1", minEwma, maxEwma, true, 1.0);
            mEwma2RangeSliderPane = new RangeSliderPane("EWMA 2", minEwma, maxEwma, true, 1.0);
            mActivityRangeSliderPane = new RangeSliderPane("Aktivitet (EWMA 1- EWMA 2)", -maxEwma, maxEwma, true, 1.0);
            mComponent = component;
            createUI();
        }

        private void clear() {
            mEwma1RangeSliderPane.clear();
            mEwma2RangeSliderPane.clear();
            mActivityRangeSliderPane.clear();
        }

        private void createUI() {
            var rowGap = FxHelper.getUIScaled(8);
            var gp = new GridPane(rowGap, rowGap);
            var rangeSliders = List.of(
                    mEwma1RangeSliderPane,
                    mEwma2RangeSliderPane,
                    mActivityRangeSliderPane
            );

            rangeSliders.forEach(rangeSlider -> {
                rangeSlider.setInvertIncluded(true);
                FxHelper.autoSizeRegionHorizontal(rangeSlider);
            });

            gp.addColumn(0,
                    rangeSliders.toArray(RangeSliderPane[]::new)
            );

            var borderNode = Borders.wrap(gp)
                    .etchedBorder()
                    .title(mComponent.getDimension().getName() + "d")
                    .innerPadding(mTopBorderInnerPadding, mBorderInnerPadding, mBorderInnerPadding, mBorderInnerPadding)
                    .outerPadding(FxHelper.getUIScaled(6.0), 0, 0, 0)
                    .raised()
                    .build()
                    .build();

            FxHelper.autoSizeColumn(gp, 1);
            setCenter(borderNode);
        }

        private String getKey() {
            return mComponent == BComponent.HEIGHT ? BKey.EWMA_H : BKey.EWMA_P;
        }

        private void initListeners(ChangeListener changeListener, ListChangeListener<Object> listChangeListener) {
            List.of(
                    mEwma1RangeSliderPane,
                    mEwma2RangeSliderPane,
                    mActivityRangeSliderPane
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
        }

        private boolean isActivatedActivity() {
            return mActivityRangeSliderPane.selectedProperty().get();
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
