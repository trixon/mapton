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
package org.mapton.core_nb.actions;

import java.awt.event.ActionListener;
import org.mapton.api.MOptions;
import org.mapton.api.MOptions2;
import org.openide.awt.Actions;
import org.openide.windows.WindowManager;
import se.trixon.almond.nbp.fx.FxTopComponent;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BaseAction implements ActionListener {

    protected MOptions mOptions = MOptions.getInstance();
    protected MOptions2 mOptions2 = MOptions2.getInstance();

    protected void toggleTopComponent(String id) {
        FxTopComponent tc = (FxTopComponent) WindowManager.getDefault().findTopComponent(id);

        if (mOptions.isMapOnly()) {
            tc.open();
        } else {
            tc.toggleOpened();
        }

        if (tc.isOpened()) {
            tc.requestActive();
        } else {
            Actions.forID("Window", "org.mapton.core_nb.ui.MapTopComponent").actionPerformed(null);
        }

    }

    protected boolean usePopover() {
        return mOptions2.general().isPreferPopover() || mOptions.isMapOnly();
    }

}
