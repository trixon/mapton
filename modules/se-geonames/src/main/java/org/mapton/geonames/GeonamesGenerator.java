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
import java.nio.charset.Charset;
import java.util.ArrayList;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GeonamesGenerator {

    private final File mBaseDir;
    private final ArrayList<Geoname> mGeonames = new ArrayList<>();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        new GeonamesGenerator();
    }

    public GeonamesGenerator() throws IOException {
        mBaseDir = FileUtils.getUserDirectory();
        populateCities();

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        FileUtils.write(new File(mBaseDir, "geonames.json"), gson.toJson(mGeonames), "utf-8");
    }

    private void populateCities() throws IOException {
        //http://download.geonames.org/export/dump/cities1000.zip
        try (CSVParser records = CSVParser.parse(
                new File(mBaseDir, "cities1000.txt"),
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
}
