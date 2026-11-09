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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import se.trixon.almond.util.EpochHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BBasePointObservation {

    protected static final ConcurrentHashMap<String, String> CELL_CACHE = new ConcurrentHashMap<>(4096);
    @JsonIgnore
    private long mEpochSeconds = EpochHelper.NULL_EPOCH_SECONDS;
    private String name;

    public static void clearCache() {
        CELL_CACHE.clear();
    }

    public BBasePointObservation() {
    }

    /**
     * For presentation/UI purposes only.
     * Use getEpochSeconds() for filtering, sorting and calculations.
     *
     * @return
     */
    @JsonProperty("date")
    public LocalDateTime getDate() {
        return EpochHelper.epochSecondsToLocalDateTime(mEpochSeconds);
    }

    @JsonIgnore
    public long getEpochSeconds() {
        return mEpochSeconds;
    }

    public String getName() {
        return name;
    }

    public boolean hasDate() {
        return EpochHelper.hasDate(mEpochSeconds);
    }

    @JsonProperty("date")
    public void setDate(LocalDateTime date) {
        mEpochSeconds = EpochHelper.localDateTimeToEpochSeconds(date);
    }

    @JsonSetter
    public void setName(String name) {
        this.name = cacheString(name);
    }

    protected String cacheString(String input) {
        if (input == null) {
            return null;
        }
        return CELL_CACHE.computeIfAbsent(input, s -> s);
    }

}
