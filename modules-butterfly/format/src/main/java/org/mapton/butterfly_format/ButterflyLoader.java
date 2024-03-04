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

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class ButterflyLoader {

    @Deprecated(forRemoval = true)
    private static File sourceDir;
    private final Butterfly butterfly = new Butterfly();
    private BundleMode mBundleMode;
    private File mSource;
    private final ZipHelper mZipHelper = ZipHelper.getInstance();

    public static ButterflyLoader getInstance() {
        return Holder.INSTANCE;
    }

    @Deprecated(forRemoval = true)
    public static File getSourceDir() {
        return sourceDir;
    }

    @Deprecated(forRemoval = true)
    public static void setSourceDir(File sourceDir) {
        ButterflyLoader.sourceDir = sourceDir;
    }

    private ButterflyLoader() {
    }

    public BundleMode getBundleMode() {
        return mBundleMode;
    }

    public Butterfly getButterfly() {
        return butterfly;
    }

    public File getSource() {
        return mSource;
    }

    public void load(BundleMode bundleMode, File source) {
        mBundleMode = bundleMode;
        mSource = source;
        var dir = mBundleMode == BundleMode.DIR ? source.getParentFile() : null;

        if (mBundleMode == BundleMode.DIR) {
            ButterflyLoader.setSourceDir(source.getParentFile());
        } else {
            mZipHelper.init(source);
        }

        butterfly.load(dir);
        butterfly.postLoad(source);
    }

    private static class Holder {

        private static final ButterflyLoader INSTANCE = new ButterflyLoader();
    }
}
