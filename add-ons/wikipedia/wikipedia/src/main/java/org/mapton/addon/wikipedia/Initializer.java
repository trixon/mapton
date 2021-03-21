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
package org.mapton.addon.wikipedia;

import org.openide.modules.OnStart;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
@OnStart
public class Initializer implements Runnable {

    @Override
    public void run() {
        SwingHelper.runLaterDelayed(45 * 1000, () -> {
            //Pre-load but don't display
            Almond.getTopComponent("WikipediaTopComponent");
        });
    }

}
