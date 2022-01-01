/*
 * Copyright 2022 Patrik Karlström.
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
package org.mapton.worldwind;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfaceCircle;
import gov.nasa.worldwind.render.SurfacePolygon;
import java.awt.Color;
import java.util.ArrayList;
import static org.mapton.worldwind.ModuleOptions.*;
import org.mapton.worldwind.api.LayerBundleManager;

/**
 *
 * @author Patrik Karlström
 */
public class MaskLayer extends RenderableLayer {

    private final ModuleOptions mOptions = ModuleOptions.getInstance();
    private final ShapeAttributes mShapeAttributes = new BasicShapeAttributes();

    public MaskLayer() {
        setName("Mask");
        setPickEnabled(false);

        init();
        initListeners();

        refresh();
    }

    private void init() {
        mShapeAttributes.setDrawOutline(false);
        mShapeAttributes.setDrawInterior(true);

        //TODO
        //SurfacePolygon eats memory
        //SurfaceCircle leaves an equator gap
//        for (int i = 0; i < 2; i++) {
//            for (int j = 0; j < 2; j++) {
//                ArrayList<LatLon> l = new ArrayList<>();
//                int latSignum = i == 0 ? 1 : -1;
//                int lonSignum = j == 0 ? 1 : -1;
//                l.add(LatLon.fromDegrees(0, 0));
//                l.add(LatLon.fromDegrees(0, lonSignum * 180));
//                l.add(LatLon.fromDegrees(latSignum * 90, lonSignum * 180));
//                l.add(LatLon.fromDegrees(latSignum * 90, 0));
//
//                addRenderable(new SurfacePolygon(mShapeAttributes, l));
//            }
//        }
        int radius = 10000 * 5000;
        addRenderable(new SurfaceCircle(mShapeAttributes, LatLon.fromDegrees(90, 0), radius));
        addRenderable(new SurfaceCircle(mShapeAttributes, LatLon.fromDegrees(-90, 0), radius));

        //Fill the gap
        final double gap = 0.668646;
        int lonStep = 10;
        for (int lon = -180; lon < 180; lon = lon + lonStep) {
            for (int lat = -1; lat < 2; lat = lat + 1) {
                ArrayList<LatLon> l = new ArrayList<>();
                l.add(LatLon.fromDegrees(lat * gap, lon));
                l.add(LatLon.fromDegrees(lat * gap, (double) lon + lonStep));
                l.add(LatLon.fromDegrees(0, (double) lon + lonStep));
                l.add(LatLon.fromDegrees(0, lon));

                addRenderable(new SurfacePolygon(mShapeAttributes, l));
            }
        }
    }

    private void initListeners() {
        mOptions.getPreferences().addPreferenceChangeListener(evt -> {
            switch (evt.getKey()) {
                case KEY_DISPLAY_MASK:
                case KEY_MASK_COLOR:
                case KEY_MASK_OPACITY:
                    refresh();
                    break;
            }
        });
    }

    private void refresh() {
        setEnabled(mOptions.is(KEY_DISPLAY_MASK, DEFAULT_DISPLAY_MASK));

        String colorString = mOptions.get(KEY_MASK_COLOR, DEFAULT_MASK_COLOR);
        Color c = Color.decode("0x" + colorString);
        float opacity = mOptions.getFloat(KEY_MASK_OPACITY, DEFAULT_MASK_OPACITY);
        mShapeAttributes.setInteriorMaterial(new Material(c));
        mShapeAttributes.setInteriorOpacity(opacity);

        try {
            LayerBundleManager.getInstance().redraw();
        } catch (Exception e) {
            //nvm
        }
    }
}
