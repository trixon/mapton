/*
 * Copyright 2021 Patrik Karlström.
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

import java.util.Map;
import java.util.Optional;
import javafx.beans.value.ObservableValue;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.PropertySheet;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class MPropertyItem implements PropertySheet.Item {

    private final String mCategory;
    private final String mKey;
    private final String mName;
    private final Map<String, Object> mPropertyMap;

    public MPropertyItem(Map<String, Object> propertyMap, String key) {
        mPropertyMap = propertyMap;
        mKey = key;

        if (key.contains("#")) {
            String[] skey = StringUtils.split(key, "#", 2);
            mCategory = skey[0];
            if (skey.length > 1) {
                mName = skey[1];
            } else {
                mName = null;
            }
        } else {
            mCategory = Dict.UNCATEGORISED.toString();
            mName = key;
        }
    }

    @Override
    public String getCategory() {
        return mCategory;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.empty();
    }

    @Override
    public Class<?> getType() {
        Object o = mPropertyMap.get(mKey);
        return o == null ? Object.class : o.getClass();
    }

    @Override
    public Object getValue() {
        return mPropertyMap.get(mKey);
    }

    @Override
    public void setValue(Object value) {
        mPropertyMap.put(mKey, value);
    }

}
