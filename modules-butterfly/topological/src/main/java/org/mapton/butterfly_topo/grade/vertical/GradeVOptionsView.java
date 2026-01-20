/*
 * Copyright 2024 Patrik Karlström.
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
package org.mapton.butterfly_topo.grade.vertical;

import javafx.scene.layout.GridPane;
import org.apache.commons.lang3.Strings;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_core.api.BOptionsView;
import org.mapton.butterfly_core.api.LabelBy;
import org.mapton.butterfly_topo.grade.GradeManagerBase;
import org.mapton.butterfly_topo.grade.GradePointBy;
import org.mapton.butterfly_topo.grade.vertical.graphics.GraphicItem;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;
import se.trixon.almond.util.fx.session.SessionComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class GradeVOptionsView extends BOptionsView {

    private final SessionCheckComboBox<GraphicItem> mGraphicSccb = new SessionCheckComboBox<>();
    private final SessionComboBox<GradePointBy> mPointScb = new SessionComboBox<>();
    private final SessionComboBox<GradeVColorBy> mColorScb = new SessionComboBox<>();
    private final GradeVOptions mOptions = GradeVOptions.getInstance();

    public GradeVOptionsView(GradeVLayerBundle layerBundle) {
        super(layerBundle, NbBundle.getMessage(GradeManagerBase.class, "grade_v"), GradeVOptions.getInstance(), "gradeH");
        createUI();

        initListerners();
        initSession();
    }

    public IndexedCheckModel<GraphicItem> getGraphicsCheckModel() {
        return mGraphicSccb.getCheckModel();
    }

    private void createUI() {
        mPointScb.getItems().setAll(GradePointBy.values());
        mColorScb.getItems().setAll(GradeVColorBy.values());
        mColorScb.setDisable(true);

        mGraphicSccb.setTitle(Dict.GRAPHICS.toString());
        mGraphicSccb.setShowCheckedCount(true);
        mGraphicSccb.getItems().setAll(GraphicItem.values());

        LabelBy.populateMenuButton(mLabelMenuButton, mOptions.labelByOperationProperty(), GradeVLabelBy.values());
        mLabelMenuButton.setText(mOptions.getLabelFromId(GradeVLabelBy.class, mOptions.labelByProperty().get().name(), GradeVOptions.DEFAULT_LABEL_BY));

        int row = 0;
        var gp = createGridPane();
        gp.addRow(row++, mPointLabel, mColorLabel);
        gp.addRow(row++, mPointScb, mColorScb);
        gp.addRow(row++, mLabelLabel);
        gp.add(mLabelMenuButton, 0, row++, GridPane.REMAINING, 1);
        gp.addRow(row++, mGraphicLabel);
        gp.add(mGraphicSccb, 0, row++, GridPane.REMAINING, 1);

        FxHelper.autoSizeRegionHorizontal(mPointScb, mColorScb, mLabelMenuButton, mGraphicSccb);
//        activateAnnotation();

        setCenter(gp);
    }

    private void initListerners() {
        mOptions.labelByOperationProperty().addListener((p, o, n) -> {
            var n2 = (LabelBy.Operations) n;
            for (var labelBy : GradeVLabelBy.values()) {
                if (Strings.CS.equals(n2.getName(), labelBy.getName())) {
                    mOptions.labelByProperty().set(labelBy);
                    break;
                }
            }
        });

        mOptions.labelByProperty().addListener((p, o, n) -> {
            mLabelMenuButton.setText(n.getFullName());
        });
    }

    private void initSession() {
        mPointScb.valueProperty().bindBidirectional(mOptions.pointProperty());
        mColorScb.valueProperty().bindBidirectional(mOptions.colorByProperty());
        mGraphicSccb.checkedStringProperty().bindBidirectional(mOptions.graphicsProperty());

        initSession(mOptions);
    }
}
