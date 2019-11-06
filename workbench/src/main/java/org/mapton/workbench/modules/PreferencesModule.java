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
package org.mapton.workbench.modules;

import com.dlsc.workbenchfx.view.controls.ToolbarItem;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import org.mapton.api.MOptions2;
import org.mapton.api.MWorkbenchModule;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class PreferencesModule extends MWorkbenchModule {

    public PreferencesModule() {
        super(Dict.OPTIONS.toString(), MaterialDesignIcon.SETTINGS);

        createUI();
    }

    @Override
    public Node activate() {
        return MOptions2.getInstance().getPreferencesFxView();
    }

    @Override
    public boolean destroy() {
        MOptions2.getInstance().save();

        return true;
    }

    private void createUI() {
        var saveToolbarItem = new ToolbarItem(new MaterialDesignIconView(MaterialDesignIcon.CONTENT_SAVE),
                event -> MOptions2.getInstance().save());

        var discardToolbarItem = new ToolbarItem(new MaterialDesignIconView(MaterialDesignIcon.DELETE),
                event -> getWorkbench().showConfirmationDialog(getBundleString("prefs.ui.discard_title"),
                        getBundleString("prefs.ui.discard_message"),
                        buttonType -> {
                            if (ButtonType.YES.equals(buttonType)) {
                                MOptions2.getInstance().discardChanges();
                            }
                        })
        );

        getToolbarControlsLeft().addAll(saveToolbarItem, discardToolbarItem);
    }
}
