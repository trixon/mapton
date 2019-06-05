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

import javafx.geometry.Insets;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Region;
import org.mapton.mapollage.api.Mapo;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class Tabs extends TabPane {

    private final Mapo mMapo = Mapo.getInstance();
    private TabPath mPathTab;
    private TabSources mSourceTab;

    public Tabs() {
        createUI();
    }

    public Mapo getMapo() {
        return mMapo;
    }

    private void createUI() {
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

        mPathTab = new TabPath(mMapo);
        mSourceTab = new TabSources(mMapo);

        getTabs().setAll(
                mSourceTab,
                mPathTab
        );

        final int size = 8;
        getTabs().forEach((tab) -> {
            Insets insets;
            if (tab == mSourceTab) {
                insets = new Insets(0, size, size, size);
            } else {
                insets = new Insets(size);
            }

            FxHelper.setPadding(insets, (Region) tab.getContent());
        });
    }
}
