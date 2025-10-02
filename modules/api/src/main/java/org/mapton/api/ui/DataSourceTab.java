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
package org.mapton.api.ui;

import java.io.File;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.input.TransferMode;
import javafx.scene.text.Font;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Strings;
import org.mapton.api.MDataSource;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class DataSourceTab extends Tab {

    private String mDefaults;
    private final String[] mDropExts;
    private final String mKey;
    private TextArea mTextArea;

    public DataSourceTab(String text, String key, String[] dropExts) {
        super(text);
        mKey = key;
        mDropExts = dropExts;
        init();
        initListeners();
    }

    public void load(String defaults) {
        mDefaults = defaults;
        mTextArea.setText(MDataSource.getPreferences().get(mKey, defaults));
    }

    public void restoreDefaults() {
        mTextArea.setText(mDefaults);
    }

    public void save() {
        MDataSource.getPreferences().put(mKey, mTextArea.getText());
    }

    private void append(File file) {
        mTextArea.appendText(file.getAbsolutePath() + "\n");
    }

    private void init() {
        mTextArea = new TextArea();
        mTextArea.setFont(Font.font("monospaced", FxHelper.getScaledFontSize() * 1.3));
        setContent(mTextArea);
    }

    private void initListeners() {
        mTextArea.setOnDragOver(dragEvent -> {
            var dragBoard = dragEvent.getDragboard();
            if (dragBoard.hasFiles()) {
                dragEvent.acceptTransferModes(TransferMode.COPY);
            }
        });

        mTextArea.setOnDragDropped(dragEvent -> {
            for (var file : dragEvent.getDragboard().getFiles()) {
                if (mDropExts == null) {
                    append(file);
                } else {
                    if (Strings.CI.equalsAny(FilenameUtils.getExtension(file.getName()), mDropExts)) {
                        append(file);
                    }
                }
            }
        });
    }
}
