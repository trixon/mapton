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
package org.mapton.base.context_menu.copy;

import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MContextMenuItem;
import org.mapton.api.MOptions;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BaseLatLon extends MContextMenuItem {

    private final MOptions mOptions = MOptions.getInstance();

    @Override
    public ContextType getType() {
        return ContextType.COPY;
    }

    protected Locale getLocale() {
        return mOptions.getDecimalSymbol().equalsIgnoreCase(".") ? Locale.ENGLISH : Locale.forLanguageTag("sv");
    }

    protected String getSeparator() {
        return StringUtils.replaceEach(mOptions.getCoordinateSeparator(),
                new String[]{"SPACE", "TAB"},
                new String[]{" ", "\t"}
        );
    }
}
