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
import java.util.Iterator;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_format.io.ImportFromCsv;
import org.mapton.butterfly_format.types.BAlarm;
import org.mapton.butterfly_format.types.BAreaActivity;
import org.mapton.butterfly_format.types.BAreaBase;
import org.mapton.butterfly_format.types.acoustic.BAcousticMeasuringPoint;
import org.mapton.butterfly_format.types.acoustic.BBlast;
import org.mapton.butterfly_format.types.hydro.BGroundwaterObservation;
import org.mapton.butterfly_format.types.hydro.BGroundwaterPoint;
import org.mapton.butterfly_format.types.monmon.BMonmon;
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

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class Butterfly {

    private final Acoustic mAcoustic = new Acoustic();
    private final ArrayList<BAlarm> mAlarms = new ArrayList<>();
    private final ArrayList<BAreaActivity> mAreaActivities = new ArrayList<>();
    private final ArrayList<BAreaBase> mAreaFilters = new ArrayList<>();
    private final ArrayList<BAcousticMeasuringPoint> mMeasuringPoints = new ArrayList<>();
    private final ArrayList<BBlast> mBlasts = new ArrayList<>();
    private final Hydro mHydro = new Hydro();
    private final ArrayList<BGroundwaterObservation> mHydroGroundwaterObservations = new ArrayList<>();
    private final ArrayList<BGroundwaterPoint> mHydroGroundwaterPoints = new ArrayList<>();
    private final ArrayList<BMonmon> mMonmons = new ArrayList<>();
    private final Tmo mTmo = new Tmo();
    private final Topo mTopo = new Topo();
    private final ArrayList<BTopoControlPoint> mTopoControlPoints = new ArrayList<>();
    private final ArrayList<BTopoControlPointObservation> mTopoControlPointsObservations = new ArrayList<>();

    public static void main(String[] args) {
        ButterflyLoader.main(args);
    }

    public Butterfly() {
    }

    public Acoustic acoustic() {
        return mAcoustic;
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

    public Tmo tmo() {
        return mTmo;
    }

    public Topo topo() {
        return mTopo;
    }

    void load(File sourceDir) {
        new ImportFromCsv<BBlast>(BBlast.class) {
        }.load(new File(sourceDir, "acousticBlasts.csv"), mBlasts);

        new ImportFromCsv<BAcousticMeasuringPoint>(BAcousticMeasuringPoint.class) {
        }.load(new File(sourceDir, "acousticMeasuringPoints.csv"), mMeasuringPoints);

        new ImportFromCsv<BAlarm>(BAlarm.class) {
        }.load(new File(sourceDir, "alarms.csv"), mAlarms);

        new ImportFromCsv<BAreaActivity>(BAreaActivity.class) {
        }.load(new File(sourceDir, "areaActivities.csv"), mAreaActivities);

        new ImportFromCsv<BAreaBase>(BAreaBase.class) {
        }.load(new File(sourceDir, "areaFilters.csv"), mAreaFilters);

        new ImportFromCsv<BTopoControlPoint>(BTopoControlPoint.class) {
        }.load(new File(sourceDir, "topoControlPoints.csv"), mTopoControlPoints);

        new ImportFromCsv<BTopoControlPointObservation>(BTopoControlPointObservation.class) {
        }.load(new File(sourceDir, "topoControlPointsObservations.csv"), mTopoControlPointsObservations);

        new ImportFromCsv<BGroundwaterPoint>(BGroundwaterPoint.class) {
        }.load(new File(sourceDir, "hydroGroundwaterPoints.csv"), mHydroGroundwaterPoints);

        new ImportFromCsv<BGroundwaterObservation>(BGroundwaterObservation.class) {
        }.load(new File(sourceDir, "hydroGroundwaterObservations.csv"), mHydroGroundwaterObservations);
    }

    void loadTmoObjekt(File sourceDir) {
        new ImportFromCsv<BGrundvatten>(BGrundvatten.class) {
        }.load(new File(sourceDir, "tmoGrundvatten.csv"), mTmo.getGrundvatten());

        new ImportFromCsv<BInfiltration>(BInfiltration.class) {
        }.load(new File(sourceDir, "tmoInfiltration.csv"), mTmo.getInfiltration());

        new ImportFromCsv<BRorelse>(BRorelse.class) {
        }.load(new File(sourceDir, "tmoRorelse.csv"), mTmo.getRorelse());

        new ImportFromCsv<BTunnelvatten>(BTunnelvatten.class) {
        }.load(new File(sourceDir, "tmoTunnelvatten.csv"), mTmo.getTunnelvatten());

        new ImportFromCsv<BVattenkemi>(BVattenkemi.class) {
        }.load(new File(sourceDir, "tmoVattenkemi.csv"), mTmo.getVattenkemi());

        new ImportFromCsv<BVaderstation>(BVaderstation.class) {
        }.load(new File(sourceDir, "tmoVaderstation.csv"), mTmo.getVaderstation());
    }

    void loadTmoObservations(File sourceDir) {
        new ImportFromCsv<BGrundvattenObservation>(BGrundvattenObservation.class) {
        }.load(new File(sourceDir, "tmoGrundvattenObservations.csv"), mTmo.getGrundvattenObservations());

        new ImportFromCsv<BRorelseObservation>(BRorelseObservation.class) {
        }.load(new File(sourceDir, "tmoRorelseObservations.csv"), mTmo.getRorelseObservations());
    }

    void postLoad(File sourceDir) {
        for (var a : mAlarms) {
            a.setButterfly(this);
            a.ext().populateRanges();
        }

        for (var p : mTopoControlPoints) {
            p.setButterfly(this);
            p.ext().setDateLatest(p.getDateLatest());
        }

        for (var p : tmo().mGrundvatten) {
            p.setButterfly(this);
        }

        populateMonmon(sourceDir);
    }

    private void populateMonmon(File sourceDir) {
        var map = new HashMap<String, BTopoControlPoint>();
        topo().getControlPoints().forEach(controlPoint -> {
            map.put(controlPoint.getName(), controlPoint);
        });

        var list = new ArrayList<BMonmon>();
        var config = MonmonConfig.getInstance().getConfig();
        for (Iterator<String> iterator = config.getKeys(); iterator.hasNext();) {
            try {
                String name = iterator.next();
                var p = map.get(name);
                if (p != null) {
                    var items = StringUtils.split(config.getString(name), ",");
                    var belongsTo = "";
                    if (items.length > 1) {
                        belongsTo = items[1];
                    }
                    var m = new BMonmon(p, Integer.parseInt(items[0]), belongsTo);
                    list.add(m);
                }
            } catch (Exception e) {
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

    public class Hydro {

        public ArrayList<BGroundwaterPoint> getGroundwaterPoints() {
            return mHydroGroundwaterPoints;
        }

        public ArrayList<BGroundwaterObservation> getGroundwaterObservations() {
            return mHydroGroundwaterObservations;
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

        public ArrayList<BTopoControlPoint> getControlPoints() {
            return mTopoControlPoints;
        }

        public ArrayList<BTopoControlPointObservation> getControlPointsObservations() {
            return mTopoControlPointsObservations;
        }

    }
}
