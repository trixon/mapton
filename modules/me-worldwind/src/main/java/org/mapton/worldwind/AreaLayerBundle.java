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
package org.mapton.worldwind;

import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.SurfacePolyline;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TreeItem;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.mapton.api.MArea;
import org.mapton.api.MAreaFilterManager;
import org.mapton.api.MDict;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class AreaLayerBundle extends LayerBundle {

    private final MAreaFilterManager mAreaFilterManager = MAreaFilterManager.getInstance();
    private final RenderableLayer mLayer = new RenderableLayer();

    public AreaLayerBundle() {
        init();
        initRepaint();
        initListeners();
    }

    @Override
    public void populate() throws Exception {
        getLayers().add(mLayer);
        repaint(2000);
    }

    private void init() {
        String name = MDict.AREAS.toString();
        mLayer.setName(name);
        setCategorySystem(mLayer);
        setName(name);
        mLayer.setEnabled(true);
        mLayer.setPickEnabled(true);
        setParentLayer(mLayer);
    }

    private void initListeners() {
        mAreaFilterManager.getItems().addListener((ListChangeListener.Change<? extends MArea> c) -> {
            repaint();
        });

        mAreaFilterManager.getCheckedItems().addListener((ListChangeListener.Change<? extends TreeItem<MArea>> c) -> {
            repaint();
        });
    }

    private void initRepaint() {
        setPainter(() -> {
            removeAllRenderables();
            var attrs = new BasicShapeAttributes();
            attrs.setOutlineMaterial(Material.CYAN);
            attrs.setOutlineWidth(3.0);
            attrs.setDrawInterior(false);
            attrs.setDrawOutline(true);

            for (var area : mAreaFilterManager.getItems()) {
                if (area.isEnabled()) {
                    try {
                        var geometry = area.getGeometry();
                        if (geometry instanceof LineString lineString) {
                            var renderable = new SurfacePolyline(attrs, WWHelper.positionsFromGeometry(lineString, 0));
                            mLayer.addRenderable(renderable);
                        } else if (geometry instanceof Polygon polygon) {
                            var renderable = new SurfacePolygon(attrs, WWHelper.positionsFromGeometry(polygon, 0));
                            mLayer.addRenderable(renderable);
                        }
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }

                }
            }

            setDragEnabled(false);
        });
    }
}
