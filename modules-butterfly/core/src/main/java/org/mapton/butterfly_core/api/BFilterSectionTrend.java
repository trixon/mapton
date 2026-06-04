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
import org.mapton.api.ui.forms.MBaseFilterSection;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.BTrendDirection;
import static org.mapton.butterfly_format.types.BTrendDirection.PARALLEL;
import org.mapton.butterfly_format.types.BTrendPeriod;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.SliderPane;
import se.trixon.almond.util.fx.session.SessionComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class BFilterSectionTrend<T extends BXyzPoint> extends MBaseFilterSection {

    private final ResourceBundle mBundle = NbBundle.getBundle(BFilterSectionTrend.class);
    private final SessionComboBox<BTrendDirection> mDirectionScb = new SessionComboBox<>();
    private TrendComponent mHeightComponent;
    private SessionComboBox<BTrendPeriod> mPeriodAbsScb;
    private SessionComboBox<BTrendPeriod> mPeriodRelScb;
    private TrendComponent mPlaneComponent;
    private final GridPane mRoot = new GridPane(columnGap, rowGap);

    public BFilterSectionTrend() {
        super("Trender");

        createUI();
        setContent(mRoot);
        mPeriodAbsScb.getSelectionModel().selectFirst();
        loadPeriodRelative();
    }

    @Override
    public void clear() {
        super.clear();
        mHeightComponent.clear();
        mPlaneComponent.clear();
        mPeriodAbsScb.getSelectionModel().selectFirst();
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
        var validPeriod1d = true;
        var validPeriod2d = true;
        var validDiffPrev1d = true;
        var validDiffPrev2d = true;
        var validDiffReference1d = true;
        var validDiffReference2d = true;
        var validRelAbs1d = true;
        var validRelAbs2d = true;

        if (isSelected()) {
            if (p.getDimension() != BDimension._2d) {
                if (mHeightComponent.isActivatedPeriod()) {
                    validPeriod1d = validatePeriod(p, mHeightComponent);
                }
                if (mHeightComponent.isActivatedDiffPrev()) {
                    validDiffPrev1d = validateDiffPrev(p, mHeightComponent);
                }
                if (mHeightComponent.isActivatedDiffReference()) {
                    validDiffReference1d = validateDiffReference(p, mHeightComponent);
                }
                if (mHeightComponent.isActivatedRel2()) {
                    validRelAbs1d = validateRelAbs(p, mHeightComponent);
                }
            }

            if (p.getDimension() != BDimension._1d) {
                if (mPlaneComponent.isActivatedPeriod()) {
                    validPeriod2d = validatePeriod(p, mPlaneComponent);
                }
                if (mPlaneComponent.isActivatedDiffReference()) {
                    validDiffReference2d = validateDiffReference(p, mPlaneComponent);
                }
                if (mPlaneComponent.isActivatedRel2()) {
                    validRelAbs2d = validateRelAbs(p, mPlaneComponent);
                }
            }
        }

        var valid = validPeriod1d
                && validPeriod2d
                && validDiffPrev1d
                && validDiffPrev2d
                && validDiffReference1d
                && validDiffReference2d
                && validRelAbs1d
                && validRelAbs2d
                && validateVerticalDirection(p);

        return valid;
    }

    public void initListeners(ChangeListener changeListener, ListChangeListener<Object> listChangeListener) {
        mPeriodAbsScb.valueProperty().addListener((p, o, n) -> {
            loadPeriodRelative();
        });

        List.of(
                selectedProperty(),
                mDirectionScb.valueProperty(),
                mPeriodAbsScb.getSelectionModel().selectedItemProperty(),
                mPeriodRelScb.getSelectionModel().selectedItemProperty()
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
        sessionManager.register(getKeyFilter("period1"), mPeriodAbsScb.selectedIndexProperty());
        sessionManager.register(getKeyFilter("period2"), mPeriodRelScb.selectedIndexProperty());
        sessionManager.register(getKeyFilter("trendDirection"), mDirectionScb.selectedIndexProperty());
        mHeightComponent.initSession(sessionManager);
        mPlaneComponent.initSession(sessionManager);
    }

    public void load() {
        mPeriodAbsScb.load();
        mPeriodRelScb.load();
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
        mPeriodAbsScb.getSelectionModel().selectFirst();
        mPeriodRelScb.getSelectionModel().selectFirst();
    }

    private void createUI() {
        mPeriodAbsScb = new SessionComboBox<>();
        mPeriodAbsScb.getItems().setAll(BTrendPeriod.values());
        mPeriodRelScb = new SessionComboBox<>();
        mHeightComponent = new TrendComponent(BComponent.HEIGHT);
        mPlaneComponent = new TrendComponent(BComponent.PLANE);
        mDirectionScb.getItems().setAll(BTrendDirection.values());

        int row = 0;
        mRoot.addRow(row++, new VBox(new Label("Trendperiod"), mPeriodAbsScb), new VBox(new Label("Trendreferens"), mPeriodRelScb));
        mRoot.addRow(row++, mHeightComponent, mPlaneComponent);
        mRoot.addRow(row++, new VBox(new Label("Riktning"), mDirectionScb), new VBox(new Label("")));
        FxHelper.autoSizeColumn(mRoot, 2);
        FxHelper.autoSizeRegionHorizontal(mPeriodAbsScb, mPeriodRelScb);
    }

    private void loadPeriodRelative() {
        mPeriodRelScb.getItems().clear();
        var afterSelected = false;
        for (var period : BTrendPeriod.values()) {
            if (afterSelected) {
                mPeriodRelScb.getItems().add(period);
            }
            if (!afterSelected && period == mPeriodAbsScb.getValue()) {
                afterSelected = true;
            }
        }

        mPeriodRelScb.getSelectionModel().selectFirst();
    }

    private boolean validateDiffPrev(BXyzPoint p, TrendComponent trendComponent) {
        var slider = trendComponent.mDiffPrevSliderPane;
        var trendPeriod = mPeriodAbsScb.getValue();

        HashMap<BTrendPeriod, TrendHelper.Trend> map1 = p.getValue(trendComponent.getKey());
        HashMap<BTrendPeriod, TrendHelper.Trend> map2 = p.getValue(trendComponent.getKey() + "Prev");
        if (ObjectUtils.anyNull(map1, map2)) {
            return false;
        }

        var trend1 = map1.get(trendPeriod);
        var trend2 = map2.get(trendPeriod);

        var diff = TrendHelper.getVelocityDiff(trend1, trend2);
        if (diff == null) {
            return false;
        }

        return validateSliderPaneGtEq(slider, Math.abs(diff) * 1);
    }

    private boolean validateDiffReference(BXyzPoint p, TrendComponent trendComponent) {
        var slider = trendComponent.mDiffReferenceSliderPane;
        var begPeriod = mPeriodRelScb.getValue();
        var endPeriod = mPeriodAbsScb.getValue();

        HashMap<BTrendPeriod, TrendHelper.Trend> map = p.getValue(trendComponent.getKey());
        if (map == null) {
            return false;
        }

        var begTrend = map.get(begPeriod);
        var endTrend = map.get(endPeriod);
        if (ObjectUtils.anyNull(begTrend, endTrend)) {
            return false;
        }

        var begValue = TrendHelper.getVelocity(begTrend);
        var endValue = TrendHelper.getVelocity(endTrend);
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

    private boolean validatePeriod(BXyzPoint p, TrendComponent trendComponent) {
        HashMap<BTrendPeriod, TrendHelper.Trend> map = p.getValue(trendComponent.getKey());
        if (map == null) {
            return false;
        }

        var trend = map.get(mPeriodAbsScb.getValue());
        if (trend == null) {
            return false;
        }

        var value = TrendHelper.getVelocity(trend);
        if (value == null) {
            return false;
        }

        return validateSliderPaneGtEq(trendComponent.mPeriodSliderPane, Math.abs(value));
    }

    private boolean validateRelAbs(BXyzPoint p, TrendComponent trendComponent) {
        var slider = trendComponent.mRelAbsSliderPane;
        var begPeriod = mPeriodRelScb.getValue();
        var endPeriod = mPeriodAbsScb.getValue();

        HashMap<BTrendPeriod, TrendHelper.Trend> map = p.getValue(trendComponent.getKey());
        if (map == null) {
            return false;
        }

        var begTrend = map.get(begPeriod);
        var endTrend = map.get(endPeriod);
        if (ObjectUtils.anyNull(begTrend, endTrend)) {
            return false;
        }

        var begValue = TrendHelper.getVelocity(begTrend);
        var endValue = TrendHelper.getVelocity(endTrend);
        if (ObjectUtils.anyNull(begValue, endValue)) {
            return false;
        }

        var diff = Math.abs(endValue - begValue);
        return validateSliderPaneLtEq(slider, diff);
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
        HashMap<BTrendPeriod, TrendHelper.Trend> map = p.getValue(BKey.TRENDS_H);
        if (map == null) {
            return false;
        }

        var trend = map.get(mPeriodAbsScb.getValue());
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

    class TrendComponent extends BorderPane {

        private final BComponent mComponent;
        private final SliderPane mDiffPrevSliderPane = new SliderPane("Min Diff föregående", 0, 100d, true, true, 1d);
        private final SliderPane mDiffReferenceSliderPane = new SliderPane("Min Diff referens", -100, 100d, true, true, 1d);
        private final SliderPane mPeriodSliderPane = new SliderPane("Min hastighet (mm/år)", 100, true, true, 1d);
        private final SliderPane mRelAbsSliderPane = new SliderPane("Max absolut differens", 0, 100d, true, true, 1d);

        public TrendComponent(BComponent component) {
            mComponent = component;
            createUI();
        }

        private void clear() {
            mPeriodSliderPane.clear();
            mDiffReferenceSliderPane.clear();
            mDiffPrevSliderPane.clear();
            mRelAbsSliderPane.clear();
        }

        private void createUI() {
            int rowGap = FxHelper.getUIScaled(8);

            var gp = new GridPane(rowGap, rowGap);
            int col = 0;
            gp.addColumn(col++,
                    mPeriodSliderPane,
                    mDiffPrevSliderPane,
                    mDiffReferenceSliderPane,
                    mRelAbsSliderPane
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
            return mComponent == BComponent.HEIGHT ? BKey.TRENDS_H : BKey.TRENDS_P;
        }

        private void initListeners(ChangeListener changeListener, ListChangeListener<Object> listChangeListener) {
            List.of(
                    mPeriodSliderPane.selectedProperty(),
                    mPeriodSliderPane.valueProperty(),
                    mDiffReferenceSliderPane.selectedProperty(),
                    mDiffReferenceSliderPane.valueProperty(),
                    mDiffPrevSliderPane.selectedProperty(),
                    mDiffPrevSliderPane.valueProperty(),
                    mRelAbsSliderPane.selectedProperty(),
                    mRelAbsSliderPane.valueProperty()
            ).forEach(propertyBase -> propertyBase.addListener(changeListener));
        }

        private void initSession(SessionManager sessionManager) {
            String mode = mComponent.getDimension().getName() + "_";
            mPeriodSliderPane.initSession(getKeyFilter(mode + "valuePeriod"), sessionManager);
            mDiffReferenceSliderPane.initSession(getKeyFilter(mode + "valueDiffReference"), sessionManager);
            mDiffPrevSliderPane.initSession(getKeyFilter(mode + "valueDiffPrev"), sessionManager);
            mRelAbsSliderPane.initSession(getKeyFilter(mode + "valueCompareAbs"), sessionManager);
        }

        private boolean isActivatedDiffPrev() {
            return mDiffPrevSliderPane.isSelected();
        }

        private boolean isActivatedDiffReference() {
            return mDiffReferenceSliderPane.isSelected() && !mPeriodRelScb.getItems().isEmpty();
        }

        private boolean isActivatedPeriod() {
            return mPeriodSliderPane.isSelected();
        }

        private boolean isActivatedRel2() {
            return mRelAbsSliderPane.isSelected() && !mPeriodRelScb.getItems().isEmpty();
        }

        private void load() {
        }

        private void reset() {
            mPeriodSliderPane.setSelected(false);
            mDiffReferenceSliderPane.setSelected(false);
            mDiffPrevSliderPane.setSelected(false);
            mRelAbsSliderPane.setSelected(false);
        }
    }
}
