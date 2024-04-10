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
package org.mapton.butterfly_topo;

import org.mapton.butterfly_format.BaseConfig;

/**
 *
 * @author Patrik Karlström
 */
public class TopoFilterDefaultsConfig extends BaseConfig {

    public static TopoFilterDefaultsConfig getInstance() {
        return Holder.INSTANCE;
    }

    private TopoFilterDefaultsConfig() {
        super("config/topo_filter.properties");
    }

    private static class Holder {

        private static final TopoFilterDefaultsConfig INSTANCE = new TopoFilterDefaultsConfig();
    }
}