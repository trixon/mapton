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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
@JsonPropertyOrder({
    "id",
    "name",
    "description"
})
public class BAlarm {

    private String description;
    private String id;
    private String limit1;
    private String limit2;
    private String limit3;
    private String name;
    private String ratio1;
    private String ratio2;
    private String ratio3;

    public BAlarm() {
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public String getLimit1() {
        return limit1;
    }

    public String getLimit2() {
        return limit2;
    }

    public String getLimit3() {
        return limit3;
    }

    public String getName() {
        return name;
    }

    public String getRatio1() {
        return ratio1;
    }

    public String getRatio2() {
        return ratio2;
    }

    public String getRatio3() {
        return ratio3;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLimit1(String limit1) {
        this.limit1 = limit1;
    }

    public void setLimit2(String limit2) {
        this.limit2 = limit2;
    }

    public void setLimit3(String limit3) {
        this.limit3 = limit3;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRatio1(String ratio1) {
        this.ratio1 = ratio1;
    }

    public void setRatio2(String ratio2) {
        this.ratio2 = ratio2;
    }

    public void setRatio3(String ratio3) {
        this.ratio3 = ratio3;
    }

}
