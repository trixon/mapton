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
package org.mapton.butterfly_format.types;

import java.time.LocalDateTime;
import java.util.HashMap;
import org.mapton.butterfly_format.Butterfly;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BBase {

    private Double azimuth;
    private transient Butterfly butterfly;
    private LocalDateTime dateChanged;
    private LocalDateTime dateCreated;
    private String externalId;
    private String meta;
    private String name;
    private String origin;
    private transient final HashMap<Object, Object> values = new HashMap<>();

    public Double getAzimuth() {
        return azimuth;
    }

    public Butterfly getButterfly() {
        return butterfly;
    }

    public LocalDateTime getDateChanged() {
        return dateChanged;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getMeta() {
        return meta;
    }

    public String getName() {
        return name;
    }

    public String getOrigin() {
        return origin;
    }

    public <T> T getValue(String key, Class<T> type) {
        return type.cast(getValues().get(key));
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(Object key) {
        return (T) (getValues().get(key));
    }

    public Object getValue(Object key, Object defaultValue) {
        return getValues().getOrDefault(key, defaultValue);
    }

    public HashMap<Object, Object> getValues() {
        return values;
    }

    public void setAzimuth(Double azimuth) {
        this.azimuth = azimuth;
    }

    public void setButterfly(Butterfly butterfly) {
        this.butterfly = butterfly;
    }

    public void setDateChanged(LocalDateTime dateChanged) {
        this.dateChanged = dateChanged;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public Object setValue(Object key, Object value) {
        return values.put(key, value);
    }

    @Override
    public String toString() {
        return getName();
    }

}
