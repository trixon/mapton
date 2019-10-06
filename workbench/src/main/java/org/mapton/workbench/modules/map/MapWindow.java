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
package org.mapton.workbench.modules.map;

import javafx.beans.value.ObservableValue;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.mapton.api.MOptions2;
import org.mapton.api.Mapton;

/**
 *
 * @author Patrik Karlström
 */
public class MapWindow extends StackPane {

    private Circle mCrosshair = new Circle(30);

    public static MapWindow getInstance() {
        return Holder.INSTANCE;
    }

    private MapWindow() {
        getChildren().setAll(
                Mapton.getEngine().getUI(),
                mCrosshair
        );

        mCrosshair.setFill(Color.TRANSPARENT);
        mCrosshair.setStroke(Color.RED);
        mCrosshair.visibleProperty().bind(MOptions2.getInstance().general().displayCrosshairProperty());

        MOptions2.getInstance().general().engineProperty().addListener((ObservableValue<? extends String> ov, String t, String t1) -> {
            getChildren().setAll(Mapton.getEngine().getUI());
        });
    }

    private static class Holder {

        private static final MapWindow INSTANCE = new MapWindow();
    }
}
