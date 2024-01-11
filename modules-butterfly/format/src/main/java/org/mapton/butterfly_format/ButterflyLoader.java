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
import java.util.Date;
import org.apache.commons.io.FileUtils;

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

    public static void main(String[] args) {
        ButterflyLoader.setSourceDir(new File(FileUtils.getTempDirectory(), "butterfly"));
        ButterflyLoader.getInstance().load();
    }

    public static void setSourceDir(File ourceDir) {
        ButterflyLoader.sourceDir = ourceDir;
    }

    private ButterflyLoader() {
    }

    public Butterfly getButterfly() {
        return butterfly;
    }

    public Date getDate(File sourceDir) {
        var f = new File(sourceDir, "butterfly.properties");

        return new Date(f.lastModified());
    }

    public boolean load() {
        if (sourceDir.isDirectory()) {
            butterfly.load(sourceDir);
            butterfly.loadTmoObjekt(sourceDir);
            butterfly.loadTmoObservations(sourceDir);
            butterfly.postLoad(sourceDir);
        }

        return sourceDir.isDirectory();
    }

    private static class Holder {

        private static final ButterflyLoader INSTANCE = new ButterflyLoader();
    }
}
