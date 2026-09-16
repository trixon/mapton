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
import se.trixon.almond.util.fx.control.SliderPane;
import se.trixon.almond.util.fx.session.SessionComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class BFilterSectionAvg<T extends BXyzPoint> extends MBaseFilterSection {

    private final ResourceBundle mBundle = NbBundle.getBundle(BFilterSectionAvg.class);
    private final SessionComboBox<BTrendDirection> mDirectionScb = new SessionComboBox<>();
    private AvgComponent mHeightComponent;
    private SessionComboBox<MAvgPeriod> mPeriod1Scb;
    private SessionComboBox<MAvgPeriod> mPeriod2Scb;
    private AvgComponent mPlaneComponent;
    private final GridPane mRoot = new GridPane(columnGap, rowGap);

    public BFilterSectionAvg() {
        super("Medel");

        createUI();
        setContent(mRoot);
        mPeriod1Scb.getSelectionModel().selectFirst();
        mPeriod2Scb.getSelectionModel().selectLast();
//        loadPeriodRelative();
    }

    @Override
    public void clear() {
        super.clear();
        mHeightComponent.clear();
        mPlaneComponent.clear();
        mPeriod1Scb.getSelectionModel().selectFirst();
        mPeriod2Scb.getSelectionModel().selectLast();
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
        var validDiffReference1d = true;
        var validDiffReference2d = true;
        var validRelAbs1d = true;
        var validRelAbs2d = true;

        if (isSelected()) {
            if (p.getDimension() != BDimension._2d) {
                if (mHeightComponent.isActivatedPeriod()) {
                    validPeriod1d = validatePeriod(p, mHeightComponent);
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
                && validDiffReference1d
                && validDiffReference2d
                && validRelAbs1d
                && validRelAbs2d //                && validateVerticalDirection(p)
                ;

        return valid;
    }

    public void initListeners(ChangeListener changeListener, ListChangeListener<Object> listChangeListener) {
        List.of(
                selectedProperty(),
                mDirectionScb.valueProperty(),
                mPeriod1Scb.getSelectionModel().selectedItemProperty(),
                mPeriod2Scb.getSelectionModel().selectedItemProperty()
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
        sessionManager.register(getKeyFilter("period1"), mPeriod1Scb.selectedIndexProperty());
        sessionManager.register(getKeyFilter("period2"), mPeriod2Scb.selectedIndexProperty());
        sessionManager.register(getKeyFilter("ewmaDirection"), mDirectionScb.selectedIndexProperty());
        mHeightComponent.initSession(sessionManager);
        mPlaneComponent.initSession(sessionManager);
    }

    public void load() {
        mPeriod1Scb.load();
        mPeriod2Scb.load();
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
        mPeriod1Scb.getSelectionModel().selectFirst();
        mPeriod2Scb.getSelectionModel().selectFirst();
    }

    private void createUI() {
        mPeriod1Scb = new SessionComboBox<>();
        mPeriod1Scb.getItems().setAll(MAvgPeriod.values());
        mPeriod2Scb = new SessionComboBox<>();
        mPeriod2Scb.getItems().setAll(MAvgPeriod.values());
        mHeightComponent = new AvgComponent(BComponent.HEIGHT);
        mPlaneComponent = new AvgComponent(BComponent.PLANE);
        mDirectionScb.getItems().setAll(BTrendDirection.values());

        int row = 0;
        mRoot.addRow(row++, new VBox(new Label("EWMA-period"), mPeriod1Scb), new VBox(new Label("EWMA-referens"), mPeriod2Scb));
        mRoot.addRow(row++, mHeightComponent, mPlaneComponent);
        mRoot.addRow(row++, new VBox(new Label("Riktning"), mDirectionScb), new VBox(new Label("")));
        FxHelper.autoSizeColumn(mRoot, 2);
        FxHelper.autoSizeRegionHorizontal(mPeriod1Scb, mPeriod2Scb);
    }

    private boolean validateDiffReference(BXyzPoint p, AvgComponent avgComponent) {
        var slider = avgComponent.mDiffReferenceSliderPane;
        var period1 = mPeriod1Scb.getValue();
        var period2 = mPeriod2Scb.getValue();

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
        if (slider.valueProperty().get() < 0) {
            return validateSliderPaneLtEq(slider, diff);
        } else {
            return validateSliderPaneGtEq(slider, diff);
        }
    }

    private boolean validatePeriod(BXyzPoint p, AvgComponent avgComponent) {
        HashMap<MAvgPeriod, EwmaHelper.Ewma> map = p.getValue(avgComponent.getKey());
        if (map == null) {
            return false;
        }

        var ewma = map.get(mPeriod1Scb.getValue());
        if (ewma == null) {
            return false;
        }

        var value = ewma.getLastValue();
//        if (value == null) {
//            return false;
//        }

        return validateSliderPaneGtEq(avgComponent.mPeriodSliderPane, Math.abs(value));
    }

    private boolean validateRelAbs(BXyzPoint p, AvgComponent avgComponent) {
        var slider = avgComponent.mRelAbsSliderPane;
        var period1 = mPeriod1Scb.getValue();
        var period2 = mPeriod2Scb.getValue();

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

        var diff = Math.abs(value1 - value2);
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
        HashMap<MAvgPeriod, TrendHelper.Trend> map = p.getValue(BKey.TRENDS_H);
        if (map == null) {
            return false;
        }

        var trend = map.get(mPeriod1Scb.getValue());
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

        private final BComponent mComponent;
        private final SliderPane mDiffReferenceSliderPane = new SliderPane("Min Aktivitet (EWMA1-EWMA2)", -100, 100d, true, true, 1d);
        private final SliderPane mPeriodSliderPane = new SliderPane("Min värde", 100, true, true, 1d);
        private final SliderPane mRelAbsSliderPane = new SliderPane("Styrka :: Abs(Aktivitet)", 0, 100d, true, true, 1d);

        public AvgComponent(BComponent component) {
            mComponent = component;
            createUI();
        }

        private void clear() {
            mPeriodSliderPane.clear();
            mDiffReferenceSliderPane.clear();
            mRelAbsSliderPane.clear();
        }

        private void createUI() {
            int rowGap = FxHelper.getUIScaled(8);

            var gp = new GridPane(rowGap, rowGap);
            int col = 0;
            gp.addColumn(col++,
                    mPeriodSliderPane,
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
            return mComponent == BComponent.HEIGHT ? BKey.EWMA_H : BKey.EWMA_P;
        }

        private void initListeners(ChangeListener changeListener, ListChangeListener<Object> listChangeListener) {
            List.of(
                    mPeriodSliderPane.selectedProperty(),
                    mPeriodSliderPane.valueProperty(),
                    mDiffReferenceSliderPane.selectedProperty(),
                    mDiffReferenceSliderPane.valueProperty(),
                    mRelAbsSliderPane.selectedProperty(),
                    mRelAbsSliderPane.valueProperty()
            ).forEach(propertyBase -> propertyBase.addListener(changeListener));
        }

        private void initSession(SessionManager sessionManager) {
            String mode = mComponent.getDimension().getName() + "_";
            mPeriodSliderPane.initSession(getKeyFilter(mode + "valuePeriod"), sessionManager);
            mDiffReferenceSliderPane.initSession(getKeyFilter(mode + "valueDiffReference"), sessionManager);
            mRelAbsSliderPane.initSession(getKeyFilter(mode + "valueCompareAbs"), sessionManager);
        }

//        private boolean isActivatedDiffPrev() {
//            return mDiffPrevSliderPane.isSelected();
//        }
        private boolean isActivatedDiffReference() {
            return mDiffReferenceSliderPane.isSelected() && !mPeriod2Scb.getItems().isEmpty();
        }

        private boolean isActivatedPeriod() {
            return mPeriodSliderPane.isSelected();
        }

        private boolean isActivatedRel2() {
            return mRelAbsSliderPane.isSelected() && !mPeriod2Scb.getItems().isEmpty();
        }

        private void load() {
        }

        private void reset() {
            mPeriodSliderPane.setSelected(false);
            mDiffReferenceSliderPane.setSelected(false);
            mRelAbsSliderPane.setSelected(false);
        }
    }
}
