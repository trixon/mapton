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
package org.mapton.butterfly_remote.insar;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author Patrik Karlström
 */
public class OptionsManager {

    private final ObjectProperty<InsarColorBy> mColorProperty = new SimpleObjectProperty<>();

    private OptionsManager() {
    }

    public static OptionsManager getInstance() {
        return OptionsManagerHolder.INSTANCE;
    }

    public ObjectProperty<InsarColorBy> colorProperty() {
        return mColorProperty;
    }

    private static class OptionsManagerHolder {

        private static final OptionsManager INSTANCE = new OptionsManager();
    }
}
