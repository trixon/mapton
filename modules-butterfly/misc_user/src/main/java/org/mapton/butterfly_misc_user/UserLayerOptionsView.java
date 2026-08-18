/*
 * Copyright 2023 Patrik Karlström.
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
package org.mapton.butterfly_misc_user;

import javafx.scene.layout.GridPane;
import org.apache.commons.lang3.Strings;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_core.api.BLayerOptionsView;
import org.mapton.butterfly_core.api.LabelBy;
import org.mapton.butterfly_misc_user.graphics.GraphicItem;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;
import se.trixon.almond.util.fx.session.SessionComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class UserLayerOptionsView extends BLayerOptionsView {

    private final SessionComboBox<UserColorBy> mColorScb = new SessionComboBox<>();
    private final SessionCheckComboBox<GraphicItem> mGraphicSccb = new SessionCheckComboBox<>();
    private final UserLayerOptions mLayerOptions = UserLayerOptions.getInstance();
    private final SessionComboBox<UserPointBy> mPointScb = new SessionComboBox<>();

    public UserLayerOptionsView(UserLayerBundle layerBundle) {
        super(layerBundle, Bundle.CTL_UserAction(), UserLayerOptions.getInstance(), "xyz");
        createUI();

        initListerners();
        initSession();
    }

    public IndexedCheckModel<GraphicItem> getGraphicsCheckModel() {
        return mGraphicSccb.getCheckModel();
    }

    private void createUI() {
        mPointScb.getItems().setAll(UserPointBy.values());
        mColorScb.getItems().setAll(UserColorBy.values());
        mColorScb.setDisable(true);

        mGraphicSccb.setTitle(Dict.GRAPHICS.toString());
        mGraphicSccb.setShowCheckedCount(true);
        mGraphicSccb.getItems().setAll(GraphicItem.values());

        LabelBy.populateMenuButton(mLabelMenuButton, mLayerOptions.labelByOperationProperty(), UserLabelBy.values());
        mLabelMenuButton.setText(mLayerOptions.getLabelFromId(UserLabelBy.class, mLayerOptions.labelByProperty().get().name(), UserLayerOptions.DEFAULT_LABEL_BY));

        int row = 0;
        var gp = createGridPane();
        gp.addRow(row++, mPointLabel, mColorLabel);
        gp.addRow(row++, mPointScb, mColorScb);
        gp.addRow(row++, mLabelLabel);
        gp.add(mLabelMenuButton, 0, row++, GridPane.REMAINING, 1);
        gp.addRow(row++, mGraphicLabel);
        gp.add(mGraphicSccb, 0, row++, GridPane.REMAINING, 1);

        FxHelper.autoSizeRegionHorizontal(mPointScb, mColorScb, mLabelMenuButton, mGraphicSccb);
        activateAnnotation();

        setCenter(gp);
    }

    private void initListerners() {
        mLayerOptions.labelByOperationProperty().addListener((p, o, n) -> {
            var n2 = (LabelBy.Operations) n;
            for (var labelBy : UserLabelBy.values()) {
                if (Strings.CS.equals(n2.getName(), labelBy.getName())) {
                    mLayerOptions.labelByProperty().set(labelBy);
                    break;
                }
            }
        });

        mLayerOptions.labelByProperty().addListener((p, o, n) -> {
            mLabelMenuButton.setText(n.getFullName());
        });
    }

    private void initSession() {
        mPointScb.valueProperty().bindBidirectional(mLayerOptions.pointProperty());
        mColorScb.valueProperty().bindBidirectional(mLayerOptions.colorByProperty());
        mGraphicSccb.checkedStringProperty().bindBidirectional(mLayerOptions.graphicsProperty());

        initSession(mLayerOptions);
    }
}
