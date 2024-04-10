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
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.action.ActionUtils.ActionTextBehavior;
import org.controlsfx.tools.Borders;
import org.mapton.api.ui.forms.DateRangePane;
import org.mapton.api.ui.forms.DisruptorPane;
import org.mapton.api.ui.forms.NegPosStringConverterDouble;
import org.mapton.api.ui.forms.NegPosStringConverterInteger;
import org.mapton.butterfly_core.api.BaseFilterPopOver;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_topo.api.TopoManager;
import org.mapton.butterfly_topo.shared.AlarmFilter;
import org.mapton.butterfly_topo.shared.AlarmLevelChangeMode;
import org.mapton.butterfly_topo.shared.AlarmLevelChangeUnit;
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
public class TopoFilterPopOver extends BaseFilterPopOver<TopoFilterFavorite> {

    private final SessionCheckComboBox<String> mAlarmNameSccb = new SessionCheckComboBox<>();
    private final SessionCheckComboBox<AlarmFilter> mAlarmSccb = new SessionCheckComboBox<>(true);
    private final SessionCheckComboBox<String> mCategorySccb = new SessionCheckComboBox<>();
    private final DateRangePane mDateRangePane = new DateRangePane();
    private final double mDefaultDiffValue = 0.020;
    private final int mDefaultMeasAlarmLevelChangeLimit = 1;
    private final int mDefaultMeasAlarmLevelChangeValue = 10;
    private final double mDefaultMeasYoyoCount = 5.0;
    private final double mDefaultMeasYoyoSize = 0.003;
    private final int mDefaultNumOfMeasfValue = 1;
    private final CheckBox mDiffMeasAllCheckbox = new CheckBox();
    private final SessionDoubleSpinner mDiffMeasAllSds = new SessionDoubleSpinner(-1.0, 1.0, mDefaultDiffValue, 0.001);
    private final CheckBox mDiffMeasLatestCheckbox = new CheckBox();
    private final SessionDoubleSpinner mDiffMeasLatestSds = new SessionDoubleSpinner(-1.0, 1.0, mDefaultDiffValue, 0.001);
    private final CheckBox mDimens1Checkbox = new CheckBox("1");
    private final CheckBox mDimens2Checkbox = new CheckBox("2");
    private final CheckBox mDimens3Checkbox = new CheckBox("3");
    private final DisruptorPane mDisruptorPane = new DisruptorPane();
    private final TopoFilter mFilter;
    private final SessionCheckComboBox<Integer> mFrequencySccb = new SessionCheckComboBox<>();
    private final SessionCheckComboBox<String> mGroupSccb = new SessionCheckComboBox<>();
    private final SessionCheckComboBox<String> mHasDateFromToSccb = new SessionCheckComboBox<>(true);
    private final CheckBox mInvertCheckbox = new CheckBox();
    private final TopoManager mManager = TopoManager.getInstance();
    private final CheckBox mMeasAlarmLevelChangeCheckbox = new CheckBox();
    private final SessionIntegerSpinner mMeasAlarmLevelChangeLimitSis = new SessionIntegerSpinner(1, 100, mDefaultMeasAlarmLevelChangeLimit);
    private final SessionComboBox<AlarmLevelChangeMode> mMeasAlarmLevelChangeModeScb = new SessionComboBox<>();
    private final SessionComboBox<AlarmLevelChangeUnit> mMeasAlarmLevelChangeUnitScb = new SessionComboBox<>();
    private final SessionIntegerSpinner mMeasAlarmLevelChangeValueSis = new SessionIntegerSpinner(2, 10000, mDefaultMeasAlarmLevelChangeValue);
    private final SessionCheckComboBox<String> mMeasCodeSccb = new SessionCheckComboBox<>(true);
    private final CheckBox mMeasIncludeWithoutCheckbox = new CheckBox();
    private final CheckBox mMeasLatestOperatorCheckbox = new CheckBox();
    private final SessionCheckComboBox<String> mMeasNextSccb = new SessionCheckComboBox<>(true);
    private final SessionIntegerSpinner mMeasNumOfSis = new SessionIntegerSpinner(Integer.MIN_VALUE, Integer.MAX_VALUE, mDefaultNumOfMeasfValue);
    private final SessionCheckComboBox<String> mMeasOperatorSccb = new SessionCheckComboBox<>();
    private final CheckBox mMeasYoyoCheckbox = new CheckBox();
    private final SessionDoubleSpinner mMeasYoyoCountSds = new SessionDoubleSpinner(0, 100.0, mDefaultMeasYoyoCount, 1.0);
    private final SessionDoubleSpinner mMeasYoyoSizeSds = new SessionDoubleSpinner(0, 1.0, mDefaultMeasYoyoSize, 0.001);
    private final CheckBox mNumOfMeasCheckbox = new CheckBox();
    private final SessionCheckComboBox<String> mOperatorSccb = new SessionCheckComboBox<>();
    private final CheckBox mSameAlarmCheckbox = new CheckBox();
    private final SessionCheckComboBox<String> mStatusSccb = new SessionCheckComboBox<>();

