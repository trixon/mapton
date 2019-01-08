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
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.text.Font;
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

    public Label createTitle(String title) {
        return createTitle(title, Mapton.getThemeBackground());
    }

    public Label createTitle(String title, Background background) {
        Label label = new Label(title);
        Font defaultFont = Font.getDefault();

        label.setBackground(background);
        label.setAlignment(Pos.BASELINE_CENTER);
        label.setFont(new Font(defaultFont.getSize() * 1.4));

        return label;
    }

    public boolean isPopOverHolder() {
        return mPopOverHolder;
    }

    public void setPopOverHolder(boolean popOverHolder) {
        mPopOverHolder = popOverHolder;
    }

}
