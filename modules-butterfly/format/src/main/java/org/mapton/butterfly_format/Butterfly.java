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

import internal.org.mapton.butterfly_format.monmon.MonmonConfig;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.mapton.butterfly_format.io.DataCloner;
import org.mapton.butterfly_format.io.ImportFromCsv;
import org.mapton.butterfly_format.types.BAlarm;
import org.mapton.butterfly_format.types.BAreaActivity;
import org.mapton.butterfly_format.types.BAreaBase;
import org.mapton.butterfly_format.types.BBasePointObservation;
import org.mapton.butterfly_format.types.BCoordinate;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.BHistory;
import org.mapton.butterfly_format.types.BKeyVal;
import org.mapton.butterfly_format.types.BMeasurementMode;
import org.mapton.butterfly_format.types.BMeteoPoint;
import org.mapton.butterfly_format.types.BMeteoPointObservation;
import org.mapton.butterfly_format.types.BRoi;
import org.mapton.butterfly_format.types.BSystemKeyVal;
import org.mapton.butterfly_format.types.BSystemSearchProvider;
import org.mapton.butterfly_format.types.BSystemUser;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.butterfly_format.types.BXyzPointObservation;
import org.mapton.butterfly_format.types.acoustic.BAcousticVibrationChannel;
import org.mapton.butterfly_format.types.acoustic.BAcousticVibrationLimit;
import org.mapton.butterfly_format.types.acoustic.BAcousticVibrationObservation;
import org.mapton.butterfly_format.types.acoustic.BAcousticVibrationPoint;
import org.mapton.butterfly_format.types.geo.BGeoInclinometerPoint;
import org.mapton.butterfly_format.types.geo.BGeoInclinometerPointObservation;
import org.mapton.butterfly_format.types.geo.BGeoInclinometerPointObservation.ObservationItem;
import org.mapton.butterfly_format.types.geo.BGeoInclinometerPointObservationPre;
import org.mapton.butterfly_format.types.geo.BGeoReinforcementPoint;
import org.mapton.butterfly_format.types.hydro.BHydroGroundwaterPoint;
import org.mapton.butterfly_format.types.hydro.BHydroGroundwaterPointObservation;
import org.mapton.butterfly_format.types.hydro.BHydroWaterLevelPoint;
import org.mapton.butterfly_format.types.hydro.BHydroWaterLevelPointObservation;
import org.mapton.butterfly_format.types.remote.BRemoteInsarPoint;
import org.mapton.butterfly_format.types.remote.BRemoteInsarPointObservation;
import org.mapton.butterfly_format.types.remote.RemoteInsarPointDefaultsConfig;
import org.mapton.butterfly_format.types.rock.BRockBlast;
import org.mapton.butterfly_format.types.rock.BRockConvergence;
import org.mapton.butterfly_format.types.rock.BRockConvergenceObservation;
import org.mapton.butterfly_format.types.rock.BRockEarthquake;
import org.mapton.butterfly_format.types.rock.BRockExtensometer;
import org.mapton.butterfly_format.types.rock.BRockExtensometerPoint;
import org.mapton.butterfly_format.types.rock.BRockExtensometerPointObservation;
import org.mapton.butterfly_format.types.structural.BStructuralCrackPoint;
import org.mapton.butterfly_format.types.structural.BStructuralCrackPointObservation;
import org.mapton.butterfly_format.types.structural.BStructuralLoadCellPoint;
import org.mapton.butterfly_format.types.structural.BStructuralLoadCellPointObservation;
import org.mapton.butterfly_format.types.structural.BStructuralStrainGaugePoint;
import org.mapton.butterfly_format.types.structural.BStructuralStrainGaugePointObservation;
import org.mapton.butterfly_format.types.structural.BStructuralTiltPoint;
import org.mapton.butterfly_format.types.structural.BStructuralTiltPointObservation;
import org.mapton.butterfly_format.types.tmo.BGrundvatten;
import org.mapton.butterfly_format.types.tmo.BGrundvattenObservation;
import org.mapton.butterfly_format.types.tmo.BInfiltration;
import org.mapton.butterfly_format.types.tmo.BInfiltrationObservation;
import org.mapton.butterfly_format.types.tmo.BRorelse;
import org.mapton.butterfly_format.types.tmo.BRorelseObservation;
import org.mapton.butterfly_format.types.tmo.BTunnelvatten;
import org.mapton.butterfly_format.types.tmo.BTunnelvattenObservation;
import org.mapton.butterfly_format.types.tmo.BVaderstation;
import org.mapton.butterfly_format.types.tmo.BVattenkemi;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_format.types.topo.BTopoControlPointObservation;
import org.mapton.butterfly_format.types.topo.BTopoMonmon;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class Butterfly {

    public static final int FORMAT = 8;
    public static final String KEY_FORMAT = "FORMAT";
    public static final String KEY_TIMESTAMP = "TIMESTAMP";
    public static final String VERSION_FILE = "version.properties";
    private final ArrayList<BAlarm> mAlarms = new ArrayList<>();
    private final ArrayList<BHistory> mAlarmsHistory = new ArrayList<>();
    private final ArrayList<BAreaActivity> mAreaActivities = new ArrayList<>();
    private final ArrayList<BAreaBase> mAreaFilters = new ArrayList<>();
    private final ArrayList<BCoordinate> mCoordinates = new ArrayList<>();
    private final Dev mDev = new Dev();
    private final ArrayList<BGeoInclinometerPoint> mGeoInclinometerPoints = new ArrayList<>();
    private final ArrayList<BGeoInclinometerPointObservation> mGeoInclinometerPointsObservations = new ArrayList<>();
    private final ArrayList<BGeoInclinometerPointObservationPre> mGeoInclinometerPointsObservationsPre = new ArrayList<>();
    private final ArrayList<BGeoReinforcementPoint> mGeoReinforcementPoints = new ArrayList<>();
    private final Geotechnical mGeotechnical = new Geotechnical();
    private final Hydro mHydro = new Hydro();
    private final ButterflyManipulator mManipulator = new ButterflyManipulator();
    private final Meteo mMeteo = new Meteo();
    private final ArrayList<BMeteoPoint> mMeteoPoints = new ArrayList<>();
    private final ArrayList<BMeteoPointObservation> mMeteoPointsObservations = new ArrayList<>();
    private final Noise mNoise = new Noise();
    private final Remote mRemote = new Remote();
    private final ArrayList<BRemoteInsarPoint> mRemoteInsarPoints = new ArrayList<>();
    private final ArrayList<BRemoteInsarPointObservation> mRemoteInsarPointsObservations = new ArrayList<>();
    private final Rock mRock = new Rock();
    private final ArrayList<BRockBlast> mRockBlasts = new ArrayList<>();
    private final ArrayList<BRockConvergence> mRockConvergence = new ArrayList<>();
    private final ArrayList<BRockConvergenceObservation> mRockConvergenceObservations = new ArrayList<>();
    private final ArrayList<BRockEarthquake> mRockEarthquakes = new ArrayList<>();
    private final ArrayList<BRockExtensometer> mRockExtensometers = new ArrayList<>();
    private final ArrayList<BRockExtensometerPoint> mRockExtensometersPoints = new ArrayList<>();
    private final ArrayList<BRockExtensometerPointObservation> mRockExtensometersPointsObservations = new ArrayList<>();
    private final ArrayList<BRoi> mRois = new ArrayList<>();
    private File mSourceDir;
    private final Structural mStructural = new Structural();
    private final ArrayList<BStructuralCrackPoint> mStructuralCrackPoints = new ArrayList<>();
    private final ArrayList<BStructuralCrackPointObservation> mStructuralCrackPointsObservations = new ArrayList<>();
    private final ArrayList<BStructuralLoadCellPoint> mStructuralLoadPoints = new ArrayList<>();
    private final ArrayList<BStructuralLoadCellPointObservation> mStructuralLoadPointsObservations = new ArrayList<>();
    private final ArrayList<BStructuralStrainGaugePoint> mStructuralStrainPoints = new ArrayList<>();
    private final ArrayList<BStructuralStrainGaugePointObservation> mStructuralStrainPointsObservations = new ArrayList<>();
    private final ArrayList<BStructuralTiltPoint> mStructuralTiltPoints = new ArrayList<>();
    private final ArrayList<BStructuralTiltPointObservation> mStructuralTiltPointsObservations = new ArrayList<>();
    private final Sys mSys = new Sys();
    private final ArrayList<BSystemKeyVal> mSystemKeyVals = new ArrayList<>();
    private final ArrayList<BSystemSearchProvider> mSystemSearchProviders = new ArrayList<>();
    private final ArrayList<BSystemUser> mSystemUsers = new ArrayList<>();
    private final Tmo mTmo = new Tmo();
    private final Topo mTopo = new Topo();
    private final ArrayList<BTopoControlPoint> mTopoControlPoints = new ArrayList<>();
    private final ArrayList<BTopoControlPointObservation> mTopoControlPointsObservations = new ArrayList<>();
    private final ArrayList<BTopoMonmon> mTopoMonmons = new ArrayList<>();
    private final ArrayList<BAcousticVibrationChannel> mVibrationChannels = new ArrayList<>();
    private final ArrayList<BAcousticVibrationLimit> mVibrationLimits = new ArrayList<>();
    private final ArrayList<BAcousticVibrationObservation> mVibrationObservations = new ArrayList<>();
    private final ArrayList<BAcousticVibrationPoint> mVibrationPoints = new ArrayList<>();

    public Butterfly() {
    }

    /**
     * A place for incubating structures
     *
     * @return
     */
    public Dev dev() {
        return mDev;
    }

    public Geotechnical geotechnical() {
        return mGeotechnical;
    }

    public ArrayList<BAlarm> getAlarms() {
        return mAlarms;
    }

    public ArrayList<BHistory> getAlarmsHistory() {
        return mAlarmsHistory;
    }

    public ArrayList<BAreaActivity> getAreaActivities() {
        return mAreaActivities;
    }

    public ArrayList<BAreaBase> getAreaFilters() {
        return mAreaFilters;
    }

    public ButterflyManipulator getManipulator() {
        return mManipulator;
    }

    public ArrayList<BRoi> getRois() {
        return mRois;
    }

    public Hydro hydro() {
        return mHydro;
    }

    public void loadManual() {
        //Remote
        new ImportFromCsv<BRemoteInsarPoint>(BRemoteInsarPoint.class) {
        }.load(mSourceDir, "remoteInsarPoints.csv", mRemoteInsarPoints);

        new ImportFromCsv<BRemoteInsarPointObservation>(BRemoteInsarPointObservation.class) {
        }.load(mSourceDir, "remoteInsarPointsObservations.csv", mRemoteInsarPointsObservations);

        postLoadManual();
    }

    public Meteo meteo() {
        return mMeteo;
    }

    public Noise noise() {
        return mNoise;
    }

    public Remote remote() {
        return mRemote;
    }

    public Rock rock() {
        return mRock;
    }

    public Structural structural() {
        return mStructural;
    }

    public Sys sys() {
        return mSys;
    }

    public Tmo tmo() {
        return mTmo;
    }

    public Topo topo() {
        return mTopo;
    }

    void load(File sourceDir) {
        mSourceDir = sourceDir;
        List<ImportTask<?>> tasks = new ArrayList<>();
        int observationsCategory = 2_000_000;

        tasks.add(new ImportTask<>(BCoordinate.class, "coordinates.csv", mCoordinates));
        tasks.add(new ImportTask<>(BRoi.class, "roi.csv", mRois));
        tasks.add(new ImportTask<>(BAcousticVibrationPoint.class, "noiseVibrationPoints.csv", mVibrationPoints));
        tasks.add(new ImportTask<>(BAcousticVibrationChannel.class, "noiseVibrationChannels.csv", mVibrationChannels));
        tasks.add(new ImportTask<>(BAcousticVibrationLimit.class, "noiseVibrationLimits.csv", mVibrationLimits));
        tasks.add(new ImportTask<>(BAcousticVibrationObservation.class, "noiseVibrationObservations.csv", mVibrationObservations, observationsCategory));
        tasks.add(new ImportTask<>(BAlarm.class, "alarms.csv", mAlarms));
        tasks.add(new ImportTask<>(BHistory.class, "alarms_history.csv", mAlarmsHistory));
        tasks.add(new ImportTask<>(BAreaActivity.class, "areaActivities.csv", mAreaActivities));
        tasks.add(new ImportTask<>(BAreaBase.class, "areaFilters.csv", mAreaFilters));
        tasks.add(new ImportTask<>(BTopoControlPoint.class, "topoControlPoints.csv", mTopoControlPoints));
        tasks.add(new ImportTask<>(BTopoControlPointObservation.class, "topoControlPointsObservations.csv", mTopoControlPointsObservations, observationsCategory));
        // Structural
        tasks.add(new ImportTask<>(BStructuralCrackPoint.class, "structuralCrackPoints.csv", mStructuralCrackPoints));
        tasks.add(new ImportTask<>(BStructuralCrackPointObservation.class, "structuralCrackPointsObservations.csv", mStructuralCrackPointsObservations, observationsCategory));
        tasks.add(new ImportTask<>(BStructuralLoadCellPoint.class, "structuralLoadCellPoints.csv", mStructuralLoadPoints));
        tasks.add(new ImportTask<>(BStructuralLoadCellPointObservation.class, "structuralLoadCellPointsObservations.csv", mStructuralLoadPointsObservations, observationsCategory));
        tasks.add(new ImportTask<>(BStructuralStrainGaugePoint.class, "structuralStrainGaugePoints.csv", mStructuralStrainPoints));
        tasks.add(new ImportTask<>(BStructuralStrainGaugePointObservation.class, "structuralStrainGaugePointsObservations.csv", mStructuralStrainPointsObservations, observationsCategory));
        tasks.add(new ImportTask<>(BStructuralTiltPoint.class, "structuralTiltPoints.csv", mStructuralTiltPoints));
        tasks.add(new ImportTask<>(BStructuralTiltPointObservation.class, "structuralTiltPointsObservations.csv", mStructuralTiltPointsObservations, observationsCategory));
        // Rock
        tasks.add(new ImportTask<>(BRockBlast.class, "rockBlasts.csv", mRockBlasts));
        tasks.add(new ImportTask<>(BRockEarthquake.class, "rockEarthquakes.csv", mRockEarthquakes));
        tasks.add(new ImportTask<>(BRockConvergence.class, "rockConvergence.csv", mRockConvergence));
        tasks.add(new ImportTask<>(BRockConvergenceObservation.class, "rockConvergenceObservations.csv", mRockConvergenceObservations, observationsCategory));
        tasks.add(new ImportTask<>(BRockExtensometer.class, "rockExtensometers.csv", mRockExtensometers));
        tasks.add(new ImportTask<>(BRockExtensometerPoint.class, "rockExtensometersPoints.csv", mRockExtensometersPoints));
        tasks.add(new ImportTask<>(BRockExtensometerPointObservation.class, "rockExtensometersPointsObservations.csv", mRockExtensometersPointsObservations, observationsCategory));
        // TMO
        tasks.add(new ImportTask<>(BGrundvatten.class, "tmoGrundvatten.csv", mTmo.getGrundvatten()));
        tasks.add(new ImportTask<>(BInfiltration.class, "tmoInfiltration.csv", mTmo.getInfiltration()));
        tasks.add(new ImportTask<>(BRorelse.class, "tmoRorelse.csv", mTmo.getRorelse()));
        tasks.add(new ImportTask<>(BTunnelvatten.class, "tmoTunnelvatten.csv", mTmo.getTunnelvatten()));
// tasks.add(new ImportTask<>(BVattenkemi.class, "tmoVattenkemi.csv", mTmo.getVattenkemi()));
// tasks.add(new ImportTask<>(BVaderstation.class, "tmoVaderstation.csv", mTmo.getVaderstation()));
        tasks.add(new ImportTask<>(BGrundvattenObservation.class, "tmoGrundvattenObservations.csv", mTmo.getGrundvattenObservations(), observationsCategory));
        tasks.add(new ImportTask<>(BInfiltrationObservation.class, "tmoInfiltrationObservations.csv", mTmo.getInfiltrationObservations(), observationsCategory));
        tasks.add(new ImportTask<>(BTunnelvattenObservation.class, "tmoTunnelvattenObservations.csv", mTmo.getTunnelvattenObservations(), observationsCategory));
        tasks.add(new ImportTask<>(BRorelseObservation.class, "tmoRorelseObservations.csv", mTmo.getRorelseObservations(), observationsCategory));

        // Geotechnical
        tasks.add(new ImportTask<>(BGeoReinforcementPoint.class, "geoReinforcementPoints.csv", mGeoReinforcementPoints));
        tasks.add(new ImportTask<>(BGeoInclinometerPoint.class, "geoInclinometerPoints.csv", mGeoInclinometerPoints));
        tasks.add(new ImportTask<>(BGeoInclinometerPointObservationPre.class, "geoInclinometerPointsObservations.csv", mGeoInclinometerPointsObservationsPre));

        // System
        tasks.add(new ImportTask<>(BSystemKeyVal.class, "systemKeyValStore.csv", mSystemKeyVals));
        tasks.add(new ImportTask<>(BSystemSearchProvider.class, "systemSearchProviders.csv", mSystemSearchProviders));
        tasks.add(new ImportTask<>(BSystemUser.class, "systemUsers.csv", mSystemUsers));
        int cores = Runtime.getRuntime().availableProcessors();

        try (ExecutorService executor = Executors.newFixedThreadPool(cores)) {
            executor.submit(() -> hydro().load());
            executor.submit(() -> meteo().load());

            tasks.forEach(task -> executor.submit(() -> task.execute(sourceDir)));
        }

        BBasePointObservation.clearCache();
    }

    void postLoad() {
        List.of(
                mAlarms,
                mAlarmsHistory,
                mGeoInclinometerPoints,
                mGeoReinforcementPoints,
                mMeteoPoints,
                mRockBlasts,
                mRockConvergence,
                mRockEarthquakes,
                mRockExtensometers,
                mStructuralCrackPoints,
                mStructuralLoadPoints,
                mStructuralStrainPoints,
                mStructuralTiltPoints,
                mTopoControlPoints,
                mTopoMonmons,
                mVibrationPoints
        ).forEach(items -> items.forEach(item -> item.setButterfly(this)));

        for (var a : mAlarms) {
            a.ext().populateRanges();
            a.ext().getPoints().clear();
            for (ArrayList<? extends BXyzPoint> list : List.of(mTopoControlPoints)) {
                for (var p : list) {
                    if (Strings.CS.equalsAny(a.getId(), p.getAlarm1Id(), p.getAlarm2Id())) {
                        a.ext().getPoints().add(p);
                    }
                }
            }
        }
        hydro().postLoad();
        for (var p : mTopoControlPoints) {
            calcFreqHighBuffer(p);
        }

        for (var p : tmo().mGrundvatten) {
            p.setButterfly(this);
        }

        for (var p : mRockExtensometers) {
            p.setMeasurementMode(BMeasurementMode.AUTOMATIC);
        }

        structural().postLoad();
        topo().postLoad();
        geotechnical().postLoad();
        sys().postLoad();

        try {
            populateMonmon();
        } catch (Exception e) {
//            System.err.println(e.getMessage());
        }
    }

    void postLoadManual() {
        List.of(
                mRemoteInsarPoints
        ).forEach(items -> items.forEach(item -> item.setButterfly(this)));

        remote().postLoad();
    }

    private void calcFreqHighBuffer(BXyzPoint p) {
        var param = p.getFrequencyHighParam();
        if (param != null && p.ext() instanceof BXyzPoint.Ext<? extends BXyzPointObservation> ext) {
            var numberString = param.replaceAll("[^0-9]", "");
            if (StringUtils.isNotBlank(numberString)) {
                ext.setFrequenceHighBuffer(Double.valueOf(numberString + "d"));
            }
        }
    }

    private void populateMonmon() {
        var dataCloner = new DataCloner();
        var list = new ArrayList<BTopoMonmon>();
        var config = MonmonConfig.getInstance().getConfig();
        for (var iterator = config.getKeys(); iterator.hasNext();) {
            try {
                String name = iterator.next();
                var p = topo().getControlPointByName(name);
                if (p != null) {
                    var items = StringUtils.split(config.getString(name), ",");
                    var belongsTo = "";
                    if (items.length > 1) {
                        belongsTo = items[1];
                    }
                    var m = new BTopoMonmon(Integer.parseInt(items[0]), belongsTo, p);
                    dataCloner.convert(m, p);
                    list.add(m);
                }
            } catch (NumberFormatException e) {
                System.err.println(e);
            }
        }

        mTopoMonmons.clear();
        mTopoMonmons.addAll(list);
    }

    public class Dev {

        public ArrayList<BCoordinate> getCoordinates() {
            return mCoordinates;
        }

    }

    public class Ext {

    }

    public class Geotechnical {

        public ArrayList<BGeoInclinometerPoint> getInclinometerPoints() {
            return mGeoInclinometerPoints;
        }

        public ArrayList<BGeoInclinometerPointObservation> getInclinometerPointsObservations() {
            return mGeoInclinometerPointsObservations;
        }

        public ArrayList<BGeoReinforcementPoint> getReinforcementPoints() {
            return mGeoReinforcementPoints;
        }

        private void postLoad() {
            mGeoInclinometerPointsObservations.clear();
            var nameToObservations = mGeoInclinometerPointsObservationsPre.stream()
                    .collect(Collectors.groupingBy(BGeoInclinometerPointObservationPre::getName));

            for (var observationsPerPoint : nameToObservations.values()) {
                var dateToObservations = observationsPerPoint.stream()
                        .collect(Collectors.groupingBy(BGeoInclinometerPointObservationPre::getDate));

                for (var entry : dateToObservations.entrySet()) {
                    var o = new BGeoInclinometerPointObservation();
                    o.setDate(entry.getKey());
                    for (var o0 : entry.getValue()) {
                        o.setName(o0.getName());
                        var item = new ObservationItem();
                        try {
                            if (o0.getA() != null) {
                                item.setA(o0.getA() / 1000.0);
                            } else {
                                item.setA(0.0);
                            }

                            if (o0.getB() != null) {
                                item.setB(o0.getB() / 1000.0);
                            } else {
                                item.setB(0.0);
                            }

                            item.setDown(o0.getDown());
                            item.recalc();
                            o.getObservationItems().add(item);
                        } catch (NullPointerException e) {
                            //nvm
                            System.out.println(e);
                        }
                    }

                    if (o.getObservationItems().size() > 1) {
                        Collections.sort(o.getObservationItems(), Comparator.comparingDouble(ObservationItem::getDown).reversed());
                        mGeoInclinometerPointsObservations.add(o);
                    }
                }
            }

            Collections.sort(mGeoInclinometerPointsObservations, Comparator.comparing(BBasePointObservation::getName).thenComparing(Comparator.comparing(BBasePointObservation::getDate)));
        }
    }

    public class Hydro {

        private final ArrayList<BHydroGroundwaterPoint> mHydroGroundwaterPoints = new ArrayList<>();
        private final ArrayList<BHydroGroundwaterPointObservation> mHydroGroundwaterPointsObservations = new ArrayList<>();
        private final ArrayList<BHydroWaterLevelPoint> mHydroWaterLevelPoints = new ArrayList<>();
        private final ArrayList<BHydroWaterLevelPointObservation> mHydroWaterLevelPointsObservations = new ArrayList<>();

        public ArrayList<BHydroGroundwaterPoint> getGroundwaterPoints() {
            return mHydroGroundwaterPoints;
        }

        public ArrayList<BHydroGroundwaterPointObservation> getGroundwaterPointsObservations() {
            return mHydroGroundwaterPointsObservations;
        }

        public ArrayList<BHydroWaterLevelPoint> getWaterLevelPoints() {
            return mHydroWaterLevelPoints;
        }

        public ArrayList<BHydroWaterLevelPointObservation> getWaterLevelPointsObservations() {
            return mHydroWaterLevelPointsObservations;
        }

        private void load() {
            new ImportFromCsv<BHydroGroundwaterPoint>(BHydroGroundwaterPoint.class) {
            }.load(mSourceDir, "hydroGroundwaterPoints.csv", mHydroGroundwaterPoints);

            new ImportFromCsv<BHydroGroundwaterPointObservation>(BHydroGroundwaterPointObservation.class) {
            }.load(mSourceDir, "hydroGroundwaterPointsObservations.csv", mHydroGroundwaterPointsObservations);

            new ImportFromCsv<BHydroWaterLevelPoint>(BHydroWaterLevelPoint.class) {
            }.load(mSourceDir, "hydroWaterLevelPoints.csv", mHydroWaterLevelPoints);

            new ImportFromCsv<BHydroWaterLevelPointObservation>(BHydroWaterLevelPointObservation.class) {
            }.load(mSourceDir, "hydroWaterLevelPointsObservations.csv", mHydroWaterLevelPointsObservations);
        }

        private void postLoad() {
            List.of(
                    mHydroGroundwaterPoints,
                    mHydroWaterLevelPoints
            ).forEach(items -> items.forEach(item -> item.setButterfly(Butterfly.this)));

            for (var p : mHydroGroundwaterPoints) {
                p.setDimension(BDimension._1d);
            }

            for (var p : mHydroWaterLevelPoints) {
                p.setDimension(BDimension._1d);
            }
        }
    }

    public class Meteo {

        private TreeMap<String, String> mMeteoCodes = new TreeMap<>();

        public TreeMap<String, String> getMeteoCodes() {
            return mMeteoCodes;
        }

        public ArrayList<BMeteoPoint> getMeteoPoints() {
            return mMeteoPoints;
        }

        public ArrayList<BMeteoPointObservation> getMeteoPointsObservations() {
            return mMeteoPointsObservations;
        }

        private void load() {
            var meteoCodes = new ArrayList<BKeyVal>();
            new ImportFromCsv<BKeyVal>(BKeyVal.class) {
            }.load(mSourceDir, "meteoCodes.csv", meteoCodes);
            Map<String, String> map = meteoCodes.stream()
                    .collect(Collectors.toMap(BKeyVal::getKey, BKeyVal::getValue));
            mMeteoCodes.clear();
            mMeteoCodes.putAll(map);

            new ImportFromCsv<BMeteoPoint>(BMeteoPoint.class) {
            }.load(mSourceDir, "meteoPoints.csv", mMeteoPoints);

            new ImportFromCsv<BMeteoPointObservation>(BMeteoPointObservation.class) {
            }.load(mSourceDir, "meteoPointsObservations.csv", mMeteoPointsObservations);
        }

    }

    public class Noise {

        public ArrayList<BAcousticVibrationChannel> getVibrationChannels() {
            return mVibrationChannels;
        }

        public ArrayList<BAcousticVibrationLimit> getVibrationLimits() {
            return mVibrationLimits;
        }

        public ArrayList<BAcousticVibrationObservation> getVibrationObservations() {
            return mVibrationObservations;
        }

        public ArrayList<BAcousticVibrationPoint> getVibrationPoints() {
            return mVibrationPoints;
        }
    }

    public class Remote {

        private final HashMap<String, BRemoteInsarPoint> mNameToInsarPoint = new HashMap<>();

        public ArrayList<BRemoteInsarPoint> getInsarPoints() {
            return mRemoteInsarPoints;
        }

        public ArrayList<BRemoteInsarPointObservation> getInsarPointsObservations() {
            return mRemoteInsarPointsObservations;
        }

        public BRemoteInsarPoint getInsarPointByName(String name) {
            return mNameToInsarPoint.get(name);
        }

        public HashMap<String, BRemoteInsarPoint> getNameToInsarPoint() {
            return mNameToInsarPoint;
        }

        private void postLoad() {
            mNameToInsarPoint.clear();
            var categoryMap = new HashMap<String, String>();
            categoryMap.put("asc", "Ascending");
            categoryMap.put("desc", "Descending");
            var now = LocalDateTime.now();
            var defaults = RemoteInsarPointDefaultsConfig.getInstance().getConfig();
            for (var p : mRemoteInsarPoints) {
                mNameToInsarPoint.put(p.getName(), p);
                p.setDimension(BDimension._1d);
                p.setMeasurementMode(BMeasurementMode.AUTOMATIC);
                p.setFrequency(14);
                p.setFrequencyDefault(14);
                p.setFrequencyHigh(14);
                p.setNumOfDecXY(1);
                p.setNumOfDecZ(4);
                p.setUnit("m");
                p.setUnitDiff("mm");
                p.setDateCreated(now);
                p.setDateChanged(now);
                p.setVisible(true);
                if (defaults != null) {
                    p.setStatus(defaults.getString("STATUS"));
                    p.setOrigin(defaults.getString("ORIGIN"));
                    p.setOperator(defaults.getString("OPERATOR"));
                    p.setAlarm1Id(defaults.getString("ALARM_H"));
                    var grp = p.getGroup();
                    var grpB = defaults.getString("GRP_B", grp);
                    var grpG = defaults.getString("GRP_G", grp);
                    grp = StringUtils.replaceEach(grp,
                            new String[]{"B", "G"},
                            new String[]{grpB, grpG});
                    p.setGroup(grp);

                    var cat = p.getCategory();
                    var catAsc = defaults.getString("CAT_ASC", cat);
                    var catDesc = defaults.getString("CAT_DESC", cat);
                    cat = StringUtils.replaceEach(cat,
                            new String[]{"asc", "desc"},
                            new String[]{catAsc, catDesc});
                    p.setCategory(cat);
                } else {
                    p.setCategory(categoryMap.computeIfAbsent(p.getCategory(), s -> s));
                }
            }
        }
    }

    public class Rock {

        public ArrayList<BRockBlast> getBlasts() {
            return mRockBlasts;
        }

        public ArrayList<BRockConvergence> getConvergence() {
            return mRockConvergence;
        }

        public ArrayList<BRockConvergenceObservation> getConvergenceObservations() {
            return mRockConvergenceObservations;
        }

        public ArrayList<BRockEarthquake> getEarthquakes() {
            return mRockEarthquakes;
        }

        public ArrayList<BRockExtensometer> getExtensometers() {
            return mRockExtensometers;
        }

        public ArrayList<BRockExtensometerPoint> getExtensometersPoints() {
            return mRockExtensometersPoints;
        }

        public ArrayList<BRockExtensometerPointObservation> getExtensometersPointsObservations() {
            return mRockExtensometersPointsObservations;
        }

    }

    public class Structural {

        private final HashMap<String, BStructuralCrackPoint> mNameToCrackPoint = new HashMap<>();
        private final HashMap<String, BStructuralLoadCellPoint> mNameToLoadPoint = new HashMap<>();
        private final HashMap<String, BStructuralStrainGaugePoint> mNameToStrainPoint = new HashMap<>();
        private final HashMap<String, BStructuralTiltPoint> mNameToTiltPoint = new HashMap<>();

        public BStructuralCrackPoint getCrackPointByName(String name) {
            return mNameToCrackPoint.get(name);
        }

        public ArrayList<BStructuralCrackPoint> getCrackPoints() {
            return mStructuralCrackPoints;
        }

        public ArrayList<BStructuralCrackPointObservation> getCrackPointsObservations() {
            return mStructuralCrackPointsObservations;
        }

        public ArrayList<BStructuralLoadCellPoint> getLoadPoints() {
            return mStructuralLoadPoints;
        }

        public ArrayList<BStructuralLoadCellPointObservation> getLoadPointsObservations() {
            return mStructuralLoadPointsObservations;
        }

        public BStructuralStrainGaugePoint getStrainPointByName(String name) {
            return mNameToStrainPoint.get(name);
        }

        public ArrayList<BStructuralStrainGaugePoint> getStrainPoints() {
            return mStructuralStrainPoints;
        }

        public ArrayList<BStructuralStrainGaugePointObservation> getStrainPointsObservations() {
            return mStructuralStrainPointsObservations;
        }

        public BStructuralTiltPoint getTiltPointByName(String name) {
            return mNameToTiltPoint.get(name);
        }

        public ArrayList<BStructuralTiltPoint> getTiltPoints() {
            return mStructuralTiltPoints;
        }

        public ArrayList<BStructuralTiltPointObservation> getTiltPointsObservations() {
            return mStructuralTiltPointsObservations;
        }

        private void postLoad() {
            mNameToCrackPoint.clear();
            getCrackPoints().forEach(crackPoint -> {
                mNameToCrackPoint.put(crackPoint.getName(), crackPoint);
            });

            mNameToLoadPoint.clear();
            getLoadPoints().forEach(loadPoint -> {
                mNameToLoadPoint.put(loadPoint.getName(), loadPoint);
            });
            mNameToStrainPoint.clear();
            getStrainPoints().forEach(strainPoint -> {
                mNameToStrainPoint.put(strainPoint.getName(), strainPoint);
            });

            mNameToTiltPoint.clear();
            getTiltPoints().forEach(tiltPoint -> {
                mNameToTiltPoint.put(tiltPoint.getName(), tiltPoint);
            });
        }

    }

    public class Sys {

        private List<String> mKeyValList = List.of();
        /**
         * The top level map
         */
        private Map<String, String> mKeyValMap = Map.of();
        private Set<String> mKeyValSet = Set.of();
        private Set<String> mOrigins;

        public Map<String, String> getKeyValMap() {
            return mKeyValMap;
        }

        public ArrayList<BSystemKeyVal> getKeyVals() {
            return mSystemKeyVals;
        }

        public ArrayList<BSystemSearchProvider> getSearchProviders() {
            return mSystemSearchProviders;
        }

        public ArrayList<BSystemUser> getUsers() {
            return mSystemUsers;
        }

        public String getVal(String origin, String key) {
            try {
                return getKeyValMap().get(origin + key);
            } catch (Exception e) {
                return null;
            }
        }

        public List<String> getValAsList(String origin, String key) {
            try {
                var value = getKeyValMap().get(origin + key);
                if (StringUtils.isBlank(value)) {
                    return List.of();
                } else {
                    return List.of(StringUtils.split(value, '\n'))
                            .stream()
                            .sorted()
                            .toList();
                }
            } catch (Exception e) {
                return List.of();
            }
        }

        public List<String> getValAsList(String key) {
            return mOrigins.stream()
                    .flatMap(origin -> getValAsList(origin, key).stream())
                    .toList();
        }

        public Map<String, String> getValAsMap(String origin, String key) {
            return getValAsList(origin, key).stream()
                    .map(item -> item.split("=", 2))
                    .filter(parts -> parts.length == 2)
                    .collect(Collectors.toMap(parts -> parts[0].trim(), parts -> parts[1].trim()));
        }

        public Set<String> getValAsSet(String origin, String key) {
            try {
                return new TreeSet<>(getValAsList(origin, key));
            } catch (Exception e) {
                return Set.of();
            }
        }

        private void postLoad() {
            mKeyValMap = getKeyVals().stream()
                    .collect(Collectors.toMap(kv -> kv.getOrigin() + kv.getName(), BSystemKeyVal::getMeta));
            mOrigins = getKeyVals().stream().map(kv -> kv.getOrigin()).collect(Collectors.toSet());
        }
    }

    public class Tmo {

        private final ArrayList<BGrundvatten> mGrundvatten = new ArrayList<>();
        private final ArrayList<BGrundvattenObservation> mGrundvattenObservations = new ArrayList<>();
        private final ArrayList<BInfiltrationObservation> mInfiltrationObservations = new ArrayList<>();
        private final ArrayList<BInfiltration> mInfiltration = new ArrayList<>();
        private final ArrayList<BRorelse> mRorelse = new ArrayList<>();
        private final ArrayList<BRorelseObservation> mRorelseObservations = new ArrayList<>();
        private final ArrayList<BTunnelvatten> mTunnelvatten = new ArrayList<>();
        private final ArrayList<BTunnelvattenObservation> mTunnelvattenObservations = new ArrayList<>();
        private final ArrayList<BVaderstation> mVaderstation = new ArrayList<>();
        private final ArrayList<BVattenkemi> mVattenkemi = new ArrayList<>();

        public ArrayList<BTunnelvattenObservation> getTunnelvattenObservations() {
            return mTunnelvattenObservations;
        }

        public ArrayList<BGrundvatten> getGrundvatten() {
            return mGrundvatten;
        }

        public ArrayList<BGrundvattenObservation> getGrundvattenObservations() {
            return mGrundvattenObservations;
        }

        public ArrayList<BInfiltration> getInfiltration() {
            return mInfiltration;
        }

        public ArrayList<BInfiltrationObservation> getInfiltrationObservations() {
            return mInfiltrationObservations;
        }

        public ArrayList<BRorelse> getRorelse() {
            return mRorelse;
        }

        public ArrayList<BRorelseObservation> getRorelseObservations() {
            return mRorelseObservations;
        }

        public ArrayList<BTunnelvatten> getTunnelvatten() {
            return mTunnelvatten;
        }

        public ArrayList<BVaderstation> getVaderstation() {
            return mVaderstation;
        }

        public ArrayList<BVattenkemi> getVattenkemi() {
            return mVattenkemi;
        }

    }

    public class Topo {

        private final HashSet<String> mDeformationPoints = new HashSet();
        private final HashMap<String, BTopoControlPoint> mNameToControlPoint = new HashMap<>();

        public BTopoControlPoint getControlPointByName(String name) {
            return mNameToControlPoint.get(name);
        }

        public ArrayList<BTopoControlPoint> getControlPoints() {
            return mTopoControlPoints;
        }

        public ArrayList<BTopoControlPointObservation> getControlPointsObservations() {
            return mTopoControlPointsObservations;
        }

        public HashSet<String> getDeformationPoints() {
            return mDeformationPoints;
        }

        public ArrayList<BTopoMonmon> getMonmons() {
            return mTopoMonmons;
        }

        public void initDeformationPoints() {
            mDeformationPoints.clear();
            var lines = sys().getValAsList("system.deformation.boundaries");
            for (var line : lines) {
                if (!Strings.CI.startsWith(line, "#")) {
                    var items = StringUtils.splitByWholeSeparatorPreserveAllTokens(line, ":");
                    var pointNames = StringUtils.splitByWholeSeparatorPreserveAllTokens(items[1], ",");
                    mDeformationPoints.addAll(Arrays.asList(pointNames));
                }
            }
        }

        private void postLoad() {
            mNameToControlPoint.clear();
            getControlPoints().forEach(p -> {
                mNameToControlPoint.put(p.getName(), p);
            });
        }
    }
}
