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
package org.mapton.wms.lantmateriet;

import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MReplacer;
import org.mapton.api.MServiceKeyManager;
import org.mapton.api.MSimpleObjectStorageManager;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MReplacer.class)
public class Replacer extends MReplacer {

    @Override
    public String getKey() {
        return "25f6210a-442e-4597-adbd-2f86fcf63846";
    }

    @Override
    public String getValue() {
        var value = MSimpleObjectStorageManager.getInstance().getString(ApiKeyProvider.class, null);
        if (StringUtils.isBlank(value)) {
            value = MServiceKeyManager.getInstance().getKey("003");
        }

        return value;
    }
}
