/*
 * Copyright 2022 Patrik Karlström.
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
package org.mapton.api.ui;

import java.util.Arrays;
import java.util.TreeSet;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MPolygonFilterManager;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MFilterBox extends VBox {

    protected final MPolygonFilterManager mPolygonFilterManager = MPolygonFilterManager.getInstance();

    protected String getSortedUnique(String s) {
        return String.join(",", new TreeSet<>(Arrays.asList(StringUtils.split(s))));
    }

    protected boolean isValidString(String searchIn, String searchFor) {
        if (StringUtils.isBlank(searchFor)) {
            return false;
        }

        try {
            String regex = ("\\Q" + searchFor.toLowerCase() + "\\E").replace("*", "\\E.*\\Q");
            if (searchIn.toLowerCase().matches(regex)) {
                return true;
            }
        } catch (Exception e) {
            //
        }

        return false;
    }

    protected boolean isValidStrings(String original, String raw) {
        boolean valid = false;
        if (StringUtils.isBlank(raw)) {
            valid = true;
        } else {
            for (var searchFor : raw.split(",")) {
                if (isValidString(original, searchFor)) {
                    valid = true;
                    break;
                }
            }
        }

        return valid;
    }

}
