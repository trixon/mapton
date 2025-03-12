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
import java.util.TreeSet;
import org.mapton.api.MTemporalManager;
import org.mapton.api.report.MReport;
import org.mapton.butterfly_topo.api.TopoManager;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MReport.class)
public class ActualFrequencyReport extends BaseTopoMeasurementReport {

    private final TopoManager mManager = TopoManager.getInstance();
    private final String mName = "Frekvens, verklig";

    public ActualFrequencyReport() {
        setName(mName);
    }

    @Override
    public ContainerTag getContent() {
        var sb = new StringBuilder();
        var keySet = new TreeSet<String>();

        mManager.getTimeFilteredItems().stream().forEachOrdered(p -> {
            keySet.addAll(p.ext().getMeasurementCountStats().keySet());
        });
        sb.append("Namn").append("\t")
                .append("Kategori").append("\t")
                .append("Grupp").append("\t")
                .append("Höjdlarm").append("\t")
                .append("Planlarm").append("\t")
                .append("Frekvens").append("\t")
                .append("Utfall").append("\t")
                .append("Totalt").append("\t");
        for (var key : keySet.reversed()) {
            sb.append(key).append("\t");
        }
        sb.append("\n");

        mManager.getTimeFilteredItems().stream()
                .filter(p -> p.ext().getNumOfObservations() > 0)
                .forEachOrdered(p -> {
                    int sumOfMeasurements = p.ext().getMeasurementCountStats().values().stream().mapToInt(Integer::intValue).sum();
                    var span = MTemporalManager.getInstance().getSpan();
                    sb.append(p.getName()).append("\t")
                            .append(p.getCategory()).append("\t")
                            .append(p.getGroup()).append("\t")
                            .append(p.getAlarm1Id()).append("\t")
                            .append(p.getAlarm2Id()).append("\t")
                            .append(p.getFrequency()).append("\t")
                            .append(Math.round((double) span / sumOfMeasurements)).append("\t")
                            .append(sumOfMeasurements).append("\t");

                    for (var key : keySet.reversed()) {
                        sb.append(p.ext().getMeasurementCountStats().getOrDefault(key, 0)).append("\t");
                    }

                    sb.append("\n");
                });

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
