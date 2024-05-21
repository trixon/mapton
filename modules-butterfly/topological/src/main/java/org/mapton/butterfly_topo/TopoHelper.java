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
import org.mapton.butterfly_core.api.ButterflyHelper;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;

/**
 *
 * @author Patrik Karlström
 */
public class TopoHelper {

    private static final Color[] sSpeedColors;
    private static final Material[] sSpeedMaterials;

    static {
        sSpeedColors = new Color[]{
            //https://colordesigner.io/gradient-generator/?mode=lch#ACF0F2-2C1DFF
            Color.decode("#acf0f2"),
            Color.decode("#77e2f2"),
            Color.decode("#22d2f8"),
            Color.decode("#00c0fa"),
            Color.decode("#00adee"),
            Color.decode("#009be1"),
            Color.decode("#008ad2"),
            Color.decode("#007ac8"),
            Color.decode("#0066cd"),
            Color.decode("#2c1dff"),
            Color.decode("#FF00FF")
        };

        sSpeedMaterials = new Material[sSpeedColors.length];
        for (int i = 0; i < sSpeedMaterials.length; i++) {
            sSpeedMaterials[i] = new Material(sSpeedColors[i]);
        }
    }

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

    public static int getAlarmLevel(BTopoControlPoint p) {
        return p.ext().getAlarmLevel(p.ext().getObservationFilteredLast());
    }

    public static int getAlarmLevelHeight(BTopoControlPoint p) {
        return p.ext().getAlarmLevelHeight(p.ext().getObservationFilteredLast());
    }

    public static int getAlarmLevelPlane(BTopoControlPoint p) {
        return p.ext().getAlarmLevelPlane(p.ext().getObservationFilteredLast());
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

    public static Color getSpeedColor(BTopoControlPoint p) {
        return sSpeedColors[getSpeedLevel(p)];
    }

    public static Material getSpeedMaterial(BTopoControlPoint p) {
        return sSpeedMaterials[getSpeedLevel(p)];
    }

    private static int getSpeedLevel(BTopoControlPoint p) {
        var dZ = p.ext().getSpeed()[0];
        var length = sSpeedMaterials.length;
        var limit = 0.025;
        int level = (int) Math.min(length - 1, (Math.abs(dZ) / limit) * (length - 1));
        return level;
    }

}
