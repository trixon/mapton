/*
 * Copyright 2018 Patrik Karlström.
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
package se.trixon.mapton.core.ui;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import se.trixon.almond.util.Dict;
import se.trixon.mapton.api.MTopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//se.trixon.mapton.core.layer//Layer//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "LayerTopComponent",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "navigator", openAtStartup = true)
@ActionID(category = "Mapton", id = "se.trixon.mapton.core.layer.LayerTopComponent")
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_LayerAction",
        preferredID = "LayerTopComponent"
)
@Messages({
    "CTL_LayerAction=Layer"
})
public final class LayerTopComponent extends MTopComponent {

    private BorderPane mBorderPane;

    public LayerTopComponent() {
        putClientProperty(PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        putClientProperty(PROP_SLIDING_DISABLED, Boolean.TRUE);
        putClientProperty(PROP_UNDOCKING_DISABLED, Boolean.TRUE);
        putClientProperty(PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN, Boolean.TRUE);

        setName(Dict.LAYERS.toString());
        setPopOverHolder(true);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                reInitFX();
            }
        });
    }

    @Override
    protected void initFX() {
        setScene(createScene());
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    private Scene createScene() {
        mBorderPane = new BorderPane();
        reInitFX();

        return new Scene(mBorderPane);
    }

    private void reInitFX() {
        Platform.runLater(() -> {
            mBorderPane.setCenter(new LayerView());
        });
    }
}
