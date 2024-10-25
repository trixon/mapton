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
package org.mapton.butterfly_projektnav.editor.topo;

import java.util.List;
import java.util.TreeSet;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.api.report.MEditor;
import org.mapton.api.report.MSplitNavSettings;
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
public class ParameterEditor extends BaseTopoEditor {

    private BorderPane mBorderPane;
    private final CheckBox mDagCheckBox = new CheckBox("Dag");
    private final Spinner mDagSpinner = new Spinner<Integer>(0, 999, 1);
    private final CheckBox mDatFromCheckBox = new CheckBox("Från");
    private final DatePicker mDatFromDatePicker = new DatePicker();
    private final CheckBox mDatToCheckBox = new CheckBox("Till");
    private final DatePicker mDatToDatePicker = new DatePicker();
    private final CheckBox mDatToLatestCheckBox = new CheckBox("Till (senaste)");
    private final CheckBox mGruppCheckBox = new CheckBox("Grupp");
    private final ComboBox<String> mGruppComboBox = new ComboBox<>();
    private final CheckBox mKategoriCheckBox = new CheckBox("Kategori");
    private final ComboBox<String> mKategoriComboBox = new ComboBox<>();
    private final CheckBox mLarmHCheckBox = new CheckBox("Larm H");
    private final ComboBox<String> mLarmHComboBox = new ComboBox<>();
    private final CheckBox mLarmPCheckBox = new CheckBox("Larm P");
    private final ComboBox<String> mLarmPComboBox = new ComboBox<>();
    private final LogPanel mPreviewLogPanel = new LogPanel();
    private final TextArea mSourceTextArea = new TextArea();
    private final CheckBox mStatusCheckBox = new CheckBox("Status");
    private final ComboBox<String> mStatusComboBox = new ComboBox<>();
    private final CheckBox mUtforareCheckBox = new CheckBox("Utförare");
    private final ComboBox<String> mUtforareComboBox = new ComboBox<>();
    private final CheckBox mUtglesningCheckBox = new CheckBox("Utglesning");
    private final TextField mUtglesningTextField = new TextField("MEDIAN / 2D");

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

    private void addConditionlly(StringBuilder sb, boolean selected, Object o) {
        if (selected) {
            sb.append("\t").append(o);
        }
    }

    private void createUI() {
        mSourceTextArea.setPromptText(mBundle.getString("prompt_source"));

        var gridPane = new GridPane();
        gridPane.addRow(0, mSourceTextArea, mPreviewLogPanel);
        FxHelper.autoSizeColumn(gridPane, 2);
        mPreviewLogPanel.setWrapText(true);
        mBorderPane = new BorderPane(gridPane);
        gridPane.prefHeightProperty().bind(mBorderPane.heightProperty());
        mSourceTextArea.prefHeightProperty().bind(gridPane.heightProperty());
        mNotificationPane.setContent(mBorderPane);

        createUIParameter();
    }

    private void createUIParameter() {
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

        mDagSpinner.disableProperty().bind(mDagCheckBox.selectedProperty().not());
        mStatusComboBox.disableProperty().bind(mStatusCheckBox.selectedProperty().not());
        mUtforareComboBox.disableProperty().bind(mUtforareCheckBox.selectedProperty().not());
        mGruppComboBox.disableProperty().bind(mGruppCheckBox.selectedProperty().not());
        mLarmHComboBox.disableProperty().bind(mLarmHCheckBox.selectedProperty().not());
        mLarmPComboBox.disableProperty().bind(mLarmPCheckBox.selectedProperty().not());
        mKategoriComboBox.disableProperty().bind(mKategoriCheckBox.selectedProperty().not());
        mDatFromDatePicker.disableProperty().bind(mDatFromCheckBox.selectedProperty().not());
        mDatToDatePicker.disableProperty().bind(mDatToCheckBox.selectedProperty().not());
        mUtglesningTextField.disableProperty().bind(mUtglesningCheckBox.selectedProperty().not());

        var settingsGridPane = new GridPane();
        int col = 0;
        settingsGridPane.addColumn(col++, mDagCheckBox, mDagSpinner);
        settingsGridPane.addColumn(col++, mStatusCheckBox, mStatusComboBox);
        settingsGridPane.addColumn(col++, mGruppCheckBox, mGruppComboBox);
        settingsGridPane.addColumn(col++, mKategoriCheckBox, mKategoriComboBox);
        settingsGridPane.addColumn(col++, mLarmHCheckBox, mLarmHComboBox);
        settingsGridPane.addColumn(col++, mLarmPCheckBox, mLarmPComboBox);
        settingsGridPane.addColumn(col++, mUtforareCheckBox, mUtforareComboBox);
        settingsGridPane.addColumn(col++, mDatFromCheckBox, mDatFromDatePicker);
        settingsGridPane.addColumn(col++, mDatToCheckBox, mDatToDatePicker);
        settingsGridPane.addColumn(col++, mDatToLatestCheckBox);
        settingsGridPane.addColumn(col++, mUtglesningCheckBox, mUtglesningTextField);
        FxHelper.setEditable(true, mDagSpinner);
        FxHelper.autoCommitSpinners(mDagSpinner);
        FxHelper.setEditable(true, mGruppComboBox, mKategoriComboBox, mUtforareComboBox);

        mBorderPane.setTop(settingsGridPane);

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

        mSplitNavSetting.getToolBarItems().setAll(toolBar.getItems());
    }

    private void preview() {
        mPreviewLogPanel.clear();
        var sb = new StringBuilder("nr");
        addConditionlly(sb, mDagCheckBox.isSelected(), "dag");
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
            if (p != null) {
                var date = p.ext().getObservationRawLastDate();
                if (date != null) {
                    toDate = date.toString();
                }
            }
            sb.append(name);
            addConditionlly(sb, mDagCheckBox.isSelected(), mDagSpinner.getValue());
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

            sb.append("\n");
        }

        var result = StringUtils.removeEnd(sb.toString(), "\n");
        result = StringUtils.removeEnd(result, "\n");
//        result = StringUtils.replace(result, "/", ".");

        mPreviewLogPanel.println(result);
    }

}
