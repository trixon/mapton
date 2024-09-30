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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Consumer;
import javax.swing.SortOrder;
import org.mapton.api.report.MReport;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.api.TopoManager;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.CollectionHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MReport.class)
public class ObjectSummaryGradeHReport extends BaseTopoMeasurementReport {

    private final TopoManager mManager = TopoManager.getInstance();
    private final String mName = "Objektsammanställning, differentialsättning";

    public ObjectSummaryGradeHReport() {
        setName(mName);
    }

    private void addBlankRow(ArrayList<ArrayList<String>> rows) {
        rows.add(new ArrayList<>());
    }

    private void addRow(ArrayList<ArrayList<String>> rows, Object... objects) {
        var columns = new ArrayList<String>();
        for (var object : objects) {
            if (object == null) {
                columns.add("");
                continue;
            }

            if (object instanceof Integer value) {
                columns.add(Integer.toString(value));
            } else {
                columns.add(object.toString());
            }
        }
        rows.add(columns);
    }

    @Override
    public ContainerTag getContent() {
        var sb = new StringBuilder();
        var rows = new ArrayList<ArrayList<String>>();
        var map = new HashMap<String, Integer>();
        var hFreqMap = new HashMap<String, Integer>();
        var pFreqMap = new HashMap<String, Integer>();
        var hAgeMap = new HashMap<String, Integer>();
        var pAgeMap = new HashMap<String, Integer>();
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
            Integer age = null;
            if (daysSinceMeasurement <= 7) {
                age = 1;
            } else if (daysSinceMeasurement <= 28) {
                age = 4;
            } else if (daysSinceMeasurement <= 182) {
                age = 26;
            } else if (daysSinceMeasurement <= 364) {
                age = 52;
            }

            var alarmLevel = p.ext().getAlarmLevel(p.ext().getObservationFilteredLast());
            if (p.getDimension() == BDimension._1d) {
                CollectionHelper.incInteger(map, "antalDubbar");
                CollectionHelper.incInteger(hFreqMap, "%d".formatted(p.getFrequency()));
                if (age != null) {
                    CollectionHelper.incInteger(hAgeMap, String.valueOf(age));
                }
                CollectionHelper.incInteger(map, "alarmLevelH%d".formatted(alarmLevel));
                if (delta != null) {
                    hDeltaMap.put(p, Math.abs(delta));
                }
            } else {
                CollectionHelper.incInteger(map, "antalPrismor");
                CollectionHelper.incInteger(pFreqMap, "%d".formatted(p.getFrequency()));
                if (age != null) {
                    CollectionHelper.incInteger(pAgeMap, String.valueOf(age));
                }
                CollectionHelper.incInteger(map, "alarmLevelP%d".formatted(alarmLevel));
                if (delta != null) {
                    pDeltaMap.put(p, Math.abs(delta));
                }
            }
        });

        Consumer<BTopoControlPoint> topListConsumer = p -> {
            var delta = p.getDimension() == BDimension._1d
                    ? p.ext().deltaZero().getDelta1(3)
                    : p.ext().deltaZero().getDelta(3);
            addRow(rows,
                    "",
                    p.getName(),
                    p.ext().getAlarmLevel(p.ext().getObservationFilteredLast()),
                    delta
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

        addRow(rows, "Ålder upp till");
        for (var val : List.of(1, 4, 26, 52)) {
            var s = String.valueOf(val);
            addRow(rows, "    " + s + (val == 1 ? " vecka" : " veckor"),
                    hAgeMap.get(s),
                    pAgeMap.get(s),
                    hAgeMap.getOrDefault(s, 0) + pAgeMap.getOrDefault(s, 0)
            );
        }
        addBlankRow(rows);

        addRow(rows, "Dagar mellan inmätningar");
        var freqSet = new TreeSet<String>(Comparator.comparingInt(s -> Integer.valueOf(s)));
        freqSet.addAll(hFreqMap.keySet());
        freqSet.addAll(pFreqMap.keySet());
        freqSet.forEach(s -> {
            addRow(rows, "    " + s,
                    hFreqMap.get(s),
                    pFreqMap.get(s),
                    hFreqMap.getOrDefault(s, 0) + pFreqMap.getOrDefault(s, 0)
            );
        });

        addBlankRow(rows);
        addBlankRow(rows);
        addBlankRow(rows);

        var topListSize = 10;
        addRow(rows, "Största rörelser", "Punkt", "Larmnivå", "Rörelse");
        addRow(rows, "Dubbar");
        getTopList(hDeltaMap, topListSize).forEach(topListConsumer);
        addBlankRow(rows);
        addRow(rows, "Prismor");
        getTopList(pDeltaMap, topListSize).forEach(topListConsumer);

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

    private List<BTopoControlPoint> getTopList(LinkedHashMap<BTopoControlPoint, Double> map, int maxSize) {
        return CollectionHelper.sortByValue(map, SortOrder.DESCENDING)
                .entrySet()
                .stream()
                .limit(maxSize)
                .map(entry -> entry.getKey())
                .toList();
    }
}