    public TopoFilterPopOver(TopoFilter filter) {
        mFilter = filter;
        createUI();
        initListeners();
        initSession();

        populate();
    }

    @Override
    public void applyFilterFavorite(TopoFilterFavorite filterFavorite) {
        //TODO
    }

    @Override
    public void clear() {
        setUsePolygonFilter(false);
        mFilter.freeTextProperty().set("");

        mDimens1Checkbox.setSelected(false);
        mDimens2Checkbox.setSelected(false);
        mDimens3Checkbox.setSelected(false);

        mSameAlarmCheckbox.setSelected(false);
        mMeasAlarmLevelChangeCheckbox.setSelected(false);
        mDiffMeasLatestCheckbox.setSelected(false);
        mDiffMeasAllCheckbox.setSelected(false);
        mMeasYoyoCheckbox.setSelected(false);
        mInvertCheckbox.setSelected(false);
        mMeasLatestOperatorCheckbox.setSelected(false);
        mMeasIncludeWithoutCheckbox.setSelected(false);
        mNumOfMeasCheckbox.setSelected(false);

        mDiffMeasLatestSds.getValueFactory().setValue(mDefaultDiffValue);
        mDiffMeasAllSds.getValueFactory().setValue(mDefaultDiffValue);
        mMeasYoyoCountSds.getValueFactory().setValue(mDefaultMeasYoyoCount);
        mMeasYoyoSizeSds.getValueFactory().setValue(mDefaultMeasYoyoSize);
        mMeasNumOfSis.getValueFactory().setValue(mDefaultNumOfMeasfValue);
        mMeasAlarmLevelChangeValueSis.getValueFactory().setValue(mDefaultMeasAlarmLevelChangeValue);
        mMeasAlarmLevelChangeLimitSis.getValueFactory().setValue(mDefaultMeasAlarmLevelChangeLimit);

        mStatusSccb.clearChecks();
        mMeasOperatorSccb.clearChecks();
        mGroupSccb.clearChecks();
        mCategorySccb.clearChecks();
        mAlarmNameSccb.clearChecks();
        mOperatorSccb.clearChecks();
        mAlarmSccb.clearChecks();
        mMeasNextSccb.clearChecks();
        mMeasCodeSccb.clearChecks();
        mHasDateFromToSccb.clearChecks();
        mFrequencySccb.clearChecks();

        mDateRangePane.reset();
        mDisruptorPane.reset();
    }

