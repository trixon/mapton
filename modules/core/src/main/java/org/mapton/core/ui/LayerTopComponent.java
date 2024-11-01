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
package org.mapton.core.ui;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import org.mapton.api.MOptions;
import org.mapton.core.api.MTopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.mapton.core.layer//Layer//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "LayerTopComponent",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED
)
@TopComponent.Registration(mode = "navigator", openAtStartup = false, position = 1)
@ActionID(category = "Mapton", id = "org.mapton.core.layer.LayerTopComponent")
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_LayerAction",
        preferredID = "LayerTopComponent"
)
@Messages({
    "CTL_LayerAction=Layer"
})
public final class LayerTopComponent extends MTopComponent {

    private BorderPane mBorderPane;
    private final MOptions mOptions = MOptions.getInstance();

    public LayerTopComponent() {
        putClientProperty(PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        putClientProperty(PROP_SLIDING_DISABLED, Boolean.TRUE);
        putClientProperty(PROP_UNDOCKING_DISABLED, Boolean.TRUE);
        putClientProperty(PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN, Boolean.TRUE);

        setName(Dict.LAYERS.toString());
        setPopOverHolder(true);

        mOptions.mapOnlyProperty().addListener((p, o, n) -> {
            FxHelper.runLaterDelayed(200, () -> fxComponentOpened());
        });
    }

    @Override
    protected void fxComponentOpened() {
        super.fxComponentOpened();
        if (mBorderPane != null) {
            mBorderPane.setCenter(LayerView.getInstance());
        }
    }

    @Override
    protected void initFX() {
        setScene(createScene());
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
    }

    private Scene createScene() {
        mBorderPane = new BorderPane(LayerView.getInstance());

        return new Scene(mBorderPane);
    }
}
