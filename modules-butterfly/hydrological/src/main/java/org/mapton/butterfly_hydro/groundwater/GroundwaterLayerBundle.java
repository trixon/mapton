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
package org.mapton.butterfly_hydro.groundwater;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.awt.Color;
import java.util.ArrayList;
import javafx.collections.ListChangeListener;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.api.Mapton;
import org.mapton.butterfly_core.api.BfLayerBundle;
import org.mapton.butterfly_format.types.hydro.BGroundwaterPoint;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class GroundwaterLayerBundle extends BfLayerBundle {

    private final RenderableLayer mLayer = new RenderableLayer();
    private final GroundwaterManager mManager = GroundwaterManager.getInstance();

    public GroundwaterLayerBundle() {
        init();
        initRepaint();
        initListeners();

        mManager.setInitialTemporalState(WWHelper.isStoredAsVisible(mLayer, mLayer.isEnabled()));
    }

    @Override
    public void populate() throws Exception {
        getLayers().add(mLayer);
        repaint(DEFAULT_REPAINT_DELAY);
    }

    private void init() {
        mLayer.setName(Bundle.CTL_GroundwaterAction());
        setCategory(mLayer, SDict.HYDROGEOLOGY.toString());
        setName(Bundle.CTL_GroundwaterAction());
        attachTopComponentToLayer("GroundwaterTopComponent", mLayer);
        setParentLayer(mLayer);
        mLayer.setPickEnabled(true);

        mLayer.setEnabled(false);
    }

    private void initListeners() {
        mManager.getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BGroundwaterPoint> c) -> {
            repaint();
        });

        mLayer.addPropertyChangeListener("Enabled", pce -> {
            boolean enabled = mLayer.isEnabled();
            mManager.updateTemporal(enabled);

            if (enabled) {
                repaint();
            }
        });
    }

    private void initRepaint() {
        setPainter(() -> {
            removeAllRenderables();

            for (var cp : new ArrayList<>(mManager.getTimeFilteredItems())) {
                if (ObjectUtils.allNotNull(cp.getLat(), cp.getLon())) {
                    var placemark = new PointPlacemark(Position.fromDegrees(cp.getLat(), cp.getLon()));
                    placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                    placemark.setEnableLabelPicking(true);
                    var attrs = new PointPlacemarkAttributes(placemark.getDefaultAttributes());

                    placemark.setLabelText(cp.getName());
                    attrs.setImageAddress("images/pushpins/plain-white.png");
                    attrs.setImageColor(Color.YELLOW);
                    attrs.setScale(Mapton.SCALE_PIN_IMAGE);
                    attrs.setLabelScale(Mapton.SCALE_PIN_LABEL);

                    placemark.setAttributes(attrs);
                    placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(attrs, 1.5));

                    placemark.setValue(WWHelper.KEY_RUNNABLE_LEFT_CLICK, (Runnable) () -> {
                        mManager.setSelectedItemAfterReset(cp);
                    });

                    placemark.setValue(WWHelper.KEY_RUNNABLE_LEFT_DOUBLE_CLICK, (Runnable) () -> {
                        Almond.openAndActivateTopComponent((String) mLayer.getValue(WWHelper.KEY_FAST_OPEN));
                    });

                    mLayer.addRenderable(placemark);
                }
            }

            setDragEnabled(false);
        });
    }
}
