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
package org.mapton.core.reports;

import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.li;
import static j2html.TagCreator.ul;
import j2html.tags.ContainerTag;
import java.util.TreeMap;
import java.util.TreeSet;
import javafx.scene.Node;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MApiReport;
import org.mapton.api.report.MReport;
import org.mapton.api.report.MSubReport;
import org.mapton.api.report.MSubReportsSystem;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MReport.class)
public class ApiReport extends MSubReportsSystem {

    @Override
    public String getName() {
        return "API ServiceProviders";
    }

    @Override
    public Node getNode() {
        var node = super.getNode();
        var implementations = new TreeMap<String, TreeSet<String>>();

        for (var implementation : Lookup.getDefault().lookupAll(MApiReport.class)) {
            implementations.putAll(implementation.getItems());
        }

        var listItems = getListView().getItems();
        listItems.clear();

        for (var key : implementations.keySet()) {
            var items = implementations.get(key);
            String[] headers = StringUtils.split(key, "/", 2);
            String tempGroup = "";
            String tempName;

            if (headers.length == 0) {
                tempName = "NO NAME";
            } else if (headers.length == 1) {
                tempName = headers[0];
            } else {
                tempGroup = headers[0];
                tempName = headers[1];
            }

            var group = tempGroup;
            var name = tempName;

            var subReport = new MSubReport() {

                @Override
                public ContainerTag getContent() {
                    var html = div(
                            ul(
                                    each(items, item -> li(item))
                            )
                    );

                    return html;

                }

                @Override
                public String getGroup() {
                    return group;
                }

                @Override
                public String getName() {
                    return name;
                }
            };

            listItems.add(subReport);
        }

        return node;
    }

    @Override
    public void subInit() {
    }
}
