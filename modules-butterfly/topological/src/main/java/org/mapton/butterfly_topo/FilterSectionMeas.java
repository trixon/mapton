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
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.shared.AlarmLevelChangeMode;
import org.mapton.butterfly_topo.shared.AlarmLevelChangeUnit;
import org.mapton.butterfly_topo.shared.AlarmLevelFilter;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;
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

    private final SessionCheckComboBox<AlarmLevelFilter> mAlarmSccb = new SessionCheckComboBox<>(true);
    private final int mDefaultAlarmLevelAgeValue = -7;
    private final int mDefaultDiffPercentageValue = 80;
    private final double mDefaultDiffValue = 0.020;
    private final int mDefaultMeasAlarmLevelChangeLimit = 1;
    private final int mDefaultMeasAlarmLevelChangeValue = 10;
    private final int mDefaultMeasTopListLimit = 14;
    private final int mDefaultMeasTopListSize = 10;
    private final double mDefaultMeasYoyoCount = 5.0;
    private final double mDefaultMeasYoyoSize = 0.003;
    private final int mDefaultNumOfMeasfValue = 1;
    private final double mDefaultSpeedValue = 0.020;
    private final CheckBox mDiffMeasAllCheckbox = new CheckBox();
    private final SessionDoubleSpinner mDiffMeasAllSds = new SessionDoubleSpinner(-1.0, 1.0, mDefaultDiffValue, 0.001);
    private final CheckBox mDiffMeasLatestCheckbox = new CheckBox();
    private final SessionDoubleSpinner mDiffMeasLatestSds = new SessionDoubleSpinner(-1.0, 1.0, mDefaultDiffValue, 0.001);
    private final CheckBox mDiffMeasPercentageHCheckbox = new CheckBox();
    private final SessionIntegerSpinner mDiffMeasPercentageHSis = new SessionIntegerSpinner(-1000, 1000, mDefaultDiffPercentageValue, 10);
    private final CheckBox mDiffMeasPercentagePCheckbox = new CheckBox();
    private final SessionIntegerSpinner mDiffMeasPercentagePSis = new SessionIntegerSpinner(-1000, 1000, mDefaultDiffPercentageValue, 10);
    private final CheckBox mMeasAlarmLevelAgeCheckbox = new CheckBox();
    private final SessionIntegerSpinner mMeasAlarmLevelAgeSis = new SessionIntegerSpinner(Integer.MIN_VALUE, Integer.MAX_VALUE, mDefaultAlarmLevelAgeValue);
    private final CheckBox mMeasAlarmLevelChangeCheckbox = new CheckBox();
    private final SessionIntegerSpinner mMeasAlarmLevelChangeLimitSis = new SessionIntegerSpinner(1, 100, mDefaultMeasAlarmLevelChangeLimit);
    private final SessionComboBox<AlarmLevelChangeMode> mMeasAlarmLevelChangeModeScb = new SessionComboBox<>();
    private final SessionComboBox<AlarmLevelChangeUnit> mMeasAlarmLevelChangeUnitScb = new SessionComboBox<>();
    private final SessionIntegerSpinner mMeasAlarmLevelChangeValueSis = new SessionIntegerSpinner(2, 10000, mDefaultMeasAlarmLevelChangeValue);
    private final RangeSliderPane mMeasBearingRangeSlider = new RangeSliderPane(Dict.BEARING.toString(), -90.0, 360.0, false);
    private final SessionCheckComboBox<String> mMeasCodeSccb = new SessionCheckComboBox<>(true);
    private final CheckBox mMeasLatestOperatorCheckbox = new CheckBox();
    private final SessionIntegerSpinner mMeasNumOfSis = new SessionIntegerSpinner(Integer.MIN_VALUE, Integer.MAX_VALUE, mDefaultNumOfMeasfValue);
    private final SessionCheckComboBox<String> mMeasOperatorSccb = new SessionCheckComboBox<>();
    private final CheckBox mMeasSpeedCheckbox = new CheckBox();
    private final SessionDoubleSpinner mMeasSpeedSds = new SessionDoubleSpinner(-1.0, 1.0, mDefaultSpeedValue, 0.001);
    private final CheckBox mMeasTopListCheckbox = new CheckBox();
    private final SessionIntegerSpinner mMeasTopListLimitSis = new SessionIntegerSpinner(0, Integer.MAX_VALUE, mDefaultMeasTopListLimit);
    private final SessionIntegerSpinner mMeasTopListSizeSds = new SessionIntegerSpinner(1, 100, mDefaultMeasTopListSize, 1);
    private final SessionComboBox<AlarmLevelChangeUnit> mMeasTopListUnitScb = new SessionComboBox<>();
    private final CheckBox mMeasYoyoCheckbox = new CheckBox();
    private final SessionDoubleSpinner mMeasYoyoCountSds = new SessionDoubleSpinner(0, 100.0, mDefaultMeasYoyoCount, 1.0);
    private final SessionDoubleSpinner mMeasYoyoSizeSds = new SessionDoubleSpinner(0, 1.0, mDefaultMeasYoyoSize, 0.001);
    private final CheckBox mNumOfMeasCheckbox = new CheckBox();
    private final GridPane mRoot = new GridPane(hGap, vGap * 4);

    public FilterSectionMeas() {
        super("Mätningar");
        init();
        setContent(mRoot);
    }

    @Override
    public void clear() {
        super.clear();
        FxHelper.setSelected(false, mMeasAlarmLevelChangeCheckbox, mMeasSpeedCheckbox, mDiffMeasLatestCheckbox, mDiffMeasAllCheckbox, mMeasYoyoCheckbox, mMeasTopListCheckbox, mMeasLatestOperatorCheckbox, mNumOfMeasCheckbox, mMeasAlarmLevelAgeCheckbox, mDiffMeasPercentageHCheckbox, mDiffMeasPercentagePCheckbox);
        mDiffMeasAllSds.getValueFactory().setValue(mDefaultDiffValue);
        mDiffMeasLatestSds.getValueFactory().setValue(mDefaultDiffValue);
        mDiffMeasPercentageHSis.getValueFactory().setValue(mDefaultDiffPercentageValue);
        mDiffMeasPercentagePSis.getValueFactory().setValue(mDefaultDiffPercentageValue);
        mMeasAlarmLevelAgeSis.getValueFactory().setValue(mDefaultAlarmLevelAgeValue);
        mMeasAlarmLevelChangeLimitSis.getValueFactory().setValue(mDefaultMeasAlarmLevelChangeLimit);
        mMeasAlarmLevelChangeValueSis.getValueFactory().setValue(mDefaultMeasAlarmLevelChangeValue);
        mMeasNumOfSis.getValueFactory().setValue(mDefaultNumOfMeasfValue);
        mMeasSpeedSds.getValueFactory().setValue(mDefaultSpeedValue);
        mMeasTopListLimitSis.getValueFactory().setValue(mDefaultMeasTopListLimit);
        mMeasTopListSizeSds.getValueFactory().setValue(mDefaultMeasTopListSize);
        mMeasYoyoCountSds.getValueFactory().setValue(mDefaultMeasYoyoCount);
        mMeasYoyoSizeSds.getValueFactory().setValue(mDefaultMeasYoyoSize);
        mMeasBearingRangeSlider.clear();
        SessionCheckComboBox.clearChecks(
                mAlarmSccb,
                mMeasOperatorSccb,
                mMeasCodeSccb
        );
    }

    public ResourceBundle getBundle() {
        return NbBundle.getBundle(getClass());
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        sessionManager.register("filter.section.meas", selectedProperty());
        sessionManager.register("filter.measCheckedMeasCode", mMeasCodeSccb.checkedStringProperty());
        sessionManager.register("filter.measCheckedOperators", mMeasOperatorSccb.checkedStringProperty());
        sessionManager.register("filter.measDiffAll", mDiffMeasAllCheckbox.selectedProperty());
        sessionManager.register("filter.measDiffPercentageH", mDiffMeasPercentageHCheckbox.selectedProperty());
        sessionManager.register("filter.measDiffPercentageP", mDiffMeasPercentagePCheckbox.selectedProperty());
        sessionManager.register("filter.measTopList", mMeasTopListCheckbox.selectedProperty());
        sessionManager.register("filter.measTopListSizeValue", mMeasTopListSizeSds.sessionValueProperty());
        sessionManager.register("filter.measTopListUnit", mMeasTopListUnitScb.selectedIndexProperty());
        sessionManager.register("filter.measTopListLimit", mMeasTopListLimitSis.sessionValueProperty());
        sessionManager.register("filter.measYoyo", mMeasYoyoCheckbox.selectedProperty());
        sessionManager.register("filter.measDiffAllValue", mDiffMeasAllSds.sessionValueProperty());
        sessionManager.register("filter.measDiffPercentageHValue", mDiffMeasPercentageHSis.sessionValueProperty());
        sessionManager.register("filter.measDiffPercentagePValue", mDiffMeasPercentagePSis.sessionValueProperty());
        sessionManager.register("filter.measYoyoCountValue", mMeasYoyoCountSds.sessionValueProperty());
        sessionManager.register("filter.measYoyoSizeValue", mMeasYoyoSizeSds.sessionValueProperty());
        sessionManager.register("filter.measDiffLatest", mDiffMeasLatestCheckbox.selectedProperty());
        sessionManager.register("filter.measDiffLatestValue", mDiffMeasLatestSds.sessionValueProperty());
        sessionManager.register("filter.measSpeed", mMeasSpeedCheckbox.selectedProperty());
        sessionManager.register("filter.measSpeedValue", mMeasSpeedSds.sessionValueProperty());
        sessionManager.register("filter.measLatestOperator", mMeasLatestOperatorCheckbox.selectedProperty());
        sessionManager.register("filter.measNumOfMeas", mNumOfMeasCheckbox.selectedProperty());
        sessionManager.register("filter.measNumOfValue", mMeasNumOfSis.sessionValueProperty());
        sessionManager.register("filter.measAlarmLevelAge", mMeasAlarmLevelAgeCheckbox.selectedProperty());
        sessionManager.register("filter.measAlarmLevelAgeValue", mMeasAlarmLevelAgeSis.sessionValueProperty());
        sessionManager.register("filter.measAlarmLevelChange", mMeasAlarmLevelChangeCheckbox.selectedProperty());
        sessionManager.register("filter.measAlarmLevelChangeMode", mMeasAlarmLevelChangeModeScb.selectedIndexProperty());
        sessionManager.register("filter.measAlarmLevelChangeUnit", mMeasAlarmLevelChangeUnitScb.selectedIndexProperty());
        sessionManager.register("filter.measAlarmLevelChangeValue", mMeasAlarmLevelChangeValueSis.sessionValueProperty());
        sessionManager.register("filter.measAlarmLevelChangeLimit", mMeasAlarmLevelChangeLimitSis.sessionValueProperty());
        sessionManager.register("filter.checkedNextAlarm", mAlarmSccb.checkedStringProperty());
        sessionManager.register("filter.checkedNextAlarm", mAlarmSccb.checkedStringProperty());
        mMeasBearingRangeSlider.initSession("filter.measBearing", sessionManager);
    }

    @Override
    public void onShownFirstTime() {
    }

    @Override
    public void reset(PropertiesConfiguration filterConfig) {
    }

    void initListeners(TopoFilter filter) {
        filter.measNumOfProperty().bind(mNumOfMeasCheckbox.selectedProperty());
        filter.measAlarmLevelAgeProperty().bind(mMeasAlarmLevelAgeCheckbox.selectedProperty());
        filter.measDiffAllProperty().bind(mDiffMeasAllCheckbox.selectedProperty());
        filter.measDiffPercentageHProperty().bind(mDiffMeasPercentageHCheckbox.selectedProperty());
        filter.measDiffPercentagePProperty().bind(mDiffMeasPercentagePCheckbox.selectedProperty());
        filter.measYoyoProperty().bind(mMeasYoyoCheckbox.selectedProperty());
        filter.measTopListProperty().bind(mMeasTopListCheckbox.selectedProperty());
        filter.measSpeedProperty().bind(mMeasSpeedCheckbox.selectedProperty());
        filter.measDiffLatestProperty().bind(mDiffMeasLatestCheckbox.selectedProperty());
        filter.measLatestOperatorProperty().bind(mMeasLatestOperatorCheckbox.selectedProperty());
        filter.measNumOfValueProperty().bind(mMeasNumOfSis.sessionValueProperty());
        filter.measAlarmLevelAgeValueProperty().bind(mMeasAlarmLevelAgeSis.sessionValueProperty());
        filter.measDiffAllValueProperty().bind(mDiffMeasAllSds.sessionValueProperty());
        filter.measDiffPercentageHValueProperty().bind(mDiffMeasPercentageHSis.sessionValueProperty());
        filter.measYoyoCountValueProperty().bind(mMeasYoyoCountSds.sessionValueProperty());
        filter.measTopListSizeValueProperty().bind(mMeasTopListSizeSds.sessionValueProperty());
        filter.measYoyoSizeValueProperty().bind(mMeasYoyoSizeSds.sessionValueProperty());
        filter.measDiffLatestValueProperty().bind(mDiffMeasLatestSds.sessionValueProperty());
        filter.measDiffPercentagePValueProperty().bind(mDiffMeasPercentagePSis.sessionValueProperty());
        filter.measSpeedValueProperty().bind(mMeasSpeedSds.sessionValueProperty());
        filter.measAlarmLevelChangeProperty().bind(mMeasAlarmLevelChangeCheckbox.selectedProperty());
        filter.measAlarmLevelChangeModeProperty().bind(mMeasAlarmLevelChangeModeScb.getSelectionModel().selectedItemProperty());
        filter.measAlarmLevelChangeUnitProperty().bind(mMeasAlarmLevelChangeUnitScb.getSelectionModel().selectedItemProperty());
        filter.measTopListUnitProperty().bind(mMeasTopListUnitScb.getSelectionModel().selectedItemProperty());
        filter.measAlarmLevelChangeValueProperty().bind(mMeasAlarmLevelChangeValueSis.sessionValueProperty());
        filter.measAlarmLevelChangeLimitProperty().bind(mMeasAlarmLevelChangeLimitSis.sessionValueProperty());
        filter.measTopListLimitProperty().bind(mMeasTopListLimitSis.sessionValueProperty());
        filter.mMeasOperatorsCheckModel = mMeasOperatorSccb.getCheckModel();
        filter.mMeasCodeCheckModel = mMeasCodeSccb.getCheckModel();
        filter.mAlarmLevelCheckModel = mAlarmSccb.getCheckModel();
        filter.mMeasBearingSelectedProperty.bind(mMeasBearingRangeSlider.selectedProperty());
        filter.mMeasBearingMinProperty.bind(mMeasBearingRangeSlider.minProperty());
        filter.mMeasBearingMaxProperty.bind(mMeasBearingRangeSlider.maxProperty());
    }

    void load(ArrayList<BTopoControlPoint> items) {
        mMeasOperatorSccb.loadAndRestoreCheckItems(items.stream().flatMap(p -> p.ext().getObservationsAllRaw().stream().map(o -> o.getOperator())));
        mMeasCodeSccb.loadAndRestoreCheckItems();
        mMeasAlarmLevelChangeModeScb.load();
        mMeasAlarmLevelChangeUnitScb.load();
        mMeasTopListUnitScb.load();
        mMeasAlarmLevelChangeValueSis.load();
        mMeasAlarmLevelChangeLimitSis.load();
        mMeasTopListLimitSis.load();
        mMeasSpeedSds.load();
        mDiffMeasLatestSds.load();
        mDiffMeasAllSds.load();
        mDiffMeasPercentageHSis.load();
        mDiffMeasPercentagePSis.load();
        mMeasYoyoCountSds.load();
        mMeasYoyoSizeSds.load();
        mMeasTopListSizeSds.load();
        mMeasNumOfSis.load();
        mMeasAlarmLevelAgeSis.load();
        mMeasNumOfSis.disableProperty().bind(mNumOfMeasCheckbox.selectedProperty().not());
        mMeasAlarmLevelAgeSis.disableProperty().bind(mMeasAlarmLevelAgeCheckbox.selectedProperty().not());
        mDiffMeasAllSds.disableProperty().bind(mDiffMeasAllCheckbox.selectedProperty().not());
        mDiffMeasLatestSds.disableProperty().bind(mDiffMeasLatestCheckbox.selectedProperty().not());
        mDiffMeasPercentageHSis.disableProperty().bind(mDiffMeasPercentageHCheckbox.selectedProperty().not());
        mDiffMeasPercentagePSis.disableProperty().bind(mDiffMeasPercentagePCheckbox.selectedProperty().not());
        mMeasSpeedSds.disableProperty().bind(mMeasSpeedCheckbox.selectedProperty().not());
        mMeasTopListSizeSds.disableProperty().bind(mMeasTopListCheckbox.selectedProperty().not());
        mMeasYoyoCountSds.disableProperty().bind(mMeasYoyoCheckbox.selectedProperty().not());
        mMeasYoyoSizeSds.disableProperty().bind(mMeasYoyoCheckbox.selectedProperty().not());
        mMeasAlarmLevelChangeLimitSis.disableProperty().bind(mMeasAlarmLevelChangeCheckbox.selectedProperty().not());
        mMeasTopListLimitSis.disableProperty().bind(mMeasTopListCheckbox.selectedProperty().not());
        mMeasAlarmLevelChangeModeScb.disableProperty().bind(mMeasAlarmLevelChangeCheckbox.selectedProperty().not());
        mMeasAlarmLevelChangeUnitScb.disableProperty().bind(mMeasAlarmLevelChangeCheckbox.selectedProperty().not());
        mMeasTopListUnitScb.disableProperty().bind(mMeasTopListCheckbox.selectedProperty().not());
        mMeasAlarmLevelChangeValueSis.disableProperty().bind(mMeasAlarmLevelChangeCheckbox.selectedProperty().not());

        mAlarmSccb.loadAndRestoreCheckItems();
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
        FxHelper.setShowCheckedCount(true, mAlarmSccb, mMeasCodeSccb, mMeasOperatorSccb);
        mAlarmSccb.setTitle(SDict.ALARM_LEVEL.toString());
        mAlarmSccb.getItems().setAll(AlarmLevelFilter.values());
        mMeasCodeSccb.setTitle(getBundle().getString("measCodeCheckComboBoxTitle"));
        mMeasOperatorSccb.setTitle(SDict.SURVEYORS.toString());
        mMeasAlarmLevelChangeModeScb.getItems().setAll(AlarmLevelChangeMode.values());
        mMeasAlarmLevelChangeUnitScb.getItems().setAll(AlarmLevelChangeUnit.values());
        mMeasTopListUnitScb.getItems().setAll(AlarmLevelChangeUnit.values());
        mMeasTopListUnitScb.getSelectionModel().selectFirst();
        mMeasCodeSccb.getItems().setAll(List.of(getBundle().getString("measCodeZeroIs"), getBundle().getString("measCodeZero"), getBundle().getString("measCodeReplacement")));
        mMeasNumOfSis.getValueFactory().setConverter(new NegPosStringConverterInteger());
        mMeasAlarmLevelAgeSis.getValueFactory().setConverter(new NegPosStringConverterInteger());
        mMeasAlarmLevelChangeCheckbox.setText(getBundle().getString("measAlarmLevelChangeCheckBoxText"));
        mMeasSpeedCheckbox.setText(Dict.SPEED.toString());
        mDiffMeasLatestCheckbox.setText(getBundle().getString("diffMeasLatestCheckBoxText"));
        mDiffMeasAllCheckbox.setText(getBundle().getString("diffMeasAllCheckBoxText"));
        mDiffMeasPercentageHCheckbox.setText(getBundle().getString("diffMeasPercentageHCheckboxText"));
        mDiffMeasPercentagePCheckbox.setText(getBundle().getString("diffMeasPercentagePCheckboxText"));
        mMeasYoyoCheckbox.setText(getBundle().getString("YoyoCheckBoxText"));
        mMeasTopListCheckbox.setText(getBundle().getString("TopListCheckBoxText"));
        mMeasLatestOperatorCheckbox.setText(getBundle().getString("measLatesOperatorCheckBoxText"));
        mNumOfMeasCheckbox.setText(getBundle().getString("numOfMeasCheckBoxText"));
        mMeasAlarmLevelAgeCheckbox.setText("Ålder på larmnivå");
        mMeasSpeedSds.getValueFactory().setConverter(new NegPosStringConverterDouble());
        mDiffMeasLatestSds.getValueFactory().setConverter(new NegPosStringConverterDouble());
        mDiffMeasAllSds.getValueFactory().setConverter(new NegPosStringConverterDouble());
        mDiffMeasPercentageHSis.getValueFactory().setConverter(new NegPosStringConverterInteger());
        mDiffMeasPercentagePSis.getValueFactory().setConverter(new NegPosStringConverterInteger());
        var diffGridPane = new GridPane(hGap, vGap);
        diffGridPane.addColumn(0, mDiffMeasAllCheckbox, mDiffMeasAllSds);
        diffGridPane.addColumn(1, mDiffMeasLatestCheckbox, mDiffMeasLatestSds);
        FxHelper.autoSizeColumn(diffGridPane, 2);
        var diffPercentGridPane = new GridPane(hGap, vGap);
        diffPercentGridPane.addColumn(0, mDiffMeasPercentageHCheckbox, mDiffMeasPercentageHSis);
        diffPercentGridPane.addColumn(1, mDiffMeasPercentagePCheckbox, mDiffMeasPercentagePSis);
        FxHelper.autoSizeColumn(diffPercentGridPane, 2);
        var yoyoGridPane = new GridPane(hGap, vGap);
        yoyoGridPane.add(mMeasYoyoCheckbox, 0, 0, GridPane.REMAINING, 1);
        yoyoGridPane.addRow(1, mMeasYoyoCountSds, mMeasYoyoSizeSds);
        FxHelper.autoSizeColumn(yoyoGridPane, 2);
        var displacementGridPane = new GridPane(hGap, vGap);
        displacementGridPane.add(mMeasTopListCheckbox, 0, 0, GridPane.REMAINING, 1);
        displacementGridPane.addRow(1, mMeasTopListSizeSds, new Label(SDict.POINTS.toLower()));
        displacementGridPane.addRow(2, mMeasTopListLimitSis, mMeasTopListUnitScb);
        mMeasTopListSizeSds.setPrefWidth(spinnerWidth);
        mMeasTopListLimitSis.setPrefWidth(spinnerWidth);
        var alcGridPane = new GridPane(hGap, vGap);
        alcGridPane.add(mMeasAlarmLevelChangeCheckbox, 0, 0, GridPane.REMAINING, 1);
        alcGridPane.addRow(1, mMeasAlarmLevelChangeLimitSis, mMeasAlarmLevelChangeModeScb);
        alcGridPane.addRow(2, mMeasAlarmLevelChangeValueSis, mMeasAlarmLevelChangeUnitScb);
        mMeasAlarmLevelChangeLimitSis.setPrefWidth(spinnerWidth);
        mMeasAlarmLevelChangeValueSis.setPrefWidth(spinnerWidth);
        var spinners = new Spinner[]{mDiffMeasAllSds, mDiffMeasLatestSds, mDiffMeasPercentageHSis, mDiffMeasPercentagePSis, mMeasSpeedSds, mMeasNumOfSis, mMeasYoyoCountSds, mMeasYoyoSizeSds, mMeasAlarmLevelChangeValueSis, mMeasAlarmLevelChangeLimitSis, mMeasAlarmLevelAgeSis, mMeasTopListSizeSds, mMeasTopListLimitSis};
        FxHelper.setEditable(true, spinners);
        FxHelper.autoCommitSpinners(spinners);
        var movementBox = new VBox(vGap, diffGridPane, diffPercentGridPane, displacementGridPane, new VBox(titleGap, mMeasSpeedCheckbox, mMeasSpeedSds), yoyoGridPane, mMeasBearingRangeSlider);
        var miscBox = new VBox(vGap, new VBox(titleGap, mNumOfMeasCheckbox, mMeasNumOfSis), new Separator(), mMeasCodeSccb, new VBox(titleGap, mMeasOperatorSccb, mMeasLatestOperatorCheckbox));
        var alarmBox = new VBox(vGap, mAlarmSccb, new VBox(titleGap, mMeasAlarmLevelAgeCheckbox, mMeasAlarmLevelAgeSis), alcGridPane);
        int row = 0;
        mRoot.add(wrapInTitleBorder("Rörelser", movementBox), 0, row, 1, GridPane.REMAINING);
        mRoot.add(wrapInTitleBorder("Larmnivå", alarmBox), 1, row++, 1, 1);
        mRoot.add(wrapInTitleBorder("Övrigt", miscBox), 1, row++, 1, 1);
        FxHelper.autoSizeRegionHorizontal(mMeasTopListUnitScb, mMeasAlarmLevelChangeModeScb, mMeasAlarmLevelChangeUnitScb);
        FxHelper.bindWidthForChildrens(movementBox, alarmBox, miscBox);
        FxHelper.bindWidthForRegions(movementBox, mMeasSpeedSds, mMeasYoyoCountSds, mMeasYoyoSizeSds, mMeasNumOfSis, mMeasAlarmLevelAgeSis, mMeasOperatorSccb);
        FxHelper.autoSizeColumn(mRoot, 2);
        int maxWidth = FxHelper.getUIScaled(500);
        setMaxWidth(maxWidth);
    }

}
