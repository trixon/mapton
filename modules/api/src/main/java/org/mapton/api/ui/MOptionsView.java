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
package org.mapton.api.ui;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.ResourceBundle;
import javafx.scene.layout.BorderPane;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MOptionsView extends BorderPane {

    private SessionManager mSessionManager;

    public MOptionsView() {
    }

    public SessionManager getSessionManager() {
        if (mSessionManager == null) {
            mSessionManager = new SessionManager(NbPreferences.forModule(getClass()));
        }

        return mSessionManager;
    }

    public ResourceBundle getBundle() {
        return NbBundle.getBundle(getClass());
    }

}
