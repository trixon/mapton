/*
 * Copyright 2025 Patrik Karlström.
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
package org.mapton.butterfly_core.api;

import java.time.LocalDate;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.mapton.butterfly_format.types.BXyzPoint;
import se.trixon.almond.util.StringHelper;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BListCell<T extends BXyzPoint> extends ListCell<T> {

    protected final Label mHeaderLabel = new Label();
    protected final String mStyleBold = "-fx-font-weight: bold;";
    protected final String mStyleMono = "-fx-font-family: monospace;";
    protected final VBox mVBox = new VBox();
    private final Tooltip mTooltip = new Tooltip();

    public BListCell() {
        mHeaderLabel.setStyle(mStyleBold);
        mHeaderLabel.setGraphicTextGap(FxHelper.getUIScaled(8));
    }

    public String getDateLatestAndNext(T p) {
        var sb = new StringBuilder(StringHelper.toString(p.getDateLatest() == null ? null : p.getDateLatest().toLocalDate(), "NOVALUE"));
        var nextDate = p.extOrNull().getObservationRawNextDate();
        if (nextDate != null) {
            var sign = "⇐";
            sb.append(" (").append(nextDate.toString()).append(")");
            if (nextDate.isBefore(LocalDate.now())) {
                sb.append(" ").append(sign);
            }
        }

        return sb.toString();
    }

    protected void activateTooltip() {
        Tooltip.install(this, mTooltip);
        mTooltip.setShowDelay(Duration.seconds(2));
    }

    protected abstract void addContent(T p);

    protected void loadTooltip(T p) {
        var showDelay = Integer.MAX_VALUE;
        var text = "";

        if (StringUtils.isNotBlank(p.getComment())) {
            showDelay = 2;
            text = Strings.CS.replace(p.getComment(), "\\n", "\r");
        }

        mTooltip.setShowDelay(Duration.seconds(showDelay));
        mTooltip.setText(text);
    }

    @Override
    protected void updateItem(T p, boolean empty) {
        super.updateItem(p, empty);
        if (p == null || empty) {
            clearContent();
        } else {
            addContent(p);
        }
    }

    private void clearContent() {
        setText(null);
        setGraphic(null);
        mTooltip.setText("");
    }

    protected abstract class BAlarmIndicator<T extends BXyzPoint> extends HBox {

        protected static final double SIZE = FxHelper.getUIScaled(12);
        protected Circle m1dShape;
        protected Polygon m2dShape;
        protected Rectangle m3dShape;

        public BAlarmIndicator() {
            super(SIZE / 4);
            m1dShape = new Circle(SIZE / 2);
            m2dShape = new Polygon();
            m2dShape.getPoints().addAll(new Double[]{
                SIZE / 2, 0.0,
                SIZE, SIZE,
                0.0, SIZE
            });
            m3dShape = new Rectangle(SIZE, SIZE);
        }

        public void addNodes(Node... nodes) {
            getChildren().clear();
            for (var node : nodes) {
                var pane = new StackPane(node);
                getChildren().add(pane);
            }
        }

        public abstract void update(T p);

    }
}
