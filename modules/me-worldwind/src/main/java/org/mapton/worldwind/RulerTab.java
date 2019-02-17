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
package org.mapton.worldwind;

import gov.nasa.worldwind.WorldWindowGLDrawable;
import gov.nasa.worldwind.util.measure.MeasureTool;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class RulerTab extends Tab {

    private final MeasureTool mMeasureTool;
    private final WorldWindowGLDrawable mWwd;
    private ComboBox<String> mShapeComboBox;
    private BorderPane mBorderPane;

    public RulerTab(String title, WorldWindowGLDrawable wwd, MeasureTool measureTool) {
        super(title);
        mWwd = wwd;
        mMeasureTool = measureTool;

        createUI();
        initListeners();
        postInit();
    }

    public MeasureTool getMeasureTool() {
        return mMeasureTool;
    }

    private void createUI() {
        mShapeComboBox = new ComboBox<>();
        mShapeComboBox.getItems().setAll(
                Dict.Shape.LINE.toString(),
                Dict.Shape.PATH.toString(),
                Dict.Shape.POLYGON.toString(),
                Dict.Shape.CIRCLE.toString(),
                Dict.Shape.ELLIPSE.toString(),
                Dict.Shape.SQUARE.toString(),
                Dict.Shape.RECTANGLE.toString()
        );
        Button armButton = new Button("arm");
        armButton.setOnAction((event) -> {
            mMeasureTool.setArmed(!mMeasureTool.isArmed());
        });
        VBox box = new VBox(armButton);
        mBorderPane = new BorderPane(box);
        mBorderPane.setTop(mShapeComboBox);
        mShapeComboBox.prefWidthProperty().bind(mBorderPane.widthProperty());

        setContent(mBorderPane);

    }

    private void initListeners() {
        String shapes[] = {
            MeasureTool.SHAPE_LINE,
            MeasureTool.SHAPE_PATH,
            MeasureTool.SHAPE_POLYGON,
            MeasureTool.SHAPE_CIRCLE,
            MeasureTool.SHAPE_ELLIPSE,
            MeasureTool.SHAPE_SQUARE,
            MeasureTool.SHAPE_QUAD
        };

        mShapeComboBox.setOnAction((event) -> {
            mMeasureTool.setMeasureShapeType(shapes[mShapeComboBox.getSelectionModel().getSelectedIndex()]);
        });
    }

    private void postInit() {
        mShapeComboBox.getSelectionModel().select(0);
    }
}