    @Override
    public void load(Butterfly butterfly) {
        var items = butterfly.topo().getControlPoints();

        var allAlarmNames = items.stream().map(o -> o.getNameOfAlarmHeight()).collect(Collectors.toCollection(HashSet::new));
        allAlarmNames.addAll(items.stream().map(o -> o.getNameOfAlarmPlane()).collect(Collectors.toSet()));
        mAlarmNameSccb.loadAndRestoreCheckItems(allAlarmNames.stream());
        mMeasOperatorSccb.loadAndRestoreCheckItems(items.stream().flatMap(p -> p.ext().getObservationsAllRaw().stream().map(o -> o.getOperator())));
        mGroupSccb.loadAndRestoreCheckItems(items.stream().map(o -> o.getGroup()));
        mCategorySccb.loadAndRestoreCheckItems(items.stream().map(o -> o.getCategory()));
        mOperatorSccb.loadAndRestoreCheckItems(items.stream().map(o -> o.getOperator()));
        mStatusSccb.loadAndRestoreCheckItems(items.stream().map(o -> o.getStatus()));
        mFrequencySccb.loadAndRestoreCheckItems(items.stream()
                .filter(o -> o.getFrequency() != null)
                .map(o -> o.getFrequency()));
        mMeasCodeSccb.loadAndRestoreCheckItems();
        mMeasNextSccb.loadAndRestoreCheckItems();
        mAlarmSccb.loadAndRestoreCheckItems();

        mMeasAlarmLevelChangeModeScb.load();
        mMeasAlarmLevelChangeUnitScb.load();
        mMeasAlarmLevelChangeValueSis.load();
        mMeasAlarmLevelChangeLimitSis.load();
        mDiffMeasLatestSds.load();
        mDiffMeasAllSds.load();
        mMeasYoyoCountSds.load();
        mMeasYoyoSizeSds.load();
        mMeasNumOfSis.load();

        mDisruptorPane.load();

        var temporalRange = mManager.getTemporalRange();
        mDateRangePane.setMinMaxDate(temporalRange.getFromLocalDate(), temporalRange.getToLocalDate());

        var sessionManager = getSessionManager();
        sessionManager.register("filter.DateLow", mDateRangePane.lowStringProperty());
        sessionManager.register("filter.DateHigh", mDateRangePane.highStringProperty());
    }

    @Override
    public void onPolygonFilterChange() {
        mFilter.update();
    }

    @Override
    public void onShownFirstTime() {
        FxHelper.setVisibleRowCount(25,
                mGroupSccb,
                mCategorySccb,
                mAlarmNameSccb,
                mMeasOperatorSccb
        );
    }

    @Override
    public void reset() {
        clear();

        mFilter.freeTextProperty().set("*");

        var filterConfig = TopoFilterDefaultsConfig.getInstance().getConfig();
        splitAndCheck(filterConfig.getString("STATUS"), mStatusSccb.getCheckModel());
        splitAndCheck(filterConfig.getString("GROUP"), mGroupSccb.getCheckModel());
        splitAndCheck(filterConfig.getString("CATEGORY"), mCategorySccb.getCheckModel());
        splitAndCheck(filterConfig.getString("OPERATOR"), mOperatorSccb.getCheckModel());
    }

