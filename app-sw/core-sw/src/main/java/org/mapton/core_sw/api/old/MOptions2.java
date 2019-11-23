/*
 * Copyright 2019 Patrik Karlström.
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
package org.mapton.core_sw.api.old;

/**
 *
 * @author Patrik Karlström
 */
public class MOptions2 {
    /*
    private final MOptionsGeneral mGeneralOptions = new MOptionsGeneral();
    private PreferencesFx mPreferencesFx;

    public static MOptions2 getInstance() {
        return Holder.INSTANCE;
    }

    private MOptions2() {
        new JFXPanel();
        Platform.runLater(() -> {
            createPreferences();
        });
    }

    public void createPreferences() {
        Category[] categories = Lookup.getDefault().lookupAll(MOptionsModule.class).stream()
                .sorted((MOptionsModule o1, MOptionsModule o2) -> o1.getCategory().getDescription().compareToIgnoreCase(o2.getCategory().getDescription()))
                .map(p -> p.getCategory())
                .toArray(Category[]::new);

        mPreferencesFx = PreferencesFx.of(getClass(),
                mGeneralOptions.getCategory(),
                Category.of(Dict.MODULES.toString())
                        .expand()
                        .subCategories(categories)
        ).persistWindowState(false).saveSettings(true).debugHistoryMode(false).buttonsVisibility(true);
    }

    public void discardChanges() {
        mPreferencesFx.discardChanges();
    }

    public MOptionsGeneral general() {
        return mGeneralOptions;
    }

    public PreferencesFxView getPreferencesFxView() {
        return mPreferencesFx.getView();
    }

    public void save() {
        mPreferencesFx.saveSettings();
    }

    private static class Holder {

        private static final MOptions2 INSTANCE = new MOptions2();
    }
     */
}
