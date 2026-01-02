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
package org.mapton.butterfly_rock_earthquake.data;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.commons.io.FileUtils;
import org.mapton.api.MPrint;
import org.mapton.api.Mapton;

/**
 *
 * @author Patrik Karlström
 */
public class EarthquakeGenerator {

    private final File mCacheDir;
    private File mLast;
    private MPrint mPrint;

    public static EarthquakeGenerator getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
    }

    private EarthquakeGenerator() {
        mCacheDir = new File(Mapton.getCacheDir(), "earthquakes");
        mLast = new File(mCacheDir, "last");
    }

    public File getLast() {
        return mLast;
    }

    public void update(MPrint print) throws IOException {
        var timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        var destFile = new File(mCacheDir, "earthquakes_%s.geojson.json".formatted(timestamp));
        mPrint = print;
        String url = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_month.geojson";
        mPrint.out("USGS: Download " + url);
        FileUtils.copyURLToFile(URI.create(url).toURL(), destFile, 5000, 5000);
        FileUtils.touch(mLast);
        mPrint.out("USGS: Save " + destFile.getName());
    }

    private static class Holder {

        private static final EarthquakeGenerator INSTANCE = new EarthquakeGenerator();
    }
}
