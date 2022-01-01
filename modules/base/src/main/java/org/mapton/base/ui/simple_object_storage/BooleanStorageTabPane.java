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

import org.mapton.api.MSimpleObjectStorageBoolean;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class BooleanStorageTabPane extends BaseTabPane {

    public BooleanStorageTabPane() {
        getTabs().addAll(
                new BooleanStorageTab(MSimpleObjectStorageBoolean.UpdaterAutoUpdate.class, mBundle.getString("automaticUpdates")),
                new BooleanStorageTab(MSimpleObjectStorageBoolean.Misc.class, Dict.MISCELLANEOUS.toString())
        );
    }
}
