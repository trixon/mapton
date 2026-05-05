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
import java.util.List;
import java.util.ResourceBundle;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.ui.forms.MBaseFilterSection;
import org.mapton.api.ui.forms.NegPosStringConverterDouble;
import org.mapton.api.ui.forms.NegPosStringConverterInteger;
import org.mapton.butterfly_core.api.AlarmLevelChangeUnit;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.fx.BindingHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.RangeSliderPane;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;
import se.trixon.almond.util.fx.session.SessionComboBox;
import se.trixon.almond.util.fx.session.SessionDoubleSpinner;
import se.trixon.almond.util.fx.session.SessionIntegerSpinner;

/**
 *
 * @author Patrik Karlström
 */
class FilterSectionMeas extends MBaseFilterSection {

    private final DateDiffPane mDateDiffPane;
    private final double mDefaultDiffValue = 0.020;
    private final int mDefaultMeasTopListLimit = 14;
    private final int mDefaultMeasTopListSize = 10;
    private final double mDefaultMeasYoyoCount = 5.0;
    private final double mDefaultMeasYoyoSize = 0.003;
    private final int mDefaultNumOfMeasfValue = 1;
    private final CheckBox mDiffMeasAllCheckbox = new CheckBox();
    private final SessionDoubleSpinner mDiffMeasAllSds = new SessionDoubleSpinner(-1.0, 1.0, mDefaultDiffValue, 0.001);
    private final CheckBox mDiffMeasLatestCheckbox = new CheckBox();
    private final SessionDoubleSpinner mDiffMeasLatestSds = new SessionDoubleSpinner(-1.0, 1.0, mDefaultDiffValue, 0.001);
    private final RangeSliderPane mMeasBearingRangeSlider = new RangeSliderPane(Dict.BEARING.toString(), -90.0, 360.0, false);
    private final SessionCheckComboBox<String> mMeasCodeSccb = new SessionCheckComboBox<>(true);
    private final CheckBox mMeasLatestOperatorCheckbox = new CheckBox();
    private final SessionIntegerSpinner mMeasNumOfSis = new SessionIntegerSpinner(Integer.MIN_VALUE, Integer.MAX_VALUE, mDefaultNumOfMeasfValue);
    private final SessionCheckComboBox<String> mMeasOperatorSccb = new SessionCheckComboBox<>();
    private final CheckBox mMeasTopListCheckbox = new CheckBox();
    private final SessionIntegerSpinner mMeasTopListLimitSis = new SessionIntegerSpinner(0, Integer.MAX_VALUE, mDefaultMeasTopListLimit);
    private final SessionIntegerSpinner mMeasTopListSizeSds = new SessionIntegerSpinner(1, 100, mDefaultMeasTopListSize, 1);
    private final SessionComboBox<AlarmLevelChangeUnit> mMeasTopListUnitScb = new SessionComboBox<>();
    private final CheckBox mMeasYoyoCheckbox = new CheckBox();
    private final SessionDoubleSpinner mMeasYoyoCountSds = new SessionDoubleSpinner(0, 100.0, mDefaultMeasYoyoCount, 1.0);
    private final SessionDoubleSpinner mMeasYoyoSizeSds = new SessionDoubleSpinner(0, 1.0, mDefaultMeasYoyoSize, 0.001);
    private final CheckBox mNumOfMeasCheckbox = new CheckBox();
    private final GridPane mRoot = new GridPane(GAP_H, GAP_V * 4);

    public FilterSectionMeas() {
        super(SDict.MEASUREMENTS.toString());
        mDateDiffPane = new DateDiffPane(this);
        init();
        setContent(mRoot);
    }

