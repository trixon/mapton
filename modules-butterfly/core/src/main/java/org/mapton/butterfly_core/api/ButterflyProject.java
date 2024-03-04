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
package org.mapton.butterfly_core.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.mapton.butterfly_format.ButterflyLoader;
import org.mapton.butterfly_format.ZipHelper;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class ButterflyProject {

    private final ButterflyLoader mButterflyLoader = ButterflyLoader.getInstance();
    private final Properties mProperties = new Properties();
    private final ZipHelper mZipHelper = ZipHelper.getInstance();

    public static ButterflyProject getInstance() {
        return Holder.INSTANCE;
    }

    private ButterflyProject() {
        InputStream inputStream = null;
        switch (mButterflyLoader.getBundleMode()) {
            case DIR -> {
                try {
                    inputStream = FileUtils.openInputStream(mButterflyLoader.getSource());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            case ZIP -> {
                inputStream = mZipHelper.getStream("Project.bfl");
            }

            default ->
                throw new AssertionError();
        }

        try {
            mProperties.load(inputStream);
            inputStream.close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public String getCoordinateSystemHeight() {
        return mProperties.getProperty("COOSYS.HEIGHT");
    }

    public String getCoordinateSystemPlane() {
        return mProperties.getProperty("COOSYS.PLANE");
    }

    public String getName() {
        return mProperties.getProperty("NAME", "NONAME");
    }

    public Properties getProperties() {
        return mProperties;
    }

    private static class Holder {

        private static final ButterflyProject INSTANCE = new ButterflyProject();
    }
}
