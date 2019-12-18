/*
 * Copyright 2019 Patrik Karlström.
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
package org.mapton.wikipedia;

import javafx.application.Platform;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.openide.modules.OnStart;
import se.trixon.almond.util.GlobalStateChangeEvent;

/**
 *
 * @author Patrik Karlström
 */
@OnStart
public class Initializer implements Runnable {

    private WikipediaView mWikipediaView;

    public Initializer() {
        Runnable r = () -> {
            mWikipediaView = new WikipediaView();
        };
        try {
            Platform.startup(r);
        } catch (Exception e) {
            Platform.runLater(r);
        }
    }

    @Override
    public void run() {
        Mapton.getGlobalState().addListener((GlobalStateChangeEvent evt) -> {
//aaa            Almond.openTopComponent("ObjectPropertiesTopComponent", true);
            try {
                Platform.runLater(() -> {
                    Mapton.getGlobalState().put(MKey.OBJECT_PROPERTIES, mWikipediaView);
                });
            } catch (Exception e) {

            }
        }, MKey.WIKIPEDIA_ARTICLE);
    }
}
