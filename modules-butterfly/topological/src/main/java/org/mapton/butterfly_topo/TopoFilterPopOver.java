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

import org.mapton.butterfly_topo.api.TopoManager;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javafx.event.EventType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.controlsfx.control.CheckComboBox;
import static org.mapton.api.ui.MPopOver.GAP;
import static org.mapton.api.ui.MPopOver.autoSize;
import org.mapton.api.ui.forms.NegPosStringConverterDouble;
import org.mapton.api.ui.forms.NegPosStringConverterInteger;
import org.mapton.butterfly_api.api.BaseFilterPopOver;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_topo.shared.AlarmFilter;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.CheckModelSession;
import se.trixon.almond.util.fx.session.SelectionModelSession;
import se.trixon.almond.util.fx.session.SpinnerDoubleSession;
import se.trixon.almond.util.fx.session.SpinnerIntegerSession;

/**
 *
 * @author Patrik Karlström
 */
public class TopoFilterPopOver extends BaseFilterPopOver {

    private final CheckComboBox<AlarmFilter> mAlarmCheckComboBox = new CheckComboBox<>();
    private final CheckModelSession mAlarmCheckModelSession = new CheckModelSession(mAlarmCheckComboBox);
    private final CheckComboBox<String> mCategoryCheckComboBox = new CheckComboBox<>();
    private final CheckModelSession mCategoryCheckModelSession = new CheckModelSession(mCategoryCheckComboBox);
    private final double mDefaultDiffValue = 0.020;
    private final int mDefaultNumOfMeasfValue = 1;
    private final CheckBox mDiffMeasAllCheckbox = new CheckBox();
    private final Spinner<Double> mDiffMeasAllSpinner = new Spinner<>(-1.0, 1.0, mDefaultDiffValue, 0.001);
    private final SpinnerDoubleSession mDiffMeasAllSpinnerSession = new SpinnerDoubleSession(mDiffMeasAllSpinner);
    private final CheckBox mDiffMeasLatestCheckbox = new CheckBox();
    private final Spinner<Double> mDiffMeasLatestSpinner = new Spinner<>(-1.0, 1.0, mDefaultDiffValue, 0.001);
    private final SpinnerDoubleSession mDiffMeasLatestSpinnerSession = new SpinnerDoubleSession(mDiffMeasLatestSpinner);
    private final CheckComboBox<BDimension> mDimensionCheckComboBox = new CheckComboBox<>();
    private final CheckModelSession mDimensionCheckModelSession = new CheckModelSession(mDimensionCheckComboBox);
    private final TopoFilter mFilter;
    private boolean mFirstRun = true;
    private final CheckComboBox<Integer> mFrequencyCheckComboBox = new CheckComboBox<>();
    private final CheckModelSession mFrequencyCheckModelSession = new CheckModelSession(mFrequencyCheckComboBox);
    private final CheckComboBox<String> mGroupCheckComboBox = new CheckComboBox<>();
    private final CheckModelSession mGroupCheckModelSession = new CheckModelSession(mGroupCheckComboBox);
    private final CheckComboBox<String> mHasDateFromToComboBox = new CheckComboBox<>();
    private final CheckModelSession mHasDateFromToCheckModelSession = new CheckModelSession(mHasDateFromToComboBox);
    private final CheckBox mInvertCheckbox = new CheckBox();
    private final TopoManager mManager = TopoManager.getInstance();
    private final ComboBox<String> mMaxAgeComboBox = new ComboBox<>();
    private final SelectionModelSession mMaxAgeSelectionModelSession = new SelectionModelSession(mMaxAgeComboBox.getSelectionModel());
    private final CheckComboBox<String> mMeasCodeCheckComboBox = new CheckComboBox<>();
    private final CheckModelSession mMeasCodeCheckModelSession = new CheckModelSession(mMeasCodeCheckComboBox);
    private final CheckBox mMeasIncludeWithoutCheckbox = new CheckBox();
    private final CheckBox mMeasLatestOperatorCheckbox = new CheckBox();
    private final CheckComboBox<String> mMeasOperatorsCheckComboBox = new CheckComboBox<>();
    private final CheckModelSession mMeasOperatorsCheckModelSession = new CheckModelSession(mMeasOperatorsCheckComboBox);
    private final CheckComboBox<String> mNextMeasCheckComboBox = new CheckComboBox<>();
    private final CheckModelSession mNextMeasCheckModelSession = new CheckModelSession(mNextMeasCheckComboBox);
    private final CheckBox mNumOfMeasCheckbox = new CheckBox();
    private final Spinner<Integer> mNumOfMeasSpinner = new Spinner<>(Integer.MIN_VALUE, Integer.MAX_VALUE, mDefaultNumOfMeasfValue);
    private final SpinnerIntegerSession mNumOfMeasSession = new SpinnerIntegerSession(mNumOfMeasSpinner);
    private final CheckComboBox<String> mOperatorCheckComboBox = new CheckComboBox<>();
    private final CheckModelSession mOperatorCheckModelSession = new CheckModelSession(mOperatorCheckComboBox);
    private final CheckBox mSameAlarmCheckbox = new CheckBox();
    private final CheckComboBox<String> mStatusCheckComboBox = new CheckComboBox<>();
    private final CheckModelSession mStatusCheckModelSession = new CheckModelSession(mStatusCheckComboBox);

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
        mDiffMeasLatestCheckbox.setSelected(false);
        mDiffMeasAllCheckbox.setSelected(false);
        mInvertCheckbox.setSelected(false);
        mMeasLatestOperatorCheckbox.setSelected(false);
        mMeasIncludeWithoutCheckbox.setSelected(false);
        mNumOfMeasCheckbox.setSelected(false);

