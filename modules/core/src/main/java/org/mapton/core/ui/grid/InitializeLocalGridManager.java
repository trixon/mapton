/*
 * Copyright 2022 Patrik Karlström.
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
package org.mapton.core.ui.grid;

import java.io.IOException;
import java.util.ArrayList;
import org.mapton.api.MCoordinateFile;
import org.mapton.api.MLocalGridManager;
import org.mapton.api.Mapton;
import org.openide.modules.OnStart;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
@OnStart
public class InitializeLocalGridManager implements Runnable {

    @Override
    public void run() {
        var manager = MLocalGridManager.getInstance();
        try {
            manager.getItems().setAll(manager.loadItems());
        } catch (Exception e) {
        }

        Mapton.getGlobalState().addListener(gsce -> {
            ArrayList<MCoordinateFile> coordinateFiles = gsce.getValue();
            for (var coordinateFile : coordinateFiles) {
                try {
                    manager.gridImport(coordinateFile.getFile());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            manager.save();
        }, GridFileOpener.class.getName());
    }
}
