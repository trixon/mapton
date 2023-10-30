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
import java.util.ResourceBundle;
import java.util.TreeSet;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.api.report.MEditor;
import org.mapton.api.report.MSplitNavSettings;
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
public class ParameterEditor extends BaseTopoEditor {

    private BorderPane mBorderPane;
    private final ResourceBundle mBundle = NbBundle.getBundle(ParameterEditor.class);
    private final CheckBox mDagCheckBox = new CheckBox("Dag");
    private final Spinner mDagSpinner = new Spinner<Integer>(0, 999, 1);
    private final LogPanel mPreviewLogPanel = new LogPanel();
    private final TextArea mSourceTextArea = new TextArea();
    private final CheckBox mStatusCheckBox = new CheckBox("Status");
    private final CheckBox mGruppCheckBox = new CheckBox("Grupp");
    private final CheckBox mKategoriCheckBox = new CheckBox("Kategori");
    private final ComboBox<String> mStatusComboBox = new ComboBox<>();
    private final ComboBox<String> mGruppComboBox = new ComboBox<>();
    private final ComboBox<String> mKategoriComboBox = new ComboBox<>();

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

        var grupper = new TreeSet<>(mManager.getAllItems().stream().map(p -> p.getGroup()).toList());
        mGruppComboBox.getItems().setAll(grupper);
        var kategorier = new TreeSet<>(mManager.getAllItems().stream().map(p -> p.getCategory()).toList());
        mKategoriComboBox.getItems().setAll(kategorier);

        mDagSpinner.disableProperty().bind(mDagCheckBox.selectedProperty().not());
        mStatusComboBox.disableProperty().bind(mStatusCheckBox.selectedProperty().not());

        var settingsGridPane = new GridPane();
        int col = 0;
        settingsGridPane.addColumn(col++, mDagCheckBox, mDagSpinner);
        settingsGridPane.addColumn(col++, mStatusCheckBox, mStatusComboBox);
        settingsGridPane.addColumn(col++, mGruppCheckBox, mGruppComboBox);
        settingsGridPane.addColumn(col++, mKategoriCheckBox, mKategoriComboBox);
        FxHelper.setEditable(true, mDagSpinner);
        FxHelper.autoCommitSpinners(mDagSpinner);
        FxHelper.setEditable(true, mGruppComboBox, mKategoriComboBox);

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
        sb.append("\n");

        for (var name : getPointWithNavetNames(StringUtils.split(mSourceTextArea.getText(), "\n"))) {
            sb.append(name);
            addConditionlly(sb, mDagCheckBox.isSelected(), mDagSpinner.getValue());
            addConditionlly(sb, mStatusCheckBox.isSelected(), mStatusComboBox.getValue());
            addConditionlly(sb, mGruppCheckBox.isSelected(), mGruppComboBox.getValue());
            addConditionlly(sb, mKategoriCheckBox.isSelected(), mKategoriComboBox.getValue());

            sb.append("\n");
        }

        mPreviewLogPanel.println(sb.toString());
    }

}
