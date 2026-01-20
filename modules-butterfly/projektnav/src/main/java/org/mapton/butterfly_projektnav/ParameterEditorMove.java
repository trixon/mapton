/*
 * Copyright 2026 Patrik Karlström.
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

import java.util.HashMap;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.mapton.api.Mapton;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.types.BXyzPoint;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ParameterEditorMove extends ParameterEditorBase {

    private final TextField mProj2Field = new TextField();
    private final TextField mRenameFromField = new TextField();
    private final TextField mRenameToField = new TextField();

    public ParameterEditorMove() {
        createUI();
    }

    public String preview(BaseManager<? extends BXyzPoint> manager, String[] names) {
        HashMap<String, String> originToId = Mapton.getGlobalState().getOrDefault("ParamEditor.originToId", new HashMap<String, String>());
        var rename = !mRenameFromField.getText().isBlank();
        var sb = new StringBuilder("proj1");
        addConditionlly(sb, true, "nr1");
        addConditionlly(sb, true, "proj2");
        addConditionlly(sb, rename, "nr2");
        sb.append("\n");

        for (var name : names) {
            var baseName = Strings.CI.removeEnd(name, "_H");
            baseName = Strings.CI.removeEnd(baseName, "_P");
            var p = manager.getAllItemsMap().get(baseName);

            var projid = originToId.getOrDefault(p.getOrigin(), "ERROR");
            sb.append(projid);

            addConditionlly(sb, true, name);
            addConditionlly(sb, true, StringUtils.defaultIfBlank(mProj2Field.getText(), "ERROR"));
            addConditionlly(sb, rename, Strings.CI.replaceOnce(name, mRenameFromField.getText(), mRenameToField.getText()));
            sb.append("\n");
        }

        var result = Strings.CI.removeEnd(sb.toString(), "\n");
        result = Strings.CI.removeEnd(result, "\n");

        return result;
    }

    private void createUI() {
        int row = 0;
        addRow(row++, new Label("MålprojektId"), new Label("Ersätt"), new Label("Med"));
        addRow(row++, mProj2Field, mRenameFromField, mRenameToField);
        FxHelper.autoSizeColumn(this, 3);
    }
}
