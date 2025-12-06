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
import java.util.List;
import java.util.ResourceBundle;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
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
    private BaseManager<? extends BXyzPoint> mManager;
    private final ComboBox<BaseManagerProvider> mManagerComboBox = new ComboBox<>();
    private final ParameterEditorBasic mParameterEditorBasic = new ParameterEditorBasic();
    private final ParameterEditorZero mParameterEditorZero = new ParameterEditorZero();
    private final LogPanel mPreviewLogPanel = new LogPanel();
    private final TextArea mSourceTextArea = new TextArea();
    private TabPane mTabPane;

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

    private String[] getPointWithNavetNames(String[] points) {
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

        return names.toArray(String[]::new);
    }

    private void createUI() {
        mSourceTextArea.setPromptText(mBundle.getString("prompt_source"));
        mPreviewLogPanel.setWrapText(true);
        mBorderPane = new BorderPane(mPreviewLogPanel);
        mBorderPane.setLeft(mSourceTextArea);
        mSourceTextArea.setPrefWidth(FxHelper.getUIScaled(200));
        mNotificationPane.setContent(mBorderPane);

        var baseTab = new Tab("Grunddata", mParameterEditorBasic);
        var zeroTab = new Tab("Nollmätning", mParameterEditorZero);
        mTabPane = new TabPane(baseTab, zeroTab);
        mTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        mBorderPane.setRight(mTabPane);
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
        mManagerComboBox.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
            mManager = n.getManager();
            mSourceTextArea.clear();
            mPreviewLogPanel.clear();
            mParameterEditorBasic.loadUIParameter(mManager);
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
        mPreviewLogPanel.clear();
        var names = StringUtils.split(mSourceTextArea.getText(), "\n");
        switch (mTabPane.getSelectionModel().getSelectedIndex()) {
            case 0 -> {
                mPreviewLogPanel.println(mParameterEditorBasic.preview(mManager, getPointWithNavetNames(names)));
            }

            case 1 -> {
                mPreviewLogPanel.println(mParameterEditorZero.preview(mManager, names));
            }

            default ->
                throw new AssertionError();
        }
    }

}
