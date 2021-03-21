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
package org.mapton.core.ui.bookmark;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import se.trixon.almond.nbp.fx.FxDialogPanel;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ColorPanel extends FxDialogPanel {

    private ColorView mColorView;

    public String getColor() {
        return mColorView.getColor();
    }

    @Override
    protected void fxConstructor() {
        mColorView = new ColorView();
        setScene(new Scene(mColorView));
    }

    class ColorView extends StackPane {

        private ColorPicker mColorPicker;

        public ColorView() {
            createUI();
        }

        public String getColor() {
            return FxHelper.colorToHexRGB(mColorPicker.getValue());
        }

        private void createUI() {
            mColorPicker = new ColorPicker();

            Label colorLabel = new Label(Dict.COLOR.toString());

            VBox box = new VBox(
                    colorLabel,
                    mColorPicker
            );

            box.setPadding(new Insets(8, 16, 0, 16));

            final Insets topInsets = new Insets(8, 0, 8, 0);
            VBox.setMargin(colorLabel, topInsets);

            getChildren().setAll(box);
        }
    }
}
