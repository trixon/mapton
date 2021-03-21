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
package org.mapton.api;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.MaskerPane;
import org.controlsfx.control.NotificationPane;
import static org.mapton.api.Mapton.getIconSizeToolBar;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MMaskerPaneBase {

    protected StackPane mBody = new StackPane();
    protected MaskerPane mMaskerPane = new MaskerPane();
    protected NotificationPane mNotificationPane = new NotificationPane();

    public MMaskerPaneBase() {
        mBody.getChildren().addAll(mNotificationPane, mMaskerPane);
        mMaskerPane.setText(Dict.PLEASE_WAIT.toString());
        mMaskerPane.setVisible(false);
    }

    public Node getNode() {
        return mBody;
    }

    public void notify(String message) {
        FxHelper.notify(message, mNotificationPane, mMaskerPane, getIconSizeToolBar());
    }

    public void setContent(Node node) {
        mNotificationPane.setContent(node);
    }
}
