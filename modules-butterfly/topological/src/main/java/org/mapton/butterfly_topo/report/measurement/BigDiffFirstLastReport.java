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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.mapton.api.report.MReport;
import static org.mapton.butterfly_format.Butterfly.DOUBLE_NaN_LAST_DESC;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.api.TopoManager;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MReport.class)
public class BigDiffFirstLastReport extends BaseTopoMeasurementReport {

    private final TopoManager mManager = TopoManager.getInstance();
    private final String mName = "Stor skillnad första & sista";

    public BigDiffFirstLastReport() {
        setName(mName);
    }

    @Override
    public ContainerTag getContent() {
        var sb = new StringBuilder();
        mManager.getTimeFilteredItems().stream()
                .filter(p -> p.getDimension() == BDimension._3d)
                .filter(p -> p.extOrNull().getObservationsTimeFiltered().size() > 1)
                .peek(p -> {
                    var first = p.extOrNull().getObservationsTimeFiltered().getFirst();
                    var last = p.extOrNull().getObservationsTimeFiltered().getLast();
                    var dx = last.getMeasuredX() - first.getMeasuredX();
                    var dy = last.getMeasuredY() - first.getMeasuredY();
                    var d = Math.hypot(dx, dy);
                    p.setValue(getClass().getName(), d);
                })
                .sorted((p1, p2) -> DOUBLE_NaN_LAST_DESC.compare(
                        (Double) p1.getValue(getClass().getName(), Double.NaN),
                        (Double) p2.getValue(getClass().getName(), Double.NaN)
                ))
                .forEachOrdered(p -> {
                    sb.append("%s\t%.1f\n".formatted(p.getName(), (Double) p.getValue(getClass().getName(), Double.NaN)));
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

    private double calcAverage(Stream<BTopoControlPoint> stream) {
        var now = LocalDateTime.now();
        return stream
                .filter(p -> p.getDateLatest() != null)
                .mapToDouble(p -> {
                    var duration = Duration.between(p.getDateLatest(), now);

                    return duration.toMinutes() / 60d / 24d;
                })
                .average().orElse(-1d);
    }

}
