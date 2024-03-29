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
package org.mapton.butterfly_format.types.acoustic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.mapton.butterfly_format.types.BBasePoint;

/**
 *
 * @author Patrik Karlström
 */
@JsonPropertyOrder({
    "id",
    "dateTime",
    "lat",
    "lon",
    "name",
    "z",
    "comment",
    "group"
})
public class BBlast extends BBasePoint {

    private LocalDateTime dateTime;
    private String id;
    @JsonIgnore
    private Ext mExt;
    private String url;
    private Double z;

    public BBlast() {
    }

    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public Double getZ() {
        return z;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setZ(Double z) {
        this.z = z;
    }

    public class Ext {

        public long getAge(ChronoUnit chronoUnit) {
            var latest = getDateTime() != null ? getDateTime() : LocalDateTime.now().minusDays(1);

            return chronoUnit.between(latest, LocalDateTime.now());
        }

    }

}
