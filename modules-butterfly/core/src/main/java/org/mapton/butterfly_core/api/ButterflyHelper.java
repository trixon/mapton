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
package org.mapton.butterfly_core.api;

import gov.nasa.worldwind.render.Material;
import java.awt.Color;
import java.text.SimpleDateFormat;
import org.apache.commons.lang3.StringUtils;
import org.openide.modules.Modules;
import org.openide.windows.WindowManager;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ButterflyHelper {

    public static final Color[] sGreenToRedColors;
    public static final Material[] sGreenToRedMaterials;
    public static final Color[] sSpeedColors;
    public static final Material[] sSpeedMaterials;
    public static final Color[] sVerticalNegColors;
    public static final Material[] sVerticalNegMaterials;
    public static final Color[] sVerticalPosColors;
    public static final Material[] sVerticalPosMaterials;
    private static final java.awt.Color[] mAlarmColorsAwt = new Color[]{
        java.awt.Color.BLUE,
        java.awt.Color.GREEN,
        java.awt.Color.YELLOW,
        java.awt.Color.RED,
        java.awt.Color.RED.darker()
    };
    private static final javafx.scene.paint.Color[] mAlarmColorsFx = new javafx.scene.paint.Color[]{
        javafx.scene.paint.Color.BLUE,
        javafx.scene.paint.Color.LIGHTGREEN,
        javafx.scene.paint.Color.YELLOW,
        javafx.scene.paint.Color.RED,
        javafx.scene.paint.Color.RED.darker()
    };
    private static final Material[] mAlarmMaterials = new Material[]{
        Material.BLUE,
        Material.GREEN,
        Material.YELLOW,
        Material.RED,
        new Material(Color.RED.darker())
    };

    static {
        sGreenToRedColors = new Color[]{
            //https://colordesigner.io/gradient-generator/?mode=lch#00FF00-FF0000
            Color.decode("#00ff00"),
            Color.decode("#53f500"),
            Color.decode("#76ea00"),
            Color.decode("#8fdf00"),
            Color.decode("#a1d300"),
            Color.decode("#b0c800"),
            Color.decode("#bdbc00"),
            Color.decode("#c9b000"),
            Color.decode("#d4a300"),
            Color.decode("#dd9500"),
            Color.decode("#e68700"),
            Color.decode("#ef7600"),
            Color.decode("#f66300"),
            Color.decode("#fa4e00"),
            Color.decode("#fd3500"),
            Color.decode("#ff0000")
        };

        sGreenToRedMaterials = new Material[sGreenToRedColors.length];
        for (int i = 0; i < sGreenToRedMaterials.length; i++) {
            sGreenToRedMaterials[i] = new Material(sGreenToRedColors[i]);
        }

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

    public static java.awt.Color getAlarmColorAwt(int alarmLevel) {
        return mAlarmColorsAwt[1 + alarmLevel];
    }

    public static javafx.scene.paint.Color getAlarmColorFx(int alarmLevel) {
        return mAlarmColorsFx[1 + alarmLevel];
    }

    public static java.awt.Color[] getAlarmColorsAwt() {
        return mAlarmColorsAwt;
    }

    public static javafx.scene.paint.Color[] getAlarmColorsFx() {
        return mAlarmColorsFx;
    }

    public static Material getAlarmMaterial(int alarmLevel) {
        return mAlarmMaterials[1 + alarmLevel];
    }

    public static Material[] getAlarmMaterials() {
        return mAlarmMaterials;
    }

    public static Color getColorAwt(Color[] colors, double limit, double value) {
        var length = colors.length;
        int index = (int) Math.min(length - 1, (Math.abs(value) / limit) * (length - 1));

        return colors[index];
    }

    public static int getColorIndex(int length, double limit, double value) {
        int index = (int) Math.min(length - 1, (Math.abs(value) / limit) * (length - 1));

        return index;
    }

    public static Color getRangeColor(Double value, double limit) {
        if (value == null) {
            return Color.YELLOW;
        } else if (value < 0) {
            return sVerticalNegColors[getColorIndex(sVerticalNegMaterials.length, limit, value)];
        } else {
            return sVerticalPosColors[getColorIndex(sVerticalPosMaterials.length, limit, value)];
        }
    }

    public static Material getRangeMaterial(Double value, double limit) {
        if (value == null) {
            return Material.YELLOW;
        } else if (value < 0) {
            return sVerticalNegMaterials[getColorIndex(sVerticalNegMaterials.length, limit, value)];
        } else {
            return sVerticalPosMaterials[getColorIndex(sVerticalPosMaterials.length, limit, value)];
        }
    }

    public static void refreshTitle() {
        var moduleInfo = Modules.getDefault().ownerOf(ButterflyHelper.class);
        var buildVersion = moduleInfo.getBuildVersion();

        var buildDate = "%s.%s.%s".formatted(
                StringUtils.mid(buildVersion, 0, 4),
                StringUtils.mid(buildVersion, 4, 2),
                StringUtils.mid(buildVersion, 6, 2)
        );

        var title = "Mapton Butterfly v%s".formatted(buildDate);

        try {
            var fileDate = ButterflyManager.getInstance().getFileDate();
            title = title + new SimpleDateFormat(" (yyyy-MM-dd HH.mm.ss)").format(fileDate);
        } catch (Exception e) {
            //nvm
        }

        var finalTitle = title;
        SwingHelper.runLater(() -> WindowManager.getDefault().getMainWindow().setTitle(finalTitle));
    }
}
