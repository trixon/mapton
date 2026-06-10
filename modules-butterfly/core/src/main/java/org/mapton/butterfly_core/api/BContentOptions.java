/*
 * Copyright 2026 Patrik Karlström.
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
package org.mapton.butterfly_core.api;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.prefs.Preferences;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.OptionsBase;
import se.trixon.almond.util.fx.BindingHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BContentOptions<T> extends OptionsBase {

    public static final Integer DEFAULT_LIST_LIMIT = 0;
    public static final BListSortOrder DEFAULT_LIST_SORT_ORDER = BListSortOrder.STANDARD;
    private final IntegerProperty mListLimitProperty = new SimpleIntegerProperty(DEFAULT_LIST_LIMIT);
    private final ObjectProperty<BListSortOrder> mListSortOrderProperty = new SimpleObjectProperty<>(DEFAULT_LIST_SORT_ORDER);
    private final StringProperty mListSortOrderProxyProperty;

    public BContentOptions() {
        mListSortOrderProxyProperty = BindingHelper.createStringEnumProxyProperty(mListSortOrderProperty, BListSortOrder.class);
    }

    public int getListLimit() {
        var value = mListLimitProperty.get();
        if (value > 0) {
            return value;
        } else {
            return Integer.MAX_VALUE;
        }
    }

    public int getListLimitRaw() {
        return mListLimitProperty.get();
    }

    public BListSortOrder getListSortOrder() {
        return mListSortOrderProperty.get();
    }

    public Preferences getPreferencesForPath() {
        return NbPreferences.forModule(getClass()).node("contentOptionPresets");
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        var prefix = "common.";
        sessionManager.register(prefix + "listSortOrder", mListSortOrderProxyProperty);
        sessionManager.register(prefix + "listLimit", mListLimitProperty);
    }

    public IntegerProperty listLimitProperty() {
        return mListLimitProperty;
    }

    public ObjectProperty<BListSortOrder> listSortOrderProperty() {
        return mListSortOrderProperty;
    }

}
