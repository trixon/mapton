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
package org.mapton.api.ui.forms;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Patrik Karlström
 */
public class FormHelper {

    public static Double ltGtToNegPos(String s) {
        if (StringUtils.equalsIgnoreCase(s, "= 0")) {
            return 0d;
        } else if (StringUtils.startsWith(s, "<")) {
            return Double.parseDouble(StringUtils.substringAfter(s, " ")) * -1;
        } else if (StringUtils.startsWith(s, ">")) {
            return Double.valueOf(StringUtils.substringAfter(s, " "));
        } else {
            return Double.valueOf(s);
        }
    }

    public static String negPosToLtGt(int value) {
        var s = Integer.toString(value);

        if (value == 0) {
            return "= 0";
        } else if (value < 0) {
            return StringUtils.replace(s, "-", "<= ");
        } else {
            return ">= " + s;
        }
    }

    public static String negPosToLtGt(Double value) {
        var s = "%.3f".formatted(value);

        if (value == 0) {
            return "= 0";
        } else if (value < 0) {
            return StringUtils.replace(s, "-", "<= ");
        } else {
            return ">= " + s;
        }
    }

}
