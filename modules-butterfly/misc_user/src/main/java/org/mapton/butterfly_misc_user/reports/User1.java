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

import static j2html.TagCreator.body;
import static j2html.TagCreator.html;
import static j2html.TagCreator.pre;
import j2html.tags.ContainerTag;
import java.util.stream.Collectors;
import org.mapton.api.report.MReport;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MReport.class)
public class User1 extends BaseUserReport {

    private final String mName = "Användare 1";

    public User1() {
        setName(mName);
    }

    @Override
    public ContainerTag getContent() {
        for (var user : getUsers()) {
            System.out.println(user.getName());
        }
        String result = "";
        result = getUsers().stream().map(u -> u.getName()).collect(Collectors.joining(",\n"));
        var html = html(
                body(
                        pre(result)
                )
        );

        return html;
    }

}
