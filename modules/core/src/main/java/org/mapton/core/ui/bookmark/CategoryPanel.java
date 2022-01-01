/*
 * Copyright 2022 Patrik Karlström.
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.mapton.api.MBookmarkManager;
import se.trixon.almond.nbp.fx.FxDialogPanel;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class CategoryPanel extends FxDialogPanel {

    private CategoryView mCategoryView;

    public String getCategory() {
        return mCategoryView.getCategory();
    }

    public void setCategory(String category) {
        mCategoryView.setCategory(category);
    }

    @Override
    protected void fxConstructor() {
        mCategoryView = new CategoryView();
        setScene(new Scene(mCategoryView));
    }

    class CategoryView extends StackPane {

        private ComboBox<String> mCategoryComboBox;
        private MBookmarkManager mManager = MBookmarkManager.getInstance();

        public CategoryView() {
            createUI();
        }

        public String getCategory() {
            return mCategoryComboBox.getSelectionModel().getSelectedItem();
        }

        public void setCategory(String category) {
            mCategoryComboBox.getSelectionModel().select(category);
        }

        private void createUI() {
            Label label = new Label(Dict.CATEGORY.toString());
            mCategoryComboBox = new ComboBox<>();
            mCategoryComboBox.getItems().setAll(mManager.getCategories());
            mCategoryComboBox.setEditable(true);

            VBox box = new VBox(
                    label,
                    mCategoryComboBox
            );

            box.setPadding(new Insets(8, 16, 0, 16));
            mCategoryComboBox.prefWidthProperty().bind(box.widthProperty());

            Insets topInsets = new Insets(8, 0, 8, 0);
            VBox.setMargin(label, topInsets);

            getChildren().setAll(box);
        }
    }
}
