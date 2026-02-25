/*
 * Copyright 2026 Patrik Karlström.
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
package org.mapton.butterfly_core.api;

import java.util.HashMap;

/**
 *
 * @author Patrik Karlström
 */
public class OffsetManager {

    private final HashMap<Class, Double> mZOffsetMap = new HashMap<>();

    private OffsetManager() {
    }

    public double getMinZ() {
        return mZOffsetMap.values().stream().mapToDouble(z -> z).min().orElse(0.0);
    }

    public void putZ(Class cls, double z) {
        mZOffsetMap.put(cls, z);
    }

    public static OffsetManager getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {

        private static final OffsetManager INSTANCE = new OffsetManager();
    }
}
