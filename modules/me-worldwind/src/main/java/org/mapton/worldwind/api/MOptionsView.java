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
package org.mapton.worldwind.api;

import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;

/**
 *
 * @author Patrik Karlström
 */
public class MOptionsView extends org.mapton.api.ui.MOptionsView {

    private ChangeListener<Object> mChangeListener;
    private LayerBundle mLayerBundle;
    private ListChangeListener<Object> mListChangeListener;

    public MOptionsView() {
        initListeners();
    }

    public MOptionsView(LayerBundle layerBundle) {
        this();
        mLayerBundle = layerBundle;
    }

    public ChangeListener<Object> getChangeListener() {
        return mChangeListener;
    }

    public ListChangeListener<Object> getListChangeListener() {
        return mListChangeListener;
    }

    public void setLayerBundle(LayerBundle layerBundle) {
        mLayerBundle = layerBundle;
    }

    private void initListeners() {
        Runnable r = () -> {
            mLayerBundle.repaint();
        };

        mChangeListener = (p, o, n) -> {
            r.run();
        };

        mListChangeListener = c -> {
            r.run();
        };
    }

}
