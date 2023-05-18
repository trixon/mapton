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
package org.mapton.core.ui.simple_object_storage;

import java.util.ResourceBundle;
import org.mapton.api.MSimpleObjectStorageString;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class StringStorageTabPane extends BaseTabPane {

    private final ResourceBundle mBundle = NbBundle.getBundle(StringStorageTabPane.class);

    public StringStorageTabPane() {
        getTabs().addAll(
                new StringStorageTab(MSimpleObjectStorageString.ApiKey.class, mBundle.getString("api_key")),
                new StringStorageTab(MSimpleObjectStorageString.Path.class, Dict.PATH.toString()),
                new StringStorageTab(MSimpleObjectStorageString.Url.class, "URL"),
                new StringStorageTab(MSimpleObjectStorageString.Misc.class, Dict.MISCELLANEOUS.toString())
        );
    }
}
