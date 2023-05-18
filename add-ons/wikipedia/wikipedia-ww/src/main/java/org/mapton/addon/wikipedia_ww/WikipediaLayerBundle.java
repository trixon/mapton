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
package org.mapton.addon.wikipedia_ww;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.awt.Color;
import javafx.collections.ListChangeListener;
import org.mapton.addon.wikipedia.api.WikipediaArticle;
import org.mapton.addon.wikipedia.api.WikipediaArticleManager;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.SystemHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class WikipediaLayerBundle extends LayerBundle {

    private final RenderableLayer mLayer = new RenderableLayer();
    private final WikipediaArticleManager mWikipediaArticleManager = WikipediaArticleManager.getInstance();

    public WikipediaLayerBundle() {
        mLayer.setName("Wikipedia");
        setCategoryAddOns(mLayer);
        setName("Wikipedia");

        init();
        initRepaint();
        initListeners();
    }

    @Override
    public void populate() throws Exception {
        getLayers().add(mLayer);
        repaint(0);
    }

    private void init() {
        mLayer.setPickEnabled(true);
        attachTopComponentToLayer("WikipediaTopComponent", mLayer);
    }

    private void initListeners() {
        mWikipediaArticleManager.getAllItems().addListener((ListChangeListener.Change<? extends WikipediaArticle> c) -> {
            repaint();
            mLayer.setEnabled(true);
        });
    }

    private void initRepaint() {
        setPainter(() -> {
            removeAllRenderables();

            String imageAddress = SystemHelper.getPackageAsPath(getClass()) + "Wikipedia-logo.png";
            for (var article : mWikipediaArticleManager.getAllItems()) {
                var placemark = new PointPlacemark(Position.fromDegrees(article.getLatLon().getLatitude(), article.getLatLon().getLongitude()));
                placemark.setLabelText(article.getTitle());
                placemark.setValue(AVKey.DISPLAY_NAME, article.getDescription());
                placemark.setLineEnabled(false);
                placemark.setEnableLabelPicking(true);

                placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                placemark.setEnableLabelPicking(true);

                var attrs = new PointPlacemarkAttributes(placemark.getDefaultAttributes());

                attrs.setImageAddress(imageAddress);
                attrs.setImageColor(Color.decode("#ff8888"));
                attrs.setLabelScale(1.0);
                attrs.setScale(0.15);
                attrs.setImageOffset(Offset.CENTER);

                placemark.setAttributes(attrs);
                placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(attrs, 1.5));
                placemark.setValue(WWHelper.KEY_RUNNABLE_LEFT_CLICK, (Runnable) () -> {
                    mWikipediaArticleManager.setSelectedItemAfterReset(article);
                });

                mLayer.addRenderable(placemark);
            }

            setDragEnabled(false);
        });
    }
}
