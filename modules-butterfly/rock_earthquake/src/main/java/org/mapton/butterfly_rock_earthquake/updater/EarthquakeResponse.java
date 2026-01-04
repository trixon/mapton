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
package org.mapton.butterfly_rock_earthquake.updater;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Point;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EarthquakeResponse {

    private double[] mBbox;
    private List<EarthquakeFeature> mFeatures;
    private Metadata mMetadata;

    public double[] getBbox() {
        return mBbox;
    }

    public List<EarthquakeFeature> getFeatures() {
        return mFeatures;
    }

    public Metadata getMetadata() {
        return mMetadata;
    }

    public void setBbox(double[] bbox) {
        mBbox = bbox;
    }

    public void setFeatures(List<EarthquakeFeature> features) {
        mFeatures = features;
    }

    public void setMetadata(Metadata metadata) {
        mMetadata = metadata;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EarthquakeFeature {

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
            var value = mProperties.get(key);
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

    public static class Metadata {

        private String mApi;
        private int mCount;
        private long mGenerated;
        private int mStatus;
        private String mTitle;
        private String mUrl;

        public String getApi() {
            return mApi;
        }

        public int getCount() {
            return mCount;
        }

        public long getGenerated() {
            return mGenerated;
        }

        public int getStatus() {
            return mStatus;
        }

        public String getTitle() {
            return mTitle;
        }

        public String getUrl() {
            return mUrl;
        }

        public void setApi(String api) {
            mApi = api;
        }

        public void setCount(int count) {
            mCount = count;
        }

        public void setGenerated(long generated) {
            mGenerated = generated;
        }

        public void setStatus(int status) {
            mStatus = status;
        }

        public void setTitle(String title) {
            mTitle = title;
        }

        public void setUrl(String url) {
            mUrl = url;
        }
    }
}
