/*
 * Copyright 2022 Patrik Karlström.
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

import java.util.LinkedHashMap;

/**
 *
 * @author Patrik Karlström
 */
public class MDocumentInfo {

    private LinkedHashMap<String, MAttribution> mAttributions = new LinkedHashMap<>();
    private String mName;

    public MDocumentInfo() {
    }

    public MDocumentInfo(String name) {
        mName = name;
    }

    public MDocumentInfo(String name, LinkedHashMap<String, MAttribution> attributions) {
        mName = name;
        mAttributions = attributions;
    }

    public LinkedHashMap<String, MAttribution> getAttributions() {
        return mAttributions;
    }

    public String getName() {
        return mName;
    }

    public void setAttributions(LinkedHashMap<String, MAttribution> attributions) {
        mAttributions = attributions;
    }

    public void setName(String name) {
        mName = name;
    }
}