    @Override
    public void clear() {
        super.clear();
        FxHelper.setSelected(false,
                mDiffMeasLatestCheckbox,
                mDiffMeasAllCheckbox,
                mMeasYoyoCheckbox,
                mMeasTopListCheckbox,
                mMeasLatestOperatorCheckbox,
                mNumOfMeasCheckbox
        );
        mDiffMeasAllSds.getValueFactory().setValue(mDefaultDiffValue);
        mDiffMeasLatestSds.getValueFactory().setValue(mDefaultDiffValue);
        mMeasNumOfSis.getValueFactory().setValue(mDefaultNumOfMeasfValue);
        mMeasTopListLimitSis.getValueFactory().setValue(mDefaultMeasTopListLimit);
        mMeasTopListSizeSds.getValueFactory().setValue(mDefaultMeasTopListSize);
        mMeasYoyoCountSds.getValueFactory().setValue(mDefaultMeasYoyoCount);
        mMeasYoyoSizeSds.getValueFactory().setValue(mDefaultMeasYoyoSize);
        mMeasBearingRangeSlider.clear();
        SessionCheckComboBox.clearChecks(
                mMeasOperatorSccb,
                mMeasCodeSccb
        );
    }

    public ResourceBundle getBundle() {
        return NbBundle.getBundle(getClass());
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        sessionManager.register(getKeyFilter("section"), selectedProperty());
        sessionManager.register(getKeyFilter("checkedMeasCode"), mMeasCodeSccb.checkedStringProperty());
        sessionManager.register(getKeyFilter("CheckedOperators"), mMeasOperatorSccb.checkedStringProperty());
        sessionManager.register(getKeyFilter("diffAll"), mDiffMeasAllCheckbox.selectedProperty());
        sessionManager.register(getKeyFilter("topList"), mMeasTopListCheckbox.selectedProperty());
        sessionManager.register(getKeyFilter("topListSizeValue"), mMeasTopListSizeSds.sessionValueProperty());
        sessionManager.register(getKeyFilter("topListUnit"), mMeasTopListUnitScb.selectedIndexProperty());
        sessionManager.register(getKeyFilter("topListLimit"), mMeasTopListLimitSis.sessionValueProperty());
        sessionManager.register(getKeyFilter("yoyo"), mMeasYoyoCheckbox.selectedProperty());
        sessionManager.register(getKeyFilter("diffAllValue"), mDiffMeasAllSds.sessionValueProperty());
        sessionManager.register(getKeyFilter("yoyoCountValue"), mMeasYoyoCountSds.sessionValueProperty());
        sessionManager.register(getKeyFilter("yoyoSizeValue"), mMeasYoyoSizeSds.sessionValueProperty());
        sessionManager.register(getKeyFilter("diffLatest"), mDiffMeasLatestCheckbox.selectedProperty());
        sessionManager.register(getKeyFilter("diffLatestValue"), mDiffMeasLatestSds.sessionValueProperty());
        sessionManager.register(getKeyFilter("latestOperator"), mMeasLatestOperatorCheckbox.selectedProperty());
        sessionManager.register(getKeyFilter("numOfMeas"), mNumOfMeasCheckbox.selectedProperty());
        sessionManager.register(getKeyFilter("numOfValue"), mMeasNumOfSis.sessionValueProperty());
        mMeasBearingRangeSlider.initSession(getKeyFilter("bearing"), sessionManager);
        mDateDiffPane.initSession(sessionManager);
    }

    @Override
    public void onShownFirstTime() {
    }

    @Override
    public void reset(PropertiesConfiguration filterConfig) {
    }

