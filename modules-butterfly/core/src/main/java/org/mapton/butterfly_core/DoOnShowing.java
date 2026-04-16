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
package org.mapton.butterfly_core;

import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MOptions;
import org.mapton.butterfly_core.api.AlarmHistoryManager;
import org.mapton.butterfly_core.api.ButterflyHelper;
import org.openide.modules.Modules;
import org.openide.windows.OnShowing;
import org.openide.windows.WindowManager;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
@OnShowing
public class DoOnShowing implements Runnable {

    private final MOptions mOptions = MOptions.getInstance();

    @Override
    public void run() {
        if (mOptions.isFirstRun()) {
            Almond.openTopComponent("LayerPropertiesTopComponent");
            Almond.openTopComponent("TrendsTopComponent");
            Almond.openTopComponent("PoiTopComponent");
            Almond.openTopComponent("LayerTopComponent");
            Almond.openTopComponent("AreaTopComponent");
            Almond.openTopComponent("PropertiesTopComponent");
            Almond.openTopComponent("ChartTopComponent");
            Almond.openTopComponent("TopoTopComponent");
        }

        var moduleInfo = Modules.getDefault().ownerOf(ButterflyHelper.class);
        var buildVersion = moduleInfo.getBuildVersion();

        var buildDate = "%s.%s.%s".formatted(
                StringUtils.mid(buildVersion, 0, 4),
                StringUtils.mid(buildVersion, 4, 2),
                StringUtils.mid(buildVersion, 6, 2)
        );

        var title = "Mapton Butterfly v%s".formatted(buildDate);
        SwingHelper.runLater(() -> WindowManager.getDefault().getMainWindow().setTitle(title));

        AlarmHistoryManager.getInstance();
    }

}
