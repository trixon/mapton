/*
 * Copyright 2025 Patrik Karlström.
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
package org.mapton.butterfly_hydro.groundwater;

import com.dlsc.gemsfx.util.SessionManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.controlsfx.tools.Borders;
import org.mapton.api.MTemporalRange;
import org.mapton.api.ui.forms.DateRangePane;
import org.mapton.api.ui.forms.MBaseFilterSection;
import org.mapton.api.ui.forms.NegPosStringConverterDouble;
import org.mapton.butterfly_format.types.hydro.BHydroGroundwaterPoint;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionDoubleSpinner;

/**
 *
 * @author Patrik Karlström
 */
public class FilterSectionMeas extends MBaseFilterSection {

    private final MeasFilterUI mMeasFilterUI;

    public FilterSectionMeas() {
        super("Mätningar");
        mMeasFilterUI = new MeasFilterUI();
        init();
        setContent(mMeasFilterUI.getBaseBox());
    }

    @Override
    public void clear() {
        super.clear();
        mMeasFilterUI.clear();
    }

    public GridPane getRoot() {
        return mMeasFilterUI.getBaseBox();
    }

    @Override
    public void createInfoContent(LinkedHashMap<String, String> map) {
        if (!isSelected()) {
            return;
        }
        map.put("MÄTNINGAR", ".");
        map.put("Period " + Dict.FROM.toString(), dateLowProperty().get() != null ? dateLowProperty().get().toString() : "");
        map.put("Period " + Dict.TO.toString(), dateHighProperty().get() != null ? dateHighProperty().get().toString() : "");
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        sessionManager.register("filter.section.meas", selectedProperty());
        mMeasFilterUI.initSession(sessionManager);
    }

    @Override
    public void onShownFirstTime() {
        mMeasFilterUI.onShownFirstTime();
    }

    @Override
    public void reset(PropertiesConfiguration filterConfig) {
        if (filterConfig != null) {
            mMeasFilterUI.reset(filterConfig);
        }
    }

    boolean filter(BHydroGroundwaterPoint p) {
        if (isSelected()) {
            return validatePeriodChanges(p)
                    && true;
        } else {
            return true;
        }
    }

    void initListeners(ChangeListener changeListenerObject, ListChangeListener<Object> listChangeListener) {
        List.of(
                selectedProperty(),
                //
                dateHighProperty(),
                dateLowProperty()
        ).forEach(propertyBase -> propertyBase.addListener(changeListenerObject));
    }

    private SimpleObjectProperty<LocalDate> dateHighProperty() {
        return mMeasFilterUI.mDateRangePane.highDateProperty();
    }

    private SimpleObjectProperty<LocalDate> dateLowProperty() {
        return mMeasFilterUI.mDateRangePane.lowDateProperty();
    }

    void load(ArrayList<BHydroGroundwaterPoint> items, MTemporalRange temporalRange) {
        mMeasFilterUI.mDateRangePane.setMinMaxDate(temporalRange.getFromLocalDate(), temporalRange.getToLocalDate());
        mMeasFilterUI.mDiffMeasAllSds.load();
        mMeasFilterUI.mDiffMeasAllSds.disableProperty().bind(mMeasFilterUI.mDiffMeasAllCheckbox.selectedProperty().not());

    }

    private void init() {
    }

    private boolean validatePeriodChanges(BHydroGroundwaterPoint p) {
        return true;
    }

    public class MeasFilterUI {

        private final double mDefaultDiffValue = 0.25;

        private Node mBaseBorderBox;
        private GridPane mBaseBox;
        private final double mBorderInnerPadding = FxHelper.getUIScaled(8.0);
        private final double mTopBorderInnerPadding = FxHelper.getUIScaled(16.0);
        private final DateRangePane mDateRangePane = new DateRangePane();
        private final CheckBox mDiffMeasAllCheckbox = new CheckBox();
        private final SessionDoubleSpinner mDiffMeasAllSds = new SessionDoubleSpinner(-20.0, 20.0, mDefaultDiffValue, 0.1);

        public MeasFilterUI() {
            createUI();
        }

        public void clear() {
            FxHelper.setSelected(false, mDiffMeasAllCheckbox);
            mDiffMeasAllSds.getValueFactory().setValue(mDefaultDiffValue);

            mDateRangePane.reset();
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
            sessionManager.register("filter.measDiffAll", mDiffMeasAllCheckbox.selectedProperty());
            sessionManager.register("filter.measDiffAllValue", mDiffMeasAllSds.sessionValueProperty());
            sessionManager.register("filter.DateLow", mDateRangePane.lowStringProperty());
            sessionManager.register("filter.DateHigh", mDateRangePane.highStringProperty());
        }

