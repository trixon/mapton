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

import com.fasterxml.jackson.annotation.JsonSetter;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BBasePointObservation {

    protected static final ConcurrentHashMap<String, String> CELL_CACHE = new ConcurrentHashMap<>(4096);

    private LocalDateTime date;
    private String name;

    protected String cacheString(String input) {
        if (input == null) {
            return null;
        }
        return CELL_CACHE.computeIfAbsent(input, s -> s);
    }

    public static void clearCache() {
        CELL_CACHE.clear();
    }

    public BBasePointObservation() {
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @JsonSetter
    public void setName(String name) {
        this.name = cacheString(name);
    }

}
