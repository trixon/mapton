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
import org.mapton.butterfly_format.types.BAxis;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_format.types.topo.BTopoGrade;

/**
 *
 * @author Patrik Karlström
 */
public class TopoHelper {

    private static final Color[] sSpeedColors;
    private static final Material[] sSpeedMaterials;
    private static final Color[] sVerticalNegColors;
    private static final Material[] sVerticalNegMaterials;
    private static final Color[] sVerticalPosColors;
    private static final Material[] sVerticalPosMaterials;

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

        sVerticalNegColors = new Color[]{
            //https://colordesigner.io/gradient-generator/?mode=oklab#FFFFFF-FF0000
            Color.decode("#00ff00"),
            Color.decode("#ffe5df"),
            Color.decode("#ffcac0"),
            Color.decode("#ffafa1"),
            Color.decode("#ff9281"),
            Color.decode("#ff7361"),
            Color.decode("#ff4e3e"),
            Color.decode("#ff0000"),
            Color.decode("#aa0000")
        };

        sVerticalPosColors = new Color[]{
            //https://colordesigner.io/gradient-generator/?mode=oklab#0000FF-FFFFFF
            Color.decode("#00ff00"),
            Color.decode("#d6e6ff"),
            Color.decode("#aeccff"),
            Color.decode("#87b1ff"),
            Color.decode("#6094ff"),
            Color.decode("#3a75ff"),
            Color.decode("#1250ff"),
            Color.decode("#0000ff"),
            Color.decode("#0000aa")
        };

        sVerticalPosMaterials = new Material[sVerticalPosColors.length];
        for (int i = 0; i < sVerticalPosMaterials.length; i++) {
            sVerticalPosMaterials[i] = new Material(sVerticalPosColors[i]);
        }
        sVerticalNegMaterials = new Material[sVerticalNegColors.length];
        for (int i = 0; i < sVerticalNegMaterials.length; i++) {
            sVerticalNegMaterials[i] = new Material(sVerticalNegColors[i]);
        }
    }

    public static Color getAlarmColorAwt(BTopoGrade p) {
        if (p.getAxis() == BAxis.HORIZONTAL) {
            return ButterflyHelper.getAlarmColorAwt(getAlarmLevelHeight(p));
        } else {
            return ButterflyHelper.getAlarmColorAwt(getAlarmLevelPlane(p));
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

    public static javafx.scene.paint.Color getAlarmColorHeightFx(BTopoGrade p) {
        return ButterflyHelper.getAlarmColorFx(getAlarmLevelHeight(p));
    }

    public static Color getAlarmColorPlaneAwt(BTopoControlPoint p) {
        return ButterflyHelper.getAlarmColorAwt(getAlarmLevelPlane(p));
    }

    public static javafx.scene.paint.Color getAlarmColorPlaneFx(BTopoGrade p) {
        return ButterflyHelper.getAlarmColorFx(getAlarmLevelPlane(p));
    }

    public static javafx.scene.paint.Color getAlarmColorPlaneFx(BTopoControlPoint p) {
        return ButterflyHelper.getAlarmColorFx(getAlarmLevelPlane(p));
    }

    public static int getAlarmLevel(BTopoGrade p) {
        if (p.getAxis() == BAxis.HORIZONTAL) {
            return getAlarmLevelHeight(p);
        } else {
            return getAlarmLevelPlane(p);
        }
    }

    public static int getAlarmLevel(BTopoControlPoint p) {
        return p.ext().getAlarmLevel(p.ext().getObservationFilteredLast());
    }

    public static int getAlarmLevelHeight(BTopoGrade p) {
        return p.ext().getAlarmLevelHeight(Math.abs(p.ext().getDiff().getZQuota()));
    }

    public static int getAlarmLevelHeight(BTopoControlPoint p) {
        return p.ext().getAlarmLevelHeight(p.ext().getObservationFilteredLast());
    }

    public static int getAlarmLevelPlane(BTopoGrade p) {
        return p.ext().getAlarmLevelPlane(Math.abs(p.ext().getDiff().getRQuota()));
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

    public static Color getVerticalColor(BTopoControlPoint p) {
        var dZ = p.ext().deltaZero().getDelta1();
        if (dZ == null) {
            return Color.YELLOW;
        } else if (dZ < 0) {
            return sVerticalNegColors[getColorIndex(sVerticalNegMaterials.length, 0.025, dZ)];
        } else {
            return sVerticalPosColors[getColorIndex(sVerticalPosMaterials.length, 0.025, dZ)];
        }
    }

    public static Material getVerticalMaterial(BTopoControlPoint p) {
        var dZ = p.ext().deltaZero().getDelta1();
        if (dZ == null) {
            return Material.YELLOW;
        } else if (dZ < 0) {
            return sVerticalNegMaterials[getColorIndex(sVerticalNegMaterials.length, 0.025, dZ)];
        } else {
            return sVerticalPosMaterials[getColorIndex(sVerticalPosMaterials.length, 0.025, dZ)];
        }
    }

    private static int getColorIndex(int length, double limit, double value) {
        int index = (int) Math.min(length - 1, (Math.abs(value) / limit) * (length - 1));

        return index;
    }

}
