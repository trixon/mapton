/*
 * Copyright 2020 Patrik Karlström.
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
package org.mapton.addon.notes;

import java.util.prefs.Preferences;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import org.mapton.api.report.MEditor;
import org.mapton.api.report.MEditorSystem;
import org.mapton.api.report.MSplitNavSettings.TitleMode;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MEditor.class)
public class NotesEditor extends MEditorSystem {

    private static final String KEY_NOTES = "notes";
    private final Preferences mPreferences = NbPreferences.forModule(NotesEditor.class);
    private TextArea mTextArea;

    public NotesEditor() {
        getSplitNavSettings().setTitleMode(TitleMode.NAME);
    }

    @Override
    public String getName() {
        return Dict.NOTES.toString();
    }

    @Override
    public Node getNode() {
        if (mTextArea == null) {
            createUI();
        }

        return mBody;
    }

    private void createUI() {
        mTextArea = new TextArea();
        mTextArea.setText(mPreferences.get(KEY_NOTES, ""));
        mTextArea.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            mPreferences.put(KEY_NOTES, mTextArea.getText());
        });

        mNotificationPane.setContent(mTextArea);
    }
}
