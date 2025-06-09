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
package org.mapton.butterfly_structural.load;

import java.awt.Color;
import org.mapton.butterfly_core.api.ButterflyHelper;
import org.mapton.butterfly_format.types.structural.BStructuralLoadCellPoint;

/**
 *
 * @author Patrik Karlström
 */
public class LoadHelper {

    public static Color getAlarmColorAwt(BStructuralLoadCellPoint p) {
        return ButterflyHelper.getAlarmColorAwt(getAlarmLevel(p));
    }

    public static javafx.scene.paint.Color getAlarmColorHeightFx(BStructuralLoadCellPoint p) {
        return ButterflyHelper.getAlarmColorFx(getAlarmLevelHeight(p));
    }

    public static int getAlarmLevel(BStructuralLoadCellPoint p) {
        return p.ext().getAlarmLevel(p.ext().getObservationFilteredLast());
    }

    public static int getAlarmLevelHeight(BStructuralLoadCellPoint p) {
        return p.ext().getAlarmLevel(p.ext().getObservationFilteredLast());
    }

}
