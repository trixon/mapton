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
package se.trixon.mapton.core.map;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import se.trixon.mapton.core.api.MapStyleProvider;
import se.trixon.mapton.core.api.MaptonOptions;

/**
 *
 * @author Patrik Karlström
 */
public class StyleView extends VBox {

    private final MaptonOptions mOptions = MaptonOptions.getInstance();

    public StyleView() {
        setSpacing(16);
        setPadding(new Insets(16));
        setPrefWidth(200);
        Lookup.getDefault().lookupResult(MapStyleProvider.class).addLookupListener((LookupEvent ev) -> {
            update();
        });

        update();
    }

    public void update() {
        Platform.runLater(() -> {
            getChildren().clear();
            for (MapStyleProvider mapStyle : Lookup.getDefault().lookupAll(MapStyleProvider.class)) {
                Button button = new Button(mapStyle.getName());
                button.setOnAction((ActionEvent event) -> {
                    mOptions.setMapStyle(mapStyle.getName());
                });
                button.prefWidthProperty().bind(widthProperty());

                getChildren().add(button);
            }
        });
    }
}