    private void createUI() {
        FxHelper.setShowCheckedCount(true,
                mHasDateFromToSccb,
                mMeasNextSccb,
                mAlarmSccb,
                mMeasCodeSccb,
                mMeasOperatorSccb,
                mStatusSccb,
                mGroupSccb,
                mCategorySccb,
                mAlarmNameSccb,
                mOperatorSccb,
                mFrequencySccb
        );

        mHasDateFromToSccb.setTitle(SDict.VALID_FROM_TO.toString());
        mMeasNextSccb.setTitle(getBundle().getString("nextMeasCheckComboBoxTitle"));
        mAlarmSccb.setTitle(SDict.ALARM_LEVEL.toString());
        mAlarmSccb.getItems().setAll(AlarmFilter.values());
        mMeasCodeSccb.setTitle(getBundle().getString("measCodeCheckComboBoxTitle"));
        mMeasOperatorSccb.setTitle(SDict.SURVEYORS.toString());
        mStatusSccb.setTitle(Dict.STATUS.toString());
        mGroupSccb.setTitle(Dict.GROUP.toString());
        mCategorySccb.setTitle(Dict.CATEGORY.toString());
        mAlarmNameSccb.setTitle(SDict.ALARMS.toString());
        mOperatorSccb.setTitle(SDict.OPERATOR.toString());
        mFrequencySccb.setTitle(SDict.FREQUENCY.toString());

        mHasDateFromToSccb.getItems().setAll(List.of(
                SDict.HAS_VALID_FROM.toString(),
                SDict.HAS_VALID_TO.toString(),
                SDict.WITHOUT_VALID_FROM.toString(),
                SDict.WITHOUT_VALID_TO.toString(),
                SDict.IS_VALID.toString(),
                SDict.IS_INVALID.toString()
        ));

        mMeasAlarmLevelChangeModeScb.getItems().setAll(AlarmLevelChangeMode.values());
        mMeasAlarmLevelChangeUnitScb.getItems().setAll(AlarmLevelChangeUnit.values());

        mMeasNextSccb.getItems().setAll(List.of(
                "<0",
                "0",
                "1-7",
                "8-14",
                "15-28",
                "29-182",
                "∞"
        ));

        mMeasCodeSccb.getItems().setAll(List.of(
                getBundle().getString("measCodeZero"),
                getBundle().getString("measCodeReplacement")
        ));

        mMeasNumOfSis.getValueFactory().setConverter(new NegPosStringConverterInteger());
        mSameAlarmCheckbox.setText(getBundle().getString("sameAlarmCheckBoxText"));
        mMeasAlarmLevelChangeCheckbox.setText(getBundle().getString("measAlarmLevelChangeCheckBoxText"));
        mDiffMeasLatestCheckbox.setText(getBundle().getString("diffMeasLatestCheckBoxText"));
        mDiffMeasAllCheckbox.setText(getBundle().getString("diffMeasAllCheckBoxText"));
        mMeasYoyoCheckbox.setText(getBundle().getString("YoyoCheckBoxText"));
        mInvertCheckbox.setText(getBundle().getString("invertCheckBoxText"));
        mMeasLatestOperatorCheckbox.setText(getBundle().getString("measLatesOperatorCheckBoxText"));
        mMeasIncludeWithoutCheckbox.setText(getBundle().getString("measIncludeWithoutCheckboxText"));
        mNumOfMeasCheckbox.setText(getBundle().getString("numOfMeasCheckBoxText"));
        mDiffMeasLatestSds.getValueFactory().setConverter(new NegPosStringConverterDouble());
        mDiffMeasAllSds.getValueFactory().setConverter(new NegPosStringConverterDouble());

        int columnGap = FxHelper.getUIScaled(16);
        int rowGap = FxHelper.getUIScaled(12);
        int titleGap = FxHelper.getUIScaled(3);
        var dimensButton = new Button("Alla dimensioner");
        dimensButton.setOnAction(actionEvent -> {
            List.of(mDimens1Checkbox, mDimens2Checkbox, mDimens3Checkbox).forEach(cb -> cb.setSelected(false));
        });
        var dimensBox = new HBox(FxHelper.getUIScaled(8), mDimens1Checkbox, mDimens2Checkbox, mDimens3Checkbox, new Spacer(), dimensButton);
        dimensBox.setAlignment(Pos.CENTER_LEFT);

        var basicBox = new VBox(rowGap,
                dimensBox,
                mStatusSccb,
                mGroupSccb,
                mCategorySccb,
                mOperatorSccb,
                mAlarmNameSccb,
                mHasDateFromToSccb,
                mFrequencySccb,
                mMeasNextSccb,
                mDisruptorPane.getRoot()
        );
        double borderInnerPadding = FxHelper.getUIScaled(8.0);
        double topBorderInnerPadding = FxHelper.getUIScaled(16.0);

        var wrappedBasicBox = Borders.wrap(basicBox)
                .etchedBorder()
                .title("Grunddata")
                .innerPadding(topBorderInnerPadding, borderInnerPadding, borderInnerPadding, borderInnerPadding)
                .outerPadding(0)
                .raised()
                .build()
                .build();

        var wrappedDateBox = Borders.wrap(mDateRangePane.getRoot())
                .etchedBorder()
                .title("Period för senaste mätning")
                .innerPadding(topBorderInnerPadding, borderInnerPadding, borderInnerPadding, borderInnerPadding)
                .outerPadding(0)
                .raised()
                .build()
                .build();

        var leftBox = new VBox(rowGap,
                wrappedBasicBox,
                new Spacer(),
                wrappedDateBox
        );

        var rightBox = new VBox(rowGap,
                mAlarmSccb,
                new VBox(titleGap,
                        mNumOfMeasCheckbox,
                        mMeasNumOfSis
                ),
                new VBox(titleGap,
                        mDiffMeasAllCheckbox,
                        mDiffMeasAllSds
                ),
                new VBox(titleGap,
                        mDiffMeasLatestCheckbox,
                        mDiffMeasLatestSds
                ),
                new VBox(titleGap,
                        mMeasYoyoCheckbox,
                        mMeasYoyoCountSds,
                        mMeasYoyoSizeSds
                ),
                new VBox(titleGap,
                        mMeasAlarmLevelChangeCheckbox,
                        new HBox(8, mMeasAlarmLevelChangeLimitSis, mMeasAlarmLevelChangeModeScb),
                        new HBox(8, mMeasAlarmLevelChangeValueSis, mMeasAlarmLevelChangeUnitScb)
                ),
                mMeasCodeSccb,
                new VBox(titleGap,
                        mMeasOperatorSccb,
                        mMeasLatestOperatorCheckbox
                )
        );

        var wrappedRightBox = Borders.wrap(rightBox)
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
        var actionTextBehavior = ActionTextBehavior.HIDE;
        addToToolBar("copyNames", actionTextBehavior);
        addToToolBar("paste", actionTextBehavior);
        var internalBox = new HBox(FxHelper.getUIScaled(8.0), mInvertCheckbox);
        internalBox.setPadding(FxHelper.getUIScaledInsets(0, 0, 0, 8.0));
        internalBox.setAlignment(Pos.CENTER_LEFT);
        getToolBar().getItems().add(internalBox);

        FxHelper.setEditable(true, mDiffMeasAllSds, mDiffMeasLatestSds, mMeasNumOfSis, mMeasYoyoCountSds, mMeasYoyoSizeSds, mMeasAlarmLevelChangeValueSis, mMeasAlarmLevelChangeLimitSis);
        FxHelper.autoCommitSpinners(mDiffMeasAllSds, mDiffMeasLatestSds, mMeasNumOfSis, mMeasYoyoCountSds, mMeasYoyoSizeSds, mMeasAlarmLevelChangeValueSis, mMeasAlarmLevelChangeLimitSis);
        FxHelper.bindWidthForChildrens(leftBox, rightBox, basicBox);
        FxHelper.bindWidthForRegions(leftBox, mDisruptorPane.getRoot());
        FxHelper.bindWidthForRegions(rightBox,
                mDiffMeasLatestSds,
                mDiffMeasAllSds,
                mMeasYoyoCountSds,
                mMeasYoyoSizeSds,
                mMeasNumOfSis,
                mMeasOperatorSccb
        );

        mMeasAlarmLevelChangeValueSis.setMinWidth(FxHelper.getUIScaled(90));
        mMeasAlarmLevelChangeLimitSis.setMinWidth(FxHelper.getUIScaled(90));
        mMeasAlarmLevelChangeModeScb.setPrefWidth(400);
        mMeasAlarmLevelChangeUnitScb.setPrefWidth(400);

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
        rightBox.setPrefWidth(prefWidth);

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
        mFilter.measDiffAllProperty().bind(mDiffMeasAllCheckbox.selectedProperty());
        mFilter.measYoyoProperty().bind(mMeasYoyoCheckbox.selectedProperty());
        mFilter.invertProperty().bind(mInvertCheckbox.selectedProperty());
        mFilter.measDiffLatestProperty().bind(mDiffMeasLatestCheckbox.selectedProperty());
        mFilter.measLatestOperatorProperty().bind(mMeasLatestOperatorCheckbox.selectedProperty());
        mFilter.measIncludeWithoutProperty().bind(mMeasIncludeWithoutCheckbox.selectedProperty());
        mFilter.dimens1Property().bind(mDimens1Checkbox.selectedProperty());
        mFilter.dimens2Property().bind(mDimens2Checkbox.selectedProperty());
        mFilter.dimens3Property().bind(mDimens3Checkbox.selectedProperty());
        mFilter.disruptorDistanceProperty().bind(mDisruptorPane.distanceProperty());

        mFilter.measNumOfValueProperty().bind(mMeasNumOfSis.sessionValueProperty());
        mFilter.measDiffAllValueProperty().bind(mDiffMeasAllSds.sessionValueProperty());
        mFilter.measYoyoCountValueProperty().bind(mMeasYoyoCountSds.sessionValueProperty());
        mFilter.measYoyoSizeValueProperty().bind(mMeasYoyoSizeSds.sessionValueProperty());
        mFilter.measDiffLatestValueProperty().bind(mDiffMeasLatestSds.sessionValueProperty());

        mFilter.measAlarmLevelChangeProperty().bind(mMeasAlarmLevelChangeCheckbox.selectedProperty());
        mFilter.measAlarmLevelChangeModeProperty().bind(mMeasAlarmLevelChangeModeScb.getSelectionModel().selectedItemProperty());
        mFilter.measAlarmLevelChangeUnitProperty().bind(mMeasAlarmLevelChangeUnitScb.getSelectionModel().selectedItemProperty());
        mFilter.measAlarmLevelChangeValueProperty().bind(mMeasAlarmLevelChangeValueSis.sessionValueProperty());
        mFilter.measAlarmLevelChangeLimitProperty().bind(mMeasAlarmLevelChangeLimitSis.sessionValueProperty());

        mFilter.sameAlarmProperty().bind(mSameAlarmCheckbox.selectedProperty());
        mFilter.polygonFilterProperty().bind(usePolygonFilterProperty());
        mFilter.measDateLowProperty().bind(mDateRangePane.lowDateProperty());
        mFilter.measDateHighProperty().bind(mDateRangePane.highDateProperty());

        mFilter.mStatusCheckModel = mStatusSccb.getCheckModel();
        mFilter.mMeasOperatorsCheckModel = mMeasOperatorSccb.getCheckModel();
        mFilter.mGroupCheckModel = mGroupSccb.getCheckModel();
        mFilter.mDisruptorCheckModel = mDisruptorPane.getCheckModel();
        mFilter.mCategoryCheckModel = mCategorySccb.getCheckModel();
        mFilter.mAlarmNameCheckModel = mAlarmNameSccb.getCheckModel();
        mFilter.mOperatorCheckModel = mOperatorSccb.getCheckModel();
        mFilter.mMeasNextCheckModel = mMeasNextSccb.getCheckModel();
        mFilter.mAlarmNameCheckModel = mAlarmNameSccb.getCheckModel();
        mFilter.mAlarmCheckModel = mAlarmSccb.getCheckModel();
        mFilter.mMeasCodeCheckModel = mMeasCodeSccb.getCheckModel();
        mFilter.mDateFromToCheckModel = mHasDateFromToSccb.getCheckModel();
        mFilter.mFrequencyCheckModel = mFrequencySccb.getCheckModel();

        mMeasNumOfSis.disableProperty().bind(mNumOfMeasCheckbox.selectedProperty().not());
        mDiffMeasAllSds.disableProperty().bind(mDiffMeasAllCheckbox.selectedProperty().not());
        mDiffMeasLatestSds.disableProperty().bind(mDiffMeasLatestCheckbox.selectedProperty().not());
        mMeasYoyoCountSds.disableProperty().bind(mMeasYoyoCheckbox.selectedProperty().not());
        mMeasYoyoSizeSds.disableProperty().bind(mMeasYoyoCheckbox.selectedProperty().not());

        mMeasAlarmLevelChangeLimitSis.disableProperty().bind(mMeasAlarmLevelChangeCheckbox.selectedProperty().not());
        mMeasAlarmLevelChangeModeScb.disableProperty().bind(mMeasAlarmLevelChangeCheckbox.selectedProperty().not());
        mMeasAlarmLevelChangeUnitScb.disableProperty().bind(mMeasAlarmLevelChangeCheckbox.selectedProperty().not());
        mMeasAlarmLevelChangeValueSis.disableProperty().bind(mMeasAlarmLevelChangeCheckbox.selectedProperty().not());

        mFilter.initCheckModelListeners();
    }

