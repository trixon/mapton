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

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import static org.mapton.api.ui.MPopOver.GAP;
import static org.mapton.api.ui.MPopOver.autoSize;
import org.mapton.api.ui.forms.NegPosStringConverterDouble;
import org.mapton.api.ui.forms.NegPosStringConverterInteger;
import org.mapton.butterfly_core.api.BaseFilterPopOver;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_topo.api.TopoManager;
import org.mapton.butterfly_topo.shared.AlarmFilter;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;
import se.trixon.almond.util.fx.session.SessionComboBox;
import se.trixon.almond.util.fx.session.SpinnerDoubleSession;
import se.trixon.almond.util.fx.session.SpinnerIntegerSession;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class TopoFilterPopOver extends BaseFilterPopOver {

    private final CheckBox mAlarmLevelChangeCheckbox = new CheckBox();
    private final SessionCheckComboBox<String> mAlarmNameSccb = new SessionCheckComboBox<>();
    private final SessionCheckComboBox<AlarmFilter> mAlarmSccb = new SessionCheckComboBox<>();
    private final SessionCheckComboBox<String> mCategorySccb = new SessionCheckComboBox<>();
    private final double mDefaultDiffValue = 0.020;
    private final int mDefaultNumOfMeasfValue = 1;
    private final CheckBox mDiffMeasAllCheckbox = new CheckBox();
    private final Spinner<Double> mDiffMeasAllSpinner = new Spinner<>(-1.0, 1.0, mDefaultDiffValue, 0.001);
    private final SpinnerDoubleSession mDiffMeasAllSpinnerSession = new SpinnerDoubleSession(mDiffMeasAllSpinner);
    private final CheckBox mDiffMeasLatestCheckbox = new CheckBox();
    private final SessionCheckComboBox<BDimension> mDimensionSccb = new SessionCheckComboBox<>();
    private final TopoFilter mFilter;
    private final SessionCheckComboBox<Integer> mFrequencySccb = new SessionCheckComboBox<>();
    private final SessionCheckComboBox<String> mGroupSccb = new SessionCheckComboBox<>();
    private final SessionCheckComboBox<String> mHasDateFromToSccb = new SessionCheckComboBox<>();
    private final CheckBox mInvertCheckbox = new CheckBox();
    private final TopoManager mManager = TopoManager.getInstance();
    private final SessionCheckComboBox<String> mMeasCodeSccb = new SessionCheckComboBox<>();
    private final Spinner<Double> mMeasDiffLatestSpinner = new Spinner<>(-1.0, 1.0, mDefaultDiffValue, 0.001);
    private final SpinnerDoubleSession mMeasDiffLatestSpinnerSession = new SpinnerDoubleSession(mMeasDiffLatestSpinner);
    private final CheckBox mMeasIncludeWithoutCheckbox = new CheckBox();
    private final CheckBox mMeasLatestOperatorCheckbox = new CheckBox();
    private final SessionComboBox<String> mMeasMaxAgeScb = new SessionComboBox<>();
    private final SessionCheckComboBox<String> mMeasNextSccb = new SessionCheckComboBox<>();
    private final Spinner<Integer> mMeasNumOfSpinner = new Spinner<>(Integer.MIN_VALUE, Integer.MAX_VALUE, mDefaultNumOfMeasfValue);
    private final SpinnerIntegerSession mMeasNumOfSession = new SpinnerIntegerSession(mMeasNumOfSpinner);
    private final SessionCheckComboBox<String> mMeasOperatorSccb = new SessionCheckComboBox<>();
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
    public void clear() {
        getPolygonFilterCheckBox().setSelected(false);
        mFilter.freeTextProperty().set("");

        mSameAlarmCheckbox.setSelected(false);
        mAlarmLevelChangeCheckbox.setSelected(false);
        mDiffMeasLatestCheckbox.setSelected(false);
        mDiffMeasAllCheckbox.setSelected(false);
        mInvertCheckbox.setSelected(false);
        mMeasLatestOperatorCheckbox.setSelected(false);
        mMeasIncludeWithoutCheckbox.setSelected(false);
        mNumOfMeasCheckbox.setSelected(false);

        mMeasDiffLatestSpinner.getValueFactory().setValue(mDefaultDiffValue);
        mDiffMeasAllSpinner.getValueFactory().setValue(mDefaultDiffValue);
        mMeasNumOfSpinner.getValueFactory().setValue(mDefaultNumOfMeasfValue);

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

        mMeasMaxAgeScb.getSelectionModel().select(0);
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

        mMeasMaxAgeScb.load();
        mMeasDiffLatestSpinnerSession.load();
        mDiffMeasAllSpinnerSession.load();
        mMeasNumOfSession.load();
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

        mMeasMaxAgeScb.getItems().setAll(List.of(
                "*",
                "1",
                "7",
                "14",
                "28",
                "84",
                "182",
                "365",
                "∞",
                "NODATA"
        ));

        mMeasMaxAgeScb.setConverter(new StringConverter<String>() {
            @Override
            public String fromString(String s) {
                return s;
            }

            @Override
            public String toString(String s) {
                var timeUnit = StringUtils.equalsIgnoreCase(s, "1") ? Dict.Time.DAY.toString() : Dict.Time.DAYS.toString();

                return "%s: %s %s".formatted(
                        Dict.Time.MAX_AGE.toString(),
                        s,
                        NumberUtils.isDigits(s) ? timeUnit.toLowerCase() : ""
                );
            }
        });

        mMeasNumOfSpinner.getValueFactory().setConverter(new NegPosStringConverterInteger());
        mSameAlarmCheckbox.setText(getBundle().getString("sameAlarmCheckBoxText"));
        mAlarmLevelChangeCheckbox.setText(getBundle().getString("alarmLevelChangeCheckBoxText"));
        mDiffMeasLatestCheckbox.setText(getBundle().getString("diffMeasLatestCheckBoxText"));
        mDiffMeasAllCheckbox.setText(getBundle().getString("diffMeasAllCheckBoxText"));
        mInvertCheckbox.setText(getBundle().getString("invertCheckBoxText"));
        mMeasLatestOperatorCheckbox.setText(getBundle().getString("measLatesOperatorCheckBoxText"));
        mMeasIncludeWithoutCheckbox.setText(getBundle().getString("measIncludeWithoutCheckboxText"));
        mNumOfMeasCheckbox.setText(getBundle().getString("numOfMeasCheckBoxText"));
        mMeasDiffLatestSpinner.getValueFactory().setConverter(new NegPosStringConverterDouble());
        mDiffMeasAllSpinner.getValueFactory().setConverter(new NegPosStringConverterDouble());

        int columnGap = SwingHelper.getUIScaled(16);
        int rowGap = SwingHelper.getUIScaled(8);
        int titleGap = SwingHelper.getUIScaled(2);

        var leftBox = new VBox(rowGap,
                mDimensionSccb,
                mStatusSccb,
                mGroupSccb,
                mCategorySccb,
                mAlarmNameSccb,
                mOperatorSccb,
                mFrequencySccb,
                mHasDateFromToSccb
        );

        var rightBox = new VBox(rowGap,
                mAlarmSccb,
                mMeasNextSccb,
                mMeasMaxAgeScb,
                mMeasCodeSccb,
                new VBox(titleGap,
                        mMeasOperatorSccb,
                        mMeasLatestOperatorCheckbox
                ),
                new VBox(titleGap,
                        mNumOfMeasCheckbox,
                        mMeasNumOfSpinner
                ),
                new VBox(titleGap,
                        mDiffMeasAllCheckbox,
                        mDiffMeasAllSpinner
                ),
                new VBox(titleGap,
                        mDiffMeasLatestCheckbox,
                        mMeasDiffLatestSpinner
                )
        );

        var hBox = new HBox(columnGap, leftBox, rightBox);

        var buttonBox = new GridPane(columnGap, 0);
        buttonBox.addRow(0, getCopyNamesButton(), getPasteNameButton());
        FxHelper.autoSizeColumn(buttonBox, 2);

        var vBox = new VBox(GAP,
                getButtonBox(),
                buttonBox,
                new Separator(),
                hBox,
                new Separator(),
                mAlarmLevelChangeCheckbox,
                mMeasIncludeWithoutCheckbox,
                mSameAlarmCheckbox,
                new Separator(),
                mInvertCheckbox
        );

        FxHelper.setEditable(true, mDiffMeasAllSpinner, mMeasDiffLatestSpinner, mMeasNumOfSpinner);
        FxHelper.autoCommitSpinners(mDiffMeasAllSpinner, mMeasDiffLatestSpinner, mMeasNumOfSpinner);

        leftBox.getChildren().stream()
                .filter(node -> node instanceof Region)
                .map(node -> (Region) node)
                .forEachOrdered(region -> {
                    region.prefWidthProperty().bind(leftBox.widthProperty());
                });

        rightBox.getChildren().stream()
                .filter(node -> node instanceof Region)
                .map(node -> (Region) node)
                .forEachOrdered(region -> {
                    region.prefWidthProperty().bind(rightBox.widthProperty());
                });

        mMeasDiffLatestSpinner.prefWidthProperty().bind(rightBox.widthProperty());
        mDiffMeasAllSpinner.prefWidthProperty().bind(rightBox.widthProperty());
        mMeasNumOfSpinner.prefWidthProperty().bind(rightBox.widthProperty());
        mMeasOperatorSccb.prefWidthProperty().bind(rightBox.widthProperty());

        leftBox.setPrefWidth(FxHelper.getUIScaled(200));
        rightBox.setPrefWidth(FxHelper.getUIScaled(200));

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
        mFilter.invertProperty().bind(mInvertCheckbox.selectedProperty());
        mFilter.measDiffLatestProperty().bind(mDiffMeasLatestCheckbox.selectedProperty());
        mFilter.measLatestOperatorProperty().bind(mMeasLatestOperatorCheckbox.selectedProperty());
        mFilter.measIncludeWithoutProperty().bind(mMeasIncludeWithoutCheckbox.selectedProperty());

        mFilter.measNumOfValueProperty().bind(mMeasNumOfSession.valueProperty());
        mFilter.measDiffAllValueProperty().bind(mDiffMeasAllSpinnerSession.valueProperty());
        mFilter.measDiffLatestValueProperty().bind(mMeasDiffLatestSpinnerSession.valueProperty());

        mFilter.alarmLevelChangeProperty().bind(mAlarmLevelChangeCheckbox.selectedProperty());
        mFilter.sameAlarmProperty().bind(mSameAlarmCheckbox.selectedProperty());
        mFilter.maxAgeProperty().bind(mMeasMaxAgeScb.getSelectionModel().selectedItemProperty());
        mFilter.polygonFilterProperty().bind(getPolygonFilterCheckBox().selectedProperty());

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

        mMeasNumOfSpinner.disableProperty().bind(mNumOfMeasCheckbox.selectedProperty().not());
        mDiffMeasAllSpinner.disableProperty().bind(mDiffMeasAllCheckbox.selectedProperty().not());
        mMeasDiffLatestSpinner.disableProperty().bind(mDiffMeasLatestCheckbox.selectedProperty().not());

        mFilter.initCheckModelListeners();
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
        sessionManager.register("filter.measDiffAllValue", mMeasDiffLatestSpinnerSession.valueProperty());
        sessionManager.register("filter.measDiffLatest", mDiffMeasLatestCheckbox.selectedProperty());
        sessionManager.register("filter.measDiffLatestValue", mMeasDiffLatestSpinnerSession.valueProperty());
        sessionManager.register("filter.measIncludeWithout", mMeasIncludeWithoutCheckbox.selectedProperty());
        sessionManager.register("filter.measLatestOperator", mMeasLatestOperatorCheckbox.selectedProperty());
        sessionManager.register("filter.measMaxAge", mMeasMaxAgeScb.selectedIndexProperty());
        sessionManager.register("filter.measNumOfMeas", mNumOfMeasCheckbox.selectedProperty());
        sessionManager.register("filter.measNumOfValue", mMeasNumOfSession.valueProperty());

        sessionManager.register("filter.invert", mInvertCheckbox.selectedProperty());
        sessionManager.register("filter.sameAlarm", mSameAlarmCheckbox.selectedProperty());
        sessionManager.register("filter.alarmLevelChange", mAlarmLevelChangeCheckbox.selectedProperty());
    }

}
