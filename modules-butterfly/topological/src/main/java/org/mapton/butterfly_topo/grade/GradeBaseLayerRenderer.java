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
package org.mapton.butterfly_topo.grade;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.PointPlacemark;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.mapton.butterfly_format.types.topo.BTopoGrade;
import org.mapton.butterfly_topo.TopoLabelBy;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class GradeBaseLayerRenderer {

    private static final Logger sLOG = Logger.getLogger(GradeBaseLayerRenderer.class.getName());

    protected final GradeAttributeManager mAttributeManager = GradeAttributeManager.getInstance();
    protected final RenderableLayer mLabelLayer = new RenderableLayer();
    protected final RenderableLayer mPinLayer = new RenderableLayer();
    private final ArrayList<Layer> mLayers = new ArrayList<>();
    private final Layer mMasterLayer;

    public GradeBaseLayerRenderer(Layer masterLayer) {
        mMasterLayer = masterLayer;
        mLayers.add(mLabelLayer);
        mLayers.add(mPinLayer);
    }

    public ArrayList<Layer> getLayers() {
        return mLayers;
    }

    public Layer[] getLayersArr() {
        return mLayers.toArray(Layer[]::new);
    }

    public abstract void plot();

    public PointPlacemark plotLabel(BTopoGrade p, TopoLabelBy labelBy, Position position) {
//        if (labelBy == TopoLabelBy.NONE) {
//            return null;
//        }

        String label;
        try {
//            label = mOptionsView.getLabelBy().getLabel(p);
            label = p.getName();
        } catch (Exception e) {
            label = "ERROR %s <<<<<<<<".formatted(p.getName());
        }

        var placemark = new PointPlacemark(position);
        placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        placemark.setAttributes(mAttributeManager.getLabelPlacemarkAttributes());
        placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(mAttributeManager.getLabelPlacemarkAttributes(), 1.5));
        placemark.setLabelText(label);
        mLabelLayer.addRenderable(placemark);

        return placemark;
    }

    public PointPlacemark plotPin(BTopoGrade p, Position position, PointPlacemark labelPlacemark) {
        var attrs = mAttributeManager.getPinAttributes(p);

        var placemark = new PointPlacemark(position);
        placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        placemark.setAttributes(attrs);
        placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(attrs, 1.5));

        mPinLayer.addRenderable(placemark);
        if (labelPlacemark != null) {
            placemark.setValue(WWHelper.KEY_RUNNABLE_HOOVER_ON, (Runnable) () -> {
                labelPlacemark.setHighlighted(true);
            });
            placemark.setValue(WWHelper.KEY_RUNNABLE_HOOVER_OFF, (Runnable) () -> {
                labelPlacemark.setHighlighted(false);
            });
        }

        return placemark;
    }

    public abstract void reset();

}
