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
package org.mapton.butterfly_projektnav;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeSet;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.api.report.MEditor;
import org.mapton.api.report.MSplitNavSettings;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_core.api.BaseManagerProvider;
import static org.mapton.butterfly_format.types.BDimension._1d;
import static org.mapton.butterfly_format.types.BDimension._2d;
import static org.mapton.butterfly_format.types.BDimension._3d;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.butterfly_projektnav.editor.BaseEditor;
import org.mapton.butterfly_projektnav.editor.topo.*;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.LogPanel;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MEditor.class)
public class ParameterEditor extends BaseEditor {

    private BorderPane mBorderPane;
    private final ResourceBundle mBundle = NbBundle.getBundle(BaseTopoEditor.class);
    private final CheckBox mDagCheckBox = new CheckBox("Dag");
    private final Spinner<Integer> mDagSpinner = new Spinner<>(0, 999, 1);
    private final CheckBox mDatFromCheckBox = new CheckBox("Från");
    private final DatePicker mDatFromDatePicker = new DatePicker();
    private final CheckBox mDatToCheckBox = new CheckBox("Till");
    private final DatePicker mDatToDatePicker = new DatePicker();
    private final CheckBox mDatToLatestCheckBox = new CheckBox("Till (senaste)");
    private final CheckBox mDagDefCheckBox = new CheckBox("DagDef");
    private final CheckBox mDagDefRestoreCheckBox = new CheckBox("Reset dag def");
    private final CheckBox mDagIntCheckBox = new CheckBox("DagInt");
    private final CheckBox mDagIntRestoreCheckBox = new CheckBox("Reset dag int");
    private final CheckBox mDagIntParamCheckBox = new CheckBox("DagIntParam");
    private final Spinner<Integer> mDagDefSpinner = new Spinner<>(0, 999, 1);
    private final Spinner<Integer> mDagIntSpinner = new Spinner<>(0, 999, 1);
    private final CheckBox mGruppCheckBox = new CheckBox("Grupp");
    private final ComboBox<String> mGruppComboBox = new ComboBox<>();
    private final CheckBox mKategoriCheckBox = new CheckBox("Kategori");
    private final ComboBox<String> mKategoriComboBox = new ComboBox<>();
    private final CheckBox mLarmHCheckBox = new CheckBox("Larm H");
    private final ComboBox<String> mLarmHComboBox = new ComboBox<>();
    private final CheckBox mLarmPCheckBox = new CheckBox("Larm P");
    private final ComboBox<String> mLarmPComboBox = new ComboBox<>();
    private BaseManager<? extends BXyzPoint> mManager;
    private final LogPanel mPreviewLogPanel = new LogPanel();
    private final TextArea mSourceTextArea = new TextArea();
    private final CheckBox mStatusCheckBox = new CheckBox("Status");
    private final ComboBox<String> mStatusComboBox = new ComboBox<>();
    private final CheckBox mUtforareCheckBox = new CheckBox("Utförare");
    private final ComboBox<String> mUtforareComboBox = new ComboBox<>();
    private final CheckBox mUtglesningCheckBox = new CheckBox("Utglesning");
    private final TextField mUtglesningTextField = new TextField("MEDIAN / 2D");
    private final TextField mDagIntParamTextField = new TextField();
    private final ComboBox<BaseManagerProvider> mManagerComboBox = new ComboBox<>();
    private final CheckBox mRullandeCheckBox = new CheckBox("Rullande");
    private final TextField mRullandeTextField = new TextField("Roll(10)");

    public ParameterEditor() {
        setName("Parametrar");
        getSplitNavSettings().setTitleMode(MSplitNavSettings.TitleMode.FULL_PATH);
    }

    @Override
    public Node getNode() {
        if (mBorderPane == null) {
            initToolBar();
            createUI();
            initListeners();
        }

        return mBody;
    }

    public ArrayList<String> getPointWithNavetNames(String[] points) {
        var names = new ArrayList<String>();
        for (var name : points) {
            var p = mManager.getItemForKey(name);

            if (p != null) {
                if (null == p.getDimension()) {
                    names.add(name + "_P");
                } else {
                    switch (p.getDimension()) {
                        case _1d ->
                            names.add(name);
                        case _2d ->
                            names.add(name + "_P");
                        case _3d -> {
                            names.add(name + "_P");
                            names.add(name + "_H");
                        }
                    }
                }
            } else {
                System.err.println("Point not found: " + name);
            }
            mManager.getAllItemsSet();
        }

        return names;
    }

    private void addConditionlly(StringBuilder sb, boolean selected, Object o) {
        if (selected) {
            sb.append("\t").append(o);
        }
    }

    private Region createSpacer() {
        var region = new Region();
        region.setPrefHeight(FxHelper.getUIScaled(4.0));
        return region;
    }

