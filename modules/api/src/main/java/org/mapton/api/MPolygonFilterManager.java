/*
 * Copyright 2021 Patrik Karlström.
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

import java.awt.geom.Path2D;
import java.util.HashMap;
import java.util.HashSet;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Patrik Karlström
 */
public class MPolygonFilterManager {

    private final HashSet<Runnable> mListeners = new HashSet<>();
    private final HashMap<String, Path2D.Double> mNameToPolygon = new HashMap<>();
    private final ObservableMap<String, Path2D.Double> mObservableMap = FXCollections.observableMap(mNameToPolygon);

    public static MPolygonFilterManager getInstance() {
        return Holder.INSTANCE;
    }

    private MPolygonFilterManager() {
        mObservableMap.addListener((MapChangeListener.Change<? extends String, ? extends Path2D.Double> change) -> {
            mListeners.forEach(r -> {
                r.run();
            });
        });
    }

    public boolean addListener(Runnable r) {
        return mListeners.add(r);
    }

    public boolean contains(Double lat, Double lon) {
        return mNameToPolygon.values().stream()
                .anyMatch(path -> (path.contains(lon, lat)));
    }

    public boolean contains(MLatLon latLon) {
        return contains(latLon.getLatitude(), latLon.getLongitude());
    }

    public ObservableMap<String, Path2D.Double> getObservableMap() {
        return mObservableMap;
    }

    public boolean hasItems() {
        return !mNameToPolygon.isEmpty();
    }

    public boolean isValidCoordinate(Double lat, Double lon, boolean usePolygonfilter) {
        boolean validCoordinate = !hasItems() || !usePolygonfilter || usePolygonfilter && contains(lat, lon);

        return validCoordinate;
    }

    public boolean isValidCoordinate(MLatLon latLon, boolean usePolygonfilter) {
        return isValidCoordinate(latLon.getLatitude(), latLon.getLongitude(), usePolygonfilter);
    }

    public Path2D.Double put(String key, String value) {
        return put(key, createPathFromString(value));
    }

    public Path2D.Double put(String key, Path2D.Double value) {
        if (value != null) {
            return mObservableMap.put(key, value);

        } else {
            mObservableMap.remove(key);
            return null;
        }
    }

    private Path2D.Double createPathFromString(String string) {
        try {
            var p = new Path2D.Double();
            boolean first = true;
            for (var line : StringUtils.split(string, "\n")) {
                var elements = StringUtils.split(line, " ");
                var x = Double.valueOf(elements[0]);
                var y = Double.valueOf(elements[1]);
                if (first) {
                    first = false;
                    p.moveTo(x, y);
                } else {
                    p.lineTo(x, y);
                }
            }

            p.closePath();
            return p;
        } catch (Exception e) {
            //nvm - Invalid input
        }

        return null;
    }

    private static class Holder {

        private static final MPolygonFilterManager INSTANCE = new MPolygonFilterManager();
    }
}
