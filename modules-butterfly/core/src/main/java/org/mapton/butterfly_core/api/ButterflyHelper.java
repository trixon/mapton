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
        Material.RED
    };

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
