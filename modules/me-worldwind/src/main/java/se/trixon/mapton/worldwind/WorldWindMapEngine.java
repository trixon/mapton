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
package se.trixon.mapton.worldwind;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import javafx.scene.Node;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.NbLog;
import se.trixon.mapton.core.api.MapController;
import se.trixon.mapton.core.api.MapEngine;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MapEngine.class)
public class WorldWindMapEngine extends MapEngine {

    public static final String LOG_TAG = "WorldWind";
    private WorldWindMapController mMapController;
    private WorldWindowGLJPanel mWorldWindow;

    public WorldWindMapEngine() {
    }

    @Override
    public MapController getController() {
        return mMapController;
    }

    @Override
    public String getName() {
        return "WorldWind";
    }

    @Override
    public Node getStyleView() {
        return null;
    }

    @Override
    public Object getUI() {
        if (mWorldWindow == null) {
            init();
            mMapController = new WorldWindMapController();
        }

        return mWorldWindow;
    }

    private void init() {
        mWorldWindow = new WorldWindowGLJPanel();
        Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
        mWorldWindow.setModel(m);

        NbLog.v(LOG_TAG, "Loaded and ready");
    }

}
