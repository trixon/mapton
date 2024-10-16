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
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_format.io.ImportFromCsv;
import org.mapton.butterfly_format.types.BAlarm;
import org.mapton.butterfly_format.types.BAreaActivity;
import org.mapton.butterfly_format.types.BAreaBase;
import org.mapton.butterfly_format.types.acoustic.BAcousticMeasuringPoint;
import org.mapton.butterfly_format.types.acoustic.BBlast;
import org.mapton.butterfly_format.types.geo.BGeoExtensometer;
import org.mapton.butterfly_format.types.geo.BGeoExtensometerPoint;
import org.mapton.butterfly_format.types.geo.BGeoExtensometerPointObservation;
import org.mapton.butterfly_format.types.hydro.BGroundwaterObservation;
import org.mapton.butterfly_format.types.hydro.BGroundwaterPoint;
import org.mapton.butterfly_format.types.monmon.BMonmon;
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

    private final Acoustic mAcoustic = new Acoustic();
    private final ArrayList<BAlarm> mAlarms = new ArrayList<>();
    private final ArrayList<BAreaActivity> mAreaActivities = new ArrayList<>();
    private final ArrayList<BAreaBase> mAreaFilters = new ArrayList<>();
    private final ArrayList<BBlast> mBlasts = new ArrayList<>();
    private final ArrayList<BGeoExtensometer> mGeoExtensometers = new ArrayList<>();
    private final ArrayList<BGeoExtensometerPoint> mGeoExtensometersPoints = new ArrayList<>();
    private final ArrayList<BGeoExtensometerPointObservation> mGeoExtensometersPointsObservations = new ArrayList<>();
    private final Geotechnical mGeotechnical = new Geotechnical();
    private final Hydro mHydro = new Hydro();
    private final ArrayList<BGroundwaterObservation> mHydroGroundwaterObservations = new ArrayList<>();
    private final ArrayList<BGroundwaterPoint> mHydroGroundwaterPoints = new ArrayList<>();
    private final ArrayList<BAcousticMeasuringPoint> mMeasuringPoints = new ArrayList<>();
    private final ArrayList<BMonmon> mMonmons = new ArrayList<>();
    private final Structural mStructural = new Structural();
    private final ArrayList<BStructuralStrainGaugePoint> mStructuralStrainPoints = new ArrayList<>();
    private final ArrayList<BStructuralStrainGaugePointObservation> mStructuralStrainPointsObservations = new ArrayList<>();
    private final ArrayList<BStructuralTiltPoint> mStructuralTiltPoints = new ArrayList<>();
    private final ArrayList<BStructuralTiltPointObservation> mStructuralTiltPointsObservations = new ArrayList<>();
    private final Tmo mTmo = new Tmo();
    private final Topo mTopo = new Topo();
    private final ArrayList<BTopoControlPoint> mTopoControlPoints = new ArrayList<>();
    private final ArrayList<BTopoControlPointObservation> mTopoControlPointsObservations = new ArrayList<>();
    private final ArrayList<BTopoConvergenceGroup> mTopoConvergenceGroups = new ArrayList<>();

    public static void main(String[] args) {
    }

    public Butterfly() {
    }

    public Acoustic acoustic() {
        return mAcoustic;
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

    public Structural structural() {
        return mStructural;
    }

    public Tmo tmo() {
        return mTmo;
    }

    public Topo topo() {
        return mTopo;
    }

    void load(File sourceDir) {
        new ImportFromCsv<BBlast>(BBlast.class) {
        }.load(sourceDir, "acousticBlasts.csv", mBlasts);

        new ImportFromCsv<BAcousticMeasuringPoint>(BAcousticMeasuringPoint.class) {
        }.load(sourceDir, "acousticMeasuringPoints.csv", mMeasuringPoints);

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
        new ImportFromCsv<BGroundwaterPoint>(BGroundwaterPoint.class) {
        }.load(sourceDir, "hydroGroundwaterPoints.csv", mHydroGroundwaterPoints);

        new ImportFromCsv<BGroundwaterObservation>(BGroundwaterObservation.class) {
        }.load(sourceDir, "hydroGroundwaterObservations.csv", mHydroGroundwaterObservations);

        //TMO
        new ImportFromCsv<BGrundvatten>(BGrundvatten.class) {
        }.load(sourceDir, "tmoGrundvatten.csv", mTmo.getGrundvatten());

        new ImportFromCsv<BInfiltration>(BInfiltration.class) {
        }.load(sourceDir, "tmoInfiltration.csv", mTmo.getInfiltration());

        new ImportFromCsv<BRorelse>(BRorelse.class) {
        }.load(sourceDir, "tmoRorelse.csv", mTmo.getRorelse());

        new ImportFromCsv<BTunnelvatten>(BTunnelvatten.class) {
        }.load(sourceDir, "tmoTunnelvatten.csv", mTmo.getTunnelvatten());

        new ImportFromCsv<BVattenkemi>(BVattenkemi.class) {
        }.load(sourceDir, "tmoVattenkemi.csv", mTmo.getVattenkemi());

        new ImportFromCsv<BVaderstation>(BVaderstation.class) {
        }.load(sourceDir, "tmoVaderstation.csv", mTmo.getVaderstation());

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
    }

    void postLoad() {
        for (var a : mAlarms) {
            a.setButterfly(this);
            a.ext().populateRanges();
        }

        for (var p : mStructuralStrainPoints) {
            p.setButterfly(this);
        }

        for (var p : mStructuralTiltPoints) {
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

        structural().postLoad();
        topo().postLoad();
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

    public class Acoustic {

        public ArrayList<BBlast> getBlasts() {
            return mBlasts;
        }

        public ArrayList<BAcousticMeasuringPoint> getMeasuringPoints() {
            return mMeasuringPoints;
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

    }

    public class Hydro {

        public ArrayList<BGroundwaterPoint> getGroundwaterPoints() {
            return mHydroGroundwaterPoints;
        }

        public ArrayList<BGroundwaterObservation> getGroundwaterObservations() {
            return mHydroGroundwaterObservations;
        }

    }

    public class Structural {

        private final HashMap<String, BStructuralStrainGaugePoint> mNameToStrainPoint = new HashMap<>();
        private final HashMap<String, BStructuralTiltPoint> mNameToTiltPoint = new HashMap<>();

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
