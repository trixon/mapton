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

import javafx.scene.Node;
import javafx.scene.control.TextArea;
import org.mapton.api.report.MEditor;
import org.mapton.api.report.MSplitNavSettings.TitleMode;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MEditor.class)
public class Base64Editor extends MEditor {

    private TextArea mSourceTextArea;
    private TextArea mDestTextArea;

    public Base64Editor() {
        getSplitNavSettings().setTitleMode(TitleMode.NAME);
    }

    @Override
    public String getName() {
        return "Base64";
    }

    @Override
    public Node getNode() {
        if (mSourceTextArea == null) {
            createUI();
        }

        return mBody;
    }

    private void createUI() {
        mSourceTextArea = new TextArea();
        mSourceTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
        });

        mDestTextArea = new TextArea();

        mNotificationPane.setContent(mSourceTextArea);
    }
}
