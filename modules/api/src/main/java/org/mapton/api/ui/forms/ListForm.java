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
package org.mapton.api.ui.forms;

import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ListForm {

    private final TextArea mFilterTextArea = new TextArea();
    private ListFormConfiguration mListFormConfiguration;
    private final BorderPane mRoot = new BorderPane();
    private final String mTitle;
    private ToolBar mToolBar = new ToolBar();
    private final BorderPane mTopBorderPane = new BorderPane();

    public ListForm(String title) {
        mTitle = title;

        createUI();
    }

    public void applyConfiguration(ListFormConfiguration lfc) {
        mListFormConfiguration = lfc;

        if (lfc.isUseTextFilter()) {
            mTopBorderPane.setCenter(mFilterTextArea);
        }

        if (lfc.getToolbarActions() != null) {
            initToolbar();
        }
    }

    public StringProperty freeTextProperty() {
        return mFilterTextArea.textProperty();
    }

    public ToolBar getToolBar() {
        return mToolBar;
    }

    public Pane getView() {
        return mRoot;
    }

    public void setContent(Node node) {
        mRoot.setCenter(node);
    }

    public void setFreeTextTooltip(String... strings) {
        var sb = new StringBuilder(Dict.FILTER.toString()).append("\r\r");

        for (var string : strings) {
            sb.append("‣ ").append(string).append("\r");
        }

        mFilterTextArea.setTooltip(new Tooltip(sb.toString()));
    }

    private void createUI() {
        var titleLabel = Mapton.createTitle(mTitle);

        mRoot.setTop(new VBox(titleLabel, mTopBorderPane));
        mFilterTextArea.setPromptText(Dict.FILTER.toString());
        mFilterTextArea.setPrefRowCount(4);

        titleLabel.prefWidthProperty().bind(mRoot.widthProperty());
    }

    private void initToolbar() {
        mToolBar = ActionUtils.createToolBar(mListFormConfiguration.getToolbarActions(), ActionUtils.ActionTextBehavior.HIDE);
        FxHelper.adjustButtonWidth(mToolBar.getItems().stream(), getIconSizeToolBarInt());
        FxHelper.undecorateButtons(mToolBar.getItems().stream());
        FxHelper.slimToolBar(mToolBar);
        mTopBorderPane.setTop(mToolBar);
    }
}
