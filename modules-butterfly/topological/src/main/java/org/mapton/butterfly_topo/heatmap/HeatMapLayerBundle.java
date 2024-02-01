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
package org.mapton.butterfly_topo.heatmap;

import org.mapton.api.ui.forms.TabOptionsViewProvider;
import java.util.ArrayList;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.api.MLatLon;
import org.mapton.api.Mapton;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.TopoBaseLayerBundle;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.analytic.AnalyticGrid;
import org.mapton.worldwind.api.analytic.CellAggregate;
import org.mapton.worldwind.api.analytic.GridData;
import org.mapton.worldwind.api.analytic.GridValue;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class HeatMapLayerBundle extends TopoBaseLayerBundle {

    private HeatMapOptionsView mOptionsView;

    public HeatMapLayerBundle() {
        init();
        initRepaint();
        initListeners();
    }

    @Override
    public Node getOptionsView() {
        if (mOptionsView == null) {
            mOptionsView = (HeatMapOptionsView) TabOptionsViewProvider.getInstance(HeatMapOptionsView.class);
            if (mOptionsView != null) {
                mOptionsView.setLayerBundle(this);
            }
        }

        return mOptionsView;
    }

    @Override
    public void populate() {
        getLayers().addAll(mLayer, mLabelLayer, mSymbolLayer, mPinLayer);
        repaint(DEFAULT_REPAINT_DELAY);
    }

    private void init() {
        setCategory(mLayer, SDict.TOPOGRAPHY.toString());
        mLayer.setName(SDict.HEAT_MAP.toString());
        attachTopComponentToLayer("TopoTopComponent", mLayer);

        setName(SDict.HEAT_MAP.toString());
        mLabelLayer.setMaxActiveAltitude(2000);
        setParentLayer(mLayer);
        setAllChildLayers(mLabelLayer, mSymbolLayer, mPinLayer);

        mLayer.setPickEnabled(false);
        mLayer.setEnabled(false);

        setVisibleInLayerManager(mLayer, false);
    }

    private void initListeners() {
        mManager.getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BTopoControlPoint> c) -> {
            repaint();
        });

        mLayer.addPropertyChangeListener("Enabled", pce -> {
            boolean enabled = mLayer.isEnabled();
            if (enabled) {
                repaint();
            }
        });

        Mapton.getGlobalState().addListener(gsce -> {
            mLayer.setEnabled(gsce.getValue());
            repaint();
        }, HeatMapOptionsView.class.getName());
    }

    private void initRepaint() {
        setPainter(() -> {
            removeAllRenderables();

            if (!mLayer.isEnabled()) {
                return;
            }

            var values = new ArrayList<GridValue>();

            mManager.getTimeFilteredItems().stream()
                    .filter(p -> ObjectUtils.allNotNull(p.getLat(), p.getLon()))
                    .forEach(p -> {
                        var value = Double.valueOf(p.ext().getNumOfObservationsFiltered());
                        values.add(new GridValue(new MLatLon(p.getLat(), p.getLon()), value));

                    });

            int width = 5;
            int height = 30;

            var gridData = new GridData(width, height, values, CellAggregate.SUM);

            var minValue = values.stream()
                    .map(g -> g.getValue())
                    .mapToDouble(Double::doubleValue).min().orElse(0);

            var maxValue = values.stream()
                    .map(g -> g.getValue())
                    .mapToDouble(Double::doubleValue).max().orElse(0);

            var analyticGrid = new AnalyticGrid(mLayer, 50.0, minValue, maxValue);
            analyticGrid.setNullOpacity(0.0);
            analyticGrid.setZeroOpacity(0.3);
            analyticGrid.setZeroValueSearchRange(5);
            analyticGrid.setGridData(gridData);

            mLayer.addRenderable(analyticGrid.getSurface());
            setDragEnabled(false);
        });
    }
}
