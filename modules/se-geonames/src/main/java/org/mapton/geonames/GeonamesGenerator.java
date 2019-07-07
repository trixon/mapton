/*
 * Copyright 2019 Patrik Karlström.
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
package org.mapton.geonames;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.mapton.api.Mapton;
import se.trixon.almond.nbp.NbPrint;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GeonamesGenerator {

    private final File mCacheDir;
    private final File mCities1000txtFile;
    private final File mCities1000zipFile;
    private final ArrayList<Geoname> mGeonames = new ArrayList<>();
    private NbPrint mPrint;
    private final File mSearchEngineFile;

    public static GeonamesGenerator getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
    }

    private GeonamesGenerator() {
        mCacheDir = new File(Mapton.getCacheDir(), "geonames");
        mCities1000zipFile = new File(mCacheDir, "cities1000.zip");
        mCities1000txtFile = new File(mCacheDir, "cities1000.txt");
        mSearchEngineFile = new File(mCacheDir, "geonames.json");
    }

    public File getCities1000zipFile() {
        return mCities1000zipFile;
    }

    public File getSearchEngineFile() {
        return mSearchEngineFile;
    }

    public void update(NbPrint print) throws IOException {
        mPrint = print;
        mPrint.out("GeoNames: Download https://download.geonames.org/export/dump/cities1000.zip");
        FileUtils.copyURLToFile(new URL("https://download.geonames.org/export/dump/cities1000.zip"), mCities1000zipFile, 5000, 5000);
        mPrint.out("GeoNames: Extract cities1000.txt");
        extractZip();
        populateCities();
        mPrint.out("GeoNames: Save geonames.json");
        saveJson();
    }

    private void extractZip() throws IOException {
        HashMap<String, String> env = new HashMap<>();
        env.put("create", "true");

        URI uri = URI.create("jar:" + mCities1000zipFile.toURI().toString());

        try (FileSystem fileSystem = FileSystems.newFileSystem(uri, env)) {
            Path path = fileSystem.getPath("cities1000.txt");
            FileUtils.writeByteArrayToFile(mCities1000txtFile, Files.readAllBytes(path));
        }
    }

    private void populateCities() throws IOException {
        try (CSVParser records = CSVParser.parse(
                new File(mCacheDir, "cities1000.txt"),
                Charset.forName("utf-8"),
                CSVFormat.DEFAULT.withDelimiter('\t')
        )) {
            for (CSVRecord record : records) {
                String name = record.get(1);
                String asciiname = record.get(2);
                String alternateames = record.get(3);
                Double lat = MathHelper.convertStringToDouble(record.get(4));
                Double lon = MathHelper.convertStringToDouble(record.get(5));
                String countryCode = record.get(8);

                Geoname geoname = new Geoname();

                geoname.setName(name);
                geoname.setAsciiName(asciiname);
                geoname.setAlternateNames(alternateames);
                geoname.setCountryCode(countryCode);
                geoname.setLatitude(lat);
                geoname.setLongitude(lon);

                mGeonames.add(geoname);
            }
        }
    }

    private void saveJson() throws IOException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        FileUtils.write(mSearchEngineFile, gson.toJson(mGeonames), "utf-8");
    }

    private static class Holder {

        private static final GeonamesGenerator INSTANCE = new GeonamesGenerator();
    }
}
