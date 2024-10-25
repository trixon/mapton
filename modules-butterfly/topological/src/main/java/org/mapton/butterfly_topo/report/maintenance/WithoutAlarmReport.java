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
package org.mapton.butterfly_topo.report.maintenance;

import static j2html.TagCreator.body;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.hr;
import static j2html.TagCreator.html;
import static j2html.TagCreator.p;
import j2html.tags.ContainerTag;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.report.MReport;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_topo.api.TopoManager;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MReport.class)
public class WithoutAlarmReport extends BaseTopoMaintenanceReport {

    private final TopoManager mManager = TopoManager.getInstance();
    private final String mName = "Utan larm";

    public WithoutAlarmReport() {
        setName(mName);
    }

    @Override
    public ContainerTag getContent() {

        var list = mManager.getAllItems().stream()
                .filter(p -> {
                    var missingH = p.getDimension() != BDimension._2d && StringUtils.isBlank(p.getAlarm1Id());
                    var missingP = p.getDimension() != BDimension._1d && StringUtils.isBlank(p.getAlarm2Id());
                    return missingH || missingP;
                })
                .toList();

        var html = html(
                body(
                        h1(mName),
                        hr(),
                        each(list, p -> div(p(p.getName())))
                )
        );

        return html;
    }
}
