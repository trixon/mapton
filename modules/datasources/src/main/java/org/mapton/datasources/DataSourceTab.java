/*
 * Copyright 2019 Patrik Karlström.
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
package org.mapton.datasources;

import java.io.File;
import java.util.prefs.Preferences;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.text.Font;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class DataSourceTab extends Tab {

    private String mDefaults;
    private final String[] mDropExts;
    private final String mKey;
    private final Preferences mPreferences = NbPreferences.forModule(DataSourcesPane.class);
    private TextArea mTextArea;

    public DataSourceTab(String text, String key, String[] dropExts) {
        super(text);
        mKey = key;
        mDropExts = dropExts;
        init();
        initListeners();
    }

    void load(String defaults) {
        mDefaults = defaults;
        mTextArea.setText(mPreferences.get(mKey, defaults));
    }

    void restoreDefaults() {
        mTextArea.setText(mDefaults);
    }

    void save() {
        mPreferences.put(mKey, mTextArea.getText());
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
        mTextArea.setOnDragOver((DragEvent event) -> {
            Dragboard board = event.getDragboard();
            if (board.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
        });

        mTextArea.setOnDragDropped((DragEvent event) -> {
            for (File file : event.getDragboard().getFiles()) {
                if (mDropExts == null) {
                    append(file);
                } else {
                    if (StringUtils.equalsAnyIgnoreCase(FilenameUtils.getExtension(file.getName()), mDropExts)) {
                        append(file);
                    }
                }
            }
        });
    }
}
