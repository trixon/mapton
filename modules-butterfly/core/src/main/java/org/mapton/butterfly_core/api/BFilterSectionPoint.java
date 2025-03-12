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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.IndexedCheckModel;
import org.controlsfx.tools.Borders;
import org.mapton.api.ui.forms.MBaseFilterSection;
import static org.mapton.butterfly_format.types.BDimension._1d;
import static org.mapton.butterfly_format.types.BDimension._2d;
import static org.mapton.butterfly_format.types.BDimension._3d;
import org.mapton.butterfly_format.types.BMeasurementMode;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class BFilterSectionPoint extends MBaseFilterSection {

    private final SessionCheckComboBox<String> mAlarmNameSccb;
    private final ResourceBundle mBundle = NbBundle.getBundle(getClass());
    private final SessionCheckComboBox<String> mCategorySccb;
    private final SessionCheckComboBox<Integer> mFrequencySccb;
    private final SessionCheckComboBox<String> mGroupSccb;
    private final SessionCheckComboBox<String> mMeasNextSccb;
    private final SessionCheckComboBox<String> mMeasurementModeSccb;
    private final SessionCheckComboBox<String> mOperatorSccb;
    private final SessionCheckComboBox<String> mOriginSccb;
    private final PointFilterUI mPointFilterUI;
    private final SessionCheckComboBox<String> mStatusSccb;

    public BFilterSectionPoint() {
        super("Grunddata");
        mAlarmNameSccb = new SessionCheckComboBox<>();
        mStatusSccb = new SessionCheckComboBox<>();
        mOriginSccb = new SessionCheckComboBox<>();
        mOperatorSccb = new SessionCheckComboBox<>();
        mMeasNextSccb = new SessionCheckComboBox<>(true);
        mGroupSccb = new SessionCheckComboBox<>();
        mFrequencySccb = new SessionCheckComboBox<>();
        mCategorySccb = new SessionCheckComboBox<>();
        mMeasurementModeSccb = new SessionCheckComboBox<>();
        mPointFilterUI = new PointFilterUI();
        init();
        setContent(mPointFilterUI.getBaseBox());
    }

    @Override
    public void clear() {
        super.clear();
        mPointFilterUI.clear();
    }

    @Override
    public void createInfoContent(LinkedHashMap<String, String> map) {
        if (!isSelected()) {
            return;
        }
        map.put(Dict.Geometry.POINT.toUpper(), ".");
        map.put(Dict.STATUS.toString(), makeInfo(mStatusSccb.getCheckModel().getCheckedItems()));
        map.put(SDict.FREQUENCY.toString(), makeInfoInteger(mFrequencySccb.getCheckModel().getCheckedItems()));
        map.put(mBundle.getString("nextMeasCheckComboBoxTitle"), makeInfo(mMeasNextSccb.getCheckModel().getCheckedItems()));
        map.put(Dict.GROUP.toString(), makeInfo(mGroupSccb.getCheckModel().getCheckedItems()));
        map.put(Dict.CATEGORY.toString(), makeInfo(mCategorySccb.getCheckModel().getCheckedItems()));
        map.put(SDict.ALARMS.toString(), makeInfo(mAlarmNameSccb.getCheckModel().getCheckedItems()));
        map.put(SDict.OPERATOR.toString(), makeInfo(mOperatorSccb.getCheckModel().getCheckedItems()));
        map.put(Dict.ORIGIN.toString(), makeInfo(mOriginSccb.getCheckModel().getCheckedItems()));
        map.put("Mätläge", makeInfo(mMeasurementModeSccb.getCheckModel().getCheckedItems()));
    }

    public boolean filter(BXyzPoint p, Long remainingDays) {
        if (isSelected()) {
            return validateCheck(getStatusSccb().getCheckModel(), p.getStatus())
                    && validateCheck(getGroupSccb().getCheckModel(), p.getGroup())
                    && validateCheck(getCategorySccb().getCheckModel(), p.getCategory())
                    && validateAlarmName(p, getAlarmNameSccb().getCheckModel())
                    && validateCheck(getFrequencySccb().getCheckModel(), p.getFrequency())
                    && validateCheck(getOperatorSccb().getCheckModel(), p.getOperator())
                    && validateCheck(getOriginSccb().getCheckModel(), p.getOrigin())
                    && validateCheckMeasurementMode(getMeasurementModeSccb().getCheckModel(), p.getMeasurementMode())
                    && validateNextMeas(p, getMeasNextSccb().getCheckModel(), remainingDays)
                    && true;
        } else {
            return true;
        }
    }

    public SessionCheckComboBox<String> getAlarmNameSccb() {
        mAlarmNameSccb.setDisable(false);
        return mAlarmNameSccb;
    }

    public SessionCheckComboBox<String> getCategorySccb() {
        mCategorySccb.setDisable(false);
        return mCategorySccb;
    }

    public SessionCheckComboBox<Integer> getFrequencySccb() {
        mFrequencySccb.setDisable(false);
        return mFrequencySccb;
    }

    public SessionCheckComboBox<String> getGroupSccb() {
        mGroupSccb.setDisable(false);
        return mGroupSccb;
    }

    public SessionCheckComboBox<String> getMeasNextSccb() {
        mMeasNextSccb.setDisable(false);
        return mMeasNextSccb;
    }

    public SessionCheckComboBox<String> getMeasurementModeSccb() {
        mMeasurementModeSccb.setDisable(false);

        return mMeasurementModeSccb;
    }

    public SessionCheckComboBox<String> getOperatorSccb() {
        mOperatorSccb.setDisable(false);
        return mOperatorSccb;
    }

    public SessionCheckComboBox<String> getOriginSccb() {
        mOriginSccb.setDisable(false);
        return mOriginSccb;
    }

    public GridPane getRoot() {
        return mPointFilterUI.getBaseBox();
    }

    public SessionCheckComboBox<String> getStatusSccb() {
        mStatusSccb.setDisable(false);
        return mStatusSccb;
    }

    public void initListeners(ChangeListener changeListenerObject, ListChangeListener<Object> listChangeListener) {
        List.of(
                selectedProperty()
        ).forEach(propertyBase -> propertyBase.addListener(changeListenerObject));

        List.of(
                getMeasNextSccb().getCheckModel(),
                getStatusSccb().getCheckModel(),
                getGroupSccb().getCheckModel(),
                getCategorySccb().getCheckModel(),
                getAlarmNameSccb().getCheckModel(),
                getFrequencySccb().getCheckModel(),
                getMeasurementModeSccb().getCheckModel(),
                getOperatorSccb().getCheckModel(),
                getOriginSccb().getCheckModel()
        ).forEach(cm -> cm.getCheckedItems().addListener(listChangeListener));
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        sessionManager.register("filter.section.point", selectedProperty());
        mPointFilterUI.initSession(sessionManager);
    }

    public void load(ArrayList<? extends BXyzPoint> items) {
        var allAlarmNames = items.stream().map(o -> o.getAlarm1Id()).collect(Collectors.toCollection(HashSet::new));
        allAlarmNames.addAll(items.stream().map(o -> o.getAlarm2Id()).collect(Collectors.toSet()));
        getAlarmNameSccb().loadAndRestoreCheckItems(allAlarmNames.stream());
        getGroupSccb().loadAndRestoreCheckItems(items.stream().map(o -> o.getGroup()));
        getCategorySccb().loadAndRestoreCheckItems(items.stream().map(o -> o.getCategory()));
        getOperatorSccb().loadAndRestoreCheckItems(items.stream().map(o -> o.getOperator()));
        getOriginSccb().loadAndRestoreCheckItems(items.stream().map(o -> o.getOrigin()));
        getStatusSccb().loadAndRestoreCheckItems(items.stream().map(o -> o.getStatus()));
        getFrequencySccb().loadAndRestoreCheckItems(items.stream().filter(o -> o.getFrequency() != null).map(o -> o.getFrequency()));
        getMeasNextSccb().loadAndRestoreCheckItems();
        getMeasurementModeSccb().loadAndRestoreCheckItems(Stream.of("Automatisk", "Manuell", "Odefinierad"));

    }

    @Override
    public void onShownFirstTime() {
        mPointFilterUI.onShownFirstTime();
    }

    @Override
    public void reset(PropertiesConfiguration filterConfig) {
        if (filterConfig != null) {
            mPointFilterUI.reset(filterConfig);
        }
    }

    public boolean validateAlarmName(BXyzPoint p, IndexedCheckModel checkModel) {
        var ah = p.getAlarm1Id();
        var ap = p.getAlarm2Id();

        switch (p.getDimension()) {
            case _1d -> {
                return validateCheck(checkModel, ah);
            }
            case _2d -> {
                return validateCheck(checkModel, ap);
            }
            case _3d -> {
                return validateCheck(checkModel, ah) && validateCheck(checkModel, ap);
            }
        }

        return true;
    }

    public boolean validateAlarmName1(BXyzPoint p, IndexedCheckModel checkModel) {
        return validateCheck(checkModel, p.getAlarm1Id());
    }

    public boolean validateAlarmName2(BXyzPoint p, IndexedCheckModel checkModel) {
        return validateCheck(checkModel, p.getAlarm2Id());
    }

    public boolean validateCheckMeasurementMode(IndexedCheckModel checkModel, BMeasurementMode m) {
        if (checkModel.isEmpty()) {
            return true;
        }

        return m == BMeasurementMode.AUTOMATIC && checkModel.isChecked("Automatisk")
                || m == BMeasurementMode.MANUAL && checkModel.isChecked("Manuell")
                || m == null && checkModel.isChecked("Odefinierad");
    }

    public boolean validateNextMeas(BXyzPoint p, IndexedCheckModel<String> checkModel, long remainingDays) {
        var frequency = p.getFrequency();
        var latest = p.getDateLatest() != null ? p.getDateLatest().toLocalDate() : LocalDate.MIN;
        var today = LocalDate.now();
        var nextMeas = latest.plusDays(frequency);
//        var remainingDays = ;

        if (checkModel.isEmpty()) {
            return true;
        } else if (checkModel.isChecked("∞") && frequency == 0) {
            return true;
        } else if (frequency > 0 && checkModel.isChecked("<0") && nextMeas.isBefore(today)) {
            return true;
        } else if (frequency > 0 && checkModel.isChecked("0") && remainingDays == 0) {
            return true;
        } else {
            return checkModel.getCheckedItems().stream()
                    .filter(s -> StringUtils.countMatches(s, "-") == 1)
                    .anyMatch(s -> {
                        int start = Integer.parseInt(StringUtils.substringBefore(s, "-"));
                        int end = Integer.parseInt(StringUtils.substringAfter(s, "-"));
                        return remainingDays >= start && remainingDays <= end;
                    });
        }
    }

    private void init() {
    }

    public class PointFilterUI {

        private Node mBaseBorderBox;
        private GridPane mBaseBox;
        private final double mBorderInnerPadding = FxHelper.getUIScaled(8.0);
        private final double mTopBorderInnerPadding = FxHelper.getUIScaled(16.0);

        public PointFilterUI() {
            createUI();
        }

        public void clear() {
            SessionCheckComboBox.clearChecks(
                    mStatusSccb,
                    mGroupSccb,
                    mCategorySccb,
                    mAlarmNameSccb,
                    mOperatorSccb,
                    mOriginSccb,
                    mMeasNextSccb,
                    mMeasurementModeSccb,
                    mFrequencySccb
            );
        }

        public Node getBaseBorderBox() {
            if (mBaseBorderBox == null) {
                mBaseBorderBox = Borders.wrap(mBaseBox)
                        .etchedBorder()
                        .title("Grunddata")
                        .innerPadding(mTopBorderInnerPadding, mBorderInnerPadding, mBorderInnerPadding, mBorderInnerPadding)
                        .outerPadding(0)
                        .raised()
                        .build()
                        .build();
            }
            return mBaseBorderBox;
        }

        public GridPane getBaseBox() {
            return mBaseBox;
        }

        public void initSession(SessionManager sessionManager) {
            sessionManager.register("filter.checkedAlarmName", mAlarmNameSccb.checkedStringProperty());
            sessionManager.register("filter.checkedCategory", mCategorySccb.checkedStringProperty());
            sessionManager.register("filter.checkedFrequency", mFrequencySccb.checkedStringProperty());
            sessionManager.register("filter.checkedGroup", mGroupSccb.checkedStringProperty());
            sessionManager.register("filter.checkedOperators", mOperatorSccb.checkedStringProperty());
            sessionManager.register("filter.checkedOrigin", mOriginSccb.checkedStringProperty());
            sessionManager.register("filter.checkedStatus", mStatusSccb.checkedStringProperty());
            sessionManager.register("filter.measCheckedNextMeas", mMeasNextSccb.checkedStringProperty());
            sessionManager.register("filter.measCheckedMeasMode", mMeasurementModeSccb.checkedStringProperty());
        }

        public void onShownFirstTime() {
            FxHelper.setVisibleRowCount(25,
                    mGroupSccb,
                    mCategorySccb,
                    mAlarmNameSccb
            );
        }

        public void reset(PropertiesConfiguration filterConfig) {
            BaseFilterPopOver.splitAndCheck(filterConfig.getString("STATUS"), mStatusSccb.getCheckModel());
            BaseFilterPopOver.splitAndCheck(filterConfig.getString("GROUP"), mGroupSccb.getCheckModel());
            BaseFilterPopOver.splitAndCheck(filterConfig.getString("CATEGORY"), mCategorySccb.getCheckModel());
            BaseFilterPopOver.splitAndCheck(filterConfig.getString("OPERATOR"), mOperatorSccb.getCheckModel());
        }

        private void createUI() {
            FxHelper.setShowCheckedCount(true,
                    mMeasNextSccb,
                    mStatusSccb,
                    mGroupSccb,
                    mCategorySccb,
                    mAlarmNameSccb,
                    mOperatorSccb,
                    mOriginSccb,
                    mMeasurementModeSccb,
                    mFrequencySccb
            );

            mStatusSccb.setDisable(true);
            mGroupSccb.setDisable(true);
            mCategorySccb.setDisable(true);
            mAlarmNameSccb.setDisable(true);
            mOperatorSccb.setDisable(true);
            mOriginSccb.setDisable(true);
            mFrequencySccb.setDisable(true);
            mMeasNextSccb.setDisable(true);
            mMeasurementModeSccb.setDisable(true);

            mMeasNextSccb.setTitle(mBundle.getString("nextMeasCheckComboBoxTitle"));
            mStatusSccb.setTitle(Dict.STATUS.toString());
            mGroupSccb.setTitle(Dict.GROUP.toString());
            mCategorySccb.setTitle(Dict.CATEGORY.toString());
            mAlarmNameSccb.setTitle(SDict.ALARMS.toString());
            mOperatorSccb.setTitle(SDict.OPERATOR.toString());
            mOriginSccb.setTitle(Dict.ORIGIN.toString());
            mFrequencySccb.setTitle(SDict.FREQUENCY.toString());
            mMeasurementModeSccb.setTitle("Mätläge");

            mMeasNextSccb.getItems().setAll(List.of(
                    "<0",
                    "0",
                    "1-6",
                    "7-14",
                    "15-28",
                    "29-182",
                    "∞"
            ));

            int rowGap = FxHelper.getUIScaled(12);
            mBaseBox = new GridPane(rowGap, rowGap);
            var leftBox = new VBox(rowGap,
                    mStatusSccb,
                    mFrequencySccb,
                    mMeasurementModeSccb,
                    mGroupSccb,
                    mOriginSccb
            );

            var dummyLabel = new Label();
            dummyLabel.prefHeightProperty().bind(mMeasurementModeSccb.heightProperty());

            var rightBox = new VBox(rowGap,
                    mAlarmNameSccb,
                    mMeasNextSccb,
                    dummyLabel,
                    mCategorySccb,
                    mOperatorSccb
            );

            int row = 1;
            mBaseBox.addRow(row++, leftBox, rightBox);

            FxHelper.autoSizeColumn(mBaseBox, 2);
            FxHelper.bindWidthForChildrens(leftBox, rightBox);
        }
    }
}
