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
package org.mapton.butterfly_misc_user.reports;

import j2html.TagCreator;
import static j2html.TagCreator.body;
import static j2html.TagCreator.each;
import static j2html.TagCreator.html;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.th;
import j2html.tags.ContainerTag;
import java.util.Objects;
import org.mapton.api.report.MReport;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MReport.class)
public class User0 extends BaseUserReport {

    private final String mName = "Användare 0";

    public User0() {
        setName(mName);
    }

    @Override
    public ContainerTag getContent() {
        var userTable = table().with(
                TagCreator.thead().withStyle("text-align: left;").with(
                        TagCreator.tr(
                                th("Id"),
                                th("Sign"),
                                th("Namn"),
                                th("Organisation"),
                                th("Domän"),
                                th("Epost"),
                                th("T.o.m."),
                                th("Fr.o.m.")
                        )
                ),
                tbody(
                        each(getUniqueUsers(), user -> TagCreator.tr(
                                TagCreator.td(user.getExternalId()),
                                TagCreator.td(Objects.toString(user.getInitials(), "")),
                                TagCreator.td(Objects.toString(user.getName(), "")),
                                TagCreator.td(Objects.toString(user.getOperator(), "")),
                                TagCreator.td(Objects.toString(user.getGroup(), "")),
                                TagCreator.td(Objects.toString(user.getEmail(), "")),
                                TagCreator.td(user.getExpires() != null ? user.getExpires().toString() : ""),
                                TagCreator.td(user.getDateCreated() != null ? user.getDateCreated().toLocalDate().toString() : "")
                        ))
                )
        );

        var html = html(
                body(
                        userTable
                )
        );

        return html;
    }

}
