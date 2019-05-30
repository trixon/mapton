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
package org.mapton.ww_mapollage;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.render.UserFacingIcon;
import java.awt.Dimension;
import java.io.File;
import org.mapton.api.Mapton;
import org.mapton.mapollage.api.Mapo;
import org.mapton.mapollage.api.MapoPhoto;
import org.mapton.mapollage.api.MapoSource;
import org.mapton.mapollage.api.MapoSourceManager;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.LayerBundleManager;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.GlobalState;
import se.trixon.almond.util.GlobalStateChangeEvent;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class MapollageLayerBundle extends LayerBundle {

    private final IconLayer mLayer = new IconLayer();
    private final MapoSourceManager mManager = MapoSourceManager.getInstance();
    private Mapo mMapo;

    public MapollageLayerBundle() {
        init();
        initListeners();
    }

    @Override
    public void populate() throws Exception {
        getLayers().add(mLayer);

        GlobalState globalState = Mapton.getGlobalState();
        globalState.addListener((GlobalStateChangeEvent evt) -> {
            mMapo = evt.getValue();
            refresh();
        }, Mapo.KEY_MAPO);

        globalState.addListener((GlobalStateChangeEvent evt) -> {
            if (mMapo != null) {
                refresh();
            }
        }, Mapo.KEY_SOURCE_MANAGER);

        setPopulated(true);
    }

    private void init() {
        mLayer.setName("Mapollage");
        mLayer.setEnabled(true);
        setName("Mapollage");
    }

    private void initListeners() {
    }

    private void refresh() {
        mLayer.removeAllIcons();

        for (MapoSource source : mManager.getItems()) {
            for (MapoPhoto photo : source.getCollection().getPhotos()) {
                String absolutePath = new File(source.getThumbnailDir(), String.format("%s.jpg", photo.getChecksum())).getAbsolutePath();
                UserFacingIcon icon = new UserFacingIcon(absolutePath, Position.fromDegrees(photo.getLat(), photo.getLon()));
                int downSample = 10;
                icon.setSize(new Dimension(photo.getWidth() / downSample, photo.getHeight() / downSample));
                icon.setHighlightScale(downSample);

                mLayer.addIcon(icon);
            }
        }

        LayerBundleManager.getInstance().redraw();
    }
}
