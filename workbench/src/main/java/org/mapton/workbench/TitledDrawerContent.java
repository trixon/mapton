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
package org.mapton.workbench;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import org.mapton.api.MActivatable;

/**
 *
 * @author Patrik Karlström
 */
public class TitledDrawerContent extends BorderPane implements MActivatable {

    private final Node mNode;
    private final String mTitle;

    public TitledDrawerContent(String title, Node node) {
        mTitle = title;
        mNode = node;

        Label label = new Label(mTitle);
        label.setFont(Font.font(Font.getDefault().getSize() * 2));
        label.setPadding(new Insets(12));
        setTop(label);

        StackPane stackPane = new StackPane(node);
        stackPane.setPadding(new Insets(2));
        setCenter(stackPane);

    }

    @Override
    public void activate() {
        if (mNode instanceof MActivatable) {
            ((MActivatable) mNode).activate();
        }
    }

    public Node getNode() {
        return mNode;
    }

    public String getTitle() {
        return mTitle;
    }
}
