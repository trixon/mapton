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
import java.time.format.DateTimeFormatter;
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
public abstract class BContentListCell<T extends BXyzPoint> extends ListCell<T> {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm");
    public static final DateTimeFormatter DATE_TIME_FORMATTER_UTC = DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm 'UTC'");

    protected final Label mHeaderLabel = new Label();
    protected final String mStyleBold = "-fx-font-weight: bold;";
    protected final String mStyleMono = "-fx-font-family: monospace;";
    protected final VBox mVBox = new VBox();
    private final Tooltip mTooltip = new Tooltip();

    public BContentListCell() {
        mHeaderLabel.setStyle(mStyleBold);
        mHeaderLabel.setGraphicTextGap(FxHelper.getUIScaled(8));
    }

    public String getDateLatestAndFirst(T p) {
        var dateLatest = p.extOrNull().getDateLatest();
        var dateFirst = p.extOrNull().getDateFirst();
        var latest = dateLatest != null ? dateLatest.format(DATE_TIME_FORMATTER) : "-";
        var first = dateFirst != null ? dateFirst.toLocalDate().toString() : "-";
        var date = "%s (%s)".formatted(latest, first);

        return date;
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

    public String getDateLatestAndZero(T p, boolean addDebtIndicator) {
        var dateLatest = p.extOrNull().getDateLatest();
        var latest = dateLatest != null ? dateLatest.format(DATE_TIME_FORMATTER) : "-";
        var zeroDate = p.getDateZero();
        var zero = zeroDate != null ? zeroDate.toString() : "-";

        var debtIndicator = "";
        //TODO implement addDebtIndicator
//        var sign = "⇐";

//                var dateSB = new StringBuilder(StringHelper.toString(p.getDateLatest() == null ? null : p.getDateLatest().toLocalDate(), "NOVALUE"));
//        LocalDate nextDate = null;
//        if (nextDate != null) {
//            dateSB.append(" (").append(nextDate.toString()).append(")");
//            if (nextDate.isBefore(LocalDate.now())) {
//                dateSB.append(" ").append(sign);
//            }
//        }
        var date = "%s (%s)%s".formatted(latest, zero, debtIndicator);

        return date;
    }

    protected void activateTooltip() {
        Tooltip.install(this, mTooltip);
        mTooltip.setShowDelay(Duration.seconds(2));
    }

    protected abstract void addContent(T p);

    protected String getHeader(BXyzPoint p) {
        return getHeader(p, true);
    }

    protected String getHeader(BXyzPoint p, boolean withStatus) {
        var header = "%s  %s".formatted(p.getOrigin(), p.getName());
        var sta = p.getStatus();

        if (withStatus && StringUtils.isNotBlank(sta)) {
            header = "%s [%s]".formatted(header, sta);
        }

        return header;
    }

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