    private void createUI() {
        mSourceTextArea.setPromptText(mBundle.getString("prompt_source"));
        mPreviewLogPanel.setWrapText(true);
        mBorderPane = new BorderPane(mPreviewLogPanel);
        mBorderPane.setLeft(mSourceTextArea);
        mSourceTextArea.setPrefWidth(FxHelper.getUIScaled(200));
        mNotificationPane.setContent(mBorderPane);

        createUIParameter();
    }

    private void loadUIParameter() {
        mStatusComboBox.getItems().setAll("S0", "S1", "S2", "S3", "S4", "S5");
        mStatusComboBox.getSelectionModel().select("S1");

        var larmH = new TreeSet<>(mManager.getAllItems().stream()
                .map(p -> p.getAlarm1Id())
                .toList());
        mLarmHComboBox.getItems().setAll(larmH);
        var larmP = new TreeSet<>(mManager.getAllItems().stream()
                .map(p -> p.getAlarm2Id())
                .toList());
        mLarmPComboBox.getItems().setAll(larmP);
        var grupper = new TreeSet<>(mManager.getAllItems().stream().map(p -> p.getGroup()).toList());
        mGruppComboBox.getItems().setAll(grupper);
        var kategorier = new TreeSet<>(mManager.getAllItems().stream().map(p -> p.getCategory()).toList());
        mKategoriComboBox.getItems().setAll(kategorier);
        var utforare = new TreeSet<>(mManager.getAllItems().stream().map(p -> p.getOperator()).toList());
        mUtforareComboBox.getItems().setAll(utforare);
    }

    private void createUIParameter() {
        mDagSpinner.disableProperty().bind(mDagCheckBox.selectedProperty().not());
        mDagDefSpinner.disableProperty().bind(mDagDefCheckBox.selectedProperty().not());
        mDagIntSpinner.disableProperty().bind(mDagIntCheckBox.selectedProperty().not());
        mDagIntParamTextField.disableProperty().bind(mDagIntParamCheckBox.selectedProperty().not());
        mStatusComboBox.disableProperty().bind(mStatusCheckBox.selectedProperty().not());
        mUtforareComboBox.disableProperty().bind(mUtforareCheckBox.selectedProperty().not());
        mGruppComboBox.disableProperty().bind(mGruppCheckBox.selectedProperty().not());
        mLarmHComboBox.disableProperty().bind(mLarmHCheckBox.selectedProperty().not());
        mLarmPComboBox.disableProperty().bind(mLarmPCheckBox.selectedProperty().not());
        mKategoriComboBox.disableProperty().bind(mKategoriCheckBox.selectedProperty().not());
        mDatFromDatePicker.disableProperty().bind(mDatFromCheckBox.selectedProperty().not());
        mDatToDatePicker.disableProperty().bind(mDatToCheckBox.selectedProperty().not());
        mUtglesningTextField.disableProperty().bind(mUtglesningCheckBox.selectedProperty().not());
        mRullandeTextField.disableProperty().bind(mRullandeCheckBox.selectedProperty().not());

        var gp = new GridPane(FxHelper.getUIScaled(8.0), FxHelper.getUIScaled(2.0));
        gp.setPadding(FxHelper.getUIScaledInsets(8.0));
        int row = 0;
        gp.addRow(row, mDagCheckBox, mDagDefCheckBox, mDagIntCheckBox);
        gp.addRow(++row, mDagSpinner, mDagDefSpinner, mDagIntSpinner);
        gp.addRow(++row, mDagIntParamCheckBox, mDagDefRestoreCheckBox, mDagIntRestoreCheckBox);
        gp.addRow(++row, mDagIntParamTextField);
        gp.addRow(++row, createSpacer());
        gp.addRow(++row, mStatusCheckBox, mGruppCheckBox, mKategoriCheckBox);
        gp.addRow(++row, mStatusComboBox, mGruppComboBox, mKategoriComboBox);
        gp.addRow(++row, createSpacer());
        gp.addRow(++row, mLarmHCheckBox, mLarmPCheckBox, mUtforareCheckBox);
        gp.addRow(++row, mLarmHComboBox, mLarmPComboBox, mUtforareComboBox);
        gp.addRow(++row, createSpacer());
        gp.addRow(++row, mDatFromCheckBox, mDatToCheckBox, mDatToLatestCheckBox);
        gp.addRow(++row, mDatFromDatePicker, mDatToDatePicker);
        gp.addRow(++row, createSpacer());
        gp.addRow(++row, mUtglesningCheckBox, mRullandeCheckBox);
        gp.addRow(++row, mUtglesningTextField, mRullandeTextField);
        FxHelper.autoSizeColumn(gp, 3);

        FxHelper.setEditable(true, mDagSpinner, mDagDefSpinner, mDagIntSpinner);
        FxHelper.autoCommitSpinners(mDagSpinner, mDagDefSpinner);
        FxHelper.setEditable(true, mGruppComboBox, mKategoriComboBox, mUtforareComboBox);

        mBorderPane.setRight(gp);

    }

