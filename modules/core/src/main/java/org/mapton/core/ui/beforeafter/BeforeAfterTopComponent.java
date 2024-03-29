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
package org.mapton.core.ui.beforeafter;

import com.dlsc.gemsfx.BeforeAfterView;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.mapton.core.api.MTopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.Actions;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.mapton.core.ui.beforeafter//BeforeAfter//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "BeforeAfterTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Mapton", id = "org.mapton.core.ui.beforeafter.BeforeAfterTopComponent")
@ActionReferences({
    @ActionReference(path = "Shortcuts", name = "DS-B"),
    @ActionReference(path = "Menu/Tools", position = 10)
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_BeforeAfterAction",
        preferredID = "BeforeAfterTopComponent"
)
@Messages({
    "CTL_BeforeAfterAction=&Before and after"
})
public final class BeforeAfterTopComponent extends MTopComponent {

    private BeforeAfterView mBeforeAfterView;

    public BeforeAfterTopComponent() {
        String name = Actions.cutAmpersand(Bundle.CTL_BeforeAfterAction());
        setName(name);
        putClientProperty("print.printable", Boolean.TRUE); // NOI18N
        putClientProperty("print.name", "Mapton - %s".formatted(name)); // NOI18N
    }

    @Override
    protected void initFX() {
        mBeforeAfterView = new BeforeAfterView();
        mBeforeAfterView.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        var scene = new Scene(mBeforeAfterView);
        setScene(scene);

        scene.setOnDragOver(dragEvent -> {
            var board = dragEvent.getDragboard();
            if (board.hasFiles()) {
                dragEvent.acceptTransferModes(TransferMode.COPY);
            }
        });

        getScene().setOnDragDropped(dragEvent -> {
            refresh(dragEvent.getDragboard().getFiles());
        });

        Mapton.getGlobalState().addListener(gsce -> {
            Platform.runLater(() -> {
                refresh(gsce.getValue());
            });
        }, MKey.BEFORE_AFTER_IMAGE);

        Mapton.getGlobalState().put(MKey.BEFORE_AFTER_IMAGE, Mapton.getGlobalState().get(MKey.BEFORE_AFTER_IMAGE));
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
    }

    @SuppressWarnings("unchecked")
    private void refresh(Object o) {
        if (o instanceof BufferedImage bi) {
            mBeforeAfterView.setAfter(mBeforeAfterView.getBefore());
            mBeforeAfterView.setBefore(new ImageView(SwingFXUtils.toFXImage(bi, null)));
        } else if (o instanceof List li && !li.isEmpty() && li.get(0) instanceof File) {
            List<File> files = (List<File>) o;
            if (files.size() > 1) {
                mBeforeAfterView.setAfter(new ImageView(files.get(1).getAbsolutePath()));
                mBeforeAfterView.setBefore(new ImageView(files.get(0).getAbsolutePath()));
            } else {
                mBeforeAfterView.setAfter(mBeforeAfterView.getBefore());
                mBeforeAfterView.setBefore(new ImageView(files.get(0).getAbsolutePath()));
            }
        }
    }
}
