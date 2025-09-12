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
package org.mapton.api;

import java.util.HashSet;
import org.apache.commons.lang3.ObjectUtils;

/**
 *
 * @author Patrik Karlström
 */
public class MCircleFilterManager {

    private MLatLon mLatLon;
    private final HashSet<Runnable> mListeners = new HashSet<>();
    private Double mRadius;

    public static MCircleFilterManager getInstance() {
        return Holder.INSTANCE;
    }

    private MCircleFilterManager() {
    }

    public boolean addListener(Runnable r) {
        return mListeners.add(r);
    }

    public MLatLon getLatLon() {
        return mLatLon;
    }

    public Double getRadius() {
        return mRadius;
    }

    public boolean isSet() {
        return ObjectUtils.allNotNull(mLatLon, mRadius);
    }

    public boolean isWithinCircle(Double lat, Double lon) {
        if (ObjectUtils.anyNull(lat, lon)) {
            return false;
        } else {
            if (ObjectUtils.allNotNull(mLatLon, mRadius)) {
                return mLatLon.distance(new MLatLon(lat, lon)) <= mRadius;
            } else {
                return true;
            }
        }
    }

    public void reset() {
        mLatLon = null;
        mRadius = null;
        mListeners.forEach(r -> {
            r.run();
        });
    }

    public void set(double lat, double lon, Double radius) {
        if (radius != null && radius > 0) {
            mLatLon = new MLatLon(lat, lon);
            mRadius = radius;
        } else {
            reset();
        }

        mListeners.forEach(r -> {
            r.run();
        });
    }

    private static class Holder {

        private static final MCircleFilterManager INSTANCE = new MCircleFilterManager();
    }
}
