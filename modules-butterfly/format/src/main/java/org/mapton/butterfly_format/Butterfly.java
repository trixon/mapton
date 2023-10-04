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
import java.util.LinkedHashMap;
import java.util.TreeSet;
import org.mapton.butterfly_format.io.ImportFromCsv;
import org.mapton.butterfly_format.types.BAlarm;
import org.mapton.butterfly_format.types.controlpoint.BHydroControlPoint;
import org.mapton.butterfly_format.types.controlpoint.BHydroControlPointObservation;
import org.mapton.butterfly_format.types.controlpoint.BTopoControlPoint;
import org.mapton.butterfly_format.types.controlpoint.BTopoControlPointObservation;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class Butterfly {

    private final ArrayList<BAlarm> mAlarms = new ArrayList<>();
    private final ArrayList<BHydroControlPoint> mHydroControlPoints = new ArrayList<>();
    private final ArrayList<BHydroControlPointObservation> mHydroControlPointsObservations = new ArrayList<>();
    private final LinkedHashMap<String, ArrayList<BTopoControlPointObservation>> mNameToTopoControlPointObservations = new LinkedHashMap<>();
    private final LinkedHashMap<String, BTopoControlPoint> mNameToTopoControlPoints = new LinkedHashMap<>();
    private final TreeSet<String> mTopoControlPointNames = new TreeSet<>();
    private final ArrayList<BTopoControlPoint> mTopoControlPoints = new ArrayList<>();
    private final ArrayList<BTopoControlPointObservation> mTopoControlPointsObservations = new ArrayList<>();

    public Butterfly() {
    }

    public ArrayList<BAlarm> getAlarms() {
        return mAlarms;
    }

    public ArrayList<BHydroControlPoint> getHydroControlPoints() {
        return mHydroControlPoints;
    }

    public ArrayList<BHydroControlPointObservation> getHydroControlPointsObservations() {
        return mHydroControlPointsObservations;
    }

    public LinkedHashMap<String, ArrayList<BTopoControlPointObservation>> getNameToTopoControlPointObservations() {
        return mNameToTopoControlPointObservations;
    }

    public LinkedHashMap<String, BTopoControlPoint> getNameToTopoControlPoints() {
        return mNameToTopoControlPoints;
    }

    public BTopoControlPoint getTopoControlPoint(String name) {
        return mNameToTopoControlPoints.get(name);
    }

    public TreeSet<String> getTopoControlPointNames() {
        return mTopoControlPointNames;
    }

    public ArrayList<BTopoControlPointObservation> getTopoControlPointObservations(String name) {
        return mNameToTopoControlPointObservations.computeIfAbsent(name, k -> new ArrayList<>());
    }

    public ArrayList<BTopoControlPoint> getTopoControlPoints() {
        return mTopoControlPoints;
    }

    public ArrayList<BTopoControlPointObservation> getTopoControlPointsObservations() {
        return mTopoControlPointsObservations;
    }

    public void load(File sourceDir) {
        new ImportFromCsv<BAlarm>(BAlarm.class) {
        }.load(new File(sourceDir, "alarms.csv"), mAlarms);

        new ImportFromCsv<BTopoControlPoint>(BTopoControlPoint.class) {
        }.load(new File(sourceDir, "topoControlPoints.csv"), mTopoControlPoints);

        new ImportFromCsv<BTopoControlPointObservation>(BTopoControlPointObservation.class) {
        }.load(new File(sourceDir, "topoControlPointsObservations.csv"), mTopoControlPointsObservations);

        new ImportFromCsv<BHydroControlPoint>(BHydroControlPoint.class) {
        }.load(new File(sourceDir, "hydroControlPoints.csv"), mHydroControlPoints);

        new ImportFromCsv<BHydroControlPointObservation>(BHydroControlPointObservation.class) {
        }.load(new File(sourceDir, "hydroControlPointsObservations.csv"), mHydroControlPointsObservations);

        populateMaps();
    }

    private void populateMaps() {
        for (var o : mTopoControlPointsObservations) {
            mNameToTopoControlPointObservations.computeIfAbsent(o.getName(), k -> new ArrayList<>()).add(o);
        }

        for (var p : mTopoControlPoints) {
            mTopoControlPointNames.add(p.getName());
            mNameToTopoControlPoints.put(p.getName(), p);
            p.ext().setObservationsRaw(mNameToTopoControlPointObservations.get(p.getName()));
        }
    }

}
