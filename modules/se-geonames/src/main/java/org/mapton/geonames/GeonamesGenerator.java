/*
 * Copyright 2021 Patrik Karlström.
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

import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.io.FileUtils;
import org.mapton.api.MPrint;
import org.mapton.api.Mapton;
import org.mapton.geonames.api.Geoname;
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
    private MPrint mPrint;
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

    public void update(MPrint print) throws IOException {
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

        try ( var fileSystem = FileSystems.newFileSystem(uri, env)) {
            var path = fileSystem.getPath("cities1000.txt");
            FileUtils.writeByteArrayToFile(mCities1000txtFile, Files.readAllBytes(path));
        }
    }

    private void populateCities() throws IOException {
        try ( var csvRecords = CSVParser.parse(
                new File(mCacheDir, "cities1000.txt"),
                Charset.forName("utf-8"),
                CSVFormat.DEFAULT.builder().setHeader().setAllowMissingColumnNames(true).setDelimiter('\t').build()
        )) {
            for (var csvRecord : csvRecords) {
                String name = csvRecord.get(1);
                String asciiname = csvRecord.get(2);
                String alternateames = csvRecord.get(3);
                Double lat = MathHelper.convertStringToDouble(csvRecord.get(4));
                Double lon = MathHelper.convertStringToDouble(csvRecord.get(5));
                String countryCode = csvRecord.get(8);
                Integer population = MathHelper.convertStringToInteger(csvRecord.get(14));
                Integer elevation = MathHelper.convertStringToInteger(csvRecord.get(15));

                var geoname = new Geoname();

                geoname.setName(name);
                geoname.setAsciiName(asciiname);
                geoname.setAlternateNames(alternateames);
                geoname.setCountryCode(countryCode);
                geoname.setLatitude(lat);
                geoname.setLongitude(lon);
                geoname.setPopulation(population);
                geoname.setElevation(elevation);

                mGeonames.add(geoname);
            }

            mGeonames.sort((Geoname o1, Geoname o2) -> o1.getName().compareTo(o2.getName()));
        }
    }

    private void saveJson() throws IOException {
        var gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        FileUtils.write(mSearchEngineFile, gson.toJson(mGeonames), "utf-8");
    }

    private static class Holder {

        private static final GeonamesGenerator INSTANCE = new GeonamesGenerator();
    }
}
