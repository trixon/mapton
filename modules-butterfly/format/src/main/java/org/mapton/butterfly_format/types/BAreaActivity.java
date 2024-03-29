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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDateTime;

/**
 *
 * @author Patrik Karlström
 */
@JsonPropertyOrder({
    "id",
    "name",
    "description",
    "status",
    "datFrom",
    "datTo",
    "lat",
    "lon",
    "wkt"
})
@JsonIgnoreProperties(value = {"geometry"})
public class BAreaActivity extends BAreaBase {

    private LocalDateTime datFrom;
    private LocalDateTime datTo;
    private BAreaStatus status;

    public BAreaActivity() {
    }

    public LocalDateTime getDatFrom() {
        return datFrom;
    }

    public LocalDateTime getDatTo() {
        return datTo;
    }

    public BAreaStatus getStatus() {
        return status;
    }

    public void setDatFrom(LocalDateTime datFrom) {
        this.datFrom = datFrom;
    }

    public void setDatTo(LocalDateTime datTo) {
        this.datTo = datTo;
    }

    public void setStatus(BAreaStatus status) {
        this.status = status;
    }

    public enum BAreaStatus {
        TRIGGER,
        INFORMATION,
        OTHER;
    }
}
