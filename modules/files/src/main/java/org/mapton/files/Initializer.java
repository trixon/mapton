/*
 * Copyright 2021 Patrik Karlström.
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
package org.mapton.files;

import org.mapton.api.MCoordinateFileManager;
import org.mapton.api.Mapton;
import org.openide.windows.OnShowing;
import org.openide.windows.WindowManager;
import se.trixon.almond.nbp.Almond;

/**
 *
 * @author Patrik Karlström
 */
@OnShowing
public class Initializer implements Runnable {

    public Initializer() {
    }

    @Override
    public void run() {
        WindowManager.getDefault().invokeWhenUIReady(() -> {
            var coordinateFileManager = MCoordinateFileManager.getInstance();
            coordinateFileManager.load();

            Mapton.getGlobalState().addListener(gsce -> {
                Almond.openAndActivateTopComponent("FilesTopComponent");
            }, coordinateFileManager.getFileOpenerKeys());
        });
    }
}
