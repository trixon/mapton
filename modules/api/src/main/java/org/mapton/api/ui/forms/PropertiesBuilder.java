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
package org.mapton.api.ui.forms;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Patrik Karlström
 * @param <T>
 */
public abstract class PropertiesBuilder<T> {

    public static final String NA = "N/A";
    public static final String SEPARATOR = " :: ";
    private final HashMap<String, String> mNameToIndexName = new HashMap<>();

    public PropertiesBuilder() {
    }

    public abstract Object build(T selectedObject);

    public String getCatKey(String category, String value) {
        return "%s#%s".formatted(category, value);
    }

    public String getCatKeyNum(String category, String value) {
        String s = mNameToIndexName.computeIfAbsent(category, r -> {
            return "%d. %s".formatted(mNameToIndexName.size() + 1, category);
        });
        return getCatKey(s, value);
    }

    public void remove(Map<String, String> map, String category, String key, String value) {
        map.remove(getCatKeyNum(category, key));
    }

    public void removeByValues(Map<String, Object> map, String... values) {
        var iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            Object value = entry.getValue();
            if (value != null && StringUtils.equalsAnyIgnoreCase(value.toString(), values)) {
                iterator.remove();
            }
        }
    }

    public void replace(Map<String, String> map, String category, String key, String value) {
        map.put(getCatKeyNum(category, key), value);
    }

}
