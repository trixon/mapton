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
package org.mapton.butterfly_composite.report;

import static j2html.TagCreator.body;
import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.hr;
import static j2html.TagCreator.html;
import static j2html.TagCreator.li;
import static j2html.TagCreator.text;
import static j2html.TagCreator.ul;
import j2html.tags.ContainerTag;
import org.mapton.api.report.MReport;
import org.mapton.butterfly_composite.CompositeManager;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MReport.class)
public class BasicReport extends BaseCompositeReport {

    private final CompositeManager mManager = CompositeManager.getInstance();
    private final String mName = "Struktur";

    public BasicReport() {
        setName(mName);
    }

    @Override
    public ContainerTag getContent() {
        var html = html(
                body(
                        each(mManager.getAllItems(), p -> div(
                                h1(p.getName()),
                                hr(),
                                h2("Topo"),
                                ul(
                                        each(p.ext().getTopoPoints(), p2 -> li(text(p2.getName())))
                                ),
                                h2("Lastceller"),
                                ul(
                                        each(p.ext().getLoadPoints(), p2 -> li(text(p2.getName())))
                                ),
                                h2("Töjningsgivare"),
                                ul(
                                        each(p.ext().getStrainPoints(), p2 -> li(text(p2.getName())))
                                ),
                                br()
                        ))
                )
        );
        return html;
    }
}
