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
package org.mapton.gmapsfx;

import com.dlsc.gmapsfx.javascript.object.MapTypeIdEnum;
import java.util.ArrayList;
import java.util.Collections;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import static org.mapton.gmapsfx.ModuleOptions.*;
import org.mapton.gmapsfx.api.MapStyle;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class StyleView extends HBox {

    private final ModuleOptions mOptions = ModuleOptions.getInstance();
    private final VBox mTypeBox = new VBox(16);
    private final VBox mStyleBox = new VBox(16);

    public StyleView() {
        setSpacing(16);
        setPadding(new Insets(8, 16, 16, 16));
        mTypeBox.setPrefWidth(200);
        mStyleBox.setPrefWidth(200);

        Lookup.getDefault().lookupResult(MapStyle.class).addLookupListener((LookupEvent ev) -> {
            initStyle();
        });

        initType();
        initStyle();

        getChildren().addAll(mTypeBox, new Separator(Orientation.VERTICAL), mStyleBox);
    }

    private void initStyle() {
        Platform.runLater(() -> {
            ToggleGroup group = new ToggleGroup();

            mStyleBox.getChildren().clear();
            ArrayList< MapStyle> styles = new ArrayList<>(Lookup.getDefault().lookupAll(MapStyle.class));
            Collections.sort(styles, (MapStyle o1, MapStyle o2) -> o1.getName().compareTo(o2.getName()));

            for (MapStyle mapStyle : styles) {
                ToggleButton button = new ToggleButton(mapStyle.getName());
                button.prefWidthProperty().bind(widthProperty());
                button.setToggleGroup(group);
                button.setOnAction((ActionEvent event) -> {
                    MapTypeIdEnum type = mOptions.getMapType();
                    mOptions.put(KEY_MAP_STYLE, mapStyle.getName());
                    if (MapTypeIdEnum.ROADMAP != type) {
                        mOptions.setMapType(MapTypeIdEnum.ROADMAP);
                        mOptions.setMapType(type);
                    }
                });

                mStyleBox.getChildren().add(button);
            }
        });
    }

    private void initType() {
        ToggleButton hybridToggleButton = new ToggleButton(Dict.MAP_TYPE_HYBRID.toString());
        ToggleButton terrainToggleButton = new ToggleButton(Dict.MAP_TYPE_TERRAIN.toString());
        ToggleButton roadmapToggleButton = new ToggleButton(Dict.MAP_TYPE_ROADMAP.toString());
        ToggleButton satelliteToggleButton = new ToggleButton(Dict.MAP_TYPE_SATELLITE.toString());

        hybridToggleButton.setOnAction((ActionEvent event) -> {
            mOptions.setMapType(MapTypeIdEnum.HYBRID);
        });

        terrainToggleButton.setOnAction((ActionEvent event) -> {
            mOptions.setMapType(MapTypeIdEnum.TERRAIN);
        });

        roadmapToggleButton.setOnAction((ActionEvent event) -> {
            mOptions.setMapType(MapTypeIdEnum.ROADMAP);
        });

        satelliteToggleButton.setOnAction((ActionEvent event) -> {
            mOptions.setMapType(MapTypeIdEnum.SATELLITE);
        });

        mTypeBox.getChildren().addAll(
                roadmapToggleButton,
                satelliteToggleButton,
                hybridToggleButton,
                terrainToggleButton
        );

        ToggleGroup group = new ToggleGroup();
        mTypeBox.getChildren().forEach((node) -> {
            ToggleButton button = (ToggleButton) node;
            button.prefWidthProperty().bind(widthProperty());
            button.setToggleGroup(group);
        });
    }
}
