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
import java.io.IOException;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import static org.mapton.butterfly_format.BundleMode.DIR;
import static org.mapton.butterfly_format.BundleMode.ZIP;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BaseConfig {

    private final ButterflyLoader mButterflyLoader = ButterflyLoader.getInstance();
    private PropertiesConfiguration mConfig;
    private final String mFileName;
    private final ZipHelper mZipHelper = ZipHelper.getInstance();

    public BaseConfig(String fileName) {
        mFileName = fileName;
        init();
    }

    public PropertiesConfiguration getConfig() {
        return mConfig;
    }

    public void init() {
        var propertiesBuilderParameters = new Parameters().properties().setEncoding("UTF-8").setFile(getFile(mFileName));
        var builder = new Configurations().propertiesBuilder(propertiesBuilderParameters);

        try {
            mConfig = builder.getConfiguration();
        } catch (ConfigurationException ex) {
            try {
                var file = File.createTempFile("butterfly", "config");
                FileUtils.forceDeleteOnExit(file);
                builder = new Configurations().propertiesBuilder(file);
                mConfig = builder.getConfiguration();
            } catch (IOException | ConfigurationException ex1) {
                //Exceptions.printStackTrace(ex1);
            }

            //Exceptions.printStackTrace(ex);
        }
    }

    private File getFile(String fileName) {
        switch (mButterflyLoader.getBundleMode()) {
            case DIR -> {
                return new File(mButterflyLoader.getSource().getParentFile(), fileName);
            }

            case ZIP -> {
                return mZipHelper.extractResourceToTempFile(fileName);
            }

            default -> {
                return null;
            }
        }
    }
}
