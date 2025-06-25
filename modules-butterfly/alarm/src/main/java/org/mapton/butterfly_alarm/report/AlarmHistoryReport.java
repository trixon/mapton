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
package org.mapton.butterfly_alarm.report;

import static j2html.TagCreator.body;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.hr;
import static j2html.TagCreator.html;
import static j2html.TagCreator.pre;
import j2html.tags.ContainerTag;
import java.util.ArrayList;
import org.mapton.api.report.MReport;
import org.mapton.butterfly_core.api.AlarmHistoryManager;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MReport.class)
public class AlarmHistoryReport extends BaseAlarmReport {

    private final AlarmHistoryManager mManager = AlarmHistoryManager.getInstance();
    private final String mName = "Larmhistorik";

    public AlarmHistoryReport() {
        setName(mName);
    }

    @Override
    public ContainerTag getContent() {
        var sb = new StringBuilder();
        var rows = new ArrayList<ArrayList<String>>();
        mManager.getTimeFilteredItems().stream()
                .forEachOrdered(a -> {
                    add(sb,
                            a.getOrigin(),
                            a.getName(),
                            a.getDateChanged().toLocalDate().toString(),
                            a.getField(),
                            a.getOld(),
                            a.getNew(),
                            ""
                    );
                });

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

    private void add(StringBuilder sb, String... strings) {
        for (var string : strings) {
            sb.append(string).append("\t");
        }
        sb.append("\n");
    }
}
