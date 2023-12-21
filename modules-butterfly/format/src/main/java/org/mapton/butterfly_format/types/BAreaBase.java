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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.locationtech.jts.geom.Geometry;

/**
 *
 * @author Patrik Karlström
 */
@JsonPropertyOrder({
    "id",
    "name",
    "description",
    "wkt"
})
public class BAreaBase extends BBasePoint {

    private String description;
    private String id;
    @JsonIgnore
    private Geometry geometry;
    @JsonIgnore
    private Geometry targetGeometry;
    private String wkt;

    public BAreaBase() {
    }

    public String getDescription() {
        return description;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public String getId() {
        return id;
    }

    public Geometry getTargetGeometry() {
        return targetGeometry;
    }

    public String getWkt() {
        return wkt;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTargetGeometry(Geometry targetGeometry) {
        this.targetGeometry = targetGeometry;
    }

    public void setWkt(String wkt) {
        this.wkt = wkt;
    }

}
