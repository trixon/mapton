/*
 * Copyright 2023 Patrik Karlström.
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

import com.dlsc.gemsfx.Spacer;
import com.dlsc.gemsfx.util.SessionManager;
import java.util.HashSet;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import static javafx.scene.layout.GridPane.REMAINING;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.action.ActionUtils.ActionTextBehavior;
import org.controlsfx.tools.Borders;
import org.mapton.api.ui.forms.DisruptorPane;
import org.mapton.api.ui.forms.NegPosStringConverterDouble;
import org.mapton.api.ui.forms.NegPosStringConverterInteger;
import org.mapton.butterfly_core.api.BaseFilterPopOver;
import org.mapton.butterfly_core.api.BaseFilters;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_topo.api.TopoManager;
import org.mapton.butterfly_topo.shared.AlarmLevelChangeMode;
import org.mapton.butterfly_topo.shared.AlarmLevelChangeUnit;
import org.mapton.butterfly_topo.shared.AlarmLevelFilter;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;
import se.trixon.almond.util.fx.session.SessionComboBox;
import se.trixon.almond.util.fx.session.SessionDoubleSpinner;
import se.trixon.almond.util.fx.session.SessionIntegerSpinner;

/**
 *
 * @author Patrik Karlström
 */
public class TopoFilterPopOver extends BaseFilterPopOver {

    private final SessionCheckComboBox<AlarmLevelFilter> mAlarmSccb = new SessionCheckComboBox<>(true);
    private final BaseFilters mBaseFilters = new BaseFilters();
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
    private final CheckBox mDimens1Checkbox = new CheckBox("1");
    private final CheckBox mDimens2Checkbox = new CheckBox("2");
    private final CheckBox mDimens3Checkbox = new CheckBox("3");
    private final DisruptorPane mDisruptorPane = new DisruptorPane();
    private final TopoFilter mFilter;
    private final CheckBox mInvertCheckbox = new CheckBox();
    private final TopoManager mManager = TopoManager.getInstance();
    private final CheckBox mMeasAlarmLevelAgeCheckbox = new CheckBox();
    private final SessionIntegerSpinner mMeasAlarmLevelAgeSis = new SessionIntegerSpinner(Integer.MIN_VALUE, Integer.MAX_VALUE, mDefaultAlarmLevelAgeValue);
    private final CheckBox mMeasAlarmLevelChangeCheckbox = new CheckBox();
    private final SessionIntegerSpinner mMeasAlarmLevelChangeLimitSis = new SessionIntegerSpinner(1, 100, mDefaultMeasAlarmLevelChangeLimit);
    private final SessionComboBox<AlarmLevelChangeMode> mMeasAlarmLevelChangeModeScb = new SessionComboBox<>();
    private final SessionComboBox<AlarmLevelChangeUnit> mMeasAlarmLevelChangeUnitScb = new SessionComboBox<>();
    private final SessionIntegerSpinner mMeasAlarmLevelChangeValueSis = new SessionIntegerSpinner(2, 10000, mDefaultMeasAlarmLevelChangeValue);
    private final SessionCheckComboBox<String> mMeasCodeSccb = new SessionCheckComboBox<>(true);
    private final CheckBox mMeasIncludeWithoutCheckbox = new CheckBox();
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
    private final CheckBox mSameAlarmCheckbox = new CheckBox();

    public TopoFilterPopOver(TopoFilter filter) {
        mFilter = filter;
        setFilter(filter);
        createUI();
        initListeners();
        initSession(NbPreferences.forModule(getClass()).node(getClass().getSimpleName()));

        populate();
    }

    @Override
    public void clear() {
        setUsePolygonFilter(false);
        mFilter.freeTextProperty().set("");

        FxHelper.setSelected(false,
                mDimens1Checkbox,
                mDimens2Checkbox,
                mDimens3Checkbox,
                mSameAlarmCheckbox,
                mMeasAlarmLevelChangeCheckbox,
                mMeasSpeedCheckbox,
                mDiffMeasLatestCheckbox,
                mDiffMeasAllCheckbox,
                mMeasYoyoCheckbox,
                mMeasTopListCheckbox,
                mInvertCheckbox,
                mMeasLatestOperatorCheckbox,
                mMeasIncludeWithoutCheckbox,
                mNumOfMeasCheckbox,
                mMeasAlarmLevelAgeCheckbox,
                mDiffMeasPercentageHCheckbox,
                mDiffMeasPercentagePCheckbox
        );

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

        SessionCheckComboBox.clearChecks(
                mMeasOperatorSccb,
                mAlarmSccb,
                mMeasCodeSccb
        );
        mBaseFilters.clear();
        mDisruptorPane.reset();
    }

