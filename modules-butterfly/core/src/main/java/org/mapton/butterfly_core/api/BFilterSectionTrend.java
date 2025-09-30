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
import org.apache.commons.lang3.ObjectUtils;
import org.controlsfx.tools.Borders;
import org.mapton.api.ui.forms.MBaseFilterSection;
import org.mapton.butterfly_format.types.BComponent;
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

    private final ResourceBundle mBundle = NbBundle.getBundle(BFilterSectionTrend.class);
    private TrendComponent mHeightComponent;
    private TrendComponent mPlaneComponent;
    private final GridPane mRoot = new GridPane(columnGap, rowGap);

    public BFilterSectionTrend() {
        super("Trender");

        createUI();
        setContent(mRoot);
    }

    @Override
    public void clear() {
        super.clear();
        mHeightComponent.clear();
        mPlaneComponent.clear();
    }

    @Override
    public void createInfoContent(LinkedHashMap<String, String> map) {
        if (!isSelected()) {
            return;
        }
        map.put(Dict.MISCELLANEOUS.toUpper(), "TODO");
    }

    public boolean filter(BXyzPoint p) {
        var validAbs1d = true;
        var validAbs2d = true;
        var validRel1d = true;
        var validRel2d = true;

        if (isSelected()) {
            if (p.getDimension() != BDimension._2d) {
                if (mHeightComponent.isActivatedAbs()) {
                    validAbs1d = validateAbs(p, mHeightComponent);
                }
                if (mHeightComponent.isActivatedRel()) {
                    validRel1d = validateRel(p, mHeightComponent);
                }
            }

            if (p.getDimension() != BDimension._1d) {
                if (mPlaneComponent.isActivatedAbs()) {
                    validAbs2d = validateAbs(p, mPlaneComponent);
                }
                if (mPlaneComponent.isActivatedRel()) {
                    validRel2d = validateRel(p, mPlaneComponent);
                }
            }
        }

        var valid = validAbs1d && validAbs2d && validRel1d && validRel2d;

        return valid;
    }

    public void initListeners(ChangeListener changeListener, ListChangeListener<Object> listChangeListener) {
        List.of(
                selectedProperty()
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
        mHeightComponent.initSession(sessionManager);
        mPlaneComponent.initSession(sessionManager);
    }

    public void load() {
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
    }

    private void createUI() {
        mHeightComponent = new TrendComponent(BComponent.HEIGHT);
        mPlaneComponent = new TrendComponent(BComponent.PLANE);
        int row = 0;
        mRoot.addRow(row++, mHeightComponent, mPlaneComponent);
        FxHelper.autoSizeColumn(mRoot, 2);
    }

    private boolean validateAbs(BXyzPoint p, TrendComponent trendComponent) {
        HashMap<BTrendPeriod, TrendHelper.Trend> map = p.getValue(trendComponent.getKey());
        if (map == null) {
            return false;
        }

        var trend = map.get(trendComponent.mAbsPeriodScb.getValue());
        if (trend == null) {
            return false;
        }

        var value = TrendHelper.getMmPerYear(trend);
        if (value == null) {
            return false;
        }

        return validateSliderPaneGtEq(trendComponent.mAbsSliderPane, Math.abs(value));
    }

    private boolean validateRel(BXyzPoint p, TrendComponent trendComponent) {
        var slider = trendComponent.mRelSliderPane;
        var begPeriod = trendComponent.mRelPeriodScb.getValue();
        var endPeriod = trendComponent.mAbsPeriodScb.getValue();

        HashMap<BTrendPeriod, TrendHelper.Trend> map = p.getValue(trendComponent.getKey());
        if (map == null) {
            return false;
        }

        var begTrend = map.get(begPeriod);
        var endTrend = map.get(endPeriod);
        if (ObjectUtils.anyNull(begTrend, endTrend)) {
            return false;
        }

        var begValue = TrendHelper.getMmPerYear(begTrend);
        var endValue = TrendHelper.getMmPerYear(endTrend);
        if (ObjectUtils.anyNull(begValue, endValue)) {
            return false;
        }

        var diff = endValue - begValue;
        if (slider.valueProperty().get() < 0) {
            return validateSliderPaneLtEq(slider, diff);
        } else {
            return validateSliderPaneGtEq(slider, diff);
        }
    }

    class TrendComponent extends BorderPane {

        private final SessionComboBox<BTrendPeriod> mAbsPeriodScb;
        private final SliderPane mAbsSliderPane = new SliderPane("Årshastighet (mm/år)", 100, true, true, 1d);
        private final SessionCheckBox mActiveScbx;
        private final BComponent mComponent;
        private final SessionComboBox<BTrendPeriod> mRelPeriodScb;
        private final SliderPane mRelSliderPane = new SliderPane("Differens", -100, 100d, true, true, 1d);

        public TrendComponent(BComponent component) {
            mComponent = component;
            mActiveScbx = new SessionCheckBox(mComponent.getDimension().getName() + "d");
            mAbsPeriodScb = new SessionComboBox<>();
            mAbsPeriodScb.getItems().setAll(BTrendPeriod.values());
            mRelPeriodScb = new SessionComboBox<>();
            createUI();
            mAbsPeriodScb.getSelectionModel().select(0);
            loadTrend2();
        }

        private void clear() {
            mAbsSliderPane.clear();
            mRelSliderPane.clear();
        }

        private void createUI() {
            setTop(mActiveScbx);
            int rowGap = FxHelper.getUIScaled(8);

            var gp = new GridPane(rowGap, rowGap);
            int col = 0;
            gp.addColumn(col++,
                    mAbsSliderPane,
                    mAbsPeriodScb,
                    mRelSliderPane,
                    mRelPeriodScb
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
            FxHelper.autoSizeRegionHorizontal(mAbsPeriodScb, mRelPeriodScb);
            setCenter(borderNode);

            mAbsPeriodScb.disableProperty().bind(mAbsSliderPane.selectedProperty().not().and(mRelSliderPane.selectedProperty().not()));
            mRelPeriodScb.disableProperty().bind(mRelSliderPane.selectedProperty().not());
        }

        private String getKey() {
            return mComponent == BComponent.HEIGHT ? BKey.TRENDS_H : BKey.TRENDS_P;
        }

        private void initListeners(ChangeListener changeListener, ListChangeListener<Object> listChangeListener) {
            mAbsPeriodScb.valueProperty().addListener((p, o, n) -> {
                if (mRelSliderPane.isSelected()) {
                    loadTrend2();
                }
            });

            List.of(
                    mActiveScbx.selectedProperty(),
                    mAbsSliderPane.selectedProperty(),
                    mAbsSliderPane.valueProperty(),
                    mRelSliderPane.selectedProperty(),
                    mRelSliderPane.valueProperty(),
                    mAbsPeriodScb.getSelectionModel().selectedItemProperty(),
                    mRelPeriodScb.getSelectionModel().selectedItemProperty()
            ).forEach(propertyBase -> propertyBase.addListener(changeListener));
        }

        private void initSession(SessionManager sessionManager) {
            String mode = mComponent.getDimension().getName() + "_";
            sessionManager.register(getKeyFilter(mode + "active"), mActiveScbx.selectedProperty());
            mAbsSliderPane.initSession(getKeyFilter(mode + "valueAbsolute"), sessionManager);
            mRelSliderPane.initSession(getKeyFilter(mode + "valueCompare"), sessionManager);
            sessionManager.register(getKeyFilter(mode + "period1"), mAbsPeriodScb.selectedIndexProperty());
            sessionManager.register(getKeyFilter(mode + "period2"), mRelPeriodScb.selectedIndexProperty());
        }

        private boolean isActivated() {
            return mActiveScbx.isSelected();
        }

        private boolean isActivatedAbs() {
            return isActivated() && mAbsSliderPane.isSelected();
        }

        private boolean isActivatedRel() {
            return isActivated() && mRelSliderPane.isSelected() && !mRelPeriodScb.getItems().isEmpty();
        }

        private void load() {
            mAbsPeriodScb.load();
            mRelPeriodScb.load();
        }

        private void loadTrend2() {
            mRelPeriodScb.getItems().clear();
            var afterSelected = false;
            for (var period : BTrendPeriod.values()) {
                if (afterSelected) {
                    mRelPeriodScb.getItems().add(period);
                }
                if (!afterSelected && period == mAbsPeriodScb.getValue()) {
                    afterSelected = true;
                }
            }
//
            mRelPeriodScb.getSelectionModel().selectFirst();
        }

        private void reset() {
            mAbsPeriodScb.getSelectionModel().selectFirst();
            mRelPeriodScb.getSelectionModel().selectFirst();
            mActiveScbx.setSelected(false);
            mAbsSliderPane.setSelected(false);
            mRelSliderPane.setSelected(false);
        }
    }
}
