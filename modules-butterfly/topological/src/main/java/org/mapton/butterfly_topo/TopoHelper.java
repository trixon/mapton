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
import org.mapton.butterfly_format.types.rock.BRockConvergence;
import org.mapton.butterfly_format.types.rock.BRockConvergencePair;
import org.mapton.butterfly_format.types.topo.BTopoGrade;
import org.mapton.butterfly_format.types.topo.BTopoGradeDiff;

/**
 *
 * @author Patrik Karlström
 */
public class TopoHelper {

    public static Color getAlarmColorAwt(BRockConvergence p) {
        return ButterflyHelper.getAlarmColorAwt(getAlarmLevel(p));
    }

    public static Color getAlarmColorAwt(BRockConvergencePair p) {
        return ButterflyHelper.getAlarmColorAwt(getAlarmLevel(p));
    }

    public static Color getAlarmColorAwt(BTopoGrade p) {
        if (p.getAxis() == BAxis.HORIZONTAL) {
            return ButterflyHelper.getAlarmColorAwt(getAlarmLevelHorizontal(p));
        } else {
            return ButterflyHelper.getAlarmColorAwt(getAlarmLevelVertical(p));
        }
    }

    public static Color getAlarmColorAwt(BTopoControlPoint p) {
        return ButterflyHelper.getAlarmColorAwt(getAlarmLevel(p));
    }

    public static javafx.scene.paint.Color getAlarmColorFx(BRockConvergence p) {
        return ButterflyHelper.getAlarmColorFx(getAlarmLevel(p));
    }

    public static javafx.scene.paint.Color getAlarmColorFx(BRockConvergencePair p) {
        return ButterflyHelper.getAlarmColorFx(getAlarmLevel(p));
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
        return ButterflyHelper.getAlarmColorFx(getAlarmLevelHorizontal(p));
    }

    public static Color getAlarmColorPlaneAwt(BTopoControlPoint p) {
        return ButterflyHelper.getAlarmColorAwt(getAlarmLevelPlane(p));
    }

    public static javafx.scene.paint.Color getAlarmColorPlaneFx(BTopoGrade p) {
        return ButterflyHelper.getAlarmColorFx(getAlarmLevelVertical(p));
    }

    public static javafx.scene.paint.Color getAlarmColorPlaneFx(BTopoControlPoint p) {
        return ButterflyHelper.getAlarmColorFx(getAlarmLevelPlane(p));
    }

    public static int getAlarmLevel(BTopoGrade p, BTopoGradeDiff diff) {
        if (p.getAxis() == BAxis.HORIZONTAL) {
            return getAlarmLevelHorizontal(p, diff);
        } else {
            return getAlarmLevelVertical(p, diff);
        }
    }

    public static int getAlarmLevel(BTopoGrade p) {
        if (p.getAxis() == BAxis.HORIZONTAL) {
            return getAlarmLevelHorizontal(p);
        } else {
            return getAlarmLevelVertical(p);
        }
    }

    public static int getAlarmLevel(BTopoControlPoint p) {
        try {
            return p.ext().getAlarmLevel(p.ext().getObservationFilteredLast());
        } catch (Exception e) {
            return -1;
        }
    }

    public static int getAlarmLevel(BRockConvergence p) {
        return p.ext().getAlarmLevel(p.ext().getObservationFilteredLast());
    }

    public static int getAlarmLevel(BRockConvergencePair p) {
        return p.ext().getAlarmLevel(p.ext().getObservationFilteredLast());
    }

    public static int getAlarmLevelHeight(BTopoControlPoint p) {
        return p.ext().getAlarmLevelHeight(p.ext().getObservationFilteredLast());
    }

    public static int getAlarmLevelHorizontal(BTopoGrade p, BTopoGradeDiff diff) {
        return p.ext().getAlarmLevelHeight(Math.abs(diff.getZQuota()));
    }

    public static int getAlarmLevelHorizontal(BTopoGrade p) {
        return getAlarmLevelHorizontal(p, p.ext().getDiff());
    }

    public static int getAlarmLevelPlane(BTopoControlPoint p) {
        return p.ext().getAlarmLevelPlane(p.ext().getObservationFilteredLast());
    }

    public static int getAlarmLevelVertical(BTopoGrade p, BTopoGradeDiff diff) {
        return p.ext().getAlarmLevelPlane(Math.abs(diff.getRQuota()));
    }

    public static int getAlarmLevelVertical(BTopoGrade p) {
        return getAlarmLevelVertical(p, p.ext().getDiff());
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

    public static Color getGradeDistanceColor(BTopoGrade p) {
        var gradeDiff = p.ext().getDiff();
        var dZ = gradeDiff.getPartialDiffDistance() / 1000.0;
        if (dZ < 0) {
            return ButterflyHelper.sVerticalNegColors[ButterflyHelper.getColorIndex(ButterflyHelper.sVerticalNegMaterials.length, 0.025, dZ)];
        } else {
            return ButterflyHelper.sVerticalPosColors[ButterflyHelper.getColorIndex(ButterflyHelper.sVerticalPosMaterials.length, 0.025, dZ)];
        }
    }

    public static Material getGradeDistanceMaterial(BTopoGrade p) {
        var gradeDiff = p.ext().getDiff();
        var dZ = gradeDiff.getPartialDiffDistance() / 1000.0;
        if (dZ < 0) {
            return ButterflyHelper.sVerticalNegMaterials[ButterflyHelper.getColorIndex(ButterflyHelper.sVerticalNegMaterials.length, 0.025, dZ)];
        } else {
            return ButterflyHelper.sVerticalPosMaterials[ButterflyHelper.getColorIndex(ButterflyHelper.sVerticalPosMaterials.length, 0.025, dZ)];
        }
    }

    public static Color getVerticalColor(BTopoControlPoint p) {
        var dZ = p.ext().deltaZero().getDelta1();
        if (dZ == null) {
            return Color.YELLOW;
        } else if (dZ < 0) {
            return ButterflyHelper.sVerticalNegColors[ButterflyHelper.getColorIndex(ButterflyHelper.sVerticalNegMaterials.length, 0.025, dZ)];
        } else {
            return ButterflyHelper.sVerticalPosColors[ButterflyHelper.getColorIndex(ButterflyHelper.sVerticalPosMaterials.length, 0.025, dZ)];
        }
    }

    public static Material getVerticalMaterial(BTopoControlPoint p) {
        var dZ = p.ext().deltaZero().getDelta1();
        if (dZ == null) {
            return Material.YELLOW;
        } else if (dZ < 0) {
            return ButterflyHelper.sVerticalNegMaterials[ButterflyHelper.getColorIndex(ButterflyHelper.sVerticalNegMaterials.length, 0.025, dZ)];
        } else {
            return ButterflyHelper.sVerticalPosMaterials[ButterflyHelper.getColorIndex(ButterflyHelper.sVerticalPosMaterials.length, 0.025, dZ)];
        }
    }

}
