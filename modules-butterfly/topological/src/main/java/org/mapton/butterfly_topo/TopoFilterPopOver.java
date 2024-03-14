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
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.tools.Borders;
import static org.mapton.api.ui.MPopOver.GAP;
import static org.mapton.api.ui.MPopOver.autoSize;
import org.mapton.api.ui.forms.NegPosStringConverterDouble;
import org.mapton.api.ui.forms.NegPosStringConverterInteger;
import org.mapton.butterfly_core.api.BaseFilterPopOver;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_topo.api.TopoManager;
import org.mapton.butterfly_topo.shared.AlarmFilter;
import org.mapton.butterfly_topo.shared.AlarmLevelChangeMode;
import org.mapton.butterfly_topo.shared.AlarmLevelChangeUnit;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.DatePane;
import se.trixon.almond.util.fx.control.TemporalPreset;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;
import se.trixon.almond.util.fx.session.SessionComboBox;
import se.trixon.almond.util.fx.session.SessionDoubleSpinner;
import se.trixon.almond.util.fx.session.SessionIntegerSpinner;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class TopoFilterPopOver extends BaseFilterPopOver<TopoFilterFavorite> {

    private final SessionCheckComboBox<String> mAlarmNameSccb = new SessionCheckComboBox<>();
    private final SessionCheckComboBox<AlarmFilter> mAlarmSccb = new SessionCheckComboBox<>(true);
    private final SessionCheckComboBox<String> mCategorySccb = new SessionCheckComboBox<>();
    private final DatePane mDatePane = new DatePane();
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
    private final SessionCheckComboBox<BDimension> mDimensionSccb = new SessionCheckComboBox<>();
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
    private final SplitMenuButton mPresetSplitMenuButton = new SplitMenuButton();
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
        getPolygonFilterCheckBox().setSelected(false);
        mFilter.freeTextProperty().set("");

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

        mDimensionSccb.clearChecks();
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

        mDatePane.reset();
    }

    @Override
    public void load(Butterfly butterfly) {
        var items = butterfly.topo().getControlPoints();

        var allAlarmNames = items.stream().map(o -> o.getNameOfAlarmHeight()).collect(Collectors.toCollection(HashSet::new));
        allAlarmNames.addAll(items.stream().map(o -> o.getNameOfAlarmPlane()).collect(Collectors.toSet()));
        mAlarmNameSccb.loadAndRestoreCheckItems(allAlarmNames.stream());
        mMeasOperatorSccb.loadAndRestoreCheckItems(items.stream().flatMap(p -> p.ext().getObservationsAllRaw().stream().map(o -> o.getOperator())));
        mDimensionSccb.loadAndRestoreCheckItems(items.stream().map(o -> o.getDimension()));
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

        var temporalRange = mManager.getTemporalRange();
        mDatePane.setMinMaxDate(temporalRange.getFromLocalDate(), temporalRange.getToLocalDate());

        var sessionManager = getSessionManager();
        var dateRangeSlider = mDatePane.getDateRangeSlider();
        sessionManager.register("filter.DateLow", dateRangeSlider.lowStringProperty());
        sessionManager.register("filter.DateHigh", dateRangeSlider.highStringProperty());
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
    }

    private MenuItem createPresetMenuItem(String name, LocalDate lowDate, LocalDate highDate) {
        var preset = new TemporalPreset(name, lowDate, highDate);
        var menuItem = new MenuItem(preset.name());
        menuItem.setOnAction(actionEvent -> {
            mDatePane.getDateRangeSlider().setLowHighDate(preset.lowDate(), preset.highDate());
        });
        return menuItem;
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
                mFrequencySccb,
                mDimensionSccb
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
        mDimensionSccb.setTitle(SDict.DIMENSION.toString());

        mDimensionSccb.setConverter(new StringConverter<BDimension>() {
            @Override
            public BDimension fromString(String string) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String toString(BDimension object) {
                return String.valueOf(object.ordinal() + 1);
            }
        });

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

        int columnGap = SwingHelper.getUIScaled(16);
        int rowGap = SwingHelper.getUIScaled(12);
        int titleGap = SwingHelper.getUIScaled(3);

        mPresetSplitMenuButton.setText(Dict.RESET.toString());
        var latestMeasBox = new VBox(mDatePane, mPresetSplitMenuButton);
        latestMeasBox.setAlignment(Pos.TOP_RIGHT);

        var basicBox = new VBox(rowGap,
                mDimensionSccb,
                mStatusSccb,
                mGroupSccb,
                mCategorySccb,
                mOperatorSccb,
                mAlarmNameSccb,
                mHasDateFromToSccb,
                mFrequencySccb,
                mMeasNextSccb
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

        var wrappedMeasBox = Borders.wrap(latestMeasBox)
                .etchedBorder()
                .title("Period för senaste mätning")
                .innerPadding(topBorderInnerPadding, borderInnerPadding, borderInnerPadding, borderInnerPadding)
                .outerPadding(0)
                .raised()
                .build()
                .build();

        Spacer spacer = new Spacer();
        spacer.setActive(true);

        var leftBox = new VBox(rowGap,
                wrappedBasicBox,
                spacer,
                wrappedMeasBox
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

        var gp = new GridPane(columnGap, 0);
        gp.addRow(0, leftBox, wrappedRightBox);
        var buttonBox = new GridPane(columnGap, 0);
        buttonBox.addRow(0, getCopyNamesButton(), getPasteNameButton());
        FxHelper.autoSizeColumn(buttonBox, 2);
        FxHelper.autoSizeColumn(gp, 2);
        var vBox = new VBox(GAP,
                getButtonBox(),
                buttonBox,
                new Separator(),
                gp,
                new Separator(),
                mMeasIncludeWithoutCheckbox,
                mSameAlarmCheckbox,
                new Separator(),
                mInvertCheckbox
        );

        FxHelper.setEditable(true, mDiffMeasAllSds, mDiffMeasLatestSds, mMeasNumOfSis, mMeasYoyoCountSds, mMeasYoyoSizeSds, mMeasAlarmLevelChangeValueSis, mMeasAlarmLevelChangeLimitSis);
        FxHelper.autoCommitSpinners(mDiffMeasAllSds, mDiffMeasLatestSds, mMeasNumOfSis, mMeasYoyoCountSds, mMeasYoyoSizeSds, mMeasAlarmLevelChangeValueSis, mMeasAlarmLevelChangeLimitSis);
        FxHelper.bindWidthForChildrens(leftBox, rightBox, basicBox);
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

        buttonBox.getChildren().stream()
                .filter(n -> n instanceof Region)
                .map(n -> (Region) n)
                .forEach(r -> FxHelper.autoSizeRegionHorizontal(r));

        autoSize(vBox);
        setContentNode(vBox);
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
        mFilter.polygonFilterProperty().bind(getPolygonFilterCheckBox().selectedProperty());
        mFilter.measDateLowProperty().bind(mDatePane.getDateRangeSlider().lowDateProperty());
        mFilter.measDateHighProperty().bind(mDatePane.getDateRangeSlider().highDateProperty());

        mFilter.mDimensionCheckModel = mDimensionSccb.getCheckModel();
        mFilter.mStatusCheckModel = mStatusSccb.getCheckModel();
        mFilter.mMeasOperatorsCheckModel = mMeasOperatorSccb.getCheckModel();
        mFilter.mGroupCheckModel = mGroupSccb.getCheckModel();
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

        mPresetSplitMenuButton.setOnShowing(event -> {
            populatePresets();
        });

        mPresetSplitMenuButton.setOnAction(ae -> {
            mDatePane.reset();
        });
    }

    private void initSession() {
        var sessionManager = getSessionManager();
        sessionManager.register("filter.freeText", mFilter.freeTextProperty());

        sessionManager.register("filter.checkedAlarmName", mAlarmNameSccb.checkedStringProperty());
        sessionManager.register("filter.checkedCategory", mCategorySccb.checkedStringProperty());
        sessionManager.register("filter.checkedDateFromTo", mHasDateFromToSccb.checkedStringProperty());
        sessionManager.register("filter.checkedDimensions", mDimensionSccb.checkedStringProperty());
        sessionManager.register("filter.checkedFrequency", mFrequencySccb.checkedStringProperty());
        sessionManager.register("filter.checkedGroup", mGroupSccb.checkedStringProperty());
        sessionManager.register("filter.checkedNextAlarm", mAlarmSccb.checkedStringProperty());
        sessionManager.register("filter.checkedPerformers", mOperatorSccb.checkedStringProperty());
        sessionManager.register("filter.checkedStatus", mStatusSccb.checkedStringProperty());

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

    private void populatePresets() {
        mPresetSplitMenuButton.getItems().clear();
        var now = LocalDate.now();
        var latestMenu = new Menu("Senaste");
        var presentMenu = new Menu("Innevarande");
        var previousMenu = new Menu("Föregående");

        mPresetSplitMenuButton.getItems().setAll(
                createPresetMenuItem(Dict.Time.TODAY.toString(), now, now),
                new SeparatorMenuItem(),
                latestMenu,
                presentMenu,
                previousMenu
        );

        latestMenu.getItems().addAll(
                createPresetMenuItem("dygnet", now.minusDays(1), now),
                createPresetMenuItem("veckan", now.minusWeeks(1), now),
                createPresetMenuItem("två veckorna", now.minusWeeks(2), now),
                createPresetMenuItem("månaden", now.minusMonths(1), now),
                createPresetMenuItem("tre månaderna", now.minusMonths(3), now),
                createPresetMenuItem("sex månaderna", now.minusMonths(6), now),
                createPresetMenuItem("året", now.minusYears(1), now),
                createPresetMenuItem("två åren", now.minusYears(2), now)
        );

        presentMenu.getItems().addAll(
                createPresetMenuItem("månad", now.withDayOfMonth(1), now),
                createPresetMenuItem("år", now.withDayOfYear(1), now)
        );

        var prevMonthStart = now.minusMonths(1).withDayOfMonth(1);
        var prevMonthEnd = prevMonthStart.withDayOfMonth(prevMonthStart.lengthOfMonth());
        var prevYearStart = now.minusYears(1).withDayOfYear(1);
        var prevYearEnd = prevYearStart.withDayOfYear(prevYearStart.lengthOfYear());
        previousMenu.getItems().addAll(
                createPresetMenuItem("månad", prevMonthStart, prevMonthEnd),
                createPresetMenuItem("år", prevYearStart, prevYearEnd)
        );
    }
}
