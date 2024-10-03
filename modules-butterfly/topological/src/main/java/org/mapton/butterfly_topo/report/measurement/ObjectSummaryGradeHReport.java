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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Consumer;
import javafx.collections.ObservableList;
import org.mapton.api.report.MReport;
import org.mapton.butterfly_format.types.topo.BTopoGrade;
import org.mapton.butterfly_topo.api.TopoManager;
import org.mapton.butterfly_topo.grade.horizontal.GradeHManager;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MReport.class)
public class ObjectSummaryGradeHReport extends BaseTopoMeasurementReport {

    private final GradeHManager mManager = GradeHManager.getInstance();
    private final String mName = "Objektsammanställning, differentialsättning";
    private final TopoManager mTopoManager = TopoManager.getInstance();

    public ObjectSummaryGradeHReport() {
        setName(mName);
    }

    @Override
    public ContainerTag getContent() {
        var sb = new StringBuilder();
        var rows = new ArrayList<ArrayList<String>>();
        var map = new HashMap<String, Integer>();
        var dates = new TreeSet<LocalDate>();

        mTopoManager.getTimeFilteredItems().stream().forEachOrdered(p -> {
            try {
                dates.add(p.ext().getObservationFilteredFirstDate());
                dates.add(p.ext().getObservationFilteredLastDate());
            } catch (Exception e) {
                //nvm
            }
//            var delta = p.ext().deltaZero().getDelta();
//            var daysSinceMeasurement = p.ext().getMeasurementAge(ChronoUnit.DAYS);
//            Integer age = null;
//            if (daysSinceMeasurement <= 7) {
//                age = 1;
//            } else if (daysSinceMeasurement <= 28) {
//                age = 4;
//            } else if (daysSinceMeasurement <= 182) {
//                age = 26;
//            } else if (daysSinceMeasurement <= 364) {
//                age = 52;
//            }
//
//            var alarmLevel = p.ext().getAlarmLevel(p.ext().getObservationFilteredLast());
//            if (p.getDimension() == BDimension._1d) {
//                CollectionHelper.incInteger(map, "antalDubbar");
//                CollectionHelper.incInteger(hFreqMap, "%d".formatted(p.getFrequency()));
//                if (age != null) {
//                    CollectionHelper.incInteger(hAgeMap, String.valueOf(age));
//                }
//                CollectionHelper.incInteger(map, "alarmLevelH%d".formatted(alarmLevel));
//                if (delta != null) {
//                    hDeltaMap.put(p, Math.abs(delta));
//                }
//            } else {
//                CollectionHelper.incInteger(map, "antalPrismor");
//                CollectionHelper.incInteger(pFreqMap, "%d".formatted(p.getFrequency()));
//                if (age != null) {
//                    CollectionHelper.incInteger(pAgeMap, String.valueOf(age));
//                }
//                CollectionHelper.incInteger(map, "alarmLevelP%d".formatted(alarmLevel));
//                if (delta != null) {
//                    pDeltaMap.put(p, Math.abs(delta));
//                }
//            }
        });

        Consumer<BTopoGrade> topListConsumer = p -> {
            addRow(rows,
                    p.getName(),
                    MathHelper.convertDoubleToString(p.ext().getDiff().getZPerMille(), 1),
                    "%.1f".formatted(p.ext().getDiff().getPartialDiffZ() * 1000),
                    MathHelper.convertDoubleToString(p.getDistancePlane(), 2),
                    p.getPeriod(),
                    p.ext().getNumOfCommonObservations()
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
        addBlankRow(rows);
        var topListSize = 25;
        addRow(rows, "Alla punktpar", mManager.getAllItems().size());
        addRow(rows, "Filtrerade punktpar", mManager.getTimeFilteredItems().size());
        addRow(rows, "Max antal punktpar", topListSize);
        addBlankRow(rows);

        addRow(rows, "Punktpar", "mm/m", "Diff", "Planavstånd", "Period", "Mätningar");
        getTopList(mManager.getTimeFilteredItems(), topListSize).forEach(topListConsumer);
        addBlankRow(rows);

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

    private List<BTopoGrade> getTopList(ObservableList<BTopoGrade> items, int limit) {
        return items.stream()
                .limit(limit)
                .toList();
    }
}
