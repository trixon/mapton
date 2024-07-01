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
package org.mapton.core.ui.simple_object_storage;

import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import org.mapton.api.MGenericLoader;
import org.mapton.api.MGenericSaver;
import org.mapton.api.MSimpleObjectStorageManager;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BaseTab extends Tab implements MGenericLoader<Object>, MGenericSaver<Object> {

    protected final MSimpleObjectStorageManager mManager = MSimpleObjectStorageManager.getInstance();

    public BaseTab(String title) {
        super(title);
        setClosable(false);
    }

    public Label getGroupLabel(String group) {
        var label = new Label(group);
        var fontSize = FxHelper.getScaledFontSize();
        var fontStyle = "-fx-font-size: %.0fpx; -fx-font-weight: %s;";

        label.setStyle(fontStyle.formatted(fontSize * 1.2, "bold"));

        return label;
    }
}
