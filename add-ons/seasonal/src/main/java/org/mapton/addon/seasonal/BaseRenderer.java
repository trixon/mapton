/*
 * Copyright 2020 Patrik Karlström.
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
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.api.MLatLon;
import org.mapton.api.Mapton;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BaseRenderer implements Runnable {

    protected final RenderableLayer mLayer;
    private boolean mHollow = false;
    private MLatLon mLatLon = new MLatLon(0, 0);
    private Double mZoom = 0.05;

    public BaseRenderer(RenderableLayer layer) {
        mLayer = layer;
    }

    public MLatLon getLatLon() {
        return mLatLon;
    }

    public Double getZoom() {
        return mZoom;
    }

    public boolean isHollow() {
        return mHollow;
    }

    public void panTo() {
        if (ObjectUtils.allNotNull(mLatLon, mZoom)) {
            Mapton.getEngine().panTo(mLatLon, mZoom);
        }
    }

    public void setHollow(boolean hollow) {
        mHollow = hollow;
    }

    public void setLatLon(MLatLon latLon) {
        mLatLon = latLon;
    }

    public void setZoom(Double zoom) {
        mZoom = zoom;
    }

}
