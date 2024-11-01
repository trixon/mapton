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
import java.util.List;
import java.util.TreeSet;
import java.util.function.Consumer;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.math3.util.FastMath;
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

    private final ActManager mActivityManager = ActManager.getInstance();
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
        var dates = new TreeSet<LocalDate>();
        mManager.getTimeFilteredItems().stream().forEachOrdered(p -> {
            try {
                dates.add(p.ext().getObservationFilteredFirstDate());
                dates.add(p.ext().getObservationFilteredLastDate());
            } catch (Exception e) {
                //nvm
            }

            var alarmLevel = p.ext().getAlarmLevel(p.ext().getObservationFilteredLast());
            if (p.getDimension() == BDimension._1d) {
                CollectionHelper.incInteger(map, "antalDubbar");
                CollectionHelper.incInteger(map, "alarmLevelH%d".formatted(alarmLevel));
            } else {
                CollectionHelper.incInteger(map, "antalPrismor");
                CollectionHelper.incInteger(map, "alarmLevelP%d".formatted(alarmLevel));
            }
        });

        Consumer<BTopoControlPoint> listConsumer = p -> {
            var distance = "%.0f".formatted(mActivityManager.distanceToClosest(p.getZeroX(), p.getZeroY()));

            var percentH = p.ext().getAlarmPercent(BComponent.HEIGHT);
            var percentStringH = getPercentString(percentH);
            var percentClassH = getPercentClass(percentH);
            var levelH = TopoHelper.getAlarmLevelHeight(p);

            var percentP = p.ext().getAlarmPercent(BComponent.PLANE);
            var percentStringP = getPercentString(percentP);
            var percentClassP = getPercentClass(percentP);
            var levelP = TopoHelper.getAlarmLevelPlane(p);

            if (p.getDimension() == BDimension._1d) {
                addRow(rows,
                        p.getName(),
                        p.getGroup(),
                        percentStringH,
                        percentClassH,
                        getAlarmLevel(levelH),
                        p.getDateZero().toString(),
                        p.ext().getObservationFilteredLastDate().toString(),
                        p.getDateZero().until(p.ext().getObservationFilteredLastDate(), ChronoUnit.DAYS),
                        distance
                );
            } else {
                var percentString = "";
                var percentClass = "";
                var level = -1;
                if (percentP != null && percentH != null) {
                    if (percentP > percentH) {
                        percentString = percentStringP;
                        percentClass = percentClassP;
                        level = levelP;
                    } else {
                        percentString = percentStringH;
                        percentClass = percentClassH;
                        level = levelH;
                    }
                } else if (percentH != null) {
                    percentString = percentStringH;
                    percentClass = percentClassH;
                    level = levelH;
                } else if (percentP != null) {
                    percentString = percentStringP;
                    percentClass = percentClassP;
                    level = levelP;
                }

                addRow(rows,
                        p.getName(),
                        p.getGroup(),
                        percentString,
                        percentClass,
                        getAlarmLevel(level),
                        p.getDateZero().toString(),
                        p.ext().getObservationFilteredLastDate().toString(),
                        p.getDateZero().until(p.ext().getObservationFilteredLastDate(), ChronoUnit.DAYS),
                        distance,
                        percentStringH,
                        percentClassH,
                        getAlarmLevel(levelH),
                        percentStringP,
                        percentClassP,
                        getAlarmLevel(levelP)
                );
            }
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
            var s = getAlarmLevel(val);
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

        addRow(rows, "1D");
        addRow(rows,
                "Punkt",
                "Grupp",
                "Förbrukning",
                "Klass",
                "Larm",
                "Noll",
                "Senaste",
                "Dagar",
                "Avstånd"
        );

        Comparator<BTopoControlPoint> comparator = new Comparator<BTopoControlPoint>() {
            @Override
            public int compare(BTopoControlPoint p1, BTopoControlPoint p2) {
                var pp1 = getPercent(p1);
                var pp2 = getPercent(p2);
                if (pp1 == null && pp2 == null) {
                    return -1;
                }
                if (pp1 == null) {
                    return +1;
                } else if (pp2 == null) {
                    return -1;
                } else {
                    return pp2.compareTo(pp1);
                }
            }
        }.thenComparing(Comparator.comparing(BTopoControlPoint::getName));

        mManager.getTimeFilteredItems().stream()
                .filter(p -> p.getDimension() == BDimension._1d)
                .sorted(comparator)
                .forEachOrdered(listConsumer);
        addBlankRow(rows);

        addRow(rows, "3D");
        addRow(rows,
                "Punkt",
                "Grupp",
                "Förbrukning",
                "Klass",
                "Larm",
                "Noll",
                "Senaste",
                "Dagar",
                "Avstånd",
                "H Förbrukning",
                "H Klass",
                "H Larm",
                "P Förbrukning",
                "P Klass",
                "P Larm"
        );

        mManager.getTimeFilteredItems().stream()
                .filter(p -> p.getDimension() == BDimension._3d)
                .sorted(comparator)
                .forEachOrdered(listConsumer);

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

    private String getAlarmLevel(int level) {
        return switch (level) {
            case 0 ->
                "Grön";
            case 1 ->
                "Gul";
            case 2 ->
                "Röd";
            default ->
                "";
        };
    }

    private Integer getMax(Integer n1, Integer n2) {
        if (ObjectUtils.allNotNull(n1, n2)) {
            return FastMath.max(n1, n2);
        } else if (n1 != null) {
            return n1;
        } else {
            return n2;
        }
    }

    private Integer getPercent(BTopoControlPoint p) {
        var percentP = p.ext().getAlarmPercent(BComponent.PLANE);
        var percentH = p.ext().getAlarmPercent(BComponent.HEIGHT);
        return getMax(percentH, percentP);
    }

    private String getPercentClass(Integer percent) {
        var classing = "";
        if (percent != null) {
            if (percent < 40) {
                classing = "A";
            } else if (percent < 80) {
                classing = "B";
            } else {
                classing = "C";
            }
        }
        return classing;
    }

    private String getPercentString(Integer percent) {
        if (percent != null && percent != -1) {
            return "%d%%".formatted(percent);
        } else {
            return "";
        }
    }

}
