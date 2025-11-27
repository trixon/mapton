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
package org.mapton.butterfly_projektnav;

import java.util.HashMap;
import java.util.TreeSet;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.mapton.api.Mapton;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.types.BXyzPoint;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ParameterEditorBasic extends ParameterEditorBase {

    private final CheckBox mAnmCheckBox = new CheckBox("Kommentar");
    private final ComboBox<EditMode> mAnmComboBox = new ComboBox<>();
    private final TextField mAnmTextField = new TextField();
    private final CheckBox mClassCheckBox = new CheckBox("Klassning");
    private final ComboBox<String> mClassComboBox = new ComboBox<>();
    private final CheckBox mDagCheckBox = new CheckBox("Dag");
    private final CheckBox mDagDefCheckBox = new CheckBox("DagDef");
    private final CheckBox mDagDefRestoreCheckBox = new CheckBox("Reset dag def");
    private final Spinner<Integer> mDagDefSpinner = new Spinner<>(0, 999, 1);
    private final CheckBox mDagIntCheckBox = new CheckBox("DagInt");
    private final CheckBox mDagIntParamCheckBox = new CheckBox("DagIntParam");
    private final TextField mDagIntParamTextField = new TextField();
    private final CheckBox mDagIntRestoreCheckBox = new CheckBox("Reset dag int");
    private final Spinner<Integer> mDagIntSpinner = new Spinner<>(0, 999, 1);
    private final Spinner<Integer> mDagSpinner = new Spinner<>(0, 999, 1);
    private final CheckBox mDatFromCheckBox = new CheckBox("Från");
    private final DatePicker mDatFromDatePicker = new DatePicker();
    private final CheckBox mDatToCheckBox = new CheckBox("Till");
    private final DatePicker mDatToDatePicker = new DatePicker();
    private final CheckBox mDatToLatestCheckBox = new CheckBox("Till (senaste)");
    private final CheckBox mDynamicCheckBox = new CheckBox("Dynamisk");
    private final ComboBox<String> mDynamicComboBox = new ComboBox<>();
    private final TextField mDynamicTextField = new TextField();
    private final CheckBox mGruppCheckBox = new CheckBox("Grupp");
    private final ComboBox<String> mGruppComboBox = new ComboBox<>();
    private final CheckBox mKategoriCheckBox = new CheckBox("Kategori");
    private final ComboBox<String> mKategoriComboBox = new ComboBox<>();
    private final CheckBox mLarmHCheckBox = new CheckBox("Larm H");
    private final ComboBox<String> mLarmHComboBox = new ComboBox<>();
    private final CheckBox mLarmPCheckBox = new CheckBox("Larm P");
    private final ComboBox<String> mLarmPComboBox = new ComboBox<>();
    private final CheckBox mRullandeCheckBox = new CheckBox("Rullande");
    private final TextField mRullandeTextField = new TextField("Roll(10)");
    private final CheckBox mStatusCheckBox = new CheckBox("Status");
    private final ComboBox<String> mStatusComboBox = new ComboBox<>();
    private final CheckBox mTagCheckBox = new CheckBox("Etikett");
    private final ComboBox<EditMode> mTagComboBox = new ComboBox<>();
    private final TextField mTagTextField = new TextField();
    private final CheckBox mUtforareCheckBox = new CheckBox("Utförare");
    private final ComboBox<String> mUtforareComboBox = new ComboBox<>();
    private final CheckBox mUtglesningCheckBox = new CheckBox("Utglesning");
    private final TextField mUtglesningTextField = new TextField("MEDIAN / 2D");

    public ParameterEditorBasic() {
        createUI();
    }

    public String preview(BaseManager<? extends BXyzPoint> manager, String[] names) {
        HashMap<String, String> originToId = Mapton.getGlobalState().getOrDefault("ParamEditor.originToId", new HashMap<String, String>());
        var sb = new StringBuilder("projid");
        addConditionlly(sb, true, "nr");
        addConditionlly(sb, mDagCheckBox.isSelected(), "dag");
        addConditionlly(sb, mDagDefCheckBox.isSelected(), "meta");
        addConditionlly(sb, mDagIntCheckBox.isSelected(), "meta");
        addConditionlly(sb, mDagIntParamCheckBox.isSelected(), "meta");
        addConditionlly(sb, mStatusCheckBox.isSelected(), "status");
        addConditionlly(sb, mClassCheckBox.isSelected(), "meta");
        addConditionlly(sb, mGruppCheckBox.isSelected(), "grupp");
        addConditionlly(sb, mKategoriCheckBox.isSelected(), "kategori");
        addConditionlly(sb, mLarmHCheckBox.isSelected() || mLarmPCheckBox.isSelected(), "larm");
        addConditionlly(sb, mUtforareCheckBox.isSelected(), "utf");
        addConditionlly(sb, mDatFromCheckBox.isSelected(), "df");
        addConditionlly(sb, mDatToCheckBox.isSelected(), "dt");
        addConditionlly(sb, mDatToLatestCheckBox.isSelected(), "dt");
        if (mDatToCheckBox.isSelected()) {
            addConditionlly(sb, mDatToLatestCheckBox.isSelected(), "dt");
        }
        addConditionlly(sb, mUtglesningCheckBox.isSelected(), "sparse");
        addConditionlly(sb, mRullandeCheckBox.isSelected(), "roll");
        addConditionlly(sb, mTagCheckBox.isSelected(), "tag");
        addConditionlly(sb, mAnmCheckBox.isSelected(), "anm");
        addConditionlly(sb, mDynamicCheckBox.isSelected(), mDynamicComboBox.getValue());

        sb.append("\n");

        for (var name : names) {
            String larm = null;
            if (mLarmHCheckBox.isSelected() || mLarmPCheckBox.isSelected()) {
                if (Strings.CI.endsWith(name, "_P")) {
                    larm = mLarmPComboBox.getValue();
                } else {
                    larm = mLarmHComboBox.getValue();
                }
            }

            var baseName = Strings.CI.removeEnd(name, "_H");
            baseName = Strings.CI.removeEnd(baseName, "_P");
            var p = manager.getAllItemsMap().get(baseName);
            var toDate = "ERROR";
            if (p != null) {
                var date = p.extOrNull().getObservationRawLastDate();
                if (date != null) {
                    toDate = date.toString();
                }
            }

            var projid = originToId.getOrDefault(p.getOrigin(), "ERROR");
            sb.append(projid);
            addConditionlly(sb, true, name);
            var dag = mDagSpinner.getValue();
            if (mDagDefRestoreCheckBox.isSelected() && p.getFrequencyDefault() != null) {
                dag = p.getFrequencyDefault();
            }
            if (mDagIntRestoreCheckBox.isSelected() && p.getFrequencyHigh() != null) {
                dag = p.getFrequencyHigh();
            }
            addConditionlly(sb, mDagCheckBox.isSelected(), dag);
            addConditionlly(sb, mDagDefCheckBox.isSelected(), "DefaultDag=%d".formatted(mDagDefSpinner.getValue()));
            addConditionlly(sb, mDagIntCheckBox.isSelected(), "IntenseDag=%d".formatted(mDagIntSpinner.getValue()));
            addConditionlly(sb, mDagIntParamCheckBox.isSelected(), "IntenseDagParam=%s".formatted(mDagIntParamTextField.getText()));
            addConditionlly(sb, mClassCheckBox.isSelected(), "Classification=%s".formatted(mClassComboBox.getValue()));
            addConditionlly(sb, mStatusCheckBox.isSelected(), mStatusComboBox.getValue());
            addConditionlly(sb, mGruppCheckBox.isSelected(), mGruppComboBox.getValue());
            addConditionlly(sb, mKategoriCheckBox.isSelected(), mKategoriComboBox.getValue());
            addConditionlly(sb, larm != null, larm);
            addConditionlly(sb, mUtforareCheckBox.isSelected(), mUtforareComboBox.getValue());
            addConditionlly(sb, mDatFromCheckBox.isSelected(), mDatFromDatePicker.getValue() == null ? "ERROR" : mDatFromDatePicker.getValue().toString());
            addConditionlly(sb, mDatToCheckBox.isSelected(), mDatToDatePicker.getValue() == null ? "ERROR" : mDatToDatePicker.getValue().toString());
            if (!mDatToCheckBox.isSelected()) {
                addConditionlly(sb, mDatToLatestCheckBox.isSelected(), toDate);
            }
            addConditionlly(sb, mUtglesningCheckBox.isSelected(), mUtglesningTextField.getText());
            addConditionlly(sb, mRullandeCheckBox.isSelected(), mRullandeTextField.getText());

            var tag = "";
            var tagNew = mTagTextField.getText();
            var tagOld = p.getTag();
            switch (mTagComboBox.getSelectionModel().getSelectedItem()) {
                case FIRST:
                    if (StringUtils.isBlank(tagOld)) {
                        tag = tagNew;
                    } else {
                        tag = "%s,%s".formatted(tagNew, tagOld);
                    }
                    break;

                case LAST:
                    if (StringUtils.isBlank(tagOld)) {
                        tag = tagNew;
                    } else {
                        tag = "%s,%s".formatted(tagOld, tagNew);
                    }
                    break;
                case REPLACE:
                    tag = tagNew;
                    break;
                default:
                    throw new AssertionError();
            }
            addConditionlly(sb, mTagCheckBox.isSelected(), tag);

            var anm = "";
            var anmNew = mAnmTextField.getText();
            var anmOld = p.getComment();
            switch (mAnmComboBox.getSelectionModel().getSelectedItem()) {
                case FIRST:
                    if (StringUtils.isBlank(anmOld)) {
                        anm = anmNew;
                    } else {
                        anm = "%s\\n%s".formatted(anmNew, anmOld);
                    }
                    break;

                case LAST:
                    if (StringUtils.isBlank(anmOld)) {
                        anm = anmNew;
                    } else {
                        anm = "%s\\n%s".formatted(anmOld, anmNew);
                    }
                    break;
                case REPLACE:
                    anm = anmNew;
                    break;
                default:
                    throw new AssertionError();
            }
            addConditionlly(sb, mAnmCheckBox.isSelected(), anm);
            addConditionlly(sb, mDynamicCheckBox.isSelected(), mDynamicTextField.getText());

            sb.append("\n");
        }

        var result = Strings.CI.removeEnd(sb.toString(), "\n");
        result = Strings.CI.removeEnd(result, "\n");
//        result = StringUtils.replace(result, "/", ".");

        return result;
    }

    void loadUIParameter(BaseManager<? extends BXyzPoint> manager) {
        mClassComboBox.getItems().setAll("K0", "K1", "K2", "K3", "K4", "K5");
        mClassComboBox.getSelectionModel().select("K1");
        mStatusComboBox.getItems().setAll("S0", "S1", "S2", "S3", "S4", "S5");
        mStatusComboBox.getSelectionModel().select("S1");

        mTagComboBox.getItems().setAll(EditMode.values());
        mTagComboBox.getSelectionModel().selectFirst();
        mAnmComboBox.getItems().setAll(EditMode.values());
        mAnmComboBox.getSelectionModel().selectFirst();

        mDynamicComboBox.getItems().setAll(
                "ref",
                "decx",
                "decz",
                "meta",
                "sysp",
                "sysh");

        var larmH = new TreeSet<>(manager.getAllItems().stream()
                .map(p -> p.getAlarm1Id())
                .toList());
        mLarmHComboBox.getItems().setAll(larmH);
        var larmP = new TreeSet<>(manager.getAllItems().stream()
                .map(p -> p.getAlarm2Id())
                .toList());
        mLarmPComboBox.getItems().setAll(larmP);
        var grupper = new TreeSet<>(manager.getAllItems().stream().map(p -> p.getGroup()).toList());
        mGruppComboBox.getItems().setAll(grupper);
        var kategorier = new TreeSet<>(manager.getAllItems().stream().map(p -> p.getCategory()).toList());
        mKategoriComboBox.getItems().setAll(kategorier);
        var utforare = new TreeSet<>(manager.getAllItems().stream().map(p -> p.getOperator()).toList());
        mUtforareComboBox.getItems().setAll(utforare);
    }

    private void createUI() {
        mDagSpinner.disableProperty().bind(mDagCheckBox.selectedProperty().not());
        mDagDefSpinner.disableProperty().bind(mDagDefCheckBox.selectedProperty().not());
        mDagIntSpinner.disableProperty().bind(mDagIntCheckBox.selectedProperty().not());
        mDagIntParamTextField.disableProperty().bind(mDagIntParamCheckBox.selectedProperty().not());
        mStatusComboBox.disableProperty().bind(mStatusCheckBox.selectedProperty().not());
        mClassComboBox.disableProperty().bind(mClassCheckBox.selectedProperty().not());
        mUtforareComboBox.disableProperty().bind(mUtforareCheckBox.selectedProperty().not());
        mGruppComboBox.disableProperty().bind(mGruppCheckBox.selectedProperty().not());
        mLarmHComboBox.disableProperty().bind(mLarmHCheckBox.selectedProperty().not());
        mLarmPComboBox.disableProperty().bind(mLarmPCheckBox.selectedProperty().not());
        mKategoriComboBox.disableProperty().bind(mKategoriCheckBox.selectedProperty().not());
        mDatFromDatePicker.disableProperty().bind(mDatFromCheckBox.selectedProperty().not());
        mDatToDatePicker.disableProperty().bind(mDatToCheckBox.selectedProperty().not());
        mUtglesningTextField.disableProperty().bind(mUtglesningCheckBox.selectedProperty().not());
        mRullandeTextField.disableProperty().bind(mRullandeCheckBox.selectedProperty().not());
        mTagTextField.disableProperty().bind(mTagCheckBox.selectedProperty().not());
        mTagComboBox.disableProperty().bind(mTagCheckBox.selectedProperty().not());
        mAnmTextField.disableProperty().bind(mAnmCheckBox.selectedProperty().not());
        mAnmComboBox.disableProperty().bind(mAnmCheckBox.selectedProperty().not());
        mAnmComboBox.disableProperty().bind(mAnmCheckBox.selectedProperty().not());
        mDynamicTextField.disableProperty().bind(mDynamicCheckBox.selectedProperty().not());
        mDynamicComboBox.disableProperty().bind(mDynamicCheckBox.selectedProperty().not());
        mDynamicComboBox.disableProperty().bind(mDynamicCheckBox.selectedProperty().not());

        mDynamicComboBox.setEditable(true);
        int row = 0;
        addRow(row, mDagCheckBox, mDagDefCheckBox, mDagIntCheckBox);
        addRow(++row, mDagSpinner, mDagDefSpinner, mDagIntSpinner);
        addRow(++row, mDagIntParamCheckBox, mDagDefRestoreCheckBox, mDagIntRestoreCheckBox);
        addRow(++row, mDagIntParamTextField);
        addRow(++row, createSpacer());
        addRow(++row, mClassCheckBox);
        addRow(++row, mClassComboBox);
        addRow(++row, mStatusCheckBox, mGruppCheckBox, mKategoriCheckBox);
        addRow(++row, mStatusComboBox, mGruppComboBox, mKategoriComboBox);
        addRow(++row, createSpacer());
        addRow(++row, mLarmHCheckBox, mLarmPCheckBox, mUtforareCheckBox);
        addRow(++row, mLarmHComboBox, mLarmPComboBox, mUtforareComboBox);
        addRow(++row, createSpacer());
        addRow(++row, mDatFromCheckBox, mDatToCheckBox, mDatToLatestCheckBox);
        addRow(++row, mDatFromDatePicker, mDatToDatePicker);
        addRow(++row, createSpacer());
        addRow(++row, mUtglesningCheckBox, mRullandeCheckBox);
        addRow(++row, mUtglesningTextField, mRullandeTextField);
        addRow(++row, createSpacer());
        addRow(++row, mTagCheckBox);
        addRow(++row, mTagTextField, mTagComboBox);
        addRow(++row, createSpacer());
        addRow(++row, mAnmCheckBox);
        addRow(++row, mAnmTextField, mAnmComboBox);
        addRow(++row, mDynamicCheckBox);
        addRow(++row, mDynamicTextField, mDynamicComboBox);

        FxHelper.autoSizeColumn(this, 3);

        FxHelper.setEditable(true,
                mDagSpinner,
                mDagDefSpinner,
                mDagIntSpinner
        );
        FxHelper.autoCommitSpinners(mDagSpinner, mDagDefSpinner);
        FxHelper.setEditable(true,
                mGruppComboBox,
                mKategoriComboBox,
                mUtforareComboBox,
                mStatusComboBox,
                mLarmHComboBox,
                mLarmPComboBox
        );
    }

}