    private void initSession() {
        var sessionManager = getSessionManager();
        sessionManager.register("filter.freeText", mFilter.freeTextProperty());

        sessionManager.register("filter.checkedAlarmName", mAlarmNameSccb.checkedStringProperty());
        sessionManager.register("filter.checkedCategory", mCategorySccb.checkedStringProperty());
        sessionManager.register("filter.checkedDateFromTo", mHasDateFromToSccb.checkedStringProperty());
        sessionManager.register("filter.checkedFrequency", mFrequencySccb.checkedStringProperty());
        sessionManager.register("filter.checkedGroup", mGroupSccb.checkedStringProperty());
        sessionManager.register("filter.checkedDisruptors", mDisruptorPane.checkedStringProperty());
        sessionManager.register("filter.checkedNextAlarm", mAlarmSccb.checkedStringProperty());
        sessionManager.register("filter.checkedPerformers", mOperatorSccb.checkedStringProperty());
        sessionManager.register("filter.checkedStatus", mStatusSccb.checkedStringProperty());

        sessionManager.register("filter.checkedDimension1", mDimens1Checkbox.selectedProperty());
        sessionManager.register("filter.checkedDimension2", mDimens2Checkbox.selectedProperty());
        sessionManager.register("filter.checkedDimension3", mDimens3Checkbox.selectedProperty());
        sessionManager.register("filter.disruptorDistance", mDisruptorPane.distanceProperty());
        sessionManager.register("filter.measCheckedMeasCode", mMeasCodeSccb.checkedStringProperty());
        sessionManager.register("filter.measCheckedNextMeas", mMeasNextSccb.checkedStringProperty());
        sessionManager.register("filter.measCheckedOperators", mMeasOperatorSccb.checkedStringProperty());
        sessionManager.register("filter.measDiffAll", mDiffMeasAllCheckbox.selectedProperty());
        sessionManager.register("filter.measYoyo", mMeasYoyoCheckbox.selectedProperty());
        sessionManager.register("filter.measDiffAllValue", mDiffMeasLatestSds.sessionValueProperty());
        sessionManager.register("filter.measYoyoCountValue", mMeasYoyoCountSds.sessionValueProperty());
        sessionManager.register("filter.measYoyoSizeValue", mMeasYoyoSizeSds.sessionValueProperty());
        sessionManager.register("filter.measDiffLatest", mDiffMeasLatestCheckbox.selectedProperty());
        sessionManager.register("filter.measDiffLatestValue", mDiffMeasLatestSds.sessionValueProperty());
        sessionManager.register("filter.measIncludeWithout", mMeasIncludeWithoutCheckbox.selectedProperty());
        sessionManager.register("filter.measLatestOperator", mMeasLatestOperatorCheckbox.selectedProperty());
        sessionManager.register("filter.measNumOfMeas", mNumOfMeasCheckbox.selectedProperty());
        sessionManager.register("filter.measNumOfValue", mMeasNumOfSis.sessionValueProperty());
        sessionManager.register("filter.measAlarmLevelChange", mMeasAlarmLevelChangeCheckbox.selectedProperty());
        sessionManager.register("filter.measAlarmLevelChangeMode", mMeasAlarmLevelChangeModeScb.selectedIndexProperty());
        sessionManager.register("filter.measAlarmLevelChangeUnit", mMeasAlarmLevelChangeUnitScb.selectedIndexProperty());
        sessionManager.register("filter.measAlarmLevelChangeValue", mMeasAlarmLevelChangeValueSis.sessionValueProperty());
        sessionManager.register("filter.measAlarmLevelChangeLimit", mMeasAlarmLevelChangeLimitSis.sessionValueProperty());

        sessionManager.register("filter.invert", mInvertCheckbox.selectedProperty());
        sessionManager.register("filter.sameAlarm", mSameAlarmCheckbox.selectedProperty());
    }
}
