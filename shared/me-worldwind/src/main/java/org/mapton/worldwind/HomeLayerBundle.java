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
package org.mapton.worldwind;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.awt.Color;
import java.util.prefs.PreferenceChangeEvent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import org.mapton.api.MLatLon;
import org.mapton.api.MOptions;
import org.mapton.api.Mapton;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.LayerBundleManager;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.GraphicsHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class HomeLayerBundle extends LayerBundle {

    private final BooleanProperty mDisplayHomeIconProperty = MOptions.getInstance().displayHomeIconProperty();
    private final RenderableLayer mLayer = new RenderableLayer();
    private final MOptions mOptions = MOptions.getInstance();

    public HomeLayerBundle() {
        init();
        initListeners();

        updatePlacemarks();
    }

    @Override
    public void populate() throws Exception {
        getLayers().add(mLayer);
        setPopulated(true);
    }

    private void init() {
        mLayer.setName("Home symbol");
        setVisibleInLayerManager(mLayer, false);
        mLayer.setEnabled(mDisplayHomeIconProperty.get());
        mLayer.setPickEnabled(false);
    }

    private void initListeners() {
        mDisplayHomeIconProperty.addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            mLayer.setEnabled(t1);
        });

        mOptions.getPreferences().addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            switch (evt.getKey()) {
                case MOptions.KEY_MAP_HOME_LAT:
                    updatePlacemarks();
                    break;
            }
        });
    }

    private void updatePlacemarks() {
        mLayer.removeAllRenderables();

        MLatLon home = mOptions.getMapHome();
        PointPlacemark placemark = new PointPlacemark(Position.fromDegrees(home.getLatitude(), home.getLongitude()));
        placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);

        PointPlacemarkAttributes attrs = new PointPlacemarkAttributes(placemark.getDefaultAttributes());
        attrs.setImage(GraphicsHelper.toBufferedImage(MaterialIcon._Action.HOME.getImageIcon(Mapton.getIconSizeToolBar() * 2, Color.RED).getImage()));
        attrs.setImageOffset(Offset.CENTER);

        placemark.setAttributes(attrs);
        placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(attrs, 1.0));

        mLayer.addRenderable(placemark);

        LayerBundleManager.getInstance().redraw();
    }
}
