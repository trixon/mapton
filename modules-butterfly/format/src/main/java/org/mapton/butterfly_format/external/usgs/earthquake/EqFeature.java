/*
 * Copyright 2026 Patrik Karlström.
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
package org.mapton.butterfly_format.external.usgs.earthquake;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import org.locationtech.jts.geom.Point;

/**
 *
 * @author Patrik Karlström
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EqFeature {

    private Point mGeometry;
    private String mId;
    private Map<String, Object> mProperties;

    public Point getGeometry() {
        return mGeometry;
    }

    public String getId() {
        return mId;
    }

    public Map<String, Object> getProperties() {
        return mProperties;
    }

    public <T> T getProperty(String key, Class<T> type) {
        java.lang.Object value = mProperties.get(key);
        if (value == null) {
            return null;
        }
        if (type == Double.class && value.getClass() == Integer.class) {
            return type.cast(((Integer) value).doubleValue());
        }
        try {
            return type.cast(value);
        } catch (Exception e) {
            System.out.println(value);
            System.out.println(value.getClass());
            throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    public void setGeometry(Point geometry) {
        mGeometry = geometry;
    }

    public void setId(String id) {
        mId = id;
    }

    public void setProperties(Map<String, Object> properties) {
        mProperties = properties;
    }

}
