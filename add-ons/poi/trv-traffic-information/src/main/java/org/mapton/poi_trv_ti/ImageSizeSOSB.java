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
package org.mapton.poi_trv_ti;

import java.util.ResourceBundle;
import org.mapton.api.MSimpleObjectStorageBoolean;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MSimpleObjectStorageBoolean.Misc.class)
public class ImageSizeSOSB extends MSimpleObjectStorageBoolean.Misc {

    public static final boolean DEFAULT_VALUE = true;
    private final ResourceBundle mBundle = NbBundle.getBundle(ImageSizeSOSB.class);

    public ImageSizeSOSB() {
        setGroup(mBundle.getString("name"));
        setName(mBundle.getString("imageSize"));
        setDefaultValue(DEFAULT_VALUE);
    }

}
