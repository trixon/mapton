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
package org.mapton.ww_datasources;

import gov.nasa.worldwind.layers.RenderableLayer;
import java.io.File;
import java.util.ArrayList;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.mapton.worldwind.api.LayerBundle;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GlobalStateChangeEvent;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class DatasourcesLayerBundle extends LayerBundle {

    private final RenderableLayer mLayer = new RenderableLayer();
    private static final String PREFIX = String.format("%s/", Dict.DATA_SOURCES.toString());

    public DatasourcesLayerBundle() {
        mLayer.setName(PREFIX + "Misc");
        refresh();
    }

    @Override
    public void populate() throws Exception {
        getLayers().add(mLayer);
        Mapton.getGlobalState().addListener((GlobalStateChangeEvent evt) -> {
            refresh();
        }, MKey.DATA_SOURCES_FILES);

        setPopulated(true);
    }

    private void refresh() {
        ArrayList<File> files = Mapton.getGlobalState().get(MKey.DATA_SOURCES_FILES);
        if (files != null) {
            for (File file : files) {
                System.out.println("got file! " + file.getAbsolutePath());
            }
        }
    }
}
