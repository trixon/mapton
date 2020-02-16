/*
 * Copyright 2020 Patrik Karlström.
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
package org.mapton.base;

import javafx.stage.FileChooser.ExtensionFilter;
import org.openide.modules.OnStart;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
@OnStart
public class Initializer implements Runnable {

    @Override
    public void run() {
        FxHelper.runLaterDelayed(10, () -> {
            var map = se.trixon.almond.util.fx.dialogs.SimpleDialog.getExtensionFilters();
            map.put("*", new ExtensionFilter(Dict.ALL_FILES.toString(), "*"));
            map.put("csv", new ExtensionFilter("Comma-separated value (*.csv)", "*.csv"));
            map.put("geo", new ExtensionFilter("SBG Geo (*.geo)", "*.geo"));
            map.put("json", new ExtensionFilter("JSON (*.json)", "*.json"));
            map.put("kml", new ExtensionFilter("Keyhole Markup Language (*.kml)", "*.kml"));
            map.put("grid", new ExtensionFilter("Mapton Grid (*.grid)", "*.grid"));
        });
    }

}
