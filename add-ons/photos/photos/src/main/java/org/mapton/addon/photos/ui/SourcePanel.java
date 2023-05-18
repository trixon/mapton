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
package org.mapton.addon.photos.ui;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import org.mapton.addon.photos.api.MapoSource;
import se.trixon.almond.nbp.fx.FxDialogPanel;

/**
 *
 * @author Patrik Karlström
 */
public class SourcePanel extends FxDialogPanel {

    private BorderPane mRoot;

    private SourceView mSourceView;

    public SourcePanel() {
    }

    public void load(MapoSource source) {
        mSourceView.load(source);
    }

    public void save(MapoSource source) {
        mSourceView.save(source);
    }

    @Override
    protected void fxConstructor() {
        setScene(createScene());
        mRoot.setCenter(mSourceView = new SourceView(mNotifyDescriptor));
    }

    private Scene createScene() {
        return new Scene(mRoot = new BorderPane());
    }
}