        mDiffMeasLatestSpinner.getValueFactory().setValue(mDefaultDiffValue);
        mDiffMeasAllSpinner.getValueFactory().setValue(mDefaultDiffValue);
        mNumOfMeasSpinner.getValueFactory().setValue(mDefaultNumOfMeasfValue);
        mDimensionCheckComboBox.getCheckModel().clearChecks();
        mStatusCheckComboBox.getCheckModel().clearChecks();
        mMeasOperatorsCheckComboBox.getCheckModel().clearChecks();
        mMeasOperatorsCheckComboBox.getCheckModel().clearChecks();
        mGroupCheckComboBox.getCheckModel().clearChecks();
        mCategoryCheckComboBox.getCheckModel().clearChecks();
        mOperatorCheckComboBox.getCheckModel().clearChecks();
        mAlarmCheckComboBox.getCheckModel().clearChecks();
        mNextMeasCheckComboBox.getCheckModel().clearChecks();
        mMeasCodeCheckComboBox.getCheckModel().clearChecks();
        mHasDateFromToComboBox.getCheckModel().clearChecks();
        mFrequencyCheckComboBox.getCheckModel().clearChecks();
        mMaxAgeComboBox.getSelectionModel().select(0);
    }

    @Override
    public void load(Butterfly butterfly) {
        var topoControlPoints = butterfly.getTopoControlPoints();

        var dimensionCheckModel = mDimensionCheckComboBox.getCheckModel();
        var checkedDimensions = dimensionCheckModel.getCheckedItems();
        var allDimensions = new TreeSet<>(topoControlPoints.stream().map(o -> o.getDimension()).collect(Collectors.toSet()));

        mDimensionCheckComboBox.getItems().setAll(allDimensions);
        checkedDimensions.stream().forEach(d -> dimensionCheckModel.check(d));
//
        var statusCheckModel = mStatusCheckComboBox.getCheckModel();
        var checkedStatus = statusCheckModel.getCheckedItems();
        var allStatus = new TreeSet<>(topoControlPoints.stream().map(o -> o.getStatus()).collect(Collectors.toSet()));

        mStatusCheckComboBox.getItems().setAll(allStatus);
        checkedStatus.stream().forEach(d -> statusCheckModel.check(d));
//
        var measOperatorsCheckModel = mMeasOperatorsCheckComboBox.getCheckModel();
        var checkedMeasOperators = measOperatorsCheckModel.getCheckedItems();
        var allMeasOperator = new TreeSet<>(topoControlPoints.stream()
                .flatMap(p -> p.ext().getObservationsRaw().stream().map(o -> o.getOperator()))
                .collect(Collectors.toSet()));

        mMeasOperatorsCheckComboBox.getItems().setAll(allMeasOperator);
        checkedMeasOperators.stream().forEach(d -> measOperatorsCheckModel.check(d));
//
        var groupCheckModel = mGroupCheckComboBox.getCheckModel();
        var checkedGroup = groupCheckModel.getCheckedItems();
        var allGroupss = new TreeSet<>(topoControlPoints.stream().map(o -> o.getGroup()).collect(Collectors.toSet()));

        mGroupCheckComboBox.getItems().setAll(allGroupss);
        checkedGroup.stream().forEach(d -> groupCheckModel.check(d));
//
        var categoryCheckModel = mCategoryCheckComboBox.getCheckModel();
        var checkedCategory = categoryCheckModel.getCheckedItems();
        var allCategory = new TreeSet<>(topoControlPoints.stream().map(o -> o.getCategory()).collect(Collectors.toSet()));

        mCategoryCheckComboBox.getItems().setAll(allCategory);
        checkedCategory.stream().forEach(d -> categoryCheckModel.check(d));
//
        var performerCheckModel = mOperatorCheckComboBox.getCheckModel();
        var checkedPerformer = performerCheckModel.getCheckedItems();
        var allPerformers = new TreeSet<>(topoControlPoints.stream().map(o -> o.getOperator()).collect(Collectors.toSet()));

        mOperatorCheckComboBox.getItems().setAll(allPerformers);
        checkedPerformer.stream().forEach(d -> performerCheckModel.check(d));

        var frequencyCheckModel = mFrequencyCheckComboBox.getCheckModel();
        var checkedFrequency = frequencyCheckModel.getCheckedItems();
        var allFrequency = new TreeSet<>(topoControlPoints.stream()
                .filter(o -> o.getFrequency() != null)
                .map(o -> o.getFrequency())
                .collect(Collectors.toSet()));

        mFrequencyCheckComboBox.getItems().setAll(allFrequency);
        checkedFrequency.stream().forEach(d -> frequencyCheckModel.check(d));

        mDimensionCheckModelSession.load();
        mStatusCheckModelSession.load();
        mMeasOperatorsCheckModelSession.load();
        mCategoryCheckModelSession.load();
        mGroupCheckModelSession.load();
        mOperatorCheckModelSession.load();
        mFrequencyCheckModelSession.load();
        mMaxAgeSelectionModelSession.load();
        mNextMeasCheckModelSession.load();
        mAlarmCheckModelSession.load();
        mMeasCodeCheckModelSession.load();
        mHasDateFromToCheckModelSession.load();
        mDiffMeasLatestSpinnerSession.load();
        mDiffMeasAllSpinnerSession.load();
        mNumOfMeasSession.load();
    }

    @Override
    public void onPolygonFilterChange() {
        mFilter.update();
    }

    @Override
    public void reset() {
        clear();

        mFilter.freeTextProperty().set("*");
    }

    private void createUI() {
        mHasDateFromToComboBox.setShowCheckedCount(true);
        mHasDateFromToComboBox.setTitle(SDict.VALID_FROM_TO.toString());

        mNextMeasCheckComboBox.setShowCheckedCount(true);
        mNextMeasCheckComboBox.setTitle(getBundle().getString("nextMeasCheckComboBoxTitle"));

        mAlarmCheckComboBox.setShowCheckedCount(true);
        mAlarmCheckComboBox.setTitle(SDict.ALARM.toString());
        mAlarmCheckComboBox.getItems().setAll(AlarmFilter.values());

        mMeasCodeCheckComboBox.setShowCheckedCount(true);
        mMeasCodeCheckComboBox.setTitle(getBundle().getString("measCodeCheckComboBoxTitle"));

        mMeasOperatorsCheckComboBox.setShowCheckedCount(true);
        mMeasOperatorsCheckComboBox.setTitle(SDict.SURVEYORS.toString());

        mStatusCheckComboBox.setShowCheckedCount(true);
        mStatusCheckComboBox.setTitle(Dict.STATUS.toString());

        mGroupCheckComboBox.setShowCheckedCount(true);
        mGroupCheckComboBox.setTitle(Dict.GROUP.toString());

        mCategoryCheckComboBox.setShowCheckedCount(true);
        mCategoryCheckComboBox.setTitle(Dict.CATEGORY.toString());

        mOperatorCheckComboBox.setShowCheckedCount(true);
        mOperatorCheckComboBox.setTitle(SDict.OPERATOR.toString());

        mFrequencyCheckComboBox.setShowCheckedCount(true);
        mFrequencyCheckComboBox.setTitle(SDict.FREQUENCY.toString());

        mDimensionCheckComboBox.setShowCheckedCount(true);
        mDimensionCheckComboBox.setTitle(SDict.DIMENSION.toString());
        mDimensionCheckComboBox.setConverter(new StringConverter<BDimension>() {
            @Override
            public BDimension fromString(String string) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String toString(BDimension object) {
                return String.valueOf(object.ordinal() + 1);
            }
        });

        mHasDateFromToComboBox.getItems().setAll(List.of(
                SDict.HAS_VALID_FROM.toString(),
                SDict.HAS_VALID_TO.toString(),
                SDict.IS_VALID.toString(),
                SDict.IS_INVALID.toString()
        ));

        mNextMeasCheckComboBox.getItems().setAll(List.of(
                "<0",
                "0",
                "1-7",
                "8-14",
                "15-28",
                "29-182",
                "∞"
        ));

        mMeasCodeCheckComboBox.getItems().setAll(List.of(
                getBundle().getString("measZeroCount"),
                getBundle().getString("measReplacementCount")
        ));

        mMaxAgeComboBox.getItems().setAll(List.of(
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

        mMaxAgeComboBox.setConverter(new StringConverter<String>() {
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

        mNumOfMeasSpinner.getValueFactory().setConverter(new NegPosStringConverterInteger());
        mSameAlarmCheckbox.setText(getBundle().getString("sameAlarmCheckBoxText"));
        mDiffMeasLatestCheckbox.setText(getBundle().getString("diffMeasLatestCheckBoxText"));
        mDiffMeasAllCheckbox.setText(getBundle().getString("diffMeasAllCheckBoxText"));
        mInvertCheckbox.setText(getBundle().getString("invertCheckBoxText"));
        mMeasLatestOperatorCheckbox.setText(getBundle().getString("measLatesOperatorCheckBoxText"));
        mMeasIncludeWithoutCheckbox.setText(getBundle().getString("measIncludeWithoutCheckboxText"));
        mNumOfMeasCheckbox.setText(getBundle().getString("numOfMeasCheckBoxText"));
        mDiffMeasLatestSpinner.getValueFactory().setConverter(new NegPosStringConverterDouble());
        mDiffMeasAllSpinner.getValueFactory().setConverter(new NegPosStringConverterDouble());

        var vBox = new VBox(GAP,
                getButtonBox(),
                getCopyNamesButton(),
                getPasteNameButton(),
                new Separator(),
                mDimensionCheckComboBox,
                mStatusCheckComboBox,
                mGroupCheckComboBox,
                mCategoryCheckComboBox,
                mOperatorCheckComboBox,
                mFrequencyCheckComboBox,
                mHasDateFromToComboBox,
                mAlarmCheckComboBox,
                mNextMeasCheckComboBox,
                mMaxAgeComboBox,
                mMeasCodeCheckComboBox,
                mMeasOperatorsCheckComboBox,
                new Separator(),
                mMeasLatestOperatorCheckbox,
                mNumOfMeasCheckbox,
                mNumOfMeasSpinner,
                mDiffMeasAllCheckbox,
                mDiffMeasAllSpinner,
                mDiffMeasLatestCheckbox,
                mDiffMeasLatestSpinner,
                new Separator(),
                mMeasIncludeWithoutCheckbox,
                mSameAlarmCheckbox,
                mInvertCheckbox
        );

        FxHelper.setEditable(true, mDiffMeasAllSpinner, mDiffMeasLatestSpinner, mNumOfMeasSpinner);
        FxHelper.autoCommitSpinners(mDiffMeasAllSpinner, mDiffMeasLatestSpinner, mNumOfMeasSpinner);

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

        addEventHandler(EventType.ROOT, event -> {
            if (mFirstRun && event.getEventType() == WindowEvent.WINDOW_SHOWN) {
                var dropDownCount = 25;
                FxHelper.getComboBox(mGroupCheckComboBox).setVisibleRowCount(dropDownCount);
                FxHelper.getComboBox(mCategoryCheckComboBox).setVisibleRowCount(dropDownCount);
                FxHelper.getComboBox(mMeasOperatorsCheckComboBox).setVisibleRowCount(dropDownCount);
                mFirstRun = false;
            }
        });

        mFilter.numOfMeasProperty().bind(mNumOfMeasCheckbox.selectedProperty());
        mFilter.diffMeasAllProperty().bind(mDiffMeasAllCheckbox.selectedProperty());
        mFilter.invertProperty().bind(mInvertCheckbox.selectedProperty());
        mFilter.diffMeasLatestProperty().bind(mDiffMeasLatestCheckbox.selectedProperty());
        mFilter.measLatestOperatorProperty().bind(mMeasLatestOperatorCheckbox.selectedProperty());
        mFilter.measIncludeWithoutProperty().bind(mMeasIncludeWithoutCheckbox.selectedProperty());

        mFilter.numOfMeasValueProperty().bind(mNumOfMeasSession.valueProperty());
        mFilter.diffMeasAllValueProperty().bind(mDiffMeasAllSpinnerSession.valueProperty());
        mFilter.diffMeasLatestValueProperty().bind(mDiffMeasLatestSpinnerSession.valueProperty());

        mFilter.sameAlarmProperty().bind(mSameAlarmCheckbox.selectedProperty());
        mFilter.maxAgeProperty().bind(mMaxAgeComboBox.getSelectionModel().selectedItemProperty());
        mFilter.polygonFilterProperty().bind(getPolygonFilterCheckBox().selectedProperty());
        mFilter.setCheckModelDimension(mDimensionCheckComboBox.getCheckModel());
        mFilter.setCheckModelStatus(mStatusCheckComboBox.getCheckModel());
        mFilter.setCheckModelMeasOperators(mMeasOperatorsCheckComboBox.getCheckModel());
        mFilter.setCheckModelGroup(mGroupCheckComboBox.getCheckModel());
        mFilter.setCheckModelCategory(mCategoryCheckComboBox.getCheckModel());
        mFilter.setCheckModelOperator(mOperatorCheckComboBox.getCheckModel());
        mFilter.setCheckModelNextMeas(mNextMeasCheckComboBox.getCheckModel());
        mFilter.setCheckModelAlarm(mAlarmCheckComboBox.getCheckModel());
        mFilter.setCheckModelMeasCode(mMeasCodeCheckComboBox.getCheckModel());
        mFilter.setCheckModelDateFromTo(mHasDateFromToComboBox.getCheckModel());
        mFilter.setCheckModelFrequency(mFrequencyCheckComboBox.getCheckModel());

        mNumOfMeasSpinner.disableProperty().bind(mNumOfMeasCheckbox.selectedProperty().not());
        mDiffMeasAllSpinner.disableProperty().bind(mDiffMeasAllCheckbox.selectedProperty().not());
        mDiffMeasLatestSpinner.disableProperty().bind(mDiffMeasLatestCheckbox.selectedProperty().not());
    }

    private void initSession() {
        getSessionManager().register("filter.freeText", mFilter.freeTextProperty());
        getSessionManager().register("filter.checkedDimensions", mDimensionCheckModelSession.checkedStringProperty());
        getSessionManager().register("filter.checkedStatus", mStatusCheckModelSession.checkedStringProperty());
        getSessionManager().register("filter.checkedMeasOperators", mMeasOperatorsCheckModelSession.checkedStringProperty());
        getSessionManager().register("filter.checkedGroup", mGroupCheckModelSession.checkedStringProperty());
        getSessionManager().register("filter.checkedCategory", mCategoryCheckModelSession.checkedStringProperty());
        getSessionManager().register("filter.checkedPerformers", mOperatorCheckModelSession.checkedStringProperty());
        getSessionManager().register("filter.checkedNextAlarm", mAlarmCheckModelSession.checkedStringProperty());
        getSessionManager().register("filter.checkedNextMeas", mNextMeasCheckModelSession.checkedStringProperty());
        getSessionManager().register("filter.checkedMeasCode", mMeasCodeCheckModelSession.checkedStringProperty());
        getSessionManager().register("filter.checkedDateFromTo", mHasDateFromToCheckModelSession.checkedStringProperty());
        getSessionManager().register("filter.checkedFrequency", mFrequencyCheckModelSession.checkedStringProperty());
        getSessionManager().register("filter.maxAge", mMaxAgeSelectionModelSession.selectedIndexProperty());
        getSessionManager().register("filter.diffMeasLatestValue", mDiffMeasLatestSpinnerSession.valueProperty());
        getSessionManager().register("filter.diffMeasAllValue", mDiffMeasLatestSpinnerSession.valueProperty());
        getSessionManager().register("filter.numOfMeasValue", mNumOfMeasSession.valueProperty());

        getSessionManager().register("filter.measLatestOperator", mMeasLatestOperatorCheckbox.selectedProperty());
        getSessionManager().register("filter.measIncludeWithout", mMeasIncludeWithoutCheckbox.selectedProperty());
        getSessionManager().register("filter.diffMeasLatest", mDiffMeasLatestCheckbox.selectedProperty());
        getSessionManager().register("filter.diffMeasAll", mDiffMeasAllCheckbox.selectedProperty());
        getSessionManager().register("filter.invert", mInvertCheckbox.selectedProperty());
        getSessionManager().register("filter.numOfMeas", mNumOfMeasCheckbox.selectedProperty());
        getSessionManager().register("filter.sameAlarm", mSameAlarmCheckbox.selectedProperty());
    }

}
