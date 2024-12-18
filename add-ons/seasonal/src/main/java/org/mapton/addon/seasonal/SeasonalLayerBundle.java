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
package org.mapton.addon.seasonal;

import gov.nasa.worldwind.layers.RenderableLayer;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.mapton.api.MKey;
import org.mapton.api.MSimpleObjectStorageManager;
import org.mapton.api.Mapton;
import org.mapton.worldwind.api.LayerBundle;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class SeasonalLayerBundle extends LayerBundle {

    private final int STARTUP_DELAY = 5;
    private final RenderableLayer mCandleLayer = new RenderableLayer();
    private final CandleRenderer mCandleRenderer;
    private final RenderableLayer mJulgranLayer = new RenderableLayer();
    private final JulgranRenderer mJulgranRenderer;
    private final RenderableLayer mLayer = new RenderableLayer();

    public SeasonalLayerBundle() {
        mLayer.setPickEnabled(false);
        setVisibleInLayerManager(mLayer, false);

        mCandleLayer.setPickEnabled(false);
        setVisibleInLayerManager(mCandleLayer, false);
        mCandleRenderer = new CandleRenderer(mCandleLayer);
        mJulgranRenderer = new JulgranRenderer(mJulgranLayer);
        initListeners();
    }

    @Override
    public void populate() {
        getLayers().addAll(mLayer, mCandleLayer, mJulgranLayer);
        setPopulated(true);
        mLayer.setEnabled(true);
        mLayer.setMinActiveAltitude(10000);
    }

    private void checkForRefresh() {
        String[] fettisdagar = {"20220301", "20230221", "20240213", "20250304", "20260217", "20270209", "20280229", "20290213"};
        String[] halloween = {"1030", "1031"};
        final String today = FastDateFormat.getInstance("yyyyMMdd").format(new Date(System.currentTimeMillis()));
        if (ArrayUtils.contains(fettisdagar, today)) {
            refresh(new MardiGrasRenderer(mLayer));
        } else if (Arrays.stream(halloween).anyMatch(s -> StringUtils.endsWith(today, s))) {
            refresh(new HalloweenRenderer(mLayer));
        }
    }

    private void initListeners() {
        Mapton.getExecutionFlow().executeWhenReady(MKey.EXECUTION_FLOW_MAP_WW_INITIALIZED, () -> {
            checkForRefresh();
        });

        MSimpleObjectStorageManager.getInstance().addListener(pcl -> {
            checkForRefresh();
        }, SeasonalSOSB.class);
    }

    private void refresh(BaseRenderer renderer) {
        removeAllRenderables();
        if (MSimpleObjectStorageManager.getInstance().getBoolean(SeasonalSOSB.class, true)) {
            new Thread(() -> {
                try {
                    TimeUnit.SECONDS.sleep(STARTUP_DELAY);
                    renderer.run();
                    if (renderer.isHollow()) {
                        var node = NbPreferences.root().node("org/mapton/me/worldwind");
                        node.put("map_style_prev", node.get("map_style", "se.trixon.hollow"));
                        node.put("map_style", "se.trixon.hollow");
                    }
                    renderer.panTo();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                    Thread.currentThread().interrupt();
                }
            }, getClass().getCanonicalName()).start();
        }
    }
}
