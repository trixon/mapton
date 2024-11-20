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
package org.mapton.butterfly_core.indicators;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Cylinder;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import javafx.collections.ListChangeListener;
import org.mapton.butterfly_core.api.BfLayerBundle;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class ScaleRulerLayerBundle extends BfLayerBundle {

    private final IndicatorAttributeManager mAttributeManager = IndicatorAttributeManager.getInstance();
    private final ScaleRulerManager mScaleRulerManager = ScaleRulerManager.getInstance();

    public ScaleRulerLayerBundle() {
        mLayer.setPickEnabled(false);
        setVisibleInLayerManager(mLayer, false);

        initRepaint();
        initListeners();
    }

    @Override
    public void populate() throws Exception {
        super.populate();
        setPopulated(true);
        mLayer.setEnabled(true);
        mLayer.setMaxActiveAltitude(1000);
        initCommons();

        repaint(DEFAULT_REPAINT_DELAY);
    }

    private void initListeners() {
        mScaleRulerManager.getItems().addListener((ListChangeListener.Change<? extends Position> c) -> {
            repaint();
        });
    }

    private void initRepaint() {
        setPainter(() -> {
            removeAllRenderables();
            if (!mLayer.isEnabled()) {
                return;
            }

            for (var position : mScaleRulerManager.getItems()) {
                plotScaleRuler(position);
            }

            setDragEnabled(false);
        });
    }

    private void plotScaleRuler(Position position) {
        var minutesYearToDate = TimeUnit.DAYS.toMinutes(LocalDateTime.now().getDayOfYear());
        var minutesYear = TimeUnit.DAYS.toMinutes(365);
        var altitude = 0.0;
        var prevHeight = 0.0;

        for (int i = 0; i < 10; i++) {
            var timeSpan = i == 0 ? minutesYearToDate : minutesYear;
            var height = timeSpan / 24000.0;
            altitude = altitude + height * 0.5 + prevHeight * 0.5;
            prevHeight = height;

            var pos = WWHelper.positionFromPosition(position, altitude);
            var cylinder = new Cylinder(pos, height, 5.0);

            cylinder.setAttributes(mAttributeManager.getScaleRulerAttribute(i));
            mLayer.addRenderable(cylinder);
        }
    }

}
