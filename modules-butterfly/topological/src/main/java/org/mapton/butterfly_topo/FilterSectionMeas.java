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
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.lang3.Strings;
import org.mapton.api.ui.forms.MBaseFilterSection;
import org.mapton.api.ui.forms.NegPosStringConverterDouble;
import org.mapton.butterfly_core.api.AlarmLevelChangeUnit;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.openide.util.NbBundle;
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
class FilterSectionMeas extends MBaseFilterSection {

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
        init();
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

    public ResourceBundle getBundle() {
        return NbBundle.getBundle(getClass());
    }

    public Region getRoot() {
        return mRoot;
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

    void initListeners(TopoFilter filter) {
        filter.measDiffAllProperty().bind(mDiffAllCheckbox.selectedProperty());
        filter.measYoyoProperty().bind(mYoyoCheckbox.selectedProperty());
        filter.measTopListProperty().bind(mTopListCheckbox.selectedProperty());
        filter.measDiffLatestProperty().bind(mDiffLatestCheckbox.selectedProperty());
        filter.measDiffAllValueProperty().bind(mDiffAllSds.sessionValueProperty());
        filter.measYoyoCountValueProperty().bind(mYoyoCountSds.sessionValueProperty());
        filter.measTopListSizeValueProperty().bind(mTopListSizeSds.sessionValueProperty());
        filter.measYoyoSizeValueProperty().bind(mYoyoSizeSds.sessionValueProperty());
        filter.measDiffLatestValueProperty().bind(mDiffLatestSds.sessionValueProperty());
        filter.measTopListUnitProperty().bind(mTopListUnitScb.getSelectionModel().selectedItemProperty());
        filter.measTopListLimitProperty().bind(mTopListLimitSis.sessionValueProperty());
        filter.mMeasBearingSelectedProperty.bind(mBearingRangeSlider.selectedProperty());
        filter.mMeasBearingMinProperty.bind(mBearingRangeSlider.minProperty());
        filter.mMeasBearingMaxProperty.bind(mBearingRangeSlider.maxProperty());

        mDateDiffPane.initListeners(filter);
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

    private void init() {
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

}
