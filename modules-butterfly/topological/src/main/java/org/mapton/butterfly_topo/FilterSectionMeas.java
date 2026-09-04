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
package org.mapton.butterfly_topo;

import com.dlsc.gemsfx.util.SessionManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javax.swing.SortOrder;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Strings;
import org.mapton.api.ui.forms.FormHelper;
import org.mapton.api.ui.forms.MBaseFilterSection;
import org.mapton.api.ui.forms.NegPosStringConverterDouble;
import org.mapton.butterfly_core.api.AlarmLevelChangeUnit;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_format.types.topo.BTopoControlPointObservation;
import org.openide.util.NbBundle;
import se.trixon.almond.util.CollectionHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.RangeSliderPane;
import se.trixon.almond.util.fx.session.SessionComboBox;
import se.trixon.almond.util.fx.session.SessionDoubleSpinner;
import se.trixon.almond.util.fx.session.SessionIntegerSpinner;

/**
 *
 * @author Patrik Karlström
 */
public class FilterSectionMeas extends MBaseFilterSection {

    private final RangeSliderPane mBearingRangeSlider = new RangeSliderPane(Dict.BEARING.toString(), -90.0, 360.0, false);
    private final DateDiffPane mDateDiffPane;
    private final double mDefaultDiffValue = 0.020;
    private final int mDefaultTopListLimit = 14;
    private final int mDefaultTopListSize = 10;
    private final double mDefaultYoyoCount = 5.0;
    private final double mDefaultYoyoSize = 0.003;
    private final CheckBox mDiffAllCheckbox = new CheckBox();
    private final SessionDoubleSpinner mDiffAllSds = new SessionDoubleSpinner(-1.0, 1.0, mDefaultDiffValue, 0.001);
    private final CheckBox mDiffLatestCheckbox = new CheckBox();
    private final SessionDoubleSpinner mDiffLatestSds = new SessionDoubleSpinner(-1.0, 1.0, mDefaultDiffValue, 0.001);
    private VBox mRoot;
    private final CheckBox mTopListCheckbox = new CheckBox();
    private final SessionIntegerSpinner mTopListLimitSis = new SessionIntegerSpinner(0, Integer.MAX_VALUE, mDefaultTopListLimit);
    private final SessionIntegerSpinner mTopListSizeSds = new SessionIntegerSpinner(1, 100, mDefaultTopListSize, 1);
    private final SessionComboBox<AlarmLevelChangeUnit> mTopListUnitScb = new SessionComboBox<>();
    private final CheckBox mYoyoCheckbox = new CheckBox();
    private final SessionDoubleSpinner mYoyoCountSds = new SessionDoubleSpinner(0, 100.0, mDefaultYoyoCount, 1.0);
    private final SessionDoubleSpinner mYoyoSizeSds = new SessionDoubleSpinner(0, 1.0, mDefaultYoyoSize, 0.001);

    public FilterSectionMeas() {
        super(SDict.MEASUREMENTS.toString() + "*");
        mDateDiffPane = new DateDiffPane(this);
        createUI();
    }

    @Override
    public void clear() {
        super.clear();
        FxHelper.setSelected(false,
                mDiffLatestCheckbox,
                mDiffAllCheckbox,
                mYoyoCheckbox,
                mTopListCheckbox
        );
        mDiffAllSds.getValueFactory().setValue(mDefaultDiffValue);
        mDiffLatestSds.getValueFactory().setValue(mDefaultDiffValue);
        mTopListLimitSis.getValueFactory().setValue(mDefaultTopListLimit);
        mTopListSizeSds.getValueFactory().setValue(mDefaultTopListSize);
        mYoyoCountSds.getValueFactory().setValue(mDefaultYoyoCount);
        mYoyoSizeSds.getValueFactory().setValue(mDefaultYoyoSize);
        mBearingRangeSlider.clear();
    }

    @Override
    public void createInfoContent(LinkedHashMap<String, String> map) {
        if (!isSelected()) {
            return;
        }
        try {

            if (mDiffAllCheckbox.isSelected()) {
                map.put(getBundle().getString("diffMeasAllCheckBoxText"), FormHelper.negPosToLtGt(mDiffAllSds.getValue()));
            }

            if (mDiffLatestCheckbox.isSelected()) {
                map.put(getBundle().getString("diffMeasLatestCheckBoxText"), FormHelper.negPosToLtGt(mDiffLatestSds.getValue()));
            }
        } catch (NullPointerException e) {
        }
    }

    public boolean filter(BTopoControlPoint p) {
        var valid = true
                && validateMeasDisplacementAll(p)
                && validateMeasDisplacementLatest(p)
                //                                && validateMeasDateDiff(p)
                && validateMeasYoyo(p)
                && validateMeasBearing(p)
                && true;

        return valid;
    }

    public ResourceBundle getBundle() {
        return NbBundle.getBundle(getClass());
    }

    public Region getRoot() {
        return mRoot;
    }

