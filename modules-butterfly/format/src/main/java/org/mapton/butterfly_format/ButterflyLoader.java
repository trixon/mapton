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

    private final Butterfly butterfly = new Butterfly();
    private static File sourceDir;

    public static ButterflyLoader getInstance() {
        return Holder.INSTANCE;
    }

    public static File getSourceDir() {
        return sourceDir;
    }

    public static void setSourceDir(File sourceDir) {
        ButterflyLoader.sourceDir = sourceDir;
    }

    private ButterflyLoader() {
    }

    public Butterfly getButterfly() {
        return butterfly;
    }

    public void loadDir(File dir) {
        butterfly.loadDir(dir);
        butterfly.loadTmoObjekt(dir);
        butterfly.loadTmoObservations(dir);
        butterfly.postLoad(dir);
    }

    public void loadFile(File file) {
        butterfly.loadZip(file);
//        butterfly.postLoad(file);
    }

    private static class Holder {

        private static final ButterflyLoader INSTANCE = new ButterflyLoader();
    }
}
