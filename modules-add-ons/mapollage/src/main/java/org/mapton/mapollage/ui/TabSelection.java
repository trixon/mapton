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
package org.mapton.mapollage.ui;

import javafx.scene.layout.GridPane;
import org.mapton.mapollage.api.Mapo;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class TabSelection extends TabBase {

    public TabSelection(Mapo mapo) {
        setText(Dict.SELECTION.toString());
        mMapo = mapo;
        createUI();
//        initListeners();
//        load();
    }

    private void createUI() {
        GridPane gp = new GridPane();
        setScrollPaneContent(gp);
    }

}
