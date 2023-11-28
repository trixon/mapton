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
import java.util.ArrayList;
import org.mapton.butterfly_format.io.ImportFromCsv;
import org.mapton.butterfly_format.types.BAlarm;
import org.mapton.butterfly_format.types.BAreaActivity;
import org.mapton.butterfly_format.types.BAreaBase;
import org.mapton.butterfly_format.types.acoustic.BBlast;
import org.mapton.butterfly_format.types.hydro.BGroundwaterObservation;
import org.mapton.butterfly_format.types.hydro.BGroundwaterPoint;
import org.mapton.butterfly_format.types.tmo.BGrundvatten;
import org.mapton.butterfly_format.types.tmo.BInfiltration;
import org.mapton.butterfly_format.types.tmo.BRorelse;
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
    private final ArrayList<BBlast> mBlasts = new ArrayList<>();
    private final Hydro mHydro = new Hydro();
    private final ArrayList<BGroundwaterObservation> mHydroGroundwaterObservations = new ArrayList<>();
    private final ArrayList<BGroundwaterPoint> mHydroGroundwaterPoints = new ArrayList<>();
    private final Tmo mTmo = new Tmo();
    private final Topo mTopo = new Topo();
    private final ArrayList<BTopoControlPoint> mTopoControlPoints = new ArrayList<>();
    private final ArrayList<BTopoControlPointObservation> mTopoControlPointsObservations = new ArrayList<>();

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

    public Hydro hydro() {
        return mHydro;
    }

    public void load(File sourceDir) {
        new ImportFromCsv<BBlast>(BBlast.class) {
        }.load(new File(sourceDir, "acousticBlasts.csv"), mBlasts);

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

    public void loadTmoMatobjekt(File sourceDir) {
        new ImportFromCsv<BGrundvatten>(BGrundvatten.class) {
        }.load(new File(sourceDir, "tmoMatobjektGrundvatten.csv"), mTmo.getGrundvatten());

        new ImportFromCsv<BInfiltration>(BInfiltration.class) {
        }.load(new File(sourceDir, "tmoMatobjektInfiltration.csv"), mTmo.getInfiltration());

        new ImportFromCsv<BRorelse>(BRorelse.class) {
        }.load(new File(sourceDir, "tmoMatobjektRorelse.csv"), mTmo.getRörelse());

        new ImportFromCsv<BTunnelvatten>(BTunnelvatten.class) {
        }.load(new File(sourceDir, "tmoMatobjektTunnelvatten.csv"), mTmo.getTunnelvatten());

        new ImportFromCsv<BVattenkemi>(BVattenkemi.class) {
        }.load(new File(sourceDir, "tmoMatobjektVattenkemi.csv"), mTmo.getVattenkemi());

        new ImportFromCsv<BVaderstation>(BVaderstation.class) {
        }.load(new File(sourceDir, "tmoMatobjektVaderstation.csv"), mTmo.getVaderstation());
    }

    public void postLoad() {
        for (var a : mAlarms) {
            a.setButterfly(this);
            a.ext().populateRanges();
        }

        for (var p : mTopoControlPoints) {
            p.setButterfly(this);
        }
    }

    public Tmo tmo() {
        return mTmo;
    }

    public Topo topo() {
        return mTopo;
    }

    public class Acoustic {

        public ArrayList<BBlast> getBlasts() {
            return mBlasts;
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
        private final ArrayList<BInfiltration> mInfiltration = new ArrayList<>();
        private final ArrayList<BRorelse> mRörelse = new ArrayList<>();
        private final ArrayList<BTunnelvatten> mTunnelvatten = new ArrayList<>();
        private final ArrayList<BVaderstation> mVaderstation = new ArrayList<>();
        private final ArrayList<BVattenkemi> mVattenkemi = new ArrayList<>();

        public ArrayList<BGrundvatten> getGrundvatten() {
            return mGrundvatten;
        }

        public ArrayList<BInfiltration> getInfiltration() {
            return mInfiltration;
        }

        public ArrayList<BRorelse> getRörelse() {
            return mRörelse;
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
