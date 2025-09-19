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
package org.mapton.butterfly_topo;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.ArrayList;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import org.mapton.api.ui.forms.DateRangePane;
import org.mapton.api.ui.forms.MBaseFilterSection;
import org.mapton.api.ui.forms.NegPosStringConverterDouble;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.api.TopoManager;
import org.mapton.butterfly_topo.shared.AlarmLevelChangeMode;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckBox;
import se.trixon.almond.util.fx.session.SessionComboBox;
import se.trixon.almond.util.fx.session.SessionDoubleSpinner;
import se.trixon.almond.util.fx.session.SessionIntegerSpinner;

/**
 *
 * @author Patrik Karlström
 */
public class DateDiffPane {

    private final DateRangePane mDateRangePane = new DateRangePane();
    private final double mDefaultDiffMeters = 0.020;
    private final int mDefaultDiffPercent = 50;
    private final SessionComboBox<AlarmLevelChangeMode> mMeasAlarmLevelChangeModeScb = new SessionComboBox<>();
    private final SessionCheckBox mMeters1dCheckbox = new SessionCheckBox("1d (meter)");
    private final SessionDoubleSpinner mMeters1dSds = new SessionDoubleSpinner(-1.0, 1.0, mDefaultDiffMeters, 0.001);
    private final SessionCheckBox mMeters2dCheckbox = new SessionCheckBox("2d (meter)");
    private final SessionDoubleSpinner mMeters2dSds = new SessionDoubleSpinner(-1.0, 1.0, mDefaultDiffMeters, 0.001);
    private final SessionCheckBox mPercent1dCheckbox = new SessionCheckBox("1d (procent)");
    private final SessionIntegerSpinner mPercent1dSis = new SessionIntegerSpinner(0, 200, mDefaultDiffPercent, 5);
    private final SessionCheckBox mPercent2dCheckbox = new SessionCheckBox("2d (procent)");
    private final SessionIntegerSpinner mPercent2dSis = new SessionIntegerSpinner(0, 200, mDefaultDiffPercent, 5);
    private final GridPane mRoot = new GridPane(MBaseFilterSection.GAP_H, MBaseFilterSection.GAP_V);

    public DateDiffPane() {
        init();
    }

    public void clear() {
        FxHelper.setSelected(false,
                mMeters1dCheckbox,
                mMeters2dCheckbox
        );
        mMeters1dSds.getValueFactory().setValue(mDefaultDiffMeters);
        mMeters2dSds.getValueFactory().setValue(mDefaultDiffMeters);
    }

    public GridPane getRoot() {
        return mRoot;
    }

    public void initListeners(TopoFilter filter) {
        filter.measDateDiffProperty().bind(mMeters1dCheckbox.selectedProperty());
        filter.measDateDiffProperty().bind(mMeters2dCheckbox.selectedProperty());
//**
        filter.measDateDiffValueProperty().bind(mMeters1dSds.sessionValueProperty());
        filter.measDateDiffValueProperty().bind(mMeters2dSds.sessionValueProperty());
//**
        filter.measAlarmLevelChangeModeProperty().bind(mMeasAlarmLevelChangeModeScb.getSelectionModel().selectedItemProperty());

    }

    public void initSession(SessionManager sessionManager) {
        sessionManager.register("filter.measDateMeters1d", mMeters1dCheckbox.selectedProperty());
        sessionManager.register("filter.measDateMeters1dValue", mMeters1dSds.sessionValueProperty());
        sessionManager.register("filter.measDateMeters2d", mMeters2dCheckbox.selectedProperty());
        sessionManager.register("filter.measDateMeters2dValue", mMeters2dSds.sessionValueProperty());

        sessionManager.register("filter.measDatePercent1d", mPercent1dCheckbox.selectedProperty());
        sessionManager.register("filter.measDatePercent1dValue", mPercent1dSis.sessionValueProperty());
        sessionManager.register("filter.measDatePercent2d", mPercent2dCheckbox.selectedProperty());
        sessionManager.register("filter.measDatePercent2dValue", mPercent2dSis.sessionValueProperty());

        sessionManager.register("filter.measDateDiffChangeMode", mMeasAlarmLevelChangeModeScb.selectedIndexProperty());
    }

    public void load(ArrayList<BTopoControlPoint> items) {
        mMeasAlarmLevelChangeModeScb.load();
        mMeters1dSds.load();
        mMeters2dSds.load();
        mMeters1dSds.disableProperty().bind(mMeters1dCheckbox.selectedProperty().not());
        mMeters2dSds.disableProperty().bind(mMeters2dCheckbox.selectedProperty().not());
        mPercent1dSis.load();
        mPercent2dSis.load();
        mPercent1dSis.disableProperty().bind(mPercent1dCheckbox.selectedProperty().not());
        mPercent2dSis.disableProperty().bind(mPercent2dCheckbox.selectedProperty().not());

        var range = TopoManager.getInstance().getTemporalRange();
        mDateRangePane.setMinMaxDate(range.getFromLocalDate(), range.getToLocalDate());
        mDateRangePane.getRoot().disableProperty().bind(
                mMeters1dCheckbox.selectedProperty().not()
                        .and(mMeters2dCheckbox.selectedProperty().not())
                        .and(mPercent1dCheckbox.selectedProperty().not())
                        .and(mPercent2dCheckbox.selectedProperty().not())
        );
        mMeasAlarmLevelChangeModeScb.disableProperty().bind(mDateRangePane.getRoot().disableProperty());
    }

    private void init() {
        mMeters1dSds.getValueFactory().setConverter(new NegPosStringConverterDouble());
        mMeters2dSds.getValueFactory().setConverter(new NegPosStringConverterDouble());
        mMeasAlarmLevelChangeModeScb.getItems().setAll(AlarmLevelChangeMode.values());

        var row = 0;
        mRoot.add(mMeasAlarmLevelChangeModeScb, 0, row++, 2, 1);
        mRoot.addRow(row++, mMeters1dCheckbox, mMeters2dCheckbox);
        mRoot.addRow(row++, mMeters1dSds, mMeters2dSds);
        mRoot.addRow(row++, mPercent1dCheckbox, mPercent2dCheckbox);
        mRoot.addRow(row++, mPercent1dSis, mPercent2dSis);

        mRoot.add(mDateRangePane.getRoot(), 0, row++, 2, 1);

        var spinners = new Spinner[]{mMeters1dSds, mMeters2dSds, mPercent1dSis, mPercent2dSis};
        FxHelper.setEditable(true, spinners);
        FxHelper.autoCommitSpinners(spinners);
        FxHelper.bindWidthForChildrens(mRoot);
        FxHelper.bindWidthForRegions(mRoot);
        FxHelper.autoSizeColumn(mRoot, 2);
    }
}