    void initListeners(TopoFilter filter) {
        filter.measNumOfProperty().bind(mNumOfMeasCheckbox.selectedProperty());
        filter.measDiffAllProperty().bind(mDiffMeasAllCheckbox.selectedProperty());
        filter.measYoyoProperty().bind(mMeasYoyoCheckbox.selectedProperty());
        filter.measTopListProperty().bind(mMeasTopListCheckbox.selectedProperty());
        filter.measDiffLatestProperty().bind(mDiffMeasLatestCheckbox.selectedProperty());
        filter.measLatestOperatorProperty().bind(mMeasLatestOperatorCheckbox.selectedProperty());
        filter.measNumOfValueProperty().bind(mMeasNumOfSis.sessionValueProperty());
        filter.measDiffAllValueProperty().bind(mDiffMeasAllSds.sessionValueProperty());
        filter.measYoyoCountValueProperty().bind(mMeasYoyoCountSds.sessionValueProperty());
        filter.measTopListSizeValueProperty().bind(mMeasTopListSizeSds.sessionValueProperty());
        filter.measYoyoSizeValueProperty().bind(mMeasYoyoSizeSds.sessionValueProperty());
        filter.measDiffLatestValueProperty().bind(mDiffMeasLatestSds.sessionValueProperty());
        filter.measTopListUnitProperty().bind(mMeasTopListUnitScb.getSelectionModel().selectedItemProperty());
        filter.measTopListLimitProperty().bind(mMeasTopListLimitSis.sessionValueProperty());
        filter.mMeasOperatorsCheckModel = mMeasOperatorSccb.getCheckModel();
        filter.mMeasCodeCheckModel = mMeasCodeSccb.getCheckModel();
        filter.mMeasBearingSelectedProperty.bind(mMeasBearingRangeSlider.selectedProperty());
        filter.mMeasBearingMinProperty.bind(mMeasBearingRangeSlider.minProperty());
        filter.mMeasBearingMaxProperty.bind(mMeasBearingRangeSlider.maxProperty());

        mDateDiffPane.initListeners(filter);
    }

    void load(ArrayList<BTopoControlPoint> items) {
        mMeasOperatorSccb.loadAndRestoreCheckItems(items.stream().flatMap(p -> p.ext().getObservationsAllRaw().stream().map(o -> o.getOperator())));
        mMeasCodeSccb.loadAndRestoreCheckItems();
        mMeasTopListUnitScb.load();
        mMeasTopListLimitSis.load();
        mDiffMeasLatestSds.load();
        mDiffMeasAllSds.load();
        mMeasYoyoCountSds.load();
        mMeasYoyoSizeSds.load();
        mMeasTopListSizeSds.load();
        mMeasNumOfSis.load();
        mMeasNumOfSis.disableProperty().bind(mNumOfMeasCheckbox.selectedProperty().not());
        mDiffMeasAllSds.disableProperty().bind(mDiffMeasAllCheckbox.selectedProperty().not());
        mDiffMeasLatestSds.disableProperty().bind(mDiffMeasLatestCheckbox.selectedProperty().not());
        mMeasTopListSizeSds.disableProperty().bind(mMeasTopListCheckbox.selectedProperty().not());
        mMeasYoyoCountSds.disableProperty().bind(mMeasYoyoCheckbox.selectedProperty().not());
        mMeasYoyoSizeSds.disableProperty().bind(mMeasYoyoCheckbox.selectedProperty().not());
        mMeasTopListLimitSis.disableProperty().bind(mMeasTopListCheckbox.selectedProperty().not());
        mMeasTopListUnitScb.disableProperty().bind(mMeasTopListCheckbox.selectedProperty().not());

        mDateDiffPane.load(items);
    }

