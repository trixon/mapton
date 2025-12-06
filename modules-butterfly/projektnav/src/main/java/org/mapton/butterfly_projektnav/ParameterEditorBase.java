/*
 * Copyright 2025 Patrik Karlström.
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

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class ParameterEditorBase extends GridPane {

    public ParameterEditorBase() {
        setPadding(FxHelper.getUIScaledInsets(8.0));
        setHgap(FxHelper.getUIScaled(8.0));
        setVgap(FxHelper.getUIScaled(2.0));
    }

    protected void addConditionlly(StringBuilder sb, boolean selected, Object o) {
        if (selected) {
            sb.append("\t").append(o);
        }
    }

    protected Region createSpacer() {
        var region = new Region();
        region.setPrefHeight(FxHelper.getUIScaled(4.0));
        return region;
    }

    public enum EditMode {
        REPLACE,
        FIRST,
        LAST
    }

}
