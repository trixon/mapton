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

import java.util.List;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.api.MBaseDataManager;
import org.mapton.api.MLatLon;
import org.mapton.api.MLatLonBox;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.BBase;
import org.mapton.butterfly_format.types.BBasePoint;
import org.mapton.worldwind.api.WWHelper;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 * @param <T>
 */
public abstract class BaseManager<T extends BBase> extends MBaseDataManager<T> {

    private Butterfly mButterfly;
    private final ButterflyManager mButterflyManager = ButterflyManager.getInstance();

    public BaseManager(Class<T> typeParameterClass) {
        super(typeParameterClass);

        mButterflyManager.butterflyProperty().addListener((p, o, n) -> {
            mButterfly = n;
            FxHelper.runLater(() -> load(n));
        });

        if (mButterflyManager.getButterfly() != null) {
            mButterfly = mButterflyManager.getButterfly();
            FxHelper.runLater(() -> load(mButterfly));
        }
    }

    public Callable<List<String>> getCopyNamesCallable() {
        return () -> {
            return getTimeFilteredItems().stream().map(o -> o.getName()).toList();
        };
    }

    @Override
    public MLatLon getLatLonForItem(T t) {
        if (t instanceof BBasePoint cp) {
            try {
                return new MLatLon(cp.getLat(), cp.getLon());
            } catch (NullPointerException e) {
                //
            }
        }

        return null;
    }

    @Override
    public Object getMapIndicator(T t) {
        if (t instanceof BBasePoint) {
            try {
                return WWHelper.createIndicator(getLatLonForItem(t));
            } catch (NullPointerException e) {
                //
            }
        }

        return null;
    }

    public void initObjectToItemMap() {
        synchronized (getAllItems()) {
            for (var item : getAllItems()) {
                getAllItemsMap().put(item.getName(), item);
            }
        }
    }

    public abstract void load(Butterfly butterfly);

    @Override
    protected MLatLonBox getTimeFilteredExtents() {
        var latLons = getTimeFilteredItems().stream()
                .filter(p -> p instanceof BBasePoint)
                .map(p -> (BBasePoint) p)
                .filter(p -> ObjectUtils.allNotNull(p.getLat(), p.getLon()))
                .map(p -> new MLatLon(p.getLat(), p.getLon()))
                .toList();

        return new MLatLonBox(latLons);
    }
}
