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
package se.trixon.mapton.core.ui;

import java.util.stream.Stream;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.CheckListView;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import se.trixon.mapton.api.MDict;
import se.trixon.mapton.api.MEngine;
import se.trixon.mapton.api.MOptions;
import se.trixon.mapton.api.Mapton;

/**
 *
 * @author Patrik Karlström
 */
public class LayerView extends BorderPane {

    private VBox mEngineBox;
    private Label mEngineLabel = new Label(MDict.MAP_ENGINE.toString());
    private CheckListView<String> mListView;
    private final MOptions mOptions = MOptions.getInstance();

    public LayerView() {
        createUI();
        Lookup.getDefault().lookupResult(MEngine.class).addLookupListener((LookupEvent ev) -> {
            populateEngines();
        });

    }

    private void createUI() {
        mListView = new CheckListView<>();
        mEngineBox = new VBox(8);
        mEngineBox.setPadding(new Insets(8));
        mEngineBox.backgroundProperty().bind(mListView.backgroundProperty());

        setCenter(mListView);
        setBottom(mEngineBox);

        populateEngines();
    }

    private void populateEngines() {
        final ObservableList<Node> children = mEngineBox.getChildren();
        children.clear();
        children.add(mEngineLabel);

        final ToggleGroup mapEngineToggleGroup = new ToggleGroup();
        Stream<? extends MEngine> engines = Lookup.getDefault().lookupAll(MEngine.class).stream().sorted((MEngine o1, MEngine o2) -> o1.getName().compareTo(o2.getName()));
        engines.forEach((mapEngine) -> {
            final String name = mapEngine.getName();
            final RadioButton radioButton = new RadioButton(name);
            if (StringUtils.equalsIgnoreCase(name, mOptions.getEngine())) {
                radioButton.setSelected(true);
            }

            radioButton.setToggleGroup(mapEngineToggleGroup);
            radioButton.setOnAction((event) -> {
                switchEngine(mapEngine);
            });

            children.add(radioButton);
        });
    }

    private void switchEngine(MEngine newEngine) {
        AppStatusPanel.getInstance().getProvider().setMessage("");

        final MEngine oldEngine = Mapton.getEngine();
        try {
            mOptions.setMapZoom(oldEngine.getZoom());
            mOptions.setMapCenter(oldEngine.getCenter());
        } catch (NullPointerException e) {
        }

        mOptions.setEngine(newEngine.getName());
    }
}
