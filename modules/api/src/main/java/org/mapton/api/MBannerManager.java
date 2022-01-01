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
package org.mapton.api;

import java.util.HashSet;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;

/**
 *
 * @author Patrik Karlström
 */
public class MBannerManager {

    private final ObjectProperty<Node> mGraphicProperty = new SimpleObjectProperty<>();
    private final StringProperty mMessageProperty = new SimpleStringProperty();
    private final HashSet<String> mMessagesSet = new HashSet<>();

    public static MBannerManager getInstance() {
        return Holder.INSTANCE;
    }

    private MBannerManager() {
    }

    public Node getGraphic() {
        return mGraphicProperty.get();
    }

    public String getMessage() {
        return mMessageProperty.get();
    }

    public ObjectProperty<Node> graphicProperty() {
        return mGraphicProperty;
    }

    public StringProperty messageProperty() {
        return mMessageProperty;
    }

    public void setGraphic(Node graphic) {
        mGraphicProperty.set(graphic);
    }

    public void setMessage(String message) {
        mMessageProperty.set(null);
        mMessageProperty.set(message);
    }

    private static class Holder {

        private static final MBannerManager INSTANCE = new MBannerManager();
    }
}