    private void importPoints() {
        mPreviewLogPanel.clear();
        var names = mManager.getTimeFilteredItems().stream().map(p -> p.getName()).toList();
        mSourceTextArea.setText(String.join("\n", names));

    }

    private void initListeners() {
        mSourceTextArea.textProperty().addListener((p, o, n) -> {
//            preview();
        });
    }

    private void initToolBar() {
        var clearAction = new Action(Dict.CLEAR.toString(), event -> {
            mSourceTextArea.clear();
        });
        clearAction.setGraphic(MaterialIcon._Content.CLEAR.getImageView(getIconSizeToolBarInt(), Mapton.getThemeForegroundColor()));

        var importAction = new Action(Dict.IMPORT.toString(), event -> {
            importPoints();
        });
        importAction.setGraphic(MaterialIcon._Navigation.CHEVRON_RIGHT.getImageView(getIconSizeToolBarInt(), Mapton.getThemeForegroundColor()));

        var previewAction = new Action(Dict.PREVIEW.toString(), event -> {
            preview();
        });
        previewAction.setGraphic(MaterialIcon._Navigation.CHEVRON_LEFT.getImageView(getIconSizeToolBarInt(), Mapton.getThemeForegroundColor()));

        var copyAction = new Action(Dict.COPY.toString(), event -> {
            SystemHelper.copyToClipboard(mPreviewLogPanel.getText());
        });
        copyAction.setGraphic(MaterialIcon._Content.CONTENT_COPY.getImageView(getIconSizeToolBarInt(), Mapton.getThemeForegroundColor()));

        var actions = List.of(
                importAction,
                previewAction,
                ActionUtils.ACTION_SEPARATOR,
                copyAction,
                ActionUtils.ACTION_SPAN,
                clearAction
        );

        var toolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);
        FxHelper.adjustButtonWidth(toolBar.getItems().stream(), getIconSizeToolBarInt());
        FxHelper.slimToolBar(toolBar);
        FxHelper.undecorateButtons(toolBar.getItems().stream());

        var managerProviders = Lookup.getDefault().lookupAll(BaseManagerProvider.class).stream()
                .sorted(Comparator.comparing(BaseManagerProvider::getName))
                .toList();
        mManagerComboBox.getItems().setAll(managerProviders);
        mManagerComboBox.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends BaseManagerProvider> observable, BaseManagerProvider oldValue, BaseManagerProvider newValue) -> {
            mManager = newValue.getManager();
            mSourceTextArea.clear();
            mPreviewLogPanel.clear();
            loadUIParameter();
        });

        var defultProvider = managerProviders.stream()
                .filter(p -> p.getName().equalsIgnoreCase("Topografiska"))
                .findAny().orElse(null);
        if (defultProvider != null) {
            mManagerComboBox.getSelectionModel().select(defultProvider);
        } else {
            mManagerComboBox.getSelectionModel().selectFirst();
        }
        toolBar.getItems().add(0, mManagerComboBox);
        mSplitNavSetting.getToolBarItems().setAll(toolBar.getItems());
    }

    private void preview() {
        HashMap<String, String> originToId = Mapton.getGlobalState().getOrDefault("ParamEditor.originToId", new HashMap<String, String>());
        mPreviewLogPanel.clear();
        var sb = new StringBuilder("projid");
        addConditionlly(sb, true, "nr");
        addConditionlly(sb, mDagCheckBox.isSelected(), "dag");
        addConditionlly(sb, mDagDefCheckBox.isSelected(), "meta");
        addConditionlly(sb, mDagIntCheckBox.isSelected(), "meta");
        addConditionlly(sb, mDagIntParamCheckBox.isSelected(), "meta");
        addConditionlly(sb, mStatusCheckBox.isSelected(), "status");
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
        sb.append("\n");

        for (var name : getPointWithNavetNames(StringUtils.split(mSourceTextArea.getText(), "\n"))) {
            String larm = null;
            if (mLarmHCheckBox.isSelected() || mLarmPCheckBox.isSelected()) {
                if (StringUtils.endsWith(name, "_P")) {
                    larm = mLarmPComboBox.getValue();
                } else {
                    larm = mLarmHComboBox.getValue();
                }
            }

            var baseName = StringUtils.removeEnd(name, "_H");
            baseName = StringUtils.removeEnd(baseName, "_P");
            var p = mManager.getAllItemsMap().get(baseName);
            var toDate = "ERROR";
//            if (p != null) {
//                var date = p.ext().getObservationRawLastDate();
//                if (date != null) {
//                    toDate = date.toString();
//                }
//            }

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

            sb.append("\n");
        }

        var result = StringUtils.removeEnd(sb.toString(), "\n");
        result = StringUtils.removeEnd(result, "\n");
//        result = StringUtils.replace(result, "/", ".");

        mPreviewLogPanel.println(result);
    }

}
