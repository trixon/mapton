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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.report.MReport;
import org.mapton.butterfly_alarm.api.AlarmManager;
import org.mapton.butterfly_format.types.BAlarm;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MReport.class)
public class AlarmPropertyReport extends BaseAlarmPropertyReport {

    private final AlarmManager mManager = AlarmManager.getInstance();
    private final String mName = "Larmsammanställning";

    public AlarmPropertyReport() {
        setName(mName);
    }

    @Override
    public ContainerTag getContent() {
        var sb = new StringBuilder();
        var rows = new ArrayList<ArrayList<String>>();
        var hMap = new HashMap<String, BAlarm>();
        var pMap = new HashMap<String, BAlarm>();
        var names = new TreeSet<String>();
        mManager.getTimeFilteredItems().stream()
                .filter(a -> StringUtils.contains(a.getId(), ":"))
                .filter(a -> !StringUtils.containsIgnoreCase(a.getId(), "tilt"))
                .forEachOrdered(a -> {
                    var name = StringUtils.removeEnd(a.getId(), "_H");
                    name = StringUtils.removeEnd(name, "_P");
                    names.add(name);
                    if (StringUtils.endsWith(a.getId(), "_H")) {
                        hMap.put(name, a);
                    } else if (StringUtils.endsWith(a.getId(), "_P")) {
                        pMap.put(name, a);
                    }
                });

        names.forEach(name -> {
            var ha = hMap.get(name);
            var pa = pMap.get(name);

            String h1 = "";
            String h2 = "";
            String hd = "";
            String ht = "";
            LocalDateTime hdu = null;
            LocalDateTime hdc = null;

            String p1 = "";
            String p2 = "";
            String pd = "";
            String pt = "";
            LocalDateTime pdu = null;
            LocalDateTime pdc = null;

            if (ha != null) {
                h1 = ha.getLimit1();
                h2 = ha.getLimit2();
                hd = ha.getName();
//                ht = ha.getType();
                hdu = ha.getDateChanged();
                hdc = ha.getDateCreated();
            }

            if (pa != null) {
                p1 = pa.getLimit1();
                p2 = pa.getLimit2();
                pd = pa.getName();
                pt = pa.getType();
                pdu = pa.getDateChanged();
                pdc = pa.getDateCreated();
            }

            var desc = StringUtils.defaultIfBlank(hd, pd);
            add(sb,
                    name,
                    desc,
                    ht + h1,
                    ht + h2,
                    pt + p1,
                    pt + p2,
                    hdc != null ? hdc.toLocalDate().toString() : "",
                    hdu != null ? hdu.toLocalDate().toString() : "",
                    pdc != null ? pdc.toLocalDate().toString() : "",
                    pdu != null ? pdu.toLocalDate().toString() : "",
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
