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
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.api.report.MReport;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_topo.api.TopoManager;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MReport.class)
public class InvalidHeightMeasurementsReport extends BaseTopoMeasurementReport {

    private final TopoManager mManager = TopoManager.getInstance();
    private final String mName = "Ogiltiga mätningar i höjd";

    public InvalidHeightMeasurementsReport() {
        setName(mName);
    }

    @Override
    public ContainerTag getContent() {
        var sb = new StringBuilder();

        sb.append("Namn").append("\t")
                .append("Utförare").append("\t")
                .append("Datum").append("\t")
                .append("Kommentar").append("\t")
                .append("\n");

        mManager.getTimeFilteredItems().stream()
                .filter(p -> p.getDimension() == BDimension._1d)
                .forEachOrdered(p -> {
                    for (var o : p.ext().getObservationsTimeFiltered()) {
                        if (ObjectUtils.anyNull(o.getMeasuredZ())) {
                            sb.append(p.getName()).append("\t")
                                    .append(p.getOperator()).append("\t")
                                    .append(o.getDate().toString()).append("\t")
                                    .append(o.getComment()).append("\t")
                                    .append("\n");

                        }
                    }
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