    @Override
    public void filterPresetRestore(Preferences preferences) {
        clear();
        filterPresetStore(preferences);
        //mDateRangePane.reset();
    }

    @Override
    public void filterPresetStore(Preferences preferences) {
        var sessionManager = initSession(preferences);
        sessionManager.unregisterAll();
    }

    @Override
    public void load(Butterfly butterfly) {
        var items = butterfly.topo().getControlPoints();

        var allAlarmNames = items.stream().map(o -> o.getNameOfAlarmHeight()).collect(Collectors.toCollection(HashSet::new));
        allAlarmNames.addAll(items.stream().map(o -> o.getNameOfAlarmPlane()).collect(Collectors.toSet()));
        mBaseFilters.getAlarmNameSccb().loadAndRestoreCheckItems(allAlarmNames.stream());
        mBaseFilters.getGroupSccb().loadAndRestoreCheckItems(items.stream().map(o -> o.getGroup()));
        mBaseFilters.getCategorySccb().loadAndRestoreCheckItems(items.stream().map(o -> o.getCategory()));
        mBaseFilters.getOperatorSccb().loadAndRestoreCheckItems(items.stream().map(o -> o.getOperator()));
        mBaseFilters.getOriginSccb().loadAndRestoreCheckItems(items.stream().map(o -> o.getOrigin()));
        mBaseFilters.getStatusSccb().loadAndRestoreCheckItems(items.stream().map(o -> o.getStatus()));
        mBaseFilters.getFrequencySccb().loadAndRestoreCheckItems(items.stream()
                .filter(o -> o.getFrequency() != null)
                .map(o -> o.getFrequency()));
        mBaseFilters.getMeasNextSccb().loadAndRestoreCheckItems();
        mMeasOperatorSccb.loadAndRestoreCheckItems(items.stream().flatMap(p -> p.ext().getObservationsAllRaw().stream().map(o -> o.getOperator())));
        mMeasCodeSccb.loadAndRestoreCheckItems();
        mAlarmSccb.loadAndRestoreCheckItems();

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

        mDisruptorPane.load();

        var temporalRange = mManager.getTemporalRange();
        if (temporalRange != null) {
            mBaseFilters.getDateRangeFirstPane().setMinMaxDate(temporalRange.getFromLocalDate(), temporalRange.getToLocalDate());
            mBaseFilters.getDateRangeLastPane().setMinMaxDate(temporalRange.getFromLocalDate(), temporalRange.getToLocalDate());
        }

        var sessionManager = getSessionManager();
        sessionManager.register("filter.DateFirstLow", mBaseFilters.getDateRangeFirstPane().lowStringProperty());
        sessionManager.register("filter.DateFirstHigh", mBaseFilters.getDateRangeFirstPane().highStringProperty());
        sessionManager.register("filter.DateLastLow", mBaseFilters.getDateRangeLastPane().lowStringProperty());
        sessionManager.register("filter.DateLastHigh", mBaseFilters.getDateRangeLastPane().highStringProperty());
    }

    @Override
    public void onPolygonFilterChange() {
        mFilter.update();
    }

    @Override
    public void onShownFirstTime() {
        FxHelper.setVisibleRowCount(25,
                mMeasOperatorSccb,
                mAlarmSccb
        );
        mBaseFilters.onShownFirstTime();
    }

    @Override
    public void reset() {
        clear();

        mFilter.freeTextProperty().set("*");
        mBaseFilters.reset(TopoFilterDefaultsConfig.getInstance().getConfig());
    }

