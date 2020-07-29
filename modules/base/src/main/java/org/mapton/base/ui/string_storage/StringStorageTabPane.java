/*
 * Copyright 2020 Patrik Karlström.
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
package org.mapton.base.ui.string_storage;

import javafx.scene.control.TabPane;
import org.mapton.api.MStringStorage;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class StringStorageTabPane extends TabPane {

    public StringStorageTabPane() {
        getTabs().addAll(
                new StringStorageTab(MStringStorage.ApiKey.class, "API KEY"),
                new StringStorageTab(MStringStorage.Path.class, Dict.PATH.toString()),
                new StringStorageTab(MStringStorage.Url.class, "URL"),
                new StringStorageTab(MStringStorage.Misc.class, Dict.MISCELLANEOUS.toString())
        );
    }

    public void load() {
        getTabs().stream().filter(tab -> (tab instanceof StringStorageTab)).forEachOrdered(tab -> {
            ((StringStorageTab) tab).load();
        });
    }

    public void store() {
        getTabs().stream().filter(tab -> (tab instanceof StringStorageTab)).forEachOrdered(tab -> {
            ((StringStorageTab) tab).store();
        });
    }
}
