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
package org.mapton.addon.notes;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import org.mapton.workbench.api.MWorkbenchModule;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MWorkbenchModule.class)
public class NotesModule extends MWorkbenchModule {

    private static final String KEY_NOTES = "notes";
    private TextArea mTextArea;

    public NotesModule() {
        super(Dict.NOTES.toString(), MaterialDesignIcon.PEN);

        mTextArea = new TextArea();
        mTextArea.setText(mPreferences.get(KEY_NOTES, ""));
        mTextArea.textProperty().addListener((ObservableValue<? extends String> ov, String t, String t1) -> {
            mPreferences.put(KEY_NOTES, mTextArea.getText());
        });
    }

    @Override
    public Node activate() {
        Platform.runLater(() -> {
            mTextArea.requestFocus();
        });

        return mTextArea;
    }
}
