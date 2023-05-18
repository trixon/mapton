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

import org.locationtech.jts.geom.Geometry;

/**
 *
 * @author Patrik Karlström
 */
public class MArea {

    private boolean mEnabled;
    private Geometry mGeometry;
    private final String mKey;
    private String mName;
    private String mWktGeometry;

    public MArea(String key, String wktGeometry) {
        mKey = key;
        mWktGeometry = wktGeometry;
    }

    public MArea(String key) {
        mKey = key;
    }

    public Geometry getGeometry() {
        return mGeometry;
    }

    public String getKey() {
        return mKey;
    }

    public String getName() {
        return mName;
    }

    public String getWktGeometry() {
        return mWktGeometry;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public void setGeometry(Geometry geometry) {
        mGeometry = geometry;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setWktGeometry(String wktGeometry) {
        mWktGeometry = wktGeometry;
    }

    @Override
    public String toString() {
        return mName;
    }
}