        public void onShownFirstTime() {
//            FxHelper.setVisibleRowCount(25,
//                    mGroupSccb,
//                    mCategorySccb,
//                    mAlarmNameSccb
//            );
        }

        public void reset(PropertiesConfiguration filterConfig) {
//            BaseFilterPopOver.splitAndCheck(filterConfig.getString("STATUS"), mStatusSccb.getCheckModel());
//            BaseFilterPopOver.splitAndCheck(filterConfig.getString("GROUP"), mGroupSccb.getCheckModel());
//            BaseFilterPopOver.splitAndCheck(filterConfig.getString("CATEGORY"), mCategorySccb.getCheckModel());
//            BaseFilterPopOver.splitAndCheck(filterConfig.getString("OPERATOR"), mOperatorSccb.getCheckModel());
        }

        private void createUI() {
//            FxHelper.setShowCheckedCount(true,
//                    mMeasNextSccb,
//            );

//            mMeasNextSccb.setDisable(true);
//            mMeasNextSccb.setTitle(mBundle.getString("nextMeasCheckComboBoxTitle"));
//            mStatusSccb.setTitle(Dict.STATUS.toString());
//            mGroupSccb.setTitle(Dict.GROUP.toString());
//            mCategorySccb.setTitle(Dict.CATEGORY.toString());
//            mAlarmNameSccb.setTitle(SDict.ALARMS.toString());
//            mOperatorSccb.setTitle(SDict.OPERATOR.toString());
//            mOriginSccb.setTitle(Dict.ORIGIN.toString());
//            mFrequencySccb.setTitle(SDict.FREQUENCY.toString());
//
//            mMeasNextSccb.getItems().setAll(List.of(
//                    "<0",
//                    "0",
//                    "1-6",
//                    "7-14",
//                    "15-28",
//                    "29-182",
//                    "∞"
//            ));
            mDiffMeasAllCheckbox.setText("Nivåförändring");
            mDiffMeasAllSds.getValueFactory().setConverter(new NegPosStringConverterDouble());

            var upDownCombobox = new ComboBox<Direction>();
            upDownCombobox.getItems().setAll(Direction.values());
            var diffGridPane = new GridPane(hGap, vGap);
            diffGridPane.add(mDiffMeasAllCheckbox, 0, 0, 2, 1);
            diffGridPane.addRow(1, mDiffMeasAllSds, upDownCombobox);
//            diffGridPane.addColumn(1, mDiffMeasLatestCheckbox, mDiffMeasLatestSds);
            FxHelper.autoSizeColumn(diffGridPane, 2);

            int rowGap = FxHelper.getUIScaled(12);
            mBaseBox = new GridPane(rowGap, rowGap);
            double borderInnerPadding = FxHelper.getUIScaled(8.0);
            double topBorderInnerPadding = FxHelper.getUIScaled(16.0);
            var wrappedDateBox = Borders.wrap(mDateRangePane.getRoot())
                    .etchedBorder()
                    .title("Nivåförändringsperiod")
                    .innerPadding(topBorderInnerPadding, borderInnerPadding, borderInnerPadding, borderInnerPadding)
                    .outerPadding(0)
                    .raised()
                    .build()
                    .build();
            var leftBox = new VBox(rowGap,
                    diffGridPane,
                    wrappedDateBox
            );

            var rightBox = new VBox(rowGap,
                    new Label("2")
            );

            int row = 1;
            mBaseBox.addRow(row++, leftBox, rightBox);

            var spinners = new Spinner[]{mDiffMeasAllSds};
            FxHelper.setEditable(true, spinners);
            FxHelper.autoCommitSpinners(spinners);

            FxHelper.autoSizeColumn(mBaseBox, 2);
            FxHelper.bindWidthForChildrens(leftBox, rightBox);

            upDownCombobox.disableProperty().bind(mDiffMeasAllCheckbox.selectedProperty().not());
            wrappedDateBox.disableProperty().bind(mDiffMeasAllCheckbox.selectedProperty().not());

        }
    }

    public enum Direction {
        DOWN("Ner"),
        UP("Upp"),
        EITHER("Upp eller ner");
        private final String mTitle;

        private Direction(String title) {
            mTitle = title;
        }

        @Override
        public String toString() {
            return mTitle;
        }

    }
}
