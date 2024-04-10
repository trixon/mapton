/*
 * Copyright 2024 Patrik Karlström.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import javafx.beans.property.SimpleLongProperty;
import org.apache.commons.lang3.ObjectUtils;
import org.controlsfx.control.IndexedCheckModel;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.openide.util.Lookup;

/**
 *
 * @author Patrik Karlström
 */
public class MDisruptorManager {

    private final GeometryFactory mGeometryFactory = JTSFactoryFinder.getGeometryFactory();
    private final Map<String, List<? extends Geometry>> mIdToGeometries = new HashMap<>();
    private final Map<String, List<MLatLon>> mIdToPoints = new HashMap<>();
    private final SimpleLongProperty mLastChangedProperty = new SimpleLongProperty();

    public static MDisruptorManager getInstance() {
        return Holder.INSTANCE;
    }

    private MDisruptorManager() {
    }

    public void clear() {
        mIdToPoints.clear();
    }

    public TreeSet<String> getCategories() {
        var providers = Lookup.getDefault().lookupAll(MDisruptorProvider.class);

        return new TreeSet<>(providers.stream().map(p -> p.getName()).toList());
    }

    public boolean isValidCoordinate(IndexedCheckModel checkModel, Double maxDistance, Double x, Double y) {
        if (ObjectUtils.anyNull(maxDistance, x, y)) {
            return false;
        }

        var point = mGeometryFactory.createPoint(new Coordinate(y, x));

        for (var entry : mIdToGeometries.entrySet()) {
            if (!checkModel.isChecked(entry.getKey())) {
                continue;
            }

            for (var geometry : entry.getValue()) {
                var distance = geometry.distance(point);
                if (distance <= maxDistance) {
                    return true;
                }
            }
        }

        return false;
    }

    public SimpleLongProperty lastChangedProperty() {
        return mLastChangedProperty;
    }

    public void put(String id, List<? extends Geometry> list) {
        mIdToGeometries.put(id, list);
        mLastChangedProperty.set(System.currentTimeMillis());
    }

    public List<MLatLon> remove(String id) {
        return mIdToPoints.remove(id);
    }

    private static class Holder {

        private static final MDisruptorManager INSTANCE = new MDisruptorManager();
    }
}