    private void createUI() {
        FxHelper.setShowCheckedCount(true,
                mAlarmSccb,
                mMeasCodeSccb,
                mMeasOperatorSccb
        );

        mAlarmSccb.setTitle(SDict.ALARM_LEVEL.toString());
        mAlarmSccb.getItems().setAll(AlarmLevelFilter.values());
        mMeasCodeSccb.setTitle(getBundle().getString("measCodeCheckComboBoxTitle"));
        mMeasOperatorSccb.setTitle(SDict.SURVEYORS.toString());

        mMeasAlarmLevelChangeModeScb.getItems().setAll(AlarmLevelChangeMode.values());
        mMeasAlarmLevelChangeUnitScb.getItems().setAll(AlarmLevelChangeUnit.values());
        mMeasTopListUnitScb.getItems().setAll(AlarmLevelChangeUnit.values());
        mMeasTopListUnitScb.getSelectionModel().selectFirst();

        mMeasCodeSccb.getItems().setAll(List.of(
                getBundle().getString("measCodeZero"),
                getBundle().getString("measCodeReplacement")
        ));

        mMeasNumOfSis.getValueFactory().setConverter(new NegPosStringConverterInteger());
        mMeasAlarmLevelAgeSis.getValueFactory().setConverter(new NegPosStringConverterInteger());
        mSameAlarmCheckbox.setText(getBundle().getString("sameAlarmCheckBoxText"));
        mMeasAlarmLevelChangeCheckbox.setText(getBundle().getString("measAlarmLevelChangeCheckBoxText"));
        mMeasSpeedCheckbox.setText(Dict.SPEED.toString());
        mDiffMeasLatestCheckbox.setText(getBundle().getString("diffMeasLatestCheckBoxText"));
        mDiffMeasAllCheckbox.setText(getBundle().getString("diffMeasAllCheckBoxText"));
        mDiffMeasPercentageHCheckbox.setText(getBundle().getString("diffMeasPercentageHCheckboxText"));
        mDiffMeasPercentagePCheckbox.setText(getBundle().getString("diffMeasPercentagePCheckboxText"));
        mMeasYoyoCheckbox.setText(getBundle().getString("YoyoCheckBoxText"));
        mMeasTopListCheckbox.setText(getBundle().getString("TopListCheckBoxText"));
        mInvertCheckbox.setText(getBundle().getString("invertCheckBoxText"));
        mMeasLatestOperatorCheckbox.setText(getBundle().getString("measLatesOperatorCheckBoxText"));
        mMeasIncludeWithoutCheckbox.setText(getBundle().getString("measIncludeWithoutCheckboxText"));
        mNumOfMeasCheckbox.setText(getBundle().getString("numOfMeasCheckBoxText"));
        mMeasAlarmLevelAgeCheckbox.setText("Ålder på larmnivå");
        mMeasSpeedSds.getValueFactory().setConverter(new NegPosStringConverterDouble());
        mDiffMeasLatestSds.getValueFactory().setConverter(new NegPosStringConverterDouble());
        mDiffMeasAllSds.getValueFactory().setConverter(new NegPosStringConverterDouble());
        mDiffMeasPercentageHSis.getValueFactory().setConverter(new NegPosStringConverterInteger());
        mDiffMeasPercentagePSis.getValueFactory().setConverter(new NegPosStringConverterInteger());

        int columnGap = FxHelper.getUIScaled(16);
        int rowGap = FxHelper.getUIScaled(12);
        int titleGap = FxHelper.getUIScaled(3);
        var dimensButton = new Button("Alla dimensioner");
        dimensButton.setOnAction(actionEvent -> {
            List.of(mDimens1Checkbox, mDimens2Checkbox, mDimens3Checkbox).forEach(cb -> cb.setSelected(false));
        });
        var dimensBox = new HBox(FxHelper.getUIScaled(8), mDimens1Checkbox, mDimens2Checkbox, mDimens3Checkbox, new Spacer(), dimensButton);
        dimensBox.setAlignment(Pos.CENTER_LEFT);

        mBaseFilters.getBaseBox().getChildren().add(0, dimensBox);
        mBaseFilters.getBaseBox().getChildren().add(mDisruptorPane.getRoot());

        var leftBox = new VBox(rowGap,
                mBaseFilters.getBaseBorderBox(),
                new Spacer(),
                mBaseFilters.getDateFirstBorderBox(),
                mBaseFilters.getDateLastBorderBox()
        );

        var hGap = FxHelper.getUIScaled(9.0);
        var vGap = FxHelper.getUIScaled(4.0);
        var spinnerWidth = FxHelper.getUIScaled(70.0);

        var diffGridPane = new GridPane(hGap, vGap);
        diffGridPane.addColumn(0, mDiffMeasAllCheckbox, mDiffMeasAllSds);
        diffGridPane.addColumn(1, mDiffMeasLatestCheckbox, mDiffMeasLatestSds);
        FxHelper.autoSizeColumn(diffGridPane, 2);

        var diffPercentGridPane = new GridPane(hGap, vGap);
        diffPercentGridPane.addColumn(0, mDiffMeasPercentageHCheckbox, mDiffMeasPercentageHSis);
        diffPercentGridPane.addColumn(1, mDiffMeasPercentagePCheckbox, mDiffMeasPercentagePSis);
        FxHelper.autoSizeColumn(diffPercentGridPane, 2);

        var yoyoGridPane = new GridPane(hGap, vGap);
        yoyoGridPane.add(mMeasYoyoCheckbox, 0, 0, REMAINING, 1);
        yoyoGridPane.addRow(1, mMeasYoyoCountSds, mMeasYoyoSizeSds);
        FxHelper.autoSizeColumn(yoyoGridPane, 2);

        var displacementGridPane = new GridPane(hGap, vGap);
        displacementGridPane.add(mMeasTopListCheckbox, 0, 0, REMAINING, 1);
        displacementGridPane.addRow(1, mMeasTopListSizeSds, new Label(SDict.POINTS.toLower()));
        displacementGridPane.addRow(2, mMeasTopListLimitSis, mMeasTopListUnitScb);
        mMeasTopListSizeSds.setPrefWidth(spinnerWidth);
        mMeasTopListLimitSis.setPrefWidth(spinnerWidth);

        var alcGridPane = new GridPane(hGap, vGap);
        alcGridPane.add(mMeasAlarmLevelChangeCheckbox, 0, 0, REMAINING, 1);
        alcGridPane.addRow(1, mMeasAlarmLevelChangeLimitSis, mMeasAlarmLevelChangeModeScb);
        alcGridPane.addRow(2, mMeasAlarmLevelChangeValueSis, mMeasAlarmLevelChangeUnitScb);
        mMeasAlarmLevelChangeLimitSis.setPrefWidth(spinnerWidth);
        mMeasAlarmLevelChangeValueSis.setPrefWidth(spinnerWidth);

        FxHelper.autoSizeRegionHorizontal(mMeasTopListUnitScb, mMeasAlarmLevelChangeModeScb, mMeasAlarmLevelChangeUnitScb);

        var measBox = new VBox(rowGap,
                diffGridPane,
                diffPercentGridPane,
                displacementGridPane,
                new VBox(titleGap,
                        mMeasSpeedCheckbox,
                        mMeasSpeedSds
                ),
                yoyoGridPane,
                new Separator(),
                mAlarmSccb,
                new VBox(titleGap,
                        mMeasAlarmLevelAgeCheckbox,
                        mMeasAlarmLevelAgeSis
                ),
                alcGridPane,
                new Separator(),
                new VBox(titleGap,
                        mNumOfMeasCheckbox,
                        mMeasNumOfSis
                ),
                new Separator(),
                mMeasCodeSccb,
                new VBox(titleGap,
                        mMeasOperatorSccb,
                        mMeasLatestOperatorCheckbox
                )
        );

        double borderInnerPadding = FxHelper.getUIScaled(8.0);
        double topBorderInnerPadding = FxHelper.getUIScaled(16.0);

        var wrappedRightBox = Borders.wrap(measBox)
                .etchedBorder()
                .title("Mätdataanalys")
                .innerPadding(topBorderInnerPadding, borderInnerPadding, borderInnerPadding, borderInnerPadding)
                .outerPadding(0)
                .raised()
                .build()
                .build();

        var row = 0;
        var gridPane = new GridPane(columnGap, GAP);
        gridPane.setPadding(FxHelper.getUIScaledInsets(GAP));

        gridPane.addRow(row++, leftBox, wrappedRightBox);
        gridPane.add(mMeasIncludeWithoutCheckbox, 0, row++, GridPane.REMAINING, 1);
        gridPane.add(mSameAlarmCheckbox, 0, row++, GridPane.REMAINING, 1);
        FxHelper.autoSizeColumn(gridPane, 2);

        var root = new BorderPane(gridPane);
        root.setTop(getToolBar());
        getToolBar().getItems().add(new Separator());
        addToToolBar("mc", ActionTextBehavior.SHOW);
        addToToolBar("mr", ActionTextBehavior.SHOW);
        addToToolBar("mm", ActionTextBehavior.SHOW);
        addToToolBar("mp", ActionTextBehavior.SHOW);
        getToolBar().getItems().add(new Separator());
        addToToolBar("copyNames", ActionTextBehavior.HIDE);
        addToToolBar("paste", ActionTextBehavior.HIDE);
        var internalBox = new HBox(FxHelper.getUIScaled(8.0), mInvertCheckbox);
        internalBox.setPadding(FxHelper.getUIScaledInsets(0, 0, 0, 8.0));
        internalBox.setAlignment(Pos.CENTER_LEFT);
        getToolBar().getItems().add(internalBox);
        var spinners = new Spinner[]{
            mDiffMeasAllSds,
            mDiffMeasLatestSds,
            mDiffMeasPercentageHSis,
            mDiffMeasPercentagePSis,
            mMeasSpeedSds,
            mMeasNumOfSis,
            mMeasYoyoCountSds,
            mMeasYoyoSizeSds,
            mMeasAlarmLevelChangeValueSis,
            mMeasAlarmLevelChangeLimitSis,
            mMeasAlarmLevelAgeSis,
            mMeasTopListSizeSds,
            mMeasTopListLimitSis
        };
        FxHelper.setEditable(true, spinners);
        FxHelper.autoCommitSpinners(spinners);
        FxHelper.bindWidthForChildrens(leftBox, measBox, mBaseFilters.getBaseBox());
        FxHelper.bindWidthForRegions(leftBox, mDisruptorPane.getRoot());
        FxHelper.bindWidthForRegions(measBox,
                mMeasSpeedSds,
                mMeasYoyoCountSds,
                mMeasYoyoSizeSds,
                mMeasNumOfSis,
                mMeasAlarmLevelAgeSis,
                mMeasOperatorSccb
        );

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

        int prefWidth = FxHelper.getUIScaled(250);
        leftBox.setPrefWidth(prefWidth);
        measBox.setPrefWidth(prefWidth);

        setContentNode(root);
    }

