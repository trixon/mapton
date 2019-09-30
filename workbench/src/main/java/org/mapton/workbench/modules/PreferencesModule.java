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
import java.util.ResourceBundle;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import org.mapton.api.MOptions2;
import org.mapton.api.MWorkbenchModule;
import static org.mapton.api.Mapton.ICON_SIZE_MODULE;
import static org.mapton.api.Mapton.ICON_SIZE_MODULE_TOOLBAR;
import org.mapton.workbench.MaptonApplication;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class PreferencesModule extends MWorkbenchModule {

    private final ResourceBundle mBundle = SystemHelper.getBundle(MaptonApplication.class, "Bundle");

    public PreferencesModule(Scene scene) {
        super(scene, Dict.OPTIONS.toString(), MaterialIcon._Action.SETTINGS.getImageView(ICON_SIZE_MODULE).getImage());

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
        ToolbarItem saveToolbarItem = new ToolbarItem(MaterialIcon._Content.SAVE.getImageView(ICON_SIZE_MODULE_TOOLBAR),
                event -> MOptions2.getInstance().save());

        ToolbarItem discardToolbarItem = new ToolbarItem(MaterialIcon._Action.DELETE.getImageView(ICON_SIZE_MODULE_TOOLBAR),
                event -> getWorkbench().showConfirmationDialog(mBundle.getString("prefs.ui.discard_title"),
                        mBundle.getString("prefs.ui.discard_message"),
                        buttonType -> {
                            if (ButtonType.YES.equals(buttonType)) {
                                MOptions2.getInstance().discardChanges();
                            }
                        })
        );

        getToolbarControlsLeft().addAll(saveToolbarItem, discardToolbarItem);
    }
}
