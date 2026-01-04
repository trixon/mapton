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
package org.mapton.butterfly_rock_earthquake.updater;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.mapton.api.MPrint;
import org.mapton.api.Mapton;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.rock.BRockEarthquake;
import org.mapton.butterfly_rock_earthquake.updater.EarthquakeResponse.EarthquakeFeature;
import org.n52.jackson.datatype.jts.JtsModule;
import org.openide.util.Exceptions;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class EarthquakeGenerator {

    private final String mBaseUrl = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_%s.geojson";
    private final File mCacheDir;
    private final ObjectProperty<ObservableList<BRockEarthquake>> mItemsProperty = new SimpleObjectProperty<>();
    private final File mTrackerFile;
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
        mItemsProperty.setValue(FXCollections.synchronizedObservableList(FXCollections.observableArrayList()));
        mCacheDir = new File(Mapton.getCacheDir(), "earthquakes");
        mTrackerFile = new File(mCacheDir, "tracker");
    }

    public ObservableList<BRockEarthquake> getItems() {
        return mItemsProperty.get();
    }

    public File getTrackerFile() {
        return mTrackerFile;
    }

    private void deleteRedundantFiles() {
        //TODO delete all files older than the most recent MONTH file.
    }

    private String replace(String s) {
        return StringUtils.replaceEach(s,
                new String[]{"CA", "NV", "MX"},
                new String[]{"California", "Nevada", "Mexico"}
        );
    }

    public void parse() {
        var earthquakes = new ArrayList<BRockEarthquake>();
        var existingIds = new HashSet<String>();

        var mapper = new ObjectMapper();
        mapper.registerModule(new JtsModule());

        try (var paths = Files.walk(Paths.get(mCacheDir.toURI()))) {
            paths.filter(Files::isRegularFile)
                    .map(path -> path.toFile())
                    .filter(file -> file.getName().endsWith(".geojson"))
                    .forEach(file -> {
                        try {
                            var data = mapper.readValue(file, EarthquakeResponse.class);
                            var earthquakeFileItems = data.getFeatures().stream()
                                    .filter(f -> !existingIds.contains(f.getId()))
                                    .map(f -> {
                                        var q = new BRockEarthquake();
                                        var place = f.getProperty("place", String.class);
                                        var region = StringUtils.substringAfterLast(place, ", ");
                                        var placeMinusRegion = Strings.CI.removeEnd(place, ", " + region);
                                        region = replace(region);
                                        var polar = StringUtils.substringBefore(place, " of ");
                                        var town = StringUtils.substringAfter(place, " of ");
                                        var name = "%s (%s of)".formatted(town, polar);
                                        if (Strings.CI.contains(place, ",")) {
                                            q.setName("%s, %s".formatted(region, placeMinusRegion));
                                        } else {
                                            q.setName(place);
                                        }
                                        q.setClassification(Objects.toString(getCapitalized(f, "alert"), "-"));
                                        q.setCategory(f.getProperty("type", String.class));
                                        q.setStatus(getCapitalized(f, "status"));
                                        q.setTag(f.getProperty("types", String.class));
                                        q.setMag(f.getProperty("mag", Double.class));
                                        q.setMagType(f.getProperty("magType", String.class));
                                        q.setSig(f.getProperty("sig", Integer.class));
                                        q.setExternalId(f.getId());
                                        q.setLon(f.getGeometry().getX());
                                        q.setLat(f.getGeometry().getY());
                                        q.setZeroZ(f.getGeometry().getCoordinate().getZ());
                                        existingIds.add(f.getId());
                                        var time = f.getProperty("time", Long.class);
                                        q.setDateLatest(DateHelper.convertUtcMillisToLocalDateTime(time, ZoneId.of("Z")));
                                        q.setUnit(q.getMagType());
                                        q.setOrigin(getUpper(f, "net"));
                                        var sources = f.getProperty("sources", String.class);
                                        sources = StringUtils.mid(sources, 1, sources.length() - 2);
                                        q.setOperator(sources);
                                        var group = StringUtils.substringAfterLast(place, ", ");
                                        group = replace(group);
                                        q.setGroup(group);
                                        var tsunami = f.getProperty("tsunami", Integer.class);
                                        q.setAlarm1Id(tsunami == 1 ? "Tsunami" : "");
                                        var felt = f.getProperty("felt", Integer.class);
                                        q.setFrequency(felt == null ? 0 : felt);
//
                                        q.setDimension(BDimension._1d);
                                        q.setAlarm2Id("");
                                        q.setRollingFormula("");
                                        q.setSparse("");
                                        q.setUnitDiff("");
                                        q.setFrequencyDefault(0);
                                        q.setFrequencyHigh(0);
                                        q.setFrequencyHighParam("");

                                        return q;
                                    })
                                    .toList();
                            earthquakes.addAll(earthquakeFileItems);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }

                    });
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        earthquakes.sort(Comparator.comparing(BRockEarthquake::getDateLatest).reversed());
        FxHelper.runLater(() -> mItemsProperty.get().setAll(earthquakes));
    }

    private enum Feed {
        MONTH(TimeUnit.DAYS.toMillis(7)),
        WEEK(TimeUnit.DAYS.toMillis(1)),
        DAY(TimeUnit.HOURS.toMillis(6)),
        HOUR(TimeUnit.MINUTES.toMillis(5));
        private long mAgeLimit;

        private Feed(long ageLimit) {
            mAgeLimit = ageLimit;
        }

        public long getAgeLimit() {
            return mAgeLimit;
        }
    }

    public void update(MPrint print) throws IOException {
        mPrint = print;
        mPrint.out("USGS: Download BEG");
        var timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));

        var dl = false;
        dl = optionalDownload(Feed.MONTH, timestamp) || dl;
        dl = optionalDownload(Feed.WEEK, timestamp) || dl;
        dl = optionalDownload(Feed.DAY, timestamp) || dl;
        dl = optionalDownload(Feed.HOUR, timestamp) || dl;

        deleteRedundantFiles();

        if (dl) {
            parse();
        }
        FileUtils.touch(mTrackerFile);
        mPrint.out("USGS: Download END " + dl);
    }

    private String getCapitalized(EarthquakeFeature f, String key) {
        return StringUtils.capitalize(f.getProperty(key, String.class));
    }

    private String getUpper(EarthquakeFeature f, String key) {
        return StringUtils.toRootUpperCase(f.getProperty(key, String.class));
    }

    private boolean optionalDownload(Feed feed, String timestamp) throws IOException {
        var type = feed.name().toLowerCase(Locale.ENGLISH);
        mPrint.out("USGS: Needs update? " + type);

        var maxTimeStamp = Files.walk(mCacheDir.toPath())
                .filter(path -> Files.isRegularFile(path))
                .map(path -> path.toFile())
                .filter(file -> file.getName().matches("%s_.*\\.geojson".formatted(type)))
                .max(Comparator.comparingLong(file -> file.lastModified()))
                .map(file -> file.lastModified())
                .orElse(null);

        if (maxTimeStamp == null || SystemHelper.age(maxTimeStamp) > feed.getAgeLimit()) {
            mPrint.out("USGS: YES");
            var destFile = new File(mCacheDir, "%s_%s.geojson".formatted(type, timestamp));
            var url = URI.create(mBaseUrl.formatted(type)).toURL();
            mPrint.out("USGS: Copy <from,to>\n%s\n%s".formatted(url.toString(), destFile.toString()));
            FileUtils.copyURLToFile(url, destFile, 5000, 5000);
            return true;
        } else {
            mPrint.out("USGS: NO");
        }

        return false;
    }

    private static class Holder {

        private static final EarthquakeGenerator INSTANCE = new EarthquakeGenerator();
    }
}