    private void initListeners() {
        activateCopyNames(actionEvent -> {
            var names = TopoManager.getInstance().getTimeFilteredItems().stream().map(o -> o.getName()).toList();
            copyNames(names);
        });

        activatePasteName(actionEvent -> {
            mFilter.freeTextProperty().set(mManager.getSelectedItem().getName());
            mSameAlarmCheckbox.setSelected(true);
        });

        mFilter.measNumOfProperty().bind(mNumOfMeasCheckbox.selectedProperty());
        mFilter.measAlarmLevelAgeProperty().bind(mMeasAlarmLevelAgeCheckbox.selectedProperty());
        mFilter.measDiffAllProperty().bind(mDiffMeasAllCheckbox.selectedProperty());
        mFilter.measDiffPercentageHProperty().bind(mDiffMeasPercentageHCheckbox.selectedProperty());
        mFilter.measDiffPercentagePProperty().bind(mDiffMeasPercentagePCheckbox.selectedProperty());
        mFilter.measYoyoProperty().bind(mMeasYoyoCheckbox.selectedProperty());
        mFilter.measTopListProperty().bind(mMeasTopListCheckbox.selectedProperty());
        mFilter.invertProperty().bind(mInvertCheckbox.selectedProperty());
        mFilter.measSpeedProperty().bind(mMeasSpeedCheckbox.selectedProperty());
        mFilter.measDiffLatestProperty().bind(mDiffMeasLatestCheckbox.selectedProperty());
        mFilter.measLatestOperatorProperty().bind(mMeasLatestOperatorCheckbox.selectedProperty());
        mFilter.measIncludeWithoutProperty().bind(mMeasIncludeWithoutCheckbox.selectedProperty());
        mFilter.dimens1Property().bind(mDimens1Checkbox.selectedProperty());
        mFilter.dimens2Property().bind(mDimens2Checkbox.selectedProperty());
        mFilter.dimens3Property().bind(mDimens3Checkbox.selectedProperty());
        mFilter.disruptorDistanceProperty().bind(mDisruptorPane.distanceProperty());

        mFilter.measNumOfValueProperty().bind(mMeasNumOfSis.sessionValueProperty());
        mFilter.measAlarmLevelAgeValueProperty().bind(mMeasAlarmLevelAgeSis.sessionValueProperty());
        mFilter.measDiffAllValueProperty().bind(mDiffMeasAllSds.sessionValueProperty());
        mFilter.measDiffPercentageHValueProperty().bind(mDiffMeasPercentageHSis.sessionValueProperty());
        mFilter.measYoyoCountValueProperty().bind(mMeasYoyoCountSds.sessionValueProperty());
        mFilter.measTopListSizeValueProperty().bind(mMeasTopListSizeSds.sessionValueProperty());
        mFilter.measYoyoSizeValueProperty().bind(mMeasYoyoSizeSds.sessionValueProperty());
        mFilter.measDiffLatestValueProperty().bind(mDiffMeasLatestSds.sessionValueProperty());
        mFilter.measDiffPercentagePValueProperty().bind(mDiffMeasPercentagePSis.sessionValueProperty());
        mFilter.measSpeedValueProperty().bind(mMeasSpeedSds.sessionValueProperty());

        mFilter.measAlarmLevelChangeProperty().bind(mMeasAlarmLevelChangeCheckbox.selectedProperty());
        mFilter.measAlarmLevelChangeModeProperty().bind(mMeasAlarmLevelChangeModeScb.getSelectionModel().selectedItemProperty());
        mFilter.measAlarmLevelChangeUnitProperty().bind(mMeasAlarmLevelChangeUnitScb.getSelectionModel().selectedItemProperty());
        mFilter.measTopListUnitProperty().bind(mMeasTopListUnitScb.getSelectionModel().selectedItemProperty());
        mFilter.measAlarmLevelChangeValueProperty().bind(mMeasAlarmLevelChangeValueSis.sessionValueProperty());
        mFilter.measAlarmLevelChangeLimitProperty().bind(mMeasAlarmLevelChangeLimitSis.sessionValueProperty());
        mFilter.measTopListLimitProperty().bind(mMeasTopListLimitSis.sessionValueProperty());

        mFilter.sameAlarmProperty().bind(mSameAlarmCheckbox.selectedProperty());
        mFilter.polygonFilterProperty().bind(usePolygonFilterProperty());
        mFilter.measDateFirstLowProperty().bind(mBaseFilters.getDateRangeFirstPane().lowDateProperty());
        mFilter.measDateFirstHighProperty().bind(mBaseFilters.getDateRangeFirstPane().highDateProperty());
        mFilter.measDateLastLowProperty().bind(mBaseFilters.getDateRangeLastPane().lowDateProperty());
        mFilter.measDateLastHighProperty().bind(mBaseFilters.getDateRangeLastPane().highDateProperty());

        mFilter.mStatusCheckModel = mBaseFilters.getStatusSccb().getCheckModel();
        mFilter.mGroupCheckModel = mBaseFilters.getGroupSccb().getCheckModel();
        mFilter.mCategoryCheckModel = mBaseFilters.getCategorySccb().getCheckModel();
        mFilter.mOperatorCheckModel = mBaseFilters.getOperatorSccb().getCheckModel();
        mFilter.mOriginCheckModel = mBaseFilters.getOriginSccb().getCheckModel();
        mFilter.mMeasNextCheckModel = mBaseFilters.getMeasNextSccb().getCheckModel();
        mFilter.mAlarmNameCheckModel = mBaseFilters.getAlarmNameSccb().getCheckModel();
        mFilter.mDateFromToCheckModel = mBaseFilters.getHasDateFromToSccb().getCheckModel();
        mFilter.mFrequencyCheckModel = mBaseFilters.getFrequencySccb().getCheckModel();
        mFilter.mMeasOperatorsCheckModel = mMeasOperatorSccb.getCheckModel();
        mFilter.mDisruptorCheckModel = mDisruptorPane.getCheckModel();
        mFilter.mAlarmLevelCheckModel = mAlarmSccb.getCheckModel();
        mFilter.mMeasCodeCheckModel = mMeasCodeSccb.getCheckModel();

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

        mFilter.initCheckModelListeners();
    }

    private SessionManager initSession(Preferences preferences) {
        var sessionManager = new SessionManager(preferences);
        sessionManager.register("filter.freeText", mFilter.freeTextProperty());

        sessionManager.register("filter.checkedDisruptors", mDisruptorPane.checkedStringProperty());
        sessionManager.register("filter.checkedNextAlarm", mAlarmSccb.checkedStringProperty());

        sessionManager.register("filter.checkedDimension1", mDimens1Checkbox.selectedProperty());
        sessionManager.register("filter.checkedDimension2", mDimens2Checkbox.selectedProperty());
        sessionManager.register("filter.checkedDimension3", mDimens3Checkbox.selectedProperty());
        sessionManager.register("filter.disruptorDistance", mDisruptorPane.distanceProperty());
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
        sessionManager.register("filter.measIncludeWithout", mMeasIncludeWithoutCheckbox.selectedProperty());
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

        sessionManager.register("filter.invert", mInvertCheckbox.selectedProperty());
        sessionManager.register("filter.sameAlarm", mSameAlarmCheckbox.selectedProperty());

        mBaseFilters.initSession(sessionManager);

        return sessionManager;
    }
}
