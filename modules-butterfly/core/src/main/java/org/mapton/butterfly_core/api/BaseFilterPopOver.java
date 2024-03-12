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
package org.mapton.butterfly_core.api;

import javafx.event.EventType;
import javafx.stage.WindowEvent;
import org.mapton.api.ui.MFilterPopOver;
import org.mapton.butterfly_format.Butterfly;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 * @param <T>
 */
public abstract class BaseFilterPopOver<T extends BaseFilterFavorite> extends MFilterPopOver {

    private Butterfly mButterfly;
    private final ButterflyManager mButterflyManager = ButterflyManager.getInstance();
    private boolean mFirstRun = true;

    public BaseFilterPopOver() {
        var butterflyProperty = mButterflyManager.butterflyProperty();
        butterflyProperty.addListener((p, o, n) -> {
            mButterfly = n;
            FxHelper.runLater(() -> load(n));
        });

        addEventHandler(EventType.ROOT, event -> {
            if (mFirstRun && event.getEventType() == WindowEvent.WINDOW_SHOWN) {
                onShownFirstTime();
                mFirstRun = false;
            }
        });
    }

    public void onShownFirstTime() {
    }

    public void applyFilterFavorite(T t) {

    }

    public T populateFilterFavorite() {
        return null;
    }

    public Butterfly getButterfly() {
        return mButterfly;
    }

    public void populate() {
        if (mButterflyManager.getButterfly() != null) {
            mButterfly = mButterflyManager.getButterfly();

            load(mButterfly);
        }
    }

    public abstract void load(Butterfly butterfly);

}
