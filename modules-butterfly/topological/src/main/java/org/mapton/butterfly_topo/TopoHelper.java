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

import gov.nasa.worldwind.render.Material;
import java.awt.Color;
import org.mapton.butterfly_api.api.ButterflyHelper;
import org.mapton.butterfly_format.types.controlpoint.BTopoControlPoint;

/**
 *
 * @author Patrik Karlström
 */
public class TopoHelper {

    public static Color getAlarmColorAwt(BTopoControlPoint p) {
        return ButterflyHelper.getAlarmColorAwt(getAlarmLevel(p));
    }

    public static javafx.scene.paint.Color getAlarmColorFx(BTopoControlPoint p) {
        return ButterflyHelper.getAlarmColorFx(getAlarmLevel(p));
    }

    public static Color getAlarmColorHeightAwt(BTopoControlPoint p) {
        return ButterflyHelper.getAlarmColorAwt(getAlarmLevelHeight(p));
    }

    public static javafx.scene.paint.Color getAlarmColorHeightFx(BTopoControlPoint p) {
        return ButterflyHelper.getAlarmColorFx(getAlarmLevelHeight(p));
    }

    public static Color getAlarmColorPlaneAwt(BTopoControlPoint p) {
        return ButterflyHelper.getAlarmColorAwt(getAlarmLevelPlane(p));
    }

    public static javafx.scene.paint.Color getAlarmColorPlaneFx(BTopoControlPoint p) {
        return ButterflyHelper.getAlarmColorFx(getAlarmLevelPlane(p));
    }

    public static Material getAlarmMaterial(BTopoControlPoint p) {
        return ButterflyHelper.getAlarmMaterial(getAlarmLevel(p));
    }

    public static Material getAlarmMaterialHeight(BTopoControlPoint p) {
        return ButterflyHelper.getAlarmMaterial(getAlarmLevelHeight(p));
    }

    public static Material getAlarmMaterialPlane(BTopoControlPoint p) {
        return ButterflyHelper.getAlarmMaterial(getAlarmLevelPlane(p));
    }

    private static int getAlarmLevel(BTopoControlPoint p) {
        return p.ext().getAlarmLevel(p.ext().getObservationFilteredLast());
    }

    private static int getAlarmLevelHeight(BTopoControlPoint p) {
        return p.ext().getAlarmLevelHeight(p.ext().getObservationFilteredLast());
    }

    private static int getAlarmLevelPlane(BTopoControlPoint p) {
        return p.ext().getAlarmLevelPlane(p.ext().getObservationFilteredLast());
    }

}
