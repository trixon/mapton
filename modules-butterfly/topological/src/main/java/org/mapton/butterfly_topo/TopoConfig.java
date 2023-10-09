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
package org.mapton.butterfly_topo;

import java.awt.Color;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_api.api.BaseConfig;
import org.mapton.butterfly_format.types.controlpoint.BTopoControlPoint;
import se.trixon.almond.util.StringHelper;

/**
 *
 * @author Patrik Karlström
 */
public class TopoConfig extends BaseConfig {

    public TopoConfig() {
        super(new File(new File(FileUtils.getTempDirectory(), "butterfly"), "styles.TopoControlPoint"));
    }

    public Color getColor(BTopoControlPoint point) {
        //TODO use prefix
        var s = getConfig().getString("color_default", "#888888");

        for (var iterator = getConfig().getKeys(); iterator.hasNext();) {
            var key = iterator.next();
            var pattern = StringUtils.substringAfterLast(key, "_");

            if (StringUtils.startsWith(key, "color_cat") && macthes(point.getCategory(), pattern)
                    || StringUtils.startsWith(key, "color_name") && macthes(point.getName(), pattern)
                    || StringUtils.startsWith(key, "color_group") && macthes(point.getGroup(), pattern)) {
                s = getConfig().getString(key);
            }
        }

        return Color.decode(s);
    }

    private boolean macthes(String s, String pattern) {
        return StringHelper.matchesSimpleGlob(s, pattern, true, false);
    }

}
