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
import java.time.LocalDateTime;
import org.mapton.butterfly_format.Butterfly;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BBase {

    @JsonIgnore
    private transient Butterfly butterfly;
    private LocalDateTime dateChanged;
    private LocalDateTime dateCreated;
    private String meta;
    private String name;
    private String origin;

    public Butterfly getButterfly() {
        return butterfly;
    }

    public LocalDateTime getDateChanged() {
        return dateChanged;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
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

    public void setButterfly(Butterfly butterfly) {
        this.butterfly = butterfly;
    }

    public void setDateChanged(LocalDateTime dateChanged) {
        this.dateChanged = dateChanged;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
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

    @Override
    public String toString() {
        return getName();
    }

}
