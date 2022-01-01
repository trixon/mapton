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
package org.mapton.base.ui.simple_object_storage;

import java.util.ResourceBundle;
import javafx.scene.control.TabPane;
import org.mapton.api.MGenericLoader;
import org.mapton.api.MGenericSaver;
import org.openide.util.NbBundle;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BaseTabPane extends TabPane implements MGenericLoader<Object>, MGenericSaver<Object> {

    protected final ResourceBundle mBundle = NbBundle.getBundle(BaseTabPane.class);

    public BaseTabPane() {
    }

    @Override
    public void load(Object object) {
        getTabs().stream().filter(tab -> (tab instanceof MGenericLoader)).forEachOrdered(tab -> {
            ((MGenericLoader) tab).load(null);
        });
    }

    @Override
    public void save(Object object) {
        getTabs().stream().filter(tab -> (tab instanceof MGenericSaver)).forEachOrdered(tab -> {
            ((MGenericSaver) tab).save(null);
        });
    }
}
