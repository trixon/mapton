/*
 * Copyright 2024 Patrik Karlström.
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
package org.mapton.butterfly_core.api;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.mapton.api.ui.forms.MBaseFilterSection;
import org.openide.util.NbBundle;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class FilterSectionMisc extends MBaseFilterSection {

    private final ResourceBundle mBundle = NbBundle.getBundle(FilterSectionMisc.class);
    private final CheckBox mInvertCheckbox = new CheckBox();

    public FilterSectionMisc() {
        super("");
    }

    @Override
    public void clear() {
        super.clear();
        FxHelper.setSelected(false,
                mInvertCheckbox
        );
    }

    public CheckBox getInvertCheckbox() {
        return mInvertCheckbox;
    }

    public Node getInvertCheckboxToolBarItem() {
        mInvertCheckbox.setText(mBundle.getString("invertCheckBoxText"));
        var internalBox = new HBox(FxHelper.getUIScaled(8.0), mInvertCheckbox);
        internalBox.setPadding(FxHelper.getUIScaledInsets(0, 0, 0, 8.0));
        internalBox.setAlignment(Pos.CENTER_LEFT);

        return internalBox;
    }

    public void initListeners(FilterSectionMiscProvider filter) {
        filter.invertProperty().bind(invertSelectionProperty());
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        setSessionManager(sessionManager);

        sessionManager.register("filter.invert", invertSelectionProperty());
    }

    public BooleanProperty invertSelectionProperty() {
        return mInvertCheckbox.selectedProperty();
    }

    public void load() {
    }

    @Override
    public void onShownFirstTime() {
    }

    @Override
    public void reset(PropertiesConfiguration filterConfig) {
    }

}
