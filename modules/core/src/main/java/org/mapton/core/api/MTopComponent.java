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
package org.mapton.core.api;

import javafx.beans.value.ObservableValue;
import javax.swing.SwingUtilities;
import org.mapton.api.MOptions;
import org.mapton.api.Mapton;
import se.trixon.almond.nbp.fx.FxTopComponent;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MTopComponent extends FxTopComponent {

    protected final MOptions mMOptions = Mapton.options();
    private boolean mPopOverHolder;

    public MTopComponent() {
        mMOptions.preferPopoverProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
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
        });
    }

    public boolean isPopOverHolder() {
        return mPopOverHolder;
    }

    public void setPopOverHolder(boolean popOverHolder) {
        mPopOverHolder = popOverHolder;
    }

}
