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
package org.mapton.butterfly_topo.report;

import static j2html.TagCreator.body;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.hr;
import static j2html.TagCreator.html;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;
import static j2html.TagCreator.tr;
import j2html.tags.ContainerTag;
import java.util.Objects;
import org.mapton.api.report.MReport;
import org.mapton.butterfly_alarm.api.AlarmHelper;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_topo.api.TopoManager;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MReport.class)
public class BasicReport extends BaseTopoReport {

    private final TopoManager mManager = TopoManager.getInstance();
    private final String mName = "Grundinformation";

    public BasicReport() {
        setName(mName);
    }

    @Override
    public ContainerTag getContent() {
        var html = html(
                body(
                        h1(mName),
                        hr(),
                        table(
                                tr(
                                        th(Dict.NAME.toString()),
                                        th(Dict.CATEGORY.toString()),
                                        th(Dict.GROUP.toString()),
                                        th(SDict.ALARM_HEIGHT.toString()),
                                        th(SDict.ALARM_LEVEL.toString()),
                                        th(SDict.ALARM_PLANE.toString()),
                                        th(SDict.ALARM_LEVEL.toString()),
                                        th(SDict.FREQUENCY.toString()),
                                        th(SDict.DIMENSION.toString())
                                ),
                                tbody(
                                        each(mManager.getTimeFilteredItems(), p
                                                -> tr(
                                                td(p.getName()),
                                                td(p.getCategory()),
                                                td(p.getGroup()),
                                                td(p.getNameOfAlarmHeight()),
                                                td(AlarmHelper.getInstance().getLimitsAsString(BComponent.HEIGHT, p)),
                                                td(p.getNameOfAlarmPlane()),
                                                td(AlarmHelper.getInstance().getLimitsAsString(BComponent.PLANE, p)),
                                                td(Objects.toString(p.getFrequency(), "")),
                                                td(Objects.toString(p.getDimension().getName(), ""))
                                        )
                                        )
                                )
                        )
                ));

        return html;
    }
}
