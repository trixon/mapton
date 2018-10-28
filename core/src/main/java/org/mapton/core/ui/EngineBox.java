/*
 * Copyright 2018 Patrik Karlström.
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
package org.mapton.core.ui;

import java.util.stream.Stream;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.mapton.api.MDict;
import org.mapton.api.MEngine;
import org.mapton.api.MOptions;
import org.mapton.api.Mapton;

/**
 *
 * @author Patrik Karlström
 */
public class EngineBox extends VBox {

    private Label mEngineLabel = new Label(MDict.MAP_ENGINE.toString());
    private final MOptions mOptions = MOptions.getInstance();

    public EngineBox() {
        setSpacing(8);
        setPadding(new Insets(8));
        Lookup.getDefault().lookupResult(MEngine.class).addLookupListener((LookupEvent ev) -> {
            populateEngines();
        });

        populateEngines();
    }

    private void populateEngines() {
        final ObservableList<Node> children = getChildren();
        children.clear();
        children.add(mEngineLabel);

        final ToggleGroup mapEngineToggleGroup = new ToggleGroup();
        Stream<? extends MEngine> engines = Lookup.getDefault().lookupAll(MEngine.class).stream().sorted((MEngine o1, MEngine o2) -> o1.getName().compareTo(o2.getName()));
        engines.forEach((engine) -> {
            final String name = engine.getName();
            final RadioButton radioButton = new RadioButton(name);
            if (StringUtils.equalsIgnoreCase(engine.getClass().getName(), mOptions.getEngine())) {
                radioButton.setSelected(true);
            }

            radioButton.setToggleGroup(mapEngineToggleGroup);
            radioButton.setOnAction((event) -> {
                switchEngine(engine);
            });

            children.add(radioButton);
        });
    }

    private void switchEngine(MEngine newEngine) {
        AppStatusPanel.getInstance().getProvider().setMessage("");

        final MEngine oldEngine = Mapton.getEngine();
        try {
            oldEngine.onDeactivate();
            mOptions.setMapZoom(oldEngine.getZoom());
            mOptions.setMapCenter(oldEngine.getCenter());
        } catch (NullPointerException e) {
        }

        mOptions.setEngine(newEngine.getClass().getName());
    }

}
