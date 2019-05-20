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
package org.mapton.api;

import java.util.prefs.PreferenceChangeEvent;
import javax.swing.SwingUtilities;
import se.trixon.almond.nbp.fx.FxTopComponent;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MTopComponent extends FxTopComponent {

    protected final MOptions mMOptions = MOptions.getInstance();
    private boolean mPopOverHolder;

    public MTopComponent() {
        mMOptions.getPreferences().addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            switch (evt.getKey()) {
                case MOptions.KEY_PREFER_POPOVER:
                    if (mPopOverHolder) {
                        SwingUtilities.invokeLater(() -> {
                            if (mMOptions.isPreferPopover()) {
                                close();
                            } else {
                                open();
                                requestActive();
                            }
                        });
                    }

                    break;

                default:
                    break;
            }
        });
    }

    public boolean isPopOverHolder() {
        return mPopOverHolder;
    }

    public void setPopOverHolder(boolean popOverHolder) {
        mPopOverHolder = popOverHolder;
    }

}
