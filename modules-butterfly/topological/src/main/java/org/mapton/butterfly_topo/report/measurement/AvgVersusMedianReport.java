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
import static j2html.TagCreator.html;
import static j2html.TagCreator.pre;
import j2html.tags.ContainerTag;
import java.util.List;
import java.util.stream.Collectors;
import org.mapton.api.report.MReport;
import org.mapton.butterfly_topo.api.TopoManager;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MReport.class)
public class AvgVersusMedianReport extends BaseTopoMeasurementReport {

    private final TopoManager mManager = TopoManager.getInstance();
    private final String mName = "Test, medel kontra median";

    public AvgVersusMedianReport() {
        setName(mName);
    }

    @Override
    public ContainerTag getContent() {
        var sb = new StringBuilder(String.join("\t", List.of(
                "NAME",
                "DATE",
                "COUNT",
                "AVERAGE",
                "MEDIAN",
                "DIFF 1",
                "MIN",
                "MAX",
                "DIFF 2",
                "\n"
        )));

        mManager.getTimeFilteredItems().forEach(p -> {
            var dateToObservations = p.ext().getObservationsTimeFiltered().stream()
                    .collect(Collectors.groupingBy(o -> o.getDate().toLocalDate()));

            for (var entry : dateToObservations.entrySet()) {
                var date = entry.getKey();
                var observations = entry.getValue();
                var size = observations.size();
                if (size > 1) {
                    var observationsZ = observations.stream().mapToDouble(o -> o.getMeasuredZ() * 1000).sorted().boxed().toList();
                    var avg = observationsZ.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                    double med;
                    if (size % 2 == 0) {
                        med = (observationsZ.get(size / 2 - 1) + observationsZ.get(size / 2)) / 2.0;
                    } else {
                        med = observationsZ.get(size / 2);
                    }
                    var diff1 = Math.abs(Math.abs(med) - Math.abs(avg));
                    var min = observationsZ.getFirst();
                    var max = observationsZ.getLast();
                    var diff2 = Math.abs(Math.abs(min) - Math.abs(max));
                    var line = String.format("%s\t%s\t%d\t"
                            + "%.3f\t"
                            + "%.3f\t"
                            + "%.3f\t"
                            + "%.3f\t"
                            + "%.3f\t"
                            + "%.3f\t"
                            + "",
                            p.getName(),
                            date,
                            size,
                            avg,
                            med,
                            diff1,
                            min,
                            max,
                            diff2
                    );

                    sb.append(line).append("\n");
                }
            }
        });

        var html = html(
                body(
                        pre(sb.toString())
                )
        );

        return html;
    }

}
