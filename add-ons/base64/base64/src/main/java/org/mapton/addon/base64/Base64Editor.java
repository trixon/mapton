/*
 * Copyright 2021 Patrik Karlström.
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
package org.mapton.addon.base64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.ResourceBundle;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import org.apache.commons.io.FileUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.api.report.MEditor;
import org.mapton.api.report.MEditorSystem;
import org.mapton.api.report.MSplitNavSettings.TitleMode;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.dialogs.NbMessage;
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
public class Base64Editor extends MEditorSystem {

    private BorderPane mBorderPane;
    private final ResourceBundle mBundle = NbBundle.getBundle(Base64Editor.class);
    private final LogPanel mPreviewLogPanel = new LogPanel();
    private final TextArea mSourceTextArea = new TextArea();

    public Base64Editor() {
        getSplitNavSettings().setTitleMode(TitleMode.NAME);
    }

    @Override
    public String getName() {
        return "Base64";
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

    private void createUI() {
        mSourceTextArea.setPromptText(mBundle.getString("prompt_source"));
        mSourceTextArea.setFont(Font.font("monospaced"));
        mSourceTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            encode();
        });

        var gridPane = new GridPane();
        gridPane.addRow(0, mSourceTextArea, mPreviewLogPanel);
        FxHelper.autoSizeColumn(gridPane, 2);
        mPreviewLogPanel.setWrapText(true);
        mBorderPane = new BorderPane(gridPane);
        gridPane.prefHeightProperty().bind(mBorderPane.heightProperty());
        mSourceTextArea.prefHeightProperty().bind(gridPane.heightProperty());
        mNotificationPane.setContent(mBorderPane);
    }

    private void decode() {
        try {
            mPreviewLogPanel.setText(new String(Base64.getDecoder().decode(mSourceTextArea.getText().getBytes()), "utf-8"));
        } catch (UnsupportedEncodingException | IllegalArgumentException ex) {
            NbMessage.error(Dict.Dialog.ERROR.toString(), ex.getMessage());
        }
    }

    private void encode() {
        try {
            mPreviewLogPanel.setText(new String(Base64.getEncoder().encode(mSourceTextArea.getText().getBytes()), "utf-8"));
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void initListeners() {
        mSourceTextArea.setOnDragOver(dragEvent -> {
            var dragboard = dragEvent.getDragboard();
            if (dragboard.hasFiles()) {
                dragEvent.acceptTransferModes(TransferMode.COPY);
            }
        });

        mSourceTextArea.setOnDragDropped((DragEvent event) -> {
            try {
                var file = event.getDragboard().getFiles().get(0);
                String s = FileUtils.readFileToString(file, "utf-8");
                mSourceTextArea.setText(s);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
    }

    private void initToolBar() {
        var clearAction = new Action(Dict.CLEAR.toString(), event -> {
            mSourceTextArea.clear();
        });
        clearAction.setGraphic(MaterialIcon._Content.CLEAR.getImageView(getIconSizeToolBarInt(), Mapton.getThemeForegroundColor()));

        var encodeAction = new Action(Dict.ENCODE.toString(), event -> {
            encode();
        });
        encodeAction.setGraphic(MaterialIcon._Navigation.CHEVRON_RIGHT.getImageView(getIconSizeToolBarInt(), Mapton.getThemeForegroundColor()));

        var decodeAction = new Action(Dict.DECODE.toString(), event -> {
            decode();
        });
        decodeAction.setGraphic(MaterialIcon._Navigation.CHEVRON_LEFT.getImageView(getIconSizeToolBarInt(), Mapton.getThemeForegroundColor()));

        var copyAction = new Action(Dict.COPY.toString(), event -> {
            SystemHelper.copyToClipboard(mPreviewLogPanel.getText());
        });
        copyAction.setGraphic(MaterialIcon._Content.CONTENT_COPY.getImageView(getIconSizeToolBarInt(), Mapton.getThemeForegroundColor()));

        ArrayList<Action> actions = new ArrayList<>();
        actions.add(encodeAction);
        actions.add(decodeAction);
        actions.add(ActionUtils.ACTION_SEPARATOR);
        actions.add(copyAction);
        actions.add(ActionUtils.ACTION_SPAN);
        actions.add(clearAction);

        var toolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);
        FxHelper.adjustButtonWidth(toolBar.getItems().stream(), getIconSizeToolBarInt());
        FxHelper.slimToolBar(toolBar);
        FxHelper.undecorateButtons(toolBar.getItems().stream());

        mSplitNavSetting.getToolBarItems().setAll(toolBar.getItems());
    }
}
