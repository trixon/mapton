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
package org.mapton.core_wb.modules.map;

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import org.mapton.api.MOptions2;
import org.mapton.api.Mapton;
import org.mapton.base.ui.MapContextMenu;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.windowsystemfx.Window;
import se.trixon.windowsystemfx.WindowSystemComponent;

/**
 *
 * @author Patrik Karlström
 */
@WindowSystemComponent.Description(
        iconBase = "",
        preferredId = MapWindow.ID,
        parentId = "editor",
        position = 1
)
@ServiceProvider(service = Window.class)
public class MapWindow extends Window {

    public static final String ID = "org.mapton.core_wb.modules.map.MapWindow";
    private StackPane mNode;

    public MapWindow() {
    }

    @Override
    public Node getNode() {
        if ((mNode == null)) {
            createUI();
            new MapContextMenu();
            initListeners();
        }

        return mNode;
    }

    private void createUI() {
        mNode = new StackPane(
                Mapton.getEngine().getMapNode(),
                crosshairSegment(Side.TOP),
                crosshairSegment(Side.RIGHT),
                crosshairSegment(Side.BOTTOM),
                crosshairSegment(Side.LEFT)
        );
    }

    private Node crosshairSegment(Side side) {
        final var gap = FxHelper.getUIScaled(6);
        final var length = FxHelper.getUIScaled(6) + gap;
        final var pad = length * 1.8;
        final var h = length / 4;

        Rectangle r = new Rectangle();
        r.visibleProperty().bind(MOptions2.getInstance().general().displayCrosshairProperty());
        r.setDisable(true);

        if (side == Side.BOTTOM || side == Side.TOP) {
            r.setWidth(h);
            r.setHeight(length);
        } else {
            r.setWidth(length);
            r.setHeight(h);
        }

        r.setStroke(Color.BLACK);
        r.setFill(Color.WHITE);
        r.setStrokeWidth(1.0);
        r.setStrokeLineCap(StrokeLineCap.BUTT);

        switch (side) {
            case TOP:
                StackPane.setMargin(r, new Insets(pad, 0, 0, 0));
                break;
            case RIGHT:
                StackPane.setMargin(r, new Insets(0, pad, 0, 0));
                break;
            case BOTTOM:
                StackPane.setMargin(r, new Insets(0, 0, pad, 0));
                break;
            case LEFT:
                StackPane.setMargin(r, new Insets(0, 0, 0, pad));
                break;
        }

        return r;
    }

    private void initListeners() {
        final ObjectProperty<String> engineProperty = MOptions2.getInstance().general().engineProperty();
        engineProperty.addListener((ObservableValue<? extends String> ov, String t, String t1) -> {
            mNode.getChildren().set(0, Mapton.getEngine().getMapNode());
        });
    }
}
