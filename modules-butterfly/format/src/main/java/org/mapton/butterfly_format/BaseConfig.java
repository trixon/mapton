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
package org.mapton.butterfly_format;

import java.io.File;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BaseConfig {

    private PropertiesConfiguration mConfig;

    public BaseConfig() {
    }

    public PropertiesConfiguration getConfig() {
        return mConfig;
    }

    public void init(String fileName) {
        var file = new File(ButterflyLoader.getSourceDir(), fileName);
        var builder = new Configurations().propertiesBuilder(file);
        try {
            mConfig = builder.getConfiguration();
        } catch (ConfigurationException ex) {
            //Exceptions.printStackTrace(ex);
        }
    }
}
