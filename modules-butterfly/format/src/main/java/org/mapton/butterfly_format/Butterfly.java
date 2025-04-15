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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_format.io.ImportFromCsv;
import org.mapton.butterfly_format.types.BAlarm;
import org.mapton.butterfly_format.types.BAreaActivity;
import org.mapton.butterfly_format.types.BAreaBase;
import org.mapton.butterfly_format.types.BBasePointObservation;
import org.mapton.butterfly_format.types.BCoordinate;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.BSystemSearchProvider;
import org.mapton.butterfly_format.types.acoustic.BAcousticVibrationChannel;
import org.mapton.butterfly_format.types.acoustic.BAcousticVibrationLimit;
import org.mapton.butterfly_format.types.acoustic.BAcousticVibrationObservation;
import org.mapton.butterfly_format.types.acoustic.BAcousticVibrationPoint;
import org.mapton.butterfly_format.types.acoustic.BAcousticBlast;
import org.mapton.butterfly_format.types.geo.BGeoExtensometer;
import org.mapton.butterfly_format.types.geo.BGeoExtensometerPoint;
import org.mapton.butterfly_format.types.geo.BGeoExtensometerPointObservation;
import org.mapton.butterfly_format.types.geo.BGeoInclinometerPoint;
import org.mapton.butterfly_format.types.geo.BGeoInclinometerPointObservation;
import org.mapton.butterfly_format.types.geo.BGeoInclinometerPointObservation.ObservationItem;
import org.mapton.butterfly_format.types.geo.BGeoInclinometerPointObservationPre;
import org.mapton.butterfly_format.types.hydro.BHydroGroundwaterPoint;
import org.mapton.butterfly_format.types.hydro.BHydroGroundwaterPointObservation;
import org.mapton.butterfly_format.types.monmon.BMonmon;
import org.mapton.butterfly_format.types.structural.BStructuralCrackPoint;
import org.mapton.butterfly_format.types.structural.BStructuralCrackPointObservation;
import org.mapton.butterfly_format.types.structural.BStructuralStrainGaugePoint;
import org.mapton.butterfly_format.types.structural.BStructuralStrainGaugePointObservation;
import org.mapton.butterfly_format.types.structural.BStructuralTiltPoint;
import org.mapton.butterfly_format.types.structural.BStructuralTiltPointObservation;
import org.mapton.butterfly_format.types.tmo.BGrundvatten;
import org.mapton.butterfly_format.types.tmo.BGrundvattenObservation;
import org.mapton.butterfly_format.types.tmo.BInfiltration;
import org.mapton.butterfly_format.types.tmo.BRorelse;
import org.mapton.butterfly_format.types.tmo.BRorelseObservation;
import org.mapton.butterfly_format.types.tmo.BTunnelvatten;
import org.mapton.butterfly_format.types.tmo.BVaderstation;
import org.mapton.butterfly_format.types.tmo.BVattenkemi;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_format.types.topo.BTopoControlPointObservation;
import org.mapton.butterfly_format.types.topo.BTopoConvergenceGroup;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class Butterfly {

    private final ArrayList<BAlarm> mAlarms = new ArrayList<>();
    private final ArrayList<BAreaActivity> mAreaActivities = new ArrayList<>();
    private final ArrayList<BAreaBase> mAreaFilters = new ArrayList<>();
    private final ArrayList<BAcousticBlast> mBlasts = new ArrayList<>();
    private final ArrayList<BCoordinate> mCoordinates = new ArrayList<>();
    private final Dev mDev = new Dev();
    private final ArrayList<BGeoExtensometer> mGeoExtensometers = new ArrayList<>();
    private final ArrayList<BGeoExtensometerPoint> mGeoExtensometersPoints = new ArrayList<>();
    private final ArrayList<BGeoExtensometerPointObservation> mGeoExtensometersPointsObservations = new ArrayList<>();
    private final ArrayList<BGeoInclinometerPoint> mGeoInclinometerPoints = new ArrayList<>();
    private final ArrayList<BGeoInclinometerPointObservation> mGeoInclinometerPointsObservations = new ArrayList<>();
    private final ArrayList<BGeoInclinometerPointObservationPre> mGeoInclinometerPointsObservationsPre = new ArrayList<>();
    private final Geotechnical mGeotechnical = new Geotechnical();
    private final Hydro mHydro = new Hydro();
    private final ArrayList<BHydroGroundwaterPoint> mHydroGroundwaterPoints = new ArrayList<>();
    private final ArrayList<BHydroGroundwaterPointObservation> mHydroGroundwaterPointsObservations = new ArrayList<>();
    private final ArrayList<BAcousticVibrationChannel> mVibrationChannels = new ArrayList<>();
    private final ArrayList<BAcousticVibrationLimit> mVibrationLimits = new ArrayList<>();
    private final ArrayList<BAcousticVibrationObservation> mVibrationObservations = new ArrayList<>();
    private final ArrayList<BAcousticVibrationPoint> mVibrationPoints = new ArrayList<>();
    private final ArrayList<BMonmon> mMonmons = new ArrayList<>();
    private final Noise mNoise = new Noise();
    private final Structural mStructural = new Structural();
    private final ArrayList<BStructuralCrackPoint> mStructuralCrackPoints = new ArrayList<>();
    private final ArrayList<BStructuralCrackPointObservation> mStructuralCrackPointsObservations = new ArrayList<>();
    private final ArrayList<BStructuralStrainGaugePoint> mStructuralStrainPoints = new ArrayList<>();
    private final ArrayList<BStructuralStrainGaugePointObservation> mStructuralStrainPointsObservations = new ArrayList<>();
    private final ArrayList<BStructuralTiltPoint> mStructuralTiltPoints = new ArrayList<>();
    private final ArrayList<BStructuralTiltPointObservation> mStructuralTiltPointsObservations = new ArrayList<>();
    private final Sys mSys = new Sys();
    private final ArrayList<BSystemSearchProvider> mSystemSearchProviders = new ArrayList<>();
    private final Tmo mTmo = new Tmo();
    private final Topo mTopo = new Topo();
    private final ArrayList<BTopoControlPoint> mTopoControlPoints = new ArrayList<>();
    private final ArrayList<BTopoControlPointObservation> mTopoControlPointsObservations = new ArrayList<>();
    private final ArrayList<BTopoConvergenceGroup> mTopoConvergenceGroups = new ArrayList<>();

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

    public ArrayList<BAreaActivity> getAreaActivities() {
        return mAreaActivities;
    }

    public ArrayList<BAreaBase> getAreaFilters() {
        return mAreaFilters;
    }

    public ArrayList<BMonmon> getMonmons() {
        return mMonmons;
    }

    public Hydro hydro() {
        return mHydro;
    }

    public Noise noise() {
        return mNoise;
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
        new ImportFromCsv<BCoordinate>(BCoordinate.class) {
        }.load(sourceDir, "coordinates.csv", mCoordinates);

        new ImportFromCsv<BAcousticBlast>(BAcousticBlast.class) {
        }.load(sourceDir, "noiseBlasts.csv", mBlasts);

        new ImportFromCsv<BAcousticVibrationPoint>(BAcousticVibrationPoint.class) {
        }.load(sourceDir, "noiseVibrationPoints.csv", mVibrationPoints);

        new ImportFromCsv<BAcousticVibrationChannel>(BAcousticVibrationChannel.class) {
        }.load(sourceDir, "noiseVibrationChannels.csv", mVibrationChannels);

        new ImportFromCsv<BAcousticVibrationLimit>(BAcousticVibrationLimit.class) {
        }.load(sourceDir, "noiseVibrationLimits.csv", mVibrationLimits);

        new ImportFromCsv<BAcousticVibrationObservation>(BAcousticVibrationObservation.class) {
        }.load(sourceDir, "noiseVibrationObservations.csv", mVibrationObservations);

        new ImportFromCsv<BAlarm>(BAlarm.class) {
        }.load(sourceDir, "alarms.csv", mAlarms);

        new ImportFromCsv<BAreaActivity>(BAreaActivity.class) {
        }.load(sourceDir, "areaActivities.csv", mAreaActivities);

        new ImportFromCsv<BAreaBase>(BAreaBase.class) {
        }.load(sourceDir, "areaFilters.csv", mAreaFilters);

        new ImportFromCsv<BTopoControlPoint>(BTopoControlPoint.class) {
        }.load(sourceDir, "topoControlPoints.csv", mTopoControlPoints);

        new ImportFromCsv<BTopoControlPointObservation>(BTopoControlPointObservation.class) {
        }.load(sourceDir, "topoControlPointsObservations.csv", mTopoControlPointsObservations);

        //Structural
        new ImportFromCsv<BStructuralCrackPoint>(BStructuralCrackPoint.class) {
        }.load(sourceDir, "structuralCrackPoints.csv", mStructuralCrackPoints);

        new ImportFromCsv<BStructuralCrackPointObservation>(BStructuralCrackPointObservation.class) {
        }.load(sourceDir, "structuralCrackPointsObservations.csv", mStructuralCrackPointsObservations);

        new ImportFromCsv<BStructuralStrainGaugePoint>(BStructuralStrainGaugePoint.class) {
        }.load(sourceDir, "structuralStrainGaugePoints.csv", mStructuralStrainPoints);

        new ImportFromCsv<BStructuralStrainGaugePointObservation>(BStructuralStrainGaugePointObservation.class) {
        }.load(sourceDir, "structuralStrainGaugePointsObservations.csv", mStructuralStrainPointsObservations);

        new ImportFromCsv<BStructuralTiltPoint>(BStructuralTiltPoint.class) {
        }.load(sourceDir, "structuralTiltPoints.csv", mStructuralTiltPoints);

        new ImportFromCsv<BStructuralTiltPointObservation>(BStructuralTiltPointObservation.class) {
        }.load(sourceDir, "structuralTiltPointsObservations.csv", mStructuralTiltPointsObservations);

        new ImportFromCsv<BTopoConvergenceGroup>(BTopoConvergenceGroup.class) {
        }.load(sourceDir, "topoControlPointsConvergence.csv", mTopoConvergenceGroups);

        //Hydro
        new ImportFromCsv<BHydroGroundwaterPoint>(BHydroGroundwaterPoint.class) {
        }.load(sourceDir, "hydroGroundwaterPoints.csv", mHydroGroundwaterPoints);

        new ImportFromCsv<BHydroGroundwaterPointObservation>(BHydroGroundwaterPointObservation.class) {
        }.load(sourceDir, "hydroGroundwaterPointsObservations.csv", mHydroGroundwaterPointsObservations);

        //TMO
        new ImportFromCsv<BGrundvatten>(BGrundvatten.class) {
        }.load(sourceDir, "tmoGrundvatten.csv", mTmo.getGrundvatten());

        new ImportFromCsv<BInfiltration>(BInfiltration.class) {
        }.load(sourceDir, "tmoInfiltration.csv", mTmo.getInfiltration());

        new ImportFromCsv<BRorelse>(BRorelse.class) {
        }.load(sourceDir, "tmoRorelse.csv", mTmo.getRorelse());

        new ImportFromCsv<BTunnelvatten>(BTunnelvatten.class) {
        }.load(sourceDir, "tmoTunnelvatten.csv", mTmo.getTunnelvatten());

//        new ImportFromCsv<BVattenkemi>(BVattenkemi.class) {
//        }.load(sourceDir, "tmoVattenkemi.csv", mTmo.getVattenkemi());
//
//        new ImportFromCsv<BVaderstation>(BVaderstation.class) {
//        }.load(sourceDir, "tmoVaderstation.csv", mTmo.getVaderstation());
        new ImportFromCsv<BGrundvattenObservation>(BGrundvattenObservation.class) {
        }.load(sourceDir, "tmoGrundvattenObservations.csv", mTmo.getGrundvattenObservations());

        new ImportFromCsv<BRorelseObservation>(BRorelseObservation.class) {
        }.load(sourceDir, "tmoRorelseObservations.csv", mTmo.getRorelseObservations());

        //Geotechnical
        new ImportFromCsv<BGeoExtensometer>(BGeoExtensometer.class) {
        }.load(sourceDir, "geoExtensometers.csv", mGeoExtensometers);

        new ImportFromCsv<BGeoExtensometerPoint>(BGeoExtensometerPoint.class) {
        }.load(sourceDir, "geoExtensometersPoints.csv", mGeoExtensometersPoints);

        new ImportFromCsv<BGeoExtensometerPointObservation>(BGeoExtensometerPointObservation.class) {
        }.load(sourceDir, "geoExtensometersPointsObservations.csv", mGeoExtensometersPointsObservations);

        new ImportFromCsv<BGeoInclinometerPoint>(BGeoInclinometerPoint.class) {
        }.load(sourceDir, "geoInclinometerPoints.csv", mGeoInclinometerPoints);

        new ImportFromCsv<BGeoInclinometerPointObservationPre>(BGeoInclinometerPointObservationPre.class) {
        }.load(sourceDir, "geoInclinometerPointsObservations.csv", mGeoInclinometerPointsObservationsPre);

        //System
        new ImportFromCsv<BSystemSearchProvider>(BSystemSearchProvider.class) {
        }.load(sourceDir, "systemSearchProviders.csv", mSystemSearchProviders);

    }

    void postLoad() {
        for (var a : mAlarms) {
            a.setButterfly(this);
            a.ext().populateRanges();
        }

        for (var p : mHydroGroundwaterPoints) {
            p.setButterfly(this);
            p.setDimension(BDimension._1d);
        }

        for (var p : mStructuralCrackPoints) {
            p.setButterfly(this);
        }

        for (var p : mStructuralStrainPoints) {
            p.setButterfly(this);
        }

        for (var p : mStructuralTiltPoints) {
            p.setButterfly(this);
        }

        for (var p : mTopoConvergenceGroups) {
            p.setButterfly(this);
        }
        for (var p : mTopoControlPoints) {
            p.setButterfly(this);
        }

        for (var p : tmo().mGrundvatten) {
            p.setButterfly(this);
        }

        for (var p : mGeoExtensometers) {
            p.setButterfly(this);
        }

        for (var p : mGeoInclinometerPoints) {
            p.setButterfly(this);
        }

        structural().postLoad();
        topo().postLoad();
        geotechnical().postLoad();
        populateMonmon();
    }

    private void populateMonmon() {
        var list = new ArrayList<BMonmon>();
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
                    var m = new BMonmon(p, Integer.parseInt(items[0]), belongsTo);
                    list.add(m);
                }
            } catch (NumberFormatException e) {
                System.err.println(e);
            }
        }

        mMonmons.clear();
        mMonmons.addAll(list);
    }

    public class Dev {

        public ArrayList<BCoordinate> getCoordinates() {
            return mCoordinates;
        }

    }

    public class Ext {

    }

    public class Geotechnical {

        public ArrayList<BGeoExtensometer> getExtensometers() {
            return mGeoExtensometers;
        }

        public ArrayList<BGeoExtensometerPoint> getExtensometersPoints() {
            return mGeoExtensometersPoints;
        }

        public ArrayList<BGeoExtensometerPointObservation> getExtensometersPointsObservations() {
            return mGeoExtensometersPointsObservations;
        }

        public ArrayList<BGeoInclinometerPoint> getInclinometerPoints() {
            return mGeoInclinometerPoints;
        }

        public ArrayList<BGeoInclinometerPointObservation> getInclinometerPointsObservations() {
            return mGeoInclinometerPointsObservations;
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
                            item.setA(o0.getA() / 1000.0);
                            item.setB(o0.getB() / 1000.0);
                            item.setDown(o0.getDown());
                            item.recalc();
                            o.getObservationItems().add(item);
                        } catch (NullPointerException e) {
                            //nvm
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

        public ArrayList<BHydroGroundwaterPoint> getGroundwaterPoints() {
            return mHydroGroundwaterPoints;
        }

        public ArrayList<BHydroGroundwaterPointObservation> getGroundwaterPointsObservations() {
            return mHydroGroundwaterPointsObservations;
        }

    }

    public class Noise {

        public ArrayList<BAcousticBlast> getBlasts() {
            return mBlasts;
        }

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

    public class Structural {

        private final HashMap<String, BStructuralCrackPoint> mNameToCrackPoint = new HashMap<>();
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

        public ArrayList<BSystemSearchProvider> getSearchProviders() {
            return mSystemSearchProviders;
        }
    }

    public class Tmo {

        private final ArrayList<BGrundvatten> mGrundvatten = new ArrayList<>();
        private final ArrayList<BGrundvattenObservation> mGrundvattenObservations = new ArrayList<>();
        private final ArrayList<BInfiltration> mInfiltration = new ArrayList<>();
        private final ArrayList<BRorelse> mRorelse = new ArrayList<>();
        private final ArrayList<BRorelseObservation> mRorelseObservations = new ArrayList<>();
        private final ArrayList<BTunnelvatten> mTunnelvatten = new ArrayList<>();
        private final ArrayList<BVaderstation> mVaderstation = new ArrayList<>();
        private final ArrayList<BVattenkemi> mVattenkemi = new ArrayList<>();

        public ArrayList<BGrundvatten> getGrundvatten() {
            return mGrundvatten;
        }

        public ArrayList<BGrundvattenObservation> getGrundvattenObservations() {
            return mGrundvattenObservations;
        }

        public ArrayList<BInfiltration> getInfiltration() {
            return mInfiltration;
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

        public ArrayList<BTopoConvergenceGroup> getConvergenceGroups() {
            return mTopoConvergenceGroups;
        }

        private void postLoad() {
            mNameToControlPoint.clear();
            getControlPoints().forEach(controlPoint -> {
                mNameToControlPoint.put(controlPoint.getName(), controlPoint);
            });
        }
    }
}