    public void initListeners(ChangeListener changeListenerObject, ListChangeListener<Object> listChangeListener) {
        List.of(
                selectedProperty(),
                mDiffAllCheckbox.selectedProperty(),
                mTopListCheckbox.selectedProperty(),
                mYoyoCheckbox.selectedProperty(),
                mDiffLatestCheckbox.selectedProperty(),
                mDiffAllSds.sessionValueProperty(),
                mYoyoCountSds.sessionValueProperty(),
                mTopListSizeSds.sessionValueProperty(),
                mYoyoSizeSds.sessionValueProperty(),
                mDiffLatestSds.sessionValueProperty(),
                mTopListUnitScb.getSelectionModel().selectedItemProperty(),
                mTopListLimitSis.sessionValueProperty(),
                mBearingRangeSlider.selectedProperty(),
                mBearingRangeSlider.minProperty(),
                mBearingRangeSlider.maxProperty()
        ).forEach(propertyBase -> propertyBase.addListener(changeListenerObject));

//        List.of(
//                mInstrumentSccb.getCheckModel(),
//                mOperatorSccb.getCheckModel(),
//                mCodeSccb.getCheckModel(),
//        ).forEach(cm -> cm.getCheckedItems().addListener(listChangeListener));
//        mDateDiffPane.initListeners(filter);
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        sessionManager.register(getKeyFilter("section"), selectedProperty());
        sessionManager.register(getKeyFilter("diffAll"), mDiffAllCheckbox.selectedProperty());
        sessionManager.register(getKeyFilter("topList"), mTopListCheckbox.selectedProperty());
        sessionManager.register(getKeyFilter("topListSizeValue"), mTopListSizeSds.sessionValueProperty());
        sessionManager.register(getKeyFilter("topListUnit"), mTopListUnitScb.selectedIndexProperty());
        sessionManager.register(getKeyFilter("topListLimit"), mTopListLimitSis.sessionValueProperty());
        sessionManager.register(getKeyFilter("yoyo"), mYoyoCheckbox.selectedProperty());
        sessionManager.register(getKeyFilter("diffAllValue"), mDiffAllSds.sessionValueProperty());
        sessionManager.register(getKeyFilter("yoyoCountValue"), mYoyoCountSds.sessionValueProperty());
        sessionManager.register(getKeyFilter("yoyoSizeValue"), mYoyoSizeSds.sessionValueProperty());
        sessionManager.register(getKeyFilter("diffLatest"), mDiffLatestCheckbox.selectedProperty());
        sessionManager.register(getKeyFilter("diffLatestValue"), mDiffLatestSds.sessionValueProperty());
        mBearingRangeSlider.initSession(getKeyFilter("bearing"), sessionManager);
        mDateDiffPane.initSession(sessionManager);
    }

    @Override
    public void onShownFirstTime() {
    }

    @Override
    public void reset(PropertiesConfiguration filterConfig) {
    }

    public boolean shouldCreateTopList() {
        return mTopListCheckbox.isSelected();
    }

