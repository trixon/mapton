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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.controlsfx.tools.Borders;
import org.mapton.api.ui.forms.MBaseFilterSection;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.BTrendPeriod;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.SliderPane;
import se.trixon.almond.util.fx.session.SessionCheckBox;
import se.trixon.almond.util.fx.session.SessionComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class BFilterSectionTrend<T extends BXyzPoint> extends MBaseFilterSection {

    private DimensionSection m1dDimensionSection;
    private DimensionSection m2dDimensionSection;
    private final ResourceBundle mBundle = NbBundle.getBundle(BFilterSectionTrend.class);
    private final GridPane mRoot = new GridPane(columnGap, rowGap);

    public BFilterSectionTrend() {
        super("Trender");

        createUI();
        setContent(mRoot);
    }

    @Override
    public void clear() {
        super.clear();
        m1dDimensionSection.clear();
        m2dDimensionSection.clear();
    }

    @Override
    public void createInfoContent(LinkedHashMap<String, String> map) {
        if (!isSelected()) {
            return;
        }
        map.put(Dict.MISCELLANEOUS.toUpper(), "TODO");
    }

    public boolean filter(BXyzPoint p) {
        var valid1d = true;
        var valid2d = true;
        if (isSelected()) {
            if (m1dDimensionSection.isActivated() && p.getDimension() != BDimension._2d) {
                valid1d = validate1d(p);
            }
            if (m2dDimensionSection.isActivated() && p.getDimension() != BDimension._1d) {
                valid2d = validate2d(p);
            }
        }

        return valid1d && valid2d;
    }

    public void initListeners(ChangeListener changeListener, ListChangeListener<Object> listChangeListener) {
        System.out.println("initListeners ARG");
        List.of(
                selectedProperty()
        ).forEach(propertyBase -> propertyBase.addListener(changeListener));

        m1dDimensionSection.initListeners(changeListener, listChangeListener);
        m2dDimensionSection.initListeners(changeListener, listChangeListener);
    }

    public void initListeners(BFilterSectionTrendProvider filter) {
        System.out.println("initListeners");
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        setSessionManager(sessionManager);
        sessionManager.register(getKeyFilter("section"), selectedProperty());
        m1dDimensionSection.initSession(sessionManager);
        m2dDimensionSection.initSession(sessionManager);
    }

    public void load() {
        m1dDimensionSection.load();
        m2dDimensionSection.load();
    }

    @Override
    public void onShownFirstTime() {
    }

    @Override
    public void reset(PropertiesConfiguration filterConfig) {
        m1dDimensionSection.reset();
        m2dDimensionSection.reset();
    }

    private void createUI() {
        m1dDimensionSection = new DimensionSection(BDimension._1d);
        m2dDimensionSection = new DimensionSection(BDimension._2d);
        int row = 0;
        mRoot.addRow(row++, m1dDimensionSection, m2dDimensionSection);
        FxHelper.autoSizeColumn(mRoot, 2);
    }

    private boolean validate1d(BXyzPoint p) {
        if (m1dDimensionSection.mTrendAbsoluteSliderPane.isSelected()) {
            return validateAbsolute(p, BKey.TRENDS_H, m1dDimensionSection.mTrend1Scb, m1dDimensionSection.mTrendAbsoluteSliderPane);
        } else {
            return true;
        }
    }

    private boolean validate2d(BXyzPoint p) {
        if (m2dDimensionSection.mTrendAbsoluteSliderPane.isSelected()) {
            return validateAbsolute(p, BKey.TRENDS_P, m2dDimensionSection.mTrend1Scb, m2dDimensionSection.mTrendAbsoluteSliderPane);
        } else {
            return true;
        }
    }

    private boolean validateAbsolute(BXyzPoint p, String key, SessionComboBox<BTrendPeriod> scb, SliderPane sliderPane) {
        HashMap<BTrendPeriod, TrendHelper.Trend> map = p.getValue(key);
        if (map == null) {
            return false;
        }

        var trend = map.get(scb.getValue());
        if (trend == null) {
            return false;
        }

        var value = TrendHelper.getMmPerYear(trend);
        if (value == null) {
            return false;
        }

        return validateSliderPaneGtEq(sliderPane, Math.abs(value));
    }

    class DimensionSection extends BorderPane {

        private final SessionCheckBox mActiveScbx;
        private final BDimension mDimension;
        private final SessionComboBox<BTrendPeriod> mTrend1Scb;
        private final SessionComboBox<BTrendPeriod> mTrend2Scb;
        private final SliderPane mTrendAbsoluteSliderPane = new SliderPane("Minsta årshastighet (mm/år)", 200, true, true, 1d);
        private final SliderPane mTrendCompareSliderPane = new SliderPane("Jämförelse", 300d, true, true, 1d);

        public DimensionSection(BDimension dimension) {
            mDimension = dimension;
            mActiveScbx = new SessionCheckBox(mDimension.getName() + "d");
            mTrend1Scb = new SessionComboBox<>();
            mTrend1Scb.getItems().setAll(BTrendPeriod.values());
            mTrend2Scb = new SessionComboBox<>();
            createUI();
            mTrend1Scb.getSelectionModel().select(0);
            loadTrend2();
        }

        private void clear() {
            mTrendAbsoluteSliderPane.clear();
            mTrendCompareSliderPane.clear();
        }

        private void createUI() {
            setTop(mActiveScbx);
            int rowGap = FxHelper.getUIScaled(8);

            var gp = new GridPane(rowGap, rowGap);
            int col = 0;
            gp.addColumn(col++,
                    mTrendAbsoluteSliderPane,
                    mTrend1Scb,
                    mTrendCompareSliderPane,
                    mTrend2Scb
            );

            var borderNode = Borders.wrap(gp)
                    .etchedBorder()
                    //.title(mDimension.getName() + "d")
                    .innerPadding(mTopBorderInnerPadding, mBorderInnerPadding, mBorderInnerPadding, mBorderInnerPadding)
                    .outerPadding(FxHelper.getUIScaled(6.0), 0, 0, 0)
                    .raised()
                    .build()
                    .build();

            borderNode.disableProperty().bind(mActiveScbx.selectedProperty().not());
            FxHelper.autoSizeColumn(gp, 1);
            FxHelper.autoSizeRegionHorizontal(mTrend1Scb, mTrend2Scb);
            setCenter(borderNode);

            mTrend1Scb.disableProperty().bind(mTrendAbsoluteSliderPane.selectedProperty().not().and(mTrendCompareSliderPane.selectedProperty().not()));
            mTrend2Scb.disableProperty().bind(mTrendCompareSliderPane.selectedProperty().not());
        }

        private void initListeners(ChangeListener changeListener, ListChangeListener<Object> listChangeListener) {
            mTrend1Scb.valueProperty().addListener((p, o, n) -> {
                if (mTrendCompareSliderPane.isSelected()) {
                    loadTrend2();
                }
            });

            List.of(
                    mActiveScbx.selectedProperty(),
                    mTrendAbsoluteSliderPane.selectedProperty(),
                    mTrendAbsoluteSliderPane.valueProperty(),
                    mTrendCompareSliderPane.selectedProperty(),
                    mTrendCompareSliderPane.valueProperty(),
                    mTrend1Scb.getSelectionModel().selectedItemProperty(),
                    mTrend2Scb.getSelectionModel().selectedItemProperty()
            ).forEach(propertyBase -> propertyBase.addListener(changeListener));
        }

        private void initSession(SessionManager sessionManager) {
            String mode = mDimension.getName() + "_";
            sessionManager.register(getKeyFilter(mode + "active"), mActiveScbx.selectedProperty());
            mTrendAbsoluteSliderPane.initSession(getKeyFilter(mode + "valueAbsolute"), sessionManager);
            mTrendCompareSliderPane.initSession(getKeyFilter(mode + "valueCompare"), sessionManager);
            sessionManager.register(getKeyFilter(mode + "period1"), mTrend1Scb.selectedIndexProperty());
            sessionManager.register(getKeyFilter(mode + "period2"), mTrend2Scb.selectedIndexProperty());
        }

        private boolean isActivated() {
            return mActiveScbx.isSelected();
        }

        private void load() {
            mTrend1Scb.load();
            mTrend2Scb.load();
        }

        private void loadTrend2() {
            mTrend2Scb.getItems().clear();
            var afterSelected = false;
            for (var period : BTrendPeriod.values()) {
                if (afterSelected) {
                    mTrend2Scb.getItems().add(period);
                }
                if (!afterSelected && period == mTrend1Scb.getValue()) {
                    afterSelected = true;
                }
            }
//
            mTrend2Scb.getSelectionModel().selectFirst();
        }

        private void reset() {
            mTrend1Scb.getSelectionModel().selectFirst();
            mTrend2Scb.getSelectionModel().selectFirst();
            mActiveScbx.setSelected(false);
            mTrendAbsoluteSliderPane.setSelected(false);
            mTrendCompareSliderPane.setSelected(false);
        }
    }
}
