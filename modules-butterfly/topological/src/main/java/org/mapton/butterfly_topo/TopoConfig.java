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
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_format.BaseConfig;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import se.trixon.almond.util.StringHelper;

/**
 *
 * @author Patrik Karlström
 */
public class TopoConfig extends BaseConfig {

    public TopoConfig() {
        super("styles.TopoControlPoint");
    }

    public Color getColor(BTopoControlPoint point) {
        var colorCode = getConfig().getString("color.default", "#888888");

        for (var iterator = getConfig().getKeys("color"); iterator.hasNext();) {
            var key = iterator.next();
            var pattern = StringUtils.substringAfterLast(key, "_");

            if (StringUtils.startsWith(key, "color.cat") && macthes(pattern, point.getCategory())
                    || StringUtils.startsWith(key, "color.name") && macthes(pattern, point.getName())
                    || StringUtils.startsWith(key, "color.alarm") && macthes(pattern, point.getNameOfAlarmHeight(), point.getNameOfAlarmPlane())
                    || StringUtils.startsWith(key, "color.group") && macthes(pattern, point.getGroup())) {
                colorCode = getConfig().getString(key);
            }
        }

        return Color.decode(colorCode);
    }

    private boolean macthes(String pattern, String... strings) {
        return StringHelper.matchesSimpleGlob(pattern, true, false, strings);
    }

}