    List<BTopoControlPoint> createTopList(List<BTopoControlPoint> filteredItems) {
        var topListMaxSize = mTopListSizeSds.getValue();
        var limit = mTopListLimitSis.getValue();
        var unit = mTopListUnitScb.getValue();
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

            if (limitedObservations.size() >= 2 && ObjectUtils.allNotNull(limitedObservations.getFirst().ext().getDelta(), limitedObservations.getLast().ext().getDelta())) {
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

    void load(ArrayList<BTopoControlPoint> items) {
        mTopListUnitScb.load();
        mTopListLimitSis.load();
        mDiffLatestSds.load();
        mDiffAllSds.load();
        mYoyoCountSds.load();
        mYoyoSizeSds.load();
        mTopListSizeSds.load();
        mDiffAllSds.disableProperty().bind(mDiffAllCheckbox.selectedProperty().not());
        mDiffLatestSds.disableProperty().bind(mDiffLatestCheckbox.selectedProperty().not());
        mTopListSizeSds.disableProperty().bind(mTopListCheckbox.selectedProperty().not());
        mYoyoCountSds.disableProperty().bind(mYoyoCheckbox.selectedProperty().not());
        mYoyoSizeSds.disableProperty().bind(mYoyoCheckbox.selectedProperty().not());
        mTopListLimitSis.disableProperty().bind(mTopListCheckbox.selectedProperty().not());
        mTopListUnitScb.disableProperty().bind(mTopListCheckbox.selectedProperty().not());

        mDateDiffPane.load(items);
    }

    private void createUI() {
        mYoyoSizeSds.getValueFactory().setConverter(new StringConverter<Double>() {
            @Override
            public Double fromString(String string) {
                return Double.valueOf(Strings.CS.replace(string, ",", "."));
            }

            @Override
            public String toString(Double value) {
                if (value == null) {
                    return null;
                } else {
                    return "%.3f".formatted(value);
                }
            }
        });

        mTopListUnitScb.getItems().setAll(AlarmLevelChangeUnit.values());
        mTopListUnitScb.getSelectionModel().selectFirst();
        mDiffLatestCheckbox.setText(getBundle().getString("diffMeasLatestCheckBoxText"));
        mDiffAllCheckbox.setText(getBundle().getString("diffMeasAllCheckBoxText"));
        mYoyoCheckbox.setText(getBundle().getString("YoyoCheckBoxText"));
        mTopListCheckbox.setText(getBundle().getString("TopListCheckBoxText"));
        mDiffLatestSds.getValueFactory().setConverter(new NegPosStringConverterDouble());
        mDiffAllSds.getValueFactory().setConverter(new NegPosStringConverterDouble());
        var diffGridPane = new GridPane(GAP_H, GAP_V);
        diffGridPane.addColumn(0, mDiffAllCheckbox, mDiffAllSds);
        diffGridPane.addColumn(1, mDiffLatestCheckbox, mDiffLatestSds);
        FxHelper.autoSizeColumn(diffGridPane, 2);
        var diffPercentGridPane = new GridPane(GAP_H, GAP_V);
        FxHelper.autoSizeColumn(diffPercentGridPane, 2);
        var yoyoGridPane = new GridPane(GAP_H, GAP_V);
        yoyoGridPane.add(mYoyoCheckbox, 0, 0, GridPane.REMAINING, 1);
        yoyoGridPane.addRow(1, mYoyoCountSds, mYoyoSizeSds);
        FxHelper.autoSizeColumn(yoyoGridPane, 2);
        var displacementGridPane = new GridPane(GAP_H, GAP_V);
        displacementGridPane.add(mTopListCheckbox, 0, 0, GridPane.REMAINING, 1);
        displacementGridPane.addRow(1, mTopListSizeSds, new Label(SDict.POINTS.toLower()));
        displacementGridPane.addRow(2, mTopListLimitSis, mTopListUnitScb);
        mTopListSizeSds.setPrefWidth(spinnerWidth);
        mTopListLimitSis.setPrefWidth(spinnerWidth);
        var spinners = new Spinner[]{
            mDiffAllSds,
            mDiffLatestSds,
            mYoyoCountSds,
            mYoyoSizeSds,
            mTopListSizeSds,
            mTopListLimitSis
        };
        FxHelper.setEditable(true, spinners);
        FxHelper.autoCommitSpinners(spinners);

        mRoot = new VBox(GAP_V,
                diffGridPane,
                diffPercentGridPane,
                displacementGridPane,
                yoyoGridPane,
                mBearingRangeSlider
        );

        mDateDiffPane.getRoot().setDisable(true);
    }

    private boolean validateMeasBearing(BTopoControlPoint p) {
        try {
            var o = p.ext().getObservationsTimeFiltered().getLast();
            var bearing = o.ext().getBearing();
            if (mBearingRangeSlider.selectedProperty().get()) {
                return inRange(bearing, mBearingRangeSlider.minProperty(), mBearingRangeSlider.maxProperty())
                        || inRange(bearing - 360.0, mBearingRangeSlider.minProperty(), mBearingRangeSlider.maxProperty());
            } else {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private boolean validateMeasDisplacementAll(BTopoControlPoint p) {
        if (mDiffAllCheckbox.isSelected() && p.ext().deltaZero().getDelta() != null) {
            double lim = mDiffAllSds.getValue();
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
        if (!mDiffLatestCheckbox.isSelected()) {
            return true;
        }

        var observations = p.ext().getObservationsTimeFiltered();
        if (observations.size() > 1) {
            var first = observations.get(observations.size() - 2);
            var last = observations.get(observations.size() - 1);
            double lim = mDiffLatestSds.getValue();
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

//    private boolean validateMeasDateDiff(BTopoControlPoint p) {
//        if (mMeasDateDiffProperty.get()) {
    ////        if (mMeasSpeedProperty.get() && p.ext().deltaZero().getDelta() != null && p.ext().deltaZero().getDelta1() != null) {
//            double lim = mMeasDateDiffValueProperty.get();
//            double value = Math.abs(p.ext().getSpeed()[0]);
//
//            if (lim == 0) {
//                return value == 0;
//            } else if (lim < 0) {
//                return value <= Math.abs(lim);
//            } else {
//                return value >= lim;
//            }
//        } else {
//            return true;
//        }
//    }

    private boolean validateMeasYoyo(BTopoControlPoint p) {
        if (!mYoyoCheckbox.isSelected()) {
            return true;
        } else if (p.getDimension() == BDimension._2d || p.ext().getObservationsTimeFiltered().size() < 2) {
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

            if (directionChange && Math.abs(currentDeltaZ - prevDeltaZ) >= mYoyoSizeSds.getValue()) {
                matches++;
            }
        }

        return matches >= mYoyoCountSds.getValue();
    }

}
