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

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.PointPlacemark;
import org.apache.commons.io.FilenameUtils;
import org.mapton.api.Mapton;
import org.mapton.mapollage.api.Mapo;
import org.mapton.mapollage.api.MapoPhoto;
import org.mapton.mapollage.api.MapoSource;
import org.mapton.mapollage.api.MapoSourceManager;
import org.mapton.worldwind.api.LayerBundle;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.GlobalState;
import se.trixon.almond.util.GlobalStateChangeEvent;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class MapollageLayerBundle extends LayerBundle {

    private final RenderableLayer mLayer = new RenderableLayer();
    private final MapoSourceManager mManager = MapoSourceManager.getInstance();
    private Mapo mMapo;

    public MapollageLayerBundle() {
        mLayer.setName("Mapollage-dev");
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

    private void refresh() {
        mLayer.removeAllRenderables();

        for (MapoSource source : mManager.getItems()) {
            for (MapoPhoto photo : source.getCollection().getPhotos()) {
                PointPlacemark placemark = new PointPlacemark(Position.fromDegrees(photo.getLat(), photo.getLon()));
                placemark.setLabelText(FilenameUtils.getBaseName(photo.getPath()));
                placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                placemark.setEnableLabelPicking(true);

                mLayer.addRenderable(placemark);
            }
        }
    }
}