    private void init() {
        mMeasYoyoSizeSds.getValueFactory().setConverter(new StringConverter<Double>() {
            @Override
            public Double fromString(String string) {
                return Double.valueOf(StringUtils.replace(string, ",", "."));
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
        FxHelper.setShowCheckedCount(true, mMeasCodeSccb, mMeasOperatorSccb);
        mMeasCodeSccb.setTitle(getBundle().getString("measCodeCheckComboBoxTitle"));
        mMeasOperatorSccb.setTitle(SDict.SURVEYORS.toString());
        mMeasTopListUnitScb.getItems().setAll(AlarmLevelChangeUnit.values());
        mMeasTopListUnitScb.getSelectionModel().selectFirst();
        mMeasCodeSccb.getItems().setAll(List.of(
                getBundle().getString("measCodeZeroIs"),
                getBundle().getString("measCodeZero"),
                getBundle().getString("measCodeReplacement"),
                getBundle().getString("measCodeReplacementNot")
        ));
        mMeasNumOfSis.getValueFactory().setConverter(new NegPosStringConverterInteger());
        mDiffMeasLatestCheckbox.setText(getBundle().getString("diffMeasLatestCheckBoxText"));
        mDiffMeasAllCheckbox.setText(getBundle().getString("diffMeasAllCheckBoxText"));
        mMeasYoyoCheckbox.setText(getBundle().getString("YoyoCheckBoxText"));
        mMeasTopListCheckbox.setText(getBundle().getString("TopListCheckBoxText"));
        mMeasLatestOperatorCheckbox.setText(getBundle().getString("measLatesOperatorCheckBoxText"));
        mNumOfMeasCheckbox.setText(getBundle().getString("numOfMeasCheckBoxText"));
        mDiffMeasLatestSds.getValueFactory().setConverter(new NegPosStringConverterDouble());
        mDiffMeasAllSds.getValueFactory().setConverter(new NegPosStringConverterDouble());
        var diffGridPane = new GridPane(GAP_H, GAP_V);
        diffGridPane.addColumn(0, mDiffMeasAllCheckbox, mDiffMeasAllSds);
        diffGridPane.addColumn(1, mDiffMeasLatestCheckbox, mDiffMeasLatestSds);
        FxHelper.autoSizeColumn(diffGridPane, 2);
        var diffPercentGridPane = new GridPane(GAP_H, GAP_V);
        FxHelper.autoSizeColumn(diffPercentGridPane, 2);
        var yoyoGridPane = new GridPane(GAP_H, GAP_V);
        yoyoGridPane.add(mMeasYoyoCheckbox, 0, 0, GridPane.REMAINING, 1);
        yoyoGridPane.addRow(1, mMeasYoyoCountSds, mMeasYoyoSizeSds);
        FxHelper.autoSizeColumn(yoyoGridPane, 2);
        var displacementGridPane = new GridPane(GAP_H, GAP_V);
        displacementGridPane.add(mMeasTopListCheckbox, 0, 0, GridPane.REMAINING, 1);
        displacementGridPane.addRow(1, mMeasTopListSizeSds, new Label(SDict.POINTS.toLower()));
        displacementGridPane.addRow(2, mMeasTopListLimitSis, mMeasTopListUnitScb);
        mMeasTopListSizeSds.setPrefWidth(spinnerWidth);
        mMeasTopListLimitSis.setPrefWidth(spinnerWidth);
        var spinners = new Spinner[]{
            mDiffMeasAllSds,
            mDiffMeasLatestSds,
            mMeasNumOfSis,
            mMeasYoyoCountSds,
            mMeasYoyoSizeSds,
            mMeasTopListSizeSds,
            mMeasTopListLimitSis
        };
        FxHelper.setEditable(true, spinners);
        FxHelper.autoCommitSpinners(spinners);

        var movementBox = new VBox(GAP_V, diffGridPane, diffPercentGridPane, displacementGridPane, yoyoGridPane, mMeasBearingRangeSlider);
        var miscBox = new VBox(GAP_V, new VBox(titleGap, mNumOfMeasCheckbox, mMeasNumOfSis), new Separator(), mMeasCodeSccb, new VBox(titleGap, mMeasOperatorSccb, mMeasLatestOperatorCheckbox));
        int row = 0;
        mDateDiffPane.getRoot().setDisable(true);
        mRoot.add(wrapInTitleBorder("Rörelser under period", mDateDiffPane.getRoot()), 0, row++, 1, 1);
        mRoot.add(wrapInTitleBorder("Rörelser", movementBox), 0, row++, 1, 1);
        row = 0;
        mRoot.add(wrapInTitleBorder("Övrigt", miscBox), 1, row++, 1, 1);
        FxHelper.autoSizeRegionHorizontal(mMeasTopListUnitScb);
        BindingHelper.bindWidthForChildrens(movementBox, miscBox);
        BindingHelper.bindWidthForRegions(movementBox, mMeasYoyoCountSds, mMeasYoyoSizeSds, mMeasNumOfSis, mMeasOperatorSccb);
        FxHelper.autoSizeColumn(mRoot, 2);
        mRoot.setMaxWidth(getMaxWidth());
    }

}
