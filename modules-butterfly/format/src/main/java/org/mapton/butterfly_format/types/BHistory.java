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
package org.mapton.butterfly_format.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.mapton.butterfly_format.types.BBase;

/**
 *
 * @author Patrik Karlström
 */
@JsonPropertyOrder({
    "origin",
    "name",
    "dateChanged",
    "field",
    "new",
    "old"
})
public class BHistory extends BBase {

    private String field;
    @JsonProperty("new")
    private String neww;
    private String old;

    public BHistory() {
    }

    public String getField() {
        return field;
    }

    public String getNew() {
        return neww;
    }

    public String getOld() {
        return old;
    }

    public void setField(String field) {
        this.field = field;
    }

    public void setNew(String neww) {
        this.neww = neww;
    }

    public void setOld(String old) {
        this.old = old;
    }

}
