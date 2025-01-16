/*
 * Copyright 2024 Patrik Karlström.
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
package org.mapton.butterfly_core.api;

import java.awt.Color;
import org.mapton.butterfly_format.types.BXyzPoint;

/**
 *
 * @author Patrik Karlström
 */
public class XyzHelper {

    public static Color getColorForFrequency(BXyzPoint p) {
        Color color;

        var f = p.getFrequency();
        if (f == null || f == 0) {
            color = Color.WHITE;
        } else if (f == 1) {
            color = Color.RED;
        } else if (f <= 7) {
            color = Color.ORANGE;
        } else if (f <= 28) {
            color = Color.YELLOW;
        } else if (f <= 365) {
            color = Color.GREEN;
        } else {
            color = Color.BLACK;
        }

        return color;
    }

}
