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
package org.mapton.core_sw.ui.bookmark;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.VBox;
import se.trixon.almond.nbp.fx.FxDialogPanel;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class BookmarkZoomPanel extends FxDialogPanel {

    private Spinner<Double> mZoomSpinner;

    public double getZoom() {
        return mZoomSpinner.getValue();
    }

    @Override
    protected void fxConstructor() {
        setScene(createScene());
    }

    private Scene createScene() {
        Label colorLabel = new Label(Dict.ZOOM.toString());
        mZoomSpinner = new Spinner<>(0.0, 1.0, 0.5, 0.1);
        mZoomSpinner.setEditable(true);
        FxHelper.autoCommitSpinner(mZoomSpinner);

        VBox box = new VBox(
                colorLabel,
                mZoomSpinner
        );

        box.setPadding(new Insets(8, 16, 0, 16));

        Insets topInsets = new Insets(8, 0, 8, 0);
        VBox.setMargin(colorLabel, topInsets);

        return new Scene(box);
    }
}
