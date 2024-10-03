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
package org.mapton.butterfly_topo.report.measurement;

import static j2html.TagCreator.body;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.hr;
import static j2html.TagCreator.html;
import static j2html.TagCreator.pre;
import j2html.tags.ContainerTag;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Consumer;
import org.mapton.api.report.MReport;
import org.mapton.butterfly_activities.api.ActManager;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.TopoHelper;
import org.mapton.butterfly_topo.api.TopoManager;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.CollectionHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MReport.class)
public class AlarmProgressReport extends BaseTopoMeasurementReport {

    private final TopoManager mManager = TopoManager.getInstance();
    private final String mName = "Larmförbrukning";

    public AlarmProgressReport() {
        setName(mName);
    }

    @Override
    public ContainerTag getContent() {
        var sb = new StringBuilder();
        var rows = new ArrayList<ArrayList<String>>();
        var map = new HashMap<String, Integer>();
        var hDeltaMap = new LinkedHashMap<BTopoControlPoint, Double>();
        var pDeltaMap = new LinkedHashMap<BTopoControlPoint, Double>();
        var dates = new TreeSet<LocalDate>();
        mManager.getTimeFilteredItems().stream().forEachOrdered(p -> {
            try {
                dates.add(p.ext().getObservationFilteredFirstDate());
                dates.add(p.ext().getObservationFilteredLastDate());
            } catch (Exception e) {
                //nvm
            }
            var delta = p.ext().deltaZero().getDelta();
            var daysSinceMeasurement = p.ext().getMeasurementAge(ChronoUnit.DAYS);

            var alarmLevel = p.ext().getAlarmLevel(p.ext().getObservationFilteredLast());
            if (p.getDimension() == BDimension._1d) {
                CollectionHelper.incInteger(map, "antalDubbar");
                CollectionHelper.incInteger(map, "alarmLevelH%d".formatted(alarmLevel));
                if (delta != null) {
                    hDeltaMap.put(p, Math.abs(delta));
                }
            } else {
                CollectionHelper.incInteger(map, "antalPrismor");
                CollectionHelper.incInteger(map, "alarmLevelP%d".formatted(alarmLevel));
                if (delta != null) {
                    pDeltaMap.put(p, Math.abs(delta));
                }
            }
        });

        Consumer<BTopoControlPoint> listConsumer = p -> {
            var am = ActManager.getInstance();

            var distance = "%.0f".formatted(am.distanceToClosest(p.getZeroX(), p.getZeroY()));
            var hpercent = p.ext().getAlarmPercent(BComponent.HEIGHT);
            var hps = "";
            if (hpercent != null && hpercent != -1) {
                hps = "%d%%".formatted(hpercent);
            }

            var ppercent = p.ext().getAlarmPercent(BComponent.PLANE);
            var pps = "";
            if (ppercent != null && ppercent != -1) {
                pps = "%d%%".formatted(ppercent);
            }
            var catH = "";
            var catP = "";

            if (p.getDimension() != BDimension._2d) {
                if (hpercent != null) {
                    if (hpercent < 40) {
                        catH = "A";
                    } else if (hpercent < 80) {
                        catH = "B";
                    } else {
                        catH = "C";
                    }
                }
            }

            if (p.getDimension() != BDimension._1d) {
                if (ppercent != null) {
                    if (ppercent < 40) {
                        catP = "A";
                    } else if (ppercent < 80) {
                        catP = "B";
                    } else {
                        catP = "C";
                    }
                }
            }
            var levelH = TopoHelper.getAlarmLevelHeight(p) > -1 ? TopoHelper.getAlarmLevelHeight(p) : "";
            var levelP = TopoHelper.getAlarmLevelPlane(p) > -1 ? TopoHelper.getAlarmLevelPlane(p) : "";

            addRow(rows,
                    p.getName(),
                    p.getDateZero().toString(),
                    p.ext().getObservationFilteredLastDate().toString(),
                    p.getDateZero().until(p.ext().getObservationFilteredLastDate(), ChronoUnit.DAYS),
                    distance,
                    //                    -1,
                    hps,
                    catH,
                    levelH,
                    pps,
                    catP,
                    levelP
            );
        };

        addRow(rows, "Grupp/objekt");
        addRow(rows, "Rapportdatum", LocalDate.now().toString());
        if (!dates.isEmpty()) {
            addRow(rows, "Senaste mätning", dates.getLast().toString());
            addRow(rows, "Första mätning", dates.getFirst().toString());
        }

        addBlankRow(rows);
        addRow(rows, "", "Dubbar", "Prismor", "Totalt");
        addRow(rows, "Antal punkter",
                map.get("antalDubbar"),
                map.get("antalPrismor"),
                map.getOrDefault("antalDubbar", 0) + map.getOrDefault("antalPrismor", 0)
        );
        addBlankRow(rows);
        addRow(rows, "På larmnivå");
        for (var val : List.of(0, 1, 2)) {
            var s = String.valueOf(val);
            var hKey = "alarmLevelH%d".formatted(val);
            var pKey = "alarmLevelP%d".formatted(val);
            addRow(rows, "    " + s,
                    map.get(hKey),
                    map.get(pKey),
                    map.getOrDefault(hKey, 0) + map.getOrDefault(pKey, 0)
            );
        }
        addBlankRow(rows);

        addBlankRow(rows);
        addBlankRow(rows);
        addBlankRow(rows);

        var header = new Object[]{
            "Punkt",
            "Noll",
            "Senaste",
            "Dagar",
            "Avstånd",
            "H%",
            "H Klass",
            "H Larm",
            "P%",
            "P Klass",
            "P Larm"
        };
        addRow(rows, "1D");
        addRow(rows, header);

        mManager.getTimeFilteredItems()
                .stream().filter(p -> p.getDimension() == BDimension._1d).forEachOrdered(listConsumer);
        addBlankRow(rows);

        addRow(rows, "3D");
        addRow(rows, header);

        mManager.getTimeFilteredItems()
                .stream().filter(p -> p.getDimension() == BDimension._3d).forEachOrdered(listConsumer);

        for (var columns : rows) {
            sb.append(String.join("\t", columns)).append("\n");
        }

        var html = html(
                body(
                        h1(mName),
                        hr(),
                        pre(sb.toString())
                )
        );

        return html;
    }
}
