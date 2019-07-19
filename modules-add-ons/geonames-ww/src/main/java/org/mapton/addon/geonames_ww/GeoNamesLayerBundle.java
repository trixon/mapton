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
package org.mapton.addon.geonames_ww;

import gov.nasa.worldwind.layers.RenderableLayer;
import java.util.ArrayList;
import javafx.collections.ObservableList;
import org.mapton.api.Mapton;
import org.mapton.geonames.api.Country;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.LayerBundleManager;
import org.mapton.worldwind.api.WWHelper;
import org.mapton.worldwind.api.analytic.AnalyticGrid;
import org.mapton.worldwind.api.analytic.CellAggregate;
import org.mapton.worldwind.api.analytic.GridData;
import org.mapton.worldwind.api.analytic.GridValue;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.GlobalStateChangeEvent;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class GeoNamesLayerBundle extends LayerBundle {

    private final RenderableLayer mLayer = new RenderableLayer();

    public GeoNamesLayerBundle() {
        init();
        initListeners();
    }

    @Override
    public void populate() {
        getLayers().add(mLayer);

        setPopulated(true);
    }

    private GridData getGridData(Country country) {
        ArrayList<GridValue> values = new ArrayList<>();
        country.getGeonames().forEach((geoname) -> {
            Double value = new Double(geoname.getPopulation());
            values.add(new GridValue(geoname.getLatLon(), value));
        });

        int width = 100;
        int height = 100;

        return new GridData(width, height, values, CellAggregate.SUM);
    }

    private void init() {
        mLayer.setName("GeoNames Visualizer");
        setCategoryAddOns(mLayer);
        mLayer.setValue(WWHelper.KEY_FAST_OPEN, "GeoNamesTopComponent");
        mLayer.setPickEnabled(false);
        mLayer.setEnabled(true);

        setName("GeoNames");
    }

    private void initListeners() {
        Mapton.getGlobalState().addListener((GlobalStateChangeEvent evt) -> {
            refresh();
        }, GeoN.KEY_LIST_SELECTION);
    }

    private void refresh() {
        mLayer.removeAllRenderables();

        ObservableList<Country> countries = Mapton.getGlobalState().get(GeoN.KEY_LIST_SELECTION);

        int altitude = 50000;
        int minValue = 0;
        int maxValue = 100000;

        for (Country country : countries) {
            AnalyticGrid analyticGrid = new AnalyticGrid(mLayer, altitude, minValue, maxValue);
            analyticGrid.setNullOpacity(0.0);
            analyticGrid.setZeroOpacity(0.3);
            analyticGrid.setZeroValueSearchRange(5);
            analyticGrid.setGridData(getGridData(country));
            mLayer.addRenderable(analyticGrid.getSurface());
        }

        LayerBundleManager.getInstance().redraw();
    }
}
